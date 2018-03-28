package com.attunedlabs.osgi.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.core.BundleContextSingleton;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.TenantSitePropertiesLoader;
import com.attunedlabs.osgi.helper.OSGIEnvironmentHelper;


public class RoiActivator implements BundleActivator {
	final static Logger logger = LoggerFactory.getLogger(RoiActivator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		logger.debug(".start() of RoiActivator");
		BundleContextSingleton.getBundleContextSingleton(context);
		OSGIEnvironmentHelper osgiEnvHelper=new OSGIEnvironmentHelper();
		osgiEnvHelper.setOSGIEnabled(context);
		logger.debug("isOSGIENabled : "+osgiEnvHelper.isOSGIEnabled);
		logger.debug("bundle context got initialized : "+BundleContextSingleton.getBundleContext());
		TenantSitePropertiesLoader tenantSitePropLoader=new TenantSitePropertiesLoader();
		tenantSitePropLoader.setTenantAndSite(context);
		logger.debug("tenant : "+LeapHeaderConstant.tenant+", site : "+LeapHeaderConstant.site);
		logger.debug("exiting start() of RoiActivator");

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		logger.debug(".stop() of RoiActivator");

	}

}
