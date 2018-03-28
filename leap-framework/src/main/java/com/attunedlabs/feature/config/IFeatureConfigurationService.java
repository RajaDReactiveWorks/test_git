package com.attunedlabs.feature.config;

import java.util.Map;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.feature.jaxb.Feature;
import com.attunedlabs.feature.jaxb.Service;


public interface IFeatureConfigurationService {
	public void addFeatureConfiguration(ConfigurationContext configContext,Feature feature) throws FeatureConfigurationException;

	public FeatureConfigurationUnit getFeatureConfiguration(FeatureConfigRequestContext requestContext, String configName)
			throws FeatureConfigurationException;
	public void addNewServiceInFeatureConfiguration(ConfigurationContext configContext, Service service)throws FeatureConfigurationException;
	public int updateFeatureConfiguration(ConfigurationContext configContext,String groupName,Feature fsConfig, int configNodedataId) throws FeatureConfigurationException;
	public void changeStatusOfFeatureConfig(ConfigurationContext configContext, String featureName, boolean isConfigEnabled) throws FeatureConfigurationException;
	public void changeStatusOfFeatureService(ConfigurationContext configContextt, String configName, Map<String, Boolean> enabled) throws FeatureConfigurationException;
	public boolean deleteFeatureConfiguration(ConfigurationContext configContext, String configName)throws FeatureConfigurationException;
    public boolean checkFeatureExistInDBAndCache(ConfigurationContext configContext, String configName)throws FeatureConfigurationException,FeatureConfigRequestException;
    
    public boolean reloadFeatureCacheObject(RequestContext requestContext, String configName)
			throws FeatureConfigRequestException;
    	
    
}
