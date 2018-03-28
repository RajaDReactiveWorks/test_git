package com.attunedlabs.config.util;

public class PropertiesConfigException extends Exception {

	private static final long serialVersionUID = 1L;

	public PropertiesConfigException() {
		super();
	}

	public PropertiesConfigException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PropertiesConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public PropertiesConfigException(String message) {
		super(message);
	}

	public PropertiesConfigException(Throwable cause) {
		super(cause);
	}

}
