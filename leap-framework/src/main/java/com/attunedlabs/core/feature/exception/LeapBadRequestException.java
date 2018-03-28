package com.attunedlabs.core.feature.exception;

public class LeapBadRequestException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1513115875408348059L;
	public static final Integer RESPONSE_CODE = 400;

	private String developerMessage;
	private long appErrorCode;
	private String userMessage;
	private String feature;
	private long vendorErrorCode;
	private String vendorErrorMessage;

	public LeapBadRequestException(String message, Throwable cause) {
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

	public long getVendorErrorCode() {
		return vendorErrorCode;
	}

	public void setVendorErrorCode(long vendorErrorCode) {
		this.vendorErrorCode = vendorErrorCode;
	}

	public String getVendorErrorMessage() {
		return vendorErrorMessage;
	}

	public void setVendorErrorMessage(String vendorErrorMessage) {
		this.vendorErrorMessage = vendorErrorMessage;
	}
	
}
