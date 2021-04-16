package in.handyman.process.onethread

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.util.ParameterisationEngine


/**
 * TODO - Still need to add more rich ness to audit trail with respect to statement warnings
 */
class GradBoostRegressModelTrainingAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]

  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    //Setting up the proxy for retrieving configuration for the macro
    val gradBoostRegressModelTrainingAsIs: in.handyman.dsl.GradBoostRegressModelTraining = action.asInstanceOf[in.handyman.dsl.GradBoostRegressModelTraining]
    val gradBoostRegressModelTraining: in.handyman.dsl.GradBoostRegressModelTraining = CommandProxy.createProxy(gradBoostRegressModelTrainingAsIs, classOf[in.handyman.dsl.GradBoostRegressModelTraining], context)

    //Retrieving the global config map for default value
    val configMap = ConfigurationService.getGlobalconfig()

    val instanceId = context.getValue("process-id")
    
    val response : String = "{ \"sigma-id\" : \"\", \"template-name\": \"cub_jewelloan\", \"pipeline-name\": \"\", \"process-id\": " + instanceId + ", \"model-type\": \"GradientBoostRegressionModelTraining\"}";
    in.handyman.audit.AuditService.insertModelResponse(response)

    context
  }
  
  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean =
    {
      val gradBoostRegressModelTrainingAsIs: in.handyman.dsl.GradBoostRegressModelTraining = action.asInstanceOf[in.handyman.dsl.GradBoostRegressModelTraining]
      val gradBoostRegressModelTraining: in.handyman.dsl.GradBoostRegressModelTraining = CommandProxy.createProxy(gradBoostRegressModelTrainingAsIs, classOf[in.handyman.dsl.GradBoostRegressModelTraining], context)

      val expression = gradBoostRegressModelTraining.getCondition
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