package in.handyman.process.onethread

import in.handyman.command.Action

object CommandFactory {
                  
  def create(name:String) :Action={
      
    name.toLowerCase match {
      case "abort" =>  new AbortAction
      case "googlecal" => new GoogleCalendarAction
      case "transform" => new TransformAction
      case "assign" => new FetchVariableAction
      case "callprocess" => new CallProcessAction
      case "doozle"=>new DoozleAction     
      case "fbclead"=>new FBLeadCreateAction
      case "googlecontactput"=>new GContactCreateAction
      case "googlecalput"=>new GoogleCalendarAction     
      case "fetch"=>new FetchVariableAction
      case "smsleadsms"=>new SmsLeadsAction          
    }
  }
}