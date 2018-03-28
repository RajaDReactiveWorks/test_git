package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;

/**
 * <code>SubscriptionPerProcessHandlerBean</code> initialized the configuration
 * and stores the retry implementation in cache.
 * 
 * @author Reactiveworks42
 *
 */
public class SubscriptionPerProcessHandlerBean implements Processor {
	final Logger log = LoggerFactory.getLogger(SubscriptionFailureHandlerBean.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		log.debug("inside process ...SubscriptionPerProcessHandlerBean");
		AbstractSubscriptionRetryStrategy.processBean(exchange);
	}

}