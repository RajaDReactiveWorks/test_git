package com.attunedlabs.policy.config.impl;

public class PolicyFactBuilderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -565140650508366682L;
	public PolicyFactBuilderException() {
		super();
	}

	public PolicyFactBuilderException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public PolicyFactBuilderException(String message, Throwable cause) {
		super(message, cause);
	}

	public PolicyFactBuilderException(String message) {
		super(message);
		
	}

	public PolicyFactBuilderException(Throwable cause) {
		super(cause);
	}

}
