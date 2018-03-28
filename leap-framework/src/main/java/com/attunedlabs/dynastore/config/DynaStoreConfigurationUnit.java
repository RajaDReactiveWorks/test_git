package com.attunedlabs.dynastore.config;

import java.io.Serializable;

import com.attunedlabs.config.beans.ConfigurationUnit;
import com.attunedlabs.dynastore.config.jaxb.DynastoreConfiguration;
import com.attunedlabs.dynastore.config.jaxb.DynastoreName;

public class DynaStoreConfigurationUnit extends ConfigurationUnit implements Serializable{
	private static final long serialVersionUID = 5000658055134380162L;

	public  static final String DYNASTORECONFIG_GROUPKEY_SUFFIX="-DSC";
	public  static final String DYNASTORECONFIG_DEFULAT_VERSION="v1";
	public static final String DYNA_UNIQUE_ID_NAME="DYNA_COLLECTION";
	public static final String DYNA_COLLECTION_PREFIX="DYNA_COLL-";
	
	private String siteId;
	/** It is matches with the ConfigId in the database i.e confignodedata{table}.nodeDataId{column}	 */
	private Integer dbconfigId;
	/** Id of the Node in db that this configuration is attached with */
	private Integer attachedNodeId; 
	//private DynastoreConfiguration dynastoreConfiguration;
	private String dynaCollectionId;
	
	public DynaStoreConfigurationUnit(String tenantId, String siteId,Integer attachedNodeId, 
			Boolean isEnabled,DynastoreConfiguration dynastoreConfiguration,String dynaCollectionId) {
		
		super(tenantId, dynastoreConfiguration, isEnabled, getDynaStoreKey(dynastoreConfiguration.getDynastoreName()), getConfigGroupKey(attachedNodeId));
		this.siteId=siteId;
		this.attachedNodeId=attachedNodeId;
		this.dynaCollectionId=dynaCollectionId;
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


	public static String getDynastoreconfigGroupkeySuffix() {
		return DYNASTORECONFIG_GROUPKEY_SUFFIX;
	}
	

	public String getDynaCollectionId() {
		return dynaCollectionId;
	}


	public void setDynaCollectionId(String dynaCollectionId) {
		this.dynaCollectionId = dynaCollectionId;
	}


	public static String getConfigGroupKey(Integer attachedToNodeId){
		String dsGroupKey = attachedToNodeId.intValue() +DYNASTORECONFIG_GROUPKEY_SUFFIX;
		return dsGroupKey;
	}
	

	public static String getDynaStoreKey(DynastoreName dynastoreName){
		
		String dynaStoreName=dynastoreName.getValue();
	
		if(dynastoreName.getVersion()!=null &&!dynastoreName.getVersion().isEmpty() )
			return dynaStoreName+"-"+dynastoreName.getVersion();
		
		
		return dynaStoreName+"-"+DYNASTORECONFIG_DEFULAT_VERSION;
	}

	public DynastoreConfiguration getDynastoreConfiguration() {
		return (DynastoreConfiguration)this.getConfigData();
	}


	public String toString() {
		return "DynaStoreConfigurationUnit [siteId=" + siteId + ", dbconfigId=" + dbconfigId + ", attachedNodeId=" + attachedNodeId + ", dynaCollectionId="
				+ dynaCollectionId + ", getKey()=" + getKey() + ", getConfigGroup()=" + getConfigGroup() + "]";
	}

	
	
	
	
}
