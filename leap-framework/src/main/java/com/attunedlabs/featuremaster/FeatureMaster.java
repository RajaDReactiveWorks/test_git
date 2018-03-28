package com.attunedlabs.featuremaster;

public class FeatureMaster {

	private int id;
	
	private int siteId;
	public FeatureMaster(int id, int siteId, String feature, String featureGroup, String version, String description,
			boolean multipleVendorSupport, boolean allowMultipleImpl, String product) {
		super();
		this.id = id;
		this.siteId = siteId;
		this.feature = feature;
		this.featureGroup = featureGroup;
		this.version = version;
		this.description = description;
		this.multipleVendorSupport = multipleVendorSupport;
		this.allowMultipleImpl = allowMultipleImpl;
		this.product = product;
	}

	public FeatureMaster(int siteId) {
		super();
		this.siteId = siteId;
	}

	public int getSiteId() {
		return siteId;
	}

	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}
	private String feature;
	private String featureGroup;
	private String version;
	private String description;
	private boolean multipleVendorSupport;
	private boolean allowMultipleImpl;
	private String product="wms2.0";
	
	public FeatureMaster() {
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public String getFeature() {
		return feature;
	}
	public void setFeature(String feature) {
		this.feature = feature;
	}
	public String getFeatureGroup() {
		return featureGroup;
	}
	public void setFeatureGroup(String featureGroup) {
		this.featureGroup = featureGroup;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isMultipleVendorSupport() {
		return multipleVendorSupport;
	}
	public void setMultipleVendorSupport(boolean multipleVendorSupport) {
		this.multipleVendorSupport = multipleVendorSupport;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	
	public boolean isAllowMultipleImpl() {
		return allowMultipleImpl;
	}

	public void setAllowMultipleImpl(boolean allowMultipleImpl) {
		this.allowMultipleImpl = allowMultipleImpl;
	}

	@Override
	public String toString() {
		return "FeatureMaster [id=" + id + ", siteId=" + siteId + ", feature=" + feature + ", featureGroup="
				+ featureGroup + ", version=" + version + ", description=" + description + ", multipleVendorSupport="
				+ multipleVendorSupport + ", allowMultipleImpl=" + allowMultipleImpl + ", product=" + product + "]";
	}
	
	
	
}
