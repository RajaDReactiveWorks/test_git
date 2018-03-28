package com.attunedlabs.security.exception;

public class AccountRegistrationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3056269092850390103L;

	public AccountRegistrationException() {
		super();
	}

	public AccountRegistrationException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public AccountRegistrationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AccountRegistrationException(String message) {
		super(message);
	}

	public AccountRegistrationException(Throwable cause) {
		super(cause);
	}

}
