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
 * <code>SubscriptionFailureHandlerBean</code> marks the failed record in
 * subscriptionTracker table to FAILED. This class is also responsible to
 * perform retry on retryable type subscriptions if invoked through quartz.
 * 
 * @author Reactiveworks42
 *
 */
public class SubscriptionFailureHandlerBean implements Processor {
	final Logger log = LoggerFactory.getLogger(SubscriptionFailureHandlerBean.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		log.debug("inside process ...SubscriptionFailureHandlerBean");
		Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		if (leapHeader != null) {
			Map<String, Object> oldLeap = leapHeader.getGenricdata();
			if (oldLeap != null) {
				AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = (AbstractSubscriptionRetryStrategy) oldLeap
						.get(SubscriptionConstant.RETRY_STRATEGY_CLASS);
				abstractRetryStrategyBean.onFailure(exchange, oldLeap, exception);

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
