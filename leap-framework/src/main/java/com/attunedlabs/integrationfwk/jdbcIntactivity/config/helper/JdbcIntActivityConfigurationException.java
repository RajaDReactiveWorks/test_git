package com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper;

public class JdbcIntActivityConfigurationException extends Exception {

	/**
	 * Used when object configuration fails of any reason
	 * 
	 * @author Bizruntime
	 */
	private static final long serialVersionUID = 1L;

	public JdbcIntActivityConfigurationException() {
		super();
	}

	public JdbcIntActivityConfigurationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JdbcIntActivityConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public JdbcIntActivityConfigurationException(String message) {
		super(message);
	}

	public JdbcIntActivityConfigurationException(Throwable cause) {
		super(cause);
	}

}
