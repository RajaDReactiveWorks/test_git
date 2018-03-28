package com.attunedlabs.eventsubscription.exception;

public class RouteInvocationException extends NonRetryableException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RouteInvocationException() {
		// TODO Auto-generated constructor stub
	}

	public RouteInvocationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public RouteInvocationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public RouteInvocationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public RouteInvocationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
