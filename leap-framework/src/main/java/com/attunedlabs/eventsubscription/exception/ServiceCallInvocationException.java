package com.attunedlabs.eventsubscription.exception;

public class ServiceCallInvocationException extends RetryableException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceCallInvocationException() {
		// TODO Auto-generated constructor stub
	}

	public ServiceCallInvocationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ServiceCallInvocationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public ServiceCallInvocationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ServiceCallInvocationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
