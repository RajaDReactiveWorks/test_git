package com.attunedlabs.eventsubscription.util;

import com.attunedlabs.eventsubscription.exception.MissingConfigurationException;
import com.attunedlabs.eventsubscription.exception.RouteInvocationException;
import com.attunedlabs.eventsubscription.exception.ServiceCallInvocationException;

public class SubscriptionConstant {
	public final static String SUBSCRIPTION_QUARTZ_CONFIGS = "globalAppDeploymentConfig.properties";
	public final static String KAFKA_CONSUMER_CONFIGS = "globalAppDeploymentConfig.properties";

	public final static String CRON_EXPRESSION_URL_KEY = "$cron$";
	public final static String CRON_EXPRESSION_CONFIG_KEY = "cron";

	public final static String KAFKA_BROKER_HOST_PORT_URL_KEY = "$kafkaBrokerHostPort$";
	public final static String KAFKA_TOPIC_NAME_URL_KEY = "$topicName$";
	public final static String KAFKA_GROUP_ID_URL_KEY = "$groupId$";
	public final static String KAFKA_CLIENT_ID_URL_KEY = "$clientId$";
	public final static String KAFKA_AUTO_OFFSET_RESET_URL_KEY = "$autoOffsetReset$";
	public final static String KAFKA_AUTO_COMMIT_ENABLE_URL_KEY = "$autoCommitEnable$";
	public final static String KAFKA_BREAK_ON_FIRST_ERROR_URL_KEY = "$breakOnFirstError$";
	public final static String KAFKA_CONSUMER_COUNT_URL_KEY = "$consumerCount$";
	public final static String KAFKA_AUTO_COMMIT_INTERVAL_MS_URL_KEY = "$autoCommitIntervalMs$";
	public final static String KAFKA_AUTO_COMMIT_ON_STOP_URL_KEY = "$autoCommitOnStop$";
	public final static String KAFKA_MAX_POLL_RECORDS_URL_KEY = "$maxPollRecords$";
	public final static String KAFKA_POLL_TIMEOUT_MS_URL_KEY = "$pollTimeoutMs$";
	public final static String KAFKA_SESSION_TIMEOUT_MS_URL_KEY = "$sessionTimeoutMs$";
	public final static String KAFKA_CONSUMER_REQUEST_TIMEOUT_MS_URL_KEY = "$consumerRequestTimeoutMs$";
	public final static String KAFKA_FETCH_WAIT_MAX_MS_URL_KEY = "$fetchWaitMaxMs$";

	public final static String KAFKA_BROKER_HOST_PORT_CONFIG_KEY = "brokerHostPort";
	public final static String KAFKA_TOPIC_NAME_CONFIG_KEY = "topicName";
	public final static String KAFKA_GROUP_ID_CONFIG_KEY = "groupId";
	public final static String KAFKA_CLIENT_ID_CONFIG_KEY = "clientId";
	public final static String KAFKA_AUTO_OFFSET_RESET_CONFIG_KEY = "autoOffsetReset";
	public final static String KAFKA_AUTO_COMMIT_ENABLE_CONFIG_KEY = "autoCommitEnable";
	public final static String KAFKA_CONSUMER_COUNT_CONFIG_KEY = "consumerCount";
	public final static String KAFKA_BREAK_ON_FIRST_ERROR_CONFIG_KEY = "breakOnFirstError";
	public final static String KAFKA_AUTO_COMMIT_INTERVAL_MS_CONFIG_KEY = "autoCommitIntervalMs";
	public final static String KAFKA_AUTO_COMMIT_ON_STOP_CONFIG_KEY = "autoCommitOnStop";
	public final static String KAFKA_MAX_POLL_RECORDS_CONFIG_KEY = "maxPollRecords";
	public final static String KAFKA_POLL_TIMEOUT_MS_CONFIG_KEY = "pollTimeoutMs";
	public final static String KAFKA_SESSION_TIMEOUT_MS_CONFIG_KEY = "sessionTimeoutMs";
	public final static String KAFKA_CONSUMER_REQUEST_TIMEOUT_MS_CONFIG_KEY = "consumerRequestTimeoutMs";
	public final static String KAFKA_FETCH_WAIT_MAX_MS_CONFIG_KEY = "fetchWaitMaxMs";

	public final static String KAFKA_ENDPOINT_URI = "kafka:$topicName$?brokers=$kafkaBrokerHostPort$"
			+ "&groupId=$groupId$" + "&clientId=$clientId$" + "&consumersCount=$consumerCount$"
			+ "&autoCommitEnable=$autoCommitEnable$" + "&autoCommitOnStop=$autoCommitOnStop$"
			+ "&autoOffsetReset=$autoOffsetReset$" + "&maxPollRecords=$maxPollRecords$"
			+ "&pollTimeoutMs=$pollTimeoutMs$" + "&sessionTimeoutMs=$sessionTimeoutMs$"
			+ "&consumerRequestTimeoutMs=$consumerRequestTimeoutMs$"
			+ "&fetchWaitMaxMs=$fetchWaitMaxMs$&breakOnFirstError=$breakOnFirstError$";

	public final static String QUARTZ_ENDPOINT_URI = "quartz2://subsciptionTracking/"
			+ "retryFailedSubscription?cron=$cron$&stateful=true";

	// At second :00, every minute starting at minute :00, every hour, every day
	// starting on the 1st, every month
	public static final String DEFAULT_CRON_VALUE = "0+0/1+*+1/1+*+?+*";

	public static final String SEDA_ENDPOINT_URI = "seda:subscriptionRouteForRetry?concurrentConsumers=$concurrentConsumers$";
	public static final String DEFAULT_RETRY_CONCURRENT_CONSUMERS_COUNT = "1";
	public static final String SEDA_CONCURRENT_CONSUMERS_FOR_RERTY_URL_KEY = "$concurrentConsumers$";
	public static final String SEDA_CONCURRENT_CONSUMERS_FOR_RERTY_KEY = "concurrentRetryConsumers";

	public final static String PROCESSING_DECISION_KEY = "processingDecision";
	public static final String PARALLEL_PROCESS_ROUTE_URI = "seda:parallelProcessingSubscriberRoute$subscriptionId$?concurrentConsumers=$concurrentConsumers$";
	public static final String DEFAULT_CONCURRENT_CONSUMERS_COUNT = "1";
	public static final String SEDA_SUBSCRIBER_URL_KEY = "$subscriptionId$";
	public static final String SEDA_CONCURRENT_CONSUMERS_URL_KEY = "$concurrentConsumers$";
	public static final String SEDA_CONCURRENT_CONSUMERS_KEY = "parallelProcessingConsumers";
	public final static String PARALLEL_PROCESSING_ROUTE_ENDPOINT = "seda:parallelProcessingSubscriberRoute";
	public final static String SIMPLE_PROCESSING_ROUTE_ENDPOINT = "direct:simpleProcessingSubscriberRoute";

	// default values
	public static final String DEFAULT_BROKER_HOST_VALUE = "localhost:9092";
	public static final String DEFAULT_GROUP_ID_VALUE = "testGroup";
	public static final String DEFAULT_CLIENT_ID_VALUE = "C1";
	public static final String DEFAULT_CONSUMER_COUNT_VALUE = "1";
	public static final String DEFAULT_MAX_POLL_RECORDS_VALUE = "1";
	public static final String DEFAULT_POLL_TIMEOUT_MS_VALUE = "1000";
	public static final String DEFAULT_SESSION_TIMEOUT_MS_VALUE = "80000";
	public static final String DEFAULT_CONSUMER_REQUEST_TIMEOUT_MS_VALUE = "600000";
	public static final String DEFAULT_FETCH_WAIT_MAX_MS_VALUE = "60000";

	public static final String DEFAULT_AUTO_COMMIT_ENABLE_VALUE = "true";
	public static final String DEFAULT_AUTO_COMMIT_ON_STOP_VALUE = "sync";
	public static final String DEFAULT_AUTO_OFFSET_RESET_VALUE = "earliest";
	public static final String DEFAULT_AUTO_COMMIT_INTERVAL_MS_VALUE = "1000";
	public static final String DEFAULT_BREAK_ON_FIRST_ERROR_VALUE = "true";

	public final static String LOOP_INDEX_KEY = "CamelLoopIndex";
	public final static String INVOKE_ENDPOINT_KEY = "invoke-camel-endpoint";
	public final static String HTTP_POST_REQUEST_SERVICE_CALL_KEY = "http-post-request";
	public final static String ROUTE_ENDPOINT_KEY = "camelRouteDestination";
	public static final String ROUTING_RULES_PER_SUBSCIBER_LOOP_COUNT_KEY = "routingRuleLoopCount";
	public static final String TOPIC_SUBSCRIBER_LOOP_COUNT_KEY = "subscriberLoopCount";
	public static final String SUBSCIBER_EVENT_CONFIG_KEY = "SubscriberEvent";
	public static final String EVENT_ROUTING_RULE_KEY = "EventRoutingRule";
	public static final String DIRECT_COMPONENT = "direct:";
	public final static String ENTRY_ROUTE_FOR_PIPLINE = "direct:pipeactivity";
	public final static String ENTRY_ROUTE_FOR_SUBSCIBER = "direct:baseEntryForSubscriber";
	public static final String SUBSCRIPTION_ID_KEY = "subscriptionId";
	public static final String IS_SUBSCRIPTION_INVOCATION_KEY = "subscriptionInvocation";
	public static final String CONFGIURATION_CONTEXT_KEY = "Configuration-CTX-Key";
	public static final String SUBSCRIPTION_QUARTZ_TRIGGER_KEY = "subscriptionQuartzTrigger";

	public final static String ACTION_KEY = "Action";
	public final static String DEFAULT_ACTION = "NoAction";
	public final static String INVOKE_CAMEL_ROUTE_KEY = "InvokeCamelRoute";
	public final static String HTTP_POST_REQUEST_KEY = "HttpPostRequest";
	public final static String PIPELINE_KEY = "Pipeline";

	public final static String SUBSCRIPTION_ID_REPLACEMENT_CHARACTER = "-";
	public final static String SUBSCRIPTION_ID_NEW_CHARACTER = "_";

	public final static String HOST_NAME_KEY = "hostName";
	public final static String PORT_ADDRESS_KEY = "port";

	public static final String CHARSET_UTF_8_FORMAT = "UTF-8";

	// ognl expression for predicate exception type check will in future change
	// on exception message
	public static final String IS_MISSING_CONFIGURATION = "${exception.class} == \'"
			+ MissingConfigurationException.class.getName() + "\'";
	public static final String IS_ROUTE_INVOKATION_FAILED = "${exception.class} == \'"
			+ RouteInvocationException.class.getName() + "\'";
	public static final String SERVICE_INVOCATION_FAILED = "${exception.class} == \'"
			+ ServiceCallInvocationException.class.getName() + "\'";

	public static final String EXCHANGE_BODY = "ExchangeBody";
	public static final String ERROR_FLAG = "ERROR";
	public static final String SUBSCRIPTION_CALL_HEADER_KEY = "subscriberCall";
	public static final String HTTP_POST_REQUEST_URL_KEY = "POST_URL";
	public static final String IS_RETRY_ENABLED = "retryEnabled";
	public static final String RETRY_COUNT = "retryCount";
	public static final String RETRY_INTERVAL = "retryInterval";
	public static final String INTERVAL_TIMEUNIT = "intervalTimeUnit";
	public static final Integer DEFAULT_RETRY_COUNT = 3;
	public static final Long DEFAULT_RETRY_INTERVAL = (long) 1000;
	public final static String RETRY_STRATEGY_CLASS = "RetryStrategy";
	public static final String DEFAULT_INTERVAL_TIMEUNIT = "MILLISECONDS";
	public final static String EVENT_SUBSCRIPTION_TRACKER_CLASS = "EventSubscriptionTracker";
	public final static String RETRYABLE_SUBSCRIPTIONS_COUNT_KEY = "RetryableSubscriptionLoopCount";
	public final static String RETRYABLE_SUBSCRIPTIONS_LIST_KEY = "RetryableSubscriptionList";
	public final static String LEAP_DEFAULT_RETRY_STRATEGY_CLASS = "LeapDefaultRetryStrategy";
	public final static String LEAP_NO_RETRY_STRATEGY_CLASS = "LeapNoRetryStrategy";

	public static final String TIMEUNIT_MINUTES = "MINUTES";
	public static final String TIMEUNIT_HOURS = "HOURS";
	public static final String TIMEUNIT_SECONDS = "SECONDS";
	public static final String TIMEUNIT_MILLSECONDS = "MILLISECONDS";

	public static final String DEFAULT_ASSIGNED_TENANT_KEY = "Default_Assigned_Tenant";
	public static final String NO_CONFIG_FOR_TENANT_KEY = "NO_DEFAULT_CONFIG";
	public static final String SUB_ID_CLASS_SEPERATOR = "/";
	public static final String TENANT_SITE_SEPERATOR = "--";

}
