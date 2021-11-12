package in.handyman.process.onethread

import java.util.concurrent.Callable
import com.typesafe.scalalogging.LazyLogging
import in.handyman.server.ProcessAST
import in.handyman.server.RuntimeContext
import in.handyman.server.ProcessExecutor
import in.handyman.util.ExceptionUtil
import java.util.concurrent.CountDownLatch

class ForkProcessCallable(runtimeContext:RuntimeContext, countDownLatch: CountDownLatch) extends Callable[Void] with LazyLogging {
  
  def call(): Void = {
     try {
        //TODO - change to create(config.get("runmode"))
        val runtime = ProcessExecutor.execute("in.handyman.process.onethread", runtimeContext)

      } catch {
        case ex: Throwable => {
          handleError(ex)
          throw ex
        }

      } finally {
        val onFinally = runtimeContext.process.getFinally
        handleFinally()
        countDownLatch.countDown
      }
    ???
  }
  
   def handleError(ex: Throwable) = {
    logger.info("Error section of call process", ex)
    throw ex
  }

  def handleFinally() = {
    logger.info("Finally section of call process")
  }

}