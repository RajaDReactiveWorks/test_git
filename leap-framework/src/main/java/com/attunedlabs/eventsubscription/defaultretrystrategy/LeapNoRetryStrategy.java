package com.attunedlabs.eventsubscription.defaultretrystrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.json.simple.JSONObject;

import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.exception.ConfigurationValidationFailedException;
import com.attunedlabs.eventsubscription.retrypolicy.SubscriptionNoRetryPolicy;
import com.attunedlabs.eventsubscription.util.EventSubscriptionTrackerConstants;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;

/**
 * Default implementation provided for {@link AbstractSubscriptionRetryStrategy}
 * class. This class will store the record in EventSubscriptionTracker table and
 * also take care of the status at each stage.Retry attempt will not be
 * performed and also whether to log or not to the table also can be decided
 * with configuration.
 * 
 * @see : {@link SubscriptionNoRetryPolicy}
 * @author Reactiveworks42
 *
 */
public class LeapNoRetryStrategy extends AbstractSubscriptionRetryStrategy {

	public LeapNoRetryStrategy(String strategyConfig) throws ConfigurationValidationFailedException {
		super(strategyConfig);
		initializeRetryConfig(getRetryConfiguration());

	}

	@Override
	public boolean validatePassedStrategyConfiguration() {
		return true;
	}

	@Override
	public List<String> getAllStrategyConfigurationKeys() {
		return Arrays.asList(new String[] { SubscriptionNoRetryPolicy.MSG_LOGGING_ENABLED_KEY,
				SubscriptionNoRetryPolicy.PARALLEL_PROCESSING_KEY });
	}

	@Override
	public void preProcessing(Exchange exchange, Map<String, Object> metaData) {
		log.debug("inside preProcessing() of NonRetryableStrategy...");
		if (SubscriptionNoRetryPolicy.assertMessageLogEnabled(this.getRetryConfiguration())) {
			eventSubscriptionLogService.addNewSubscriptionRecord(exchange, metaData);
			eventSubscriptionLogService.updateSubscriptionRecordStatus(exchange, metaData,
					EventSubscriptionTrackerConstants.STATUS_IN_PROCESS, null, this.getRetryConfiguration());
		} else
			log.debug("PRE_PROCESS "
					+ (EventSubscriptionTracker) metaData.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS));

	}

	@Override
	public void onSuccess(Exchange exchange, Map<String, Object> metaData) {
		log.debug("inside onSuccess() of NonRetryableStrategy...");
		if (SubscriptionNoRetryPolicy.assertMessageLogEnabled(getRetryConfiguration())) {
			eventSubscriptionLogService.updateSubscriptionRecordStatus(exchange, metaData,
					EventSubscriptionTrackerConstants.STATUS_COMPLETE, null, this.getRetryConfiguration());
		} else
			log.debug("SUCCESS "
					+ (EventSubscriptionTracker) metaData.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS));

	}

	@Override
	public void onFailure(Exchange exchange, Map<String, Object> metaData, Exception exception) {
		log.debug("inside onFailure() of NonRetryableStrategy...");
		if (SubscriptionNoRetryPolicy.assertMessageLogEnabled(getRetryConfiguration())) {
			eventSubscriptionLogService.updateSubscriptionRecordStatus(exchange, metaData,
					EventSubscriptionTrackerConstants.STATUS_FAILED, exception, this.getRetryConfiguration());
		} else
			log.debug("FAILED "
					+ (EventSubscriptionTracker) metaData.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS));

	}

	@Override
	public List<EventSubscriptionTracker> getRetryableRecords(Exchange exchange, String tenantId, String siteId,
			String subscriptionId) throws Exception {
		log.debug("inside getRetryableRecords() of NonRetryableStrategy...");
		return new ArrayList<>();
	}

	/**
	 * initialize in jsonConfig for specific default no strategy.
	 * 
	 * @param retryConfigurationJSON
	 */
	private void initializeRetryConfig(JSONObject retryConfigurationJSON) {
		SubscriptionNoRetryPolicy.assertMessageLogEnabled(retryConfigurationJSON);
		SubscriptionNoRetryPolicy.assertParallelProcessingEnabled(retryConfigurationJSON);
	}

}
