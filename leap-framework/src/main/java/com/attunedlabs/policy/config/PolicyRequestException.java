package com.attunedlabs.policy.config;

public class PolicyRequestException extends Exception {

	public PolicyRequestException() {
		super();
	}

	public PolicyRequestException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public PolicyRequestException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public PolicyRequestException(String arg0) {
		super(arg0);
	}

	public PolicyRequestException(Throwable arg0) {
		super(arg0);
	}

}
