package com.attunedlabs.core;

import org.osgi.framework.BundleContext;

/**
 * This class is responsible for generating singleton object of BundleContext
 * @author bizruntime
 *
 */
public class BundleContextSingleton {
	
	private static BundleContext bundleContext;
	private static BundleContextSingleton bundlecontextSingleton;
	
	/**
	 * This method is used to created single object of BundleContext
	 * @return BundleContextSingleton object
	 */
	public static BundleContextSingleton getBundleContextSingleton(BundleContext context) {

		if (bundlecontextSingleton == null) {

			synchronized (BundleContextSingleton.class) {
				
				bundlecontextSingleton = new BundleContextSingleton(context);
			}
		}
		return bundlecontextSingleton;
	}
	
	/**
	 * Private constructor to support singleon pattern
	 * @param bundleContext
	 */
	private BundleContextSingleton(BundleContext bundleContext){
		this.bundleContext=bundleContext;
	}
	
	
	/**
	 * This methis is used to return the BundleContext object
	 * @return BundleContext object
	 */
	public static BundleContext getBundleContext() {
		return bundleContext;
		
	}

}
