package com.getusroi.eventframework.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.eventframework.dispatcher.EventFrameworkDispatcherException;
import com.getusroi.eventframework.dispatcher.EventFrameworkDispatcherService;


public class ROIEventService implements IROIEventService{
	private static final Logger logger = LoggerFactory.getLogger(ROIEventService.class);
	
	public void publishEvent(ROIEvent event) throws InvalidEventException, EventFrameworkDispatcherException {
		logger.debug("publishEvent() in eventId="+event.getEventId());
		EventFrameworkDispatcherService dispatchService=new EventFrameworkDispatcherService();
		dispatchService.dispatchforEvent(event);
		logger.debug("publishEvent() eventId="+event.getEventId()+" dispatched to dispatcher Service");
	}

	@Override
	public boolean validateEvent(ROIEvent event) throws InvalidEventException {
		// TODO Auto-generated method stub
		return false;
	}

	public void publishSystemEvent(ROIEvent event) throws InvalidEventException, EventFrameworkDispatcherException {
		logger.debug("publishSystemEvent--"+event.toString());
		if(!(event instanceof ServiceCompletionFailureEvent || event instanceof ServiceCompletionSuccessEvent)){
			throw new InvalidEventException();
		}
		EventFrameworkDispatcherService dispatchService=new EventFrameworkDispatcherService();
		dispatchService.dispatchforSystemEvent(event);
	}
	

}
