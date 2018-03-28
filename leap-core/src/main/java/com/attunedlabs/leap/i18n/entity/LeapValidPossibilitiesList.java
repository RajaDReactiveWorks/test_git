package com.attunedlabs.leap.i18n.entity;

public class LeapValidPossibilitiesList {

	private int vpListId;
	private String tenantId;
	private String siteId;
	private String feature;
	private String vpListI18nId;
	private String vpType;

	public LeapValidPossibilitiesList(int vpListId, String tenantId, String siteId, String feature, String vpListI18nId,
			String vpType) {
		super();
		this.vpListId = vpListId;
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.feature = feature;
		this.vpListI18nId = vpListI18nId;
		this.vpType = vpType;
	}

	public LeapValidPossibilitiesList(String tenantId, String siteId, String feature, String vpListI18nId,
			String vpType) {
		super();
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.feature = feature;
		this.vpListI18nId = vpListI18nId;
		this.vpType = vpType;
	}

	public int getVpListId() {
		return vpListId;
	}

	public void setVpListId(int vpListId) {
		this.vpListId = vpListId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}

	public String getVpListI18nId() {
		return vpListI18nId;
	}

	public void setVpListI18nId(String vpListI18nId) {
		this.vpListI18nId = vpListI18nId;
	}

	public String getVpType() {
		return vpType;
	}

	public void setVpType(String vpType) {
		this.vpType = vpType;
	}

	@Override
	public String toString() {
		return "LeapValidPossibilitiesList [vpListId=" + vpListId + ", tenantId=" + tenantId + ", siteId=" + siteId
				+ ", feature=" + feature + ", vpListI18nId=" + vpListI18nId + ", vpType=" + vpType + "]";
	}

}
