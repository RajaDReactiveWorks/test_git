package com.attunedlabs.security.exception;

public class AccountFetchException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3817426865053539569L;

	public AccountFetchException() {
	}

	public AccountFetchException(String message) {
		super(message);
	}

	public AccountFetchException(Throwable cause) {
		super(cause);
	}

	public AccountFetchException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccountFetchException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
