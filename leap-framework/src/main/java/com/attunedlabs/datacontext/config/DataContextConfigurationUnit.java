package com.attunedlabs.datacontext.config;

import java.io.Serializable;

import com.attunedlabs.config.beans.ConfigurationUnit;

public class DataContextConfigurationUnit extends ConfigurationUnit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2250298242450485391L;
	public static final String FEATURE_DATACONTEXT_GROUPKEY_SUFFIX = "-FDC";
	private String siteId;
	/**
	 * It is matches with the ConfigId in the database i.e
	 * confignodedata{table}.nodeDataId{column}
	 */
	private Integer dbconfigId;
	/** Id of the Node in db that this configuration is attached with */
	private Integer attachedNodeId;
	private String groupId;

	public DataContextConfigurationUnit(String tenantId, String siteId,Integer attachedNodeId, Boolean isEnabled, String key,Serializable configData) {
		super(tenantId+"-"+siteId, configData, isEnabled, key,getConfigGroupKey(attachedNodeId));
		this.siteId = siteId;
		this.attachedNodeId = attachedNodeId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
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

	/**
	 * Util Method to generate the Policy Configuration Group Key
	 * 
	 * @param attachedToNodeId
	 * @return
	 */
	public static String getConfigGroupKey(Integer attachedToNodeId) {
		String fdcGroupKey = attachedToNodeId.intValue() + FEATURE_DATACONTEXT_GROUPKEY_SUFFIX;
		return fdcGroupKey;
	}

}
