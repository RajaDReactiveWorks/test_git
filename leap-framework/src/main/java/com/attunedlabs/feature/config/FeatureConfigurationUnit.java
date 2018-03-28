package com.attunedlabs.feature.config;

import java.io.Serializable;

import com.attunedlabs.config.beans.ConfigurationUnit;
import com.attunedlabs.feature.jaxb.Feature;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration;
import com.attunedlabs.policy.jaxb.Policy;


/**
 *  Configuration Unit that is Cached in DataGrid for Feature 
 * @author bizruntime
 *
 */
public class FeatureConfigurationUnit extends ConfigurationUnit implements Serializable{
	
	private static final long serialVersionUID = -2799484988383135067L;
	
	public static final String FEATURECONFIG_GROUPKEY_SUFFIX="-FSC";
	
	private String siteId;
	/** It is matches with the ConfigId in the database i.e confignodedata{table}.nodeDataId{column}	 */
	private Integer dbconfigId;
	/** Id of the Node in db that this configuration is attached with */
	private Integer attachedNodeId; 
	private String groupId;
	public FeatureConfigurationUnit(String tenantId,String siteId,Integer attachedNodeId,boolean isEnabled,Feature feature) {
		super(tenantId+"-"+siteId, feature, isEnabled, feature.getFeatureName(), getConfigGroupKey(attachedNodeId));		
		this.siteId=siteId;
		this.attachedNodeId=attachedNodeId;
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
	 * @param attachedToNodeId
	 * @return
	 */
	public static String getConfigGroupKey(Integer attachedToNodeId){
		String fsGroupKey = attachedToNodeId.intValue() + FEATURECONFIG_GROUPKEY_SUFFIX;
		return fsGroupKey;
	}
}
