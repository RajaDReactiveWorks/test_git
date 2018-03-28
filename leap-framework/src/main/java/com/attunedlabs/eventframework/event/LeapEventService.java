package com.attunedlabs.eventframework.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.EventFrameworkDispatcherService;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;

public class LeapEventService implements ILeapEventService {
	private static final Logger logger = LoggerFactory.getLogger(LeapEventService.class);

	public void publishEvent(LeapEvent event) throws InvalidEventException, 
			EventFrameworkConfigurationException, LeapEventTransformationException, MessageDispatchingException {
		logger.debug("publishEvent() in eventId=" + event.getEventId());
		EventFrameworkDispatcherService dispatchService = new EventFrameworkDispatcherService();
		dispatchService.dispatchforEvent(event);
		logger.debug("publishEvent() eventId=" + event.getEventId() + " dispatched to dispatcher Service");
	}

	@Override
	public boolean validateEvent(LeapEvent event) throws InvalidEventException {
		// TODO Auto-generated method stub
		return false;
	}

	public void publishSystemEvent(LeapEvent event) throws InvalidEventException, LeapEventTransformationException,
			MessageDispatchingException, EventFrameworkConfigurationException {
		logger.debug("publishSystemEvent--" + event.toString());
		if (!(event instanceof ServiceCompletionFailureEvent || event instanceof ServiceCompletionSuccessEvent)) {
			throw new InvalidEventException();
		}
		EventFrameworkDispatcherService dispatchService = new EventFrameworkDispatcherService();
		dispatchService.dispatchforSystemEvent(event);
	}

}
