package com.attunedlabs.zookeeper.staticconfig.watcher.service;

public interface IZookeeperDataWatcherListner {
	
	/**
	 * The existence status of the node has changed.
	 */
	void exists(byte data[]);

	/**
	 * The ZooKeeper session is no longer valid.
	 *
	 * @param rc
	 *            the ZooKeeper reason code
	 */
	void closing(int reasonCode);
}
