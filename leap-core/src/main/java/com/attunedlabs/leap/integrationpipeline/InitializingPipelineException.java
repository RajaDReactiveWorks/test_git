package com.attunedlabs.leap.integrationpipeline;

public class InitializingPipelineException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InitializingPipelineException() {
		super();
	}

	public InitializingPipelineException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InitializingPipelineException(String message, Throwable cause) {
		super(message, cause);
	}

	public InitializingPipelineException(String message) {
		super(message);
	}

	public InitializingPipelineException(Throwable cause) {
		super(cause);
	}

}
