package com.attunedlabs.config;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationContext implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String tenantId;
	private String siteId;
	private String featureGroup;
	private String featureName;
	private String implementationName;
	private String vendorName;
	private String version;
	private String provider;

	public ConfigurationContext(String tenantId, String siteId, String featureGroup, String featureName,
			String implementationName, String vendorName, String version, String provider) {
		super();
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.featureGroup = featureGroup;
		this.featureName = featureName;
		this.implementationName = implementationName;
		this.vendorName = vendorName;
		this.version = version;
		this.provider = provider;
	}

	public ConfigurationContext() {
		// TODO Auto-generated constructor stub
	}

	public ConfigurationContext(String tenantId, String siteId, String featureGroup, String featureName,
			String implementationName, String vendor, String version) {
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.featureGroup = featureGroup;
		this.featureName = featureName;
		this.implementationName = implementationName;
		this.vendorName = vendor;
		this.version = version;
	}

	public ConfigurationContext(String tenantId, String siteId, String featureGroup, String featureName,
			String implementationName) {
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.featureGroup = featureGroup;
		this.featureName = featureName;
		this.implementationName = implementationName;
	}

	public ConfigurationContext(String tenantId, String siteId, String featureGroup) {
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.featureGroup = featureGroup;
	}

	public ConfigurationContext(String tenantId, String siteId) {
		this.tenantId = tenantId;
		this.siteId = siteId;
	}

	public ConfigurationContext(RequestContext reqContext) {
		this.tenantId = reqContext.getTenantId();
		this.siteId = reqContext.getSiteId();
		this.featureGroup = reqContext.getFeatureGroup();
		this.featureName = reqContext.getFeatureName();
		this.implementationName = reqContext.getImplementationName();
		this.vendorName = reqContext.getVendor();
		this.version = reqContext.getVersion();
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

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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
	
	@Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return new JSONObject(mapper.writeValueAsString(this)).toString();
		} catch (JsonProcessingException | JSONException e) {
			return "ConfigurationContext [tenantId=" + tenantId + ", siteId=" + siteId + ", featureGroup="
					+ featureGroup + ", featureName=" + featureName + ", implementationName=" + implementationName
					+ ", vendorName=" + vendorName + ", version=" + version + ", provider=" + provider + "]";
		}
	}

}
