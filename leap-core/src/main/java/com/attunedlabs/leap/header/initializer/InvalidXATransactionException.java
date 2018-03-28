package com.attunedlabs.leap.header.initializer;

public class InvalidXATransactionException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidXATransactionException() {
		super();
	}

	public InvalidXATransactionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidXATransactionException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidXATransactionException(String message) {
		super(message);
	}

	public InvalidXATransactionException(Throwable cause) {
		super(cause);
	}

}
