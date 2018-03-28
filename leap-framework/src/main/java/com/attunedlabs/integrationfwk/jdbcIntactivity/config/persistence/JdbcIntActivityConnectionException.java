package com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence;

public class JdbcIntActivityConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JdbcIntActivityConnectionException() {
		super();
	}

	public JdbcIntActivityConnectionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JdbcIntActivityConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public JdbcIntActivityConnectionException(String message) {
		super(message);
	}

	public JdbcIntActivityConnectionException(Throwable cause) {
		super(cause);
	}

}
