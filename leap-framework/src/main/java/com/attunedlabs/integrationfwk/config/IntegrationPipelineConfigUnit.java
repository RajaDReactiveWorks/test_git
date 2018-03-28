package com.attunedlabs.integrationfwk.config;

import java.io.Serializable;

import com.attunedlabs.config.beans.ConfigurationUnit;
import com.attunedlabs.integrationfwk.config.jaxb.IntegrationPipe;

public class IntegrationPipelineConfigUnit extends ConfigurationUnit implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2967680676808108584L;
	public static final String INTEGRATIONPIPE_CONFIG_GROUPKEY_SUFFIX = "-IPC";
	private IntegrationPipe integrationPipe;
	private String siteId;
	
	private Integer dbconfigId;
	/** Id of the Node in db that this configuration is attached with */
	private Integer attachedNodeId;

	public IntegrationPipelineConfigUnit(String tenantId, String siteId, Integer attachedNodeId, Boolean isEnabled,
			IntegrationPipe integrationPipe) {
		super(tenantId, integrationPipe, isEnabled, integrationPipe.getName(),getConfigGroupKey(attachedNodeId));
		this.integrationPipe = integrationPipe;
		this.siteId = siteId;
		this.attachedNodeId = attachedNodeId;
	}

	public IntegrationPipe getIntegrationPipe() {
		return integrationPipe;
	}

	public void setIntegrationPipe(IntegrationPipe integrationPipe) {
		this.integrationPipe = integrationPipe;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static String getIntegrationpipeConfigGroupkeySuffix() {
		return INTEGRATIONPIPE_CONFIG_GROUPKEY_SUFFIX;
	}
	
	public static String getConfigGroupKey(Integer attachedToNodeId) {
		return attachedToNodeId.intValue() + INTEGRATIONPIPE_CONFIG_GROUPKEY_SUFFIX;
	}

}
