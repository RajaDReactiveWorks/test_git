package com.attunedlabs.ddlutils.config;

public class DbConfigNotfoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1938782188680629092L;

	public DbConfigNotfoundException() {
		super();
	}

	public DbConfigNotfoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DbConfigNotfoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public DbConfigNotfoundException(String message) {
		super(message);
	}

	public DbConfigNotfoundException(Throwable cause) {
		super(cause);
	}

}
