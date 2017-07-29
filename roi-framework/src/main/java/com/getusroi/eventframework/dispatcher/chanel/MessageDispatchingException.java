package com.getusroi.eventframework.dispatcher.chanel;
/**
 * Exception thrown from the DispatcherChanel when it fails to dispatch a msg
 * @author bizruntime
 *
 */
public class MessageDispatchingException extends Exception {

	private static final long serialVersionUID = 3522568235156366620L;

	public MessageDispatchingException() {
		
	}

	public MessageDispatchingException(String message) {
		super(message);
	}

	public MessageDispatchingException(Throwable cause) {
		super(cause);
	}

	public MessageDispatchingException(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageDispatchingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
