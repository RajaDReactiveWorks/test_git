package com.getusroi.integrationfwk.activities.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteActivityException extends Exception {
	private Logger logger = LoggerFactory.getLogger(RouteActivityException.class.getName());
	public RouteActivityException() {
		super();
	}

	public RouteActivityException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RouteActivityException(String message, Throwable cause) {
		super(message, cause);
	}

	public RouteActivityException(String message) {
		logger.error("Error in pipeConfiguration : "+message);
	}

	public RouteActivityException(Throwable cause) {
		super(cause);
	}

}
