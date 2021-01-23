package in.handyman.process.onethread

import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Statement
import java.util.ArrayList
import java.util.HashMap
import java.util.Iterator
import java.util.List
import java.util.Optional

import scala.collection.JavaConversions.`deprecated collectionAsScalaIterable`

import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.MarkerFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.dsl.Action
import in.handyman.util.ParameterisationEngine
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.sql.DriverManager
import java.util.stream.Collectors
import java.util.Objects
import java.net.HttpURLConnection
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.net.ssl.HttpsURLConnection
import java.net.URL
import java.io.OutputStream
import org.w3c.dom.NodeList
import org.w3c.dom.Node
import com.fasterxml.jackson.core.json.JsonReadFeature
import in.handyman.util.ResourceAccess
import java.sql.SQLException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonFactory

class JsonTranformationAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "JsonTranformation";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  val mapper: ObjectMapper = new ObjectMapper()
  val xmlMapper: XmlMapper = new XmlMapper()
  var mapObjFromJSONToInsert: HashMap[String, Any] = null
  var companyDistinctList: List[String] = new ArrayList()

  def execute(context: Context, action: Action, actionId: Integer): Context = {
    val jsonTransformAsIs = action.asInstanceOf[in.handyman.dsl.JSONTransformation]
    val jsonTransform: in.handyman.dsl.JSONTransformation = CommandProxy.createProxy(jsonTransformAsIs, classOf[in.handyman.dsl.JSONTransformation], context)
    val processId = context.getValue("process-id")

    //source DB
    val sourceDB = jsonTransform.getDb

    //type
    var apiType = jsonTransform.getApiCallType

    //target table name
    val INSERT_TABLE_NAME: String = jsonTransform.getTargetTableName

    //select table to know the column names
    val COLUMN_VAL_SELECT_STMT: String = jsonTransform.getJsonKeyValue

    //select values from temp table and proceed further processing
    val INPUT_SELECT_STMT: String = jsonTransform.getValue

    mainExecution()

    def mainExecution(): Unit = {
      var connection: Connection = ResourceAccess.rdbmsConn(sourceDB)
      var statement: Statement = connection.createStatement()
      try {
        var preparedStatement: PreparedStatement = null
        mapper.enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature())
        mapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature())
        mapper.enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature())
        val mapList: List[Any] = new ArrayList()
        val resultColumnNames: List[String] = createAndSelectStmtForKeys(COLUMN_VAL_SELECT_STMT)
        val resultSet = fetchInputParamsFromDB(INPUT_SELECT_STMT)
        val parentJSONArray: JSONArray = resultSetToJSONArray(resultSet)
        var dataNodeResult: JsonNode = null
        var companyCodeValue: String = ""
        parentJSONArray.forEach((entry) =>
          if (entry.isInstanceOf[JSONObject]) {
            val obj: JSONObject = new JSONObject(entry.toString)
            obj.keySet.forEach((key) =>
              if (key.equalsIgnoreCase("company_code")) {
                companyCodeValue = String.valueOf(obj.get(key))
              } else if (key.equalsIgnoreCase("response")) {
                val inputData = obj.get(key).toString()
                val factory: JsonFactory = mapper.getFactory();
                val parser: JsonParser = factory.createParser(inputData);
                dataNodeResult = xmlMapper.readTree(parser)
              })
            val errorsResultNode: JsonNode = dataNodeResult
            if (apiType.toLowerCase() == "company") {
              iterateResultDataNodeForCompany(mapList, resultColumnNames, dataNodeResult)
            } else if (apiType.toLowerCase() == "employee" || apiType.toLowerCase() == "jobmaster") {
              iterateResultDataNode(resultColumnNames, mapList, dataNodeResult, companyCodeValue)
            }
          })
        if (!mapList.isEmpty) {
          mapList.stream().forEach((mapEntry) =>
            createAndExecuteinsertStmtFromMap(INSERT_TABLE_NAME, mapEntry.asInstanceOf[java.util.HashMap[Any, Any]],connection))
        }
      } catch {
        case e: Exception => e.printStackTrace()

      } finally {
        if (Objects.nonNull(statement))
          statement.close()
        if (Objects.nonNull(connection))
          connection.close()
      }
    }

    def createAndExecuteinsertStmtFromMap(tableName: String, dataMap: HashMap[Any, Any],connection:Connection): Unit = {
      //var connection: Connection = ResourceAccess.rdbmsConn(sourceDB)
      try {
        val sql: StringBuilder =
          new StringBuilder("INSERT INTO ").append(tableName).append(" (")
        val placeholders: StringBuilder = new StringBuilder()
        var iter: Iterator[Any] = dataMap.keySet.iterator()
        while (iter.hasNext) {
          sql.append(iter.next())
          placeholders.append("?")
          if (iter.hasNext) {
            sql.append(",")
            placeholders.append(",")
          }
        }
        sql.append(") VALUES (").append(placeholders).append(");")
        print(sql)
        var preparedStatement: PreparedStatement = connection.prepareStatement(String.valueOf(sql))
        var i: Int = 1
        for (value <- dataMap.values) {
          preparedStatement.setObject(i, value)
          i += 1
        }
        print(preparedStatement)
        preparedStatement.execute()
        preparedStatement.closeOnCompletion()
      } catch {
        case e: Exception => { e.printStackTrace() }
      } finally {
        if (Objects.nonNull(connection)) {
          connection.commit()
          //connection.close()
        }
      }
    }

    def fetchInputParamsFromDB(selectStmt: String): ResultSet = {
      try {
        var connection: Connection = ResourceAccess.rdbmsConn(sourceDB)
        var statement: Statement = connection.createStatement()
        val rs: ResultSet = statement.executeQuery(selectStmt.replaceAll("\"", ""))
        val columnCount: Int = rs.getMetaData.getColumnCount
        val columnNames: List[String] = new ArrayList[String]()
        var i: Int = 1
        while (i <= columnCount) {
          columnNames.add(rs.getMetaData.getColumnName(i))
          i += 1
        }
        statement.closeOnCompletion()
        connection.commit()
        rs
      } catch {
        case e: Exception => {
          e.printStackTrace()
          null
        }
      } finally {

      }
    }

    def resultSetToJSONArray(rs: ResultSet): JSONArray = {
      val json: JSONArray = new JSONArray()
      try {
        val rsmd: ResultSetMetaData = rs.getMetaData
        while (rs.next()) {
          val numColumns: Int = rsmd.getColumnCount
          val obj: JSONObject = new JSONObject()
          var i: Int = 1
          while (i <= numColumns) {
            val columnName: String = rsmd.getColumnName(i)
            obj.put(columnName, rs.getObject(columnName))
            i += 1;
          }
          json.put(obj)
        }
      } catch {
        case e: SQLException => e.printStackTrace()

      }
      json
    }

    def iterateResultDataNodeForCompany(mapList: List[Any], resultColumnNames: List[String],
      dataNodeResult: JsonNode): Unit = {
      try {
        mapObjFromJSONToInsert = new HashMap()
        dataNodeResult.fieldNames().forEachRemaining((jsonKeyVal) => {
          val nodeType: String = dataNodeResult.get(jsonKeyVal).getNodeType.toString
          if (nodeType.equalsIgnoreCase("object")) {
            mapObjFromJSONToInsert = new HashMap()
            val childObject: JSONObject =
              new JSONObject(dataNodeResult.get(jsonKeyVal).textValue())
            iterateJSONObjectForCompany(resultColumnNames, childObject, mapList)
          } else if (nodeType.equalsIgnoreCase("array")) {
            val value: AnyRef = dataNodeResult.get(jsonKeyVal)
            val childJSONArrayObj: JSONArray = new JSONArray(value.toString)
            for (i <- 0 until childJSONArrayObj.length) {
              val childJSONValue: AnyRef = childJSONArrayObj.get(i)
              if (childJSONValue.isInstanceOf[JSONObject]) {
                mapObjFromJSONToInsert = new HashMap()
                val childArrayObject: JSONObject = new JSONObject(childJSONValue.toString)
                iterateJSONObjectForCompany(resultColumnNames, childArrayObject, mapList)
              }
            }
          } else {
            if (resultColumnNames.contains(jsonKeyVal)) {
              mapObjFromJSONToInsert.put(jsonKeyVal, dataNodeResult.get(jsonKeyVal))
            }
          }
        })
      } catch {
        case e: Exception => e.printStackTrace()

      }
    }

    def iterateJSONObjectForCompany(resultColumnNames: List[String], childObject: JSONObject, mapList: List[Any]): Unit = {
      try {
        childObject.keySet.forEach((childJSON) =>
          if (resultColumnNames.contains(childJSON)) {
            mapObjFromJSONToInsert.put(childJSON, childObject.get(childJSON))
            if (childObject.get(childJSON).isInstanceOf[JSONObject]) {
              val childArrayObject: JSONObject =
                new JSONObject(childObject.get(childJSON).toString())
              iterateJSONObjectForCompany(resultColumnNames, childArrayObject, mapList)
            }
          })
        if (!mapObjFromJSONToInsert.isEmpty) {
          if (resultColumnNames.contains("process_id"))
            mapObjFromJSONToInsert.put("process_id", processId)
          mapList.add(mapObjFromJSONToInsert)
        }
      } catch {
        case e: Exception => e.printStackTrace()

      }
    }

    def iterateJSONObject(resultColumnNames: List[String], childObject: JSONObject, mapList: List[Any],
      companyCodeValue: String): Unit = {
      try {
        childObject.keySet.forEach((childJSON) =>
          if (resultColumnNames.contains(childJSON)) {
            mapObjFromJSONToInsert.put(childJSON, childObject.get(childJSON))
            if (childObject.get(childJSON).isInstanceOf[JSONObject]) {
              val childArrayObject: JSONObject =
                new JSONObject(childObject.get(childJSON).toString())
              iterateJSONObject(resultColumnNames, childArrayObject, mapList, companyCodeValue)
            }
          })
        if (!mapObjFromJSONToInsert.isEmpty) {
          if (resultColumnNames.contains("CompanyCode"))
            mapObjFromJSONToInsert.put("CompanyCode", companyCodeValue)
          if (resultColumnNames.contains("process_id"))
            mapObjFromJSONToInsert.put("process_id", processId)
          mapList.add(mapObjFromJSONToInsert)
        }
      } catch {
        case e: Exception => e.printStackTrace()

      }
    }

    def createAndSelectStmtForKeys(selectStmt: String): List[String] =
      try {
        var connection: Connection = ResourceAccess.rdbmsConn(sourceDB)
        var statement: Statement = connection.createStatement()
        var rs: ResultSet = null
        rs = statement.executeQuery(selectStmt.replaceAll("\"", ""))
        val resultColumnNames: List[String] = new ArrayList[String]()
        var i: Int = 1
        while (i <= rs.getMetaData.getColumnCount) {
          resultColumnNames.add(rs.getMetaData.getColumnLabel(i))
          i += 1;
        }
        rs.close()
        statement.closeOnCompletion()
        connection.commit()
        if (Objects.nonNull(connection))
          connection.close()
        resultColumnNames
      } catch {
        case e: Exception => {
          e.printStackTrace()
          null
        }

      }

    def iterateResultDataNode(resultColumnNames: List[String], mapList: List[Any], dataNodeResult: JsonNode, companyCodeValue: String): Unit = {
      mapObjFromJSONToInsert = new HashMap()
      dataNodeResult.fieldNames().forEachRemaining((dataNodeEntry) => {
        val nodeType: String = dataNodeResult.get(dataNodeEntry).getNodeType.toString
        if (nodeType.equalsIgnoreCase("object")) {
          mapObjFromJSONToInsert = new HashMap()
          val childObject: JSONObject = new JSONObject(dataNodeResult.get(dataNodeEntry).asText())
          iterateJSONObject(resultColumnNames, childObject, mapList, companyCodeValue)
        } else if (nodeType.equalsIgnoreCase("array")) {
          val value: AnyRef = dataNodeResult.get(dataNodeEntry)
          val childJSONArrayObj: JSONArray = new JSONArray(value.toString)
          for (i <- 0 until childJSONArrayObj.length) {
            val childJSONValue: AnyRef = childJSONArrayObj.get(i)
            if (childJSONValue.isInstanceOf[JSONObject]) {
              mapObjFromJSONToInsert = new HashMap()
              val childArrayObject: JSONObject =
                new JSONObject(childJSONValue.toString)
              iterateJSONObject(resultColumnNames, childArrayObject, mapList, companyCodeValue)
            }
          }
        }
        if (!mapObjFromJSONToInsert.isEmpty)
          mapList.add(mapObjFromJSONToInsert)
      })
    }

    context
  }

  def executeIf(context: Context, action: Action): Boolean = {
    val jsonTransformAsIs = action.asInstanceOf[in.handyman.dsl.JSONTransformation]
    val jsonTransform: in.handyman.dsl.JSONTransformation = CommandProxy.createProxy(jsonTransformAsIs, classOf[in.handyman.dsl.JSONTransformation], context)
    val name = jsonTransform.getName
    val id = context.getValue("process-id")
    val expression = jsonTransform.getCondition
    try {
      val output = ParameterisationEngine.doYieldtoTrue(expression)
      logger.info(aMarker, "Completed evaluation to execute id#{}, name#{}, dbSrc#{}, expression#{}", id, name, expression)
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