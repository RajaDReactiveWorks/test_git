package com.attunedlabs.dynastore.config.impl;

public class ConfigDynaStoreInitializationException extends Exception {
	private static final long serialVersionUID = -3707381420703805467L;

	public ConfigDynaStoreInitializationException() {
		super();
	}

	public ConfigDynaStoreInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConfigDynaStoreInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigDynaStoreInitializationException(String message) {
		super(message);
	}

	public ConfigDynaStoreInitializationException(Throwable cause) {
		super(cause);
	}

}
