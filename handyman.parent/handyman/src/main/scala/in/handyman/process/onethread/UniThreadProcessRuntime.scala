package in.handyman.process.onethread

import java.util.Objects

import org.slf4j.MarkerFactory

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.Context
import in.handyman.command.ErrorContext
import in.handyman.command.FinallyContext
import in.handyman.command.TryContext
import in.handyman.dsl.Catch
import in.handyman.dsl.Finally
import in.handyman.server.ProcessResponse
import in.handyman.server.ProcessRuntime

class UniThreadProcessRuntime(name: String, id: Int) extends ProcessRuntime with LazyLogging {
  val jsonSerializer = new ObjectMapper
  val auditMarkerText = "Actioncaller";
  val auditMarker = MarkerFactory.getMarker(auditMarkerText);

  @throws(classOf[Exception])
  def execute(process: in.handyman.dsl.Process, context: Context): ProcessResponse = {
    var errorContext: ErrorContext = new ErrorContext(context.asInstanceOf[TryContext])
    var processResponse = new ProcessResponse
    try {
      logger.info(auditMarker, "Executing try block for process {}", process.getName)
      val detailMap = executeChain(process.getTry.getAction, context)
      processResponse.detailMap = detailMap
      processResponse.context = context
      processResponse
    } catch {
      case ex: in.handyman.AbortException => {
        //logError(ex, process.getName)
        logger.info(auditMarker, "Abort exception caught in try block for process {}", process.getName)
        processResponse.exception = ex
        ex.processResponse = processResponse
        processResponse
        throw ex
      }
      case ex: Throwable => {
        logError(ex, process.getName)
        val onError = process.getCatch
        logger.info(auditMarker, "General exception caught in try block for process {}, attempting to execute catch block", process.getName)
        errorContext = executeCatch(onError, context.asInstanceOf[TryContext])
        errorContext.exception = ex
        processResponse.context = errorContext
        processResponse.exception = ex
        processResponse
        throw ex
      }

    } finally {
      val onFinally = process.getFinally
      logger.info(auditMarker, "Executing finally block for process {}, attempting to execute catch block", process.getName)
      executeFinally(onFinally, errorContext)
      processResponse.context = errorContext
      processResponse
    }
    //Send context + commandDetailAsMap + exception stack trace back to client
  }

  @throws(classOf[Exception])
  def executeChain(actionList: org.eclipse.emf.common.util.EList[in.handyman.dsl.Action], context: Context): java.util.HashMap[String, java.util.Map[String, String]] =
    {

      val iterator = actionList.iterator
      val detailMap: java.util.HashMap[String, java.util.Map[String, String]] = new java.util.HashMap[String, java.util.Map[String, String]]
      while (iterator.hasNext) {
        val action = iterator.next
        val actionRuntime = CommandFactory.create(action.eClass.getName)
        if (actionRuntime.executeIf(context, action)) {
          val actionId = in.handyman.audit.AuditService.insertCommandAudit(id, action.eClass().getName + "->" + action.getName, name)
          context.addValue("process-name", name)
          actionRuntime.execute(context, action, actionId)
          //TODO still need to fix the status part
          val commandDetailAsMap = actionRuntime.generateAudit()
          if (commandDetailAsMap != null && !commandDetailAsMap.isEmpty()) {
            val commandDetail = jsonSerializer.writeValueAsString(commandDetailAsMap)
            detailMap.put(action.getName + "." + actionId.toString(), commandDetailAsMap)
            in.handyman.audit.AuditService.updateCommandAudit(actionId, 1, commandDetail)
          }
        }
      }
      detailMap
    }

  def logError(ex: Throwable, process: String) = {
    logger.error(auditMarker, "Error executing process {}", process, ex)
  }

  def executeCatch(onError: Catch, context: TryContext): ErrorContext = {
    // logger.warn(marker, message)
    val errorContext: ErrorContext = new ErrorContext(context)
    executeChain(onError.getAction, errorContext)
    errorContext
  }

  def executeFinally(onFinally: Finally, errorContext: ErrorContext): FinallyContext = {
    val finallyContext: FinallyContext = new FinallyContext(errorContext)
    if (Objects.nonNull(onFinally))
      executeChain(onFinally.getAction, finallyContext)
    val processId: String = errorContext.getValue("process-id")
    val contextLog: String = errorContext.getJson()
    val status: Int = if (contextLog.isEmpty) 1 else -1
    in.handyman.audit.AuditService.updateProcessAudit(Integer.parseInt(processId), status, contextLog, name)
    finallyContext
  }

}