package com.attunedlabs.security.exception;

public class DigestMakeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -988669575726497821L;

	public DigestMakeException() {
	}

	public DigestMakeException(String message) {
		super(message);
	}

	public DigestMakeException(Throwable cause) {
		super(cause);
	}

	public DigestMakeException(String message, Throwable cause) {
		super(message, cause);
	}

	public DigestMakeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
