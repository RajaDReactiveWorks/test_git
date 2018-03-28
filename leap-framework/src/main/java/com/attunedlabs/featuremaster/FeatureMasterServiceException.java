package com.attunedlabs.featuremaster;

public class FeatureMasterServiceException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FeatureMasterServiceException() {
		super();
	}

	public FeatureMasterServiceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FeatureMasterServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public FeatureMasterServiceException(String message) {
		super(message);
	}

	public FeatureMasterServiceException(Throwable cause) {
		super(cause);
	}

}
