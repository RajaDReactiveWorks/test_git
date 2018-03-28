package com.attunedlabs.featuredeployment;

public class FeatureDeploymentServiceException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7607502332270413103L;

	public FeatureDeploymentServiceException() {
		super();
	}

	public FeatureDeploymentServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FeatureDeploymentServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public FeatureDeploymentServiceException(String message) {
		super(message);
	}

	public FeatureDeploymentServiceException(Throwable cause) {
		super(cause);
	}
	
	

}
