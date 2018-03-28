package com.attunedlabs.leap.eventsubscription.processor;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.EventRoutingRule;
import com.attunedlabs.eventframework.jaxb.EventRoutingRules;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;

/**
 * <code>SubscriberRoutingRuleCalculationProcessor</code> will get the number of
 * routing rules configured per subsciber.
 * 
 * @author Reactiveworks42
 *
 */
public class SubscriberRoutingRuleCalculationProcessor implements Processor {

	final static Logger log = LoggerFactory.getLogger(SubscriberRoutingRuleCalculationProcessor.class);

	private IEventFrameworkConfigService eventFrameworkConfigService;
	private SubscriptionUtil subscriptionUtil;

	public SubscriberRoutingRuleCalculationProcessor(IEventFrameworkConfigService eventFrameworkConfigService,
			SubscriptionUtil subscriptionUtil) {
		this.eventFrameworkConfigService = eventFrameworkConfigService;
		this.subscriptionUtil = subscriptionUtil;

	}

	/**
	 * adds some extra headers based on the result of exchange which is the
	 * number of subsciber routing rules count and the subsciption
	 * configuration.
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		log.debug("processor invocation for calculation routing rules for subscriber");
		if (exchange.getIn() != null) {

			// initialize routing rules loop count
			exchange.getIn().setHeader(SubscriptionConstant.ROUTING_RULES_PER_SUBSCIBER_LOOP_COUNT_KEY, 0);

			// getSubscription Event confiuration from header.
			SubscribeEvent eventSubscription = exchange.getIn()
					.getHeader(SubscriptionConstant.SUBSCIBER_EVENT_CONFIG_KEY, SubscribeEvent.class);

			if (eventSubscription != null) {
				log.debug("actual subscription Id" + eventSubscription.getSubscriptionId());

				EventRoutingRules eventRoutingRules = eventSubscription.getEventRoutingRules();
				if (eventRoutingRules != null) {
					List<EventRoutingRule> totalEventRoutingRule = eventRoutingRules.getEventRoutingRule();

					log.debug("total number of routing rules configured for subsciberId : "
							+ eventSubscription.getSubscriptionId() + " are " + totalEventRoutingRule.size());

					// setting subscriber routing rules count in exchange header.
					exchange.getIn().setHeader(SubscriptionConstant.ROUTING_RULES_PER_SUBSCIBER_LOOP_COUNT_KEY,
							totalEventRoutingRule.size());
				}
			}

		}
	}
}
