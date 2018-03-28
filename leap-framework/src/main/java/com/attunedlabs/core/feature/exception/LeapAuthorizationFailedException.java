package com.attunedlabs.core.feature.exception;

public class LeapAuthorizationFailedException extends Exception {

	/**
	 * Error code and message are mandatory and applicable for all the classes
	 */
	private static final long serialVersionUID = -4997126706470012576L;
	public static final Integer RESPONSE_CODE = 403;

	private String developerMessage;
	private long appErrorCode;
	private String userMessage;
	private String feature;

	public LeapAuthorizationFailedException(String message, Throwable cause) {
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
