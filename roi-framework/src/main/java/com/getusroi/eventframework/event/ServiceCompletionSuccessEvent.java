package com.getusroi.eventframework.event;

import java.util.Date;

import com.getusroi.config.RequestContext;
//#TODO Move instance variable to ROIEvent header and param variable
public class ServiceCompletionSuccessEvent extends ROIEvent {
	private static final long serialVersionUID = 195566530802505078L;
	public static final String EVENTID="SERVICE_COMPLETION_SUCCESS";
	
	private String serviceType;
	private Date completedDtm;
	private String executedOnIPAddr;
	
	public ServiceCompletionSuccessEvent(RequestContext reqCtx) {
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

	@Override
	public String toString() {
		
	//	return "ServiceCompletionSuccessEvent [completedDtm=" + completedDtm + "]";
		
		return "ServiceCompletionSuccessEvent [serviceType=" + serviceType
				+ ", completedDtm=" + completedDtm + ", executedOnIPAddr="
				+ executedOnIPAddr + ", "+super.toString()+"]";
	}

	
	

}
