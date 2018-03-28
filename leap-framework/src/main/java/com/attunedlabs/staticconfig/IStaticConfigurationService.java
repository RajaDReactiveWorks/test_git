package com.attunedlabs.staticconfig;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.staticconfig.impl.AccessProtectionException;
import com.attunedlabs.zookeeper.staticconfig.service.impl.InvalidFilePathException;

/**
 * This Service is responsible for managing the static content like
 * xls,freemarker template etc.4 Each static config is stored in its own
 * namespace.
 * 
 * @author bizruntime
 *
 */
public interface IStaticConfigurationService {
	/**
	 * Adds the StaticConfiguration to the MESH Framework for a given
	 * Configuration context<br>
	 * It builds the namespace from the configuration context and than add the
	 * static config to that <br>
	 * namespace
	 * 
	 * @throws StaticConfigDuplicateNameofFileException
	 * @throws AccessProtectionException
	 * @throws InvalidFilePathException 
	 * @throws FileStaticConfigurationServiceException
	 */
	public void addStaticConfiguration(ConfigurationContext ctx, String staticConfigName, String configContent)
			throws AddStaticConfigException, StaticConfigInitializationException,
			StaticConfigDuplicateNameofFileException, AccessProtectionException, InvalidFilePathException;

	/**
	 * Gets the static configuration for the given request context<br>
	 * Based on the Request context it builds the namespace and than searches it
	 * in that amespace
	 * 
	 * @param reqCtx
	 * @param staticConfigName
	 * @return
	 * @throws StaticConfigFetchException
	 * @throws StaticConfigInitializationException
	 * @throws AccessProtectionException
	 */
	public String getStaticConfiguration(RequestContext reqCtx, String staticConfigName)
			throws StaticConfigFetchException, StaticConfigInitializationException, AccessProtectionException;

	/**
	 * Service to update a file
	 * 
	 * @param path
	 * @param fileName
	 * @param data
	 * @return Response, Success if updated
	 * @throws StaticConfigUpdateException 
	 */
	public String updateStaticConfiguration(ConfigurationContext configurationContext, String staticConfigName,
			String configContent) throws StaticConfigUpdateException;
	
	/**
	 * Service to delete a file
	 * 
	 * @param path
	 * @param fileName
	 * @param data
	 * @return Response, Success if updated
	 * @throws StaticConfigDeleteException 
	 */
	public String deleteStaticConfiguration(ConfigurationContext configurationContext, String staticConfigName) throws StaticConfigDeleteException;
}
