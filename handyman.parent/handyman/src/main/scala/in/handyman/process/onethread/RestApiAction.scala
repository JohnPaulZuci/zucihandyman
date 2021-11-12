package in.handyman.process.onethread

import java.io.IOException
import java.net.MalformedURLException

import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.slf4j.MarkerFactory

import com.typesafe.scalalogging.LazyLogging

import in.handyman.audit.AuditService
import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.dsl.Action
import in.handyman.util.ExceptionUtil
import in.handyman.util.ParameterisationEngine

class RestApiAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "RestApi";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: Action, actionId: Integer): Context = {
    val restApiAsIs = action.asInstanceOf[in.handyman.dsl.RestApi]
    val restApi: in.handyman.dsl.RestApi = CommandProxy.createProxy(restApiAsIs, classOf[in.handyman.dsl.RestApi], context)

    val url = restApi.getUrl
    val method = restApi.getMethod
    val property = restApi.getProperty
    val name = restApi.getName
    val payload = restApi.getPayload
    val id = context.getValue("process-id")
    val targetTable = restApi.getTargetTable
    val dbSrc = restApi.getSource
    val sql = restApi.getValue.replaceAll("\"", "")
    logger.info(aMarker, "Rest API id#{}, name#{}, url#{}, sqlList#{}", id, name, url)
    detailMap.put("url", url)
    detailMap.put("method", method)
    detailMap.put("property", property)
    detailMap.put("payload", payload)
    detailMap.put("targetTable", targetTable)
    detailMap.put("dbSrc", dbSrc)

    method match {
      case "GET" => {
        GetRequest(context, restApi)
      }
      case "POST" => {
        PostRequest(context, restApi)
      }
    }

    context
  }

  def GetRequest(context: Context, restApi: in.handyman.dsl.RestApi) {
    try {
      val client = HttpClients.createDefault()
      val getRequest = new HttpGet(restApi.getUrl)
      getRequest.addHeader("Accept", "application/json")

      val response = client.execute(getRequest)
      logger.info("Rest Api Response: " + response + " for URL: " + restApi.getUrl)

      val entity = response.getEntity()
      var content = ""
      if (entity != null) {
        val inputStream = entity.getContent()
        content = scala.io.Source.fromInputStream(inputStream).getLines.mkString
        inputStream.close
      }
      logger.info("Rest Api Response Content: " + content + " for URL: " + restApi.getUrl)
      AuditService.insertRestApiResponse(content, Integer.valueOf(context.getValue("process-id")), restApi.getTargetTable, restApi.getSource)
      client.close()

    } catch {
      case ex: MalformedURLException => {
        logger.error(aMarker, "Stopping execution, {}", ex)
        detailMap.put("Exception", ExceptionUtil.completeStackTraceex(ex))
        throw ex
      }

      case ex: IOException => {
        logger.error(aMarker, "Stopping execution, {} ", ex)
        detailMap.put("Exception", ExceptionUtil.completeStackTraceex(ex))
        throw ex
      }

      case ex: Throwable => {
        logger.error(aMarker, "Stopping execution, {}", ex)
        detailMap.put("Exception", ExceptionUtil.completeStackTraceex(ex))
        throw ex
      }
    }
  }

  def PostRequest(context: Context, restApi: in.handyman.dsl.RestApi) {
    try {

      val client = HttpClients.createDefault()
      val postRequest = new HttpPost(restApi.getUrl)
      postRequest.addHeader("Content-Type", "application/json")
      postRequest.addHeader("Accept", "application/json")
      postRequest.setEntity(new StringEntity(restApi.getPayload))

      val response = client.execute(postRequest)
      logger.info("Rest Api Response: " + response + " for URL: " + restApi.getUrl)

      val entity = response.getEntity()
      var content = ""
      if (entity != null) {
        val inputStream = entity.getContent()
        content = scala.io.Source.fromInputStream(inputStream).getLines.mkString
        inputStream.close
      }
      logger.info("Rest Api Response Content: " + content + " for URL: " + restApi.getUrl)
      AuditService.insertRestApiResponse(content, Integer.valueOf(context.getValue("process-id")), restApi.getTargetTable, restApi.getSource)
      client.close()

    } catch {
      case ex: MalformedURLException => {
        logger.error(aMarker, "Stopping execution, {}", ex)
        detailMap.put("Exception", ExceptionUtil.completeStackTraceex(ex))
        throw ex
      }

      case ex: IOException => {
        logger.error(aMarker, "Stopping execution, {} ", ex)
        detailMap.put("Exception", ExceptionUtil.completeStackTraceex(ex))
        throw ex
      }

      case ex: Throwable => {
        logger.error(aMarker, "Stopping execution, {}", ex)
        detailMap.put("Exception", ExceptionUtil.completeStackTraceex(ex))
        throw ex
      }
    }
  }

  def executeIf(context: Context, action: Action): Boolean = {
    val restApiAsIs = action.asInstanceOf[in.handyman.dsl.RestApi]
    val restApi: in.handyman.dsl.RestApi = CommandProxy.createProxy(restApiAsIs, classOf[in.handyman.dsl.RestApi], context)
    val name = restApi.getName
    val id = context.getValue("process-id")
    val expression = restApi.getCondition
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