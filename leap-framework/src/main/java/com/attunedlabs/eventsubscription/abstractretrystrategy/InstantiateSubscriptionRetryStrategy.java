package com.attunedlabs.eventsubscription.abstractretrystrategy;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.core.BeanDependencyResolverFactory;
import com.attunedlabs.core.IBeanDependencyResolver;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.jaxb.FailureHandlingStrategy;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapDefaultRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapNoRetryStrategy;
import com.attunedlabs.eventsubscription.exception.ConfigurationValidationFailedException;
import com.attunedlabs.eventsubscription.exception.MissingConfigurationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;

public class InstantiateSubscriptionRetryStrategy {
	final static Logger logger = LoggerFactory.getLogger(InstantiateSubscriptionRetryStrategy.class);
	// map holding
	// tenantSiteKey --> (generatedSubscriptionId + '/' + strategyClass)key -->
	// classInstance
	private static HashMap<String, Map<String, AbstractSubscriptionRetryStrategy>> cachingInstance = new HashMap<>();

	public static void cacheStrategyClassInstancePerSubscription(SubscribeEvent eventSubscriptionConfig,
			String tenantId, String siteId, String featureGroup, String featureName, String implementation,
			String vendorName, String version) throws EventFrameworkConfigurationException {
		logger.debug(".cacheStrategyClassInstancePerSubscription() SubscribeEvent=" + eventSubscriptionConfig);
		try {
			// we upload in cache only when enabled
			if (eventSubscriptionConfig.isIsEnabled()) {

				String strategy = "";
				String strategyConfig = "{}";
				FailureHandlingStrategy failureHandlingStrategy = eventSubscriptionConfig.getFailureHandlingStrategy();
				if (failureHandlingStrategy != null && failureHandlingStrategy.getFailureStrategyName() != null) {
					strategy = failureHandlingStrategy.getFailureStrategyName().getValue();
					if (strategy == null || strategy.trim().isEmpty())
						strategy = failureHandlingStrategy.getFailureStrategyName().getHandlerQualifiedClass();
					strategyConfig = eventSubscriptionConfig.getFailureHandlingStrategy().getFailureStrategyConfig();

				} else
					strategy = SubscriptionConstant.LEAP_NO_RETRY_STRATEGY_CLASS;

				if (strategyConfig == null || strategyConfig.trim().isEmpty())
					strategyConfig = "{}";

				// setting the strategy in header for executing onSuccess and
				// onFailure
				String tenantSiteKey = tenantId + SubscriptionConstant.TENANT_SITE_SEPERATOR + siteId;
				String generatedSubscriptionId = generateSubscriptionId(tenantId, siteId, featureGroup, featureName,
						implementation, vendorName, version, eventSubscriptionConfig.getSubscriptionId().trim());
				loadAndGetStrategyImplementation(strategy.trim(), generatedSubscriptionId,
						strategyConfig, tenantSiteKey);
			}
		} catch (Exception e) {
			logger.error("subscription failed to cache the strategy instace for subscriptionId  : "
					+ eventSubscriptionConfig.getSubscriptionId());
			throw new EventFrameworkConfigurationException(e.getMessage(), e);
		}
	}

	/**
	 * provides the implementation of strategy class if present in the local
	 * cache else creates new instance and stores in local cache and return the
	 * same.
	 * 
	 * @param strategy
	 * @param subscriptionId
	 * @param strategyConfig
	 * @param tenantSiteKey
	 * @return
	 * @throws MissingConfigurationException
	 * @throws ConfigurationValidationFailedException
	 */
	public static AbstractSubscriptionRetryStrategy loadAndGetStrategyImplementation(String strategy,
			String subscriptionId, String strategyConfig, String tenantSiteKey)
			throws MissingConfigurationException, ConfigurationValidationFailedException {
		Map<String, AbstractSubscriptionRetryStrategy> tenantStrategyMap = cachingInstance.get(tenantSiteKey);
		AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = null;
		if (tenantStrategyMap != null)
			abstractRetryStrategyBean = (AbstractSubscriptionRetryStrategy) tenantStrategyMap
					.get(subscriptionId + SubscriptionConstant.SUB_ID_CLASS_SEPERATOR + strategy);
		// found in the local Map
		if (abstractRetryStrategyBean != null) {
			logger.debug("strategy already instantiated and found in localcache");
			return abstractRetryStrategyBean;
		}

		switch (strategy) {
		case SubscriptionConstant.LEAP_DEFAULT_RETRY_STRATEGY_CLASS:
			abstractRetryStrategyBean = new LeapDefaultRetryStrategy(strategyConfig);
			break;
		case SubscriptionConstant.LEAP_NO_RETRY_STRATEGY_CLASS:
			abstractRetryStrategyBean = new LeapNoRetryStrategy(strategyConfig);
			break;
		default:
			try {
				abstractRetryStrategyBean = getRetryStrategyInstance(strategy, strategyConfig);
			} catch (BeanDependencyResolveException e) {
				/*
				 * logger.
				 * error("failed to get the custom getStrategyImplementation with "
				 * + strategy + ", will not use strategy...");
				 * abstractRetryStrategyBean = new
				 * LeapNonRetryableSubscriptionStrategy(strategyConfig);
				 */

				throw new MissingConfigurationException(
						"BEAN INSTANTIATION FAILED  : Unable to getStrategyImplementation " + " due to "
								+ e.getMessage(),
						e);

			}
		}
		// caching the instance before returning
		cachingRetryStrategyInstance(abstractRetryStrategyBean, strategy, subscriptionId, tenantSiteKey);
		return abstractRetryStrategyBean;
	}

	/**
	 * Instantiation of retry strategy class.
	 * 
	 * @param strategyClass
	 * @return
	 * @throws BeanDependencyResolveException
	 */
	private static AbstractSubscriptionRetryStrategy getRetryStrategyInstance(String strategyClass, String retryConfig)
			throws BeanDependencyResolveException {
		logger.debug("inside getRetryStrategyInstance : " + strategyClass);
		AbstractSubscriptionRetryStrategy retryStrategyImpl = null;
		try {
			IBeanDependencyResolver beanResolver = BeanDependencyResolverFactory.getBeanDependencyResolver();
			retryStrategyImpl = (AbstractSubscriptionRetryStrategy) beanResolver.getBeanInstance(
					AbstractSubscriptionRetryStrategy.class, strategyClass, new Class<?>[] { retryConfig.getClass() },
					new String[] { retryConfig });
		} catch (IllegalArgumentException | BeanDependencyResolveException exp) {
			logger.error("Failed to initialize CustomRetryStrategy {" + strategyClass + "}", exp);
			throw new BeanDependencyResolveException(
					"Failed to initialize CustomRetryStrategy {" + strategyClass + "} ==> " + exp.getMessage(), exp);
		}
		return retryStrategyImpl;

	}

	/**
	 * caching the strategy instance for next request.
	 * 
	 * @param abstractRetryStrategyBean
	 * @param strategy
	 * @param subscriptionId
	 * @param tenantSiteKey
	 * @param site
	 */
	private static void cachingRetryStrategyInstance(AbstractSubscriptionRetryStrategy abstractRetryStrategyBean,
			String strategy, String subscriptionId, String tenantSiteKey) {
		Map<String, AbstractSubscriptionRetryStrategy> tenantStrategyMap = cachingInstance.get(tenantSiteKey);
		if (tenantStrategyMap == null) {
			tenantStrategyMap = new HashMap<>();
		}
		tenantStrategyMap.put(subscriptionId + SubscriptionConstant.SUB_ID_CLASS_SEPERATOR + strategy,
				abstractRetryStrategyBean);
		cachingInstance.put(tenantSiteKey, tenantStrategyMap);
	}

	/**
	 * subscription id construction as
	 * fGroup-fName-impl-vendor-version-subscriptionId
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param featureGroup
	 * @param featureName
	 * @param implementation
	 * @param vendorName
	 * @param version
	 * @param subscriptionId
	 * @return
	 */
	private static String generateSubscriptionId(String tenantId, String siteId, String featureGroup, String featureName,
			String implementation, String vendorName, String version, String subscriptionId) {
		return nullParameterCheck(featureGroup) + EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER
				+ nullParameterCheck(featureName) + EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER
				+ nullParameterCheck(implementation) + EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER
				+ nullParameterCheck(vendorName) + EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER
				+ nullParameterCheck(version) + EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER
				+ nullParameterCheck(subscriptionId);
	}

	/**
	 * nodeType empty check
	 * 
	 * @param nodeType
	 * @return
	 */
	private static String nullParameterCheck(String nodeType) {
		nodeType.replace(EventFrameworkConstants.ATTRIBUTE_CHARACTER_REPLACE,
				EventFrameworkConstants.EMPTY_REPLACEMENT);
		return nodeType.isEmpty() ? "" : nodeType;
	}

	/**
	 * @return the cachingInstance
	 */
	public static HashMap<String, Map<String, AbstractSubscriptionRetryStrategy>> getCachingInstance() {
		return cachingInstance;
	}

}
