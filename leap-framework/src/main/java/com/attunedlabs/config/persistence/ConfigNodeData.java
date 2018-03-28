package com.attunedlabs.config.persistence;

import java.util.Date;

/**
 * DTO for Mapping with the ConfigNodeData table.
 * This object represent the one configuration under the Configuration Node tree
 * @author amit
 *
 */
public class ConfigNodeData 
{
	private Integer nodeDataId;
	private Integer parentConfigNodeId;
	private String  configName;
	private String configData;
	private String configType;
	private String configLoadStatus;
	private Date   createdDTM;
	private String failureMsg;
	private boolean isEnabled;
	
	
	
	public ConfigNodeData(Integer parentConfigNodeId, String configName, String configData, String configType) {
		this.parentConfigNodeId = parentConfigNodeId;
		this.configName = configName;
		this.configData = configData;
		this.configType = configType;
	}


	public boolean isEnabled() {
		return isEnabled;
	}


	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}


	public ConfigNodeData(){
		
	}
	
	
	public Integer getNodeDataId() {
		return nodeDataId;
	}

	public void setNodeDataId(Integer nodeDataId) {
		this.nodeDataId = nodeDataId;
	}

	public Integer getParentConfigNodeId() {
		return parentConfigNodeId;
	}

	public void setParentConfigNodeId(Integer parentConfigNodeId) {
		this.parentConfigNodeId = parentConfigNodeId;
	}

	public String getConfigData() {
		return configData;
	}

	public void setConfigData(String configData) {
		this.configData = configData;
	}

	public String getConfigType() {
		return configType;
	}

	public void setConfigType(String configType) {
		this.configType = configType;
	}


	public String getConfigLoadStatus() {
		return configLoadStatus;
	}

	public void setConfigLoadStatus(String configStatus) {
		this.configLoadStatus = configStatus;
	}
 
	public Date getCreatedDTM() {
		return createdDTM;
	}

	public void setCreatedDTM(Date createdDTM) {
		this.createdDTM = createdDTM;
	}

	public String getFailureMsg() {
		return failureMsg;
	}

	public void setFailureMsg(String failureMsg) {
		this.failureMsg = failureMsg;
	}


	public String getConfigName() {
		return configName;
	}


	public void setConfigName(String configName) {
		this.configName = configName;
	}


	@Override
	public String toString() {
		return "ConfigNodeData [nodeDataId=" + nodeDataId + ", parentConfigNodeId=" + parentConfigNodeId
				+ ", configName=" + configName + ",  configType=" + configType
				+ ", configStatus=" + configLoadStatus +", isEnabled="+isEnabled+ ", createdDTM=" + createdDTM + ", failureMsg=" + failureMsg +", configData=" + configData +  "]";
	}

	
	
}
