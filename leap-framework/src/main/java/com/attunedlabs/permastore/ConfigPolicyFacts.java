package com.attunedlabs.permastore;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigPolicyFacts {
	private Map<String,String> facts;
	
	public ConfigPolicyFacts(){
		facts=new LinkedHashMap<>();
	}
	
	public Map getFacts() {
		return facts;
	}

	public void setFacts(Map facts) {
		this.facts = facts;
	}
	
	public String getFact(String factName){
		return facts.get(factName);
	}
	public String addFact(String factName,String factType){
		return facts.put(factName,factType);
	}

	@Override
	public String toString() {
		return "ConfigPolicyFacts [facts=" + facts + "]";
	}
	
}
