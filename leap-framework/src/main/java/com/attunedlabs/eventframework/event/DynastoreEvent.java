package com.attunedlabs.eventframework.event;

import java.io.Serializable;

import com.attunedlabs.config.RequestContext;

public class DynastoreEvent extends LeapEvent implements Serializable{
	private static final long serialVersionUID = 6619551511803052169L;
	
	
	
	public static final String EVENTTYPE_ONRELOAD="onReload";
	public static final String EVENTTYPE_ONTERMINATION="DYNASTORE_SESSION_TERMINATED";
	public static final String EVENTTYPE_ONENTRY_ADDED="DYNASTORE_ENTRY_ADDED";
	public static final String EVENTTYPE_ONENTRY_DELETED="DYNASTORE_ENTRY_DELETED";
	public static final String EVENTTYPE_ONENTRY_UPDATED="DYNASTORE_ENTRY_UPDATED";
	
	public static final String PARAM_ENTRY_KEY="EntryKey";
	public static final String PARAM_ENTRY_VALUE="EntryValue";
	public static final String PARAM_OLD_ENTRY_VALUE="EntryValue";
	
	
	private String dynastoreName;
	private String eventType;
	public DynastoreEvent() {
		super();
	}
	public DynastoreEvent(String eventId,String dynaStoreName,String eventType, RequestContext reqContext) {
		super(eventId, reqContext);
		this.dynastoreName=dynaStoreName;
		this.eventType=eventType;
	}
	public String getDynastoreName() {
		return dynastoreName;
	}
	public void setDynastoreName(String dynastoreName) {
		this.dynastoreName = dynastoreName;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
}
