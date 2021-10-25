package in.handyman.process.onethread

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.util.Random
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable.HashMap
import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.LinkedHashSet

import com.typesafe.scalalogging.LazyLogging

import in.handyman.HandymanException
import in.handyman.audit.AuditService
import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ResourceAccess

/*process "test.writecsv.process"
{
	try{
		 writecsv as "test-writecsv" from "sme-cub" to "/home/thamaraiselvi/Desktop/test_writecsv.csv" with "," fielding "10" with-fetch-batch-size 
		 "50000" with-write-batch-size "10" using
		 {
			"SELECT * FROM CollateralDetails;"
		 } 
	}
	catch{}
	finally{}
}*/

/**
 * TODO - Still need to add more rich ness to audit trail with respect to statement warnings
 */
class WriteCsvAction extends in.handyman.command.Action with LazyLogging {

  val rowQueueMap: LinkedHashMap[Int, BlockingQueue[Row]] = new LinkedHashMap[Int, BlockingQueue[Row]];
  val executor: ExecutorService = Executors.newCachedThreadPool;
  val detailMap = new java.util.HashMap[String, String]
  var rowsProcessed: AtomicInteger = new AtomicInteger(0)
  val rand: Random = new Random()

  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    //Setting up the proxy for retrieving configuration for the macro
    val writecsvAsIs: in.handyman.dsl.WriteCsv = action.asInstanceOf[in.handyman.dsl.WriteCsv]
    val writecsv: in.handyman.dsl.WriteCsv = CommandProxy.createProxy(writecsvAsIs, classOf[in.handyman.dsl.WriteCsv], context)

    //Retrieving the global config map for default value
    val configMap = ConfigurationService.getGlobalconfig()

    val instanceId = context.getValue("process-id")

    val targetFile: File = new File(writecsvAsIs.getTo)
    val name = writecsvAsIs.getName
    val delim = writecsvAsIs.getDelim
    
    val fop: FileOutputStream = new FileOutputStream(targetFile)
    val out: Writer = new OutputStreamWriter(new BufferedOutputStream(fop))
    
    //retrieving the insert into sql statement
    val selectStatementAsIs = writecsvAsIs.getValue.replaceAll("\"", "")
    val select = {
      if (selectStatementAsIs.trim.isEmpty())
        throw new HandymanException("SELECT .... cannot be empty for writetocsv for " + name)
      else
        selectStatementAsIs.trim
    }
    
    val source = {
      if (writecsvAsIs.getSource.trim.isEmpty()) {
        throw new HandymanException("source data source cannot be empty for copydata for " + name)
      }
      writecsvAsIs.getSource.trim
    }

    val target = {
      if (writecsvAsIs.getTo.trim.isEmpty()) {
        throw new HandymanException("target data source cannot be empty for copydata for " + name)
      }
      writecsvAsIs.getTo.trim
    }
    logger.info("WriteCsv id#{}, name#{}, from#{}, sqlList#{}", instanceId, name, source, select)
    
    val fetchSize: Int = {
      if (!writecsvAsIs.getFetchBatchSize.isEmpty && writecsvAsIs.getFetchBatchSize.toInt > 0)
        writecsvAsIs.getFetchBatchSize.toInt
      else {
        configMap.getOrElse(Constants.READSIZE, Constants.DEFAULT_READ_SIZE).toInt
      }
    }

    val writeSize = {
      if (!writecsvAsIs.getWriteBatchSize.isEmpty && writecsvAsIs.getWriteBatchSize.toInt > 0)
        writecsvAsIs.getWriteBatchSize.toInt
      else {
        configMap.getOrElse(Constants.WRITESIZE, Constants.DEFAULT_WRITE_SIZE).toInt
      }
    }

    val threadCount: Int = {
      if (!writecsvAsIs.getWriteThreadCount.isEmpty && writecsvAsIs.getWriteThreadCount.toInt > 0)
        writecsvAsIs.getWriteThreadCount.toInt
      else {
        configMap.getOrElse(Constants.WRITERTHREAD, Constants.DEFAULT_WRITER_COUNT).toInt
      }
    }
    
    val upperThreadCount = threadCount
    val lowerThreadCount = 1

    logger.info(s"WriteCsv action input variables id:$instanceId,name: $name, source-database:$source, target-database:$target, fetchSize:$fetchSize, writeSize:$writeSize,threadCount:$threadCount ")

    //initializing the connection related statement
    val sourceConnection = ResourceAccess.rdbmsConn(source)
    val stmt = sourceConnection.createStatement
    stmt.setFetchSize(fetchSize)
    /*val statementId = AuditService.insertStatementAudit(actionId, "writecsv->" + name, context.getValue("process-name"))
    //Updating the
    AuditService.updateStatementAudit(statementId, -1, 0, select, 1)*/

    //Prepping up the parallelization framework

    val prepOut = prepWokerPool(configMap, out, writecsvAsIs, threadCount, instanceId)
    val countDownLatch = prepOut._1
    val workerPool = prepOut._2

    //Retrieving the data from the source
    val rs: ResultSet = stmt.executeQuery(select)
    val rsmd = rs.getMetaData
    val nrCols = rsmd.getColumnCount

    while (rs.next()) {
    
      val startTime = System.currentTimeMillis
      val columnSet: LinkedHashSet[ColumnInARow] = new LinkedHashSet[ColumnInARow]
      val id = rs.getRow
      for (i <- 1 to nrCols) {
        val column: ColumnInARow = createColumn(i, rs, rsmd, nrCols)
        columnSet.add(column)
      }

      val row = new Row(id, columnSet)
      val queuNumber = rand.nextInt((upperThreadCount-lowerThreadCount)+1)+lowerThreadCount
      val rowQueue = rowQueueMap.get(queuNumber).get
      rowQueue.add(row)
      rowsProcessed.incrementAndGet
      
      /*if (rowsProcessed.incrementAndGet % fetchSize == 0) {
        val endTime = System.currentTimeMillis
        val timeTaken = endTime - startTime        
        //Taken care of batch audit
        AuditService.insertBatchAudit(statementId, name, instanceId.toInt, rowsProcessed.get, timeTaken.toInt)
      }*/
      
    }
   
    rowQueueMap.foreach((kv) => {
      val rowQueue = kv._2
      val row = new Row(kv._1, null)
      rowQueue.add(row)
    })

    try {
      countDownLatch.await();
      workerPool.foreach((kv)=>{
        val worker = kv._2
        logger.info(s"CopydataIntoCsv:$instanceId cleaning up worker:$worker with poison pill:$kv._1")
        worker.cleanup
      })
      
      
    } catch {
      case ex: InterruptedException => {
        logger.error(s"CopydataIntoCsv:$instanceId error during waiting for worker threads to finish their job", ex)
        throw ex
      }
    } finally {
      detailMap.put("name", name)
      detailMap.put("source", source)
      detailMap.put("destination", target)
      detailMap.put("rows-processed", String.valueOf(rowsProcessed.intValue));
      context.addValue("rows-processed", String.valueOf(rowsProcessed.intValue));

      try {
        if (out != null)
          out.close
        if (fop != null)
          fop.close
        if (rs != null)
          rs.close
        if (stmt != null)
          stmt.close
        if (sourceConnection != null)
          sourceConnection.close
      } catch {
        case ex: Throwable => {
          logger.error(s"CopydataIntoCsv:$instanceId error closing source connection for database:$source", ex)
        }
      }

    }
    context
  }

  def prepWokerPool(configMap: Map[String, String], out: Writer, writeCsv: in.handyman.dsl.WriteCsv, threadCount: Int, instanceId: String): Tuple2[CountDownLatch,HashMap[Row,CopyDataCsvWriter]] = {
    val countDownLatch: CountDownLatch = new CountDownLatch(threadCount);    
    val workerPool:HashMap[Row,CopyDataCsvWriter] = new HashMap[Row, CopyDataCsvWriter]
    
    for (i <- 1 to threadCount) {
      val rowQueue = new LinkedBlockingDeque[Row];
      val poisonPill: Row = new Row(i, null)   
      logger.info(s"CopydataIntoCsv action is prepping up writer thread with poison pill:$poisonPill")
      val csvWriter: CopyDataCsvWriter = new CopyDataCsvWriter(configMap, out, poisonPill, rowQueue, writeCsv, instanceId, countDownLatch)
      workerPool.put(poisonPill, csvWriter)
      this.executor.submit(csvWriter)
      rowQueueMap.put(poisonPill.rowId, rowQueue)

    }
    new Tuple2(countDownLatch, workerPool)
  }

  def createColumn(i: Int, rs: ResultSet, rsmd: ResultSetMetaData, nrCols: Int): ColumnInARow = {

    //case class ColumnInARow(columnType: String, columnTypeName: String, columnName: String, columnLabel: String, scale: Integer,
    //value: Object, columnFunction:String, isLastColumn:Boolean)
    val columnType = rsmd.getColumnType(i)
    val columnTypeName = rsmd.getColumnTypeName(i)
    val columnName = rsmd.getColumnName(i)
    val columnLabel = rsmd.getColumnLabel(i)
    val scale: Int = rsmd.getScale(i)
    val value = rs.getObject(i)
    val isLastColumn: Boolean = {
      if (i == nrCols)
        true
      else
        false
    }
    val column: ColumnInARow = new ColumnInARow(columnType, columnTypeName, columnName, columnLabel, scale, value, null, isLastColumn)
    column
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean =
    {
      //Setting up the proxy for retrieving configuration for the macro
      val writecsvAsIs: in.handyman.dsl.WriteCsv = action.asInstanceOf[in.handyman.dsl.WriteCsv]
      val writecsv: in.handyman.dsl.WriteCsv = CommandProxy.createProxy(writecsvAsIs, classOf[in.handyman.dsl.WriteCsv], context)

      val expression = writecsv.getCondition
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