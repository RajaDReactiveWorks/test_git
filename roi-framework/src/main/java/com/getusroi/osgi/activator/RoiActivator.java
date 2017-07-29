package com.getusroi.osgi.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.core.BundleContextSingleton;
import com.getusroi.mesh.MeshHeaderConstant;
import com.getusroi.mesh.TenantSitePropertiesLoader;
import com.getusroi.osgi.helper.OSGIEnvironmentHelper;


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
		logger.debug("tenant : "+MeshHeaderConstant.tenant+", site : "+MeshHeaderConstant.site);
		logger.debug("exiting start() of RoiActivator");

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		logger.debug(".stop() of RoiActivator");

	}

}
