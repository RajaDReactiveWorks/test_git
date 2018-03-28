package com.attunedlabs.eventframework.eventtracker;

import java.util.Date;

/**
 * <code>EventDispatcherTracker</code> keeps track of all the dispatching
 * eventingList.
 * 
 * @author Reactiveworks42
 *
 */
public class EventDispatcherTracker {
	private String tenantId;
	private String siteId;
	private String requestId;
	private String eventStoreId;
	private Date eventCreatedDTM;
	private Date lastFailureDTM;
	private String status;
	private String failureReason;
	private Integer retryCount;

	/**
	 * @return the tenantId
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * @param tenantId
	 *            the tenantId to set
	 */
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * @return the siteId
	 */
	public String getSiteId() {
		return siteId;
	}

	/**
	 * @param siteId
	 *            the siteId to set
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/**
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @param requestId
	 *            the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * @return the eventStoreId
	 */
	public String getEventStoreId() {
		return eventStoreId;
	}

	/**
	 * @param eventStoreId
	 *            the eventStoreId to set
	 */
	public void setEventStoreId(String eventStoreId) {
		this.eventStoreId = eventStoreId;
	}

	/**
	 * @return the eventCreatedDTM
	 */
	public Date getEventCreatedDTM() {
		return eventCreatedDTM;
	}

	/**
	 * @param eventCreatedDTM
	 *            the eventCreatedDTM to set
	 */
	public void setEventCreatedDTM(Date eventCreatedDTM) {
		this.eventCreatedDTM = eventCreatedDTM;
	}

	/**
	 * @return the lastFailureDTM
	 */
	public Date getLastFailureDTM() {
		return lastFailureDTM;
	}

	/**
	 * @param lastFailureDTM
	 *            the lastFailureDTM to set
	 */
	public void setLastFailureDTM(Date lastFailureDTM) {
		this.lastFailureDTM = lastFailureDTM;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the failureReason
	 */
	public String getFailureReason() {
		return failureReason;
	}

	/**
	 * @param failureReason
	 *            the failureReason to set
	 */
	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	/**
	 * @return the retryCount
	 */
	public Integer getRetryCount() {
		return retryCount;
	}

	/**
	 * @param retryCount
	 *            the retryCount to set
	 */
	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EventDispatcherTracker [tenantId=" + tenantId + ", siteId=" + siteId + ", requestId=" + requestId
				+ ", eventStoreId=" + eventStoreId + ", eventCreatedDTM=" + eventCreatedDTM + ", lastFailureDTM="
				+ lastFailureDTM + ", status=" + status + ", failureReason=" + failureReason + ", retryCount="
				+ retryCount + "]";
	}

}
