package com.getusroi.integrationfwk.activities.event;

import com.getusroi.config.RequestContext;
import com.getusroi.eventframework.event.ROIEvent;

public class PipelineEvent extends ROIEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6028100422444944625L;

	public PipelineEvent(String eventId,RequestContext requestContext){
		super(eventId,requestContext);
	}

}
