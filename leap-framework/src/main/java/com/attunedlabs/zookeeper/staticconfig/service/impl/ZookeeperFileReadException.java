package com.attunedlabs.zookeeper.staticconfig.service.impl;

public class ZookeeperFileReadException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 201351141253197520L;

	public ZookeeperFileReadException() {
		super();
	}

	public ZookeeperFileReadException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ZookeeperFileReadException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZookeeperFileReadException(String message) {
		super(message);
	}

	public ZookeeperFileReadException(Throwable cause) {
		super(cause);
	}

}
