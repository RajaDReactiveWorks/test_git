package com.attunedlabs.zookeeper.staticconfig.service.impl;

public class ZookeeperFileNotFoudException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7295306334688450285L;

	public ZookeeperFileNotFoudException() {
		super();
	}

	public ZookeeperFileNotFoudException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ZookeeperFileNotFoudException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZookeeperFileNotFoudException(String message) {
		super(message);
	}

	public ZookeeperFileNotFoudException(Throwable cause) {
		super(cause);
	}

}
