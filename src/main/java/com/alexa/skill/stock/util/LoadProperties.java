package com.alexa.skill.stock.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadProperties {

	private static List<String> propertiesFileData = new ArrayList<String>();
	private static final Logger log = LoggerFactory.getLogger(LoadProperties.class);
	
	public LoadProperties(){
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			String fileName = "StockNameAndSymbol.properties";
			input = getClass().getClassLoader().getResourceAsStream(fileName);
			if(input == null) {
				log.debug("Sorry, unable to find file "+fileName);
			}
			
			prop.load(input);
			
			Enumeration<?> e = prop.propertyNames();
			while(e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = prop.getProperty(key);
				propertiesFileData.add(key+"|"+ value);
			}
		}catch(IOException io) {
			log.debug(io.getMessage());
		}finally {
			if(input!=null) {
				try {
					input.close();
				}catch(IOException e) {
					log.debug(e.getLocalizedMessage());
				}
			}
		}
	}

	public List<String> getCode(String stockName) {
		List<String> result = propertiesFileData.stream()   // convert list to stream
                .filter(line -> line.replace("_", " ").toLowerCase().contains(stockName.toLowerCase()))   // we need one that contains key
                .distinct()
                .collect(Collectors.toList());              // collect the output and convert streams to a List

		return result;
	}
}
