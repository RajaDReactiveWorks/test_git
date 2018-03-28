package com.attunedlabs.scheduler;

import org.json.JSONObject;

public class ScheduledJobData {

	private int jobId;
	private String jobName;
//	private String description;
	private String jobService;
	private String jobType;
	private String featureGroup;
	private String feature;
	private String jobContextDetail;
	private JSONObject schedulingExpresssion;
	private String createdDTM;
	private boolean isEnabled;
	private boolean allApplicableTenant;
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getJobService() {
		return jobService;
	}
	public void setJobService(String jobService) {
		this.jobService = jobService;
	}
	public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	public String getJobContextDetail() {
		return jobContextDetail;
	}
	public void setJobContextDetail(String jobContextDetail) {
		this.jobContextDetail = jobContextDetail;
	}
	public JSONObject getSchedulingExpresssion() {
		return schedulingExpresssion;
	}
	public void setSchedulingExpresssion(JSONObject schedulingExpr) {
		this.schedulingExpresssion = schedulingExpr;
	}
	public String getCreatedDTM() {
		return createdDTM;
	}
	public void setCreatedDTM(String createdDTM) {
		this.createdDTM = createdDTM;
	}
	public boolean isEnabled() {
		return isEnabled;
	}
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public boolean isAllApplicableTenant() {
		return allApplicableTenant;
	}
	public void setAllApplicableTenant(boolean allApplicableTenant) {
		this.allApplicableTenant = allApplicableTenant;
	}
	@Override
	public String toString() {
		return "ScheduledJobData [jobId=" + jobId + ", jobName=" + jobName + ", jobService=" + jobService + ", jobType="
				+ jobType + ", jobContextDetail=" + jobContextDetail + ", schedulingExpresssion="
				+ schedulingExpresssion + ", createdDTM=" + createdDTM + ", isEnabled=" + isEnabled
				+ ", allApplicableTenant=" + allApplicableTenant + "]";
	}
	public String getFeatureGroup() {
		return featureGroup;
	}
	public void setFeatureGroup(String featureGroup) {
		this.featureGroup = featureGroup;
	}
	public String getFeature() {
		return feature;
	}
	public void setFeature(String feature) {
		this.feature = feature;
	}

	

}
