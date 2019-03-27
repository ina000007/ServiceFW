package com.nishant.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.nishant.service.util.Utility;

public class Service {
	Properties prop = new Properties();
	InputStream input = null;
	public String env;
	public String preApiFilePath;
	public String postApiFilePath;
	public String apiYmlFilePath = "";

	static public String database = "";
	static public String dbIp = "";
	static public String dbPort = "";
	static public String dbName = "";
	static public String dbUserName = "";
	static public String dbPassword = "";
	public String sqlYmlFilePath = "";

	public Service(String servicePropertyFilePath) {
		try {

			input = new FileInputStream(servicePropertyFilePath);
			prop.load(input);

//			env is common config for Api and Service
			env = readProperty("env");
			apiYmlFilePath = readProperty("apiFilePath");
			apiYmlFilePath = apiYmlFilePath.replaceAll("\\$\\(env\\)", env);
			database = readProperty("database");
			dbIp = readProperty(env + "_dbIp");
			dbPort = readProperty(env + "_dbPort");
			dbName = readProperty(env + "_dbName");
			dbUserName = readProperty(env + "_dbUserName");
			dbPassword = readProperty(env + "_dbPassword");
			sqlYmlFilePath = readProperty("sqlQueryPath");

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
	}

	public String readProperty(String property) {
		String val = "";
		val = prop.getProperty(property);
		if (Utility.IsBlank(val))
			;
//			log

		return val;
	}

}
