package com.integration.exception;

public class PermaStoreDoesNotExistException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PermaStoreDoesNotExistException() {
	}

	public PermaStoreDoesNotExistException(String message) {
		super(message);
	}

	public PermaStoreDoesNotExistException(Throwable cause) {
		super(cause);
	}

	public PermaStoreDoesNotExistException(String message, Throwable cause) {
		super(message, cause);
	}

	public PermaStoreDoesNotExistException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}