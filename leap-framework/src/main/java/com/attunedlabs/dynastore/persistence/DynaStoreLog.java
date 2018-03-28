package com.attunedlabs.dynastore.persistence;

import java.sql.Date;


public class DynaStoreLog {
	
	
	private int 	siteId;
	private String	sessionId;
	private Date opendDTM;
	private Date closedDTM;
	private String status;
	private String info;
	
	
	
	public int getSiteId() {
		return siteId;
	}
	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public Date getOpendDTM() {
		return opendDTM;
	}
	public void setOpendDTM(Date opendDTM) {
		this.opendDTM = opendDTM;
	}
	public Date getClosedDTM() {
		return closedDTM;
	}
	public void setClosedDTM(Date closedDTM) {
		this.closedDTM = closedDTM;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	
	@Override
	public String toString() {
		return "DynaStoreLog [siteId=" + siteId + ", sessionId=" + sessionId
				+ ", opendDTM=" + opendDTM + ", closedDTM=" + closedDTM
				+ ", status=" + status + ", info=" + info + "]";
	}
}
