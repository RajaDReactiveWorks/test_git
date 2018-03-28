package com.attunedlabs.staticconfig.impl;

public class AccessProtectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8635185201686461181L;

	public AccessProtectionException() {
		super();
	}

	public AccessProtectionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AccessProtectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccessProtectionException(String message) {
		super(message);
	}

	public AccessProtectionException(Throwable cause) {
		super(cause);
	}

}
