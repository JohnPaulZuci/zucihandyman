package in.handyman.process.onethread

import java.sql.SQLException
import java.sql.SQLSyntaxErrorException
import java.sql.Statement

import org.json.JSONObject
import org.slf4j.MarkerFactory

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.dsl.Action
import in.handyman.util.ExceptionUtil
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ResourceAccess
import in.handyman.audit.AuditService
import java.sql.JDBCType
import java.util.Arrays


class JsonTransformAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "JSONTRANSFORM";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: Action, actionId: Integer): Context = {
    val jsonTransformAsIs = action.asInstanceOf[in.handyman.dsl.JsonTransform]
    val jsonTransform: in.handyman.dsl.JsonTransform = CommandProxy.createProxy(jsonTransformAsIs, classOf[in.handyman.dsl.JsonTransform], context)

    val dbSrc = jsonTransform.getSource
    val name = jsonTransform.getName
    val targetTable = jsonTransform.getTargetTable
    val id = context.getValue("process-id")
    val sql = jsonTransform.getValue.replaceAll("\"", "")
    logger.info(aMarker, "Transform Json id#{}, name#{}, dbSrc#{}, sqlList#{}", id, name, dbSrc)
    detailMap.put("dbSrc", dbSrc)
    detailMap.put("targetTable", targetTable)

    val conn = ResourceAccess.rdbmsConn(dbSrc)
    conn.setAutoCommit(false)

    val stmt = conn.createStatement
    try {
      if (!sql.trim.isEmpty()) {
        logger.info(aMarker, "Transform Json id#{}, executing script {}", id, sql.trim)
        try {
          val rs = stmt.executeQuery(sql.trim)
          val columnCount = rs.getMetaData.getColumnCount
          val rsMetaData = rs.getMetaData

          val jsonObject: JSONObject = new JSONObject()

          while (rs.next()) {
        	  for (i <- 1 to columnCount) {
        		  val isArray:String = rs.getObject(i)+""

        				  if(isArray.charAt(0) == '['){

        					  val str = rs.getString(i).substring(1, rs.getString(i).length()-1);
        					  var strReplace:Array[String] = str.replace("\"", "").split(",")
        						jsonObject.put(rsMetaData.getColumnLabel(i), strReplace)

        				  }
        				  else{
        					  jsonObject.put(rsMetaData.getColumnLabel(i), rs.getObject(i))
        				  }


        	  }
          }
          println("-------------------------------------------------------------")
          println(jsonObject)
          val st = conn.prepareStatement("INSERT INTO " + targetTable + "(process_id, json, updated_date) VALUES (?, ?, NOW());", Statement.RETURN_GENERATED_KEYS)
          st.setInt(1, Integer.valueOf(id))
          st.setObject(2,jsonObject,JDBCType.JAVA_OBJECT)
          
          val rowsUpdated = st.executeUpdate()
          detailMap.put("resiltt", jsonObject.toString())

          conn.commit
        } catch {

          case ex: SQLSyntaxErrorException => {
            logger.error(aMarker, "Stopping execution, General Error executing sql for {} with for campaign {}", sql, ex)
            detailMap.put(sql.trim + ".exception", ExceptionUtil.completeStackTraceex(ex))
            throw ex
          }

          case ex: SQLException => {
            logger.error(aMarker, "Continuing to execute, even though SQL Error executing sql for {} ", sql, ex)
            detailMap.put(sql.trim + ".exception", ExceptionUtil.completeStackTraceex(ex))

          }

          case ex: Throwable => {
            logger.error(aMarker, "Stopping execution, General Error iexecuting sql for {} with for campaign {}", sql, ex)
            detailMap.put(sql.trim + ".exception", ExceptionUtil.completeStackTraceex(ex))
            throw ex
          }
        }
      }
      conn.commit
      logger.info(aMarker, "Completed Transform Json id#{}, name#{}, dbSrc#{}, sql#{}", id, name, dbSrc, sql)
    } finally {
      stmt.close
      conn.close
    }

    context
  }

  def executeIf(context: Context, action: Action): Boolean = {
    val jsonTransformAsIs = action.asInstanceOf[in.handyman.dsl.JsonTransform]
    val jsonTransform: in.handyman.dsl.JsonTransform = CommandProxy.createProxy(jsonTransformAsIs, classOf[in.handyman.dsl.JsonTransform], context)
    val dbSrc = jsonTransform.getSource
    val name = jsonTransform.getName
    val id = context.getValue("process-id")
    val expression = jsonTransform.getCondition
    try {
      val output = ParameterisationEngine.doYieldtoTrue(expression)
      logger.info(aMarker, "Completed evaluation to execute id#{}, name#{}, dbSrc#{}, expression#{}", id, name, dbSrc, expression)
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