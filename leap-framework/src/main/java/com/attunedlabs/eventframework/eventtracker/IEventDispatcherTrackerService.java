package com.attunedlabs.eventframework.eventtracker;

import java.util.List;

import org.apache.camel.Exchange;

import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerException;

public interface IEventDispatcherTrackerService {
	/**
	 * addEventTracking will add the eventStoreKey,tenant,site and requestId for
	 * every request in EventTrackerTable and initialize the Processing Status
	 * to NEW whereas eventCreatedTime based on the current SystemTime.
	 * addEventTracking is under global transaction.
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param requestId
	 * @param eventStoreKey
	 *            key for hazelcast leapEventList
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @return true if added to trackTable or else false.
	 * @throws EventDispatcherTrackerException
	 */
	public boolean addEventTracking(String tenantId, String siteId, String requestId, String eventStoreKey,
			Exchange camelExchange) throws EventDispatcherTrackerException;

	/**
	 * updateEventStatus will update processingStatus for eventlist associate
	 * with eventStoreKey and will also update the failureMsg and retryCount if
	 * isFailure flag is true.
	 * 
	 * @param processingStatus
	 *            all the available processingStatus are listed as :
	 *            <code>NEW</code>, <code>IN_PROCESS</code>,
	 *            <code>FAILED</code>, <code>RETRY_INPROCESS</code>,
	 *            <code>RETRY_FAILED</code>, <code>COMPLETE</code>.
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @param isFailure
	 *            flag to set for failure update on lastFailureDTM.
	 * @param failureMsg
	 *            reason for failure
	 * @param isFailedNow
	 *            for keeping watch on lastFailureDTM
	 * @param isRetried
	 *            used to update the retry count.
	 * @return true if updated to trackTable status or else false.
	 * @throws EventDispatcherTrackerException
	 */
	public boolean updateEventStatus(String tenantId, String siteId, String requestId, String eventStoreKey,
			String processingStatus, Exchange camelExchange, boolean isFailure, String failureMsg, boolean isFailedNow,
			boolean isRetried) throws EventDispatcherTrackerException;

	/**
	 * getRetryCount will give you the retried value for the event list present
	 * for specified eventStoreKey.
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param requestId
	 * @param eventStoreKey
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @return retryCount
	 * @throws EventDispatcherTrackerException
	 */
	public Integer getRetryCount(String tenantId, String siteId, String requestId, String eventStoreKey,
			Exchange camelExchange) throws EventDispatcherTrackerException;

	/**
	 * getTrackStatusForEventList will give you the latest status for the event
	 * list present for specified eventStoreKey.
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param requestId
	 * @param eventStoreKey
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @return trackStatus
	 * @throws EventDispatcherTrackerException
	 */
	public String getTrackStatusForEventList(String tenantId, String siteId, String requestId, String eventStoreKey,
			Exchange camelExchange) throws EventDispatcherTrackerException;

	/**
	 * removeEventTrackRecord will delete the event record from the track table
	 * if event status is <code>COMPLETE<code>.
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param requestId
	 * @param eventStoreKey
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @return true if deleted eventRecord from trackTable or else false.
	 * @throws EventDispatcherTrackerException
	 */
	public boolean removeEventTrackRecord(String tenantId, String siteId, String requestId, String eventStoreKey,
			Exchange camelExchange) throws EventDispatcherTrackerException;

	/**
	 * getAllTrackRecordsOnStatus will provide you all the records irrespective
	 * to the processingStatus provided.
	 * 
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @return list of all <code>EventDispatcherTracker</code>.
	 * @throws EventDispatcherTrackerException
	 */
	public List<EventDispatcherTracker> getAllTrackRecords(Exchange camelExchange)
			throws EventDispatcherTrackerException;

	/**
	 * getAllTrackRecordsOnStatus will provide you all the records for provided
	 * processingStatus.
	 * 
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @param processingStatus
	 *            all the available processingStatus are listed as :
	 *            <code>NEW</code>, <code>IN_PROCESS</code>,
	 *            <code>FAILED</code>, <code>RETRY_INPROCESS</code>,
	 *            <code>RETRY_FAILED</code>, <code>COMPLETE</code>.
	 * @return list of <code>EventDispatcherTracker</code> filtered on above
	 *         processingStatus or else if processing status doesn't match empty
	 *         list will be returned.
	 * @throws EventDispatcherTrackerException
	 */
	public List<EventDispatcherTracker> getAllTrackRecordsOnStatus(Exchange camelExchange, String processingStatus)
			throws EventDispatcherTrackerException;

	/**
	 * getAllTrackRecordsIntitializedForLongTime will return you the list of all
	 * the <code>EventDispatcherTracker</code> which are in NEW processing state
	 * for long time form the time event is created assuming that abruptShutdown
	 * occurred.(considered 2 minutes as long time)
	 * 
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @return list of <code>EventDispatcherTracker</code> which are NEW for
	 *         more than 2 minutes form the event created time.
	 * @throws EventDispatcherTrackerException
	 */
	public List<EventDispatcherTracker> getAllTrackRecordsIntitializedForLongTime(Exchange camelExchange)
			throws EventDispatcherTrackerException;

	/**
	 * getAllTrackRecordsInProcessForLongTime will return you the list of all
	 * the <code>EventDispatcherTracker</code> which are IN_PROCESS or
	 * RETRY_INPROCESS processing state for long time form the time event is
	 * created.(considered 2 minutes as long time)
	 * 
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @param processingStatus
	 *            processingStatus should be either : <code>IN_PROCESS</code>,
	 *            <code>RETRY_INPROCESS</code> based on the state of processing
	 *            list of track record will be returned.
	 * @return list of <code>EventDispatcherTracker</code> which are IN_PROCESS
	 *         or RETRY_INPROCESS for more than 2 minutes form the event created
	 *         time.
	 * @throws EventDispatcherTrackerException
	 */
	public List<EventDispatcherTracker> getAllTrackRecordsInProcessForLongTime(Exchange camelExchange,
			String processingStatus) throws EventDispatcherTrackerException;

	/**
	 * getAllTrackRecordsInProcessForLongTimeArrangedByRetry will return you the
	 * list of all the <code>EventDispatcherTracker</code> which are IN_PROCESS
	 * or RETRY_INPROCESS processing state for long time form the time event is
	 * created and based on the retries performed.(considered 2 minutes as long
	 * time)
	 * 
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @param processingStatus
	 *            processingStatus should be either : <code>IN_PROCESS</code>,
	 *            <code>RETRY_INPROCESS</code> based on the state of processing
	 *            list of track record will be returned.
	 * @return list of <code>EventDispatcherTracker</code> which are IN_PROCESS
	 *         or RETRY_INPROCESS for more than 2 minutes form the event created
	 *         time and based on retries performed.
	 * @throws EventDispatcherTrackerException
	 */
	public List<EventDispatcherTracker> getAllTrackRecordsInProcessForLongTimeArrangedByRetry(Exchange camelExchange,
			String processingStatus) throws EventDispatcherTrackerException;

	/**
	 * getAllFailedEventRecordsArrangedByFailureTime will provide you all the
	 * records arranged by lastFailureTime.
	 * 
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @return list of <code>EventDispatcherTracker</code> filtered on FAILED or
	 *         else empty list will be returned.
	 * @throws EventDispatcherTrackerException
	 */
	public List<EventDispatcherTracker> getAllFailedEventRecordsArrangedByFailureTime(Exchange camelExchange)
			throws EventDispatcherTrackerException;

	/**
	 * getAllFailedEventRecordsArrangedByFailureTimeAndRetryCount will provide
	 * you all the records arranged by lastFailureTime and retry count.
	 * 
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @return list of <code>EventDispatcherTracker</code> filtered on FAILED or
	 *         else empty list will be returned.
	 * @throws EventDispatcherTrackerException
	 */
	public List<EventDispatcherTracker> getAllFailedEventRecordsArrangedByFailureTimeAndRetryCount(
			Exchange camelExchange) throws EventDispatcherTrackerException;

	/**
	 * getAllRetryFailedEventRecordsArrangedByFailureTime will provide you all
	 * the records arranged by lastFailureTime.
	 * 
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @return list of <code>EventDispatcherTracker</code> filtered on
	 *         RETRY_FAILED or else empty list will be returned.
	 * @throws EventDispatcherTrackerException
	 */
	public List<EventDispatcherTracker> getAllRetryFailedEventRecordsArrangedByFailureTime(Exchange camelExchange)
			throws EventDispatcherTrackerException;

	/**
	 * getAllRetryFailedEventRecordsArrangedByFailureTimeAndRetryCount will
	 * provide you all the records arranged by lastFailureTime and retryCount.
	 * 
	 * @param camelExchange
	 *            to get DataSource by lookup
	 * @return list of <code>EventDispatcherTracker</code> filtered on
	 *         RETRY_FAILED or else empty list will be returned.
	 * @throws EventDispatcherTrackerException
	 */
	public List<EventDispatcherTracker> getAllRetryFailedEventRecordsArrangedByFailureTimeAndRetryCount(
			Exchange camelExchange) throws EventDispatcherTrackerException;

	/**
	 * getEventRecordOnRequestUUID will return you TrackDetails of generated
	 * events on particular request based on requestID passed.
	 * 
	 * @param requestUUID
	 *            : request Id generated for request
	 * @return <code>EventDispatcherTracker</code>
	 * @throws EventDispatcherTrackerException
	 */
	public EventDispatcherTracker getEventRecordOnRequestUUID(Exchange camelExchange,String requestUUID)
			throws EventDispatcherTrackerException;

}
