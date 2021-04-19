package in.handyman.process.onethread

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

import org.slf4j.MarkerFactory

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.util.ParameterisationEngine


/**
 * TODO - Still need to add more rich ness to audit trail with respect to statement warnings
 */
class ChecksumAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "CHECKSUM";
  val aMarker = MarkerFactory.getMarker(auditMarker);
  
  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    //Setting up the proxy for retrieving configuration for the macro
    val checksumAsIs: in.handyman.dsl.Checksum = action.asInstanceOf[in.handyman.dsl.Checksum]
    val checksum: in.handyman.dsl.Checksum = CommandProxy.createProxy(checksumAsIs, classOf[in.handyman.dsl.Checksum], context)

    //Retrieving the global config map for default value
    val configMap = ConfigurationService.getGlobalconfig()

    val instanceId = context.getValue("process-id")

    val name = checksum.getName
    var localDir = checksum.getLocalDir
    val localFile = checksum.getLocalFile
    val remoteDir = checksum.getRemoteDir
    val userName = checksum.getUsername
    val password = checksum.getPassword
    val host = checksum.getHost
    val port : Int = Integer.valueOf(checksum.getPort)
    val remoteFile = checksum.getRemoteFile
    val remote = remoteDir.concat(remoteFile)
    val local = localDir.concat(localFile)
    
    detailMap.put("name", name)
    detailMap.put("localDir", localDir)
    detailMap.put("localFile", localFile)
    detailMap.put("remoteDir", remoteDir)
    detailMap.put("userName", userName)
    detailMap.put("password", password)
    detailMap.put("host", host)
    detailMap.put("port", checksum.getPort)
    detailMap.put("remoteFile", remoteFile)
    try{
      val md5Digest : MessageDigest = MessageDigest.getInstance("MD5"); // We can also use SHA-256 instead of MD5 algorithm
      
      val ftpAct : FTPAction = new FTPAction();
      ftpAct.connect(host)
      ftpAct.login(userName, password)
      val remoteFileChecksum : String = ftpAct.findChecksum(md5Digest, remote)
      
      val localFile : File = new File(local);
      val localFileChecksum : String = findChecksum(md5Digest, localFile)
      
      var isEqual : String = "";
      if(localFileChecksum == remoteFileChecksum)
        isEqual = "true"
      else 
        isEqual = "false"
        
      context.addValue(name + "." + remoteFile, isEqual)
      detailMap.put(name + "." + remoteFile, isEqual)
      
      logger.info(aMarker, "Checksum id#{}, name#{}, remoteFile#{}, localFile=#{}, isEqual=#{}", 
          instanceId, name, remoteFileChecksum, localFileChecksum, isEqual)          
    }finally {
    }
    context
  }
  
  def findChecksum(digest : MessageDigest, file : File) : String = {
    //Get file input stream for reading the file content
    val fis : FileInputStream = new FileInputStream(file);
     
    //Create byte array to read data in chunks
    val byteArray : Array[Byte] = new Array[Byte](1024);
    var bytesCount : Int = 0; 
      
    //Read file data and update in message digest
    bytesCount = fis.read(byteArray);
    while (bytesCount > 0) {
      digest.update(byteArray, 0, bytesCount)
      bytesCount = fis.read(byteArray);
    }
     
    //close the stream; We don't need it now.
    fis.close();
     
    //Get the hash's bytes
    val bytes : Array[Byte] = digest.digest();
     
    //This bytes[] has bytes in decimal format;
    //Convert it to hexadecimal format
    var sb : StringBuilder = new StringBuilder();
    bytes.foreach(byt => sb.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1)))
     
    //return complete hash
    sb.toString();
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean =
    {
      val checksumAsIs: in.handyman.dsl.Checksum = action.asInstanceOf[in.handyman.dsl.Checksum]
      val checksum: in.handyman.dsl.Checksum = CommandProxy.createProxy(checksumAsIs, classOf[in.handyman.dsl.Checksum], context)

      val expression = checksum.getCondition
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