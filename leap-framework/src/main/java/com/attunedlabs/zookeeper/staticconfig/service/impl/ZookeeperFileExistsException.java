package com.attunedlabs.zookeeper.staticconfig.service.impl;

public class ZookeeperFileExistsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1810170705315198812L;

	public ZookeeperFileExistsException() {
		super();
	}

	public ZookeeperFileExistsException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ZookeeperFileExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZookeeperFileExistsException(String message) {
		super(message);
	}

	public ZookeeperFileExistsException(Throwable cause) {
		super(cause);
	}

}
