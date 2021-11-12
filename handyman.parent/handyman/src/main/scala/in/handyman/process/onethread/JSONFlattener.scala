package in.handyman.process.onethread

import java.util.ArrayList
import java.util.Date
import java.util.LinkedHashMap
import java.util.Map

import org.json.JSONArray
import org.json.JSONObject

class JSONFlattener {

    /**
     * Parse the JSON String
     *
     * @param json
     * @return
     * @throws Exception
     */
    def parseJson(json : String) : ArrayList[Map[String, Object]] = {
        var jsonObject : JSONObject = new JSONObject(json);
        return parse(jsonObject);
    }

    /**
     * Parse a JSON Object
     *
     * @param jsonObject
     * @return
     */
    def parse(jsonObject : JSONObject) : ArrayList[Map[String, Object]] = {
        var flatJson : Map[String, Object] = new LinkedHashMap[String, Object]();
        var jsonArrays : Map[String, JSONArray] = new LinkedHashMap[String, JSONArray]();
        flatten(jsonObject, flatJson, "", jsonArrays);

        var jsonList : ArrayList[Map[String, Object]] = new ArrayList[Map[String, Object]]();
  		  jsonList.add(flatJson);        		
  		  jsonArrays.keySet().forEach(jsonArrKey =>
        	jsonList = explodeJsonArray(jsonArrKey, jsonArrays.get(jsonArrKey), jsonList))
    
        return jsonList;
    }

    /**
     * Flatten the given JSON Object
     *
     * This method will convert the JSON object to a Map of
     * String keys and values
     *
     * @param obj
     * @param flatJson
     * @param prefix
     */
    def flatten(obj : JSONObject, flatJson : Map[String, Object], prefix : String, jsonArrays : Map[String, JSONArray]) {
        var iterator : java.util.Iterator[String] = obj.keys();
        var _prefix : String = "";
        if(prefix != "")
          _prefix = prefix + "_"

        while (iterator.hasNext()) {
            var key : String = iterator.next().toString();

            if (obj.get(key).isInstanceOf[JSONObject]) {
                var jsonObject : JSONObject = obj.get(key).asInstanceOf[JSONObject];
                flatten(jsonObject, flatJson, _prefix + key, jsonArrays);
            } else if (obj.get(key).isInstanceOf[JSONArray]) {
                var jsonArray : JSONArray = obj.get(key).asInstanceOf[JSONArray];

                if (jsonArray.length() > 0) 
                  flatten(jsonArray, flatJson, _prefix + key, jsonArrays);
            } else {
                var value : Object = obj.get(key);

                if (value != null) {
                    flatJson.put(_prefix + key.replaceAll(" ", "_")+"|"+getColumnDataType(value), value);
                }
            }
        }
    }
    
    def explodeJsonArray(akey : String, arr : JSONArray, flatJson : ArrayList[Map[String, Object]]) : ArrayList[Map[String, Object]] = {
    	var jsonList : ArrayList[Map[String, Object]] = new ArrayList[Map[String, Object]]();
    	
    	var length : Int = arr.length();

        for( i <- 0 until length) {
            var value : Object = arr.get(i);

            if (value != null) {
            	flatJson.forEach(fJson => {
                	var flatJsonNew : Map[String, Object] = new LinkedHashMap[String, Object]();
                	flatJsonNew.putAll(fJson);
                	flatJsonNew.put(akey+"|"+getColumnDataType(value), value);
                	jsonList.add(flatJsonNew);   
            	});
            }
        }
        
        return jsonList;
    }
    
    def getColumnDataType(colValue : Object) : String = {
      if(colValue.isInstanceOf[Integer] | colValue.isInstanceOf[Long])
        return "Integer"
      else if(colValue.isInstanceOf[Float])
        return "Float"
      else if(colValue.isInstanceOf[Double])
        return "Double"
      else if(colValue.isInstanceOf[Date])
        return "Date"
      else if(colValue.isInstanceOf[String])
        return "Text"
      else if(colValue.isInstanceOf[Boolean])
        return "Boolean"
        
      return "Text"
    }

    /**
     * Flatten the given JSON Array
     *
     * @param obj
     * @param flatJson
     * @param prefix
     */
    def flatten(obj : JSONArray, flatJson : Map[String, Object], prefix : String, jsonArrays : Map[String, JSONArray]) {
        val length : Integer = obj.length();
        
        for( i <- 0 until length) {
            if (obj.get(i).isInstanceOf[JSONArray]) {
                var jsonArray : JSONArray = obj.get(i).asInstanceOf[JSONArray];

                if (jsonArray.length() >  0)
                  flatten(jsonArray, flatJson, prefix, jsonArrays);
            } else if (obj.get(i).isInstanceOf[JSONObject]) {
                var jsonObject : JSONObject = obj.get(i).asInstanceOf[JSONObject];
                flatten(jsonObject, flatJson, prefix, jsonArrays);
            } else {
            	if(i == 0) 
            		jsonArrays.put(prefix, obj);
            }
        }
    }
}