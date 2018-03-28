package com.attunedlabs.permastore.config;

public class PermaStoreConfigParserException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2479557273659967015L;

	public PermaStoreConfigParserException() {
		super();
	}

	public PermaStoreConfigParserException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public PermaStoreConfigParserException(String message, Throwable cause) {
		super(message, cause);
	
	}

	public PermaStoreConfigParserException(String message) {
		super(message);
	
	}

	public PermaStoreConfigParserException(Throwable cause) {
		super(cause);
		
	}

}
