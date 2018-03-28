package com.attunedlabs.integrationfwk.activities.bean;

public class ActivityEnricherException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ActivityEnricherException() {
		super();
	}

	public ActivityEnricherException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ActivityEnricherException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActivityEnricherException(String message) {
		super(message);
	}

	public ActivityEnricherException(Throwable cause) {
		super(cause);
	}

}
