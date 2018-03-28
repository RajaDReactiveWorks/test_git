package com.attunedlabs.ddlutils.config.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.ddlutils.config.DbConfigNotfoundException;
import com.attunedlabs.ddlutils.config.IDbConfigurationService;
import com.attunedlabs.ddlutils.config.InvalidDbConfigurationException;
import com.attunedlabs.featuremetainfo.jaxb.DBConfiguration;

/**
 * 
 * @author attunedlabs Implementation for the IDbConfigurationService
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
