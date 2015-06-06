/*
	Copyright 2015, Google, Inc.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
/**
 * 
 */
package com.google.cloud.das.languagecheck;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * @author tpryan
 *
 */
public class Translate {
	private String key;
	private String target;
	private static final String baseurl = "https://www.googleapis.com/language/translate/v2";
	public static final String default_target = "en";
	
	public Translate(String key, String target) {
		this.key = key;
		this.target = target;
	}	
	
	public Translate(String key) {
		this(key, default_target);
	}	
	
	public Language detect(String str){
		String key = str;
		String code = "";
		
		// Using the synchronous cache
	    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    //syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    code = (String) syncCache.get(key); // read from cache
	    if (code == null) {
	    	System.out.println("Cache miss");
	    	code = detectFromAPI(key);
	    	syncCache.put(key, code);
	    } else{
	    	System.out.println("Cache hit");
	    }
			
	    String label = getLangaugeLabel(code);
	    Language lang = new Language(code, label);
	    
		return lang;
		
	}
	
	public String detectFromAPI(String str){
		String path = "/detect";
		String query = "";
		try {
			query = "?q=" + URLEncoder.encode(str,"UTF-8") + "&key=" + key;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String url = baseurl + path + query;
		String response = "";
		try {
			response = sendGet(url);
			//System.out.println(response);
        } catch (Exception e) {
            //do something clever with the exception
        	response = "{}";
            System.out.println(e.getMessage());
        }
		
		
		JsonParser jsonParser = new JsonParser();
		String result = jsonParser.parse(response)
						.getAsJsonObject()
						.getAsJsonObject("data")
						.getAsJsonArray("detections").get(0)
						.getAsJsonArray().get(0)
						.getAsJsonObject().get("language")
						.getAsString();
						
		
		return result;
	}
	
	public String getLangaugeLabel(String lang){
		String key = "lang:" +  this.target + ":" +  lang;
		String result = "";
		
		
		// Using the synchronous cache
	    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    //syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    result = (String) syncCache.get(key); // read from cache
	    if (result == null) {
	    	System.out.println("Cache miss");
	    	Hashtable<String, String> hash = getLanguagesFromAPI();
	    	result = hash.get(lang);
	    	// populate cache
	    	Enumeration<String> en = hash.keys();
	    	while(en.hasMoreElements()){
	    		String code = en.nextElement();
	    		String tempkey = "lang:" +  this.target + ":" +  code;
	    		syncCache.put(tempkey, hash.get(code));
	    	}
	    } else{
	    	System.out.println("Cache hit");
	    }
		
		return result;
		
	}

	public Hashtable<String, String> getLanguagesFromAPI(){
		String response;
		String path = "/languages";
		String query = "?target=" + target  + "&key=" + key;
		String url = baseurl + path + query;
		Hashtable<String,String> hash = new Hashtable<String, String>();
		
		try {
			response = sendGet(url);
			//System.out.println(response);
        } catch (Exception e) {
            //do something clever with the exception
        	response = "{}";
            System.out.println(e.getMessage());
        }	
		
		JsonParser jsonParser = new JsonParser();
		JsonArray arr = jsonParser.parse(response)
						.getAsJsonObject()
						.getAsJsonObject("data")
						.getAsJsonArray("languages");
		
		for (int i = 0; i < arr.size(); i++) {
			JsonElement el = arr.get(i).getAsJsonObject();
			hash.put(	el.getAsJsonObject().get("language").getAsString(), 
						el.getAsJsonObject().get("name").getAsString());
		}
		
		return hash;
	}
	
	private String sendGet(String url) throws Exception {
		 
 
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
 		try {
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
 
			//print result
			return response.toString();
 		} catch(Exception ex) {
			ex.printStackTrace();
		} 
		return "";
	}
	
}
