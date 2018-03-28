package com.attunedlabs.staticconfig;

public class StaticConfigUpdateException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4981377396761432302L;
	public StaticConfigUpdateException() {
		super();
	}

	public StaticConfigUpdateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StaticConfigUpdateException(String message, Throwable cause) {
		super(message, cause);
	}

	public StaticConfigUpdateException(String message) {
		super(message);
	}

	public StaticConfigUpdateException(Throwable cause) {
		super(cause);
	}

}
