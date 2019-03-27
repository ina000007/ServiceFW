package com.nishant.service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.jsonpath.JsonPath;


import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;


public class Utility {
	
	public static boolean IsBlank(String str) {
        if(str != null &&  !str.equalsIgnoreCase("null") && !str.isEmpty())
            return false;
        return true;
	}

	public static String replaceStr(String str, Map<String, String> replace) {
		String tmp;
		String ptrnStr = "\\$\\([a-zA-Z0-9]+\\)";
		List<String> allMatches = new ArrayList<String>();
		Matcher m = Pattern.compile(ptrnStr, Pattern.CASE_INSENSITIVE).matcher(str);
		while (m.find()) {
			tmp = m.group().replaceAll("[$ ()]", "");
			str = str.replaceAll("\\$\\(" + tmp + "\\)", replace.get(tmp));
			allMatches.add(tmp);
		}

		return str;
	}
	
	static public String replaceBodyVar(String body,Map<String, String> replace) {
		String temp="";
		for (Map.Entry<String, String> entry : replace.entrySet()) {
			temp = entry.getKey();
			body = body.replaceAll("\\$\\(" + temp + "\\)", entry.getValue());
		}
		return body;
	}

	public static String extractDataWithJsonPath(String jsonString, String proprty, String replaceVar,
			Map<String, String> map) {
		
		String jPath = map.get(proprty);
		String res ="";
		if(!replaceVar.equals(""))
		jPath = jPath.replaceAll("\\$\\([a-zA-Z0-9]+\\)",replaceVar);
		
		Object value = JsonPath.read(jsonString, jPath);

        if (value == null) {
            res = null;
        }

        if (Map.class.isAssignableFrom(value.getClass())) {
            res= JsonPath.parse(value).jsonString();
        } else {
            res =  String.valueOf(value);
        }
        
		return res;
	}

}
