package com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper;

public class JdbcIntActivityExecutionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JdbcIntActivityExecutionException() {
		super();
	}

	public JdbcIntActivityExecutionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JdbcIntActivityExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public JdbcIntActivityExecutionException(String message) {
		super(message);
	}

	public JdbcIntActivityExecutionException(Throwable cause) {
		super(cause);
	}

}
