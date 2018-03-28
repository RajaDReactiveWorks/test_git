package com.attunedlabs.config.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.integrationfwk.groovyactivity.config.GroovyScriptUtil;

public class LeapConfigUtil {

	final static Logger logger = LoggerFactory.getLogger(LeapConfigUtil.class);
	private static final String APPS_DEPLOYMENT_ENV_CONFIG = "globalAppDeploymentConfig.properties";
	private static	Properties properties = new Properties();

	static {
		logger.debug(".getLoadProperties method of LeapConfigurationUtil");
		InputStream inputStream = LeapConfigUtil.class.getClassLoader().getResourceAsStream(APPS_DEPLOYMENT_ENV_CONFIG);
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			logger.debug("unable to load property file = " + APPS_DEPLOYMENT_ENV_CONFIG);
			e.printStackTrace();
		}
	}
	/**
	 * To load the properties file
	 * 
	 * @return Properties Object
	 * @throws PropertiesConfigException
	 */
	public static Properties loadGlobalAppDeploymentConfigProperties() throws PropertiesConfigException {
		return properties;

	}// end of method loadingPropertiesFile

}
