package com.attunedlabs.config.persistence;

public class ConfigPersistenceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8823151785972577790L;

	public ConfigPersistenceException() {
		super();
	}

	public ConfigPersistenceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public ConfigPersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigPersistenceException(String message) {
		super(message);
		
	}

	public ConfigPersistenceException(Throwable cause) {
		super(cause);
	}

}
