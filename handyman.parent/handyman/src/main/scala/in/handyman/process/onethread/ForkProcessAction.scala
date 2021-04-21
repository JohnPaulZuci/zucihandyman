package in.handyman.process.onethread

import com.typesafe.scalalogging.LazyLogging
import in.handyman.command.Action
import in.handyman.command.Context
import in.handyman.util.ResourceAccess
import in.handyman.DslStandaloneSetup
import in.handyman.server.PathResolver
import in.handyman.command.CommandProxy
import in.handyman.util.ParameterisationEngine
import java.util.concurrent.atomic.AtomicInteger
import java.util.HashSet
import in.handyman.command.TryContext
import in.handyman.util.ExceptionUtil
import java.util.concurrent.CountDownLatch
import in.handyman.server.ProcessAST
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ForkProcessAction extends in.handyman.command.Action with LazyLogging {

  val detailMap: java.util.Map[String, String] = new java.util.HashMap[String, String]
  val executor: ExecutorService = Executors.newCachedThreadPool();

  def execute(context: Context, action: in.handyman.dsl.Action, actionId: Integer): Context = {
    val callProcessAsIs: in.handyman.dsl.Forkprocess = action.asInstanceOf[in.handyman.dsl.Forkprocess]
    val callProcess: in.handyman.dsl.Forkprocess = CommandProxy.createProxy(callProcessAsIs, classOf[in.handyman.dsl.Forkprocess], context)

    val name = callProcess.getName
    val fileRelativePath: String = callProcess.getSource
    val processName: String = callProcess.getTarget
    val dbSrc = callProcess.getDatasource
    val sql = callProcess.getValue.replaceAll("\"", "")
    val id = context.getValue("process-id")

    logger.info("Forkprocess id#{}, name#{}, forked process#{}, calledfile#{}, db=#{}", id, name, processName, fileRelativePath, dbSrc)

    val conn = ResourceAccess.rdbmsConn(dbSrc)
    val stmt = conn.createStatement
    val rs = stmt.executeQuery(sql)
    val columnCount = rs.getMetaData.getColumnCount
    val threadCount: AtomicInteger = new AtomicInteger

    val contextSet: java.util.Set[TryContext] = new java.util.HashSet[TryContext]
    while (rs.next()) {
      val tryContext = new TryContext(new scala.collection.immutable.HashMap[String, String](), processName)
      tryContext.addValue("basepath", context.getValue("basepath"))
      for (i <- 1 until columnCount + 1) {
        val key = rs.getMetaData.getColumnLabel(i)
        val value = rs.getString(i)
        tryContext.addValue(key, value)
        detailMap.put("query.output." + key, value)
      }
      contextSet.add(tryContext)
      threadCount.incrementAndGet
    }
    try {} finally {
      try {
        rs.close
        stmt.close
        conn.close
      } catch {
        case ex: Throwable => {
          handleError(ex)
          detailMap.put("exception", ExceptionUtil.completeStackTraceex(ex))
        }
      }
    }
    val countDownLatch: CountDownLatch = new CountDownLatch(threadCount.intValue());
    contextSet.forEach(tryContext=>{
      val runtimeContext = ProcessAST.loadProcessAST(processName, fileRelativePath, tryContext)
      val processWorker  = new ForkProcessCallable(runtimeContext, countDownLatch);
      this.executor.submit(processWorker)
    })
    try {
      countDownLatch.await();
    } catch {
      case ex: InterruptedException => {
        ex.printStackTrace()
        throw ex
      }
    }
    context
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean = {
    val callProcessAsIs: in.handyman.dsl.Forkprocess = action.asInstanceOf[in.handyman.dsl.Forkprocess]
    val callProcess: in.handyman.dsl.Forkprocess = CommandProxy.createProxy(callProcessAsIs, classOf[in.handyman.dsl.Forkprocess], context)

    val expression = callProcess.getCondition
    try {
      val output = ParameterisationEngine.doYieldtoTrue(expression)
      detailMap.putIfAbsent("condition-output", output.toString())
      output
    } finally {
      if (expression != null)
        detailMap.putIfAbsent("condition", "LHS=" + expression.getLhs + ", Operator=" + expression.getOperator + ", RHS=" + expression.getRhs)

    }
  }

  def generateAudit(): java.util.Map[String, String] = {
    detailMap
  }

  def handleError(ex: Throwable) = {
    logger.info("Error section of call process", ex)
    throw ex
  }
}