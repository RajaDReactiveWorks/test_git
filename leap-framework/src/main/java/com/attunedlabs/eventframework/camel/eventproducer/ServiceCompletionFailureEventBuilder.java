package com.attunedlabs.eventframework.camel.eventproducer;

import org.apache.camel.Exchange;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.event.ServiceCompletionFailureEvent;
import com.attunedlabs.eventframework.jaxb.Event;

/**
 * This class is to build Service Completion Failure Event Builder
 * 
 * @author bizruntime
 *
 */
public class ServiceCompletionFailureEventBuilder extends AbstractCamelEventBuilder {

	/**
	 * This method is to Service Completion Failure Event
	 * 
	 * @return : LeapEvent
	 */
	public LeapEvent buildEvent(Exchange camelExchange,Event eventConfig) {
				
		RequestContext reqCtx=super.getRequestContextFromCamelExchange(camelExchange);
		ServiceCompletionFailureEvent evt = new ServiceCompletionFailureEvent(reqCtx);
		this.updateStandardCamelHeader(camelExchange, evt);
		this.updateFailureCamelHeader(camelExchange, evt);
		// Add other camel header into the Event Header and EventParam
		// #TODO
		return evt;
	}

}
