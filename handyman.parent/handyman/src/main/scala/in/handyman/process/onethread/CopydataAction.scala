package in.handyman.process.onethread

import java.sql.SQLException

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ResourceAccess

class CopydataAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    val copyDataAsIs: in.handyman.dsl.Copydata = action.asInstanceOf[in.handyman.dsl.Copydata]
    val copyData: in.handyman.dsl.Copydata = CommandProxy.createProxy(copyDataAsIs, classOf[in.handyman.dsl.Copydata], context)

    val source = copyData.getSource
    val destination = copyData.getTo
    val name = copyData.getName
    val ddlSql: String = copyData.getValue.replaceAll("\"", "")
    val id = context.getValue("process-id")
    val table = copyData.getTarget

    val copydataDbConnfrom = ResourceAccess.rdbmsConn(source)
    val copydataStmtfrom = copydataDbConnfrom.createStatement
    val copydataDbConnto = ResourceAccess.rdbmsConn(destination)
    val copydataStmtto = copydataDbConnto.createStatement

    copydataDbConnto.setAutoCommit(false)

    val output: Array[String] = ddlSql.split(";")
    val select: String = output(0)
    val slimit: String = copyData.getLimit
    val limit: Int = slimit.toInt

    val rs = copydataStmtfrom.executeQuery(select)

    val cols: Int = rs.getMetaData().getColumnCount()

    var tcolumn: String = ""
    var column: String = ""

    val i: Int = 0
    for (i <- 1 to cols) {
      tcolumn = rs.getMetaData().getColumnName(i)
      column = column + tcolumn + ","
    }

    column = column.substring(0, column.length() - 1)

    var query: String = ""
    var j: Int = 0
    try {

      while (rs.next()) {
        val i: Int = 0
        query = query + "("
        for (i <- 1 to cols) {

          var str: String = rs.getString(i)
          if (str != null)
            str = str.replaceAll("[^a-zA-Z0-9-:]", " ")
          query = query + "\"" + str + "\"" + ","

        }

        query = query.substring(0, query.length() - 1) + "),"

        if (j % limit == 0) {

          query = query.replace("\"null\"", "null")
          logger.info("WriteCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, source, query)

          var insert: String = "insert ignore into " + table + " (" + column + ")" + " values " + query.substring(0, query.length() - 1) + ";"
          logger.info("WriteCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, source, insert)

          copydataStmtto.execute(insert)
          insert = ""
          query = ""

        }
        j = j + 1

        copydataDbConnto.commit()

      }

      query = query.replace("\"null\"", "null")
      var insert: String = "insert ignore into " + table + " (" + column + ")" + "values " + query.substring(0, query.length() - 1) + ";"
      logger.info("WriteCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, source, insert)
      copydataStmtto.execute(insert)
      copydataDbConnto.commit()

    } catch {
      case ex: SQLException => {
        ex.printStackTrace()
      }
    } finally {
      copydataStmtfrom.close()
      copydataStmtto.close()
      copydataDbConnfrom.close()
      copydataDbConnto.close()
      detailMap.put("name", name)
      detailMap.put("source", source)
      detailMap.put("destination", destination)
      detailMap.put("ddlSql", ddlSql)
    }

    context
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean = {
    val copyDataAsIs: in.handyman.dsl.Copydata = action.asInstanceOf[in.handyman.dsl.Copydata]
    val copyData: in.handyman.dsl.Copydata = CommandProxy.createProxy(copyDataAsIs, classOf[in.handyman.dsl.Copydata], context)

    val expression = copyData.getCondition
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

}