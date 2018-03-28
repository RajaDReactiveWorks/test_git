package com.attunedlabs.eventsubscription.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.http.client.methods.HttpPost;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.mvel2.MVEL;
import org.mvel2.compiler.CompiledExpression;
import org.mvel2.compiler.ExpressionCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.jaxb.FailureHandlingStrategy;
import com.attunedlabs.eventframework.jaxb.HeaderParam;
import com.attunedlabs.eventframework.jaxb.HeaderParams;
import com.attunedlabs.eventframework.jaxb.HttpPostRequest;
import com.attunedlabs.eventframework.jaxb.InvokeCamelRoute;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.abstractretrystrategy.InstantiateSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.exception.ConfigurationValidationFailedException;
import com.attunedlabs.eventsubscription.exception.MissingConfigurationException;
import com.attunedlabs.eventsubscription.retrypolicy.SubscriptionRetryPolicy;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;

public class SubscriptionUtil {
	final static Logger log = LoggerFactory.getLogger(SubscriptionUtil.class);

	/**
	 * adding routing info to the Map.
	 * 
	 * @param routeInfoMap
	 * @param action
	 * @return map with feature-route-info
	 */
	public Map<String, HashMap<String, String>> addExtraHeadersToEndpoint(
			Map<String, HashMap<String, String>> routeInfoMap, Object action) {
		String featureGroupForInvoke = "";
		String featureNameForInvoke = "";
		String serviceTypeForInvoke = "";
		String hostName = "";
		String portAddress = "";

		if (action instanceof InvokeCamelRoute) {
			InvokeCamelRoute invokeCamelRoute = (InvokeCamelRoute) action;
			HashMap<String, String> invokeCamelRouteMap = new HashMap<>();

			if (invokeCamelRoute != null) {
				featureGroupForInvoke = invokeCamelRoute.getFeatureGroup();
				featureNameForInvoke = invokeCamelRoute.getFeatureName();
				serviceTypeForInvoke = invokeCamelRoute.getServiceType();
				if (!attributeEmptyCheck(featureGroupForInvoke))
					invokeCamelRouteMap.put(LeapHeaderConstant.FEATURE_GROUP_KEY, featureGroupForInvoke.trim());
				if (!attributeEmptyCheck(featureNameForInvoke))
					invokeCamelRouteMap.put(LeapHeaderConstant.FEATURE_KEY, featureNameForInvoke.trim());
				if (!attributeEmptyCheck(serviceTypeForInvoke))
					invokeCamelRouteMap.put(LeapHeaderConstant.SERVICETYPE_KEY, serviceTypeForInvoke.trim());
				routeInfoMap.put(SubscriptionConstant.INVOKE_ENDPOINT_KEY, invokeCamelRouteMap);
			}
		} else if (action instanceof HttpPostRequest) {
			HttpPostRequest httpPostRequest = (HttpPostRequest) action;
			HashMap<String, String> serviceHeaderMap = new HashMap<>();

			if (httpPostRequest != null) {
				featureGroupForInvoke = httpPostRequest.getFeatureGroup().trim();
				featureNameForInvoke = httpPostRequest.getFeatureName().trim();
				serviceTypeForInvoke = httpPostRequest.getServiceType().trim();
				hostName = httpPostRequest.getHostName().trim();
				portAddress = new Short(httpPostRequest.getPort()).toString();

				if (!attributeEmptyCheck(featureGroupForInvoke))
					serviceHeaderMap.put(LeapHeaderConstant.FEATURE_GROUP_KEY, featureGroupForInvoke);
				if (!attributeEmptyCheck(featureNameForInvoke))
					serviceHeaderMap.put(LeapHeaderConstant.FEATURE_KEY, featureNameForInvoke);
				if (!attributeEmptyCheck(serviceTypeForInvoke))
					serviceHeaderMap.put(LeapHeaderConstant.SERVICETYPE_KEY, serviceTypeForInvoke);
				if (!attributeEmptyCheck(hostName))
					serviceHeaderMap.put(SubscriptionConstant.HOST_NAME_KEY, hostName);
				if (!attributeEmptyCheck(portAddress))
					serviceHeaderMap.put(SubscriptionConstant.PORT_ADDRESS_KEY, portAddress);
				routeInfoMap.put(SubscriptionConstant.HTTP_POST_REQUEST_SERVICE_CALL_KEY, serviceHeaderMap);

			}
		}
		return routeInfoMap;
	}

	/***
	 * check Map contains featureInfo for routing.
	 * 
	 * @param invokeCamelRouteMap
	 * @return
	 */
	public boolean mapCheck(HashMap<String, String> invokeCamelRouteMap) {
		return invokeCamelRouteMap.containsKey(LeapHeaderConstant.FEATURE_GROUP_KEY)
				&& invokeCamelRouteMap.containsKey(LeapHeaderConstant.FEATURE_KEY)
				&& invokeCamelRouteMap.containsKey(LeapHeaderConstant.SERVICETYPE_KEY);

	}

	/**
	 * check the null and empty for attribute.
	 * 
	 */
	public boolean attributeEmptyCheck(String attribute) {
		return attribute == null || attribute.trim().isEmpty();
	}

	/**
	 * evaluate mvel on the event body passed and return true. if no mvel then
	 * return true.
	 * 
	 * @param mvelExpressionRule
	 * @param eventBody
	 * @return
	 */
	public boolean evaluateMVELForCriteriaMatch(String mvelExpressionRule, JSONObject eventBody) {
		log.debug("mvel-expression: " + mvelExpressionRule + " for event body " + eventBody);
		if (mvelExpressionRule != null && !mvelExpressionRule.trim().isEmpty()) {
			try {
				CompiledExpression expression = new ExpressionCompiler(mvelExpressionRule.trim()).compile();
				return (boolean) MVEL.executeExpression(expression, jsonToMap(eventBody));
			} catch (Exception e) {
				e.printStackTrace();
				log.error("unable to get evaluate mvel...", e.getMessage());
			}

			return false;
		} else
			return true;
	}

	/**
	 * build the configuration context based on the request context passed from
	 * the event body.
	 * 
	 * @param eventBody
	 *            consumed data.
	 * @return {@link ConfigurationContext}
	 */
	public ConfigurationContext getConfigContextFromEventBody(JSONObject eventBody) {
		ConfigurationContext configurationContext = null;
		try {
			String jsonContext = eventBody.getJSONObject("eventHeader").getString("EVT_CONTEXT");
			configurationContext = new ObjectMapper().readValue(jsonContext, ConfigurationContext.class);
			log.debug("config context getConfigContextFromEventBody.. " + configurationContext);
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			log.error("unable to get requestCtx  from event body...", e);
		}
		return configurationContext;
	}

	/**
	 * converts the json to map.
	 * 
	 * @param json
	 * @return
	 * @throws JSONException
	 */
	public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
		Map<String, Object> retMap = new HashMap<String, Object>();

		if (json != JSONObject.NULL) {
			retMap = toMap(json);
		}
		return retMap;
	}

	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

		Iterator<String> keysItr = object.keys();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	public static List<Object> toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

	/**
	 * utility that constructs the kafka uri.
	 * 
	 * @param topicNames
	 * @param subscriptionId
	 * @param subscriptionUtil
	 * @param randomClientId
	 * @param props
	 * @return kafkaconsumer uri
	 */
	public static String constructKafkaURI(String topicNames, String subscriptionId, SubscriptionUtil subscriptionUtil,
			Random randomClientId, Properties props) {
		String newEndpointUri = "";

		String brokerHostPort = kafkaConfigAssignation(props, SubscriptionConstant.KAFKA_BROKER_HOST_PORT_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_BROKER_HOST_VALUE);

		String clientId = kafkaConfigAssignation(props, SubscriptionConstant.KAFKA_CLIENT_ID_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_CLIENT_ID_VALUE);

		String consumerCount = kafkaConfigAssignation(props, SubscriptionConstant.KAFKA_CONSUMER_COUNT_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_CONSUMER_COUNT_VALUE);

		String autoOffsetReset = kafkaConfigAssignation(props, SubscriptionConstant.KAFKA_AUTO_OFFSET_RESET_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_AUTO_OFFSET_RESET_VALUE);

		String autoCommitEnable = kafkaConfigAssignation(props,
				SubscriptionConstant.KAFKA_AUTO_COMMIT_ENABLE_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_AUTO_COMMIT_ENABLE_VALUE);

		String autoCommitOnStop = kafkaConfigAssignation(props,
				SubscriptionConstant.KAFKA_AUTO_COMMIT_ON_STOP_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_AUTO_COMMIT_ON_STOP_VALUE);

		String breakOnFirstError = kafkaConfigAssignation(props,
				SubscriptionConstant.KAFKA_BREAK_ON_FIRST_ERROR_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_BREAK_ON_FIRST_ERROR_VALUE);

		String autoCommitIntervalMs = kafkaConfigAssignation(props,
				SubscriptionConstant.KAFKA_AUTO_COMMIT_INTERVAL_MS_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_AUTO_COMMIT_INTERVAL_MS_VALUE);

		String maxPollRecords = kafkaConfigAssignation(props, SubscriptionConstant.KAFKA_MAX_POLL_RECORDS_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_MAX_POLL_RECORDS_VALUE);

		String pollTimeoutMs = kafkaConfigAssignation(props, SubscriptionConstant.KAFKA_POLL_TIMEOUT_MS_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_POLL_TIMEOUT_MS_VALUE);

		String sessionTimeoutMs = kafkaConfigAssignation(props,
				SubscriptionConstant.KAFKA_SESSION_TIMEOUT_MS_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_SESSION_TIMEOUT_MS_VALUE);

		String consumerRequestTimeoutMs = kafkaConfigAssignation(props,
				SubscriptionConstant.KAFKA_CONSUMER_REQUEST_TIMEOUT_MS_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_CONSUMER_REQUEST_TIMEOUT_MS_VALUE);

		String fetchWaitMaxMs = kafkaConfigAssignation(props, SubscriptionConstant.KAFKA_FETCH_WAIT_MAX_MS_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_FETCH_WAIT_MAX_MS_VALUE);

		String clientIdGenerated = clientId + "_" + subscriptionUtil.getActualSubscriberId(subscriptionId)
				+ randomClientId.nextInt(10000);
		log.debug("generated client Id for subscriber " + subscriptionId + " is " + clientIdGenerated);
		newEndpointUri = SubscriptionConstant.KAFKA_ENDPOINT_URI
				.replace(SubscriptionConstant.KAFKA_BROKER_HOST_PORT_URL_KEY, brokerHostPort)
				.replace(SubscriptionConstant.KAFKA_TOPIC_NAME_URL_KEY, topicNames)
				.replace(SubscriptionConstant.KAFKA_GROUP_ID_URL_KEY, subscriptionId)
				.replace(SubscriptionConstant.KAFKA_CLIENT_ID_URL_KEY, clientIdGenerated)
				.replace(SubscriptionConstant.KAFKA_CONSUMER_COUNT_URL_KEY, consumerCount)
				.replace(SubscriptionConstant.KAFKA_AUTO_COMMIT_ENABLE_URL_KEY, autoCommitEnable)
				.replace(SubscriptionConstant.KAFKA_AUTO_OFFSET_RESET_URL_KEY, autoOffsetReset)
				.replace(SubscriptionConstant.KAFKA_AUTO_COMMIT_ON_STOP_URL_KEY, autoCommitOnStop)
				.replace(SubscriptionConstant.KAFKA_BREAK_ON_FIRST_ERROR_URL_KEY, breakOnFirstError)
				.replace(SubscriptionConstant.KAFKA_MAX_POLL_RECORDS_URL_KEY, maxPollRecords)
				.replace(SubscriptionConstant.KAFKA_POLL_TIMEOUT_MS_URL_KEY, pollTimeoutMs)
				.replace(SubscriptionConstant.KAFKA_SESSION_TIMEOUT_MS_URL_KEY, sessionTimeoutMs)
				.replace(SubscriptionConstant.KAFKA_CONSUMER_REQUEST_TIMEOUT_MS_URL_KEY, consumerRequestTimeoutMs)
				.replace(SubscriptionConstant.KAFKA_FETCH_WAIT_MAX_MS_URL_KEY, fetchWaitMaxMs);

		log.debug("constructed kafka uri : " + newEndpointUri);
		return newEndpointUri;
	}

	/**
	 * utility that constructs the quartz uri.
	 * 
	 * 
	 * @param props
	 * @return uri
	 */
	public static String constructQuartzURI(Properties props) {

		String cronExpression = configAssignation(props, SubscriptionConstant.CRON_EXPRESSION_CONFIG_KEY,
				SubscriptionConstant.DEFAULT_CRON_VALUE);
		cronExpression = cronExpression.trim().replaceAll(" ", "+");
		String newCronExp = SubscriptionConstant.QUARTZ_ENDPOINT_URI
				.replace(SubscriptionConstant.CRON_EXPRESSION_URL_KEY, cronExpression);
		log.debug("constructed Quartz uri : " + newCronExp);
		return newCronExp;
	}

	/**
	 * utility that constructs the seda uri.
	 * 
	 * 
	 * @param props
	 * @return uri
	 */
	public static String constructSedaURIForRetry(Properties props) {
		String sedaConcurrentConsumersCount = configAssignation(props,
				SubscriptionConstant.SEDA_CONCURRENT_CONSUMERS_FOR_RERTY_KEY,
				SubscriptionConstant.DEFAULT_RETRY_CONCURRENT_CONSUMERS_COUNT);
		String newSedaExp = SubscriptionConstant.SEDA_ENDPOINT_URI.replace(
				SubscriptionConstant.SEDA_CONCURRENT_CONSUMERS_FOR_RERTY_URL_KEY, sedaConcurrentConsumersCount);
		log.debug("constructed Seda uri For Retry : " + newSedaExp);
		return newSedaExp;
	}

	/**
	 * utility that constructs the seda uri for each subscriber.
	 * 
	 * 
	 * @param props
	 * @param subscriptionId
	 * @return uri
	 */
	public static String constructSedaURIToProcessMessage(Properties props, String subscriptionId) {
		String sedaConcurrentConsumersCount = configAssignation(props,
				SubscriptionConstant.SEDA_CONCURRENT_CONSUMERS_KEY,
				SubscriptionConstant.DEFAULT_CONCURRENT_CONSUMERS_COUNT);
		String newSedaExp = SubscriptionConstant.PARALLEL_PROCESS_ROUTE_URI
				.replace(SubscriptionConstant.SEDA_SUBSCRIBER_URL_KEY, subscriptionId)
				.replace(SubscriptionConstant.SEDA_CONCURRENT_CONSUMERS_URL_KEY, sedaConcurrentConsumersCount);
		log.debug("constructed Seda uri  for subscriber : " + subscriptionId + " is ==> " + newSedaExp);
		return newSedaExp;
	}

	/**
	 * assigning the default values for configuration if not alredy assigned.
	 * 
	 * @param props
	 * @param kafkaConfigKey
	 * @param defaultPropertyValue
	 * @return
	 */
	private static String kafkaConfigAssignation(Properties props, String kafkaConfigKey, String defaultPropertyValue) {
		String configuration = props.getProperty(kafkaConfigKey);
		if (configuration == null)
			configuration = defaultPropertyValue;
		return configuration;
	}

	/**
	 * assigning the default values for configuration if not alredy assigned.
	 * 
	 * @param props
	 * @param quartzConfigKey
	 * @param defaultPropertyValue
	 * @return
	 */
	private static String configAssignation(Properties props, String quartzConfigKey, String defaultPropertyValue) {
		String configuration = props.getProperty(quartzConfigKey);
		if (configuration == null)
			configuration = defaultPropertyValue;
		return configuration;
	}

	/**
	 * adding header @param in post request.
	 * 
	 * @param request
	 * @param headerParams
	 */
	public static void addHeaderParamsInHeader(HttpPost request, HeaderParams headerParams) {
		if (headerParams != null) {
			List<HeaderParam> listHeaderParams = headerParams.getHeaderParam();
			for (HeaderParam headerParam : listHeaderParams) {
				request.setHeader(headerParam.getParamName(), headerParam.getParamValue());
			}

		}
	}

	/**
	 * building the object of configuration based on the input parameters
	 *
	 * 
	 * @param tenant
	 * @param site
	 * @param subscriberId
	 *            as fGroup-fName-impl-vendor-version-subscriptionId
	 * @return configurationContext
	 */
	public ConfigurationContext buildConfigContext(String tenantId, String siteId, String subscriberId) {
		ConfigurationContext conFigCtx = new ConfigurationContext();
		if (!attributeEmptyCheck(tenantId) && !attributeEmptyCheck(siteId) && !attributeEmptyCheck(subscriberId)) {
			conFigCtx.setTenantId(tenantId);
			conFigCtx.setSiteId(siteId);
			List<String> configParams = Arrays
					.asList(subscriberId.split(EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER));
			if (configParams.size() - 1 == 5) {
				conFigCtx.setFeatureGroup(configParams.get(0));
				conFigCtx.setFeatureName(configParams.get(1));
				conFigCtx.setImplementationName(configParams.get(2));
				conFigCtx.setVendorName(configParams.get(3));
				conFigCtx.setVersion(configParams.get(4));
			} else
				return null;

		}
		// TODO Auto-generated method stub
		return conFigCtx;
	}

	/**
	 * generate subscriberid based on the input parameters
	 *
	 * 
	 *
	 * @param fGroup-fName-impl-vendor-version-subscriptionId
	 * @return subscriberId
	 */
	public String getActualSubscriberId(String subscriberId) {
		if (!attributeEmptyCheck(subscriberId)) {
			List<String> configParams = Arrays
					.asList(subscriberId.split(EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER));
			if (configParams.size() == 6)
				return configParams.get(5);
			else
				return "empty";
		}
		return "empty";
	}

	/**
	 * getting the exceeded retry attempts from configuration.
	 * 
	 * @param abstractRetryStrategyBean
	 * @param eventSubscriptionTracker
	 * @return maxRetryAttempts
	 */
	private Boolean hasMaxRetryAttemptExceeded(AbstractSubscriptionRetryStrategy abstractRetryStrategyBean,
			EventSubscriptionTracker eventSubscriptionTracker) {
		org.json.simple.JSONObject retryConfiguration = abstractRetryStrategyBean.getRetryConfiguration();
		if (retryConfiguration.containsKey(SubscriptionConstant.RETRY_COUNT)) {
			try {
				Object retryCount = retryConfiguration.get(SubscriptionConstant.RETRY_COUNT);
				if (retryCount instanceof Integer)
					return ((Integer) retryCount).intValue() > eventSubscriptionTracker.getRetryCount();
				if (retryCount instanceof String)
					return (Integer.parseInt((String) retryCount)) > eventSubscriptionTracker.getRetryCount();

			} catch (Exception e1) {
				log.error("failed to set rertycount " + e1.getLocalizedMessage());
			}
		}
		return true;
	}

	/**
	 * gets retry Interval from config.
	 * 
	 * @param abstractRetryStrategyBean
	 * @return retryInterval
	 */
	private Long getRetryInterval(AbstractSubscriptionRetryStrategy abstractRetryStrategyBean) {
		org.json.simple.JSONObject retryConfiguration = abstractRetryStrategyBean.getRetryConfiguration();
		if (retryConfiguration.containsKey(SubscriptionConstant.RETRY_INTERVAL)) {
			try {
				Object retryInterval = retryConfiguration.get(SubscriptionConstant.RETRY_INTERVAL);
				if (retryInterval instanceof Integer)
					return ((Integer) retryInterval).longValue();
				if (retryInterval instanceof String)
					return Long.parseLong((String) retryInterval);
				if (retryInterval instanceof Long)
					return (Long) retryInterval;

			} catch (Exception e1) {
				log.error("failed to get retryInterval from retry Config " + e1.getMessage());
			}
		}
		return SubscriptionConstant.DEFAULT_RETRY_INTERVAL;
	}

	/**
	 * gets the retryTimeUnit from config.
	 * 
	 * @param abstractRetryStrategyBean
	 * @return retryTimeUnit
	 */
	private String getRetryTimeUnit(AbstractSubscriptionRetryStrategy abstractRetryStrategyBean) {
		org.json.simple.JSONObject retryConfiguration = abstractRetryStrategyBean.getRetryConfiguration();
		if (retryConfiguration.containsKey(SubscriptionConstant.INTERVAL_TIMEUNIT)) {
			try {
				return (String) retryConfiguration.get(SubscriptionConstant.INTERVAL_TIMEUNIT);
			} catch (Exception e1) {
				log.error("failed to get retryInterval from retry Config " + e1.getMessage());
			}
		}
		return SubscriptionConstant.DEFAULT_INTERVAL_TIMEUNIT;
	}

	/**
	 * combining all failed subscription list and adding failed list in order
	 * for execution.
	 * 
	 * @param newLongTimeSubscription
	 * @param failedSubscription
	 * @param retryFailedSubscription
	 * @param inProgressSubscription
	 * @param retryInProgressSubscription
	 * @return
	 */
	public static List<EventSubscriptionTracker> mergeAllFailedEventList(
			List<EventSubscriptionTracker> newLongTimeSubscription, List<EventSubscriptionTracker> failedSubscription,
			List<EventSubscriptionTracker> retryFailedSubscription,
			List<EventSubscriptionTracker> inProgressSubscription,
			List<EventSubscriptionTracker> retryInProgressSubscription) {
		List<EventSubscriptionTracker> finalRetryEventList = new ArrayList<EventSubscriptionTracker>();

		// adding failed subscriptions in the linked-set will define the
		// priority of
		// execution.
		Set<List<EventSubscriptionTracker>> failedLists = new LinkedHashSet<>();
		failedLists.add(newLongTimeSubscription);
		failedLists.add(inProgressSubscription);
		failedLists.add(failedSubscription);
		failedLists.add(retryFailedSubscription);
		failedLists.add(retryInProgressSubscription);

		for (List<EventSubscriptionTracker> eventList : failedLists) {
			if (eventList != null)
				finalRetryEventList.addAll(eventList);

		}
		return finalRetryEventList;
	}

	/**
	 * returning the instance of Date but instance will contain specified
	 * interval before the current Date instance of system.
	 * 
	 * @param timeIntervalBefore
	 * @return date instance
	 */
	public static Date getPreviousDateInstance(org.json.simple.JSONObject retryConfigurationJSON) {
		int timeIntervalBefore = SubscriptionRetryPolicy.getRetryInterval(retryConfigurationJSON);

		long currentTime = System.currentTimeMillis();
		long specifiedMinutesBeforeTime = 0;
		switch (SubscriptionRetryPolicy.getTimeIntervalUnit(retryConfigurationJSON).toUpperCase()) {
		case SubscriptionRetryPolicy.TIMEUNIT_HOURS:
			specifiedMinutesBeforeTime = currentTime - (timeIntervalBefore * 3600) * 1000 + 0;
			break;
		case SubscriptionRetryPolicy.TIMEUNIT_MINUTES:
			specifiedMinutesBeforeTime = currentTime - (timeIntervalBefore * 60) * 1000 + 0;
			break;
		case SubscriptionRetryPolicy.TIMEUNIT_SECONDS:
			specifiedMinutesBeforeTime = currentTime - timeIntervalBefore * 1000 + 0;
			break;
		case SubscriptionRetryPolicy.TIMEUNIT_MILLSECONDS:
			specifiedMinutesBeforeTime = currentTime - timeIntervalBefore;
			break;
		default:
			// default will be considered in minutes.
			specifiedMinutesBeforeTime = currentTime - (0 + timeIntervalBefore * 60 + 0) * 1000 + 0;
			break;
		}

		// default 2 min + extratime computed for all
		specifiedMinutesBeforeTime = specifiedMinutesBeforeTime - (0 + 2 * 60 + 0) * 1000 + 0;
		Date calculatedMinutesBeforeDate = new Date(specifiedMinutesBeforeTime);
		return calculatedMinutesBeforeDate;
	}

	/**
	 * This method is used to get the retryStrategy Instance form the the cache.
	 * 
	 * @param exchange
	 * @param leapHeader
	 * @return cachedInstance
	 * @throws MissingConfigurationException
	 * @throws ConfigurationValidationFailedException
	 */
	public static AbstractSubscriptionRetryStrategy getCachedStrategyInstance(Exchange exchange, LeapHeader leapHeader)
			throws MissingConfigurationException, ConfigurationValidationFailedException {
		// getSubscription Event configuration from header.
		SubscribeEvent eventSubscription = exchange.getIn().getHeader(SubscriptionConstant.SUBSCIBER_EVENT_CONFIG_KEY,
				SubscribeEvent.class);
		String subscriberId = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, String.class);

		String strategy = "";
		String strategyConfig = "{}";

		if (eventSubscription != null) {
			FailureHandlingStrategy failureHandlingStrategy = eventSubscription.getFailureHandlingStrategy();
			if (failureHandlingStrategy != null && failureHandlingStrategy.getFailureStrategyName() != null) {
				strategy = failureHandlingStrategy.getFailureStrategyName().getValue();
				if (strategy == null || strategy.trim().isEmpty())
					strategy = failureHandlingStrategy.getFailureStrategyName().getHandlerQualifiedClass();
				strategyConfig = failureHandlingStrategy.getFailureStrategyConfig();

			} else
				strategy = SubscriptionConstant.LEAP_NO_RETRY_STRATEGY_CLASS;
		} else
			strategy = SubscriptionConstant.LEAP_NO_RETRY_STRATEGY_CLASS;

		if (strategyConfig == null || strategyConfig.trim().isEmpty())
			strategyConfig = "{}";
		String tenant = leapHeader.getTenant();
		String site = leapHeader.getSite();

		// setting the strategy in header for executing onSuccess and
		// onFailure
		String tenantSiteKey = tenant + SubscriptionConstant.TENANT_SITE_SEPERATOR + site;

		return InstantiateSubscriptionRetryStrategy.loadAndGetStrategyImplementation(strategy.trim(), subscriberId,
				strategyConfig, tenantSiteKey);
	}

	/**
	 * validating weather processing status belongs the status registered.
	 * 
	 * @param processingStatus
	 * @return validationStatus
	 */
	public static boolean validateInProcessProcessingStatus(String processingStatus) {
		List<String> processingStatusList = new ArrayList<>();
		processingStatusList.add(EventSubscriptionTrackerConstants.STATUS_IN_PROCESS);
		processingStatusList.add(EventSubscriptionTrackerConstants.STATUS_RETRY_IN_PROCESS);
		for (String status : processingStatusList) {
			if (status.equalsIgnoreCase(processingStatus))
				return true;
		}
		return false;
	}

	/**
	 * used to get the response in json format.
	 * 
	 * @param body
	 * @return
	 * @throws MissingConfigurationException
	 */
	public JSONObject identifyContentType(String body) throws MissingConfigurationException {
		JSONObject eventBody = null;
		if (body.trim().startsWith("<") && body.trim().endsWith(">")) {
			try {
				eventBody = XML.toJSONObject(body).getJSONObject(EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
			} catch (Exception e) {
				log.error("Unable to convert the XML to JSON incoming body : " + body);
			}
		} else {
			try {
				eventBody = new JSONObject(body);
			} catch (Exception e) {
				log.error("Unable to parse the json  incoming body : " + body);
			}
		}
		return eventBody;

	}

}
