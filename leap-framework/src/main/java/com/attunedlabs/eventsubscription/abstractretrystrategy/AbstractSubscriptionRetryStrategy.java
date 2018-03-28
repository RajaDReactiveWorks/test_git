package com.attunedlabs.eventsubscription.abstractretrystrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapDefaultRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapNoRetryStrategy;
import com.attunedlabs.eventsubscription.exception.ConfigurationValidationFailedException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;
import com.attunedlabs.eventsubscriptiontracker.IEventSubscriptionTrackerService;
import com.attunedlabs.eventsubscriptiontracker.impl.EventSubscriptionTrackerImpl;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;

/**
 * <code>AbstractSubscriptionRetryStrategy</code> provides the life-cycle
 * methods for execution of any action.Also provides getRetryableRecords() for
 * rertying all the failed records.
 * 
 * @see : {@link LeapDefaultRetryStrategy}
 * @see : {@link LeapNoRetryStrategy}
 * 
 * @author Reactiveworks42
 *
 */
public abstract class AbstractSubscriptionRetryStrategy {
	protected final static Logger log = LoggerFactory.getLogger(AbstractSubscriptionRetryStrategy.class);

	protected final IEventFrameworkConfigService eventFrameworkConfigService = new EventFrameworkConfigService();
	protected final IEventSubscriptionTrackerService eventSubscriptionLogService = new EventSubscriptionTrackerImpl();
	protected final static SubscriptionUtil subscriptionUtil = new SubscriptionUtil();
	protected JSONObject retryConfiguration;

	protected AbstractSubscriptionRetryStrategy(String strategyConfig) throws ConfigurationValidationFailedException {
		boolean parsingStatus = parseConfiguration(strategyConfig);
		assertConfiguration(parsingStatus);
	}

	/**
	 * Asserts whether Keys found in FailureStrategyConfiguration matches the
	 * Keys got from {@code getAllStrategyConfigurationKeys()}
	 * 
	 * @see AbstractSubscriptionRetryStrategy#getAllStrategyConfigurationKeys()
	 * 
	 * 
	 * @param parsingStatus
	 * @throws ConfigurationValidationFailedException
	 */
	private void assertConfiguration(boolean parsingStatus) throws ConfigurationValidationFailedException {
		if (validatePassedStrategyConfiguration() && !parsingStatus) {
			log.debug("Pre-Assertion retryJsonConfig " + retryConfiguration);
			if (!assertStrategyConfigurationKeys())
				throw new ConfigurationValidationFailedException(
						"Assertion of the Strategy Configuration Keys Failed : Only acceptable Config Keys for "
								+ this.getClass().getSimpleName() + " are " + getAllStrategyConfigurationKeys()
								+ " (Hint : Configuration Keys are all Case Sensitive)");
		}
		if (validatePassedStrategyConfiguration() && parsingStatus)
			throw new ConfigurationValidationFailedException(
					"Assertion of the Strategy Configuration Keys Failed against " + getAllStrategyConfigurationKeys()
							+ " for " + this.getClass().getSimpleName()
							+ " Strategy (Hint : Invalid JSON Configuration)");

	}

	private boolean parseConfiguration(String strategyConfig) {
		JSONParser parser = new JSONParser();
		boolean isFailed = false;
		Object obj = null;
		try {
			obj = parser.parse(strategyConfig);
			retryConfiguration = (JSONObject) obj;
		} catch (ParseException e) {
			log.error("failed to parse retry configuration passes . " + e.getMessage());
			isFailed = true;
			setEmptyRetryConfiguration("{}");
		}
		return isFailed;

	}

	/**
	 * 
	 * @return assertion against keys.
	 */
	@SuppressWarnings("unchecked")
	private boolean assertStrategyConfigurationKeys() {
		JSONObject validateRetryConfiguration = getRetryConfiguration();
		List<String> strategyConfigKeys = getAllStrategyConfigurationKeys();
		if (strategyConfigKeys == null)
			strategyConfigKeys = new ArrayList<>();
		Set<String> presentKeys = validateRetryConfiguration.keySet();
		for (String key : presentKeys) {
			if (strategyConfigKeys.toString().contains(key))
				continue;
			else
				return false;
		}

		return true;
	}

	public JSONObject getRetryConfiguration() {
		return retryConfiguration;
	}

	public void setEmptyRetryConfiguration(String retryJsonConfig) {
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(retryJsonConfig);
		} catch (ParseException e) {
			log.error("failed to parse retry configuration {}. " + e.getMessage());
		}
		retryConfiguration = (JSONObject) obj;
		log.debug("retryJsonConfig " + retryConfiguration);
	}

	/**
	 * This method is used to invoke pre-processing method, and sets the
	 * Implementation for AbstractSubscriptionRetryStrategy in the exchange
	 * header.
	 * 
	 * @param exch
	 * @throws Exception
	 */
	public final static void processBean(Exchange exchange) throws Exception {
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);

		AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = SubscriptionUtil
				.getCachedStrategyInstance(exchange, leapHeader);

		// set the abstract strategy in leapHeader
		Map<String, Object> oldLeap = leapHeader.getGenricdata();
		oldLeap.put(SubscriptionConstant.RETRY_STRATEGY_CLASS, abstractRetryStrategyBean);
		abstractRetryStrategyBean.preProcessing(exchange, oldLeap);
	}

	/**
	 * This method will decide whether to assert the Subscriber FailureStrategy
	 * Configuration against the List of keys passed by
	 * {@code getAllStrategyConfigurationKeys()}. Assertion will only fail if
	 * strategy configuration contains any key other than keys present in the
	 * List. <i>(Assertion process is CaseSensitive)</i>
	 * 
	 * @see AbstractSubscriptionRetryStrategy#getAllStrategyConfigurationKeys()
	 * 
	 * @return true or false : decides whether to assert or not.
	 */
	public abstract boolean validatePassedStrategyConfiguration();

	/**
	 * This method should return the List of all the applicable Configuration
	 * keys that can be passed as Retry Configuration.This method will only be
	 * helpful if validatePassedStrategyConfiguration return true else skipped.
	 * 
	 * @return list of all the available FailureStrategy Configuration keys.
	 */
	public abstract List<String> getAllStrategyConfigurationKeys();

	/**
	 * This method will be executed before executing any action.
	 * 
	 * @param exchange
	 * @param metaData
	 *            : contains the EventSubscriptionTracker resonsible for holding
	 *            the metadatof record.
	 * @throws Exception
	 */
	public abstract void preProcessing(Exchange exchange, Map<String, Object> metaData) throws Exception;

	/**
	 * This method will be executed onSuccessfull execution of action.
	 *
	 * @param metaData
	 * @param exchange
	 * @throws Exception
	 */
	public abstract void onSuccess(Exchange exchange, Map<String, Object> metaData) throws Exception;

	/**
	 * This method will be executed if any exception occurs while executing
	 * action.
	 * 
	 * @param exchange
	 * @param metaData
	 * @param exception
	 * @throws Exception
	 */
	public abstract void onFailure(Exchange exchange, Map<String, Object> metaData, Exception exception)
			throws Exception;

	/**
	 * This method will retrun all the retryable records from the failed
	 * subscriptions. Retry activity will be performed on the records returned
	 * only. Next invocation for this method will be done only after retrying
	 * all the previously returned failed records.
	 * 
	 * @param exchange
	 * @param tenantId
	 * @param siteId
	 * @param subscriptionId
	 * @return retryableFailedRecords
	 * 
	 * @throws Exception
	 */
	public abstract List<EventSubscriptionTracker> getRetryableRecords(Exchange exchange, String tenantId,
			String siteId, String subscriptionId) throws Exception;

	/**
	 * Gives Name of implementation class along with the passed configuration.
	 * 
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "--> retryConfiguration --> " + retryConfiguration;
	}

}
