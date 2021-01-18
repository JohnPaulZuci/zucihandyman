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

class SoapAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "SOAP";
  val aMarker = MarkerFactory.getMarker(auditMarker);
  val mapper: ObjectMapper = new ObjectMapper()
  val xmlMapper: XmlMapper = new XmlMapper()

  var mapObjFromJSONToInsert: HashMap[String, Any] = null
  var companyDistinctList: List[String] = new ArrayList()
  var templateName: String = null
  def execute(context: Context, action: Action, actionId: Integer): Context = {
    val soapAsIs = action.asInstanceOf[in.handyman.dsl.Soap]
    val soap: in.handyman.dsl.Soap = CommandProxy.createProxy(soapAsIs, classOf[in.handyman.dsl.Soap], context)

    val processId = context.getValue("process-id")
    //source DB
    val sourceDB = soap.getDb

    //api call method, url and type
    val API_CALL_METHOD = soap.getMethod
    val CONTENT_TYPE = soap.getContentType
    val API_URL = soap.getUrl

    //type
    var apiType = soap.getApiCallType

    //API parameters for authorization
    val REQUEST_API_PARAMS = soap.getSourceValue

    //get company distinct values
    val JONAS_COMPANY_AUDIT_DISTICT_VALUES = soap.getSelectDistinctCompany

    //insert request response audit
    val JONAS_AUDIT_TABLE_INSERT = soap.getStoreDataForTransform

    //update pagemax
    val JONAS_UPDATE_COMPANY_PAGEMAX = soap.getUpdatePageMaxValue
    var totalAPICallCount = 0;

    mainExecution()

    def mainExecution(): Unit = {
      var connection: Connection = ResourceAccess.rdbmsConn(sourceDB)
      var statement: Statement = connection.createStatement()
      try {
        var preparedStatement: PreparedStatement = null
        mapper.enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature())
        mapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature())
        mapper.enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature())
        val rootXMLDocument = createRootXML()
        val rs: ResultSet = fetchInputParamsFromDB(REQUEST_API_PARAMS)
        val finalXML: Document = createElementFromResultSetAndUpdateRoot(rs, rootXMLDocument)
        if (apiType.toLowerCase() == "company") {
          val mapList: List[Any] = new ArrayList()
          val finalInputXML: String = convertDocumentToString(finalXML)
          val response: String = postCall(finalInputXML, CONTENT_TYPE, API_CALL_METHOD)
          val readTree: JsonNode = xmlMapper.readTree(response)
          val dataNodeResult: JsonNode = getDataFromResultNode(readTree)
          templateName = dataNodeResult.get("Template").toString()
          System.out.println(dataNodeResult.get(templateName))
          System.out.println(dataNodeResult.get("ADM_CompanyProfile_tbl"))
          val datanodeArray = new JSONArray(dataNodeResult.get("ADM_CompanyProfile_tbl").toString())
          System.out.println("lenght of the data node  " + datanodeArray.length());
          val errorsResultNode: JsonNode = dataNodeResult
          val totalErrorsFound = getTotalErrorsFoundValueFromErrorNode(errorsResultNode)
          val errorsFoundList: List[String] = getErrorStatusFromJonas(dataNodeResult, errorsResultNode)
          val finalErrorsList: List[String] = errorsFoundList.stream().distinct().collect(Collectors.toList())
          updateAuditTableForAllApiCallsForCompany(finalInputXML, String.valueOf(dataNodeResult), String.valueOf(finalErrorsList), datanodeArray.length(), totalErrorsFound, JONAS_AUDIT_TABLE_INSERT)
        } else if (apiType.toLowerCase() == "employee" || apiType.toLowerCase() == "jobmaster") {
          getDistinctCompanyFromCompanyTransform()
          soapAPIExecution(finalXML, companyDistinctList, apiType)
          System.out.println("total executed count    ======>>>>>> " + totalAPICallCount)
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

    def soapAPIExecution(finalXML: Document, companyDistinctList: List[String], apiTypeVal: String): Unit = {
      companyDistinctList.stream().forEach((companyCodeValue) => {
        var pageMax: Int = 0
        var pageMaxValue: Int = 0
        var sum: Int = 1
        var firstTimeIteration: Boolean = true
        var connection: Connection = ResourceAccess.rdbmsConn(sourceDB)
        var preparedStatement: PreparedStatement = null

        try {
          do {
            val mapList: List[Any] = new ArrayList()
            val data = finalXML.getChildNodes
            val pageNum: Int = sum
            val processedXML: Document = iterateXMLandUpdateValue(data, finalXML, sum, companyCodeValue)
            val finalInputXML: String = convertDocumentToString(processedXML)
            val response: String = postCall(finalInputXML, CONTENT_TYPE, API_CALL_METHOD)
            val readTree: JsonNode = xmlMapper.readTree(response)
            val dataNodeResult: JsonNode = getDataFromResultNode(readTree)
            System.out.println(dataNodeResult.get(templateName))
            System.out.println(dataNodeResult.get(templateName))
            val datanodeArray = new JSONArray(dataNodeResult.get(templateName).toString())
            System.out.println("lenght of the data node  " + datanodeArray.length());
            val errorsResultNode: JsonNode = dataNodeResult
            val totalErrorsFound = getTotalErrorsFoundValueFromErrorNode(errorsResultNode)
            if (firstTimeIteration == true) {
              val pageMaxResultNode = getPageMaxValueFromResultNode(dataNodeResult)
              firstTimeIteration = false
              if (pageMax == 0 && Objects.nonNull(pageMaxResultNode)) {
                pageMaxValue = java.lang.Integer.valueOf(pageMaxResultNode.toString)
                println("page max value   == >" + pageMaxValue)
                if (apiTypeVal.toLowerCase() == "employee" || apiTypeVal.toLowerCase() == "jobmaster") {
                  preparedStatement = connection.prepareStatement(
                    String.valueOf(JONAS_UPDATE_COMPANY_PAGEMAX.replaceAll("\"", "")))
                }
                preparedStatement.setInt(1, pageMaxValue)
                preparedStatement.setString(2, companyCodeValue)
                preparedStatement.executeUpdate()
                preparedStatement.closeOnCompletion()
                connection.commit()
              }
            }
            pageMax += 1
            sum += 1
            totalAPICallCount += 1
            val errorsFoundList: List[String] =
              getErrorStatusFromJonas(dataNodeResult, errorsResultNode)
            val finalErrorsList: List[String] =
              errorsFoundList.stream().distinct().collect(Collectors.toList())
            if (apiTypeVal.toLowerCase() == "employee" || apiTypeVal.toLowerCase() == "jobmaster") {
              updateAuditTableForAllApiCalls(finalInputXML, String.valueOf(dataNodeResult), String.valueOf(finalErrorsList), datanodeArray.length(), totalErrorsFound, JONAS_AUDIT_TABLE_INSERT, companyCodeValue, pageNum)
            }
          } while (pageMax < pageMaxValue);
        } catch {
          case e2: Exception => e2.printStackTrace()

        }
      })
    }

    def iterateXMLandUpdateValue(nodeList: NodeList, doc: Document, sum: Int, companyCodeValue: String): Document = {
      try {
        if (nodeList != null) {
          val length: Int = nodeList.getLength
          for (i <- 0 until length if nodeList.item(i).getNodeType == Node.ELEMENT_NODE) {
            val el: Element = nodeList.item(i).asInstanceOf[Element]
            if (el.getNodeName.contains("apiParams")) {
              val apiParams: Node = doc.getElementsByTagName("apiParams").item(0)
              val currentAPIParamsValue: String = apiParams.getTextContent
              val objectJSON: JSONObject = new JSONObject(currentAPIParamsValue)
              objectJSON.keySet.forEach((entry) => {
                if (entry.equalsIgnoreCase("PageNum")) {
                  println("Previous value " + objectJSON.get("PageNum") + " and current value is  =>" + sum)
                  objectJSON.put("PageNum", sum)
                }
                if (entry.equalsIgnoreCase("Template")) {
                  templateName = objectJSON.get("Template").toString()
                }
                if (entry.equalsIgnoreCase("CompanyCode")) {
                  println(companyCodeValue)
                  objectJSON.put("CompanyCode", companyCodeValue)
                }
              })
              apiParams.setTextContent(objectJSON.toString)
            }
            if (nodeList.item(i).hasChildNodes()) {
              iterateXMLandUpdateValue(nodeList.item(i).getChildNodes, doc, sum, companyCodeValue)
            }
          }
        }
        return doc
      } catch {
        case e: Exception => {
          e.printStackTrace()
          null
        }

      }

    }

    def postCall(finalInputXML: String, contentType: String, requestMethod: String): String = {
      var response: String = ""
      try {
        val url: URL = new URL(API_URL)
        val conn: HttpURLConnection =
          url.openConnection().asInstanceOf[HttpURLConnection]
        //        conn.setReadTimeout(30000)
        //        conn.setConnectTimeout(30000)
        conn.setRequestMethod(requestMethod)
        conn.setDoInput(true)
        conn.setDoOutput(true)
        conn.setRequestProperty("Content-Type", contentType)
        var os: OutputStream = conn.getOutputStream()
        try {
          val input: Array[Byte] = finalInputXML.getBytes("utf-8")
          os.write(input, 0, input.length)

        } catch {
          case e: Exception => e.printStackTrace()

        }
        val responseCode: Int = conn.getResponseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
          var line: String = null
          val br: BufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream))
          while ({ line = br.readLine(); line != null })
            response += line
        }
      } catch {
        case e: Exception => e.printStackTrace()

      }
      response
    }
    def createRootXML(): Document = {
      try {
        val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val builder: DocumentBuilder = factory.newDocumentBuilder()
        val doc: Document = builder.newDocument()
        val root: Element = doc.createElement("soap12:Envelope")
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
        root.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema")
        root.setAttribute("xmlns:soap12", "http://www.w3.org/2003/05/soap-envelope")
        val body: Element = doc.createElement("soap12:Body")
        root.appendChild(body)
        doc.appendChild(root)
        return doc
      } catch {
        case e: Exception =>
          e.printStackTrace()
          return null
      }
    }

    def createElementFromResultSetAndUpdateRoot(rs: ResultSet, rootXMLDocument: Document): Document =
      try {
        val nodeList: NodeList = rootXMLDocument.getChildNodes
        val rsmd: ResultSetMetaData = rs.getMetaData
        val colCount: Int = rsmd.getColumnCount
        while (rs.next()) {
          val bodyElement: Element = rootXMLDocument.createElement("JonasAPI")
          bodyElement.setAttribute("xmlns", "jonas.jonasportal.com/")
          var updatedXml: Document = iterateXMLandAddNewChild(nodeList, rootXMLDocument, "Body", bodyElement)
          logger.debug("formatted xml val  ", printXML(updatedXml));
          var i: Int = 1
          //TODO - See how you can transition to functional paradigm. i.e - see if you could use recursive functions
          while (i <= colCount) {
            val columnName: String = rsmd.getColumnName(i)
            val value: AnyRef = rs.getObject(i)
            val node: Element = rootXMLDocument.createElement(columnName)
            node.appendChild(rootXMLDocument.createTextNode(value.toString))
            updatedXml = iterateXMLandAddNewChild(nodeList, rootXMLDocument, "JonasAPI", node)
            logger.debug("formatted xml val  ", printXML(updatedXml));
            i += 1
          }
        }
        return rootXMLDocument
      } catch {
        case e: Exception => {
          //TODO - throw the exception back to calling stack
          e.printStackTrace()
          return null
        }

      }

    def printXML(doc: Document): Unit = {
      TransformerFactory
        .newInstance()
        .newTransformer()
        .transform(new DOMSource(doc), new StreamResult(System.out))
    }

    def iterateXMLandAddNewChild(nodeList: NodeList, doc: Document, nodeName: String, childElement: Element): Document =
      try {
        if (nodeList != null) {
          val length: Int = nodeList.getLength
          for (i <- 0 until length) {
            if (nodeList.item(i).getNodeType == Node.ELEMENT_NODE) {
              val rootElement: Element = nodeList.item(i).asInstanceOf[Element]
              if (rootElement.getNodeName.contains(nodeName)) {
                rootElement.appendChild(childElement)
                return doc
              }
              if (nodeList.item(i).hasChildNodes())
                iterateXMLandAddNewChild(nodeList.item(i).getChildNodes, doc, nodeName, childElement)
            }
          }
        }
        return doc
      } catch {
        case e: Exception => {
          e.printStackTrace()
          null
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

    def getDistinctCompanyFromCompanyTransform(): Unit = {
      try {
        var connection: Connection = ResourceAccess.rdbmsConn(sourceDB)
        var statement: Statement = connection.createStatement()
        var rs: ResultSet = null
        rs = statement.executeQuery(JONAS_COMPANY_AUDIT_DISTICT_VALUES.replaceAll("\"", ""))
        while (rs.next()) {
          var i: Int = 1
          while (i <= rs.getMetaData.getColumnCount) {
            val value: AnyRef = rs.getObject(i)
            companyDistinctList.add(String.valueOf(value))
            i += 1
          }
        }
        rs.close()
        statement.closeOnCompletion()
        connection.commit()
        if (Objects.nonNull(connection))
          connection.close()
      } catch {
        case e: Exception => e.printStackTrace()

      }
    }

    def selectStmtExecutionForKeys(selectStmt: String): List[String] =
      try {
        var connection: Connection = ResourceAccess.rdbmsConn(sourceDB)
        var statement: Statement = connection.createStatement()
        var rs: ResultSet = null
        rs = statement.executeQuery(selectStmt.replaceAll("\"", ""))
        val resultColumnNames: List[String] = new ArrayList[String]()
        var i: Int = 1
        while (i <= rs.getMetaData.getColumnCount) {
          resultColumnNames.add(rs.getMetaData.getColumnName(i))
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

    def getErrorStatusFromJonas(dataNodeResult: JsonNode, errorsResultNode: JsonNode): List[String] = {
      val errorsFoundList: List[String] = new ArrayList[String]()
      errorsResultNode.fieldNames().forEachRemaining((entry) =>
        if (entry.equalsIgnoreCase("errorsFound")) {
          if (java.lang.Integer.valueOf(errorsResultNode.get("errorsFound").toString) > 0) {
            val value: AnyRef = dataNodeResult.get("errors").toString
            val errorChildObject: JSONArray = new JSONArray(value.toString)
            errorChildObject.forEach((errorVal) =>
              errorsFoundList.add(String.valueOf(errorVal)))
          }
        })
      errorsFoundList
    }

    def updateAuditTableForAllApiCallsForCompany(finalInputXML: String, response: String, errors: String, totalRecordsProcessed: Int, totalErrosFound: Int,
      insertStmt: String): Unit = {
      var connection: Connection = ResourceAccess.rdbmsConn(sourceDB)
      var statement: Statement = connection.createStatement()
      var preparedStatement: PreparedStatement = connection.prepareStatement(String.valueOf(insertStmt.replaceAll("\"", "")))
      preparedStatement.setObject(1, processId)
      preparedStatement.setString(2, finalInputXML)
      preparedStatement.setString(3, response)
      preparedStatement.setString(4, errors)
      preparedStatement.setObject(5, totalRecordsProcessed)
      preparedStatement.setObject(6, totalErrosFound)
      preparedStatement.executeUpdate()
      preparedStatement.closeOnCompletion()
      connection.commit()
      if (Objects.nonNull(connection))
        connection.close()
    }

    def updateAuditTableForAllApiCalls(finalInputXML: String, response: String, errors: String,
      totalRecordsProcessed: Int, totalErrorsFound: Int, insertStmt: String, companyCode: String, pageNum: Int): Unit = {
      var connection: Connection = ResourceAccess.rdbmsConn(sourceDB)
      var statement: Statement = connection.createStatement()
      var preparedStatement: PreparedStatement = connection.prepareStatement(String.valueOf(insertStmt.replaceAll("\"", "")))
      preparedStatement.setObject(1, processId)
      preparedStatement.setString(2, companyCode)
      preparedStatement.setInt(3, pageNum)
      preparedStatement.setString(4, finalInputXML)
      preparedStatement.setString(5, response)
      preparedStatement.setString(6, errors)
      preparedStatement.setObject(7, totalRecordsProcessed)
      preparedStatement.setObject(8, totalErrorsFound)
      preparedStatement.executeUpdate()
      preparedStatement.closeOnCompletion()
      connection.commit()
      if (Objects.nonNull(connection))
        connection.close()
    }

    def getPageMaxValueFromResultNode(
      readTreeValue: JsonNode): Int = {
      try {
        java.util.Optional
          .ofNullable(readTreeValue.get("PageMax").asInt()).orElse(readTreeValue.asInt(0))
      } catch {
        case e: Exception => {
          e.printStackTrace()
          return 0
        }
      }
    }

    def getTotalErrorsFoundValueFromErrorNode(readTreeValue: JsonNode): Int = {
      try {
        val error = readTreeValue.get("errorsFound")
        java.util.Optional
          .ofNullable(readTreeValue.get("errorsFound").asInt()).orElse(readTreeValue.asInt(0))
      } catch {
        case e: Exception => {
          e.printStackTrace()
          return 0
        }
      }
    }

    def loadXMLString(response: String): Document =
      try {
        val factory: DocumentBuilderFactory =
          DocumentBuilderFactory.newInstance()
        val builder: DocumentBuilder = factory.newDocumentBuilder()
        val is: InputSource = new InputSource(new StringReader(response))
        builder.parse(is)
      } catch {
        case e: Exception => {
          e.printStackTrace()
          null
        }

      }

    def convertDocumentToString(doc: Document): String = {
      var output: String = null
      try {
        val stringWriter: StringWriter = new StringWriter()
        val xmlOutput: StreamResult = new StreamResult(stringWriter)
        TransformerFactory
          .newInstance()
          .newTransformer()
          .transform(new DOMSource(doc), new StreamResult(stringWriter))
        output = stringWriter.getBuffer.toString
      } catch {
        case e: TransformerException => e.printStackTrace()

      }
      output
    }

    def getDataFromResultNode(readTree: JsonNode): JsonNode =
      try {
        val rootNode: JsonNode = Optional
          .ofNullable(readTree.at("/Body/JonasAPIResponse/JonasAPIResult")).orElseThrow(() => new RuntimeException("Cant find the result"))

        try {
          val rootNode2: JsonNode = mapper.readTree(rootNode.asText());
          if (rootNode2.get("data") != null) {
            print(mapper.readTree(rootNode2.get("data").asText().replaceAll("\\.(?!\\d)", "")))
            val result: JsonNode = mapper.readTree(rootNode2.get("data").asText().replaceAll("\\.(?!\\d)", ""));
            return result
          }
          throw new RuntimeException("Cant find the result")
        } catch {
          case e: IOException => throw new RuntimeException("can not parse json string", e)
        }

      } catch {
        case e: Exception => {
          e.printStackTrace()
          null
        }

      }

    context
  }

  def executeIf(context: Context, action: Action): Boolean = {
    val soapAsIs = action.asInstanceOf[in.handyman.dsl.Soap]
    val soap: in.handyman.dsl.Soap = CommandProxy.createProxy(soapAsIs, classOf[in.handyman.dsl.Soap], context)
    val name = soap.getName
    val id = context.getValue("process-id")
    val expression = soap.getCondition
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