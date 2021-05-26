package in.handyman.process.onethread

import com.typesafe.scalalogging.LazyLogging
import in.handyman.dsl.Action
import in.handyman.command.Context
import in.handyman.util.ResourceAccess
import in.handyman.command.CommandProxy
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ExceptionUtil
import java.sql.SQLException
import org.slf4j.MarkerFactory
import java.sql.SQLSyntaxErrorException
import in.handyman.HandymanException
import in.handyman.audit.AuditService
import java.sql.ResultSet
import java.sql.ResultSetMetaData

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayList

class PythonAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "PYTHON";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: Action, actionId: Integer): Context = {
    val PythonAsIs = action.asInstanceOf[in.handyman.dsl.Python]
    val python: in.handyman.dsl.Python = CommandProxy.createProxy(PythonAsIs, classOf[in.handyman.dsl.Python], context)

    val name = python.getName
    val id = context.getValue("process-id")
    val sql = python.getValue.replaceAll("\"", "")
    val target = python.getTarget

    val dbSrc = {
      if (python.getSource.trim.isEmpty()) {
        throw new HandymanException("source data source cannot be empty for copydata for " + name)
      }
      python.getSource.trim
    }
    logger.info(aMarker, "Python id#{}, name#{}, dbSrc#{}, sql#{}, table#{}", id, name, dbSrc, target)
    detailMap.put("dbSrc", dbSrc)
    val params: ArrayList[String] = new ArrayList[String]()

    val conn = ResourceAccess.rdbmsConn(dbSrc)
    conn.setAutoCommit(false)
    val stmt = conn.createStatement

    try {

      val rs: ResultSet = stmt.executeQuery(sql)
      val rsmd: ResultSetMetaData = rs.getMetaData()
      val columnCount = rsmd.getColumnCount()
      while (rs.next()) {
        for (i <- 1 to columnCount) {
          params.add(rs.getString(i));
        }
      }
      val param = params_assembler(params)
      val rt: Runtime = Runtime.getRuntime
      val proc: Process = rt.exec(param)
      val stderr: InputStream = proc.getErrorStream
      val isr: InputStreamReader = new InputStreamReader(stderr)
      val br: BufferedReader = new BufferedReader(isr)
      var line: String = null
      val stackTrace: ArrayList[String] = new ArrayList[String]()
      while (({ line = br.readLine(); line != null }))
        stackTrace.add(line.replaceAll("\"", "'"))
      val query = "insert into " + target + " (process_id,stack_trace) values (" + id + ",\"" + stackTrace + "\")"
      stmt.executeUpdate(query)
      conn.commit
      logger.info(aMarker, "Completed Python id#{}, name#{}, dbSrc#{}, sql#{}, table#{}", id, name, dbSrc, target)
    
    } catch {
      case t: Throwable => t.printStackTrace()
      
    } finally {
      stmt.close
      conn.close
    }
    
    context
  }

  def params_assembler(list: ArrayList[String]): String = {
    var params: String = ""
    for (i <- 0 until list.size) {
      params = params + list.get(i) + " "
    }
    params
  }

  def executeIf(context: Context, action: Action): Boolean = {
    val PythonAsIs = action.asInstanceOf[in.handyman.dsl.Python]
    val python: in.handyman.dsl.Python = CommandProxy.createProxy(PythonAsIs, classOf[in.handyman.dsl.Python], context)
    val dbSrc = python.getSource
    val name = python.getName
    val id = context.getValue("process-id")
    val target = python.getTarget
    val expression = python.getCondition
    try {
      val output = ParameterisationEngine.doYieldtoTrue(expression)
      logger.info(aMarker, "Completed evaluation to execute id#{}, name#{}, dbSrc#{}, table#{}, expression#{}", id, name, dbSrc, target, expression)
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

}