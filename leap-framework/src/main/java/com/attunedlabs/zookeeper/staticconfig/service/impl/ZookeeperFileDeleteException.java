package com.attunedlabs.zookeeper.staticconfig.service.impl;

public class ZookeeperFileDeleteException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7726594267694628553L;

	public ZookeeperFileDeleteException() {
		super();
	}

	public ZookeeperFileDeleteException(String arg0, Throwable arg1,
			boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public ZookeeperFileDeleteException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ZookeeperFileDeleteException(String arg0) {
		super(arg0);
	}

	public ZookeeperFileDeleteException(Throwable arg0) {
		super(arg0);
	}

}
