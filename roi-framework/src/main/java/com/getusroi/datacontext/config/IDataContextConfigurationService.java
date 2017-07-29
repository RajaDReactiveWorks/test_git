package com.getusroi.datacontext.config;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.datacontext.jaxb.FeatureDataContext;
import com.getusroi.integrationfwk.config.IntegrationPipelineConfigException;
import com.getusroi.integrationfwk.config.IntegrationPipelineConfigParserException;

public interface IDataContextConfigurationService {
	public void addDataContext(ConfigurationContext configContext,FeatureDataContext featureDataContext) throws  DataContextConfigurationException;
	public DataContextConfigurationUnit getDataContextConfiguration(RequestContext requestContext) throws  DataContextConfigurationException;
	public boolean deleteDataContextConfiguration(ConfigurationContext configContext) throws DataContextConfigurationException;
	public boolean checkDataContextConfigExistOrNot(ConfigurationContext configurationContext,
			String configName) throws DataContextParserException, DataContextConfigurationException;
	public boolean reloadDataContextCacheObject(RequestContext requestContext, String configName)
			throws DataContextConfigurationException;
}
