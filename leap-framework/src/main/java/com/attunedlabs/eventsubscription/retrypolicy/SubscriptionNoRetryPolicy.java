package com.attunedlabs.eventsubscription.retrypolicy;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SubscriptionNoRetryPolicy uses the defaultValues if not provided in the
 * subscriber configuration.
 * 
 * @author Reactiveworks42
 *
 */
@SuppressWarnings("unchecked")
public class SubscriptionNoRetryPolicy {
	final static Logger logger = LoggerFactory.getLogger(SubscriptionNoRetryPolicy.class);
	public static final String MSG_LOGGING_ENABLED_KEY = "MSGLoggingEnabled";
	public static final String PARALLEL_PROCESSING_KEY = "ParallelProcessing";

	/**
	 * Just the flag stating whether to enable or disable the Message Logging
	 * feature for the Event's consumed from kafka topic.
	 */
	protected static boolean defaultMsgLogEnabled = true;

	/**
	 * #TODO Implementation for this configuration will change not to be handled
	 * on the retry side. (Currently used as the configuration will decide the
	 * async call after msg consumed from topic by particular subscriber.)
	 */
	protected static boolean defaultParallelProcessingEnabled = false;

	public static boolean assertMessageLogEnabled(JSONObject retryConfigurationJSON) {
		if (getPropertyFor(MSG_LOGGING_ENABLED_KEY, retryConfigurationJSON) != null)
			return getPropertyFor(MSG_LOGGING_ENABLED_KEY, retryConfigurationJSON);
		retryConfigurationJSON.put(MSG_LOGGING_ENABLED_KEY, true);
		return defaultMsgLogEnabled;
	}

	public static boolean assertParallelProcessingEnabled(JSONObject retryConfigurationJSON) {
		if (getPropertyFor(PARALLEL_PROCESSING_KEY, retryConfigurationJSON) != null)
			return getPropertyFor(PARALLEL_PROCESSING_KEY, retryConfigurationJSON);
		retryConfigurationJSON.put(PARALLEL_PROCESSING_KEY, true);
		return defaultMsgLogEnabled;
	}

	/**
	 * get you the value for particular property orElse null will be returned if
	 * property mentioned dosen't match.
	 * 
	 * @param key
	 *            : propertyName for retry policy.
	 * @param retryConfigurationJSON
	 * @return value for property.
	 */
	private static Boolean getPropertyFor(String key, JSONObject retryConfigurationJSON) {
		try {
			Object value = 0;

			if (retryConfigurationJSON != null)
				if (retryConfigurationJSON.get(key) != null) {
					value = retryConfigurationJSON.get(key);
					if (value instanceof Boolean)
						return ((Boolean) value);
					if (value instanceof String) {
						String parseValue = (String) value;
						if (!parseValue.trim().isEmpty())
							return Boolean.parseBoolean(parseValue.toLowerCase().trim());
						else
							return null;
					}
				} else
					logger.warn("No such key is present: " + key + " for retry policy...!");
		} catch (Exception e) {
			logger.warn(" to parse value for key: " + key + " should be Integer..!");
		}
		return null;
	}

}
