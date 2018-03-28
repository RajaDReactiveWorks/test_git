package com.attunedlabs.policy.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PolicyEvaluationConfigurationUnit implements Serializable {
	private static final long serialVersionUID = -7468949968558833780L;
	
	private int salience;
	private String dialect;
	private String expression;
	private List<String> reqVarList;
	private List<String> psVarList;
	
	
	public int getSalience() {
		return salience;
	}
	public void setSalience(int salience) {
		this.salience = salience;
	}
	public String getDialect() {
		return dialect;
	}
	public void setDialect(String dialect) {
		this.dialect = dialect;
	}
	
	public List<String> getReqVarList() {
		return reqVarList;
	}
	public void setReqVarList(List<String> reqVarList) {
		this.reqVarList = reqVarList;
	}
	public List<String> getPsVarList() {
		return psVarList;
	}
	public void setPsVarList(List<String> psVarList) {
		this.psVarList = psVarList;
	}
	
	public void addReqVar(List<String> reqVarListToAdd){
		if(reqVarListToAdd==null || reqVarListToAdd.isEmpty())
			return;
		for(String reqVar:reqVarListToAdd){
			addReqVar(reqVar);
		}
	}
	
	public void addReqVar(String reqVarName) {
		if(reqVarList==null){
			reqVarList=new ArrayList();
		}
		//Check for Duplicate
		if(!reqVarList.contains(reqVarName)){
			this.reqVarList.add(reqVarName);
		}
	}

	public void addPSVar(String psVarName) {
		if(psVarList==null)
			psVarList=new ArrayList();
		//Check for Duplicate before Adding
		if(!psVarList.contains(psVarName)){
			this.psVarList.add(psVarName);
		}
		
	}
	
	public void addPSVar(List<String> psVarNameListToAdd) {
		if(psVarNameListToAdd==null || psVarNameListToAdd.isEmpty())
			return;
		for(String psVar:psVarNameListToAdd){
			addPSVar(psVar);
		}
		
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	@Override
	public String toString() {
		return "PolicyEvaluationConfigurationUnit [salience=" + salience + ", dialect=" + dialect + ", expression=" + expression
				+ ", reqVarList=" + reqVarList + ", psVarList=" + psVarList + "]";
	}
	
	
}
