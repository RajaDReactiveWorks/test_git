package com.attunedlabs.config.beans;

import java.io.Serializable;

public class ConfigurationUnit implements Serializable{
	private static final long serialVersionUID = -1723862671617471573L;
	
	private String tenantId;
	private Serializable configData;
	private Boolean isEnabled;
	private String key;
	private String configGroup;
	
	
	
	public ConfigurationUnit(String tenantId, Serializable configData,
			Boolean isEnabled, String key, String configGroup) {
		super();
		this.tenantId = tenantId;
		this.configData = configData;
		this.isEnabled = isEnabled;
		this.key = key;
		this.configGroup = configGroup;
	}
	
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public Serializable getConfigData() {
		return configData;
	}
	public void setConfigData(Serializable configData) {
		this.configData = configData;
	}
	public Boolean getIsEnabled() {
		return isEnabled;
	}
	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getConfigGroup() {
		return configGroup;
	}
	public void setConfigGroup(String configGroup) {
		this.configGroup = configGroup;
	}

	@Override
	public String toString() {
		return "ConfigurationUnit [tenantId=" + tenantId + ", isEnabled=" + isEnabled + ", key=" + key + ", configGroup=" + configGroup +",\n configData={"+ configData + "}\n  ]";
	}

	
}
