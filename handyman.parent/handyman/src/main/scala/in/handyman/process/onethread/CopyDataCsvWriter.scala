package in.handyman.process.onethread

import java.io.Writer
import java.util.ArrayList
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch

import scala.util.control.Breaks

import com.typesafe.scalalogging.LazyLogging

class CopyDataCsvWriter(configMap: Map[String, String], out: Writer, poisonPill: Row, rowQueue: BlockingQueue[Row], 
    writeCsv: in.handyman.dsl.WriteCsv, id: String, countDownLatch: CountDownLatch) extends Callable[Void] with LazyLogging {

  val writeBuffer: ArrayList[String] = new ArrayList[String]
  
  val fieldSeparator = {
    if (!writeCsv.getDelim.isEmpty)
      writeCsv.getDelim
    else {
      configMap.getOrElse(Constants.DELIM, Constants.FIELD_SEPARATOR)
    }
  }
  
  val writeSize = {
    if (!writeCsv.getWriteBatchSize.isEmpty && writeCsv.getWriteBatchSize.toInt > 0)
      writeCsv.getWriteBatchSize.toInt
    else {
      configMap.getOrElse(Constants.WRITESIZE, Constants.DEFAULT_WRITE_SIZE).toInt
    }
  }

  def call(): Void = {

    Breaks.breakable {
      while (true) {
        val row = rowQueue.take();
        if (poisonPill.equals(row)) {
          if (!writeBuffer.isEmpty) {
            val WBSize : Int = writeBuffer.size
            logger.debug(s"CopydataCsvWriter(After poison pill) flushing to csv rows:$WBSize")
            writeToCsv
          }
          countDownLatch.countDown
          Breaks.break

        } else {
          val dataFrame = generateDataFrame(row)
          writeBuffer.add(dataFrame)
          if (writeBuffer.size % writeSize == 0) {
            val WBSize : Int = writeBuffer.size
            logger.debug(s"CopydataCsvWriter(Before poison pill) flushing to csv rows:$WBSize")
            writeToCsv
          }
        }
      }
    }

    ???
  }

  def generateDataFrame(row: Row): String = {
    //logger.debug(s"Copydata csv Writer generating dataframe for row:$row")
    val columnSet = row.columnSet
    val dataFrameBuilder = new StringBuilder

    columnSet.foreach(column => {
      val columnType = column.columnTypeName
      val columnValue = column.value
      columnType.toLowerCase match {
        case Constants.STRING_DATATYPE => dataFrameBuilder.append(Constants.DOUBLE_STRING_ENCLOSER).
          append(columnValue).append(Constants.DOUBLE_STRING_ENCLOSER)
        case "datetime" => dataFrameBuilder.append(Constants.DOUBLE_STRING_ENCLOSER).
          append(columnValue).append(Constants.DOUBLE_STRING_ENCLOSER)
        case "timestamp" => dataFrameBuilder.append(Constants.DOUBLE_STRING_ENCLOSER).
          append(columnValue).append(Constants.DOUBLE_STRING_ENCLOSER)
        case _ => dataFrameBuilder.append(columnValue)
      }
      if (!column.isLastColumn)
        dataFrameBuilder.append(fieldSeparator)
    })

    dataFrameBuilder.append(Constants.ROW_SEPARATOR)
    dataFrameBuilder.toString()
  }

  def writeToCsv() = {
    val writeBufferSize : Int = writeBuffer.size()
    try {
      writeBuffer.forEach(row => {
        out.write(row)
      })
    } catch {
      case ex: Exception => {
        logger.error(s"CopydataCsvWriter:$id error writing data into csv:$writeBufferSize", ex)
      }
    } finally {
      try {
        writeBuffer.clear
      } catch {
        case ex: Throwable => {
          ex.printStackTrace
          logger.error(s"CopydataCsvWriter:$id error clearing writebuffer:$writeBufferSize", ex)
        }
      }
    }
  }
  
  def cleanup = {
  }
}