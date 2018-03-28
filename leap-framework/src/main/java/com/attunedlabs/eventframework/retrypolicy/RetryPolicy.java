package com.attunedlabs.eventframework.retrypolicy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.eventtracker.impl.EventTrackerTableConstants;

public class RetryPolicy {
	final static Logger logger = LoggerFactory.getLogger(RetryPolicy.class);
	public static final String TIMEUNIT_MINUTES = "MINUTES";
	public static final String TIMEUNIT_HOURS = "HOURS";
	public static final String TIMEUNIT_SECONDS = "SECONDS";
	public static final String TIMEUNIT_MILLSECONDS = "MILLISECONDS";
	public static final String FAILED_RETRY_INTERVAL_KEY = "failedRetryInterval";
	public static final String FAILED_RETRY_INTERVAL_MULTIPLIER_KEY = "failedRetryIntervalMultiplier";
	public static final String FAILED_MAXIMUM_RETRY_COUNT_KEY = "failedMaximumRetryCount";
	public static final String FAILED_MAXIMUM_RETRY_INTERVAL_KEY = "failedMaximumRetryInterval";
	public static final String FAILED_TIME_INTERVAL_UNIT_KEY = "failedTimeIntervalUnit";
	public static final String NORMAL_RETRY_INTERVAL_KEY = "normalRetryInterval";
	public static final String NORMAL_RETRY_COUNT_KEY = "normalRetryCount";
	public static final String NORMAL_TIME_INTERVAL_UNIT_KEY = "normalTimeIntervalUnit";
	public static final String TOP_MAX_RETRY_RECORDS_COUNT_KEY = "retryTopRecords";

	private static Properties policyProperties;

	/**
	 * Initial value of failedRetryInterval delay is 1sec should be minimum
	 * 1.</br>
	 * first retry will be done at this interval if property failedRetryInterval
	 * is not specified.</br>
	 * <b>Applicable for processingStatus : FAILED, RETRY_FAILED only.</b>
	 */
	protected static int defaultFailedRetryInterval = 1;

	/**
	 * retry failedRetryIntervalMultiplier will increase the interval gap by the
	 * number failedRetryIntervalMultiplier is specified.</br>
	 * (default failedRetryIntervalMultiplier = 2 e.g:- 1000 * 2 *
	 * failedRetryInterval= 2000 next time double of the result)</br>
	 * <b>Applicable for processingStatus : FAILED, RETRY_FAILED only.</b>
	 */
	protected static int defaultFailedRetryIntervalMultiplier = 2;

	/**
	 * Setting the failedMaximumRetryCount to a negative value such as -1 will
	 * then always retry (unlimited). Setting the maximumRetries to 0 will not
	 * retry at all.</br>
	 * <b>Applicable for processingStatus : FAILED, RETRY_FAILED only.</b>
	 * disable retry.
	 */
	protected static int defaultFailedMaximumRetryCount = -1;

	/**
	 * maximum retryInterval even after computing with multiplier will not
	 * exceed failedMaximumRetryInterval (default failedMaximumRetryInterval =
	 * 1min) this period will be mostly used when not specified.</br>
	 * <b>Applicable for processingStatus : FAILED, RETRY_FAILED only.</b>
	 */
	protected static int defaultFailedMaximumRetryInterval = 60;

	/**
	 * time unit will be considered for failed retry list all the computations
	 * will be done based on the timeUnit.</br>
	 * <b>Applicable for processingStatus : FAILED, RETRY_FAILED only.</b>
	 */
	protected static String defaultFailedTimeIntervalUnit = TIMEUNIT_SECONDS;

	/**
	 * normalRetryInterval will be <b>applicable for processingStatus : NEW,
	 * PROCESS, RETRY_INPROCESS only</b> and will be retried after specific
	 * interval. by default it will retry every 10 min.
	 *
	 */
	protected static int defaultNormalRetryInterval = 10;

	/**
	 * Setting the normalRetryCount to a negative value such as -1 will then
	 * always retry (unlimited). Setting the maximumRetries to 0 will not retry
	 * at all.</br>
	 * <b>Applicable for processingStatus : NEW, PROCESS, RETRY_INPROCESS
	 * only.</b>
	 */
	protected static int defaultNormalRetryCount = -1;

	/**
	 * time unit will be considered for failed retry list all the computations
	 * will be done based on the timeUnit.</br>
	 * <b>Applicable for processingStatus : NEW, PROCESS, RETRY_INPROCESS
	 * only.</b>
	 */
	protected static String defaultNormalTimeIntervalUnit = TIMEUNIT_MINUTES;

	/**
	 * default value is 15 which means records with particular status will be
	 * retried until the max records are not mentioned.
	 */
	protected static int defaultRetryRecordsCount = 15;

	static {
		policyProperties = new Properties();
		InputStream input = RetryPolicy.class.getClassLoader()
				.getResourceAsStream(EventTrackerTableConstants.EVENT_RETRY_POLICY_FILE);
		try {
			policyProperties.load(input);
		} catch (IOException e) {
			logger.warn("unable to load property file = " + EventTrackerTableConstants.EVENT_RETRY_POLICY_FILE
					+ " will use all the default properties...!");
		}
	}

	/**
	 * @return the policyProperties
	 */
	public static Properties getPolicyProperties() {
		return policyProperties;
	}

	/**
	 * @return the failedRetryInterval
	 */
	public static int getFailedRetryInterval() {
		if (getPropertyFor(FAILED_RETRY_INTERVAL_KEY) != null)
			return getPropertyFor(FAILED_RETRY_INTERVAL_KEY);
		return defaultFailedRetryInterval;
	}

	/**
	 * @return the failedRetryIntervalMultiplier
	 */
	public static int getFailedRetryIntervalMultiplier() {
		if (getPropertyFor(FAILED_RETRY_INTERVAL_MULTIPLIER_KEY) != null)
			return getPropertyFor(FAILED_RETRY_INTERVAL_MULTIPLIER_KEY);
		return defaultFailedRetryIntervalMultiplier;
	}

	/**
	 * @return the failedMaximumRetryCount
	 */
	public static int getFailedMaximumRetryCount() {
		if (getPropertyFor(FAILED_MAXIMUM_RETRY_COUNT_KEY) != null)
			return getPropertyFor(FAILED_MAXIMUM_RETRY_COUNT_KEY);
		return defaultFailedMaximumRetryCount;
	}

	/**
	 * @return the failedMaximumRetryDelay
	 */
	public static int getFailedMaximumRetryInterval() {
		if (getPropertyFor(FAILED_MAXIMUM_RETRY_INTERVAL_KEY) != null)
			return getPropertyFor(FAILED_MAXIMUM_RETRY_INTERVAL_KEY);
		return defaultFailedMaximumRetryInterval;
	}

	/**
	 * @return the failedTimeIntervalUnit
	 */
	public static String getFailedTimeIntervalUnit() {
		if (getPolicyProperties() != null)
			if (getPolicyProperties().getProperty(FAILED_TIME_INTERVAL_UNIT_KEY) != null)
				return getPolicyProperties().getProperty(FAILED_TIME_INTERVAL_UNIT_KEY).toUpperCase().trim();
		return defaultFailedTimeIntervalUnit;
	}

	/**
	 * @return the normalRetryInterval
	 */
	public static int getNormalRetryInterval() {
		if (getPropertyFor(NORMAL_RETRY_INTERVAL_KEY) != null)
			return getPropertyFor(NORMAL_RETRY_INTERVAL_KEY);
		return defaultNormalRetryInterval;
	}

	/**
	 * @return the normalRetryCount
	 */
	public static int getNormalRetryCount() {
		if (getPropertyFor(NORMAL_RETRY_COUNT_KEY) != null)
			return getPropertyFor(NORMAL_RETRY_COUNT_KEY);
		return defaultNormalRetryCount;
	}

	/**
	 * @return the timeIntervalUnit
	 */
	public static String getNormalTimeIntervalUnit() {
		if (getPolicyProperties() != null)
			if (getPolicyProperties().getProperty(NORMAL_TIME_INTERVAL_UNIT_KEY) != null)
				return getPolicyProperties().getProperty(NORMAL_TIME_INTERVAL_UNIT_KEY).toUpperCase().trim();
		return defaultNormalTimeIntervalUnit;
	}

	/**
	 * @return the defaultFailedMaximumRetryCount
	 */
	public static int getMaxRetryRecordsCount() {
		if (getPropertyFor(TOP_MAX_RETRY_RECORDS_COUNT_KEY) != null)
			if (getPropertyFor(TOP_MAX_RETRY_RECORDS_COUNT_KEY) > 0)
				return getPropertyFor(TOP_MAX_RETRY_RECORDS_COUNT_KEY);
		return defaultRetryRecordsCount;
	}

	/**
	 * get you the value for particular property orElse null will be returned if
	 * property mentioned dosen't match.
	 * 
	 * @param key
	 *            propertyName for retry policy.
	 * @return value for property.
	 */
	private static Integer getPropertyFor(String key) {
		try {
			if (getPolicyProperties() != null)
				if (getPolicyProperties().getProperty(key) != null)
					return Integer.parseInt(getPolicyProperties().getProperty(key).trim());
				else
					logger.warn("No such key is present: " + key + " for retry policy...!");
		} catch (Exception e) {
			logger.warn("failed to parse value for key: " + key + " should be Integer..!");
		}
		return null;
	}

}
