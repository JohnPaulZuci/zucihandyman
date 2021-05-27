package in.handyman.process.onethread

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

import org.slf4j.MarkerFactory

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.dsl.Action
import in.handyman.util.ExceptionUtil
import in.handyman.util.ParameterisationEngine

class RestApiAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "CALLRESTAPI";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: Action, actionId:Integer): Context = {
    val restApiAsIs = action.asInstanceOf[in.handyman.dsl.CallRestApi]
    val restApi: in.handyman.dsl.CallRestApi = CommandProxy.createProxy(restApiAsIs, classOf[in.handyman.dsl.CallRestApi], context)

    val url = restApi.getUrl
    val method = restApi.getMethod
    val property = restApi.getProperty
    val name = restApi.getName
    val id = context.getValue("process-id")
    val sql = restApi.getValue.replaceAll("\"", "")
    logger.info(aMarker, "Rest API id#{}, name#{}, url#{}, sqlList#{}", id, name, url)
    detailMap.put("url", url)
    detailMap.put("method", method)
    detailMap.put("property", property)

    context
  }
  
  def GetRequest(context: Context, action: Action) {
      try {
          val url : URL = new URL("http://localhost:8080/RESTfulExample/json/product/get");
          val conn : HttpURLConnection = url.openConnection().asInstanceOf[HttpURLConnection];
          conn.setRequestMethod("GET");
          conn.setRequestProperty("Accept", "application/json");
  
          if (conn.getResponseCode() != 200) {
              throw new RuntimeException("Failed : HTTP error code : "
                      + conn.getResponseCode());
          }
  
          val br : BufferedReader = new BufferedReader(new InputStreamReader(
              (conn.getInputStream())));
  
          var output : String = null;
          System.out.println("Output from Server .... \n");
          while (({ output = br.readLine(); output != null })) {
              System.out.println(output);
          }
  
          conn.disconnect();

      } catch {
        case ex:MalformedURLException=>{
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

  def PostRequest(context: Context, action: Action) {
    try {
        val url : URL = new URL("http://localhost:8080/RESTfulExample/json/product/post");
        val conn : HttpURLConnection = url.openConnection().asInstanceOf[HttpURLConnection];
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        
        val input : String = "{\"qty\":100,\"name\":\"iPad 4\"}";

        val os : OutputStream = conn.getOutputStream();
        os.write(input.getBytes());
        os.flush();

        if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
            throw new RuntimeException("Failed : HTTP error code : "
                + conn.getResponseCode());
        }

        val br : BufferedReader = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));

        var output : String = "";
        System.out.println("Output from Server .... \n");
        while (({ output = br.readLine(); output != null })) {
            System.out.println(output);
        }

        conn.disconnect();

      } catch {
        case ex:MalformedURLException=>{
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
    val restApiAsIs = action.asInstanceOf[in.handyman.dsl.CallRestApi]
    val restApi: in.handyman.dsl.CallRestApi = CommandProxy.createProxy(restApiAsIs, classOf[in.handyman.dsl.CallRestApi], context)
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