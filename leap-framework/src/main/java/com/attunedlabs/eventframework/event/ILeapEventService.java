package com.attunedlabs.eventframework.event;

import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;

public interface ILeapEventService {
	public void publishEvent(LeapEvent event) throws InvalidEventException, EventFrameworkConfigurationException,
			LeapEventTransformationException, MessageDispatchingException;

	public boolean validateEvent(LeapEvent event) throws InvalidEventException;

	public void publishSystemEvent(LeapEvent event) throws InvalidEventException, LeapEventTransformationException,
			MessageDispatchingException, EventFrameworkConfigurationException;
}
