package com.attunedlabs.eventframework.dispatcher.transformer;

public class LeapEventTransformationException extends Exception {

	public LeapEventTransformationException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LeapEventTransformationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public LeapEventTransformationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public LeapEventTransformationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public LeapEventTransformationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6028776504111905739L;

	@Override
	public String getMessage() {
		return "RoiEvent object is null, Please check the event object";
	}
}
