package com.rj.json;

import java.io.*;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JSONSchemaGen {
	private JSONParser parser;
	private DataField root;
	private StringBuffer schema;
	
	public JSONSchemaGen(){
		this.parser = new JSONParser();
		this.root = new DataField("root", DataType.OBJECT);
		this.schema=new StringBuffer();
	}
	
	public void printType(DataField field){
		DataType type = field.getType();
		switch(type){
		case LONG:
			long min = (long)field.getMin();
			long max = (long)field.getMax();
			if(min > Integer.MIN_VALUE && max < Integer.MAX_VALUE)
				schema.append("\"type\" : \"int\"");
			else if(min > Short.MIN_VALUE && max < Short.MAX_VALUE)
				schema.append("\"type\" : \"short\"");
			else
				schema.append("\"type\" : \"long\"");
			break;
		case DECIMAL:
			double dmin = field.getMin();
			double dmax = field.getMax();
			if(dmin > Float.MIN_VALUE && dmax < Float.MAX_VALUE)
				schema.append("\"type\" : \"float\"");
			else
				schema.append("\"type\" : \"decimal\"");
			break;
		default:
			schema.append("\"type\" : \"" + type.toString().toLowerCase() + "\"");
		}
	}
	
	public void printJSON(DataField field){
		DataType type = field.getType();
		schema.append("\""+ field.getName() +"\" : {");
		printType(field);
		schema.append(",\"count\" : " + field.getCount());
		int i;
		List<DataField> children = field.getChildren();
		if(type == DataType.ARRAY){
			schema.append(",");
			schema.append("\"canbenull\" : " + field.canBeNull() + ",");
			schema.append("\"minimum\" : " + (long)field.getMin() + ",");
			schema.append("\"maximum\" : " + (long)field.getMax() + ",");
			schema.append("\"average\" : " + field.getAvg() + ",");
			schema.append("\"items\" : {");
			if(children.size() > 0){
				for(i=0; i < children.size()-1; i++){
					printJSON(children.get(i));
					schema.append(",");
				}
				printJSON(children.get(i));
			}
			
			schema.append("}");
		}else if(type == DataType.OBJECT){
			schema.append(",");
			schema.append("\"canbenull\" : " + field.canBeNull() + ",");
			schema.append("\"properties\" : {");
			for(i=0; i < children.size()-1; i++){
				printJSON(children.get(i));
				schema.append(",");
			}
			printJSON(children.get(i));
			schema.append("}");
		}else if(type == DataType.BOOLEAN){
			
		}else if(type == DataType.NULL){
			
		}else if(type != DataType.DECIMAL){
			schema.append(",");
			schema.append("\"minimum\" : " + (long)field.getMin() + ",");
			schema.append("\"maximum\" : " + (long)field.getMax());
			if(type == DataType.STRING){
				schema.append(",\"average\" : " + (long)field.getAvg());
			}
			Object[] samples = field.getSamples().toArray();
			if(samples.length > 0){
				schema.append(",\"samples\" : \"");
				int j;
				for(j = 0; j< samples.length-1; j++){
					schema.append((String)samples[j] + ",");
				}
				schema.append((String)samples[j]);
				schema.append("\"");
			}
		}else if(type == DataType.DECIMAL){
			schema.append(",");
			schema.append("\"minimum\" : " + field.getMin() + ",");
			schema.append("\"maximum\" : " + field.getMax());
		}
		schema.append("}");
	}
	
	public void printJSON(){
		List<DataField> fields = root.getChildren();
		int i;
		schema.setLength(0);
		schema.append("{");
		schema.append("\"type\" : \"" + root.getType().toString().toLowerCase() + "\",");
		schema.append("\"count\" : " + root.getCount() + ",");
		schema.append("\"properties\" : {");
		for(i=0; i < fields.size()-1; i++){
			printJSON(fields.get(i));
			schema.append(",");
		}
		printJSON(fields.get(i));
		schema.append("}}");
		
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(schema.toString());
			String prettyJsonString = gson.toJson(je);
			System.out.println(prettyJsonString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DataType getFieldType(Object o){
		if(o == null)
			return DataType.NULL;
		else if(o instanceof JSONObject)
			return DataType.OBJECT;
		else if(o instanceof JSONArray)
			return DataType.ARRAY;
		else if(o instanceof String)
			return DataType.STRING;
		else if(o instanceof Integer)
			return DataType.INT;
		else if(o instanceof Float || o instanceof Double)
			return DataType.DECIMAL;
		else if(o instanceof Long)
			return DataType.LONG;
		else if(o instanceof Short)
			return DataType.SHORT;
		else if(o instanceof Boolean)
			return DataType.BOOLEAN;
		
		return DataType.UNKNOWN;
	}
	
	public void setMinMax(DataField field, Object obj){
		field.incrementCount();
		double min = field.getMin();
		double max = field.getMax();
		double val = Double.NEGATIVE_INFINITY;
		String sample = null;
		
		switch(field.getType())
		{
		case INT:
		case NUMBER:
		case LONG:
		case SHORT:
			val = ((Long)obj).longValue();
			sample = String.valueOf((long)val);
			break;
		case DECIMAL:
			val = ((Double)obj).doubleValue();
			sample = String.valueOf(val);
			break;
		case STRING:
			val = ((String)obj).length();
			String pattern= "^[a-zA-Z0-9$&+:;=\\-.%^()_~`?@#|\\s]*$";
		    if(val < 100 && ((String)obj).matches(pattern)){
		      sample = (String)obj;
		    }
		    field.setAvg((int)val);
			break;
		case ARRAY:
			val = ((JSONArray)obj).size();
			field.setAvg((int)val);
			break;
		default:
			break;
		}
		if(val != Double.NEGATIVE_INFINITY){
			if(val > max) 
				field.setMax(val);
			if(val < min)
				field.setMin(val);
		}
		if(sample != null){
			field.addSample(sample);
		}
		
	}
	
	public void parseJSON(Object json, String nodekey, DataField field){
		boolean hasFields = false;
		if(json instanceof JSONObject){
			for(Iterator<?> iter = ((JSONObject)json).keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				DataType type = getFieldType(((JSONObject)json).get(key));
				
				DataField child = field.getChild(key);
				if(child == null){
					child = new DataField(key, type);
					field.addChild(child);
				}
				parseJSON(((JSONObject)json).get(key), key, child);
				hasFields = true;
			}
			field.incrementCount();
		}else if(json instanceof JSONArray){
			for(int i=0; i<((JSONArray)json).size();i++) {
				Object j = ((JSONArray)json).get(i);
				DataType type = getFieldType(j);
				String fldkey = nodekey+"_"+type.toString();
				DataField child = field.getChild(fldkey);
				if(child == null){
					child = new DataField(fldkey, type);
					field.addChild(child);
				}
				parseJSON(j, fldkey, child);
				hasFields = true;
			}
			setMinMax(field, json);
		}else if(json == null){
		  hasFields = false;
		}else{
			// Set min/max if NUMERIC datatypes
			setMinMax(field, json);
			hasFields = true;
		}
		if(!hasFields){
			field.setCanBeNull();
		}
	}
	
	public void parseJSON(JSONObject json){
		for(Iterator<?> iter = json.keySet().iterator(); iter.hasNext();) {
			
			String key = (String) iter.next();
			DataType type = getFieldType(json.get(key));
		    
			DataField child = root.getChild(key);
			
			if(child == null){
				child = new DataField(key, type);
				root.addChild(child);
			}
			parseJSON(json.get(key), key, child);
		}
		root.incrementCount();
	}
	
	public void parseFile(String filepath){
		if(isValidJSON(filepath)){
			try{
				JSONObject json = (JSONObject)parser.parse(new FileReader(filepath));
	    		parseJSON(json);
			}catch(Exception e){
	    		System.out.println("Exception caught while parsing json file " + filepath);
	    	}
		}else{
			try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
				String line;
				int i = 1;
			    while ((line = br.readLine()) != null) {
			    	try{
			    		JSONObject json = (JSONObject)parser.parse(line);
			    		parseJSON(json);
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
		jsg.printJSON();
	}
}
