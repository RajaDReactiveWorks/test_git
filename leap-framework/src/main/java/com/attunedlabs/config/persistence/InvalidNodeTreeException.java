package com.attunedlabs.config.persistence;

import com.attunedlabs.permastore.config.PermaStoreConfigurationException;

public class InvalidNodeTreeException extends PermaStoreConfigurationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3923694798909233648L;

	public InvalidNodeTreeException() {
		super();
		
	}

	public InvalidNodeTreeException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public InvalidNodeTreeException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public InvalidNodeTreeException(String arg0) {
		super(arg0);
	}

	public InvalidNodeTreeException(Throwable arg0) {
		super(arg0);
	}

}
