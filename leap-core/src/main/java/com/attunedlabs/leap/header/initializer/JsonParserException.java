package com.attunedlabs.leap.header.initializer;

public class JsonParserException extends Exception {
	private static final long serialVersionUID = 1L;

	public JsonParserException() {
		super();
	}

	public JsonParserException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JsonParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsonParserException(String message) {
		super(message);
	}

	public JsonParserException(Throwable cause) {
		super(cause);
	}

}
