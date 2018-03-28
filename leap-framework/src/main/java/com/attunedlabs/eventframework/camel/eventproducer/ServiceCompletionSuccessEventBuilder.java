package com.attunedlabs.eventframework.camel.eventproducer;

import org.apache.camel.Exchange;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.event.ServiceCompletionSuccessEvent;
import com.attunedlabs.eventframework.jaxb.Event;

public class ServiceCompletionSuccessEventBuilder extends	AbstractCamelEventBuilder {


	/**
	 * This method is to Service Completion Success Event
	 * @return : LeapEvent
	 */
	@Override
	public LeapEvent buildEvent(Exchange camelExchange,Event event) {		
		String tenantId=getTenantId(camelExchange);
		String serviceType=getServiceType(camelExchange);
		String requestId=(String)camelExchange.getIn().getHeader(CamelEventProducerConstant.REQUEST_UID);
		
		RequestContext reqCtx=super.getRequestContextFromCamelExchange(camelExchange);
		ServiceCompletionSuccessEvent evt = new ServiceCompletionSuccessEvent(reqCtx);
		super.updateStandardCamelHeader(camelExchange, evt);
		//Add other camel header into the Event Header and EventParam
		return evt;
	}

}
