package in.handyman.process.onethread

import com.typesafe.scalalogging.LazyLogging
import in.handyman.dsl.Action
import in.handyman.command.Context
import in.handyman.command.CommandProxy
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ExceptionUtil
import org.slf4j.MarkerFactory
import java.sql.SQLSyntaxErrorException
import in.handyman.audit.AuditService
import scala.reflect.io.Directory
import java.io.File
import scala.collection.immutable.HashMap
import java.nio.file.attribute.FileTime
import java.text.SimpleDateFormat
import java.nio.file.attribute.BasicFileAttributes
import java.io.File

import java.io.IOException

import java.nio.file.Files

import java.nio.file.attribute.BasicFileAttributes

import java.sql.Connection

import java.sql.Date

import java.sql.DriverManager

import java.sql.SQLException

import java.sql.Statement

import java.text.SimpleDateFormat

import java.util.HashMap

import org.apache.commons.io.FileUtils

import org.apache.commons.io.FilenameUtils
import scala.util.control.Exception.Catch
import java.io.FileNotFoundException
import in.handyman.util.ResourceAccess

class ListFilesAction extends in.handyman.command.Action with LazyLogging {
	val detailMap = new java.util.HashMap[String, String]
			val auditMarker = "LISTFILES";
	val aMarker = MarkerFactory.getMarker(auditMarker);

	def execute(context: Context, action: Action, actionId:Integer): in.handyman.command.Context ={
			val listfilesAsIs = action.asInstanceOf[in.handyman.dsl.ListFiles]
					val listfiles: in.handyman.dsl.ListFiles = CommandProxy.createProxy(listfilesAsIs, classOf[in.handyman.dsl.ListFiles], context)


					val dbSrc = listfiles.getSource
					val targetTable = listfiles.getTargetTable
					val name = listfiles.getName
					val ddlSql = listfiles.getValue.replaceAll("\"", "")

					val db = listfiles.getTo

					val id = context.getValue("process-id")

					val conn = ResourceAccess.rdbmsConn(db)

					conn.setAutoCommit(false)
					val stmt = conn.createStatement
					try{


						val directory: File = new File(dbSrc)

								var flag: Boolean = true

								var metaData: Array[String] = null


								val afterTrim = ddlSql.replaceAll(" ", "")

								if (afterTrim.equalsIgnoreCase("select")) {
									flag = false
								} 
								else 
								{

									val replace: String = afterTrim.replace("select", "")

											metaData = replace.split(",")
								}

						if (directory.isDirectory) {
							val fileList: Array[File] = directory.listFiles()
									FileRecursion(fileList, stmt, flag, metaData,targetTable,conn,dbSrc)
						}
						else{
							throw new Exception("its not a directory => "+directory);
						}
					}
			catch {
			case ex: Throwable => {
				logger.error(aMarker, "Stopping execution, General Error iexecuting "+ex)
				detailMap.put(".exception", ExceptionUtil.completeStackTraceex(ex))
				throw ex
			}
			}
			finally {
				conn.close()
				stmt.close()
			}


			context


	}

	def FileRecursion(arr: Array[File],
			statement: Statement,
			flag: Boolean,
			metaData: Array[String],targetTable:String,conn :Connection,dbSrc:String): Unit = {
					for (file <- arr) {
						if (file.isFile) {
							val dateDetails: java.util.HashMap[String, String] = FileCreationDateAndTime(
									file)

									val columnValue = new StringBuilder();
						val columnName = new StringBuilder();

						val size: java.lang.Long = FileUtils.sizeOf(file)

								if (!flag) {

									var	file_size = FileUtils.byteCountToDisplaySize(size)

											val sql: String = "insert into "+ targetTable +" values(NULL,'" +
													file.getName +
													"','" +
													dbSrc.replace("\\","\\\\")+
													"','" +
													file.getPath.replace("\\", "\\\\\\\\") +
													"','" +
													file.getParent.replace("\\", "\\\\\\\\") +
													"','" +
													FileUtils.byteCountToDisplaySize(size) +
													"','" +
													dateDetails.get("creationTime") +
													"','" +
													dateDetails.get("lastModifiedTime") +
													"','" +
													dateDetails.get("lastAccessTime") +
													"','" +
													FilenameUtils.getExtension(file.getPath) +
													"','" +
													0 +
													"')"

													statement.execute(sql)
													conn.commit()

								} 
								else {
									for (i <- 0 until metaData.length) {
									  
										columnName.append(metaData(i)+",")
										
										if (metaData(i).==("file_name")) {
											columnValue.append("'" + file.getName + "',")
										} else if (metaData(i).==("file_directory")) {
											columnValue.append("'" +file.getPath.replace("\\", "\\\\\\\\") + "',")
										} else if (metaData(i).==("parent_directory")) {
											columnValue.append("'" +file.getParent.replace("\\", "\\\\\\\\") + "',")
										} else if (metaData(i).==("file_size")) {
											columnValue.append("'" +FileUtils.byteCountToDisplaySize(size) + "',")
										} else if (metaData(i).==("creation_Date")) {
											columnValue.append("'" +dateDetails.get("creationTime") + "',")
										} else if (metaData(i).==("last_modified_Date")) {
											columnValue.append("'" +dateDetails.get("lastModifiedTime") + "',")
										} else if (metaData(i).==("last_access_Date")) {
											columnValue.append("'" +dateDetails.get("lastAccessTime") + "',")
										} else if (metaData(i).==("extension")) {
											columnValue.append("'" +FilenameUtils.getExtension(file.getPath)+ "',")
										}
										else if (metaData(i).==("source_directory")) {
											columnValue.append("'" +dbSrc.replace("\\","\\\\")+ "',")
										}

									}
									columnValue.deleteCharAt(columnValue.length()-1);
									columnName.deleteCharAt(columnName.length()-1);
									val sql: String = "insert into "+ targetTable +"("+columnName.toString()+") values("+columnValue.toString()+")"

									statement.execute(sql)
									conn.commit()

								}

						} else if (file.isDirectory) {
							FileRecursion(file.listFiles(), statement, flag, metaData,targetTable,conn,dbSrc)
						}
					}
	}

	def FileCreationDateAndTime(file: File): java.util.HashMap[String, String] = {
			val dateDetails: java.util.HashMap[String, String] = new java.util.HashMap[String, String]()
					val attr: BasicFileAttributes =
					Files.readAttributes(file.toPath(), classOf[BasicFileAttributes])
					val simpleDateFormat: SimpleDateFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss")
					val creationTime: String =
					simpleDateFormat.format(new Date(attr.creationTime().toMillis()))
					val lastAccessTime: String =
					simpleDateFormat.format(new Date(attr.lastAccessTime().toMillis()))
					val lastModifiedTime: String =
					simpleDateFormat.format(new Date(attr.lastModifiedTime().toMillis()))
					dateDetails.put("creationTime", creationTime)
					dateDetails.put("lastAccessTime", lastAccessTime)
					dateDetails.put("lastModifiedTime", lastModifiedTime)
					dateDetails
	}
	def executeIf(context: Context, action: Action): Boolean = {
			val listfilesAsIs = action.asInstanceOf[in.handyman.dsl.ListFiles]
					val listfiles: in.handyman.dsl.ListFiles = CommandProxy.createProxy(listfilesAsIs, classOf[in.handyman.dsl.ListFiles], context)
					val dbSrc = listfiles.getSource
					val name = listfiles.getName
					val id = context.getValue("process-id")
					val expression = listfiles.getCondition
					try {
						val output = ParameterisationEngine.doYieldtoTrue(expression)
								logger.info(aMarker, "Completed evaluation to execute id#{}, name#{}, dbSrc#{}, expression#{}", id, name, dbSrc, expression)
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