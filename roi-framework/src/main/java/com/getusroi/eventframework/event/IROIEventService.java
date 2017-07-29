package com.getusroi.eventframework.event;

import com.getusroi.eventframework.dispatcher.EventFrameworkDispatcherException;



public interface IROIEventService {
	public void publishEvent(ROIEvent event)throws InvalidEventException, EventFrameworkDispatcherException ;
	public boolean validateEvent(ROIEvent event)throws InvalidEventException;
	public void publishSystemEvent(ROIEvent event)throws InvalidEventException, EventFrameworkDispatcherException ;
}
