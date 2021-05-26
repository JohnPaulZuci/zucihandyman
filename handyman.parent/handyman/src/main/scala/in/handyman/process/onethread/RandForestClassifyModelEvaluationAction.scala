package in.handyman.process.onethread

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.util.ParameterisationEngine
import in.handyman.audit.AuditService


/**
 * TODO - Still need to add more rich ness to audit trail with respect to statement warnings
 */
class RandForestClassifyModelEvaluationAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]

  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    //Setting up the proxy for retrieving configuration for the macro
    val randForestClassifyModelEvaluationAsIs: in.handyman.dsl.RandForestClassifyModelEvaluation = action.asInstanceOf[in.handyman.dsl.RandForestClassifyModelEvaluation]
    val randForestClassifyModelEvaluation: in.handyman.dsl.RandForestClassifyModelEvaluation = CommandProxy.createProxy(randForestClassifyModelEvaluationAsIs, classOf[in.handyman.dsl.RandForestClassifyModelEvaluation], context)

    //Retrieving the global config map for default value
    val configMap = ConfigurationService.getGlobalconfig()

    val instanceId = context.getValue("process-id")
    
    val response : String = "{ \"sigma-id\" : \"\", \"template-name\": \"cub_jewelloan\", \"pipeline-name\": \"\", \"process-id\": " + instanceId + ", \"model-type\": \"RandomForestClassificationModelEvaluation\"}";
    in.handyman.audit.AuditService.insertModelResponse(response)

    context
  }
  
  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean =
    {
      val randForestClassifyModelEvaluationAsIs: in.handyman.dsl.RandForestClassifyModelEvaluation = action.asInstanceOf[in.handyman.dsl.RandForestClassifyModelEvaluation]
      val randForestClassifyModelEvaluation: in.handyman.dsl.RandForestClassifyModelEvaluation = CommandProxy.createProxy(randForestClassifyModelEvaluationAsIs, classOf[in.handyman.dsl.RandForestClassifyModelEvaluation], context)

      val expression = randForestClassifyModelEvaluation.getCondition
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