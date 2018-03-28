package com.attunedlabs.integrationfwk.jdbcIntactivity.config;

public class JdbcIntActivityQueryProcessingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JdbcIntActivityQueryProcessingException() {
		super();
	}

	public JdbcIntActivityQueryProcessingException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JdbcIntActivityQueryProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public JdbcIntActivityQueryProcessingException(String message) {
		super(message);
	}

	public JdbcIntActivityQueryProcessingException(Throwable cause) {
		super(cause);
	}

}
