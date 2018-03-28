package com.attunedlabs.eventsubscription.util;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventSubscriptionTrackerConstants {
	final static Logger log = LoggerFactory.getLogger(EventSubscriptionTrackerConstants.class);

	// EventSubscriptionSucessLog & EventSubscriptionFailureLog
	public static final String EVENT_SUBSCRIBER_TRACKER_TABLE = "EventSubscriptionTracker";
	public static final String TENANT_ID_COL = "tenantId";
	public static final String SITE_ID_COL = "siteId";
	public static final String SUBSCRIPTION_ID_COL = "subscriptionId";
	public static final String TOPIC_COL = "topic";
	public static final String PARTITION_COL = "topicPartition";
	public static final String OFFEST_COL = "topicOffset";
	public static final String EVENT_FETCHED_DTM_COL = "eventFetchedDTM";
	public static final String PROCESSED_DATA_COL = "processedData";
	public static final String TRACK_STATUS = "status";
	public static final String ACTION_PERFORMED = "actionPerformed";
	public static final String EVENT_DATA_COL = "eventData";
	public static final String FAILURE_MSG_COL = "failureMsg";
	public static final String LAST_FAILURE_DTM_COL = "lastfailureDTM";
	public static final String IS_RETRYABLE_COL = "isRetryable";
	public static final String RETRY_COUNT_COL = "retryCount";

	public static final String RETRY_INTERVAL = "retryInterval";

	// processing status
	public static final String MANUAL_COMMIT_STATUS = "MANUALLY_COMMITED";
	public static final String UNCOMMITED_COMMIT_STATUS = "UN_COMMITED";
	public static final String AUTO_COMMIT_STATUS = "AUTO_COMMITED";

	// processing status
	public static final String STATUS_NEW = "NEW";
	public static final String STATUS_IN_PROCESS = "IN_PROCESS";
	public static final String STATUS_FAILED = "FAILED";
	public static final String STATUS_RETRY_IN_PROCESS = "RETRY_INPROCESS";
	public static final String STATUS_RETRY_FAILED = "RETRY_FAILED";
	public static final String STATUS_COMPLETE = "SUCCESS";

	// data source bean ref name from beans.xml
	public static final String Leap_DATASOURCE = "leapDataSource";
	public static final String Leap_SIMPLE_DATASOURCE = "leapXASource";

	// retry count storage file name
	public static final Integer RETRY_COUNT = getRetryCountForFailedSubscription();
	public static final Integer DEFAULT_RETRY_COUNT = 3;

	private static Integer getRetryCountForFailedSubscription() {
		Properties props = new Properties();

		InputStream inputStream;
		try {
			inputStream = EventSubscriptionTrackerConstants.class.getClassLoader()
					.getResourceAsStream(SubscriptionConstant.KAFKA_CONSUMER_CONFIGS);
			props.load(inputStream);
			String retryCount = props.getProperty(RETRY_COUNT_COL);
			if (retryCount != null)
				return Integer.parseInt(retryCount);
		} catch (Exception e) {
			log.error("failed to load retry count from consumer properties will use default value for retry count..."
					+ SubscriptionConstant.KAFKA_CONSUMER_CONFIGS);
		}
		return DEFAULT_RETRY_COUNT;

	}

}
