package com.attunedlabs.config;

import java.io.Serializable;

import com.hazelcast.transaction.TransactionContext;

public class RequestContext implements Serializable{
	private static final long serialVersionUID = -5504878973750093570L;

	private String tenantId;
	private String siteId;
	private String featureGroup;
	private String featureName;
	private String implementationName;
	private String vendor;
	private String version;
	private transient TransactionContext hcTransactionalContext;
	private String requestId;
	private String provider;
	
	public RequestContext(){
		
	}
	
	public RequestContext(String tenantId, String siteId, String featureGroup, String featureName,
			String implementationName, String vendor, String version, String provider) {
		super();
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.featureGroup = featureGroup;
		this.featureName = featureName;
		this.implementationName = implementationName;
		this.vendor = vendor;
		this.version = version;
		this.provider = provider;
	}
	
	public RequestContext(String tenantId, String siteId, String featureGroup, String featureName,String implementationName, String vendor,
			String version) {
		super();
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.featureGroup = featureGroup;
		this.featureName = featureName;
		this.implementationName=implementationName;
		this.vendor = vendor;
		this.version = version;
	}

	public RequestContext(String tenantId, String siteId, String featureGroup) {
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.featureGroup = featureGroup;
	}

	public RequestContext(String tenantId, String siteId, String featureGroup, String featureName,String implementationName) {
		super();
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.featureGroup = featureGroup;
		this.featureName = featureName;
		this.implementationName=implementationName;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getSiteId() {
		return siteId;
	}
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	public String getFeatureGroup() {
		return featureGroup;
	}
	public void setFeatureGroup(String featureGroup) {
		this.featureGroup = featureGroup;
	}
	
	public String getFeatureName() {
		return featureName;
	}
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}
	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	
	public TransactionContext getHcTransactionalContext() {
		return hcTransactionalContext;
	}

	public void setHcTransactionalContext(TransactionContext hcTransactionalContext) {
		this.hcTransactionalContext = hcTransactionalContext;
	}
		
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
	public String getImplementationName() {
		return implementationName;
	}

	public void setImplementationName(String implementationName) {
		this.implementationName = implementationName;
	}
	
	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public boolean isValid(){
		if(this.tenantId==null||this.tenantId.isEmpty())
			return false;
		if(this.siteId==null||this.siteId.isEmpty())
			return false;
		if(this.featureGroup==null||this.featureGroup.isEmpty())
			return false;
		
		return true;
	}

	public ConfigurationContext getConfigurationContext (){
		return new ConfigurationContext(this.tenantId,this.siteId,this.featureGroup,this.featureName,this.implementationName,this.vendor,this.version,this.provider);
	}

	@Override
	public String toString() {
		return "RequestContext [tenantId=" + tenantId + ", siteId=" + siteId + ", featureGroup=" + featureGroup
				+ ", featureName=" + featureName + ", implementationName=" + implementationName + ", vendor=" + vendor
				+ ", version=" + version + ", requestId=" + requestId + ", provider=" + provider + "]";
	}
	
}
