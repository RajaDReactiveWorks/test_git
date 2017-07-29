package com.getusroi.permastore.config.impl;

import java.io.Serializable;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.permastore.config.PermaStoreConfigurationBuilderException;
import com.getusroi.permastore.config.PermaStoreConfigurationConstant;
import com.getusroi.permastore.config.jaxb.InlineBuilder;
/**
 * 
 * Class for parsing and building the cacheable Object from the inLine Config from the 
 * Configuration
 * @author bizruntime
 *
 */
public class PermaStoreInLineCacheObjectBuilder {
	final Logger logger = LoggerFactory.getLogger(PermaStoreInLineCacheObjectBuilder.class);
	
	/**
	 *  Builds the Object to be cached from the InLine Configuration.<br>
	 *  It supports JSON string only as an inlineConfigurationbuilder
	 * @param inlineconfigBuilderConfig
	 * @return
	 * @throws PermaStoreConfigurationBuilderException
	 */
	public Serializable loadDataForCache(InlineBuilder inlineconfigBuilderConfig) throws PermaStoreConfigurationBuilderException{
		String inLineBuilderType=inlineconfigBuilderConfig.getType();
		if(inLineBuilderType.equalsIgnoreCase(PermaStoreConfigurationConstant.INLINE_CONFIGBUILDER_JSONTOMAP)){
			return (Serializable) jsontoMap(inlineconfigBuilderConfig);
		}
		throw new PermaStoreConfigurationBuilderException("Unsupported InLine-ConfigurationBuilder InLine-Type="+inLineBuilderType);
	}//end of method()
	
	/**
	 * Converts JSON String to Map
	 * @param inlineconfigBuilderConfig
	 * @return
	 * @throws PermaStoreConfigurationBuilderException
	 */
	private Map jsontoMap(InlineBuilder inlineconfigBuilderConfig) throws PermaStoreConfigurationBuilderException{
		String inLineJsonString=inlineconfigBuilderConfig.getValue();
		logger.debug("jsontoMap() jsonString to build from is"+inLineJsonString);
		JSONParser parser = new JSONParser();
		try {
			//Returning JSONObject itself as it extends the HasMap and is valid Serializable Map
			JSONObject jsonObject= (JSONObject)parser.parse(inLineJsonString);
			return jsonObject;
			
		} catch (ParseException e) {
			throw new PermaStoreConfigurationBuilderException("Failed to parse JSON String for InLine JSON-TO-Map PermaStoreConfigurationBuilder ",e);
		}
		
	}//end of method()
}
