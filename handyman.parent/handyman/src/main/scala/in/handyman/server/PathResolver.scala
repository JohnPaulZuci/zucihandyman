package in.handyman.server

import com.typesafe.scalalogging.LazyLogging
import org.slf4j.MarkerFactory

object PathResolver extends LazyLogging {

  val marker = "PROCESS-PATH RESOLVER";
  val pMarker = MarkerFactory.getMarker(marker)

  def resolvePath(processName: String, fileRelativePath: String, basePath: String): String = {
    /* val tempPath = {
      basePath.contains("file:")
    }
    val fullPath = new StringBuilder().append(basePath).
      append("/").append(fileRelativePath).toString()
    println("fullpath=" + fullPath)
    val finalPath = {
      if (basePath.contains("file:")) {

          val basePath1 = basePath.substring("file:".length(),basePath.length())
          val fullPath = new StringBuilder().append(basePath1).
          append("/").append(fileRelativePath).toString()
          logger.info(pMarker, "fullpath {} for process {} with base path as {}" , fullPath,processName , basePath1)
        ""

      } else {

        val fullPath = new StringBuilder().append(basePath).
          append("/").append(fileRelativePath).toString()
        logger.info(pMarker, "fullpath {} for process {} with base path as {}" , fullPath,processName , basePath)
        this.getClass.getClassLoader.getResource(fullPath).getPath
      }
    }
    finalPath*/

    val fullPath = new StringBuilder().append(basePath).
      append("/").append(fileRelativePath).toString()
    println("fullpath=" + fullPath)
    val finalPath = this.getClass.getClassLoader.getResource(fullPath).getPath
    finalPath
  }
}