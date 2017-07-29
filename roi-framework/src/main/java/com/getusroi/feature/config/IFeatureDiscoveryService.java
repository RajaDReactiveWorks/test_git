package com.getusroi.feature.config;

import java.util.List;

import com.getusroi.config.ConfigurationContext;

public interface IFeatureDiscoveryService {
	public void addFeatureService(ConfigurationContext configContext,String serviceName,String hostName);
	public void deleteFeatureService(ConfigurationContext configContext,String serviceName,String hostName);
	public List<String> getHostListForService(ConfigurationContext configContext,String service);
	
}
