package com.nishant.service.apiservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.nishant.service.Service;
import com.nishant.service.reader.YmlReader;
import com.nishant.service.util.Utility;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class APIService extends Service {

	public Map<String, Object> apiYmlFile;
	Map<String, Object> apiSpecTemp;

	public APIService(String configPropertyFilePath) throws IOException {
		super(configPropertyFilePath);
		YmlReader ymlReader = new YmlReader();
		this.apiYmlFile = ymlReader.readYml(apiYmlFilePath);
	}

	public Response apiRequest(String apiName, Map replaceVar)
			throws JsonParseException, JsonMappingException, IOException {

		System.out.println("\n*******************************START: API SERVICE CALL: " + apiName
				+ "*******************************");
		Map<String, Object> apiSpec = (Map<String, Object>) apiYmlFile.get(apiName);
		apiSpecTemp = new HashMap(apiSpec);
		if (replaceVar != null)
			apiSpec = replaceVariableInApiSpec(apiSpec, replaceVar);
		System.out.println("spec  " + apiSpec);
		RequestSpecification requestSpec = RestAssured.given();

		requestSpec = addBaseUrl(requestSpec, apiSpec);
		requestSpec = addEndpoint(requestSpec, apiSpec);
		requestSpec = addQueryParam(requestSpec, apiSpec);
		requestSpec = addPathParam(requestSpec, apiSpec);
		requestSpec = addHeaderParam(requestSpec, apiSpec);
		requestSpec = addBody(requestSpec, apiSpec, replaceVar);
		System.out.println(requestSpec.given().log().all());
		Response response = apiResponse(requestSpec, apiSpec); // According to METHOD
		System.out.println(response.getBody().asString());

		System.out.println("*******************************END: API SERVICE CALL: " + apiName
				+ "*******************************\n");
		return response;
	}

	public Response apiRequest(String apiName) throws JsonParseException, JsonMappingException, IOException {
		return apiRequest(apiName, null);
	}

	/*----------------------------    SERVICE SPECIFICATIONS    ------------------------------*/
	public RequestSpecification addBaseUrl(RequestSpecification requestSpec, Map<String, Object> apiSpec) {
		if (!Utility.IsBlank(apiSpec.get("baseurl") + ""))
			return requestSpec.baseUri(apiSpec.get("baseurl") + "");

		return requestSpec.baseUri(apiYmlFile.get("baseurl") + "");
	}

	public RequestSpecification addEndpoint(RequestSpecification requestSpec, Map<String, Object> apiSpec) {

		return requestSpec.basePath(apiSpec.get("endpoint") + "");

	}

	public RequestSpecification addQueryParam(RequestSpecification requestSpec, Map<String, Object> apiSpec) {
		Map<String, String> queryparams = (Map<String, String>) apiSpec.get("queryparams");
		if (queryparams != null)
			requestSpec.queryParameters(queryparams);
		return requestSpec;
	}

	public RequestSpecification addPathParam(RequestSpecification requestSpec, Map<String, Object> apiSpec) {
		Map<String, String> pathparams = (Map<String, String>) apiSpec.get("pathparams");
		if (pathparams != null)
			requestSpec.pathParams(pathparams);
		return requestSpec;
	}

	public RequestSpecification addHeaderParam(RequestSpecification requestSpec, Map<String, Object> apiSpec) {
		Map<String, String> headers = (Map<String, String>) apiSpec.get("headers");
		if (headers != null)
			requestSpec.headers(headers);
		return requestSpec;
	}

	public RequestSpecification addBody(RequestSpecification requestSpec, Map<String, Object> apiSpec,
			Map<String, String> replace) throws IOException {
		String bodypath = (String) apiSpec.get("body");
		String body = "";
		if (bodypath != null) {
			body = readFileAsString(bodypath);
			body = Utility.replaceBodyVar(body, replace);
			requestSpec.body(body);
		}
		return requestSpec;
	}

	public String readFileAsString(String fileName) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get("src/body/" + fileName));
		return new String(encoded);
	}

	public Response apiResponse(RequestSpecification requestSpec, Map<String, Object> apiSpec) {
		Response response = null;
		String method = (String) apiSpec.get("method");

		if (method.equalsIgnoreCase("GET")) {
			response = requestSpec.get();
		} else if (method.equalsIgnoreCase("PUT")) {
			response = requestSpec.put();
		} else if (method.equalsIgnoreCase("POST")) {
			response = requestSpec.post();
		} else if (method.equalsIgnoreCase("DELETE")) {
			response = requestSpec.delete();
		}
		return response;
	}

	private Map<String, Object> replaceVariableInApiSpec(Map<String, Object> apiSpec, Map replaceVar) {
		String str;
		Map<String, Object> temp = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : apiSpec.entrySet()) {

			if (entry.getValue() instanceof String) {
				str = entry.getValue() + "";
				temp.put(entry.getKey(), Utility.replaceStr(str, replaceVar));
			} else if (!entry.getKey().equalsIgnoreCase("response")) {
				Map<String, String> mp = (Map<String, String>) entry.getValue();
				Map<String, String> tm = new HashMap<String, String>();
				for (Map.Entry<String, String> entryInner : mp.entrySet()) {
					str = entryInner.getValue();
					tm.put(entryInner.getKey(), Utility.replaceStr(str, replaceVar));
				}
				temp.put(entry.getKey(), tm);
			}
		}
		return temp;
	}

	public String extractData(String jsonString, String proprty, String replaceVar) {
		Map<String, String> map = (Map<String, String>) apiSpecTemp.get("response");
		return Utility.extractDataWithJsonPath(jsonString, proprty, replaceVar, map);
	}

	public String extractData(Response response, String proprty, String replaceVar) {
		return extractData(response.asString(), proprty, replaceVar);
	}

	public String extractData(String jsonString, String proprty) {
		return extractData(jsonString, proprty, "");
	}

	public String extractData(Response response, String proprty) {
		return extractData(response.asString(), proprty, "");
	}

}
