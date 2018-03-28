package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.eventsubscription.routebuilder.GenericRetryRouteBuilder;

/**
 * <code>SubscriptionSuccessHandlerBean</code> marks the records as SUCCESS
 * after performing the action successfully.
 * 
 * @author Reactiveworks42
 *
 */
public class SubscriptionSuccessHandlerBean implements Processor {
	final Logger log = LoggerFactory.getLogger(SubscriptionSuccessHandlerBean.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		log.debug("inside process ...SubscriptionSuccessHandlerBean");

		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		if (leapHeader != null) {
			Map<String, Object> oldLeap = leapHeader.getGenricdata();
			if (oldLeap != null) {
				AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = (AbstractSubscriptionRetryStrategy) oldLeap
						.get(SubscriptionConstant.RETRY_STRATEGY_CLASS);
				abstractRetryStrategyBean.onSuccess(exchange, oldLeap);

				// deleting from retry list
				EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) oldLeap
						.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);
				if (eventSubscriptionTracker != null)
					if (GenericRetryRouteBuilder.totalRetryableRecords.contains(eventSubscriptionTracker)) {
						GenericRetryRouteBuilder.totalRetryableRecords.remove(eventSubscriptionTracker);
						log.debug("DELETING \n" + eventSubscriptionTracker
								+ "\n GenericRetryRouteBuilder.totalRetryableRecords \n"
								+ GenericRetryRouteBuilder.totalRetryableRecords.size());
					}
				return;
			}
		}
		log.warn("leapHeader or genricData is missing in exchange ..failed to find any retrystrategy");

	}

}
