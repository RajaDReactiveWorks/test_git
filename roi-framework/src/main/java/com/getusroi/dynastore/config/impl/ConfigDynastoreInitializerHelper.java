package com.getusroi.dynastore.config.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.core.BeanDependencyResolveException;
import com.getusroi.core.BeanDependencyResolverFactory;
import com.getusroi.core.IBeanDependencyResolver;
import com.getusroi.dynastore.config.DynaStoreConfigurationConstant;
import com.getusroi.dynastore.config.IDynaStoreCustomInitializer;
import com.getusroi.dynastore.config.jaxb.CustomBuilder;
import com.getusroi.dynastore.config.jaxb.DynastoreConfiguration;
import com.getusroi.dynastore.config.jaxb.DynastoreInitializer;
import com.getusroi.dynastore.config.jaxb.InlineBuilder;
import com.getusroi.dynastore.config.jaxb.SQLBuilder;



public class ConfigDynastoreInitializerHelper {
	final Logger logger = LoggerFactory.getLogger(ConfigDynastoreInitializerHelper.class);
	
	
	/**
	 * Handles the Building of Cacheable Object based on the configuration 
	 * given in the xml config.
	 * 
	 * @param configBuilder
	 * @return
	 * @throws PermaStoreConfigurationBuilderException
	 */
	public Map initializeDynaStore(DynastoreConfiguration dynaConfig) throws ConfigDynaStoreInitializationException {
		String dynaConfigName=dynaConfig.getDynastoreName().getValue();
		String version=dynaConfig.getDynastoreName().getVersion();
		DynastoreInitializer initializerConfig=dynaConfig.getDynastoreInitializer();
		if(initializerConfig==null || !initializerConfig.isRequired()){
			//No Initialization is Required for DynaStore it will be blank to start with.Create a Map
			logger.debug(".initializeDynaStore() no initialization configured returning blank Map");
			return new HashMap(); 
		}
				
		String dynaCacheBuilderType=initializerConfig.getType().value();
		Map objToCache=null;
		
		if(dynaCacheBuilderType.equalsIgnoreCase(DynaStoreConfigurationConstant.CONFIGBUILDER_CUSTOM)){
			
			//Custom Builder Handling
			CustomBuilder customBuilderConfig=initializerConfig.getCustomBuilder();
			IDynaStoreCustomInitializer customBuilder=null;
			//It is possible the class defined in config is incorrect or not resolvable
			try{
				IBeanDependencyResolver beanResolver=BeanDependencyResolverFactory.getBeanDependencyResolver();
				customBuilder=(IDynaStoreCustomInitializer)beanResolver.getBeanInstance(IDynaStoreCustomInitializer.class, customBuilderConfig.getBuilder());
			}catch(BeanDependencyResolveException beanResolveExp){
				throw new ConfigDynaStoreInitializationException("Unable to Load/instantiate DynaStoreCustomInitializer="+customBuilderConfig.getBuilder(),beanResolveExp);
			}
			
			if(customBuilder==null)
				throw new ConfigDynaStoreInitializationException("Unable to Load/instantiate DynaStoreCustomInitializer="+customBuilderConfig.getBuilder());
			//Call custom builder and build the object to be cached
			objToCache=customBuilder.initializeDynastoreWithData(initializerConfig);
			logger.debug(".initializeDynaStore() CustomBuilder{"+customBuilderConfig.getBuilder()+"} dynaCollection="+objToCache);
			return objToCache;
		}else if(dynaCacheBuilderType.equalsIgnoreCase(DynaStoreConfigurationConstant.CONFIGBUILDER_INLINE) ){
			//Inline Builder Handling
			InlineBuilder inlineConfigBuilder=initializerConfig.getInlineBuilder();
			if(inlineConfigBuilder==null){
				throw new ConfigDynaStoreInitializationException("Type is configured as INLINE but <InlineBuilder> tag is missing");
			}
			objToCache=initializeDynastoreWithJsonMapLineBuilder(inlineConfigBuilder);
			logger.debug(".initializeDynaStore() InLineBuilder{"+inlineConfigBuilder.getValue()+"} dynaCollection="+objToCache);
			return objToCache;
		}else if(dynaCacheBuilderType.equalsIgnoreCase(DynaStoreConfigurationConstant.CONFIGBUILDER_SQL)){
			SQLBuilder sqlBuilder=initializerConfig.getSQLBuilder();
			if(sqlBuilder==null || sqlBuilder.getSQLQuery()==null || sqlBuilder.getSQLQuery().getValue()==null ||sqlBuilder.getSQLQuery().getMappedClass()==null){
				throw new ConfigDynaStoreInitializationException("Failed to Build from SQLBUilder SQLqUERY OR MAPPED CLASS IS MISSING IN CONFIG");
			}
			DynaSQLCacheObjectBuilder sqlCacheBuilder=new DynaSQLCacheObjectBuilder();
			objToCache=sqlCacheBuilder.initializeDynastoreWithData(initializerConfig);
			return objToCache;
		}else{
			//Unknown Builder Type throw exception
			throw new ConfigDynaStoreInitializationException("UnKnown-DynastoreConfigBuilderType for configType="+dynaCacheBuilderType);
		}
	}
	

	/**
	 *  Builds the Object to be cached from the InLine Configuration.<br>
	 *  It supports JSON string only as an inlineConfigurationbuilder
	 * @param inlineconfigBuilderConfig
	 * @return
	 * @throws PermaStoreConfigurationBuilderException
	 */
	private Map<String,Serializable> initializeDynastoreWithJsonMapLineBuilder(InlineBuilder inlineconfigBuilderConfig) throws ConfigDynaStoreInitializationException{
		String inLineBuilderType=inlineconfigBuilderConfig.getType();
		if(inLineBuilderType.equalsIgnoreCase(DynaStoreConfigurationConstant.INLINE_CONFIGBUILDER_JSONTOMAP)){
			return  jsontoMap(inlineconfigBuilderConfig);
		}
		throw new ConfigDynaStoreInitializationException("Unsupported InLine-ConfigurationBuilder InLine-Type="+inLineBuilderType);
	}//end of method()
	
	/**
	 * Converts JSON String to Map
	 * @param inlineconfigBuilderConfig
	 * @return
	 * @throws PermaStoreConfigurationBuilderException
	 */
	private Map<String,Serializable> jsontoMap(InlineBuilder inlineconfigBuilderConfig) throws ConfigDynaStoreInitializationException{
		String inLineJsonString=inlineconfigBuilderConfig.getValue();
		logger.debug("jsontoMap() jsonString to build from is"+inLineJsonString);
		JSONParser parser = new JSONParser();
		try {
			//Returning JSONObject itself as it extends the HasMap and is valid Serializable Map
			JSONObject jsonObject= (JSONObject)parser.parse(inLineJsonString);
			return jsonObject;
			
		} catch (ParseException e) {
			throw new ConfigDynaStoreInitializationException("Failed to parse JSON String for InLine JSON-TO-Map for ConfigurableDynaStore ",e);
		}
	}//end of method()
}
