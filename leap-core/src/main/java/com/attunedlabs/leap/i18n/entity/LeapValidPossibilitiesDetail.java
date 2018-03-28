package com.attunedlabs.leap.i18n.entity;

public class LeapValidPossibilitiesDetail {

	private int vpListId;
	private String tenantId;
	private String siteId;
	private String localeId;
	private int seqNumber;
	private String vpCode;
	private String textValue;

	public LeapValidPossibilitiesDetail(int vpListId, String tenantId, String siteId, String localeId, int seqNumber,
			String vpCode, String textValue) {
		super();
		this.vpListId = vpListId;
		this.tenantId = tenantId;
		this.siteId = siteId;
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

	public String getLocaleId() {
		return localeId;
	}

	public void setLocaleId(String localeId) {
		this.localeId = localeId;
	}

	public int getSeqNumber() {
		return seqNumber;
	}

	public void setSeqNumber(int seqNumber) {
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
		return "ValidPossibilitiesDetail [vpListId=" + vpListId + ", tenantId=" + tenantId + ", siteId=" + siteId
				+ ", localeId=" + localeId + ", seqNumber=" + seqNumber + ", vpCode=" + vpCode + ", textValue="
				+ textValue + "]";
	}

}
