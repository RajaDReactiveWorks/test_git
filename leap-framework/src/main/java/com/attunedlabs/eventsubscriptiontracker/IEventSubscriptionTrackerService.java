package com.attunedlabs.eventsubscriptiontracker;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.json.simple.JSONObject;

import com.attunedlabs.eventsubscription.exception.EventSubscriptionTrackerException;
import com.attunedlabs.eventsubscription.exception.SubscriptionTableExistenceException;

public interface IEventSubscriptionTrackerService {

	/**
	 * creating EventSubscriptionTracker table for subscription if already
	 * doesn't exists.
	 * 
	 * @return tableCreationStatus
	 * @throws SubscriptionTableExistenceException
	 */
	public boolean createTrackerTableForSubscription() throws SubscriptionTableExistenceException;

	/**
	 * This method first checks wether the same record is already present.
	 * 
	 * @param exchange
	 *            : propogated exchange to get DataSource by lookup
	 * @param metaData
	 * @return : isPresent ? true : false
	 */
	public boolean recordIsNotAlreadyPresent(Exchange exchange, Map<String, Object> metaData);

	/**
	 * This method adds the fresh record to the EventSubscriptionTracker table
	 * and marks status as NEW.
	 * 
	 * @param exchange
	 *            : propogated exchange to get DataSource by lookup
	 * @param recordsDetails
	 *            : MetaData of record.
	 * @return insertionStatus
	 */
	public boolean addNewSubscriptionRecord(Exchange exchange, Map<String, Object> recordsDetails);

	/**
	 * This method does is used to update the status of the fresh record at each
	 * stage. Various stages are listed as <code>NEW</code>,
	 * <code>IN_PROCESS</code>, <code>FAILED</code>,
	 * <code>RETRY_INPROCESS</code>,<code>RETRY_FAILED</code>,<code>SUCCESS</code>
	 * 
	 * @param exchange
	 *            : propogated exchange to get DataSource by lookup
	 * @param recordsDetails
	 *            : MetaData of record.
	 * @param trackStatus
	 *            : <code>NEW</code>, <code>IN_PROCESS</code>,
	 *            <code>FAILED</code>,
	 *            <code>RETRY_INPROCESS</code>,<code>RETRY_FAILED</code>,<code>SUCCESS</code>
	 * @param exception
	 *            : if null than noRetry else retry attempted based on exception
	 *            thrown.
	 * @param retryConfigurationJSON
	 * @return updationStatus
	 */
	public boolean updateSubscriptionRecordStatus(Exchange exchange, Map<String, Object> recordsDetails,
			String trackStatus, Exception exception, JSONObject retryConfigurationJSON);

	/**
	 * getAllSubscriptionRecordsIntitializedForLongTime will return you the list
	 * of all the <code>EventSubscriptionTracker</code> which are in NEW
	 * processing state for long time form the time event is created assuming
	 * that abruptShutdown occurred.(considered 2 minutes as long time)
	 * 
	 * @param camelExchange
	 *            : propogated exchange to get DataSource by lookup
	 * @param tenantId
	 * @param siteId
	 * @param subscriptionId
	 * @param retryConfigurationJSON
	 * @return list of <code>EventSubscriptionTracker</code> which are NEW for
	 *         more than 2 minutes form the eventFetchedTime.
	 * @throws EventSubscriptionTrackerException
	 */
	public List<EventSubscriptionTracker> getAllSubscriptionRecordsIntitializedForLongTime(Exchange camelExchange,
			String tenantId, String siteId, String subscriptionId, JSONObject retryConfigurationJSON)
			throws EventSubscriptionTrackerException;

	/**
	 * getAllSubscriptionRecordsInProcessForLongTimeArrangedByRetry will return
	 * you the list of all the <code>EventSubscriptionTracker</code> which are
	 * IN_PROCESS or RETRY_INPROCESS processing state for long time form the
	 * time event is created and based on the retries performed.(considered 2
	 * minutes as long time)
	 * 
	 * @param camelExchange
	 *            : propogated exchange to get DataSource by lookup
	 * @param tenantId
	 * @param siteId
	 * @param subscriptionId
	 * @param processingStatus
	 *            processingStatus should be either : <code>IN_PROCESS</code>,
	 *            <code>RETRY_INPROCESS</code> based on the state of processing
	 *            list of track record will be returned.
	 * @param retryConfigurationJSON
	 * @return list of <code>EventSubscriptionTracker</code> which are
	 *         IN_PROCESS or RETRY_INPROCESS for more than 2 minutes form the
	 *         event created time and based on retries performed.
	 * @throws EventSubscriptionTrackerException
	 */
	public List<EventSubscriptionTracker> getAllSubscriptionRecordsInProcessForLongTimeArrangedByRetry(
			Exchange camelExchange, String tenantId, String siteId, String subscriptionId, String processingStatus,
			JSONObject retryConfigurationJSON) throws EventSubscriptionTrackerException;

	/**
	 * getAllFailedSubscriptionRecordsArrangedByFailureTimeAndRetryCount will
	 * provide you all the records arranged by lastFailureTime and retry count.
	 * 
	 * @param camelExchange
	 *            : propogated exchange to get DataSource by lookup
	 * @param statusRetryFailed
	 * @param tenantId
	 * @param siteId
	 * @param subscriptionId
	 * @param retryConfigurationJSON
	 * @return list of <code>EventSubscriptionTracker</code> filtered on FAILED
	 *         and RETRY_FAILED or else empty list will be returned.
	 * @throws EventSubscriptionTrackerException
	 */
	public List<EventSubscriptionTracker> getAllFailedSubscriptionRecordsArrangedByFailureTimeAndRetryCount(
			Exchange camelExchange, String tenantId, String siteId, String subscriptionId, String failedStatus,
			JSONObject retryConfigurationJSON) throws EventSubscriptionTrackerException;

}
