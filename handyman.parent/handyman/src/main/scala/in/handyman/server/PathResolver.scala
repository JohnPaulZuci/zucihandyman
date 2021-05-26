package in.handyman.server

object PathResolver {
  
  def resolvePath(processName:String, fileRelativePath:String, basePath:String):String={
    
    val fullPath = new StringBuilder().append(basePath).
    append("/").append(fileRelativePath).toString()
    println("fullpath="+fullPath)
    val finalPath = this.getClass.getClassLoader.getResource(fullPath).getPath
    finalPath
  }
}