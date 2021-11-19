package in.handyman.util

import in.handyman.command.Context
import org.apache.commons.text.StrSubstitutor

object ParameterisationEngine {

  def resolve(inputString: String, context: Context): String = {
    val configMap = context.getMe
    val paramEngine = new StrSubstitutor(configMap)
    if (inputString != null) {
      val output = paramEngine.replace(inputString)
        //.replace("0_", configMap.getOrDefault("process-id", "0_") + "_")
      output
    } else
      ""
  }

  def doYieldtoTrue(expression: in.handyman.dsl.Expression) = {
    expression match {
      case null => {
        true
      }
      case _ => {
        val lhs = expression.getLhs
        val rhs = expression.getRhs
        val oper = expression.getOperator
        oper match {
          case "<" => {
            val lhsInt = Integer.parseInt(lhs)
            val rhsInt = Integer.parseInt(rhs)

            lhsInt < rhsInt
          }
          case ">" => {
            val lhsInt = Integer.parseInt(lhs)
            val rhsInt = Integer.parseInt(rhs)

            lhsInt > rhsInt

          }
          case "==" => {
            lhs.equals(rhs)
          }
          case "contains" => {
            lhs.contains(rhs)
          }
        }
      }
    }

  }
}
