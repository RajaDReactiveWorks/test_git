package com.attunedlabs.integrationfwk.config;

public class IntegrationPipelineConfigException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IntegrationPipelineConfigException() {
		super();
	}

	public IntegrationPipelineConfigException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IntegrationPipelineConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public IntegrationPipelineConfigException(String message) {
		super(message);
	}

	public IntegrationPipelineConfigException(Throwable cause) {
		super(cause);
	}

}
