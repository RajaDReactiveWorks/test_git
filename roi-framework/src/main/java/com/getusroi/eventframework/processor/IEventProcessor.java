package com.getusroi.eventframework.processor;

import com.getusroi.eventframework.event.ROIEvent;

public interface IEventProcessor {
	public void processEvent(ROIEvent event) throws EventProcessException;
}
