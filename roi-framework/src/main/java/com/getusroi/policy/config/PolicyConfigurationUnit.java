package com.getusroi.policy.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.getusroi.config.beans.ConfigurationUnit;
import com.getusroi.policy.jaxb.Policy;

/**
 * Configuration Unit that is Cached in DataGrid for Policy 
 * @author bizruntime
 *
 */
public class PolicyConfigurationUnit extends ConfigurationUnit implements Serializable {
	private static final long serialVersionUID = 6560195926116793807L;
	public static final String POLICYCONFIG_GROUPKEY_SUFFIX="-POL";
	
	private String siteId;
	/** It  matches with the ConfigId in the database i.e confignodedata{table}.nodeDataId{column}	 */
	private Integer dbconfigId;
	/** Id of the Node in db that this configuration is attached with */
	private Integer attachedNodeId; 
	/** Stores all the Evaluations with MVEL expression in the policy config ordered by Salience */
	private List<PolicyEvaluationConfigurationUnit> evaluationUnitList;
	/** Response Type 1-PolicyDefinedFact 2-Mapped Fact to PermaStore	 
	 *  if-1-policyDefined Fact than stored in this.configData
	 *  if 2-Mapped Fact than the PermaStore Name is stored in this.configData
	 * */
	private String reponseType;
	
	
	public PolicyConfigurationUnit(String tenantId,String siteId,Integer configNodeId,boolean isEnabled,Policy policy,Serializable objToCache){
		super(tenantId,objToCache,isEnabled,policy.getPolicyName(), getConfigGroupKey(configNodeId));
		this.siteId=siteId;
		this.attachedNodeId=configNodeId;
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

	public List<PolicyEvaluationConfigurationUnit> getEvaluationUnitList() {
		return evaluationUnitList;
	}

	public void setEvaluationUnitList(List<PolicyEvaluationConfigurationUnit> evaluationUnitList) {
		this.evaluationUnitList = evaluationUnitList;
	}
	public void addEvaluationUnit(PolicyEvaluationConfigurationUnit evaluationUnit){
		if(this.evaluationUnitList==null){
			this.evaluationUnitList=new ArrayList();
		}
		this.evaluationUnitList.add(evaluationUnit);
	}
	
	public String getReponseType() {
		return reponseType;
	}

	public void setReponseType(String reponseType) {
		this.reponseType = reponseType;
	}
	
	/**
	 * Util Method to generate the Policy Configuration Group Key
	 * @param attachedToNodeId
	 * @return
	 */
	public static String getConfigGroupKey(Integer attachedToNodeId){
		String psGroupKey = attachedToNodeId.intValue() + POLICYCONFIG_GROUPKEY_SUFFIX;
		return psGroupKey;
	}
	
}
