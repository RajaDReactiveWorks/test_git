package com.attunedlabs.dynastore;

public class DynaStoreRequestException extends Exception {

	public DynaStoreRequestException() {
	}

	public DynaStoreRequestException(String message) {
		super(message);
	}

	public DynaStoreRequestException(Throwable cause) {
		super(cause);
	}

	public DynaStoreRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public DynaStoreRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
