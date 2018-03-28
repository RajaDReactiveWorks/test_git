package com.attunedlabs.leap.i18n.exception;

public class LocaleResolverException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5755238312609948715L;

	public LocaleResolverException() {
	}

	public LocaleResolverException(String message) {
		super(message);
	}

	public LocaleResolverException(Throwable cause) {
		super(cause);
	}

	public LocaleResolverException(String message, Throwable cause) {
		super(message, cause);
	}

	public LocaleResolverException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
