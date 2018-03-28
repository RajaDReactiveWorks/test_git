package com.attunedlabs.featuredeployment;

import java.io.Serializable;

public class FeatureDeployment implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5284566803050387019L;
	private int id;
	private int featureMasterId;
	private String featureName;
	private String implementationName;
	private String vendorName;
	private String featureVersion;
	private boolean isActive;
	private boolean isPrimary;
	private boolean isCustomized;
	private String provider;
	
	public FeatureDeployment(){
		
	}
	
	public FeatureDeployment(int featureMasterId,String featureName,String implementationName,String vendorName,String featureVersion,boolean isActive,boolean isPrimary,boolean isCustomized, String provider){
		this.featureMasterId=featureMasterId;
		this.featureName=featureName;
		this.implementationName=implementationName;
		this.vendorName=vendorName;
		this.featureVersion=featureVersion;
		this.isActive=isActive;
		this.isPrimary=isPrimary;
		this.isCustomized=isCustomized;
		this.provider = provider;
	}
	
	public FeatureDeployment(int id,int featureMasterId,String featureName,String implementationName,String vendorName,String featureVersion,boolean isActive,boolean isPrimary,boolean isCustomized){
		this.id=id;
		this.featureMasterId=featureMasterId;
		this.featureName=featureName;
		this.implementationName=implementationName;
		this.vendorName=vendorName;
		this.featureVersion=featureVersion;
		this.isActive=isActive;
		this.isPrimary=isPrimary;
		this.isCustomized=isCustomized;
	}
	
	public FeatureDeployment(int featureMasterId,String featureName,String implementationName,String vendorName,String featureVersion,boolean isActive,boolean isPrimary,boolean isCustomized){
		this.featureMasterId=featureMasterId;
		this.featureName=featureName;
		this.implementationName=implementationName;
		this.vendorName=vendorName;
		this.featureVersion=featureVersion;
		this.isActive=isActive;
		this.isPrimary=isPrimary;
		this.isCustomized=isCustomized;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFeatureMasterId() {
		return featureMasterId;
	}

	public void setFeatureMasterId(int featureMasterId) {
		this.featureMasterId = featureMasterId;
	}

	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public String getImplementationName() {
		return implementationName;
	}

	public void setImplementationName(String implementationName) {
		this.implementationName = implementationName;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getFeatureVersion() {
		return featureVersion;
	}

	public void setFeatureVersion(String featureVersion) {
		this.featureVersion = featureVersion;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public boolean isCustomized() {
		return isCustomized;
	}

	public void setCustomized(boolean isCustomized) {
		this.isCustomized = isCustomized;
	}
	
	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	@Override
	public String toString() {
		return "FeatureDeployment [id=" + id + ", featureMasterId=" + featureMasterId + ", featureName=" + featureName
				+ ", implementationName=" + implementationName + ", vendorName=" + vendorName + ", featureVersion="
				+ featureVersion + ", isActive=" + isActive + ", isPrimary=" + isPrimary + ", isCustomized="
				+ isCustomized + ", provider=" + provider + "]";
	}
	
	
}
