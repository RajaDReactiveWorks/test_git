package com.attunedlabs.leap.identityservice;

public class IdentityServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	public IdentityServiceException() {
		super();
	}

	public IdentityServiceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IdentityServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public IdentityServiceException(String message) {
		super(message);
	}

	public IdentityServiceException(Throwable cause) {
		super(cause);
	}

}
