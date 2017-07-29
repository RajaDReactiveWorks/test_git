package com.getusroi.eventframework.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.getusroi.config.RequestContext;



public class ROIEvent implements Serializable {
	public static final long serialVersionUID = -8905889714275195707L;
	public static final String EVENT_CONTEXT_KEY="EVT_CONTEXT";
	
	private String eventId;
		
	private Map<String, Serializable> eventHeader;
	private Map<String, Serializable> eventParam;
	

	public ROIEvent() {
	}

	public ROIEvent(String eventId,RequestContext reqContext) {
		eventHeader = new HashMap<String, Serializable>();
		eventParam = new HashMap<String, Serializable>();
		this.eventId = eventId;
		eventHeader.put(EVENT_CONTEXT_KEY,reqContext);
	}


	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public RequestContext getRequestContext(){
		RequestContext rq=(RequestContext)eventHeader.get(EVENT_CONTEXT_KEY);
		return rq;
	}
	
	public void setRequestContext(RequestContext rq){
		eventHeader.put(EVENT_CONTEXT_KEY,rq);
	}
	
	public Map<String, Serializable> getEventParam() {
		return eventParam;
	}

	public void setEventParam(Map<String, Serializable> eventParam) {
		this.eventParam = eventParam;
	}
	
	public Map<String, Serializable> getEventHeader() {
		return eventHeader;
	}

	public void setEventHeader(Map<String, Serializable> eventHeaderParam) {
		this.eventHeader = eventHeaderParam;
	}

	public void addEventHeader(String headerParamKey, Serializable value) {
		eventHeader.put(headerParamKey, value);
	}

	public void addEventParam(String eventParamKey, Serializable value) {
		eventParam.put(eventParamKey, value);
	}

	@Override
	public String toString() {
		return "ROIEvent [eventId=" + eventId + ", eventHeader=" + eventHeader + ", eventParam=" + eventParam + "]";
	}

}
