package in.handyman.process.onethread

import in.handyman.command.Action

object CommandFactory {

  def create(name: String): Action = {

    name.toLowerCase match {
      case "abort" => new AbortAction
      case "transform" => new TransformAction
      case "fetch" => new FetchVariableAction
      case "callprocess" => new CallProcessAction
      case "forkprocess" => new ForkProcessAction
      case "doozle" => new DoozleAction
      case "sendemail" => new SendEMailAction
      case "execjava" => new JavaAction
      case "loadcsv" => new LoadCsvIntoDbAction
      case "deletefolder" => new DeleteFolderAction
      case "copydata" => new CopydataAction
      case "terminal" => new TerminalAction
      case "writecsv" => new WriteCsvAndTsvAction
      case "mongo2db" => new Mongo2DbAction
      case "ftp" => new FTPAction
      case "zip" => new ZipAction
      case "unzip" => new UnzipAction
      case "checksum" => new ChecksumAction
      case "randforestregressmodeltraining" => new RandForestRegressModelTrainingAction
      case "randforestregressmodeltesting" => new RandForestRegressModelTestingAction
      case "randforestregressmodelevaluation" => new RandForestRegressModelEvaluationAction
      case "randforestclassifymodeltraining" => new RandForestClassifyModelTrainingAction
      case "randforestclassifymodeltesting" => new RandForestClassifyModelTestingAction
      case "randforestclassifymodelevaluation" => new RandForestClassifyModelEvaluationAction
      case "gradboostregressmodeltraining" => new GradBoostRegressModelTrainingAction
      case "gradboostregressmodeltesting" => new GradBoostRegressModelTestingAction
      case "gradboostregressmodelevaluation" => new GradBoostRegressModelEvaluationAction
      case "gradboostclassifymodeltraining" => new GradBoostClassifyModelTrainingAction
      case "gradboostclassifymodeltesting" => new GradBoostClassifyModelTestingAction
      case "gradboostclassifymodelevaluation" => new GradBoostClassifyModelEvaluationAction
      case "jsontransform" => new JsonTransformAction
      case "callrestapi" => new RestApiAction
    }
  }
}