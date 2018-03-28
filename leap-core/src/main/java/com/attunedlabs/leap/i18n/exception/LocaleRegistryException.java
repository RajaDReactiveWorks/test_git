package com.attunedlabs.leap.i18n.exception;

public class LocaleRegistryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6595092968340082707L;

	public LocaleRegistryException() {
	}

	public LocaleRegistryException(String message) {
		super(message);
	}

	public LocaleRegistryException(Throwable cause) {
		super(cause);
	}

	public LocaleRegistryException(String message, Throwable cause) {
		super(message, cause);
	}

	public LocaleRegistryException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
