package com.attunedlabs.core.feature.exception;

public class LeapValidationFailureException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8040881198316922997L;
	public static final Integer RESPONSE_CODE = 200;

	private String developerMessage;
	private long appErrorCode;
	private String userMessage;
	private String feature;

	public LeapValidationFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getDeveloperMessage() {
		return developerMessage;
	}

	public void setDeveloperMessage(String developerMessage) {
		this.developerMessage = developerMessage;
	}

	public long getAppErrorCode() {
		return appErrorCode;
	}

	public void setAppErrorCode(long appErrorCode) {
		this.appErrorCode = appErrorCode;
	}

	public String getUserMessage() {
		return userMessage;
	}

	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}
}
