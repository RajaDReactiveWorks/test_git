package com.getusroi.eventframework.dispatcher.transformer;

import java.io.Serializable;

import com.getusroi.eventframework.event.ROIEvent;

public interface IROIEventTransformer {
	
	public Serializable transformEvent(ROIEvent roievent) throws ROIEventTransformationException;

}	