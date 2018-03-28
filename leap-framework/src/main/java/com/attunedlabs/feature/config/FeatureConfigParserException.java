package com.attunedlabs.feature.config;

/***
 * This class is custom Exception class for Feature
 * @author bizruntime
 *
 */
public class FeatureConfigParserException extends Exception {

	private static final long serialVersionUID = 5590173779055262634L;
	public FeatureConfigParserException() {
		super();
	}

	public FeatureConfigParserException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public FeatureConfigParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public FeatureConfigParserException(String message) {
		super(message);
		
	}

	public FeatureConfigParserException(Throwable cause) {
		super(cause);
	}
}
