package com.attunedlabs.permastore.config;

import java.io.Serializable;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration;

/**
 * Interface for Handling the PermaStoreConfigurations.
 * 
 * @author bizruntime
 *
 */
public interface IPermaStoreConfigurationService {
	
	/**
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param permaStoreConfig
	 * @throws PermaStoreConfigurationException
	 */
	public void addPermaStoreConfiguration(ConfigurationContext configurationContext, PermaStoreConfiguration permaStoreConfig)
			throws PermaStoreConfigurationException;

	/**
	 * 
	 * @param requestContext
	 * @param configName
	 * @return
	 * @throws InvalidNodeTreeException
	 * @throws PermaStoreConfigRequestException
	 */
	
	public PermaStoreConfigurationUnit getPermaStoreConfiguration(RequestContext requestContext,
			String configName) throws InvalidNodeTreeException,PermaStoreConfigRequestException;
	
	/**
	 * 
	 * @param requestContext
	 * @param configName
	 * @return
	 * @throws PermaStoreConfigRequestException
	 */
	
	public Serializable getPermaStoreCachedObject(RequestContext requestContext, String configName)
			throws PermaStoreConfigRequestException ;
	/**
	 * 
	 * @param requestContext
	 * @param configName
	 * @return
	 * @throws PermaStoreConfigRequestException
	 */
	public boolean deletePermaStoreConfiguration(ConfigurationContext configurationContext,
			String configName) throws PermaStoreConfigurationException;
	
	/**
	 * 
	 * @param requestContext
	 * @param configName
	 * @param isEnable
	 * @throws PermaStoreConfigurationException
	 */
	public void changeStatusOfPermaStoreConfig(ConfigurationContext configurationContext,
			String configName,boolean isEnable)throws PermaStoreConfigurationException;
	
	/**
	 * 
	 * @param requestContext
	 * @param configName
	 * @throws PermaStoreConfigRequestException
	 */
	public void verifyPermaStoreConfigLoaded(RequestContext requestContext,
			String configName) throws PermaStoreConfigRequestException;

	
	/**
	 * update permstoreconfigration with referrance configNodedataId
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param psConfig
	 * @param configNodedataId
	 * @return int
	 * @throws PermaStoreConfigurationException
	 */
	public int updatePermaStoreConfiguration(ConfigurationContext configurationContext,PermaStoreConfiguration psConfig, int configNodedataId)throws PermaStoreConfigurationException, PermaStoreConfigParserException;
	
	/**
	 * Re-loads the permastore CachedObject from the configured source.
	 * @param requestContext
	 * @param configName
	 * @return
	 * @throws PermaStoreConfigurationException
	 */
	public boolean reloadPerStoreCacheObject(RequestContext requestContext, String configName)throws  PermaStoreConfigurationException;

	/**
	 * To check PermastoreConfigExist  in  DB and cache ,if Exist in DB check wether it is Enabled or not 
	 * if enabled check data exist in cache or not , if config data not Exist load the the data to cache 
	 * @param requestContext
	 * @param configName
	 * @return boolean Value 
	 * @throws PermaStoreConfigRequestException
	 */
	
	public boolean checkPermaStoreConfigarationExistOrNot(ConfigurationContext configurationContext,String configName)throws   PermaStoreConfigRequestException ,PermaStoreConfigurationException;
}
