package com.attunedlabs.integrationfwk.activities.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.integrationfwk.jdbcIntactivity.config.JdbcIntActivityConfigurationQueryProcessor;

public class EmailNotifierException extends Exception {
	private final Logger logger = LoggerFactory.getLogger(JdbcIntActivityConfigurationQueryProcessor.class.getName());

	public EmailNotifierException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public EmailNotifierException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public EmailNotifierException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public EmailNotifierException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public EmailNotifierException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
}
