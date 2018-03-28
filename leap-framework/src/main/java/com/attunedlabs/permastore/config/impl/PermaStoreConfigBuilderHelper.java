package com.attunedlabs.permastore.config.impl;

import java.io.Serializable;

import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.core.SQLCacheBuilderException;
import com.attunedlabs.config.core.SQLCacheObjectBuilder;
import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.core.BeanDependencyResolverFactory;
import com.attunedlabs.core.IBeanDependencyResolver;
import com.attunedlabs.osgi.helper.BeanResolutionHelper;
import com.attunedlabs.osgi.helper.OSGIEnvironmentHelper;
import com.attunedlabs.permastore.config.IPermaStoreCustomCacheObjectBuilder;
import com.attunedlabs.permastore.config.PermaStoreConfigurationBuilderException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationConstant;
import com.attunedlabs.permastore.config.jaxb.ConfigurationBuilder;
import com.attunedlabs.permastore.config.jaxb.CustomBuilder;

/**
 * Helper class for calling an handling the process of
 * building the cache Object
 * @author bizruntime
 *
 */
public class PermaStoreConfigBuilderHelper {
	final Logger logger = LoggerFactory.getLogger(PermaStoreConfigBuilderHelper.class);
	private PermaStoreInLineCacheObjectBuilder inlineConfigBuilder;
	
	public PermaStoreConfigBuilderHelper(){
		
	}
	
	/**
	 * Handles the Building of Cacheable Object based on the configuration 
	 * given in the xml config.
	 * 
	 * @param configBuilder
	 * @return
	 * @throws PermaStoreConfigurationBuilderException
	 */
	public Serializable handleConfigurationBuilder(ConfigurationBuilder configBuilder) throws PermaStoreConfigurationBuilderException {
		logger.debug(".handleConfigurationBuilder method of Permastore");
		String psCacheBuilderType=configBuilder.getType().value();
		logger.debug("psCacheBuilderType : "+psCacheBuilderType);
		Serializable objToCache=null;
		if(psCacheBuilderType.equalsIgnoreCase(PermaStoreConfigurationConstant.CONFIGBUILDER_CUSTOM)){
			logger.debug("psCacheBuilderType is custom type");
			CustomBuilder customBuilderConfig=configBuilder.getCustomBuilder();
			IPermaStoreCustomCacheObjectBuilder customBuilder=null;
			//It is possible the class defined in config is incorrect or not resolvable
			logger.debug("OSGIEnvironmentHelper.isOSGIEnabled : "+OSGIEnvironmentHelper.isOSGIEnabled);

			if(OSGIEnvironmentHelper.isOSGIEnabled){
				BeanResolutionHelper beanResolutionHelper=new BeanResolutionHelper();
				try {
					customBuilder=(IPermaStoreCustomCacheObjectBuilder)beanResolutionHelper.resolveBean(IPermaStoreCustomCacheObjectBuilder.class.getName(),customBuilderConfig.getBuilder());
				} catch (InvalidSyntaxException e) {
					throw new PermaStoreConfigurationBuilderException("Unable to Load/instantiate PermastoreCustomBuilder="+customBuilderConfig.getBuilder(),e);
				}
			}else{
			try{
				IBeanDependencyResolver beanResolver=BeanDependencyResolverFactory.getBeanDependencyResolver();
				customBuilder=(IPermaStoreCustomCacheObjectBuilder)beanResolver.getBeanInstance(IPermaStoreCustomCacheObjectBuilder.class, customBuilderConfig.getBuilder());
			}catch(BeanDependencyResolveException beanResolveExp){
				throw new PermaStoreConfigurationBuilderException("Unable to Load/instantiate PermastoreCustomBuilder="+customBuilderConfig.getBuilder(),beanResolveExp);
			}
			}
			if(customBuilder==null)
				throw new PermaStoreConfigurationBuilderException("Unable to Load/instantiate PermastoreCustomBuilder="+customBuilderConfig.getBuilder());
			//Call custom builder and build the object to be cached
			objToCache=customBuilder.loadDataForCache(customBuilderConfig);
			return objToCache;
		}else if(psCacheBuilderType.equalsIgnoreCase(PermaStoreConfigurationConstant.CONFIGBUILDER_INLINE) ){
			if(inlineConfigBuilder==null){
				inlineConfigBuilder=new PermaStoreInLineCacheObjectBuilder();
			}
			objToCache=inlineConfigBuilder.loadDataForCache(configBuilder.getInlineBuilder());
			return objToCache;
		}else if(psCacheBuilderType.equalsIgnoreCase(PermaStoreConfigurationConstant.CONFIGBUILDER_SQL)){
			
			if(configBuilder.getSQLBuilder()==null || configBuilder.getSQLBuilder().getSQLQuery()==null || configBuilder.getSQLBuilder().getSQLQuery().getValue()==null || configBuilder.getSQLBuilder().getSQLQuery().getMappedClass()==null){
				new PermaStoreConfigurationBuilderException("Invalid SQLBuilder Configuration mapper or SQL is missing");
			}
			try {
				SQLCacheObjectBuilder sqlCacheBuilder=new SQLCacheObjectBuilder();
				objToCache=sqlCacheBuilder.loadDataForCache(configBuilder.getSQLBuilder().getSQLQuery().getValue(),configBuilder.getSQLBuilder().getSQLQuery().getMappedClass());
			} catch (SQLCacheBuilderException e) {
				new PermaStoreConfigurationBuilderException("Failed to Build PermaStore for SQLBuilder",e);
			}
			return objToCache;
		}else{
			//Unknown Builder Type throw exception
			throw new PermaStoreConfigurationBuilderException("UnKnown-PSConfigBuilderType for configType="+psCacheBuilderType);
		}
	}

}
