package com.getusroi.eventframework.camel.eventproducer;

import org.apache.camel.Exchange;

import com.getusroi.config.RequestContext;
import com.getusroi.eventframework.event.ROIEvent;
import com.getusroi.eventframework.event.ServiceCompletionSuccessEvent;
import com.getusroi.eventframework.jaxb.Event;

public class ServiceCompletionSuccessEventBuilder extends	AbstractCamelEventBuilder {


	/**
	 * This method is to Service Completion Success Event
	 * @return : ROIEvent
	 */
	@Override
	public ROIEvent buildEvent(Exchange camelExchange,Event event) {		
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
