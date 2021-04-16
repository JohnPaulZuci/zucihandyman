package in.handyman.process.onethread

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import org.slf4j.MarkerFactory

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ResourceAccess



/**
 * TODO - Still need to add more rich ness to audit trail with respect to statement warnings
 */
class ZipAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "ZIP";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    //Setting up the proxy for retrieving configuration for the macro
    val zipAsIs: in.handyman.dsl.Zip = action.asInstanceOf[in.handyman.dsl.Zip]
    val zip: in.handyman.dsl.Zip = CommandProxy.createProxy(zipAsIs, classOf[in.handyman.dsl.Zip], context)

    //Retrieving the global config map for default value
    val configMap = ConfigurationService.getGlobalconfig()

    val instanceId = context.getValue("process-id")
    
    val zipFileName = zip.getZipFileName
    val zipFilePath = zip.getZipFilePath
    val name = zip.getName
    val source = zip.getSource
    val bufferSize : Int = Integer.valueOf(zip.getBufferSize)
    val sql = zip.getValue.trim
    
    val sqlList = sql.replaceAll("\"", "").split(";")
    logger.info(aMarker, "Fetch id#{}, name#{}, sql#{}, db=#{}", instanceId, name, sqlList, source)
    val conn = ResourceAccess.rdbmsConn(source)
    val stmt = conn.createStatement
    
    detailMap.put("zipFileName", zipFileName)
    detailMap.put("zipFilePath", zipFilePath)
    detailMap.put("source", source)
    
    try {
      sqlList.foreach(sqlItem => {
          logger.info(aMarker, "Execution query sql#{} on db=#{}", sqlItem.trim(), source)
          val rs = stmt.executeQuery(sqlItem.trim)
          val columnCount = rs.getMetaData.getColumnCount

          while (rs.next()) {
              val sourceFile = rs.getString(1)
              
              if(!sourceFile.contains(",")){
               val fileToZip : File = new File(sourceFile);
               if(fileToZip.isDirectory())
                 zipDirectory(sourceFile, zipFilePath, zipFileName, bufferSize)
               else
                 zipSingleFile(sourceFile, zipFilePath, zipFileName)
              }else if(sourceFile.contains(","))
                 zipMultipleFiles(sourceFile.split(","), zipFilePath, zipFileName)
          }       
          detailMap.put("sql", sqlItem)
      })
    } finally {

      stmt.close
      conn.close
    }

    context
  }
  
  def zipSingleFile(sourceFile: String, zipFilePath: String, zipFileName: String) {
    val file : File = new File(sourceFile);
    val zipFile : String = zipFilePath.concat(zipFileName).concat(".zip")
   
    val fos : FileOutputStream = new FileOutputStream(zipFile);
    val zos : ZipOutputStream = new ZipOutputStream(fos);
 
    zos.putNextEntry(new ZipEntry(file.getName()));
 
    val bytes: Array[Byte] = Files.readAllBytes(Paths.get(sourceFile));
    zos.write(bytes, 0, bytes.length);
    zos.closeEntry();
    zos.close();
    fos.close();
  }
  
  def zipMultipleFiles(sourceFiles: Array[String], zipFilePath: String, zipFileName: String) {
    val zipFile : String = zipFilePath.concat(zipFileName).concat(".zip")
   
    val fos: FileOutputStream = new FileOutputStream(zipFile);
    val zos : ZipOutputStream = new ZipOutputStream(fos);
 
    sourceFiles.foreach { aFile =>
      zos.putNextEntry(new ZipEntry(new File(aFile).getName()));
 
      val bytes: Array[Byte] = Files.readAllBytes(Paths.get(aFile));
      zos.write(bytes, 0, bytes.length);
      zos.closeEntry();
    }
 
    zos.close();
    fos.close();
  }
  
  def zipDirectory(sourceDir: String, zipFilePath: String, zipFileName: String, bufferSize : Int) {
    val zipFileFullPath : String = zipFilePath.concat(zipFileName).concat(".zip")
    val fos : FileOutputStream = new FileOutputStream(zipFileFullPath);
    val zipOut : ZipOutputStream = new ZipOutputStream(fos);
    val fileToZip : File = new File(sourceDir);

    zipFile(fileToZip, fileToZip.getName(), zipOut, bufferSize);
    zipOut.close();
    fos.close();
  }

  def zipFile(fileToZip : File, fileName : String, zipOut : ZipOutputStream, bufferSize : Int) {
    if (fileToZip.isHidden()) {
        return;
    }
    if (fileToZip.isDirectory()) {
        if (fileName.endsWith("/")) {
            zipOut.putNextEntry(new ZipEntry(fileName));
            zipOut.closeEntry();
        } else {
            zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            zipOut.closeEntry();
        }
        val children : Array[File] = fileToZip.listFiles();
        children.foreach { childFile =>
            zipFile(childFile, fileName + "/" + childFile.getName(), zipOut, bufferSize);
        }
        return;
    }
    val fis : FileInputStream = new FileInputStream(fileToZip);
    val zipEntry : ZipEntry = new ZipEntry(fileName);
    zipOut.putNextEntry(zipEntry);
    val bytes : Array[Byte] = new Array[Byte](bufferSize);
    var read : Int = 0
    read = fis.read(bytes);
    while (read > 0) {
      zipOut.write(bytes, 0, read)
      read = fis.read(bytes);
    }
    fis.close();
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean =
    {
      val zipAsIs: in.handyman.dsl.Zip = action.asInstanceOf[in.handyman.dsl.Zip]
      val zip: in.handyman.dsl.Zip = CommandProxy.createProxy(zipAsIs, classOf[in.handyman.dsl.Zip], context)

      val expression = zip.getCondition
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