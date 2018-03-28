package com.attunedlabs.leap.identityservice;

public class ValidationFailedException extends Exception {
	private static final long serialVersionUID = 1L;

	public ValidationFailedException() {
		super();
	}

	public ValidationFailedException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ValidationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidationFailedException(String message) {
		super(message);
	}

	public ValidationFailedException(Throwable cause) {
		super(cause);
	}

}
