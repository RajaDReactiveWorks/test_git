package com.attunedlabs.integrationfwk.config;

public class InvalidIntegrationPipelineConfigException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public InvalidIntegrationPipelineConfigException(){
		super();
	}
	
	public InvalidIntegrationPipelineConfigException(String message){
		super(message);
	}
	public InvalidIntegrationPipelineConfigException(String message,Throwable cause){
		super(message,cause);
	}
	public InvalidIntegrationPipelineConfigException(Throwable cause){
		super(cause);
	}
	public InvalidIntegrationPipelineConfigException(String message,Throwable cause,boolean enableSuppression,boolean writableStrackTrace){
		super(message,cause,enableSuppression,writableStrackTrace);
	}

}
