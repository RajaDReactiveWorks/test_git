package com.getusroi.eventframework.dispatcher.transformer;

public class ROIEventTransformationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6028776504111905739L;

	@Override
	public String getMessage() {
		return "RoiEvent object is null, Please check the event object";
	}
}
