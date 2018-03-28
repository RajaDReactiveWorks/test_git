package com.attunedlabs.security.exception;

public class TenantTokenValidationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3979152203142379947L;

	public TenantTokenValidationException() {
	}

	public TenantTokenValidationException(String message) {
		super(message);
	}

	public TenantTokenValidationException(Throwable cause) {
		super(cause);
	}

	public TenantTokenValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public TenantTokenValidationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
