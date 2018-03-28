package com.attunedlabs.leap.identityservice;

public class TrustValidationException extends Exception {

	private static final long serialVersionUID = 1L;

	public TrustValidationException() {
		super();
	}

	public TrustValidationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TrustValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public TrustValidationException(String message) {
		super(message);
	}

	public TrustValidationException(Throwable cause) {
		super(cause);
	}

}
