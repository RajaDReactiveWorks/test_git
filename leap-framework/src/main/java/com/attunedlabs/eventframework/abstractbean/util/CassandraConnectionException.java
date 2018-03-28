package com.attunedlabs.eventframework.abstractbean.util;

public class CassandraConnectionException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8280377612377969936L;

	public CassandraConnectionException() {
		super();
	}

	public CassandraConnectionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CassandraConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public CassandraConnectionException(String message) {
		super(message);
	}

	public CassandraConnectionException(Throwable cause) {
		super(cause);
	}
	
	

}
