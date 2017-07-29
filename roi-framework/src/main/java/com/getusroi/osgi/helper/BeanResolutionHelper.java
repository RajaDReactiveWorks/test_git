package com.getusroi.osgi.helper;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.core.BundleContextSingleton;

public class BeanResolutionHelper {
	final static Logger logger = LoggerFactory.getLogger(BeanResolutionHelper.class);

	public Object resolveBean(String interfaceName,String fqcnBuilder) throws InvalidSyntaxException{
		logger.debug(".resolveBean of BeanResolutionHelper : "+interfaceName+", fqcn builder : "+fqcnBuilder);
		//Object cacheObjectBuilder1=null;
		Object cacheObjectBuilder=null;

		BundleContext context=BundleContextSingleton.getBundleContext();
		logger.debug("bundle context : "+context);
		/*ServiceReference serRef=context.getServiceReference(interfaceName);
		System.out.println(serRef.getPropertyKeys());
		System.out.println("context object of policystore : "+serRef);
		cacheObjectBuilder1=(Object)context.getService(serRef);*/
		String filter = "(&(fqcnBuilder=" + fqcnBuilder + "))";
		ServiceReference[] serviceReferneces=context.getServiceReferences(interfaceName, filter);
		logger.debug("services avaliable : "+serviceReferneces);
		logger.debug("size of services avaliable : "+serviceReferneces.length);
		for(ServiceReference serviceReference:serviceReferneces){
			cacheObjectBuilder=(Object)context.getService(serviceReference);
			logger.debug("cache object in BeanResolutionHelper : "+cacheObjectBuilder);
		}
		logger.debug("exiting resolveBean of BeanResolutionHelper");

		return cacheObjectBuilder;
	}

}
