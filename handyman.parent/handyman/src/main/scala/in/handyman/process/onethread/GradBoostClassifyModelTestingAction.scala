package in.handyman.process.onethread

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.util.ParameterisationEngine


/**
 * TODO - Still need to add more rich ness to audit trail with respect to statement warnings
 */
class GradBoostClassifyModelTestingAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]

  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    //Setting up the proxy for retrieving configuration for the macro
    val gradBoostClassifyModelTestingAsIs: in.handyman.dsl.GradBoostClassifyModelTesting = action.asInstanceOf[in.handyman.dsl.GradBoostClassifyModelTesting]
    val gradBoostClassifyModelTesting: in.handyman.dsl.GradBoostClassifyModelTesting = CommandProxy.createProxy(gradBoostClassifyModelTestingAsIs, classOf[in.handyman.dsl.GradBoostClassifyModelTesting], context)

    //Retrieving the global config map for default value
    val configMap = ConfigurationService.getGlobalconfig()

    val instanceId = context.getValue("process-id")

    val response : String = "{ \"customer-goodness\" : \"\", \"principal-amount\": \"\", \"interest-rate\": \"\", \"waive-off-processing-fee\": \"\", \"process-id\": " + instanceId + ", \"model-type\": \"GradientBoostClassificationModelTesting\"}";
    in.handyman.audit.AuditService.insertModelTestingResponse(response)
    
    context
  }
  
  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean =
    {
      val gradBoostClassifyModelTestingAsIs: in.handyman.dsl.GradBoostClassifyModelTesting = action.asInstanceOf[in.handyman.dsl.GradBoostClassifyModelTesting]
      val gradBoostClassifyModelTesting: in.handyman.dsl.GradBoostClassifyModelTesting = CommandProxy.createProxy(gradBoostClassifyModelTestingAsIs, classOf[in.handyman.dsl.GradBoostClassifyModelTesting], context)

      val expression = gradBoostClassifyModelTesting.getCondition
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