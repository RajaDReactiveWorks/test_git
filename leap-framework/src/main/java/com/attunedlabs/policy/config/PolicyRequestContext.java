package com.attunedlabs.policy.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.attunedlabs.config.RequestContext;



public class PolicyRequestContext extends RequestContext implements Serializable{
	private static final long serialVersionUID = -2630182623680068637L;

	/** Containes the variables/objects that are used for the Policy Evaluation */
	private Map<String,Object> requestVariable;
	
	public PolicyRequestContext(String tenantId, String siteId, String featureGroup) {
		super(tenantId, siteId, featureGroup);
		
	}

	public PolicyRequestContext(String tenantId, String siteId, String featureGroup, String featureName,String implName) {
		super(tenantId, siteId, featureGroup, featureName,implName);
		
	}
	public PolicyRequestContext(String tenantId, String siteId, String featureGroup, String featureName,String implName,String vendor,String version) {
		super(tenantId, siteId, featureGroup, featureName,implName,vendor,version);
		
	}
	
	public Map<String, Object> getRequestVariable() {
		return requestVariable;
	}

	public void setRequestVariable(Map<String, Object> requestVariable) {
		this.requestVariable = requestVariable;
	}
	
	public void addRequestVariable(String varName,Object varValue) {
		if(this.requestVariable==null)
			this.requestVariable=new HashMap();
		//variable should start with $
		if(!varName.startsWith("$"))
			this.requestVariable.put("$"+varName, varValue);
		else
			this.requestVariable.put(varName, varValue);
	}
}