package com.attunedlabs.featureInstaller.servicetracker;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class LeapCustomServiceTacker implements
ServiceTrackerCustomizer{
	final static Logger logger = LoggerFactory.getLogger(LeapCustomServiceTacker.class);

	  private final BundleContext bundleContext;
	public LeapCustomServiceTacker(BundleContext bundleContext) {
		this.bundleContext=bundleContext;
}

	@Override
	public Object addingService(ServiceReference reference) {
		logger.debug(".addingService() of RoiCustomServiceTacker");
		Object serviceobject=(Object)bundleContext.getService(reference);
		logger.debug("object which is added to servicetracker : "+serviceobject);
		logger.debug("exiting addingService() of RoiCustomServiceTacker");
		return serviceobject;
	}

	@Override
	public void modifiedService(ServiceReference reference, Object service) {
		// TODO doesnot has provide any impl yet
		
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		logger.debug(".removedService() of RoiCustomServiceTacker");
		logger.debug("AnotherObject : "+service);
		bundleContext.ungetService(reference);
		logger.debug("How sad. Service  is gone");	
		logger.debug("exiting removedService() of RoiCustomServiceTacker");

	}

}
