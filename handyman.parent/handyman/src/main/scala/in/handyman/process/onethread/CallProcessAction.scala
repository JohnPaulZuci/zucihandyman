package in.handyman.process.onethread

import com.typesafe.scalalogging.LazyLogging
import in.handyman.command.Action
import in.handyman.command.Context
import in.handyman.util.ResourceAccess
import in.handyman.DslStandaloneSetup
import in.handyman.server.PathResolver

import in.handyman.command.TryContext
import in.handyman.parser.antlr.DslParser
import java.io.FileReader
import in.handyman.server.ProcessExecutor
import in.handyman.command.CommandProxy
import in.handyman.server.ProcessAST
import in.handyman.util.ParameterisationEngine
import java.io.StringWriter
import java.io.PrintWriter
import in.handyman.util.ExceptionUtil

class CallProcessAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId:Integer): in.handyman.command.Context = {

    val callProcessAsIs: in.handyman.dsl.Callprocess = action.asInstanceOf[in.handyman.dsl.Callprocess]
    val callProcess: in.handyman.dsl.Callprocess = CommandProxy.createProxy(callProcessAsIs, classOf[in.handyman.dsl.Callprocess], context)

    val name = callProcess.getName
    val fileRelativePath: String = callProcess.getSource
    val processName: String = callProcess.getTarget
    val dbSrc = callProcess.getDatasource
    val sql = callProcess.getValue.replaceAll("\"", "")
    val id = context.getValue("process-id")

    logger.info("Callprocess id#{}, name#{}, calledprocess#{}, calledfile#{}, db=#{}", id, name, processName, fileRelativePath, dbSrc)

    val conn = ResourceAccess.rdbmsConn(dbSrc)
    val stmt = conn.createStatement
    val rs = stmt.executeQuery(sql)
    val columnCount = rs.getMetaData.getColumnCount

    while (rs.next()) {

      for (i <- 1 until columnCount + 1) {
        val key = rs.getMetaData.getColumnLabel(i)
        val value = rs.getString(i)
        context.addValue(key, value)
        detailMap.put("query.output." + key, value)
      }

      val runtimeContext = ProcessAST.loadProcessAST(processName, fileRelativePath, context)

      try {
        //TODO - change to create(config.get("runmode"))
        val runtime = ProcessExecutor.execute("in.handyman.process.onethread", runtimeContext)

      } catch {
        case ex: Throwable => {
          handleError(ex)
          detailMap.put("exception", ExceptionUtil.completeStackTraceex(ex))
          throw ex
        }

      } finally {
        val onFinally = runtimeContext.process.getFinally
        handleFinally()
      }
    }
    try {
      logger.info("Completed callprocess name#{}, calledprocess#{}, calledfile#{}, db=#{}", name, processName, fileRelativePath, dbSrc)
      detailMap.put("name", name)
      detailMap.put("targetProcess", processName)
      detailMap.put("targetFile", fileRelativePath)
      detailMap.put("dataSource", dbSrc)
      detailMap.put("sql", sql)

    } 
   finally {
           try{
              rs.close
              stmt.close
              if(conn != null && conn.isClosed())
                conn.close
              }catch{
               case ex: Throwable => {
                  handleError(ex)
                  detailMap.put("exception", ExceptionUtil.completeStackTraceex(ex))          
                                      }
               }
            }

    context
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean = {
    val callProcessAsIs: in.handyman.dsl.Callprocess = action.asInstanceOf[in.handyman.dsl.Callprocess]
    val callProcess: in.handyman.dsl.Callprocess = CommandProxy.createProxy(callProcessAsIs, classOf[in.handyman.dsl.Callprocess], context)

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

  def handleFinally() = {
    logger.info("Finally section of call process")
  }

}
