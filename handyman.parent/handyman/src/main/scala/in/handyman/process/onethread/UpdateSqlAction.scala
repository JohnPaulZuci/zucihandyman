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
import in.handyman.audit.AuditService

class UpdateSqlAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "UPDATESQL";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: Action, actionId:Integer): Context = {
    val transformAsIs = action.asInstanceOf[in.handyman.dsl.UpdateSql]
    val transform: in.handyman.dsl.UpdateSql = CommandProxy.createProxy(transformAsIs, classOf[in.handyman.dsl.UpdateSql], context)

    val dbSrc = transform.getOn
    val name = transform.getName
    val id = context.getValue("process-id")
    val sqlList = transform.getValue
    logger.info(aMarker, "Transform id#{}, name#{}, dbSrc#{}, sqlList#{}", id, name, dbSrc)
    detailMap.put("dbSrc", dbSrc)

    val conn = ResourceAccess.rdbmsConn(dbSrc)
    conn.setAutoCommit(false)

    val stmt = conn.createStatement
    val iter = sqlList.iterator
    try {
      while (iter.hasNext) {
        val sqlWithoutQuotes = iter.next.replaceAll("\"", "")
        val sqlList = sqlWithoutQuotes.split(";")
        sqlList.foreach { sql =>
          if (!sql.trim.isEmpty()) {
             val stringreplace:String = sql.replaceAll(" ", "")
              val firstsixChr:String = stringreplace.substring(0, 6)
              if(firstsixChr.equalsIgnoreCase("update"))
               {
            logger.info(aMarker, "Transform id#{}, executing script {}", id, sql.trim)
            val statementId = AuditService.insertStatementAudit(actionId, "transform->"+name, context.getValue("process-name"))
            try {
              val rowCount = stmt.executeUpdate(sql.trim)
              val warnings = ExceptionUtil.completeSQLWarning(stmt.getWarnings)
              detailMap.put(sql.trim + ".count", rowCount.toString)
              detailMap.put(sql.trim + ".stmtcount", stmt.getUpdateCount.toString)
              detailMap.put(sql.trim + ".warnings", warnings)
              AuditService.updateStatementAudit(statementId, rowCount, 0, sql, 1)
              logger.info(aMarker, "Transform id# {}, executed script {} rows returned {}", statementId.toString(), sql.trim(), rowCount.toString())
              stmt.clearWarnings();
            } catch {
              
              case ex:SQLSyntaxErrorException=>{
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
              else{
                throw new Exception("The expected operating must be update but we found => "+sql);
              }
          }
        }
      }
      conn.commit
      logger.info(aMarker, "Completed Transform id#{}, name#{}, dbSrc#{}, sqlList#{}", id, name, dbSrc, sqlList)
    } finally {
      stmt.close
      conn.close
    }

    context
  }

  def executeIf(context: Context, action: Action): Boolean = {
    val transformAsIs = action.asInstanceOf[in.handyman.dsl.UpdateSql]
    val transform: in.handyman.dsl.UpdateSql = CommandProxy.createProxy(transformAsIs, classOf[in.handyman.dsl.UpdateSql], context)
    val dbSrc = transform.getOn
    val name = transform.getName
    val id = context.getValue("process-id")
    val expression = transform.getCondition
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