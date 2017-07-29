package com.getusroi.eventframework.config;

import java.util.HashMap;
import java.util.Map;

import com.hazelcast.transaction.TransactionContext;

public class EventRequestContext {
	
	private String tenant;
	private String site;
	private String servicetype;
	private String featureGroup;
	private String featureName;
	private String operation;
	private String requestUUID;
	private TransactionContext hazelcastTransactionalContext;
	
	Map<String,Object> eventdata=new HashMap<>();

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getServicetype() {
		return servicetype;
	}

	public void setServicetype(String servicetype) {
		this.servicetype = servicetype;
	}

	public String getFeatureGroup() {
		return featureGroup;
	}

	public void setFeatureGroup(String featureGroup) {
		this.featureGroup = featureGroup;
	}

	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getRequestUUID() {
		return requestUUID;
	}

	public void setRequestUUID(String requestUUID) {
		this.requestUUID = requestUUID;
	}

	public TransactionContext getHazelcastTransactionalContext() {
		return hazelcastTransactionalContext;
	}

	public void setHazelcastTransactionalContext(TransactionContext hazelcastTransactionalContext) {
		this.hazelcastTransactionalContext = hazelcastTransactionalContext;
	}

	public Map<String, Object> getEventdata() {
		return eventdata;
	}

	public void setEventdata(Map<String, Object> eventdata) {
		this.eventdata = eventdata;
	}

	@Override
	public String toString() {
		return "EventRequestContext [tenant=" + tenant + ", site=" + site + ", servicetype=" + servicetype + ", featureGroup=" + featureGroup
				+ ", featureName=" + featureName + ", operation=" + operation + ", requestUUID=" + requestUUID + ", hazelcastTransactionalContext="
				+ hazelcastTransactionalContext + ", eventdata=" + eventdata + "]";
	}
	
	


}
