package com.attunedlabs.zookeeper.staticconfig.service.impl;

public class ZookeeperFileUpdateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -800892931349161617L;

	public ZookeeperFileUpdateException() {
		super();
	}

	public ZookeeperFileUpdateException(String arg0, Throwable arg1,
			boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public ZookeeperFileUpdateException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ZookeeperFileUpdateException(String arg0) {
		super(arg0);
	}

	public ZookeeperFileUpdateException(Throwable arg0) {
		super(arg0);
	}

}
