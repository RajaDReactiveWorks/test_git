package com.attunedlabs.security.exception;

public class AccountUpdateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2054050871314986396L;

	public AccountUpdateException() {
	}

	public AccountUpdateException(String message) {
		super(message);
	}

	public AccountUpdateException(Throwable cause) {
		super(cause);
	}

	public AccountUpdateException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccountUpdateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
