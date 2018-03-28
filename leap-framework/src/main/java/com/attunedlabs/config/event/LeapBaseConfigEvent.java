package com.attunedlabs.config.event;

import java.io.Serializable;

import com.attunedlabs.config.beans.ConfigurationUnit;
/*
 * Base Configuration Event Class. This class represent the Event for Changes in the central configurations.
 */
public class LeapBaseConfigEvent implements Serializable{
	private static final long serialVersionUID = 7935162772233460912L;
	
	public static final String EVT_CONFIGURATION_ADDED="CONFIGURATION_ADDED";
	public static final String EVT_CONFIGURATION_REMOVED="CONFIGURATION_REMOVED";
	public static final String EVT_CONFIGURATION_UPDATED="CONFIGURATION_UPDATED";
	public static final String EVT_CONFIGURATION_STATUSCHANGED="CONFIGURATION_STATUSCHANGED";
	
	private String tenantId;
	private ConfigurationUnit newConfigUnit;
	private String eventType;
	private String configGroup;
	private String configName;
	
		
	public String getTenantId() {
		return tenantId;
	}


	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}


	public ConfigurationUnit getNewConfigUnit() {
		return newConfigUnit;
	}


	public void setNewConfigUnit(ConfigurationUnit newConfigUnit) {
		this.newConfigUnit = newConfigUnit;
	}


	public String getEventType() {
		return eventType;
	}


	public void setEventType(String eventType) {
		this.eventType = eventType;
	}


	public String getConfigGroup() {
		return configGroup;
	}


	public void setConfigGroup(String configGroup) {
		this.configGroup = configGroup;
	}


	public String getConfigName() {
		return configName;
	}


	public void setConfigName(String configName) {
		this.configName = configName;
	}


	public LeapBaseConfigEvent(String tenantId, ConfigurationUnit newConfigUnit,
			String eventType) {
		super();
		this.tenantId = tenantId;
		this.newConfigUnit = newConfigUnit;
		this.eventType = eventType;
	}


	@Override
	public String toString() {
		return "LeapBaseConfigEvent [tenantId=" + tenantId + ", newConfigUnit="
				+ newConfigUnit + ", eventType=" + eventType + ", configGroup="
				+ configGroup + ", configName=" + configName + "]";
	}

}
