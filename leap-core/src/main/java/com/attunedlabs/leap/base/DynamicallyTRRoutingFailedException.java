package com.attunedlabs.leap.base;

public class DynamicallyTRRoutingFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	public DynamicallyTRRoutingFailedException() {
		super();
	}

	public DynamicallyTRRoutingFailedException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DynamicallyTRRoutingFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public DynamicallyTRRoutingFailedException(String message) {
		super(message);
	}

	public DynamicallyTRRoutingFailedException(Throwable cause) {
		super(cause);
	}

}
