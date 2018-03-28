package com.attunedlabs.policy.config.exp.sqltomvel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PolicyCompiledExpression implements Serializable {
	private static final long serialVersionUID = -2508504473199516776L;
	private String expName;
	private List<String> reqVarList;
	private List<String> psVarList;
	private String expression;
	

	public String getExpName() {
		return expName;
	}

	public void setExpName(String expName) {
		this.expName = expName;
	}

	public List<String> getReqVarList() {
		return reqVarList;
	}

	public void setReqVarList(List<String> reqVarList) {
		this.reqVarList = reqVarList;
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

	public List<String> getPsVarList() {
		return psVarList;
	}

	public void setPsVarList(List<String> psVarList) {
		this.psVarList = psVarList;
	}
	
	public void addPSVar(String psVarName) {
		if(psVarList==null)
			psVarList=new ArrayList();
		//Check for Duplicate before Adding
		if(!psVarList.contains(psVarName)){
			this.psVarList.add(psVarName);
		}
		
	}

	
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	
	@Override
	public String toString() {
		return "PolicyCompiledExpression [expName=" + expName + ", reqVarList=" + reqVarList + ", psVarList=" + psVarList + ", expression="
				+ expression + "]";
	}

	
}
