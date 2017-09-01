package com.zylon.swagger2json;

import java.io.*;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class JSONSchemaGen {
	private JSONParser parser;
	private JSONObject JSONObject;
	private HashMap<String,Object> map = new HashMap<String,Object>();
	public JSONSchemaGen(){
		this.parser = new JSONParser();
	}
	
	
	
	public void printJSON(String objectName){
		parseJSON(objectName, JSONObject);
		parseRef(map.get(objectName));
		HashSet<String> set = new HashSet<String>();
		set.add("description");set.add("title");set.add("$ref");
		deletedAttrs(map.get(objectName),set);
		((JSONObject)map.get(objectName)).put("$schema","http://json-schema.org/draft-04/schema#");
		System.out.println(map.get(objectName));
	}
	
	private void deletedAttrs(Object json, HashSet<String> set) {
		if(json instanceof JSONObject){
			for (String key : set) {
				((JSONObject)json).remove(key);
			}
			for(Iterator<?> iter = ((JSONObject)json).keySet().iterator(); iter.hasNext();) {String key = (String) iter.next();
				deletedAttrs(((JSONObject)json).get(key),set);
			}
		}
			
	}







	public void parseRef(Object json){
		String objectName = null;
		if(json instanceof JSONObject){
			for(Iterator<?> iter = ((JSONObject)json).keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				if(key.equalsIgnoreCase("$ref")){
					objectName = 	(((JSONObject)json).get(key)).toString().substring((((JSONObject)json).get(key)).toString().lastIndexOf("/")+1);
				System.out.println(objectName);
					parseJSON(objectName, JSONObject);
				}else{
					parseRef(((JSONObject)json).get(key));
				}
			}
			if(objectName!=null){
				try {
					parseRef(map.get(objectName));
					((JSONObject)json).put("properties", ((JSONObject)map.get(objectName)).get("properties"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			}
		}
	}


	public void parseJSON(String nodeName,Object json){
		if(json instanceof JSONObject){
			for(Iterator<?> iter = ((JSONObject)json).keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
//				  System.out.println(key);
				if(key.equalsIgnoreCase(nodeName)){
						map.put(key, ((JSONObject)json).get(key));

				}
					parseJSON(nodeName,((JSONObject)json).get(key));
			}
		}
	}
	
	
	public JSONObject parseFile(String filepath){
		if(isValidJSON(filepath)){
			try{
				JSONObject = (JSONObject)parser.parse(new FileReader(filepath));
			}catch(Exception e){
	    		System.out.println("Exception caught while parsing json file " + filepath);
	    	}
		}else{
			try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
				String line;
				int i = 1;
			    while ((line = br.readLine()) != null) {
			    	try{
			    		JSONObject = (JSONObject)parser.parse(line);
			    					    	}catch(Exception e){
			    		System.out.println("Exception caught while parsing json @ line " + i + " in file " + filepath);
			    	}finally{
			    		i++;
			    	}
			    }
			    br.close();
			} catch (IOException e) {
				System.out.println("Exception caught while reading file " + filepath);
				e.printStackTrace();
			}
		}
		return JSONObject;
	}
	
	public boolean isValidJSON(String filePath){
		File file = new File(filePath);
		long size = (file.length() / 1024) / 1024;
		if(size > 100)
			return false;
		try {
		    parser.parse(new FileReader(file));
		} catch (ParseException e) {
		    return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	public static void usage(){
		
	}
	
	public static void main(String[] args) {
		if(args.length == 0){
			
			usage();
			return;
		}
		
		File file = new File(args[0]);
		if(!file.exists() ||  file.isDirectory()) { 
		    usage();
		    return;
		}
		
		JSONSchemaGen jsg = new JSONSchemaGen();
		jsg.parseFile(args[0]);
		jsg.printJSON("GetCaseModel");
	}
}
