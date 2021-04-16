package in.handyman.process.onethread

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import org.slf4j.MarkerFactory

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ResourceAccess


/**
 * TODO - Still need to add more rich ness to audit trail with respect to statement warnings
 */
class UnzipAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "UNZIP";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    //Setting up the proxy for retrieving configuration for the macro
    val unZipAsIs: in.handyman.dsl.Unzip = action.asInstanceOf[in.handyman.dsl.Unzip]
    val unZip: in.handyman.dsl.Unzip = CommandProxy.createProxy(unZipAsIs, classOf[in.handyman.dsl.Unzip], context)

    //Retrieving the global config map for default value
    val configMap = ConfigurationService.getGlobalconfig()
    
    val instanceId = context.getValue("process-id")
    
    val destDir = unZip.getDestDir
    val name = unZip.getName
    val source = unZip.getSource
    val bufferSize : Int = Integer.valueOf(unZip.getBufferSize)
    val sql = unZip.getValue.trim
    
    val sqlList = sql.replaceAll("\"", "").split(";")
    logger.info(aMarker, "Fetch id#{}, name#{}, sql#{}, db=#{}", instanceId, name, sqlList, source)
    val conn = ResourceAccess.rdbmsConn(source)
    val stmt = conn.createStatement
    
    detailMap.put("source", source)
    detailMap.put("destDir", destDir)
    try {
      sqlList.foreach(sqlItem => {
          logger.info(aMarker, "Execution query sql#{} on db=#{}", sqlItem.trim(), source)
          val rs = stmt.executeQuery(sqlItem.trim)
          val columnCount = rs.getMetaData.getColumnCount

          while (rs.next()) {
              val zipFilePath = rs.getString(1)
              
              unZipFile(destDir, zipFilePath, bufferSize);
          }       
          detailMap.put("sql", sqlItem)
      })
    } finally {
      stmt.close
      conn.close
    }
    
    context
  }
  
  def unZipFile(destDirectory: String, zipFilePath: String, bufferSize : Int) = {
    val destDir : File = new File(destDirectory);
    if (!destDir.exists()) {
        destDir.mkdir();
    }
    val zipIn : ZipInputStream = new ZipInputStream(new FileInputStream(zipFilePath));
    var entry : ZipEntry = zipIn.getNextEntry();
    // iterates over entries in the zip file
    while (entry != null) {
        val filePath : String= destDir + File.separator + entry.getName();
        if (!entry.isDirectory()) {
            extractFile(zipIn, filePath, bufferSize);
        } else {
            // if the entry is a directory, make the directory
            val dir : File = new File(filePath);
            dir.mkdirs();
        }
        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
    }
    zipIn.close();
  }

  def extractFile(zipIn: ZipInputStream, filePath: String, bufferSize : Int) = {
    var bos : BufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath))
    var bytesIn : Array[Byte] = new Array[Byte](bufferSize)
    var len : Int = zipIn.read(bytesIn);
    while (len > 0) {
      bos.write(bytesIn, 0, len)
      len = zipIn.read(bytesIn);
    }
    bos.close()
  }
  
  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean =
    {
      val unZipAsIs: in.handyman.dsl.Unzip = action.asInstanceOf[in.handyman.dsl.Unzip]
      val unzip: in.handyman.dsl.Unzip = CommandProxy.createProxy(unZipAsIs, classOf[in.handyman.dsl.Unzip], context)

      val expression = unzip.getCondition
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