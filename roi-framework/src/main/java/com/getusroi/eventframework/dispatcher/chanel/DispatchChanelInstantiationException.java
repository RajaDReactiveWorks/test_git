package com.getusroi.eventframework.dispatcher.chanel;

public class DispatchChanelInstantiationException extends Exception {

	private static final long serialVersionUID = -4532193568410942468L;

	public DispatchChanelInstantiationException(Exception rootException){
		super(rootException);
	}

	public DispatchChanelInstantiationException() {
		super();
	
	}

	public DispatchChanelInstantiationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public DispatchChanelInstantiationException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public DispatchChanelInstantiationException(String message) {
		super(message);
		
	}

	public DispatchChanelInstantiationException(Throwable cause) {
		super(cause);
		
	}
	
	
}
