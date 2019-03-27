package com.nishant.service.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.nishant.service.Service;
import com.nishant.service.reader.YmlReader;
import com.nishant.service.util.Utility;

import io.restassured.response.Response;

public class DatabaseService extends Service {

	public Map<String, Object> sqlYmlFile;
	private static DatabaseService dbService;;
	private static Connection con;
	Map<String, Object> sqlQuerySpec;

	private DatabaseService(String configPropertyFilePath) throws IOException, ClassNotFoundException, SQLException {
		super(configPropertyFilePath);
		YmlReader ymlReader = new YmlReader();
		this.sqlYmlFile = ymlReader.readYml(sqlYmlFilePath);
		con = getConnection();
	}

	public static DatabaseService getInstance(String configPropertyFilePath)
			throws IOException, ClassNotFoundException, SQLException {

		if (dbService == null || con == null) {
			dbService = new DatabaseService(configPropertyFilePath);
		}
		return dbService;
	}

	private static Connection getConnection() throws ClassNotFoundException, SQLException, IOException {
		Connection con = null;
		if (database.equalsIgnoreCase("mysql")) {
			String dbLink = "jdbc:mysql://" + dbIp + ":" + dbPort + "/" + dbName;
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println(dbLink);
			con = DriverManager.getConnection(dbLink, dbUserName, dbPassword);
		} else if (database.equalsIgnoreCase("oracle")) {

			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection("jdbc:oracle:thin:@" + dbIp + ":" + dbPort + ":" + dbName, dbUserName,
					dbPassword);
			System.out.println("Oracle " + "jdbc:oracle:thin:@" + dbIp + ":" + dbPort + ":" + dbName);
		}
		return con;
	}

	public String dbRequest(String sqlName) throws SQLException {
		return dbRequest(sqlName,new HashMap());
	}
	public String dbRequest(String sqlName, Map replaceVar) throws SQLException {
		sqlQuerySpec = (Map<String, Object>) sqlYmlFile.get(sqlName);
//		System.out.println(sqlQuerySpec);
		String query = Utility.replaceBodyVar(sqlQuerySpec.get("query") + "", replaceVar);
		PreparedStatement ps = null;
		ResultSet rs = null;
		String jsonString = "";
		JSONObject dbData = new JSONObject();

		try {
			ps = con.prepareStatement(query);
			rs = ps.executeQuery();
			JSONObject jsonobject = null;
			JSONArray jsonArray = new JSONArray();
			ResultSet resultSet = ps.executeQuery();

			while (resultSet.next()) {

				ResultSetMetaData metaData = resultSet.getMetaData();
				jsonobject = new JSONObject();

				for (int i = 0; i < metaData.getColumnCount(); i++) {

					jsonobject.put(metaData.getColumnLabel(i + 1), resultSet.getObject(i + 1));

				}

				jsonArray.put(jsonobject);
			}

			if (jsonArray.length() > 0) {

//				jsonString = jsonArray.toString();
				dbData.put("dbData", jsonArray);
				jsonString = dbData.toString();
			}

//			dbData.put("dbData", jsonArray);
//			System.out.println(dbData.toString());
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (con != null) {
				con.close();
			}
		}
		return jsonString;
	}
	
	public String extractData(String jsonString,String proprty, String replaceVar) {
		Map<String, String> map = (Map<String, String>) sqlQuerySpec.get("response");
		return Utility.extractDataWithJsonPath(jsonString,proprty,replaceVar,map);
	}
	public String extractData(String jsonString, String proprty) {
		return extractData(jsonString, proprty, "");
	}

}
