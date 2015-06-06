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
package com.google.cloud.das.languagecheck;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class LanguageCheckServlet extends HttpServlet {
	
	private String key ="";
	
	
	public LanguageCheckServlet(){
		this.key = getKey();
	}
	
	
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		String target = req.getParameter("target");
		String q = req.getParameter("q");
		
		if (target == null || target.length() == 0){
			target = Translate.default_target;
		}
		
		if (q == null || q.length() == 0){
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Must pass in a value for 'q'.");
			return;
		}
		
		
		Translate t = new Translate(this.key, target);
		Language lang = t.detect(q);
		Gson gson = new Gson();
		String json = gson.toJson(lang);
		resp.setCharacterEncoding("UTF8");
		resp.setContentType("application/json");
		resp.getWriter().println(json);
	}
	
	private String getKey(){
		Properties prop = new Properties();
		InputStream input = null;
	 
		try {
	 
			input = this.getClass().getClassLoader().getResourceAsStream("config.properties");
	 
			// load a properties file
			prop.load(input);
	 
			// get the property value and print it out
			return prop.getProperty("api_key");
	 
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}
}


