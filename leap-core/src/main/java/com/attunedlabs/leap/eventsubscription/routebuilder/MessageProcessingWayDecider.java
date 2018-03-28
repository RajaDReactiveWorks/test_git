package com.attunedlabs.leap.eventsubscription.routebuilder;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaEndpoint;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.FailureHandlingStrategy;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.abstractretrystrategy.InstantiateSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapNoRetryStrategy;
import com.attunedlabs.eventsubscription.retrypolicy.SubscriptionNoRetryPolicy;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.leap.LeapHeaderConstant;

/**
 * <code>MessageProcessingWayDecider</code> class is responsible for taking
 * descision how the message should be processed either paralle or sequential.
 * Descision is taken based on the subscribers demand.
 * 
 * @author Reactiveworks42
 *
 */
public class MessageProcessingWayDecider implements Processor {

	private IEventFrameworkConfigService eventFrameworkConfigService;
	private SubscriptionUtil subscriptionUtil;

	public MessageProcessingWayDecider(IEventFrameworkConfigService eventFrameworkConfigService,
			SubscriptionUtil subscriptionUtil) {
		this.eventFrameworkConfigService = eventFrameworkConfigService;
		this.subscriptionUtil = subscriptionUtil;

	}

	@Override
	public void process(Exchange exchange) throws Exception {
		String subscriberId = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, String.class);
		if (subscriberId == null || subscriberId.trim().isEmpty()) {
			Endpoint fromEndpoint = exchange.getFromEndpoint();
			if (fromEndpoint instanceof KafkaEndpoint)
				subscriberId = ((KafkaEndpoint) fromEndpoint).getConfiguration().getGroupId();
			exchange.getIn().setHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, subscriberId.trim());
		}

		boolean configPresent = true;
		ConfigurationContext configCtx = subscriptionUtil.buildConfigContext(LeapHeaderConstant.tenant,
				LeapHeaderConstant.site, subscriberId);
		configPresent = (configCtx != null);
		SubscribeEvent eventSubscription = null;
		if (configPresent) {
			try {
				eventSubscription = eventFrameworkConfigService.getEventSubscriptionConfiguration(configCtx,
						subscriptionUtil.getActualSubscriberId(subscriberId));
			} catch (EventFrameworkConfigurationException e) {
				configPresent = false;
			}
			configPresent = (eventSubscription != null);

		}

		if (!configPresent)
			exchange.getIn().setHeader(SubscriptionConstant.PROCESSING_DECISION_KEY,
					SubscriptionConstant.SIMPLE_PROCESSING_ROUTE_ENDPOINT);
		else {
			HashMap<String, Map<String, AbstractSubscriptionRetryStrategy>> cachingInstance = InstantiateSubscriptionRetryStrategy
					.getCachingInstance();
			Map<String, AbstractSubscriptionRetryStrategy> eventMap = cachingInstance
					.get(LeapHeaderConstant.tenant + "-" + LeapHeaderConstant.site);
			String strategy = "";
			FailureHandlingStrategy failureHandlingStrategy = eventSubscription.getFailureHandlingStrategy();
			if (failureHandlingStrategy != null && failureHandlingStrategy.getFailureStrategyName() != null) {
				strategy = failureHandlingStrategy.getFailureStrategyName().getValue();
				if (strategy == null || strategy.trim().isEmpty())
					strategy = failureHandlingStrategy.getFailureStrategyName().getHandlerQualifiedClass();

			} else
				strategy = SubscriptionConstant.LEAP_NO_RETRY_STRATEGY_CLASS;

			String subscriptionId = subscriptionUtil.getActualSubscriberId(subscriberId);
			String parralelProcessingEndpoint = SubscriptionConstant.PARALLEL_PROCESSING_ROUTE_ENDPOINT
					+ subscriptionId;
			String simpleProcessingEndpoint = SubscriptionConstant.SIMPLE_PROCESSING_ROUTE_ENDPOINT + subscriptionId;

			if (strategy.equals(SubscriptionConstant.LEAP_NO_RETRY_STRATEGY_CLASS)) {
				AbstractSubscriptionRetryStrategy abstractSubscriptionRetryStrategy = eventMap
						.get(subscriberId + SubscriptionConstant.SUB_ID_CLASS_SEPERATOR + strategy);
				LeapNoRetryStrategy leapNoRetryStrategy = (LeapNoRetryStrategy) abstractSubscriptionRetryStrategy;
				if (SubscriptionNoRetryPolicy
						.assertParallelProcessingEnabled(leapNoRetryStrategy.getRetryConfiguration()))
					exchange.getIn().setHeader(SubscriptionConstant.PROCESSING_DECISION_KEY,
							parralelProcessingEndpoint);
				else
					exchange.getIn().setHeader(SubscriptionConstant.PROCESSING_DECISION_KEY, simpleProcessingEndpoint);

			} else
				exchange.getIn().setHeader(SubscriptionConstant.PROCESSING_DECISION_KEY, simpleProcessingEndpoint);
		}
	}
}
