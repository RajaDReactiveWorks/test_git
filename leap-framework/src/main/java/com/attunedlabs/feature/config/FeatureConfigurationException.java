package com.attunedlabs.feature.config;

public class FeatureConfigurationException extends Exception {

	private static final long serialVersionUID = 4345974593993032614L;

	public FeatureConfigurationException() {
		super();
	}

	public FeatureConfigurationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public FeatureConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public FeatureConfigurationException(String message) {
		super(message);
		
	}

	public FeatureConfigurationException(Throwable cause) {
		super(cause);
	}
}
