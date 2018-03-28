package com.attunedlabs.zookeeper.staticconfig.service.impl;

public class InvalidFilePathException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -92231011092081512L;

	public InvalidFilePathException() {
		super();
	}

	public InvalidFilePathException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidFilePathException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidFilePathException(String message) {
		super(message);
	}

	public InvalidFilePathException(Throwable cause) {
		super(cause);
	}

}
