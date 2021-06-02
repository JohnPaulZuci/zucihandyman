package in.handyman.process.onethread

import com.typesafe.scalalogging.LazyLogging
import in.handyman.command.Context
import in.handyman.command.CommandProxy
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ResourceAccess
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.HttpGet
import java.net.URLEncoder
import org.apache.commons.text.StrSubstitutor
import com.sendgrid.SendGrid
import com.sendgrid.Request
import com.sendgrid.Method
import com.sendgrid.Email
import com.sendgrid.Content
import com.sendgrid.Mail
import org.slf4j.MarkerFactory
import java.util.concurrent.atomic.AtomicInteger
import in.handyman.audit.AuditService
import org.eclipse.emf.common.util.EList
import org.json.JSONArray
import org.json.JSONObject
import in.handyman.dsl.RestPart
import java.util.Objects
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.HashMap
import java.util.Map
import javax.net.ssl.HttpsURLConnection
import java.io.BufferedReader
import in.handyman.dsl.MultiPartData
import java.util.ArrayList

class RestAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "SENDMAIL";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: in.handyman.dsl.Action, actionId: Integer): Context = {
    val restAsIs: in.handyman.dsl.Rest = action.asInstanceOf[in.handyman.dsl.Rest]
    val rest: in.handyman.dsl.Rest = CommandProxy.createProxy(restAsIs, classOf[in.handyman.dsl.Rest], context)

    val name = rest.getName
    val url = rest.getUrl
    val method = rest.getMethod
    val authResource = rest.getAuthtoken
    val id = context.getValue("process-id")

    val authenticationType = rest.getAuthenticationType

    logger.info("Rest id#{}, name#{}, url#{}, method#{}, autinfo#{}", id, name, method, authResource)

    val restDbSrc = rest.getResourcedatafrom
    val restResSql = rest.getUrldata
    val restDbConn = ResourceAccess.rdbmsConn(restDbSrc)
    val restStmt = restDbConn.createStatement
    val restRs = restStmt.executeQuery(restResSql.replaceAll("\"", ""))
    restRs.next
    val restUrl = restRs.getString(1)

    restRs.close
    restStmt.close
    restDbConn.close

    logger.info("Rest id#{}, restdbsrc#{}, resturl#{}, sql#{}", id, restDbSrc, restUrl, restResSql)
    detailMap.put("restDbSrc", restDbSrc)
    detailMap.put("resturl", restUrl)
    detailMap.put("restResSql", restResSql)
    detailMap.put("method", method)
    detailMap.put("authResource", authResource)

    val headerDbSrc = rest.getHeaderdatafrom
    val headerSql = rest.getHeaderdata
    if (!headerSql.isEmpty() && Objects.nonNull(headerSql)) {
      val headerConn = ResourceAccess.rdbmsConn(headerDbSrc)
      val headerStmt = headerConn.createStatement
      val headerRs = headerStmt.executeQuery(headerSql.replaceAll("\"", ""))
      val headerColumnCount = headerRs.getMetaData.getColumnCount
      val headerMap: java.util.Map[String, String] = new java.util.HashMap[String, String]

      while (headerRs.next()) {
        val headerKey = headerRs.getString(1)
        val headerValue = headerRs.getString(2)
        headerMap.put(headerKey, headerValue)
      }
      headerRs.close
      headerStmt.close
      headerConn.close
      logger.info("Rest id#{}, headerDbSrc#{}, headerSql#{}, headerMap#{}", id, headerDbSrc, headerSql, headerMap)

    }

    val parentName = rest.getParentName
    val bodyDbSrc = rest.getPostdatafrom
    val parentData = rest.getParentdata

    logger.info("Rest id#{}, bodyDbSrc#{}, parentName#{}, parentDataSql#{}", id, bodyDbSrc, parentName, parentData)
    if (!parentData.isEmpty() && Objects.nonNull(parentData)) {
      val bodayConn = ResourceAccess.rdbmsConn(bodyDbSrc)
      val bodyStmt = bodayConn.createStatement
      val bodyResultset = bodyStmt.executeQuery(parentData.replaceAll("\"", ""))
      bodyResultset.next

      val jsonPayload = new JSONObject

      val parentColumnCount = bodyResultset.getMetaData.getColumnCount
      for (i <- 1 until parentColumnCount + 1) {
        val key = bodyResultset.getMetaData.getColumnLabel(i)
        val value = bodyResultset.getString(i)
        jsonPayload.put(key, value)
      }

      bodyResultset.close

      val parts: EList[RestPart] = rest.getParts
      val iter = parts.iterator()
      while (iter.hasNext) {
        val partSrc = iter.next()
        val partAsIs: in.handyman.dsl.RestPart = partSrc.asInstanceOf[in.handyman.dsl.RestPart]
        val part: in.handyman.dsl.RestPart = CommandProxy.createProxy(partAsIs, classOf[in.handyman.dsl.RestPart], context)
        val partName = part.getPartName
        val query = part.getPartData
        val partRs = bodyStmt.executeQuery(query.replaceAll("\"", ""))
        val partColCount = partRs.getMetaData.getColumnCount
        val partArray = new JSONArray;
        logger.info("Rest id#{}, partSrc#{}, partName#{}, partSql#{}", id, partSrc, partName, query)
        while (partRs.next) {
          val partObj = new JSONObject
          for (j <- 1 until partColCount + 1) {
            val fieldName = partRs.getMetaData.getColumnLabel(j)
            val fieldValue = partRs.getString(j)
            partObj.put(fieldName, fieldValue)
          }
          val subRestParts: EList[MultiPartData] = part.getMultiParts
          val childPartsIterator = subRestParts.iterator()
          while (childPartsIterator.hasNext) {
            val childPartSrc = childPartsIterator.next()
            val childPartAsIs: in.handyman.dsl.MultiPartData = childPartSrc.asInstanceOf[in.handyman.dsl.MultiPartData]
            val childPart: in.handyman.dsl.MultiPartData = CommandProxy.createProxy(childPartAsIs, classOf[in.handyman.dsl.MultiPartData], context)
            val childPartName = childPart.getPartName
            val childQuery = childPart.getPartData
            val childBodyStmt = bodayConn.createStatement
            val childPartRs = childBodyStmt.executeQuery(childQuery.replaceAll("\"", ""))
            val childPartColCount = childPartRs.getMetaData.getColumnCount
            val childPartArray = new JSONArray;
            logger.info("Rest id#{}, partSrc#{}, partName#{}, partSql#{}", id, partSrc, partName, query)
            while (childPartRs.next) {
              val columnValue = new ArrayList();
              for (j <- 1 until childPartColCount + 1) {
                val fieldName = childPartRs.getMetaData.getColumnLabel(j)
                val fieldValue = childPartRs.getString(j)
                childPartArray.put(fieldValue)
                println(fieldName)
                println(childPartArray)
                System.out.println(partObj)
                partObj.put(fieldName, childPartArray)
                System.out.println(partObj)
              }
            }
            childPartRs.close()
          }
          partArray.put(partObj)
        }
        if (parentName.contains(".")) {
          val parentArr = parentName.split("\\.")
          val partHolder = parentArr.apply(1)
          logger.info("Rest id#{}, adding shell holder as {}", partHolder)
          val jsonPartHolderPayload = new JSONObject
          jsonPartHolderPayload.put(partName, partArray)
          jsonPayload.put(partHolder, jsonPartHolderPayload)
        } else {
          logger.info("Rest id#{}, adding adding array as is to parent")
          jsonPayload.put(partName, partArray)
          System.out.println(jsonPayload)
        }
        partRs.close
      }
      bodyStmt.close
      bodayConn.close
      val jsonObject = jsonPayload.toString
      logger.info("Rest id#{}, outbound json object #{}", id, jsonObject.toString());

    }

    if (authenticationType.toLowerCase().equalsIgnoreCase("bearer")) {
      val params: HashMap[String, String] = new HashMap[String, String]()
      params.put("userName", " admin@ecompliance.ca")
      params.put("password", "zucisystemstest")
      params.put("grant_type", "password")
      val responseVal = getUserAuthToken(url, method, params)
    }
    val ackSql = rest.getAckdata
    val ackTarget = rest.getAckdatato
    context
  }

  def getUserAuthToken(urlValue: String, methodType: String, params: HashMap[String, String]): String = {
    var response: String = ""
    try {
      val requestUrl: URL = new URL(urlValue)
      val conn: HttpURLConnection =
        requestUrl.openConnection().asInstanceOf[HttpURLConnection]
      conn.setReadTimeout(20000)
      conn.setConnectTimeout(20000)
      conn.setRequestMethod(methodType)
      conn.setDoInput(true)
      conn.setDoOutput(true)
      val os: OutputStream = conn.getOutputStream
      val writer: BufferedWriter = new BufferedWriter(
        new OutputStreamWriter(os, "UTF-8"))
      writer.write(getPostDataString(params))
      writer.flush()
      writer.close()
      os.close()
      val responseCode: Int = conn.getResponseCode
      if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
        var line: String = null
        val br: BufferedReader = new BufferedReader(
          new InputStreamReader(conn.getInputStream))
        while ({ line = br.readLine(); line != null })
          response += line
      }
    } catch {
      case e: Exception => e.printStackTrace()

    }
    response
  }

  private def getPostDataString(params: HashMap[String, String]): String = {
    val result: StringBuilder = new StringBuilder()
    try {
      var first: Boolean = true
      params.forEach((key, value) => {
        if (first) first = false else result.append("&")
        result.append(URLEncoder.encode(key, "UTF-8"))
        result.append("=")
        result.append(URLEncoder.encode(value, "UTF-8"))
      });

    } catch {
      case e: Exception => e.printStackTrace()

    }
    result.toString
  }

  def getEmployee(): String = {
    var response: String = ""
    try {
      val finalToken: String =
        "N5EmpJoN2R_5LvQIoSvs3j4g_2_pTBKCYIGqsmSRsWvUoC2e5OD8CGNVN3IAIBu3LHhcs2kxPEDTB40-gvy2LAHurxRm8u-LYz_l9fOqmde3YCJAB-Dn2eo_y6vKfnOwV6Fiio0UYjjOCt2lDNppwtllZ45pV8UH7Wz1zUWP3A-JC4Hp0axyW9jsSM2mTyqq6S-rqaVy6hDXhXgGjnRE_CkI7ZhZo-3k0Z4Fx_cSVJvxYAa3tDIF0A2d9ituv2WXbl6sjtO4iU5XfJNzslKxqT2s9ah8r9FTcMAvdQDoWnf3YTLN8WLTCU9_9WQ6nstOA9W45dQ2dPegCJgvasQ-ZAu7LN62zXShos_5BdxxBDO8_HaWjHp9YrVV0hB6slfAojI7zh5cI_ommzlb2Q-9xLsQW2JJg7yE07006DqqFEm5uN9tCnbWW2GodrdGOx3GXio9jY0lsrxB3CaCB8HqOrxiJ9DjsAnZaeP1ORgJY4xqPmXLoft5XoYdv4aKgs3a"
      val bearerAuth: String = "bearer " + finalToken
      val url: URL = new URL("http://192.168.0.236:88/employees/import")
      val conn: HttpURLConnection =
        url.openConnection().asInstanceOf[HttpURLConnection]
      conn.setReadTimeout(15000)
      conn.setConnectTimeout(20000)
      conn.setRequestMethod("POST")
      conn.setDoInput(true)
      conn.setDoOutput(true)
      conn.setRequestProperty("Authorization", bearerAuth)
      conn.setRequestProperty("x-version", "2")
      conn.setRequestProperty("x-site", "1")
      conn.setRequestProperty("Content-Type", "application/json; utf-8")
      val jsonInputString: String = "[\n" + "                 {\n" +
        "                     \"employeeId\": \"test123\",\n" +
        "                     \"firstName\": \"adminJoe\",\n" +
        "                     \"lastName\": \"powerUser\",\n" +
        "                     \"email\": \"123email@noreply.com\",\n" +
        "                     \"phoneNumber\": \"1234567890\",\n" +
        "                     \"hireDate\": \"1989-05-22\",\n" +
        "                     \"terminationDate\": \"1989-05-25\",\n" +
        "                     \"sites\": [\n" +
        "                         {\n" +
        "                             \"name\": \"Zucisystems Chennai\",\n" +
        "                             \"jobProfiles\": [\"Worker\"],\n" +
        "                             \"permission\": \"Admin\"\n" +
        "                         }\n" +
        "                     ],\n" +
        "                     \"isSingleSignOnUser\": false\n" +
        "                 }\n" +
        "             ]"
      try {
        val os: OutputStream = conn.getOutputStream()
        val input: Array[Byte] = jsonInputString.getBytes("utf-8")
        os.write(input, 0, input.length)

      } catch {
        case e: Exception => e.printStackTrace()

      }
      val responseCode: Int = conn.getResponseCode
      if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
        var line: String = null
        val br: BufferedReader = new BufferedReader(
          new InputStreamReader(conn.getInputStream))
        while ({ line = br.readLine(); line != null })
          response += line
      }
    } catch {
      case e: Exception => e.printStackTrace()

    }
    response
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean = {
    val restAsIs: in.handyman.dsl.Rest = action.asInstanceOf[in.handyman.dsl.Rest]
    val rest: in.handyman.dsl.Rest = CommandProxy.createProxy(restAsIs, classOf[in.handyman.dsl.Rest], context)
    val expression = rest.getCondition
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
    this.detailMap
  }

}