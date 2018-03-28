package com.attunedlabs.policy.config.impl;

public class PolicyInvalidRegexExpception extends Exception{
	
	private static final long serialVersionUID = -565140650508366682L;
	public PolicyInvalidRegexExpception() {
		super();
	}

	public PolicyInvalidRegexExpception(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public PolicyInvalidRegexExpception(String message, Throwable cause) {
		super(message, cause);
	}

	public PolicyInvalidRegexExpception(String message) {
		super(message);
		
	}

	public PolicyInvalidRegexExpception(Throwable cause) {
		super(cause);
	}


}
