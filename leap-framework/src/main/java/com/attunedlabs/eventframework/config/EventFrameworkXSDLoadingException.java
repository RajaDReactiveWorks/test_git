package com.attunedlabs.eventframework.config;

public class EventFrameworkXSDLoadingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9056860244968314343L;

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return "event framework XSD doesnot exist in classpath";
	}
}
