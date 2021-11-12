package in.handyman.process.onethread

import com.typesafe.scalalogging.LazyLogging
import in.handyman.command.Action
import in.handyman.command.Context
import in.handyman.util.ResourceAccess
import in.handyman.DslStandaloneSetup
import in.handyman.server.PathResolver

import in.handyman.command.TryContext
import in.handyman.parser.antlr.DslParser
import java.io.FileReader
import in.handyman.server.ProcessExecutor
import in.handyman.command.CommandProxy
import in.handyman.server.ProcessAST
import in.handyman.util.ParameterisationEngine
import java.io.StringWriter
import java.io.PrintWriter
import in.handyman.util.ExceptionUtil
import java.sql.SQLException
import com.google.gson.Gson
import org.json.JSONObject
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity

class TerminalAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    val terminalAsIs: in.handyman.dsl.Terminal = action.asInstanceOf[in.handyman.dsl.Terminal]
    val terminal: in.handyman.dsl.Terminal = CommandProxy.createProxy(terminalAsIs, classOf[in.handyman.dsl.Terminal], context)

    try {
      val postData = terminal.getValue
      val gson = new Gson
      // create an HttpPost object
      // val url = terminal.getUrl
      val post = new HttpPost("http://localhost:8008")
      //  val hkey = terminal.getHkey
      //  val hvalue = terminal.getHvalue

      // set the Content-type
      post.setHeader("Content-type", "application/json")

      // add the JSON as a StringEntity
      post.setEntity(new StringEntity(gson.toJson(postData)))
      // send the post request
      val response = (new DefaultHttpClient).execute(post)
      var content = ""
      val entity = response.getEntity()
      if (entity != null) {
        val inputStream = entity.getContent()
        content = scala.io.Source.fromInputStream(inputStream).getLines.mkString
        inputStream.close
      }

      val resultObj = new JSONObject(content.toString())
      if (resultObj.getString("message").equals("success")) {
        println("success")
        true

      } else {
        println("error")
      }
      logger.info("response from python script  #{}", resultObj.getString("message"))
      false
    } catch {
      case e: Exception => false
    }
    context
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean = {
    val terminalAsIs: in.handyman.dsl.Terminal = action.asInstanceOf[in.handyman.dsl.Terminal]
    val terminal: in.handyman.dsl.Terminal = CommandProxy.createProxy(terminalAsIs, classOf[in.handyman.dsl.Terminal], context)

    val expression = terminal.getCondition
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