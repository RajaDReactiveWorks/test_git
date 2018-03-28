package com.attunedlabs.eventsubscription.exception;

public class ConfigurationValidationFailedException extends NonRetryableException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3196969192626536953L;

	public ConfigurationValidationFailedException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ConfigurationValidationFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public ConfigurationValidationFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ConfigurationValidationFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ConfigurationValidationFailedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
