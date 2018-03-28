package com.attunedlabs.permastore.config;

import java.io.Serializable;

public class PermaStoreEvent implements Serializable {
	private static final long serialVersionUID = -3700208024757816513L;

	public static final String EVENTTTYPE_ONCONFIG_DELETE = "OnConfigDelete";
	public static final String EVENTTTYPE_ONCONFIG_STATUSCHANGE = "OnConfigStatusChange";
	public static final String EVENTTTYPE_ONCONFIG_ENTRY_DELETE = "OnConfigEntryDelete";
	public static final String EVENTTTYPE_ONCONFIG_ENTRY_ADD = "OnConfigEntryAdd";
	public static final String EVENTTTYPE_ONCONFIG_ENTRY_UPDATE = "OnConfigEntryUpdate";

	/** Tenant with company and siteId combination */
	private String tenantId;
	/** Name of the Event */
	private String eventName;
	/** Name of the permastore That Initiated this Event */
	private String publishedFrom;
	/** Type of Event */
	private String eventType;

	public PermaStoreEvent(String tenantId, String eventName, String publishedFrom, String eventType) {
		this.tenantId = tenantId;
		this.eventName = eventName;
		this.publishedFrom = publishedFrom;
		this.eventType = eventType;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getPublishedFrom() {
		return publishedFrom;
	}

	public void setPublishedFrom(String publishedFrom) {
		this.publishedFrom = publishedFrom;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getTenantId() {
		return tenantId;
	}
}
