package com.nishant.service.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class YmlReader {
	
	public Map<String, Object> readYml(String filePath) throws IOException {
		Map<String, Object> map = null;
		InputStream inputStream = null;
		Yaml yaml;
		try {

			yaml = new Yaml();
			inputStream = new FileInputStream(new File(filePath));
			map = yaml.load(inputStream);
		} catch (Exception e) {
			System.out.println("here "+e);
		} finally {
			inputStream.close();
		}
		return map;
	}
}

