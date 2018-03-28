package com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence;

public class JdbcIntActivityPersistenceException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JdbcIntActivityPersistenceException() {
		super();
	}

	public JdbcIntActivityPersistenceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JdbcIntActivityPersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public JdbcIntActivityPersistenceException(String message) {
		super(message);
	}

	public JdbcIntActivityPersistenceException(Throwable cause) {
		super(cause);
	}

}
