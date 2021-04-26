package in.handyman.process.onethread

import java.util.Map.Entry
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.command.TryContext
import in.handyman.server.ProcessAST
import in.handyman.util.ExceptionUtil
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ResourceAccess
import org.slf4j.MarkerFactory


class ForkProcessAction extends in.handyman.command.Action with LazyLogging {

  val detailMap: java.util.Map[String, String] = new java.util.HashMap[String, String]
  val executor: ExecutorService = Executors.newCachedThreadPool();
  val auditMarker = "FORKPROCESS";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: in.handyman.dsl.Action, actionId: Integer): Context = {
    val callProcessAsIs: in.handyman.dsl.Forkprocess = action.asInstanceOf[in.handyman.dsl.Forkprocess]
    val callProcess: in.handyman.dsl.Forkprocess = CommandProxy.createProxy(callProcessAsIs, classOf[in.handyman.dsl.Forkprocess], context)

    val name = callProcess.getName
    val fileRelativePath: String = callProcess.getSource
    val processName: String = callProcess.getTarget
    val dbSrc = callProcess.getDatasource
    
    val forkBatchSize = callProcess.getForkBatchSize
    var forkBatchSizeInt : Integer = 0
    if (forkBatchSize != null && !forkBatchSize.isEmpty)
      forkBatchSizeInt = Integer.valueOf(forkBatchSize)
      
    val forkBatchMap: java.util.Map[Integer, java.util.Set[TryContext]] = new java.util.HashMap[Integer, java.util.Set[TryContext]]
    
    val sql = callProcess.getValue.replaceAll("\"", "")
    val id = context.getValue("process-id")

    logger.info("Forkprocess id#{}, name#{}, forked process#{}, calledfile#{}, db=#{}", id, name, processName, fileRelativePath, dbSrc)

    val conn = ResourceAccess.rdbmsConn(dbSrc)
    val stmt = conn.createStatement
    val rs = stmt.executeQuery(sql)
    val columnCount = rs.getMetaData.getColumnCount
    val threadCount: AtomicInteger = new AtomicInteger

    var contextSet: java.util.Set[TryContext] = new java.util.HashSet[TryContext]
    while (rs.next()) {
      val tryContext = new TryContext(new scala.collection.immutable.HashMap[String, String](), processName)
      tryContext.addValue("basepath", context.getValue("basepath"))
      for (i <- 1 until columnCount + 1) {
        val key = rs.getMetaData.getColumnLabel(i)
        val value = rs.getString(i)
        tryContext.addValue(key, value)
        
        val queryOutputKey : String = "query.output.rowId=" + rs.getRow + ".columnName=" + key
        detailMap.put(queryOutputKey, value)
      }
      contextSet.add(tryContext)
      threadCount.incrementAndGet
      
      if(forkBatchSize != null && forkBatchSizeInt > 0){
        if (threadCount.intValue() % forkBatchSizeInt == 0) {
          val batchNo : Int = threadCount.intValue() / forkBatchSizeInt
          forkBatchMap.put(batchNo, contextSet)
          
          contextSet = new java.util.HashSet[TryContext]
        }        
      }
    }
    
    if (!contextSet.isEmpty() && !forkBatchMap.isEmpty()) {
      val batchNo : Int = (threadCount.intValue() / forkBatchSizeInt) + 1
      forkBatchMap.put(batchNo, contextSet)
    }else{
      forkBatchMap.put(1, contextSet)
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
    
    var batchSize : Integer = 0;
    var batchNo : Integer = 0;
    val entrySet : java.util.Set[Entry[Integer, java.util.Set[TryContext]]] = forkBatchMap.entrySet();
    entrySet.forEach(entry => {
      batchNo = entry.getKey
      batchSize = Integer.valueOf(entry.getValue.size())
      logger.info("Started Executing Forkprocess for batch-id#{} with batch-size#{}", batchNo, batchSize)
      try{
        val countDownLatch: CountDownLatch = new CountDownLatch(batchSize);
        val tryContexSet = entry.getValue;
        tryContexSet.forEach(tryContext=>{
          val runtimeContext = ProcessAST.loadProcessAST(processName, fileRelativePath, tryContext)
          val processWorker  = new ForkProcessCallable(runtimeContext, countDownLatch);
          this.executor.submit(processWorker)
        })
        
        try {
          countDownLatch.await();
        } catch {
          case ex: InterruptedException => {
            //ex.printStackTrace()
            throw ex
          }
        }

        logger.info("Completed Executing Forkprocess for batch-id#{} with batch-size#{}", batchNo, batchSize)
    
      }catch {
        case ex: Throwable => {
          logger.error(aMarker, "Caught Exception#{} While Executing Forkprocess for batch-Id#{} with Batch-Size#{}", ex, batchNo, batchSize)
          val excepMsg : String = "Exception on Batch-Id " + batchNo + " With Batch-Size : " + batchSize
          detailMap.put(excepMsg, ExceptionUtil.completeStackTraceex(ex))
        }
      }
    })
      
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