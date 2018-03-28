package com.attunedlabs.leap.i18n.exception;

public class DBConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9023718809114186164L;

	public DBConnectionException() {
	}

	public DBConnectionException(String arg0) {
		super(arg0);
	}

	public DBConnectionException(Throwable arg0) {
		super(arg0);
	}

	public DBConnectionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public DBConnectionException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
