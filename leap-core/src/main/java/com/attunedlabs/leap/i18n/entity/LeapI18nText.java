package com.attunedlabs.leap.i18n.entity;

public class LeapI18nText {

	private int msgId;
	private String tenantId;
	private String siteId;
	private String feature;
	private String resourceType;
	private String msgVariant;
	private String localeId;
	private String elementId;
	private String i18nId;
	private String textValue;

	public LeapI18nText(int msgId, String tenantId, String siteId, String feature, String resourceType,
			String msgVariant, String localeId, String usage, String i18nId, String textValue) {
		super();
		this.msgId = msgId;
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.feature = feature;
		this.resourceType = resourceType;
		this.msgVariant = msgVariant;
		this.localeId = localeId;
		this.elementId = usage;
		this.i18nId = i18nId;
		this.textValue = textValue;
	}

	public LeapI18nText(String tenantId, String siteId, String feature, String resourceType, String msgVariant,
			String localeId, String usage, String i18nId, String textValue) {
		super();
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.feature = feature;
		this.resourceType = resourceType;
		this.msgVariant = msgVariant;
		this.localeId = localeId;
		this.elementId = usage;
		this.i18nId = i18nId;
		this.textValue = textValue;
	}

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
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

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getMsgVariant() {
		return msgVariant;
	}

	public void setMsgVariant(String msgVariant) {
		this.msgVariant = msgVariant;
	}

	public String getLocaleId() {
		return localeId;
	}

	public void setLocaleId(String localeId) {
		this.localeId = localeId;
	}

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public String getI18nId() {
		return i18nId;
	}

	public void setI18nId(String i18nId) {
		this.i18nId = i18nId;
	}

	public String getTextValue() {
		return textValue;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	@Override
	public String toString() {
		return "LeapI18nText [msgId=" + msgId + ", tenantId=" + tenantId + ", siteId=" + siteId + ", feature=" + feature
				+ ", resourceType=" + resourceType + ", msgVariant=" + msgVariant + ", localeId=" + localeId
				+ ", usage=" + elementId + ", i18nId=" + i18nId + ", textValue=" + textValue + "]";
	}

}
