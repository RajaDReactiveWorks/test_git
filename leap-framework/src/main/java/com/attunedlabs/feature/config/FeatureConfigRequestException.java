package com.attunedlabs.feature.config;

public class FeatureConfigRequestException extends Exception {

	private static final long serialVersionUID = 7315529025123130906L;
	
	public FeatureConfigRequestException() {
		super();
	}

	public FeatureConfigRequestException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public FeatureConfigRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public FeatureConfigRequestException(String message) {
		super(message);
		
	}

	public FeatureConfigRequestException(Throwable cause) {
		super(cause);
	}

}
