package com.getusroi.eventframework.config;

import java.io.Serializable;

import com.getusroi.config.beans.ConfigurationUnit;
import com.getusroi.eventframework.jaxb.DispatchChanel;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.eventframework.jaxb.EventSubscription;
import com.getusroi.eventframework.jaxb.SystemEvent;

public class EventFrameworkConfigurationUnit extends ConfigurationUnit {

	private static final long serialVersionUID = -2055488597843620428L;
	public static final String EVTFWK_EVENT_GROUPKEY_SUFFIX = "-EVT";
	public static final String EVTFWK_SYSEVENT_GROUPKEY_SUFFIX = "-SYSEVT";
	public static final String EVTFWK_DISPATCHCHANEL_GROUPKEY_SUFFIX = "-DISCHANEL";
	public static final String EVTFWK_EVENTSUBSCRIPTION_GROUPKEY_SUFFIX = "-SUBSCRIPTION";

	//EventProducer Key Suffix FOR Event Producer for Bean and For Service
	public static final String EVTFWK_EVENTPRODUCER_BEAN_GROUPKEY_SUFFIX = "-EPBEAN";
	public static final String EVTFWK_EVENTPRODUCER_SERVICE_GROUPKEY_SUFFIX = "-EPSERVICE";
	
	private String siteId;
	/**
	 * It is matches with the ConfigId in the database i.e confignodedata{table}.nodeDataId{column}
	 */
	private Integer dbconfigId;
	/** Id of the Node in db that this configuration is attached with */
	private Integer attachedNodeId;

	public EventFrameworkConfigurationUnit(String tenantId, String siteId, Integer attachedNodeId, Boolean isEnabled, Event eventConfig) {
		super(tenantId, eventConfig, isEnabled, eventConfig.getId(), getEventConfigGroupKey(attachedNodeId));
		this.siteId = siteId;
		this.attachedNodeId = attachedNodeId;
		
	}
	public EventFrameworkConfigurationUnit(String tenantId, String siteId, Integer attachedNodeId, Boolean isEnabled, SystemEvent eventConfig) {
		super(tenantId, eventConfig, isEnabled, eventConfig.getId(), getSystemEventConfigGroupKey(attachedNodeId));
		this.siteId = siteId;
		this.attachedNodeId = attachedNodeId;
		
	}
	public EventFrameworkConfigurationUnit(String tenantId, String siteId, Integer attachedNodeId, Boolean isEnabled, DispatchChanel dispatchChanelConfig) {
		super(tenantId, dispatchChanelConfig, isEnabled, dispatchChanelConfig.getId(), getDispatchChanelConfigGroupKey(attachedNodeId));
		this.siteId = siteId;
		this.attachedNodeId = attachedNodeId;
		
	}
	
	public EventFrameworkConfigurationUnit(String tenantId, String siteId, Integer attachedNodeId, Boolean isEnabled, EventSubscription eventSubscriptionConfig) {
		super(tenantId, eventSubscriptionConfig, isEnabled, eventSubscriptionConfig.getEventId(), getEventSubscriptionConfigGroupKey(attachedNodeId));
		this.siteId = siteId;
		this.attachedNodeId = attachedNodeId;
		
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

	public static String getEventConfigGroupKey(Integer attachedToNodeId) {
		String psGroupKey = attachedToNodeId.intValue() + EVTFWK_EVENT_GROUPKEY_SUFFIX;
		return psGroupKey;
	}
	
	public static String getEventProcucerForBeanGroupKey(Integer attachedToNodeId) {
		String psGroupKey = attachedToNodeId.intValue() + EVTFWK_EVENTPRODUCER_BEAN_GROUPKEY_SUFFIX;
		return psGroupKey;
	}
	
	public static String getEventProcucerForServiceGroupKey(Integer attachedToNodeId) {
		String psGroupKey = attachedToNodeId.intValue() + EVTFWK_EVENTPRODUCER_SERVICE_GROUPKEY_SUFFIX;		
		return psGroupKey;
	}

	public static String getSystemEventConfigGroupKey(Integer attachedToNodeId) {
		String psGroupKey = attachedToNodeId.intValue() + EVTFWK_SYSEVENT_GROUPKEY_SUFFIX;
		return psGroupKey;
	}

	public static String getDispatchChanelConfigGroupKey(Integer attachedToNodeId) {
		String psGroupKey = attachedToNodeId.intValue() + EVTFWK_DISPATCHCHANEL_GROUPKEY_SUFFIX;
		return psGroupKey;
	}
	
	public static String getEventSubscriptionConfigGroupKey(Integer attachedToNodeId) {
		String psGroupKey = attachedToNodeId.intValue() + EVTFWK_EVENTSUBSCRIPTION_GROUPKEY_SUFFIX;
		return psGroupKey;
	}

	public EventFrameworkConfigurationUnit(String tenantId, Serializable configData, Boolean isEnabled, String key, String configGroup) {
		super(tenantId, configData, isEnabled, key, configGroup);

	}

}
