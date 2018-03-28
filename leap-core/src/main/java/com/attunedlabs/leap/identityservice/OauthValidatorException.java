package com.attunedlabs.leap.identityservice;

public class OauthValidatorException extends Exception {

	private static final long serialVersionUID = 1L;

	public OauthValidatorException() {
		super();
	}

	public OauthValidatorException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public OauthValidatorException(String message, Throwable cause) {
		super(message, cause);
	}

	public OauthValidatorException(String message) {
		super(message);
	}

	public OauthValidatorException(Throwable cause) {
		super(cause);
	}

}
