package com.attunedlabs.eventframework.dispatcher.transformer;

import java.io.Serializable;

import com.attunedlabs.eventframework.event.LeapEvent;

public interface ILeapEventTransformer {
	
	public Serializable transformEvent(LeapEvent leapevent) throws LeapEventTransformationException;

}	