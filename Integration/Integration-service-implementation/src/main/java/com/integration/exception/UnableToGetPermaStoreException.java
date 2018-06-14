package com.integration.exception;

public class UnableToGetPermaStoreException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnableToGetPermaStoreException() {
	}

	public UnableToGetPermaStoreException(String message) {
		super(message);
	}

	public UnableToGetPermaStoreException(Throwable cause) {
		super(cause);
	}

	public UnableToGetPermaStoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnableToGetPermaStoreException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}