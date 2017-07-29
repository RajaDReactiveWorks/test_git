package com.getusroi.eventframework.camel.eventproducer;

import org.apache.camel.Exchange;

import com.getusroi.config.RequestContext;
import com.getusroi.eventframework.event.ROIEvent;
import com.getusroi.eventframework.event.ServiceCompletionFailureEvent;
import com.getusroi.eventframework.jaxb.Event;

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
	 * @return : ROIEvent
	 */
	public ROIEvent buildEvent(Exchange camelExchange,Event eventConfig) {
				
		RequestContext reqCtx=super.getRequestContextFromCamelExchange(camelExchange);
		ServiceCompletionFailureEvent evt = new ServiceCompletionFailureEvent(reqCtx);
		this.updateStandardCamelHeader(camelExchange, evt);
		this.updateFailureCamelHeader(camelExchange, evt);
		// Add other camel header into the Event Header and EventParam
		// #TODO
		return evt;
	}

}
