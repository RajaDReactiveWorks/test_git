package com.attunedlabs.leap.i18n.entity;

public class LeapValidPossibilities {

	private int vpListId;
	private String tenantId;
	private String siteId;
	private String feature;
	private String vpListI18nId;
	private String vpType;
	private String localeId;
	private String seqNumber;
	private String vpCode;
	private String textValue;

	public LeapValidPossibilities(int vpListId, String tenantId, String siteId, String feature, String vpListI18nId,
			String vpType, String localeId, String seqNumber, String vpCode, String textValue) {
		super();
		this.vpListId = vpListId;
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.feature = feature;
		this.vpListI18nId = vpListI18nId;
		this.vpType = vpType;
		this.localeId = localeId;
		this.seqNumber = seqNumber;
		this.vpCode = vpCode;
		this.textValue = textValue;
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

	public String getLocaleId() {
		return localeId;
	}

	public void setLocaleId(String localeId) {
		this.localeId = localeId;
	}

	public String getSeqNumber() {
		return seqNumber;
	}

	public void setSeqNumber(String seqNumber) {
		this.seqNumber = seqNumber;
	}

	public String getVpCode() {
		return vpCode;
	}

	public void setVpCode(String vpCode) {
		this.vpCode = vpCode;
	}

	public String getTextValue() {
		return textValue;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	@Override
	public String toString() {
		return "LeapValidPossibilities [vpListId=" + vpListId + ", tenantId=" + tenantId + ", siteId=" + siteId
				+ ", feature=" + feature + ", vpListI18nId=" + vpListI18nId + ", vpType=" + vpType + ", localeId="
				+ localeId + ", seqNumber=" + seqNumber + ", vpCode=" + vpCode + ", textValue=" + textValue + "]";
	}
	
	

}
