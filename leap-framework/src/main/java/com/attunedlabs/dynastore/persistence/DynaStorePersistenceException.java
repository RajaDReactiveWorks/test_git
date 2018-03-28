package com.attunedlabs.dynastore.persistence;

public class DynaStorePersistenceException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6255156148880465141L;

	public DynaStorePersistenceException() {
		super();
	}

	public DynaStorePersistenceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DynaStorePersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public DynaStorePersistenceException(String message) {
		super(message);
	}

	public DynaStorePersistenceException(Throwable cause) {
		super(cause);
	}

}
