package in.handyman.process.onethread

import in.handyman.command.Action

object CommandFactory {

  def create(name: String): Action = {

    name.toLowerCase match {
      case "abort" => new AbortAction
      case "transform" => new TransformAction
      case "assign" => new FetchVariableAction
      case "callprocess" => new CallProcessAction
      case "doozle" => new DoozleAction
      case "sendemail" => new SendEMailAction
      case "execjava" => new JavaAction
      case "loadcsv" => new LoadCsvIntoDbAction
      case "deletefolder" => new DeleteFolderAction
      case "copydata" => new CopydataAction
      case "terminal" => new TerminalAction
      case "writecsv" => new WriteCsvAndTsvAction
    }
  }
}