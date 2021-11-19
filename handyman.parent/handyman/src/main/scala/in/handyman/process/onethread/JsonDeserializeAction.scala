package in.handyman.process.onethread

import java.sql.Connection
import java.sql.SQLException
import java.sql.SQLSyntaxErrorException
import java.sql.Statement
import java.util.Date
import java.util.stream.Collectors

import org.slf4j.MarkerFactory

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.dsl.Action
import in.handyman.util.ExceptionUtil
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ResourceAccess

class JsonDeserializeAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "JSONDESERIALIZE";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: Action, actionId: Integer): Context = {
    val jsonDeserializeAsIs = action.asInstanceOf[in.handyman.dsl.JsonDeserialize]
    val jsonDeserialize: in.handyman.dsl.JsonDeserialize = CommandProxy.createProxy(jsonDeserializeAsIs, classOf[in.handyman.dsl.JsonDeserialize], context)

    val dbSrc = jsonDeserialize.getSource
    val name = jsonDeserialize.getName
    val targetTable = jsonDeserialize.getTargetTable
    val input = jsonDeserialize.getInput
    val id = context.getValue("process-id")
    val sql = jsonDeserialize.getValue.replaceAll("\"", "")
    logger.info(aMarker, "Json Deserialize id#{}, name#{}, dbSrc#{}, sqlList#{}", id, name, dbSrc)
    detailMap.put("dbSrc", dbSrc)
    detailMap.put("targetTable", targetTable)
    detailMap.put("input", input)
    detailMap.put("id", id)
    detailMap.put("name", name)

    var conn : Connection = null
    var stmt : Statement = null
    try{
      conn = ResourceAccess.rdbmsConn(dbSrc)
      conn.setAutoCommit(false)
      
      var jsonFlat : JSONFlattener = new JSONFlattener()
      stmt = conn.createStatement()
      createTable(jsonFlat.parseJson(input), targetTable, stmt)
    
      conn.commit
      logger.info(aMarker, "Completed Deserialize Json id#{}, name#{}, dbSrc#{}, sql#{}", id, name, dbSrc, sql)
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
    finally {
      if(stmt != null)
        stmt.close
        
      if(conn != null) 
        conn.close
    }
    context
  }
  
  def createTable(list : java.util.List[java.util.Map[String, Object]], targetTable : String, stmt : Statement) {
	    var colDetails : java.util.List[String] = list.stream().flatMap(map => map.keySet().stream()).distinct().collect(Collectors.toList());

	    val tableSchemaBuilder = new StringBuilder
      tableSchemaBuilder.append("CREATE TABLE ").append(targetTable).append(Constants.SPACE).append(Constants.OPEN_PARENTHESES)
      
      var colHeadCount : Int = 0;
	    var columnList : String = ""
      colDetails.forEach(colD => {
        val colDSplit : Array[String] = colD.split('|');
        val colName : String = colDSplit(0)
        val colType : String = colDSplit(1)
        columnList = columnList + colName + "," + Constants.SPACE
        tableSchemaBuilder.append(colName).append(Constants.SPACE).append(colType)
        
        colHeadCount = colHeadCount + 1
        if(colHeadCount != colDetails.size())
           tableSchemaBuilder.append(Constants.FIELD_SEPARATOR).append(Constants.SPACE)
      })
      columnList = columnList.substring(0, columnList.length()-2)
      tableSchemaBuilder.append(Constants.CLOSE_PARENTHESES).append(Constants.SEMICOLON)
	    stmt.executeUpdate(tableSchemaBuilder.toString());
      
      val insertBuilder = new StringBuilder
      insertBuilder.append("INSERT INTO ").append(targetTable).append(" (").
      append(columnList).append(") VALUES")
      
      var rowCount : Int = 0;
      list.forEach(lmap => {
        var colCount : Int = 0;
        insertBuilder.append(Constants.INSERT_STMT_VALUE_START)
        lmap.entrySet().forEach(rowValue => {
          var obj : Object = rowValue.getValue
          if(obj.isInstanceOf[String])
            insertBuilder.append(Constants.STRING_ENCLOSER).append(rowValue.getValue).append(Constants.STRING_ENCLOSER)
          else if(obj.isInstanceOf[Date])
            insertBuilder.append(Constants.STRING_ENCLOSER).append(rowValue.getValue).append(Constants.STRING_ENCLOSER)
          else
            insertBuilder.append(rowValue.getValue)
            
          colCount = colCount + 1
          if(colCount != lmap.entrySet().size())
             insertBuilder.append(Constants.FIELD_SEPARATOR).append(Constants.SPACE)
        })
        insertBuilder.append(Constants.INSERT_STMT_VALUE_END)
        
        rowCount = rowCount + 1
        if(rowCount != list.size())
          insertBuilder.append(Constants.FIELD_SEPARATOR)
        else
          insertBuilder.append(Constants.SEMICOLON)
      })
      
      insertBuilder.toString()
      stmt.executeUpdate(insertBuilder.toString());
      
      logger.info(aMarker, "Deserialized Json with schema#{}, executing script insert#{}", tableSchemaBuilder.toString(), insertBuilder.toString())
	}

  def executeIf(context: Context, action: Action): Boolean = {
    val jsonDeserializeAsIs = action.asInstanceOf[in.handyman.dsl.JsonDeserialize]
    val jsonDeserialize: in.handyman.dsl.JsonDeserialize = CommandProxy.createProxy(jsonDeserializeAsIs, classOf[in.handyman.dsl.JsonDeserialize], context)
    val dbSrc = jsonDeserialize.getSource
    val name = jsonDeserialize.getName
    val id = context.getValue("process-id")
    val expression = jsonDeserialize.getCondition
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