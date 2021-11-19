package in.handyman.server


import in.handyman.command.TryContext
import java.net.InetAddress




object ProcessExecutor {
  
  @throws(classOf[Exception])
  def execute(mode:String, runtimeContext:RuntimeContext):ProcessResponse = {
    
    if(mode.equals("in.handyman.process.onethread"))
    {
      val tryContext = new TryContext(runtimeContext.configMap, runtimeContext.instanceName)
      val machine = InetAddress.getLocalHost.getHostAddress      
      val instanceId = in.handyman.audit.AuditService.insertInstanceAudit(runtimeContext.instanceName, mode, machine, runtimeContext.path)
      
      if(tryContext.getValue("root-ref").isEmpty()) tryContext.addValue("root-ref", instanceId.toString())
      if(tryContext.getValue("root-process-name").isEmpty()) tryContext.addValue("root-process-name", runtimeContext.instanceName)
      
      tryContext.addValue("process-id", instanceId.toString)
      tryContext.addValue("process-name", runtimeContext.instanceName)
      val runtime = new in.handyman.process.onethread.UniThreadProcessRuntime(runtimeContext.instanceName, instanceId)
      val processResponse = runtime.execute(runtimeContext.process, tryContext)
      processResponse
    }
    else
    {
      throw new Exception("Not yet implemented")
    }
  }
}