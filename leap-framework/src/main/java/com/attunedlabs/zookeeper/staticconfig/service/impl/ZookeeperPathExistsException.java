package com.attunedlabs.zookeeper.staticconfig.service.impl;

public class ZookeeperPathExistsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9005421731326368123L;

	public ZookeeperPathExistsException() {
		super();
	}

	public ZookeeperPathExistsException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ZookeeperPathExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZookeeperPathExistsException(String message) {
		super(message);
	}

	public ZookeeperPathExistsException(Throwable cause) {
		super(cause);
	}

}
