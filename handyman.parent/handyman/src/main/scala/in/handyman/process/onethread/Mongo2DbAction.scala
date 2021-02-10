package in.handyman.process.onethread

import java.lang.reflect.Method
import java.sql.SQLException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.HashSet

import org.bson.Document
import org.json.JSONObject

import com.mongodb.BasicDBObject
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoDatabase
import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.config.Resource
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ResourceAccess

class Mongo2DbAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    val mongo2DbAsIs: in.handyman.dsl.Mongo2Db = action.asInstanceOf[in.handyman.dsl.Mongo2Db]
    val mongo2Db: in.handyman.dsl.Mongo2Db = CommandProxy.createProxy(mongo2DbAsIs, classOf[in.handyman.dsl.Mongo2Db], context)
    var rowsProcessed: Int = 1

    val source = mongo2Db.getSourceConnStr
    val destination = mongo2Db.getTo
    val sourceDb = mongo2Db.getSourceDb
    val name = mongo2Db.getName
    val ddlSql: String = mongo2Db.getValue.replaceAll("\"", "")
    val id = context.getValue("process-id")
    val table = mongo2Db.getTargetTable
    val slimit: String = mongo2Db.getLimit
    val limit: Int = slimit.toInt
    val filter: String = mongo2Db.getFilter
    val findAttr: String = mongo2Db.getFindAttr
    val manipFunc: String = mongo2Db.getApplyManipulation
    val onUpdateKey: String = mongo2Db.getOnUpdateKey
    
    val methodMap : HashMap[String, Method] = new HashMap[String, Method]()
    
    var javaBso : Any = null;
    if(manipFunc != null){
      val clazz = this.getClass.getClassLoader.loadClass(manipFunc)
      val decMethods : Array[Method] = clazz.getDeclaredMethods()      
      for (method <- decMethods) {
        methodMap.put(method.getName, method)
      }
      
      javaBso = clazz.newInstance()
    }
          
    val connResource: Resource = ConfigurationService.getResourceConfig(source)
    val srcConnStr = connResource.url
    
    val mongoClient : MongoClient = MongoClients.create(srcConnStr)
    val mongoDatabase : MongoDatabase = mongoClient.getDatabase(sourceDb)
    
    var sqlSelect : String = ddlSql.substring(ddlSql.toUpperCase().indexOf("SELECT") + 7, ddlSql.toUpperCase().indexOf("FROM"))
    var sqlFrom: String = ddlSql.substring(ddlSql.toUpperCase().indexOf("FROM") + 5, ddlSql.length())
    
    val coll : MongoCollection[Document]	=  mongoDatabase.getCollection(sqlFrom)
    
    val projectFields : BasicDBObject = new BasicDBObject()
    val selectFields : Array[String] = sqlSelect.split(",");
    var insertFields : String = "";
    val insFieldsMap : HashSet[String] = new HashSet[String]
    val insAndSelFieldsMap : HashMap[String, String] = new HashMap[String, String]
    for (col <- selectFields) {
      val colStr : String = col.trim().toString()
      var aliasCol : String = null;
      var dbCol : String = null;
      if(colStr.contains(" as ")){
        val colWithAlias : Array[String] = colStr.split(" as ")
        dbCol = colWithAlias(0).trim()
        aliasCol = colWithAlias(1).trim()
      }else{
        dbCol = colStr
        aliasCol = colStr
      }
      
      /*if(dbCol.contains(".key")){
        projectFields.append(dbCol.substring(0, dbCol.indexOf("key")-1), 1);        
      }else{*/
        projectFields.append(dbCol, 1)
      //}
      insFieldsMap.add(aliasCol.trim());
      insAndSelFieldsMap.put(aliasCol.trim(), dbCol.trim())
    }
    
    val inFieldArr = insFieldsMap.toArray()
    var updateStr : String = "";
    var insertStr : String = "";
    for (inField <- inFieldArr) {
      val dbCol  = insAndSelFieldsMap.get(inField)
      
      if(dbCol.contains("[")){
        insertFields = insertFields + inField + ","
        updateStr = updateStr + "target." + inField + " = source." + inField + ","
        insertStr = insertStr + "source." + inField + ","  
        
        val colStr : Array[String] = dbCol.substring(dbCol.indexOf("[")+1, dbCol.indexOf("]")).split(":")
        var colss: String = ""
        for (colS <- colStr) {
          colss = colS
          if(colS.contains(" alias ")){
            val colWithAlias : Array[String] = colS.split(" alias ")
            colss = colWithAlias(1).trim()
          }
          insertFields = insertFields + colss + ","
          updateStr = updateStr + "target." + colss + " = source." + colss + ","
          insertStr = insertStr + "source." + colss + ","  
        }
      }else{
        insertFields = insertFields + inField + ","
      
        if(!inField.equals("application_id")){
          updateStr = updateStr + "target." + inField + " = source." + inField + ","
        }
        insertStr = insertStr + "source." + inField + ","  
      }
    }
    
    insertFields = insertFields.substring(0, insertFields.length() - 1)
    updateStr = updateStr.substring(0, updateStr.length() - 1)
    insertStr = insertStr.substring(0, insertStr.length() - 1)
    
    var onUpdateStr:String = ""
    if(onUpdateKey != null && !onUpdateKey.isEmpty()){
      onUpdateStr = onUpdateStr + "target." + onUpdateKey + " = source." + onUpdateKey
    }
    
    var mongoCursor : MongoCursor[Document] = null
    if(filter != null && !filter.isEmpty()) {
      mongoCursor = applyFilter(filter, coll, projectFields)
    }else{
      mongoCursor = coll.find().projection(projectFields).iterator()
    }

    val mongo2DbDbConnto = ResourceAccess.rdbmsConn(destination)
    val mongo2DbStmtto = mongo2DbDbConnto.createStatement
    mongo2DbDbConnto.setAutoCommit(false)
    
    var query: String = ""
    try{
      if(mongoCursor != null){
        while (mongoCursor.hasNext()) {
          var doc : Document = mongoCursor.next();
          
         /* val docjsonStr : String = doc.toJson(JsonWriterSettings.builder().build());
          val docJsonObj: JSONObject = new JSONObject(docjsonStr);
          val updatedatStr:String = String.valueOf(docJsonObj.getJSONObject("updatedat").get("$date"))
          System.out.println(docjsonStr);
          System.out.println(doc.toJson());*/
          
          query = query + "("
          for (col <- inFieldArr) {
            val colStr : String = col.toString().trim()
            var dbCol = insAndSelFieldsMap.get(colStr);
            
            /*if(dbCol.contains(".key")){
              var query1 = query
              val dbCol1 = dbCol.substring(0, dbCol.indexOf("key")-1)
              var colVal : Object = getColumn(colStr, dbCol1, doc, javaBso, methodMap)    
              
              if(colVal != null){
                  val colValDoc = colVal.asInstanceOf[Document]
                  val keySet : java.util.Set[String]  = colValDoc.keySet()
                  val iterStr : java.util.Iterator[String] = keySet.iterator()
                  while(iterStr.hasNext()){
                    val key = iterStr.next()
                    
                    query1 = appendColumn(key, query1)
                    
                    val colStr : Array[String] = dbCol.substring(dbCol.indexOf("[")+1, dbCol.indexOf("]")).split(":")
                    var colss: String = ""
                    for (colS <- colStr) {
                      colss = colS
                      if(colS.contains(" alias ")){
                        val colWithAlias : Array[String] = colS.split(" alias ")
                        colss = colWithAlias(0).trim()
                      }
                      colVal = colValDoc.get(key).asInstanceOf[Document].get(colss)
                      query1 = appendColumn(colVal, query1)    
                    }
                    
                    query1 = query1.substring(0, query1.length() - 1) + "),"
                  }                
              }else{
                query1 = query1 + null + ","  
                
                val colStr : Array[String] = dbCol.substring(dbCol.indexOf("[")+1, dbCol.indexOf("]")).split(":")
                for (colS <- colStr) {
                  query1 = query1 + null + ","
                }         
                
                query1 = query1.substring(0, query1.length() - 1) + "),"
              }
              
              query = query1
            }else{*/
              var colVal : Object = getColumn(colStr, dbCol, doc, javaBso, methodMap)    
              
              query = appendColumn(colVal, query)
            //}
          }
          
          //if(!query.indexOf(query.length()-1).equals(")"))
            query = query.substring(0, query.length() - 1) + "),"
          
          if (rowsProcessed % 10 == 0) {
  
            query = query.replace("\"null\"", "null")
            logger.info("WriteCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, source, query)
  
            var insert: String = ""
            if(onUpdateKey != null && !onUpdateKey.isEmpty()){
              insert = "MERGE INTO " + table + " AS target USING (VALUES " + query.substring(0, query.length() - 1) + 
                                 ") AS source (" + insertFields + ") ON (" + onUpdateStr + ") WHEN MATCHED THEN UPDATE SET " +
                                 updateStr + " WHEN NOT MATCHED THEN INSERT (" + insertFields + ") VALUES (" + insertStr +");";              
            }else{
              insert = "insert into " + table + " (" + insertFields + ")" + " values " + query.substring(0, query.length() - 1) + ";"
            }
            logger.info("WriteCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, source, insert)
  
            mongo2DbStmtto.execute(insert)

            insert = ""
            query = ""
          }
          rowsProcessed = rowsProcessed + 1
          
          mongo2DbDbConnto.commit()
        }      
        
        if(!query.isEmpty()){
          query = query.replace("\"null\"", "null")
          var insert: String = ""
          if(onUpdateKey != null && !onUpdateKey.isEmpty()){
            insert = "MERGE INTO " + table + " AS target USING (VALUES " + query.substring(0, query.length() - 1) + 
                               ") AS source (" + insertFields + ") ON (" + onUpdateStr + ") WHEN MATCHED THEN UPDATE SET " +
                               updateStr + " WHEN NOT MATCHED THEN INSERT (" + insertFields + ") VALUES (" + insertStr +");";              
          }else{
            insert = "insert into " + table + " (" + insertFields + ")" + " values " + query.substring(0, query.length() - 1) + ";"
          }
          logger.info("WriteCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, source, insert)

          mongo2DbStmtto.execute(insert)
          mongo2DbDbConnto.commit()          
        }
        
      }      
    }catch {
      case ex: SQLException => {
        ex.printStackTrace()
      }
    } finally {
      if(mongoCursor != null)
        mongoCursor.close();
      
        mongoClient.close();
        mongo2DbStmtto.close()
        mongo2DbDbConnto.close()
        
        detailMap.put("name", name)
        detailMap.put("source", source)
        detailMap.put("destination", destination)
        detailMap.put("ddlSql", ddlSql)
        detailMap.put("filter", filter)
        detailMap.put("srcDb", sourceDb);
        detailMap.put("srcConnStr", srcConnStr);
        detailMap.put("targetTable", table);
        
        if(rowsProcessed > 0){
          rowsProcessed = rowsProcessed - 1;          
        }
        detailMap.put("rows-processed", String.valueOf(rowsProcessed));
  
        context.addValue("rows-processed", String.valueOf(rowsProcessed));
    }

    context
  }
  
   def appendColumn(colVal: Object, query : String) : String = {
     var queryAppend = query
     
      if(colVal != null){
         var colValStr = colVal.toString()
         
          if((colVal.isInstanceOf[Integer] || colVal.isInstanceOf[Double])){
             if(!colValStr.equals(""))
               queryAppend = queryAppend + colValStr + "," 
             else 
               queryAppend = queryAppend + null + ","    
          }else if(colVal.isInstanceOf[Date]){
            colValStr = formatDate(colVal);
            if(colValStr != null && !colValStr.isEmpty()){
              queryAppend = queryAppend + colValStr + ","               
            }else{
              queryAppend = queryAppend + "''" + ","
            }
          }
          else{
            if(colValStr.contains("'"))
              colValStr = colValStr.replaceAll("'", "''")
        
            queryAppend = queryAppend + "\'" + colValStr + "\'" + ","         
          }
       }
      else
          queryAppend = queryAppend + null + ","  
          
      return queryAppend
	}
  
  def formatDate(date: Object) : String = {
		  val df: DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ZZZ");
      if(date != null){
        val dfStr: String = df.format(date);
        var str: String = "CAST('" + dfStr.substring(0, dfStr.length()-2) + ":" + dfStr.substring(dfStr.length()-2, dfStr.length()) + "' AS DateTime2)"
        
        return str        
      }
	    
	    return null;
	}
  
  def getColNestedDocIfExists(docObj: Document, dbCol: String): Object = { 
    if(docObj.isInstanceOf[Document] && dbCol.contains(".")){
      val dbColForm = dbCol.substring(0, dbCol.indexOf("."))
      val dbColFunc = dbCol.substring(dbCol.indexOf(".")+1, dbCol.length())
      val obje: Object = docObj.get(dbColForm)
      if(obje.isInstanceOf[java.util.ArrayList[_]]){
        return obje
      }else{
        val doc1 : Document = docObj.get(dbColForm, classOf[Document])
      
        getColNestedDocIfExists(doc1, dbColFunc)        
      }
    }
    else 
      return docObj
  } 
  
  def getColumn(colStr: String, dbCol: String, doc: org.bson.Document, javaBso : Any, 
      methodMap : HashMap[String, Method]) : Object = {
    var manipFunc: Method = methodMap.get(colStr);
    if(manipFunc != null){ // apply manipulation
      val retObj : Object = manipFunc.invoke(javaBso, doc, dbCol, colStr)
      if(retObj != null){
        //logger.info("Column:" + aliasCol + ", Before Manipulation:" + colVal + ", After Manipulation:" + retObj.toString())
        return retObj                
      }
    }else{
      val docNew = getColNestedDocIfExists(doc, dbCol).asInstanceOf[Document]
      if(docNew != null){
        if(dbCol.contains(".")){
          val colValObj = docNew.get(dbCol.substring(dbCol.lastIndexOf(".")+1 , dbCol.length()));     
          if(colValObj != null)
            return colValObj
          }else{
            val obj : Object = docNew.get(dbCol);
            if(obj != null)
              return obj      
          }                
      }
    }
    
    return null
  }

  // filter format {'column':'updatedat', 'type':'date', 'format':'yyyy-MM-dd HH:mm:ss.SSS ZZZ', 'operator':'$gt', 'value':'2020-06-25 09:28:04.041 UTC'}
  def applyFilter(filter: String, coll: com.mongodb.client.MongoCollection[org.bson.Document], 
      projectFields : BasicDBObject) : com.mongodb.client.MongoCursor[org.bson.Document] = {
    val obj: JSONObject = new JSONObject(filter);
    val col:String = String.valueOf(obj.get("column"))
    val operator:String = String.valueOf(obj.get("operator"))
    var colVal:String = String.valueOf(obj.get("value"))
    val colType:String = String.valueOf(obj.get("type"))
    val colFormat:String = String.valueOf(obj.get("format"))
    
    var colFormatted: Date = null;
    var filDbObj: BasicDBObject = null;
    if(colType.equals("date") && !colFormat.isEmpty()){
      colFormatted = new SimpleDateFormat(colFormat).parse(colVal);
      filDbObj = new BasicDBObject(col, new BasicDBObject(operator, colFormatted));
    }else{
      filDbObj = new BasicDBObject(col, new BasicDBObject(operator, colVal));
    }
    
    val findIterate : FindIterable[Document] = coll.find(filDbObj).projection(projectFields)
    findIterate.iterator()
  }
  
  // find attribute format {'column':'updatedat', 'type':'date', 'format':'yyyy-MM-dd HH:mm:ss.SSS ZZZ', 'function':'max', 'context_column':'max_updatedat'}
  def findAttribute(findAttr: String, context: in.handyman.command.Context, iterCol: String, iterColVal: String) = {
    val obj: JSONObject = new JSONObject(findAttr);
    val col:String = String.valueOf(obj.get("column"))
    val function:String = String.valueOf(obj.get("function"))
    val colType:String = String.valueOf(obj.get("type"))
    val colFormat:String = String.valueOf(obj.get("format"))
    val contextCol:String = String.valueOf(obj.get("context_column"))
    
    if(col.equalsIgnoreCase(iterCol)){
      function match {
        case "max" =>  max(context, contextCol, colType, colFormat, iterColVal)      
      }      
    }
  }

  def max(context: in.handyman.command.Context, contextCol: String, colType: String, colFormat: String, iterColVal: String) = {
    val contextColVal = context.getValue(contextCol);
    
    if(contextColVal != null && !contextColVal.isEmpty()){
      var contextColFormatted: Date = null;
      var iterColValFormatted: Date = null;
      if(colType.equals("date") && !colFormat.isEmpty()){
        contextColFormatted = new SimpleDateFormat(colFormat).parse(contextColVal);
        iterColValFormatted = new SimpleDateFormat(colFormat).parse(iterColVal);
        
        if(iterColValFormatted.after(contextColFormatted))
          context.addValue(contextCol, iterColVal);
      }
    }else{
      context.addValue(contextCol, iterColVal);
    }
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean = {
    val mongo2DbAsIs: in.handyman.dsl.Mongo2Db = action.asInstanceOf[in.handyman.dsl.Mongo2Db]
    val mongo2Db: in.handyman.dsl.Mongo2Db = CommandProxy.createProxy(mongo2DbAsIs, classOf[in.handyman.dsl.Mongo2Db], context)

    val expression = mongo2Db.getCondition
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