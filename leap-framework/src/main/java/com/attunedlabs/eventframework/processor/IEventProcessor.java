package com.attunedlabs.eventframework.processor;

import com.attunedlabs.eventframework.event.LeapEvent;

public interface IEventProcessor {
	public void processEvent(LeapEvent event) throws EventProcessException;
}
