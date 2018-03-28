package com.attunedlabs.eventsubscription.exception;

public class NonRetryableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NonRetryableException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NonRetryableException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public NonRetryableException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public NonRetryableException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public NonRetryableException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
