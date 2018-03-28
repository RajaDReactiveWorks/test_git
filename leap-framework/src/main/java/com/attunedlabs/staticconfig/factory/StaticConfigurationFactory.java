package com.attunedlabs.staticconfig.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.attunedlabs.staticconfig.IStaticConfigurationService;
import com.attunedlabs.staticconfig.StaticConfigInitializationException;
import com.attunedlabs.staticconfig.impl.FileStaticConfigurationServiceImpl;
import com.attunedlabs.zookeeper.staticconfig.service.impl.ZookeeperFilemanagementServiceImpl;

public class StaticConfigurationFactory {

	private IStaticConfigurationService iStaticConfigurationService;
	private static String staticConfigImpl;
	private static Properties properties = new Properties();
	static final String STATIC_FILECONFIG_IMPL_KEY = "staticconfigimpl";
	static final String STATIC_FILE_CONFIG_PROP_KEY = "globalAppDeploymentConfig.properties";
	static {
		InputStream zooFilestorePropStream = StaticConfigurationFactory.class.getClassLoader()
				.getResourceAsStream(STATIC_FILE_CONFIG_PROP_KEY);
		if (zooFilestorePropStream != null) {
			try {
				properties.load(zooFilestorePropStream);
				staticConfigImpl = (String) properties.get(STATIC_FILECONFIG_IMPL_KEY);
			} catch (IOException e) {

			}
		}
	}// ..end of static propertiesLoader

	/**
	 * Default constructor
	 */
	public StaticConfigurationFactory() {

	}

	/**
	 * Parameterized constructor kept , if actual Strategy pattern is
	 * implemented
	 * 
	 * @param iStaticConfigurationService
	 */
	public StaticConfigurationFactory(IStaticConfigurationService iStaticConfigurationService) {
		this.iStaticConfigurationService = iStaticConfigurationService;
	}

	/**
	 * Static method to get the instance of FileManagerServices
	 * 
	 * @param isZookeeperFilemanager
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws StaticConfigInitializationException 
	 */
	public static IStaticConfigurationService getFilemanagerInstance()
			throws InstantiationException, IllegalAccessException, StaticConfigInitializationException {
		if ("com.attunedlabs.staticconfig".equals(staticConfigImpl)) {
			return FileStaticConfigurationServiceImpl.class.newInstance();
		} else if("com.attunedlabs.zookeeper.staticconfig".equals(staticConfigImpl)) {
			return ZookeeperFilemanagementServiceImpl.class.newInstance();
		}else {
			throw new StaticConfigInitializationException("Unable to identify the implementation");
		}
	}// ..end of the static method
	
}
