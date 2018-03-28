package com.attunedlabs.leap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.reflect.macros.internal.macroImpl;




public class TenantSitePropertiesLoader {
final static Logger logger = LoggerFactory.getLogger(TenantSitePropertiesLoader.class);
	
	public static final String TENANT_SITE_LIST_FILE="globalAppDeploymentConfig.properties";
	
	public void setTenantAndSite(BundleContext context) throws TenantSitePropertiesLoadingException  {
		logger.debug(".setTenantAndSite method of TenantSitePropertiesLoader Bean");
		Bundle bundle=context.getBundle();
		logger.debug("bundle id : "+bundle.getBundleId()+", bundle name : "+bundle.getSymbolicName());
		Properties  tenantsiteList=loadingPropertiesFile(TENANT_SITE_LIST_FILE,bundle);
		LeapHeaderConstant.tenant=tenantsiteList.getProperty(LeapHeaderConstant.TENANT_KEY).trim();
		LeapHeaderConstant.site=tenantsiteList.getProperty(LeapHeaderConstant.SITE_KEY).trim();
		logger.debug("tenant : "+LeapHeaderConstant.tenant+", site : "+LeapHeaderConstant.site);
		logger.debug("exiting setTenantAndSite method of TenantSitePropertiesLoader Bean");

	}
	
	public void setTenantAndSite() throws TenantSitePropertiesLoadingException {
		logger.debug(".setTenantAndSite method of TenantSitePropertiesLoader Bean");
		Properties  tenantsiteList=loadingPropertiesFile(TENANT_SITE_LIST_FILE,null);
		LeapHeaderConstant.tenant=tenantsiteList.getProperty(LeapHeaderConstant.TENANT_KEY).trim();
		LeapHeaderConstant.site=tenantsiteList.getProperty(LeapHeaderConstant.SITE_KEY).trim();
		logger.debug("tenant : "+LeapHeaderConstant.tenant+", site : "+LeapHeaderConstant.site);

		logger.debug("exiting setTenantAndSite method of TenantSitePropertiesLoader Bean");

	}
	
	/**
	 * This method is used to load property file
	 * @param filetoload
	 * @return
	 * @throws UnableToLoadPropertiesException
	 */
	private Properties loadingPropertiesFile(String filetoload,Bundle bundle) throws TenantSitePropertiesLoadingException{
		logger.debug(".loadingPropertiesFile method of TenantSitePropertiesLoader Bean");

		Properties  prop=new Properties();
		InputStream input1=null;
		
		 input1 = TenantSitePropertiesLoader.class.getClassLoader().getResourceAsStream(filetoload);
		
		try {
			prop.load(input1);
		} catch (IOException e) {
			throw new TenantSitePropertiesLoadingException("unable to load property file = "+TENANT_SITE_LIST_FILE, e);
		}
		return prop;
	}
	
	
}
