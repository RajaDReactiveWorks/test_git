package com.attunedlabs.feature.config.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.feature.config.IFeatureDiscoveryService;
import com.hazelcast.core.HazelcastInstance;

public class FeatureDiscoveryService implements IFeatureDiscoveryService {
	public static final String HostServiceGroupKey_Suffix="HostService";
	
	public void addFeatureService(ConfigurationContext configContext, String serviceName, String hostName) {
		HazelcastInstance hcInstance=DataGridService.getDataGridInstance().getHazelcastInstance();
		Map<String,List<String>> groupMap=hcInstance.getMap(getHostServiceGroupKey(configContext.getSiteId()));
		List<String> hostList=(List)groupMap.get(getServiceKey(configContext,serviceName));
		if(hostList==null){
			hostList=new ArrayList(3);
			hostList.add(hostName);
		}else if(!hostList.contains(hostName)){
			hostList.add(hostName);
		}
	}

	public void deleteFeatureService(ConfigurationContext configContext, String serviceName, String hostName) {
		HazelcastInstance hcInstance=DataGridService.getDataGridInstance().getHazelcastInstance();
		Map<String,List<String>> groupMap=hcInstance.getMap(getHostServiceGroupKey(configContext.getSiteId()));
		List<String> hostList=(List)groupMap.get(getServiceKey(configContext,serviceName));
		if(hostList!=null){
			hostList.remove(hostName);
		}
	}

	public List<String> getHostListForService(ConfigurationContext configContext, String serviceName) {
		HazelcastInstance hcInstance=DataGridService.getDataGridInstance().getHazelcastInstance();
		Map<String,List<String>> groupMap=hcInstance.getMap(getHostServiceGroupKey(configContext.getSiteId()));
		List<String> hostList=(List)groupMap.get(getServiceKey(configContext,serviceName));
		return hostList;
	}
	
	private String getHostServiceGroupKey(String siteId){
		return siteId+"-"+HostServiceGroupKey_Suffix;
	}
	
	private String getServiceKey(ConfigurationContext configContext,String serviceName){
		String version=configContext.getVersion();
		if(configContext.getVersion()==null)
			version="v1";
		return serviceName+"-"+configContext.getVersion();
	}
}
