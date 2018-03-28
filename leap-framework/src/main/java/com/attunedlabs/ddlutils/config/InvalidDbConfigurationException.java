package com.attunedlabs.ddlutils.config;

public class InvalidDbConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1663946582790447864L;

	public InvalidDbConfigurationException() {
		super();
	}

	public InvalidDbConfigurationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidDbConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidDbConfigurationException(String message) {
		super(message);
	}

	public InvalidDbConfigurationException(Throwable cause) {
		super(cause);
	}

}
