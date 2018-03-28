package com.attunedlabs.eventsubscription.retrypolicy;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SubscriptionRetryPolicy uses the defaultValues if not provided in the
 * subscriber configuration.
 * 
 * @author Reactiveworks42
 *
 */
@SuppressWarnings("unchecked")
public class SubscriptionRetryPolicy {
	final static Logger logger = LoggerFactory.getLogger(SubscriptionRetryPolicy.class);
	public static final String TIMEUNIT_MINUTES = "MINUTES";
	public static final String TIMEUNIT_HOURS = "HOURS";
	public static final String TIMEUNIT_SECONDS = "SECONDS";
	public static final String TIMEUNIT_MILLSECONDS = "MILLISECONDS";
	public static final String RETRY_INTERVAL_MULTIPLIER_KEY = "retryIntervalMultiplier";
	public static final String RETRY_INTERVAL_KEY = "retryInterval";
	public static final String MAXIMUM_RETRY_INTERVAL_KEY = "maximumRetryInterval";
	public static final String RETRY_COUNT_KEY = "retryCount";
	public static final String TOP_MAX_RETRY_RECORDS_COUNT_KEY = "retryTopRecords";
	public static final String TIME_INTERVAL_UNIT_KEY = "timeIntervalUnit";

	/**
	 * retry RetryIntervalMultiplier will increase the interval gap by the
	 * number RetryIntervalMultiplier is specified.</br>
	 * (default RetryIntervalMultiplier = 2 e.g:- 1000 * 2 * RetryInterval= 2000
	 * next time double of the result)</br>
	 * <b>Applicable for processingStatus : NEW, PROCESS, RETRY_INPROCESS, ,
	 * RETRY_, only.</b>
	 */
	protected static int defaultRetryIntervalMultiplier = 2;

	/**
	 * Initial value of RetryInterval delay is 1min should be minimum 1.</br>
	 * first retry will be done at this interval if property RetryInterval is
	 * not specified.</br>
	 * <b>Applicable for processingStatus : NEW, PROCESS, RETRY_INPROCESS, ,
	 * RETRY_, only.</b>
	 */
	protected static int defaultRetryInterval = 1;

	/**
	 * maximum retryInterval even after computing with multiplier will not
	 * exceed MaximumRetryInterval (default MaximumRetryInterval = 1min) this
	 * period will be mostly used when not specified.</br>
	 * <b>Applicable for processingStatus : NEW, PROCESS, RETRY_INPROCESS, ,
	 * RETRY_, only.</b>
	 */
	protected static int defaultMaximumRetryInterval = 60;

	/**
	 * time unit will be considered for retry list all the computations will be
	 * done based on the timeUnit.</br>
	 * <b>Applicable for processingStatus : NEW, PROCESS, RETRY_INPROCESS, ,
	 * RETRY_, only.</b>
	 */
	protected static String defaultTimeIntervalUnit = TIMEUNIT_MINUTES;

	/**
	 * Setting the RetryCount to a negative value such as -1 will then always
	 * retry (unlimited). Setting the maximumRetries to 0 will not retry at
	 * all.</br>
	 * <b>Applicable for processingStatus : NEW, PROCESS, RETRY_INPROCESS, ,
	 * RETRY_, only.</b>
	 */
	protected static int defaultRetryCount = -1;

	/**
	 * default value is 15 which means records with particular status will be
	 * retried until the max records are not mentioned.
	 */
	protected static int defaultRetryRecordsCount = 15;

	/**
	 * @return the retryIntervalMultiplier
	 */
	public static int getRetryIntervalMultiplier(JSONObject retryConfigurationJSON) {
		if (getPropertyFor(RETRY_INTERVAL_MULTIPLIER_KEY, retryConfigurationJSON) != null)
			return getPropertyFor(RETRY_INTERVAL_MULTIPLIER_KEY, retryConfigurationJSON);
		retryConfigurationJSON.put(RETRY_INTERVAL_MULTIPLIER_KEY, defaultRetryIntervalMultiplier);
		return defaultRetryIntervalMultiplier;
	}

	/**
	 * @return the retryInterval
	 */
	public static int getRetryInterval(JSONObject retryConfigurationJSON) {
		if (getPropertyFor(RETRY_INTERVAL_KEY, retryConfigurationJSON) != null)
			return getPropertyFor(RETRY_INTERVAL_KEY, retryConfigurationJSON);
		retryConfigurationJSON.put(RETRY_INTERVAL_KEY, defaultRetryInterval);
		return defaultRetryInterval;
	}

	/**
	 * @return the maximumRetryDelay
	 */
	public static int getMaximumRetryInterval(JSONObject retryConfigurationJSON) {
		if (getPropertyFor(MAXIMUM_RETRY_INTERVAL_KEY, retryConfigurationJSON) != null)
			return getPropertyFor(MAXIMUM_RETRY_INTERVAL_KEY, retryConfigurationJSON);
		retryConfigurationJSON.put(MAXIMUM_RETRY_INTERVAL_KEY, defaultMaximumRetryInterval);
		return defaultMaximumRetryInterval;
	}

	/**
	 * @return the timeIntervalUnit
	 */
	public static String getTimeIntervalUnit(JSONObject retryConfigurationJSON) {
		if (retryConfigurationJSON != null)
			if (retryConfigurationJSON.get(TIME_INTERVAL_UNIT_KEY) != null)
				return retryConfigurationJSON.get(TIME_INTERVAL_UNIT_KEY).toString().toUpperCase().trim();
		retryConfigurationJSON.put(TIME_INTERVAL_UNIT_KEY, defaultTimeIntervalUnit);
		return defaultTimeIntervalUnit;
	}

	/**
	 * @return the retryCount
	 */
	public static int getRetryCount(JSONObject retryConfigurationJSON) {
		if (getPropertyFor(RETRY_COUNT_KEY, retryConfigurationJSON) != null)
			return getPropertyFor(RETRY_COUNT_KEY, retryConfigurationJSON);
		retryConfigurationJSON.put(RETRY_COUNT_KEY, defaultRetryCount);
		return defaultRetryCount;
	}

	/**
	 * @return the maximumRetryCount
	 */
	public static int getMaxRetryRecordsCount(JSONObject retryConfigurationJSON) {
		if (getPropertyFor(TOP_MAX_RETRY_RECORDS_COUNT_KEY, retryConfigurationJSON) != null)
			if (getPropertyFor(TOP_MAX_RETRY_RECORDS_COUNT_KEY, retryConfigurationJSON) > 0)
				return getPropertyFor(TOP_MAX_RETRY_RECORDS_COUNT_KEY, retryConfigurationJSON);
		retryConfigurationJSON.put(TOP_MAX_RETRY_RECORDS_COUNT_KEY, defaultRetryRecordsCount);
		return defaultRetryRecordsCount;
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
	private static Integer getPropertyFor(String key, JSONObject retryConfigurationJSON) {
		try {
			Object value = 0;

			if (retryConfigurationJSON != null)
				if (retryConfigurationJSON.get(key) != null) {
					value = retryConfigurationJSON.get(key);
					if (value instanceof Integer)
						return ((Integer) value);
					if (value instanceof Long)
						return ((Long) value).intValue();
					if (value instanceof String) {
						String parseValue = (String) value;
						if (!parseValue.trim().isEmpty())
							return (Integer.parseInt(parseValue.trim()));
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
