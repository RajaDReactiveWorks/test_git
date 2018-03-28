package com.attunedlabs.eventsubscriptiontracker;

import java.io.Serializable;
import java.util.Date;

/**
 * <code>EventSubscriptionTracker</code> keeps track of all the subscription
 * event pulled from kafka. Also maintains the <b><i>Metadata</i></b> of reords.
 * 
 * @author Reactiveworks42
 *
 */
public class EventSubscriptionTracker implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String tenantId;
	private String siteId;
	private String subscriptionId;
	private String topic;
	private String partition;
	private String offset;
	private String eventData;
	private Date eventFetchedDTM;
	private Date lastFailureDTM;
	private String failureMsg;
	private String status;
	private Boolean isRetryable;
	private Integer retryCount;

	/**
	 * @return the subscriptionId
	 */
	public String getSubscriptionId() {
		return subscriptionId;
	}

	/**
	 * @param subscriptionId
	 *            the subscriptionId to set
	 */
	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @param topic
	 *            the topic to set
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * @return the partition
	 */
	public String getPartition() {
		return partition;
	}

	/**
	 * @param partition
	 *            the partition to set
	 */
	public void setPartition(String partition) {
		this.partition = partition;
	}

	/**
	 * @return the offset
	 */
	public String getOffset() {
		return offset;
	}

	/**
	 * @param offset
	 *            the offset to set
	 */
	public void setOffset(String offset) {
		this.offset = offset;
	}

	/**
	 * @return the eventData
	 */
	public String getEventData() {
		return eventData;
	}

	/**
	 * @param eventData
	 *            the eventData to set
	 */
	public void setEventData(String eventData) {
		this.eventData = eventData;
	}

	/**
	 * @return the eventFetchedDTM
	 */
	public Date getEventFetchedDTM() {
		return eventFetchedDTM;
	}

	/**
	 * @param eventFetchedDTM
	 *            the eventFetchedDTM to set
	 */
	public void setEventFetchedDTM(Date eventFetchedDTM) {
		this.eventFetchedDTM = eventFetchedDTM;
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
	 * @return the failureMsg
	 */
	public String getFailureMsg() {
		return failureMsg;
	}

	/**
	 * @param failureMsg
	 *            the failureMsg to set
	 */
	public void setFailureMsg(String failureMsg) {
		this.failureMsg = failureMsg;
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
	 * @return the isRetryable
	 */
	public Boolean getIsRetryable() {
		return isRetryable;
	}

	/**
	 * @param isRetryable
	 *            the isRetryable to set
	 */
	public void setIsRetryable(Boolean isRetryable) {
		this.isRetryable = isRetryable;
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
	 * Returns the column's as property.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EventSubscriptionTracker [tenantId=" + tenantId + ", siteId=" + siteId + ", subscriptionId="
				+ subscriptionId + ", topic=" + topic + ", partition=" + partition + ", offset=" + offset
				+ ", eventData=" + eventData + ", eventFetchedDTM=" + eventFetchedDTM + ", lastFailureDTM="
				+ lastFailureDTM + ", failureMsg=" + failureMsg + ", status=" + status + ", isRetryable=" + isRetryable
				+ ", retryCount=" + retryCount + "]";
	}

}
