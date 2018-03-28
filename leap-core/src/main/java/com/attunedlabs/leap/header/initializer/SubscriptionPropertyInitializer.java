package com.attunedlabs.leap.header.initializer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.attunedlabs.eventsubscription.util.SubscriptionConstant;

public class SubscriptionPropertyInitializer implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		// checking if subscription call or not set the extra property
		if (exchange.getProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY) == null)
			exchange.setProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY, false);

	}

}
