package in.handyman.process.onethread

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
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.insert.Insert




/**
 * TODO - Still need to add more rich ness to audit trail with respect to statement warnings
 */
class CopydataAction extends in.handyman.command.Action with LazyLogging {

  val rowQueueMap: LinkedHashMap[Int, BlockingQueue[Row]] = new LinkedHashMap[Int, BlockingQueue[Row]];
  val executor: ExecutorService = Executors.newCachedThreadPool;
  val detailMap = new java.util.HashMap[String, String]
  var rowsProcessed: AtomicInteger = new AtomicInteger(0)
  val rand: Random = new Random()

  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    //Setting up the proxy for retrieving configuration for the macro
    val copydataAsIs: in.handyman.dsl.Copydata = action.asInstanceOf[in.handyman.dsl.Copydata]
    val copydata: in.handyman.dsl.Copydata = CommandProxy.createProxy(copydataAsIs, classOf[in.handyman.dsl.Copydata], context)

    //Retrieving the global config map for default value
    val configMap = ConfigurationService.getGlobalconfig()

    val instanceId = context.getValue("process-id")

    val name = copydata.getName

    val source = {
      if (copydata.getSource.trim.isEmpty()) {
        throw new HandymanException("source data source cannot be empty for copydata for " + name)
      }
      copydata.getSource.trim

    }

    val target = {
      if (copydata.getTo.trim.isEmpty()) {
        throw new HandymanException("target data source cannot be empty for copydata for " + name)
      }
      copydata.getTo.trim

    }

    val fetchSize: Int = {
      if (!copydata.getFetchBatchSize.isEmpty && copydata.getFetchBatchSize.toInt > 0)
        copydata.getFetchBatchSize.toInt
      else {
        configMap.getOrElse(Constants.READSIZE, Constants.DEFAULT_READ_SIZE).toInt
      }
    }

    val writeSize = {
      if (!copydata.getWriteBatchSize.isEmpty && copydata.getWriteBatchSize.toInt > 0)
        copydata.getWriteBatchSize.toInt
      else {
        configMap.getOrElse(Constants.WRITESIZE, Constants.DEFAULT_WRITE_SIZE).toInt
      }
    }

    val threadCount: Int = {
      if (!copydata.getWriteThreadCount.isEmpty && copydata.getWriteThreadCount.toInt > 0)
        copydata.getWriteThreadCount.toInt
      else {
        configMap.getOrElse(Constants.WRITERTHREAD, Constants.DEFAULT_WRITER_COUNT).toInt
      }
    }
    
    val upperThreadCount = threadCount
    val lowerThreadCount = 1

    //retrieving the insert into sql statement
    val insertStatementAsIs = copydata.getValue.replaceAll("\"", "")
    val insertStatement = {
      if (insertStatementAsIs.trim.isEmpty())
        throw new HandymanException("INSERT INTO SELECT .... cannot be empty for copydata for " + name)
      else
        insertStatementAsIs.trim
    }
    val insert = CCJSqlParserUtil.parse(insertStatement).asInstanceOf[Insert]
    val select = insert.getSelect

    val targetTable = insert.getTable

    logger.info(s"Copydata action input variables id:$instanceId,name: $name, source-database:$source, target-database:$target, fetchSize:$fetchSize, writeSize:$writeSize,threadCount:$threadCount ")
    logger.info(s"Copydata Insert Sql input post parameter ingestion \n :$insert")
    logger.info(s"Copydata Select Sql input post parameter ingestion \n :$select")

    //initializing the connection related statement
    val sourceConnection = ResourceAccess.rdbmsConn(source)
    val stmt = sourceConnection.createStatement
    stmt.setFetchSize(fetchSize)
    val statementId = AuditService.insertStatementAudit(actionId, "copydata->" + name, context.getValue("process-name"))
    //Updating the
    AuditService.updateStatementAudit(statementId, -1, 0, insertStatement, 1)

    //Prepping up the parallelization framework

    val prepOut = prepWokerPool(configMap, insert, copydata, threadCount, instanceId)
    val countDownLatch = prepOut._1
    val workerPool = prepOut._2

    //Retrieving the data from the source
    val rs: ResultSet = stmt.executeQuery(select.toString)
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
      
      if (rowsProcessed.incrementAndGet % fetchSize == 0) {
        val endTime = System.currentTimeMillis
        val timeTaken = endTime - startTime        
        //Taken care of batch audit
        AuditService.insertBatchAudit(statementId, name, instanceId.toInt, rowsProcessed.get, timeTaken.toInt)
      }
      
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
        logger.info(s"Copydata:$instanceId cleaning up worker:$worker with poison pill:$kv._1")
        worker.cleanup
      })
      
      
    } catch {
      case ex: InterruptedException => {
        logger.error(s"Copydata:$instanceId error during waiting for worker threads to finish their job", ex)
        throw ex
      }
    } finally {
      detailMap.put("name", name)
      detailMap.put("source", source)
      detailMap.put("destination", target)
      detailMap.put("ddlSql", insertStatementAsIs)
      detailMap.put("rows-processed", String.valueOf(rowsProcessed.intValue));
      context.addValue("rows-processed", String.valueOf(rowsProcessed.intValue));

      try {
        if (rs != null)
          rs.close
        if (stmt != null)
          stmt.close
        if (sourceConnection != null)
          sourceConnection.close
      } catch {
        case ex: Throwable => {
          logger.error(s"Copydata:$instanceId error closing source connection for database:$source", ex)
        }
      }

    }
    context
  }

  def prepWokerPool(configMap: Map[String, String], insert: Insert, copydata: in.handyman.dsl.Copydata, threadCount: Int, instanceId: String): Tuple2[CountDownLatch,HashMap[Row,CopyDataJdbcWriter]] = {
    val countDownLatch: CountDownLatch = new CountDownLatch(threadCount);    
    val workerPool:HashMap[Row,CopyDataJdbcWriter] = new HashMap[Row, CopyDataJdbcWriter]
    
    for (i <- 1 to threadCount) {
      val rowQueue = new LinkedBlockingDeque[Row];
      val poisonPill: Row = new Row(i, null)   
      logger.info(s"Copydata action is prepping up writer thread with poison pill:$poisonPill")
      val jdbcWriter: CopyDataJdbcWriter = new CopyDataJdbcWriter(configMap, insert, poisonPill, copydata, instanceId, rowQueue, countDownLatch)
      workerPool.put(poisonPill, jdbcWriter)
      this.executor.submit(jdbcWriter)
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
      val copyDataAsIs: in.handyman.dsl.Copydata = action.asInstanceOf[in.handyman.dsl.Copydata]
      val copyData: in.handyman.dsl.Copydata = CommandProxy.createProxy(copyDataAsIs, classOf[in.handyman.dsl.Copydata], context)

      val expression = copyData.getCondition
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