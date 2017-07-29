package com.getusroi.zookeeper.staticconfig.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.getusroi.staticconfig.StaticConfigInitializationException;
import com.getusroi.staticconfig.util.LocalfileUtil;
import com.getusroi.zookeeper.staticconfig.watcher.service.IZookeeperDataWatcherListner;


/**
 * Zookeeper Watcher processing and event monitoring..
 * 
 * @author Bizruntime
 *
 */
public class ZookeeperNodeDataWatcherService implements Watcher, StatCallback {

	static Logger logger = (Logger) LoggerFactory.getLogger(ZookeeperNodeDataWatcherService.class);
	private ZooKeeper zkeeper;
	private static String znode;
	private Watcher watcher;
	private IZookeeperDataWatcherListner izkDatalistner;
	byte[] prevData;
	public boolean dead;

	public String getZnode() {
		return znode;
	}

	public static void setZnode(String znode) {
		ZookeeperNodeDataWatcherService.znode = znode;
	}

	public ZookeeperNodeDataWatcherService(ZooKeeper zkeeper, String znode, Watcher watcher,
			IZookeeperDataWatcherListner izkDatalistner) {
		this.zkeeper = zkeeper;
		this.znode = znode;
		this.watcher = watcher;
		this.izkDatalistner = izkDatalistner;
		zkeeper.exists(znode, true, this, null);
	}

	/**
	 * zookeeper watcher handled here, where checking for the state of the
	 * clientConnection. Deciding if successfully connected, then to continue
	 * watching, else compulsory close
	 */
	@Override
	public void process(WatchedEvent event) {
		String path = event.getPath();
		logger.debug("=> Path :" + path);
		logger.debug("=> Event Type :" + event.getType());
		KeeperState zooState = event.getState();
		try {
			processStaticConfigOperation(event);
		} catch (StaticConfigInitializationException e) {
			logger.error("BaseDirectory processed for exception in Data watcher.." + e);
		}
		if (event.getType() == Event.EventType.None) {
			switch (zooState) {
			case SyncConnected:
				logger.debug("Again syncd...");
				break;
			case Expired:
				dead = true;//#TODO have to may be change the Deprecated Api
				izkDatalistner.closing(KeeperException.Code.SessionExpired);
				break;
			}
		} else {
			if (path != null && path.equals(getZnode())) {
				zkeeper.exists(getZnode(), true, this, null);
			} /*
				 * else if (path != null && !path.equals(getZnode())) {
				 * zkeeper.exists(getZnode(), true, this, null); }
				 */
		}
		if (watcher != null) {
			watcher.process(event);
		}
	}// ..end of the method

	/**
	 * Decided where if connection is successful get the data to which we set
	 * the watcher
	 */
	@Override
	public void processResult(int reasonCode, String path, Object ctx, Stat stat) {
		byte[] data = null;
		boolean exists;
		switch (reasonCode) {
		case Code.Ok://#TODO have to may be change the Deprecated Api
			exists = true;
			break;
		case Code.NoNode://#TODO have to may be change the Deprecated Api
			exists = false;
			break;
		case Code.SessionExpired://#TODO have to may be change the Deprecated Api
		case Code.NoAuth://#TODO have to may be change the Deprecated Api
			dead = true;
			izkDatalistner.closing(reasonCode);
			return;
		default:
			zkeeper.exists(getZnode(), true, this, null);
			return;
		}
		if ((data == null && data != prevData) || (data != null && !Arrays.equals(prevData, data))) {
			izkDatalistner.exists(data);
			prevData = data;
		}
	}// ..end of the method

	/**
	 * to process the static configurationFile changes
	 *
	 * @param event
	 * @throws StaticConfigInitializationException
	 */
	private void processStaticConfigOperation(WatchedEvent event) throws StaticConfigInitializationException {
		EventType eventType = event.getType();
		String nodePath = event.getPath();
		String[] fileNameFromznodeArr = nodePath.split("/");
		String fileNameFromznode = fileNameFromznodeArr[fileNameFromznodeArr.length - 1];
		String staticConfigPath = LocalfileUtil.loadStaticConfigProperty();
		String temp = "";
		for (int i = 0; i < fileNameFromznodeArr.length - 2; i++) {
			temp += "/" + fileNameFromznodeArr[i];
		}
		String directory = temp.replaceFirst("/", "");
		if (eventType.equals(Event.EventType.NodeDataChanged) || eventType.equals(Event.EventType.NodeDeleted)) {
			try {
				deleteFile(staticConfigPath + directory, fileNameFromznode);
			} catch (IOException e) {
				throw new StaticConfigInitializationException("Unable to delete file from local directory..", e);
			}
		}
	}// ..end of the method

	/**
	 * delete only file, not the directory
	 *
	 * @param filepath
	 * @param fileNameTodelete
	 * @throws IOException
	 */
	private synchronized void deleteFile(String filepath, String fileNameTodelete) throws IOException {
		logger.debug(filepath + " file-To-delete " + fileNameTodelete);
		File foldr = new File(filepath);
		File[] files = foldr.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				String fileName = file.getName();
				if (fileName.equals(fileNameTodelete)) {
					file.delete();
				}
			}
		}
	}// ..end of the method

}
