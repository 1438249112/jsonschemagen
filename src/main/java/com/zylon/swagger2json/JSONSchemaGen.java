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
	private HashMap<String, Object> map = new HashMap<String, Object>();

	public JSONSchemaGen() {
		this.parser = new JSONParser();
	}

	public void printJSON(String objectName) {
		// 1. find an object in the Swagger .
		parseJSON(objectName, JSONObject);
		// 2. add properties for the child object that have a attribute named
		// $ref .
		parseRef(map.get(objectName), JSONObject);

		// 3. delete all useless attributes .
		Object schemaJson = map.get(objectName);
		map = null;
		JSONObject = null;
		HashSet<String> set = new HashSet<String>();
		// descript all the leaf attributes .
		descAttrs(schemaJson);
		set.add("description");
		set.add("title");
		set.add("$ref");
	
		deletedAttrs(schemaJson, set);
		// 4. add schema version attribute .
		((JSONObject) schemaJson).put("$schema", "http://json-schema.org/draft-04/schema#");
		System.out.println(schemaJson);
	}

	private void descAttrs(Object json) {
		if (json instanceof JSONObject) {
			JSONObject jsonObj = (JSONObject) json;
			parseProperties(jsonObj.get("properties"), null);
		}
	}

	private void parseProperties(Object json, String attrPath) {

		if (json instanceof JSONObject) {
			JSONObject jsonObj = (JSONObject) json;
			for (Iterator<?> iter = ((JSONObject) jsonObj).keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				String path = "";
				if (attrPath == null) {
					path = key;
				} else {
					path = attrPath + "/" + key;
				}

				parseField(((JSONObject) jsonObj).get(key), key, path);

			}
		}

	}

	private void parseArrayProperties(Object json, String attrPath) {

		if (json instanceof JSONObject) {
			JSONObject jsonObj = (JSONObject) json;
			for (Iterator<?> iter = ((JSONObject) jsonObj).keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				String path = "";
				if (attrPath == null) {
					path = key;
				} else {
					path = attrPath + "[]/" + key;
				}
				parseField(((JSONObject) jsonObj).get(key), key, path);

			}
		}

	}

	private void parseField(Object json, String fieldName, String attrPath) {

		if (json instanceof JSONObject) {
			JSONObject jsonObj = (JSONObject) json;
			if (!"object".equals(jsonObj.get("type")) && !"array".equals(jsonObj.get("type"))) {
				if(jsonObj.get("enum")!=null){
					System.out.println(parseFieldName(fieldName) + "	" + attrPath + "	enum" + "	"
							+jsonObj.get("enum")+";"+jsonObj.get("description"));
				}else{
					System.out.println(parseFieldName(fieldName) + "	" + attrPath + "	" + jsonObj.get("type") + "	"
							+ jsonObj.get("description"));
				}
			
			}

			if (jsonObj.get("properties") != null) {
				parseProperties(jsonObj.get("properties"), fieldName);
			}
			if (((JSONObject) jsonObj.get("items")) != null
					&& ((JSONObject) jsonObj.get("items")).get("properties") != null) {
				parseArrayProperties(((JSONObject) jsonObj.get("items")).get("properties"), fieldName);
			}

		}

	}

	private String parseFieldName(String fieldName) {
		String result = "";
		if (fieldName != null) {
			ArrayList<String> words = splitByUpperCase(fieldName);
		
			for (String word : words) {
				result+=word+" ";
			}
		}
		return result;
	}

	private ArrayList<String> splitByUpperCase(String str) {
		ArrayList<String> rs = new ArrayList<String>();
		int index = 0;
		int len = str.length();
		for (int i = 1; i < len; i++) {
			if (Character.isUpperCase(str.charAt(i))) {
				rs.add(str.substring(index, i));
				index = i;
			}
		}
		rs.add(str.substring(index, len));
		return rs;

	}

	private void deletedAttrs(Object json, HashSet<String> set) {
		if (json instanceof JSONObject) {
			for (String key : set) {
				((JSONObject) json).remove(key);
			}
			for (Iterator<?> iter = ((JSONObject) json).keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				deletedAttrs(((JSONObject) json).get(key), set);
			}
		}

	}

	public void parseRef(Object json, org.json.simple.JSONObject jSONObject) {
		String objectName = null;
		if (json instanceof JSONObject) {
			for (Iterator<?> iter = ((JSONObject) json).keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				if (key.equalsIgnoreCase("$ref")) {
					objectName = (((JSONObject) json).get(key)).toString()
							.substring((((JSONObject) json).get(key)).toString().lastIndexOf("/") + 1);
					// System.out.println(objectName);
					parseJSON(objectName, JSONObject);
				} else {
					parseRef(((JSONObject) json).get(key), jSONObject);
				}
			}
			if (objectName != null) {
				try {
					parseRef(map.get(objectName), jSONObject);
					((JSONObject) json).put("properties", ((JSONObject) map.get(objectName)).get("properties"));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

	public void parseJSON(String nodeName, Object json) {
		if (json instanceof JSONObject) {
			for (Iterator<?> iter = ((JSONObject) json).keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				// System.out.println(key);
				if (key.equalsIgnoreCase(nodeName)) {
					map.put(key, ((JSONObject) json).get(key));

				}
				parseJSON(nodeName, ((JSONObject) json).get(key));
			}
		}
	}

	public JSONObject parseFile(String filepath) {
		if (isValidJSON(filepath)) {
			try {
				JSONObject = (JSONObject) parser.parse(new FileReader(filepath));
			} catch (Exception e) {
				System.out.println("Exception caught while parsing json file " + filepath);
			}
		} else {
			try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
				String line;
				int i = 1;
				while ((line = br.readLine()) != null) {
					try {
						JSONObject = (JSONObject) parser.parse(line);
					} catch (Exception e) {
						System.out.println("Exception caught while parsing json @ line " + i + " in file " + filepath);
					} finally {
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

	public boolean isValidJSON(String filePath) {
		File file = new File(filePath);
		long size = (file.length() / 1024) / 1024;
		if (size > 100)
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

	public static void usage() {

	}

	public static void main(String[] args) {
		if (args.length < 2) {

			usage();
			return;
		}

		File file = new File(args[0]);
		if (!file.exists() || file.isDirectory()) {
			usage();
			return;
		}

		JSONSchemaGen jsg = new JSONSchemaGen();
		// the name of swagger json file
		jsg.parseFile(args[0]);
		// the object name that need to be print .
		jsg.printJSON(args[1]);
	}
}
