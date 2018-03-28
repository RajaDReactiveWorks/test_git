package com.attunedlabs.eventframework.dispatcher;

public class EventFrameworkDispatcherException extends Exception {
	private static final long serialVersionUID = 2942360780463294560L;

	public EventFrameworkDispatcherException() {
	}

	public EventFrameworkDispatcherException(String message) {
		super(message);
	}

	public EventFrameworkDispatcherException(Throwable cause) {
		super(cause);
	}

	public EventFrameworkDispatcherException(String message, Throwable cause) {
		super(message, cause);
	}

	public EventFrameworkDispatcherException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
