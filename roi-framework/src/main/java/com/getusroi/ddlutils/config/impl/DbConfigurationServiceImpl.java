package com.getusroi.ddlutils.config.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.ddlutils.config.DbConfigNotfoundException;
import com.getusroi.ddlutils.config.IDbConfigurationService;
import com.getusroi.ddlutils.config.InvalidDbConfigurationException;
import com.getusroi.featuremetainfo.jaxb.DBConfiguration;

/**
 * 
 * @author getusroi Implementation for the IDbConfigurationService
 *
 */
public class DbConfigurationServiceImpl implements IDbConfigurationService {

	private final Logger logger = LoggerFactory.getLogger(DbConfigurationServiceImpl.class.getName());

	@Override
	public void addDbConfiguration(ConfigurationContext ctx, DBConfiguration configContent)
			throws InvalidDbConfigurationException {
		logger.debug(".addIntegrationPipelineConfiguration().." + configContent);

	}// ..end of the method

	@Override
	public String getDbConfiguration(RequestContext reqCtx, String dbConfigName) throws DbConfigNotfoundException {

		return null;
	}// ..end of the method

	@Override
	public String updateDbConfiguration(ConfigurationContext configurationContext, int configNodeDataId,
			String configContent) throws DbConfigNotfoundException {
		// TODO Has to be implemented
		return null;
	}// ..end of the method

	@Override
	public String deleteDbConfiguration(ConfigurationContext configurationContext, String dbConfigName)
			throws DbConfigNotfoundException {
		// TODO Has to be implemented
		return null;
	}// ..end of the method

}
