package com.integration.processor;

import java.io.Serializable;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.permastore.config.IPermaStoreConfigurationService;
import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationUnit;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigurationService;
import com.attunedlabs.permastore.config.jaxb.ConfigurationBuilder;
import com.attunedlabs.permastore.config.jaxb.ConfigurationBuilderType;
import com.attunedlabs.permastore.config.jaxb.Event;
import com.attunedlabs.permastore.config.jaxb.FeatureInfo;
import com.attunedlabs.permastore.config.jaxb.InlineBuilder;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration;
import com.attunedlabs.permastore.config.jaxb.PermaStoreEvent;
import com.attunedlabs.permastore.config.jaxb.SubscribePermaStoreEvents;
import com.integration.exception.PermaStoreDoesNotExistException;
import com.integration.exception.UnableToCreatePermaStoreException;
import com.integration.exception.UnableToGetPermaStoreException;
import com.integration.exception.UnableToUpdatePermaStoreException;

public class IntegrationPermaStoreUtil {
	final static Logger  logger= LoggerFactory.getLogger(IntegrationPermaStoreUtil.class.getName());
	public static JSONObject getPermaStore(Exchange exchange, String permaName)
			throws UnableToGetPermaStoreException, PermaStoreDoesNotExistException {
		try {
			LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
			RequestContext requestContext = leapHeader.getRequestContext();
			IPermaStoreConfigurationService permastoreConfigService = new PermaStoreConfigurationService();
			PermaStoreConfigurationUnit permaStoreConfig = permastoreConfigService
					.getPermaStoreConfiguration(requestContext, permaName);

			if (permaStoreConfig == null) {
				logger.error("permastore " + permaName + " is null");
				throw new PermaStoreDoesNotExistException(
						"unable to get permastore: permastore " + permaName + " is null");
			} else {
				org.json.simple.JSONObject json = (org.json.simple.JSONObject) permaStoreConfig.getConfigData();
				JSONObject parmaJson = null;
				if (json != null && !json.isEmpty()) {
					parmaJson = new JSONObject(json.toJSONString());
					return parmaJson;
				} else {
					throw new PermaStoreDoesNotExistException(
							"unable to get permastore: permastore " + permaName + " is null or empty");
				}
			}
		} catch (JSONException | InvalidNodeTreeException | PermaStoreConfigRequestException e) {
			throw new UnableToGetPermaStoreException("unable to get permastore " + e.getMessage());
		}

	}
	
	
	public static void updatePermaStore(Exchange exchange, String permaName, Object permastoreInLineCacheData,
			String key) throws UnableToUpdatePermaStoreException, UnableToCreatePermaStoreException {
		logger.info(".updatePermaStore() ");
		try {
			LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
			RequestContext requestContext = leapHeader.getRequestContext();
			IPermaStoreConfigurationService permastoreConfigService = new PermaStoreConfigurationService();
			PermaStoreConfigurationUnit permaStoreConfig = permastoreConfigService
					.getPermaStoreConfiguration(requestContext, permaName);

			if (permaStoreConfig == null) {
				logger.error("permastore " + permaName + " is null");
				JSONObject json = new JSONObject();
				if(permastoreInLineCacheData instanceof JSONObject)
					json.put(key, (JSONObject)permastoreInLineCacheData);
				if(permastoreInLineCacheData instanceof JSONArray)
					json.put(key, (JSONArray)permastoreInLineCacheData);
				createAndLoadPermastoreInCache(exchange, permaName, json.toString());
			} else {
				org.json.simple.JSONObject json = (org.json.simple.JSONObject) permaStoreConfig.getConfigData();
				JSONObject parmaJson = null;
				if (json != null ) {
					parmaJson = new JSONObject(json.toJSONString());
					String featureGroup = requestContext.getFeatureGroup();
					String featureName = requestContext.getFeatureName();
					if(permastoreInLineCacheData instanceof JSONObject)
						parmaJson.put(key, (JSONObject)permastoreInLineCacheData);
					if(permastoreInLineCacheData instanceof JSONArray)
						parmaJson.put(key, (JSONArray)permastoreInLineCacheData);
					logger.debug("updated permastore is :"+parmaJson.toString());
					permaStoreConfig.setConfigData((Serializable) parmaJson.toString());
					permaStoreConfig.setPermaStoreConfig(mapDetailsToPermastoreConfigBeforeUpdate(
							parmaJson.toString(), permaName, true, featureGroup, featureName));
					permastoreConfigService.updatePermaStoreConfiguration(requestContext.getConfigurationContext(),
							permaStoreConfig.getPermaStoreConfig(), permaStoreConfig.getDbconfigId());
				}

			}
		} catch (Exception e) {
			throw new UnableToUpdatePermaStoreException(
					"unable to update eventTrackerPermaStore permastore " + e.getMessage());
		}
	}
	
	private static void createAndLoadPermastoreInCache(Exchange exchange, String permastoreConfigname,
			String permastoreInLineCacheData) throws UnableToCreatePermaStoreException {
		try {
			logger.info(".createAndAddPermastoreConfig() ");
			logger.info("permastoreConfigname :" + permastoreConfigname);
			logger.info("permastoreInLineCacheData :" + permastoreInLineCacheData);
			IPermaStoreConfigurationService permastoreConfigService = new PermaStoreConfigurationService();
			LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
			RequestContext requestContext = leapHeader.getRequestContext();
			String featureGroup = requestContext.getFeatureGroup();
			String featureName = requestContext.getFeatureName();
			PermaStoreConfiguration permastoreConfiguration = mapDetailsToPermastoreConfigBeforeUpdate(
					permastoreInLineCacheData, permastoreConfigname, false, featureGroup, featureName);
			permastoreConfigService.addPermaStoreConfiguration(requestContext.getConfigurationContext(),
					permastoreConfiguration);
		} catch (Exception e) {
			throw new UnableToCreatePermaStoreException("unable to create permastore " + e.getMessage(), e);
		}

	}
	
	
	private static PermaStoreConfiguration mapDetailsToPermastoreConfigBeforeUpdate(String jsonCacheData,
			String permastoreConfigname, boolean isGlobalPermastore, String featureGroup, String featureName) {

		InlineBuilder inlineBuilder = new InlineBuilder();
		inlineBuilder.setType("JSON-TO-Map");
		inlineBuilder.setValue(jsonCacheData);
		// defined configuration builder
		ConfigurationBuilder configBuilder = new ConfigurationBuilder();
		configBuilder.setType(ConfigurationBuilderType.INLINE);
		configBuilder.setInlineBuilder(inlineBuilder);

		// define permastore event
		PermaStoreEvent permaStoreEvent = new PermaStoreEvent();
		permaStoreEvent.setOnEvent(Event.RELOAD);
		permaStoreEvent.setEventName("xyz");
		permaStoreEvent.setPermaStoreEventHandler("com.getusroi.inventory.HandlePicAddressAddition");
		// define subscription in permastore
		SubscribePermaStoreEvents subscribePermaStoreEvents = new SubscribePermaStoreEvents();
		subscribePermaStoreEvents.setPermaStoreEvent(permaStoreEvent);

		FeatureInfo featureInfo = new FeatureInfo();
		featureInfo.setFeatureName(featureName);
		featureInfo.setFeatureGroup(featureGroup);

		// create permastoreconfiguration
		PermaStoreConfiguration permastoreConfiguration = new PermaStoreConfiguration();
		permastoreConfiguration.setName(permastoreConfigname);
		permastoreConfiguration.setIsEnabled(true);
		permastoreConfiguration.setIsGlobal(isGlobalPermastore);
		permastoreConfiguration.setConfigurationBuilder(configBuilder);
		permastoreConfiguration.setSubscribePermaStoreEvents(subscribePermaStoreEvents);
		permastoreConfiguration.setDataType("JSON");
		permastoreConfiguration.setFeatureInfo(featureInfo);

		return permastoreConfiguration;
	}
	
	
	public static void deleteEventIdFromPermaStore(Exchange exchange, String permaName, String key)
			throws UnableToUpdatePermaStoreException, UnableToCreatePermaStoreException,
			PermaStoreDoesNotExistException {
		logger.info(".deleteEventIdFromPermaStore() ");
		try {
			LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
			RequestContext requestContext = leapHeader.getRequestContext();
			IPermaStoreConfigurationService permastoreConfigService = new PermaStoreConfigurationService();
			PermaStoreConfigurationUnit permaStoreConfig = permastoreConfigService
					.getPermaStoreConfiguration(requestContext, permaName);

			if (permaStoreConfig == null) {
				logger.error("permastore " + permaName + " is null");

			} else {
				org.json.simple.JSONObject json = (org.json.simple.JSONObject) permaStoreConfig.getConfigData();
				JSONObject parmaJson = null;
				if (json != null && !json.isEmpty()) {
					parmaJson = new JSONObject(json.toJSONString());
					String featureGroup = requestContext.getFeatureGroup();
					String featureName = requestContext.getFeatureName();
					if (parmaJson.has(key)) {
						parmaJson.remove(key);
						permaStoreConfig.setConfigData((Serializable) parmaJson.toString());
						permaStoreConfig.setPermaStoreConfig(mapDetailsToPermastoreConfigBeforeUpdate(
								parmaJson.toString(), permaName, true, featureGroup, featureName));
						permastoreConfigService.updatePermaStoreConfiguration(requestContext.getConfigurationContext(),
								permaStoreConfig.getPermaStoreConfig(), permaStoreConfig.getDbconfigId());
					}
				}

			}
		} catch (PermaStoreConfigRequestException | PermaStoreConfigurationException | PermaStoreConfigParserException | JSONException  e) {
			throw new UnableToUpdatePermaStoreException(
					"unable to update eventTrackerPermaStore permastore " + e.getMessage());
		}
	}
}
