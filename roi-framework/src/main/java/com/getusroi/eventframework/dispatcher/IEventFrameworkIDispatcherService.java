package com.getusroi.eventframework.dispatcher;

import com.getusroi.eventframework.event.ROIEvent;

public interface IEventFrameworkIDispatcherService {
	public void dispatchforEvent(ROIEvent roiEvent)throws EventFrameworkDispatcherException;
}
