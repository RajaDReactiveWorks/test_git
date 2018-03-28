package com.attunedlabs.eventframework.abstractbean;

public class UnsupprotedSQLFunctionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5604791976904194370L;

	public UnsupprotedSQLFunctionException() {
		super();
	}

	public UnsupprotedSQLFunctionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnsupprotedSQLFunctionException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupprotedSQLFunctionException(String message) {
		super(message);
	}

	public UnsupprotedSQLFunctionException(Throwable cause) {
		super(cause);
	}

}
