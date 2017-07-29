package com.getusroi.eventframework.event;

import java.util.Date;

import com.getusroi.config.RequestContext;
//#TODO Move from instance variable to event header and event param in the base ROI
public class ServiceCompletionFailureEvent extends ROIEvent {
	private static final long serialVersionUID = 195566530802505078L;
	public static final String EVENTID="SERVICE_COMPLETION_FAILURE";
	
	private String serviceType;
	private Date completedDtm;
	private String executedOnIPAddr;
	private String failedRouteId;
	
	public ServiceCompletionFailureEvent(RequestContext reqCtx) {
		super(EVENTID,reqCtx);
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public Date getCompletedDtm() {
		return completedDtm;
	}

	public void setCompletedDtm(Date completedDtm) {
		this.completedDtm = completedDtm;
	}

	public String getExecutedOnIPAddr() {
		return executedOnIPAddr;
	}

	public void setExecutedOnIPAddr(String executedOnIPAddr) {
		this.executedOnIPAddr = executedOnIPAddr;
	}

	public String getFailedRouteId() {
		return failedRouteId;
	}

	public void setFailedRouteId(String failedRouteId) {
		this.failedRouteId = failedRouteId;
	}

	@Override
	public String toString() {
		return "ServiceCompletionFailureEvent [Failed to complete the process :  completedDtm=" + completedDtm + ", executedOnIPAddr="
				+ executedOnIPAddr + ", failedRouteId=" + failedRouteId + "]";		
		/*return "ServiceCompletionFailureEvent [serviceType=" + serviceType
				+ ", completedDtm=" + completedDtm + ", executedOnIPAddr="
				+ executedOnIPAddr + ", failedRouteId=" + failedRouteId + ", "+super.toString()+"]";*/
	}


	
	

}
