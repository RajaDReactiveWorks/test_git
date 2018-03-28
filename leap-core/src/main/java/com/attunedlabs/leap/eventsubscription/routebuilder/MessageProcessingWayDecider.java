package com.attunedlabs.leap.eventsubscription.routebuilder;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaEndpoint;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.exception.NonRetryableException;
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
		try {
			String subscriberId = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, String.class);
			// setting header to identify call is from kafka component
			exchange.getIn().setHeader(SubscriptionConstant.KAFKA_CALL, true);

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

			System.out.println("Messsage Processing..");

			String subscriptionId = subscriptionUtil.getActualSubscriberId(subscriberId);
			String parralelProcessingEndpoint = SubscriptionConstant.PARALLEL_PROCESSING_ROUTE_ENDPOINT
					+ subscriptionId;
			String simpleProcessingEndpoint = SubscriptionConstant.SIMPLE_PROCESSING_ROUTE_ENDPOINT + subscriptionId;

			if (!configPresent)
				exchange.getIn().setHeader(SubscriptionConstant.PROCESSING_DECISION_KEY, simpleProcessingEndpoint);
			else {
				if (eventSubscription.isParallelProcessing())
					exchange.getIn().setHeader(SubscriptionConstant.PROCESSING_DECISION_KEY,
							parralelProcessingEndpoint);
				else
					exchange.getIn().setHeader(SubscriptionConstant.PROCESSING_DECISION_KEY, simpleProcessingEndpoint);
			}
		} catch (Exception e) {
			throw new NonRetryableException(
					"MANUAL COMMIT OFFSET : Kafka counter explicilty incremented.. no subscriber configuration available -> "
							+ e.getMessage());
		}
	}
}
