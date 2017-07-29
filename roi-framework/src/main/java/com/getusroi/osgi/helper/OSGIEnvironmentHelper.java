package com.getusroi.osgi.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class OSGIEnvironmentHelper {
	final static Logger logger = LoggerFactory.getLogger(OSGIEnvironmentHelper.class);
	
	public static final String OSGIENV_FILE="globalenv.properties";
	public static final String OSGIENV_ISENABLE_KEY="isOSGIEnabled";

	public static boolean isOSGIEnabled=false;
	

	
	public void setOSGIEnabled(BundleContext context) throws UnableToLoadOSGIPropertiesException{
		logger.debug(".setOSGIEnabled method of OSGIEnvironmentHelper Bean");
		Bundle bundle=context.getBundle();
		logger.debug("bundle id : "+bundle.getBundleId()+", bundle name : "+bundle.getSymbolicName());
		Properties  osgienvProp=loadingPropertiesFile(OSGIENV_FILE,bundle);
		String isOSGIEnabledAsString=osgienvProp.getProperty(OSGIENV_ISENABLE_KEY);
		if(isOSGIEnabledAsString.equalsIgnoreCase("true")){
			isOSGIEnabled=true;
		}
		logger.debug("exiting setOSGIEnabled method of OSGIEnvironmentHelper Bean");

	}
	
	/**
	 * This method is used to load property file
	 * @param filetoload
	 * @return
	 * @throws UnableToLoadPropertiesException
	 */
	private Properties loadingPropertiesFile(String filetoload,Bundle bundle) throws UnableToLoadOSGIPropertiesException{
		logger.debug(".loadingPropertiesFile method of OSGIEnvironmentHelper Bean");

		Properties  prop=new Properties();

		InputStream input1 = OSGIEnvironmentHelper.class.getClassLoader().getResourceAsStream(filetoload);
		try {
			prop.load(input1);
		} catch (IOException e) {
			throw new UnableToLoadOSGIPropertiesException("unable to load property file = "+OSGIENV_FILE, e);
		}
		return prop;
	}
}
