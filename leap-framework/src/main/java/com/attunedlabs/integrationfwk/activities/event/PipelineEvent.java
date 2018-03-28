package com.attunedlabs.integrationfwk.activities.event;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.event.LeapEvent;

public class PipelineEvent extends LeapEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6028100422444944625L;

	public PipelineEvent(String eventId,RequestContext requestContext){
		super(eventId,requestContext);
	}

}
