package in.handyman.process.onethread

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.ArrayList

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.util.ParameterisationEngine


/**
 * TODO - Still need to add more rich ness to audit trail with respect to statement warnings
 "put", "get", "del", "list", "chmod", "mkdir", "rmdir"
 */
class FTPAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]
  val client = new FTPClient

  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    //Setting up the proxy for retrieving configuration for the macro
    val ftpAsIs: in.handyman.dsl.FTP = action.asInstanceOf[in.handyman.dsl.FTP]
    val ftp: in.handyman.dsl.FTP = CommandProxy.createProxy(ftpAsIs, classOf[in.handyman.dsl.FTP], context)

    //Retrieving the global config map for default value
    val configMap = ConfigurationService.getGlobalconfig()

    val instanceId = context.getValue("process-id")

    val name = ftp.getName
    var localDir = ftp.getLocalDir
    val remoteDir = ftp.getRemoteDir
    val action1 = ftp.getAction
    
    action1 match {
      case "listFiles"  => listFiles
      case "listFileNames"  => listFileNames
      case "cd"  => cd(remoteDir)
      /*case "downloadFileStream"  => downloadFileStream
      case "downloadAllFiles"  => downloadAllFiles(remoteDir, localDir)
      case "deleteFile"  => deleteFile
      case "removeDir"  => removeDir
      case "downloadFile"  => downloadFile
      case "uploadFile"  => uploadFile
      case "login" => login
      case "connect" => connect*/
    }
    
    context
  }
  
  def login(username: String, password: String) = {
    client.login(username, password)
  }

  def connect(host: String) = {
    client.connect(host)
  }

  def connected: Boolean = client.isConnected

  def disconnect(): Unit = client.disconnect()

  def canConnect(host: String): Boolean = {
    client.connect(host)
    val connectionWasEstablished = connected
    client.disconnect()
    connectionWasEstablished
  }

  def listFiles(): List[FTPFile] =
    client.listFiles.toList

  def listFileNames(): ArrayList[String] = {
    val ftpFiles : List[FTPFile] = listFiles()
    val ftpFileNames : ArrayList[String] = new ArrayList[String];
    
    for(fFile <- ftpFiles){
      if (fFile.isFile()) {
        ftpFileNames.add(fFile.getName);
      }
    }
    
    ftpFileNames
  }

  def cd(path: String): Boolean =
    client.changeWorkingDirectory(path)

  def filesInCurrentDirectory: Seq[String] =
    listFiles().map(_.getName)

  def downloadFileStream(remote: String): InputStream = {
    val stream = client.retrieveFileStream(remote)
    client.completePendingCommand()
    stream
  }

  def downloadAllFiles(remote: String, localDir: String) = {
    val ftpFiles : List[FTPFile] = listFiles()
    val ftpFileNames : ArrayList[String] = new ArrayList[String];
    
    for(fFile <- ftpFiles){
      if (fFile.isFile()) {
        val outfile : File = new File(localDir + "/" + fFile.getName());
        outfile.createNewFile();
         
        val output : FileOutputStream = new FileOutputStream(outfile);

        client.retrieveFile(fFile.getName(), output);
        output.close();
        
        logger.info(s"File " + fFile.getName()+" Download Successfull");
      }
    }
  }

  def deleteFile(remote: String): Boolean = {
    client.deleteFile(remote)
  }
  
  def removeDir(remote: String): Boolean = {
    client.removeDirectory(remote)
  }
    
  def downloadFile(remote: String): Boolean = {
    val os = new FileOutputStream(new File(remote))
    client.retrieveFile(remote, os)
  }
  
  def uploadFile(remote: String, localFileFullName: String) = {
    var localFile : File = new File(localFileFullName);
    var input : FileInputStream = new FileInputStream(localFile);
                 
    if(client.storeFile(localFile.getName(), input)){
        logger.info(s"File Upload Successfull");
    }
                 
    input.close();
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean =
    {
      val ftpAsIs: in.handyman.dsl.FTP = action.asInstanceOf[in.handyman.dsl.FTP]
      val ftp: in.handyman.dsl.FTP = CommandProxy.createProxy(ftpAsIs, classOf[in.handyman.dsl.FTP], context)

      val expression = ftp.getCondition
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