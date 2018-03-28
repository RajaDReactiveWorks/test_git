package com.attunedlabs.feature.config;

import com.attunedlabs.config.RequestContext;


public class FeatureConfigRequestContext extends RequestContext{

	public FeatureConfigRequestContext(String tenant, String site, String featureGroup, String featureName,String implementationName,String vendor,String version, String provider) {
		super(tenant, site, featureGroup, featureName,implementationName,vendor,version, provider);
	}
	
	public FeatureConfigRequestContext(String tenant, String site, String featureGroup, String featureName,String implementationName,String vendor,String version) {
		super(tenant, site, featureGroup, featureName,implementationName,vendor,version);
	}
	public FeatureConfigRequestContext(String tenant, String site, String featureGroup, String featureName,String implName) {
		super(tenant, site, featureGroup, featureName,implName);
	}

	public FeatureConfigRequestContext(String tenantId, String siteId, String groupName) {
		super(tenantId, siteId, groupName);
	}
	

}
