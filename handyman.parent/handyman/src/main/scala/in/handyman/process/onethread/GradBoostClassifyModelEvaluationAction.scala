package in.handyman.process.onethread

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.util.ParameterisationEngine


/**
 * TODO - Still need to add more rich ness to audit trail with respect to statement warnings
 */
class GradBoostClassifyModelEvaluationAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]

  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    //Setting up the proxy for retrieving configuration for the macro
    val gradBoostClassifyModelEvaluationAsIs: in.handyman.dsl.GradBoostClassifyModelEvaluation = action.asInstanceOf[in.handyman.dsl.GradBoostClassifyModelEvaluation]
    val gradBoostClassifyModelEvaluation: in.handyman.dsl.GradBoostClassifyModelEvaluation = CommandProxy.createProxy(gradBoostClassifyModelEvaluationAsIs, classOf[in.handyman.dsl.GradBoostClassifyModelEvaluation], context)

    //Retrieving the global config map for default value
    val configMap = ConfigurationService.getGlobalconfig()

    val instanceId = context.getValue("process-id")
    
    val response : String = "{ \"sigma-id\" : \"\", \"template-name\": \"cub_jewelloan\", \"pipeline-name\": \"\", \"process-id\": " + instanceId + ", \"model-type\": \"GradientBoostClassificationModelEvaluation\"}";
    in.handyman.audit.AuditService.insertModelResponse(response)

    context
  }
  
  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean =
    {
      val gradBoostClassifyModelEvaluationAsIs: in.handyman.dsl.GradBoostClassifyModelEvaluation = action.asInstanceOf[in.handyman.dsl.GradBoostClassifyModelEvaluation]
      val gradBoostClassifyModelEvaluation: in.handyman.dsl.GradBoostClassifyModelEvaluation = CommandProxy.createProxy(gradBoostClassifyModelEvaluationAsIs, classOf[in.handyman.dsl.GradBoostClassifyModelEvaluation], context)

      val expression = gradBoostClassifyModelEvaluation.getCondition
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