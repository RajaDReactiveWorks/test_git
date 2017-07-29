package com.getusroi.ddlutils.config;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.featuremetainfo.jaxb.DBConfiguration;

/**
 * 
 * @author getusroi
 * @see Service API's , implemented to perform DBConfig operations
 *
 */
public interface IDbConfigurationService {

	/**
	 * Service to add new DBConfig to process using ddlUtils - implementation
	 * 
	 * @param ctx
	 * @param dbConfigName
	 * @param configContent
	 * @throws InvalidDbConfigurationException
	 */
	public void addDbConfiguration(ConfigurationContext ctx, DBConfiguration configContent)
			throws InvalidDbConfigurationException;

	/**
	 * Service to query the existing DBConfiguration by name
	 * 
	 * @param reqCtx
	 * @param dbConfigName
	 * @return string format of DBConfig
	 * @throws DbConfigNotfoundException
	 */
	public String getDbConfiguration(RequestContext reqCtx, String dbConfigName) throws DbConfigNotfoundException;

	/**
	 * Service to update the existing DBConfiguration
	 * 
	 * @param configurationContext
	 * @param dbConfigName
	 * @param configContent
	 * @return
	 * @throws DbConfigNotfoundException
	 */
	public String updateDbConfiguration(ConfigurationContext configurationContext,  int configNodeDataId,
			String configContent) throws DbConfigNotfoundException;

	/**
	 * Service to delete the existing DBConfiguration by name
	 * 
	 * @param configurationContext
	 * @param dbConfigName
	 * @return
	 * @throws DbConfigNotfoundException
	 */
	public String deleteDbConfiguration(ConfigurationContext configurationContext, String dbConfigName)
			throws DbConfigNotfoundException;

}
