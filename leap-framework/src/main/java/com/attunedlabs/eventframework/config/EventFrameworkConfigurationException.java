package com.attunedlabs.eventframework.config;

public class EventFrameworkConfigurationException extends Exception {
	private static final long serialVersionUID = 1L;

	public EventFrameworkConfigurationException() {
	}

	public EventFrameworkConfigurationException(String message) {
		super(message);
		
	}

	public EventFrameworkConfigurationException(Throwable cause) {
		super(cause);
	}

	public EventFrameworkConfigurationException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public EventFrameworkConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
