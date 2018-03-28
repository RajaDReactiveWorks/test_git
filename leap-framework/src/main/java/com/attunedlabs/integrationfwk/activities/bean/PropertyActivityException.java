package com.attunedlabs.integrationfwk.activities.bean;

public class PropertyActivityException extends Exception {

	public PropertyActivityException() {
		super();
	}

	public PropertyActivityException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PropertyActivityException(String message, Throwable cause) {
		super(message, cause);
	}

	public PropertyActivityException(String message) {
		super(message);
	}

	public PropertyActivityException(Throwable cause) {
		super(cause);
	}

}
