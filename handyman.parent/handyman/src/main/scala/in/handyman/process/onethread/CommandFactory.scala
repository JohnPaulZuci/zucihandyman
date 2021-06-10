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
      case "jsontransform" => new JsonTransformAction
      case "jsondeserialize" => new JsonDeserializeAction
      case "restapi" => new RestApiAction
      case "python" => new PythonAction
      case "listfiles" => new ListFilesAction
      case "insertsql" => new InsertSqlAction
      case "updatesql" => new UpdateSqlAction
      case "dropsql" => new DropSqlAction
      case "deletesql" => new DeleteSqlAction
      case "truncatesql" => new TruncateSqlAction
    }
  }
}