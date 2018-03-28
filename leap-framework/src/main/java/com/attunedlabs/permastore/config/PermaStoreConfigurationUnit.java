package com.attunedlabs.permastore.config;

import java.io.Serializable;

import com.attunedlabs.config.beans.ConfigurationUnit;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration;

public class PermaStoreConfigurationUnit extends ConfigurationUnit implements Serializable {
	private static final long serialVersionUID = 2004207870731735987L;
	public static final String PERMASTORECONFIG_GROUPKEY_SUFFIX="-PSC";
	private PermaStoreConfiguration permaStoreConfig;
	private String siteId;
	/** It is matches with the ConfigId in the database i.e confignodedata{table}.nodeDataId{column}	 */
	private Integer dbconfigId;
	/** Id of the Node in db that this configuration is attached with */
	private Integer attachedNodeId; 
	
	public PermaStoreConfigurationUnit(String tenantId, String siteId,Integer attachedNodeId, Boolean isEnabled,PermaStoreConfiguration permaStoreConfig,
			Serializable cachedObj) {
		super(tenantId,cachedObj,isEnabled,permaStoreConfig.getName(),getConfigGroupKey(attachedNodeId));
		this.permaStoreConfig = permaStoreConfig;
		this.siteId=siteId;
		this.attachedNodeId=attachedNodeId;
	}

	public Integer getDbconfigId() {
		return dbconfigId;
	}

	public void setDbconfigId(Integer dbconfigId) {
		this.dbconfigId = dbconfigId;
	}

	public Integer getAttachedNodeId() {
		return attachedNodeId;
	}

	public void setAttachedNodeId(Integer attachedNodeId) {
		this.attachedNodeId = attachedNodeId;
	}

	public PermaStoreConfiguration getPermaStoreConfig() {
		return permaStoreConfig;
	}

	
	public void setPermaStoreConfig(PermaStoreConfiguration permaStoreConfig) {
		this.permaStoreConfig = permaStoreConfig;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	
	public static String getConfigGroupKey(Integer attachedToNodeId){
		String psGroupKey = attachedToNodeId.intValue() +PERMASTORECONFIG_GROUPKEY_SUFFIX;
		return psGroupKey;
	}
	@Override
	public String toString() {
		return "PermaStoreConfigurationUnit [permaStoreConfig=" + permaStoreConfig + ", siteId=" + siteId+",attachedNodeId="+attachedNodeId+",dbconfigId="+dbconfigId
				+ "," + super.toString() + "]";
	}
	
	
}
