package com.attunedlabs.eventframework.eventtracker.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.RowUpdationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.eventframework.eventtracker.EventDispatcherTracker;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.retrypolicy.RetryPolicy;

public class EventDispatcherTrackerImpl extends AbstractMetaModelBean implements IEventDispatcherTrackerService {
	final Logger logger = LoggerFactory.getLogger(EventDispatcherTrackerImpl.class);

	@Override
	public boolean addEventTracking(final String tenantId, final String siteId, final String requestId,
			final String eventStoreKey, Exchange camelExchange) throws EventDispatcherTrackerException {
		logger.debug("inside addEventToTrack()...tenantId : " + tenantId + " siteId : " + siteId + " requestId : "
				+ requestId + " eventStoreKey : " + eventStoreKey);
		JdbcDataContext dataContext = null;
		try {
			dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_DATASOURCE);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(trackerTable);
					insert.value(EventTrackerTableConstants.TENANT_ID, tenantId)
							.value(EventTrackerTableConstants.SITE_ID, siteId)
							.value(EventTrackerTableConstants.REQUEST_ID, requestId)
							.value(EventTrackerTableConstants.EVENT_STORE_ID, eventStoreKey)
							.value(EventTrackerTableConstants.EVENT_CREATED_DTM,
									new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()))
							.value(EventTrackerTableConstants.PROCESSING_STATUS, EventTrackerTableConstants.STATUS_NEW)
							.value(EventTrackerTableConstants.RETRY_COUNT, 0);

					insert.execute();
				}
			});
			Integer totalInsertedRows = 0;

			if (insertSummary.getInsertedRows().isPresent()) {
				totalInsertedRows = (Integer) insertSummary.getInsertedRows().get();
				logger.debug("added event to tracktable totalrecordsAddded : " + totalInsertedRows);
				return true;
			} else {
				logger.debug("nothing added to event tracktable..");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException("Failed to persist event details...!" + e.getMessage(), e);
		}finally {
			Connection connection = dataContext.getConnection();
			try {
				if(!connection.isClosed())
					connection.close();
			} catch (SQLException e) {
				//do nothing
			}
		}
	}

	@Override
	public boolean updateEventStatus(final String tenantId, final String siteId, final String requestId,
			final String eventStoreKey, final String processingStatus, final Exchange camelExchange,
			final boolean isFailure, final String failureMsg, final boolean isFailedNow, final boolean isRetried)
			throws EventDispatcherTrackerException {
		logger.debug(
				"inside updateEventStatus()...processingStatus : " + processingStatus + " isFailure : " + isFailure);
		int totalRowsUpdated = 0;

		JdbcDataContext dataContext = null;
		try {
			dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DefaultUpdateSummary updateSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {

				@Override
				public void run(UpdateCallback callback) {
					final RowUpdationBuilder update = callback.update(trackerTable);
					int retryCount = 0;
					try {
						retryCount = getRetryCount(tenantId, siteId, requestId, eventStoreKey, camelExchange);
					} catch (EventDispatcherTrackerException e) {
						e.printStackTrace();
						logger.error("some error couured while fetchingRetry Count...!" + e.getMessage());
					}

					update.where(EventTrackerTableConstants.TENANT_ID).eq(tenantId)
							.where(EventTrackerTableConstants.SITE_ID).eq(siteId)
							.where(EventTrackerTableConstants.REQUEST_ID).eq(requestId)
							.where(EventTrackerTableConstants.EVENT_STORE_ID).eq(eventStoreKey)
							.value(EventTrackerTableConstants.PROCESSING_STATUS, processingStatus);

					// execution of following if will update failure and retry
					// count
					if (isFailure || isRetried) {
						if (isFailedNow)
							update.value(EventTrackerTableConstants.LAST_FAILURE_DTM,
									new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));

						if (failureMsg != null)
							update.value(EventTrackerTableConstants.FAILURE_REASON, failureMsg);

						if (isRetried)
							update.value(EventTrackerTableConstants.RETRY_COUNT, ++retryCount);

					}

					if (retryCount == 0)
						update.value(EventTrackerTableConstants.RETRY_COUNT, retryCount);

					update.execute();
				}
			});

			if (updateSummary.getUpdatedRows().isPresent()) {
				totalRowsUpdated = (Integer) updateSummary.getUpdatedRows().get();
				logger.debug("total updated rows in event tracker table: " + totalRowsUpdated);
				return totalRowsUpdated > 0;
			} else {
				logger.debug("updated rows in event tracker table: " + totalRowsUpdated);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to update status for events configured to requestId : " + requestId + "...!", e);
		}finally {
			Connection connection = dataContext.getConnection();
			try {
				if(!connection.isClosed())
					connection.close();
			} catch (SQLException e) {
				//do nothing
			}
		}
	}

	@Override
	public Integer getRetryCount(final String tenantId, final String siteId, final String requestId,
			final String eventStoreKey, Exchange camelExchange) throws EventDispatcherTrackerException {
		logger.debug("inside getRetryCount()...tenantId : " + tenantId + " siteId : " + siteId + " requestId : "
				+ requestId + " eventStoreKey : " + eventStoreKey);
		Integer retryCount = 0;
		final JdbcDataContext dataContext;
		try {
			dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DataSet dataSet = dataContext.query().from(trackerTable).select(EventTrackerTableConstants.RETRY_COUNT)
					.where(EventTrackerTableConstants.TENANT_ID).eq(tenantId).where(EventTrackerTableConstants.SITE_ID)
					.eq(siteId).where(EventTrackerTableConstants.REQUEST_ID).eq(requestId)
					.where(EventTrackerTableConstants.EVENT_STORE_ID).eq(eventStoreKey).execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				retryCount = (Integer) row
						.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.RETRY_COUNT));
				if (retryCount == null)
					retryCount = 0;
				logger.debug("current retry count : " + retryCount);
			} else
				logger.debug("first time to be retried count will be set to 1...");
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to get retryCount for events configured to requestId : " + requestId + "...!", e);
		}
		return retryCount;
	}

	@Override
	public String getTrackStatusForEventList(String tenantId, String siteId, String requestId, String eventStoreKey,
			Exchange camelExchange) throws EventDispatcherTrackerException {
		logger.debug("inside getTrackStatusForEventList()...tenantId : " + tenantId + " siteId : " + siteId
				+ " requestId : " + requestId + " eventStoreKey : " + eventStoreKey);
		String processingStatus = null;
		try {
			final JdbcDataContext dataContext;
			dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DataSet dataSet = dataContext.query().from(trackerTable)
					.select(EventTrackerTableConstants.PROCESSING_STATUS).where(EventTrackerTableConstants.TENANT_ID)
					.eq(tenantId).where(EventTrackerTableConstants.SITE_ID).eq(siteId)
					.where(EventTrackerTableConstants.REQUEST_ID).eq(requestId)
					.where(EventTrackerTableConstants.EVENT_STORE_ID).eq(eventStoreKey).execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				processingStatus = (String) row
						.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.PROCESSING_STATUS));
				logger.debug("processing status  for events configured to requestId : " + requestId + " is "
						+ processingStatus + "..!");
				return processingStatus;
			} else
				logger.debug("No processing status present for events configured to requestId..." + requestId + "...!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to get status for events configured to requestId : " + requestId + "...!", e);
		}
		return processingStatus;
	}

	@Override
	public boolean removeEventTrackRecord(final String tenantId, final String siteId, final String requestId,
			final String eventStoreKey, Exchange camelExchange) throws EventDispatcherTrackerException {
		logger.debug("inside removeEventTrackRecord()...tenantId : " + tenantId + " siteId : " + siteId
				+ " requestId : " + requestId + " eventStoreKey : " + eventStoreKey);
		int totalRowsDeleted = 0;

		try {
			final JdbcDataContext dataContext;
			dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DefaultUpdateSummary eventTrackerDelete = (DefaultUpdateSummary) dataContext
					.executeUpdate(new UpdateScript() {
						@Override
						public void run(UpdateCallback callback) {
							final RowDeletionBuilder delete = callback.deleteFrom(trackerTable);
							delete.where(EventTrackerTableConstants.TENANT_ID).eq(tenantId)
									.where(EventTrackerTableConstants.SITE_ID).eq(siteId)
									.where(EventTrackerTableConstants.REQUEST_ID).eq(requestId)
									.where(EventTrackerTableConstants.EVENT_STORE_ID).eq(eventStoreKey)
									.where(EventTrackerTableConstants.PROCESSING_STATUS)
									.eq(EventTrackerTableConstants.STATUS_COMPLETE);
							delete.execute();
						}
					});
			if (eventTrackerDelete.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) eventTrackerDelete.getDeletedRows().get();
				logger.debug("track record deleted successfully for request Id -->" + requestId
						+ " total records deleted " + totalRowsDeleted);
				return totalRowsDeleted > 0;
			} else {
				logger.debug("track record not present for request Id -->" + requestId + " or else already deleted!");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to delete track record configured for requestId : " + requestId + "...!", e);
		}
	}

	@Override
	public List<EventDispatcherTracker> getAllTrackRecords(Exchange camelExchange)
			throws EventDispatcherTrackerException {
		logger.debug("inside getAllTrackRecords()...");
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		try {

			final JdbcDataContext dataContext;
			dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(RetryPolicy.getMaxRetryRecordsCount()).execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
			}
			logger.debug("retrieved list of tracking " + eventTrackingList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available...!", e);
		}
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllTrackRecordsOnStatus(Exchange camelExchange, String processingStatus)
			throws EventDispatcherTrackerException {
		logger.debug("inside getAllTrackRecordsOnStatus()...processingStatus : " + processingStatus);
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		try {
			if (validateProcessingStatus(processingStatus)) {
				final JdbcDataContext dataContext;
				dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
				// setting transaction as false because update will not be in
				// transaction called from notifier.
				dataContext.setIsInTransaction(false);
				final Table trackerTable = dataContext
						.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
				DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
						.limit(RetryPolicy.getMaxRetryRecordsCount())
						.where(EventTrackerTableConstants.PROCESSING_STATUS).eq(processingStatus).execute();
				Iterator<Row> itr = dataSet.iterator();
				while (itr.hasNext()) {
					EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
					Row row = itr.next();
					eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
				}
				logger.debug("retrieved list of tracking for processingStatus : " + processingStatus + " --> "
						+ eventTrackingList);
			} else
				logger.warn("processingStatus : " + processingStatus
						+ " dosen't match should be either NEW, IN_PRCESS, RETRY, RETRY_INPROCESS, FAILED, COMPLETE");
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available...!", e);
		}
		return eventTrackingList;
	}

	/**
	 * calcuation done based on the getting the top records on policy
	 * maxRetryRecords.
	 * 
	 * @param eventTrackingList
	 * @return topFilterList
	 */
	@SuppressWarnings("unused")
	private List<EventDispatcherTracker> getTopRecords(List<EventDispatcherTracker> eventTrackingList) {
		int topRecords = RetryPolicy.getMaxRetryRecordsCount();
		if (topRecords == -1)
			return eventTrackingList;
		int maxSize = eventTrackingList.size();
		if (topRecords > maxSize)
			topRecords = maxSize - 1;
		try {
			return eventTrackingList.subList(0, topRecords + 1);
		} catch (Exception e) {
			return eventTrackingList;
		}
	}

	@Override
	public List<EventDispatcherTracker> getAllTrackRecordsIntitializedForLongTime(Exchange camelExchange)
			throws EventDispatcherTrackerException {
		String processingStatus = EventTrackerTableConstants.STATUS_NEW;
		logger.debug("inside getAllTrackRecordsIntitializedForLongTime()..");
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		try {
			final JdbcDataContext dataContext;
			dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			Date pastTime = getPreviousDateInstance(RetryPolicy.getNormalRetryInterval());
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(RetryPolicy.getMaxRetryRecordsCount()).where(EventTrackerTableConstants.EVENT_CREATED_DTM)
					.lessThanOrEquals(pastTime).where(EventTrackerTableConstants.PROCESSING_STATUS).eq(processingStatus)
					.orderBy(EventTrackerTableConstants.EVENT_CREATED_DTM).asc().execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
			}
			logger.debug("retrieved list of tracking for processingStatus : " + processingStatus + " for long time--> "
					+ eventTrackingList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords which are intitialized for long time!", e);
		}
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllTrackRecordsInProcessForLongTime(Exchange camelExchange,
			String processingStatus) throws EventDispatcherTrackerException {
		logger.debug("inside getAllTrackRecordsInProcessForLongTime()..for processingStatus : " + processingStatus);
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		try {
			if (validateInProcessProcessingStatus(processingStatus)) {
				final JdbcDataContext dataContext;
				dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
				// setting transaction as false because update will not be in
				// transaction called from notifier.
				dataContext.setIsInTransaction(false);
				Date pastTime = getPreviousDateInstance(RetryPolicy.getNormalRetryInterval());
				final Table trackerTable = dataContext
						.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
				DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
						.limit(RetryPolicy.getMaxRetryRecordsCount())
						.where(EventTrackerTableConstants.EVENT_CREATED_DTM).lessThanOrEquals(pastTime)
						.where(EventTrackerTableConstants.PROCESSING_STATUS).eq(processingStatus)
						.orderBy(EventTrackerTableConstants.EVENT_CREATED_DTM).asc().execute();
				Iterator<Row> itr = dataSet.iterator();
				while (itr.hasNext()) {
					EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
					Row row = itr.next();
					eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
				}
				logger.debug("retrieved list of tracking for processingStatus : " + processingStatus + " --> "
						+ eventTrackingList);
			} else
				logger.warn("processingStatus : " + processingStatus
						+ " dosen't match should be either  IN_PROCESS, RETRY_INPROCESS");
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available for processingStatus : "
							+ processingStatus + "...!",
					e);
		}
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllTrackRecordsInProcessForLongTimeArrangedByRetry(Exchange camelExchange,
			String processingStatus) throws EventDispatcherTrackerException {
		// TODO Auto-generated method stub
		logger.debug("inside getAllTrackRecordsInProcessForLongTimeArrangedByRetry()..for processingStatus : "
				+ processingStatus);
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		try {
			if (validateInProcessProcessingStatus(processingStatus)) {
				final JdbcDataContext dataContext;
				dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
				// setting transaction as false because update will not be in
				// transaction called from notifier.
				dataContext.setIsInTransaction(false);
				Date pastTime = getPreviousDateInstance(RetryPolicy.getNormalRetryInterval());
				final Table trackerTable = dataContext
						.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
				DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
						.limit(RetryPolicy.getMaxRetryRecordsCount())
						.where(EventTrackerTableConstants.EVENT_CREATED_DTM).lessThanOrEquals(pastTime)
						.where(EventTrackerTableConstants.PROCESSING_STATUS).eq(processingStatus)
						.orderBy(EventTrackerTableConstants.RETRY_COUNT).asc()
						.orderBy(EventTrackerTableConstants.EVENT_CREATED_DTM).asc().execute();
				Iterator<Row> itr = dataSet.iterator();
				while (itr.hasNext()) {
					EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
					Row row = itr.next();
					eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
				}
				logger.debug("retrieved list of tracking for processingStatus : " + processingStatus + " --> "
						+ eventTrackingList);
			} else
				logger.warn("processingStatus : " + processingStatus
						+ " dosen't match should be either  IN_PROCESS, RETRY_INPROCESS");
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available for processingStatus : "
							+ processingStatus + "...!",
					e);
		}
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllFailedEventRecordsArrangedByFailureTime(Exchange camelExchange)
			throws EventDispatcherTrackerException {
		logger.debug("inside getAllFailedEventRecordsArrangedByFailureTime()...processingStatus : FAILED");
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		try {
			final JdbcDataContext dataContext;
			dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(RetryPolicy.getMaxRetryRecordsCount()).where(EventTrackerTableConstants.PROCESSING_STATUS)
					.eq(EventTrackerTableConstants.STATUS_FAILED).orderBy(EventTrackerTableConstants.RETRY_COUNT).asc()
					.orderBy(EventTrackerTableConstants.LAST_FAILURE_DTM).asc().execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
			}
			logger.debug("retrieved list of tracking for processingStatus :  FAILED --> " + eventTrackingList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available...!", e);
		}
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllFailedEventRecordsArrangedByFailureTimeAndRetryCount(
			Exchange camelExchange) throws EventDispatcherTrackerException {
		logger.debug("inside getAllFailedEventRecordsArrangedByFailureTimeAndRetryCount()...processingStatus : FAILED");
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		try {
			final JdbcDataContext dataContext;
			dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(RetryPolicy.getMaxRetryRecordsCount()).where(EventTrackerTableConstants.PROCESSING_STATUS)
					.eq(EventTrackerTableConstants.STATUS_FAILED).orderBy(EventTrackerTableConstants.RETRY_COUNT).asc()
					.orderBy(EventTrackerTableConstants.LAST_FAILURE_DTM).asc().execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
			}
			logger.debug("retrieved list of tracking for processingStatus :  FAILED --> " + eventTrackingList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available...!", e);
		}
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllRetryFailedEventRecordsArrangedByFailureTime(Exchange camelExchange)
			throws EventDispatcherTrackerException {
		logger.debug("inside getAllRetryFailedEventRecordsArrangedByFailureTime()...processingStatus : RETRY_FAILED");
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		try {
			final JdbcDataContext dataContext;
			dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(RetryPolicy.getMaxRetryRecordsCount()).limit(RetryPolicy.getMaxRetryRecordsCount())
					.where(EventTrackerTableConstants.PROCESSING_STATUS)
					.eq(EventTrackerTableConstants.STATUS_RETRY_FAILED)
					.orderBy(EventTrackerTableConstants.LAST_FAILURE_DTM).asc().execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
			}
			logger.debug("retrieved list of tracking for processingStatus : RETRY_FAILED --> " + eventTrackingList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available...!", e);
		}
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllRetryFailedEventRecordsArrangedByFailureTimeAndRetryCount(
			Exchange camelExchange) throws EventDispatcherTrackerException {
		logger.debug(
				"inside getAllRetryFailedEventRecordsArrangedByFailureTimeAndRetryCount()...processingStatus : RETRY_FAILED");
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		try {
			final JdbcDataContext dataContext;
			dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(RetryPolicy.getMaxRetryRecordsCount()).where(EventTrackerTableConstants.PROCESSING_STATUS)
					.eq(EventTrackerTableConstants.STATUS_RETRY_FAILED).orderBy(EventTrackerTableConstants.RETRY_COUNT)
					.asc().orderBy(EventTrackerTableConstants.LAST_FAILURE_DTM).asc().execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
			}
			logger.debug("retrieved list of tracking for processingStatus : RETRY_FAILED --> " + eventTrackingList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available...!", e);
		}
		return eventTrackingList;
	}

	@Override
	public EventDispatcherTracker getEventRecordOnRequestUUID(Exchange camelExchange, String requestUUID)
			throws EventDispatcherTrackerException {
		logger.debug("inside getEventRecordOnRequestUUID()...requestUUID : " + requestUUID);
		try {
			final JdbcDataContext dataContext;
			dataContext = getJdbcDataContext(camelExchange, EventTrackerTableConstants.Leap_SIMPLE_DATASOURCE);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.where(EventTrackerTableConstants.REQUEST_ID).eq(requestUUID).execute();
			Iterator<Row> itr = dataSet.iterator();
			EventDispatcherTracker eventDispatcherTracker = null;
			if (itr.hasNext()) {
				Row row = itr.next();
				eventDispatcherTracker = new EventDispatcherTracker();
				parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row);
				logger.debug("retrieved eventDispatcherTracker for requestId : " + requestUUID + "-> "
						+ eventDispatcherTracker);
				return eventDispatcherTracker;
			}
			logger.debug("retrieved eventDispatcherTracker for requestId : " + requestUUID + "-> " + eventDispatcherTracker);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventDispatcherTrackerException(
					"Failed to get eventDispatcherTracker for given requestUUID " + requestUUID + "...!", e);
		}
	}

	@Override
	protected void processBean(Exchange exch) throws Exception {
	}

	/**
	 * returning the instance of Date but instance will contain specified
	 * interval before the current Date instance of system.
	 * 
	 * @param timeIntervalBefore
	 * @return date instance
	 */
	private Date getPreviousDateInstance(int timeIntervalBefore) {
		long currentTime = System.currentTimeMillis();
		long specifiedMinutesBeforeTime = 0;
		switch (RetryPolicy.getNormalTimeIntervalUnit().toUpperCase()) {
		case RetryPolicy.TIMEUNIT_HOURS:
			specifiedMinutesBeforeTime = currentTime - (timeIntervalBefore * 3600) * 1000 + 0;
			break;
		case RetryPolicy.TIMEUNIT_MINUTES:
			specifiedMinutesBeforeTime = currentTime - (timeIntervalBefore * 60) * 1000 + 0;
			break;
		case RetryPolicy.TIMEUNIT_SECONDS:
			specifiedMinutesBeforeTime = currentTime - timeIntervalBefore * 1000 + 0;
			break;
		case RetryPolicy.TIMEUNIT_MILLSECONDS:
			specifiedMinutesBeforeTime = currentTime - timeIntervalBefore;
			break;
		default:
			// default will be considered in minutes.
			specifiedMinutesBeforeTime = currentTime - (0 + timeIntervalBefore * 60 + 0) * 1000 + 0;
			break;
		}

		Date calculatedMinutesBeforeDate = new Date(specifiedMinutesBeforeTime);
		return calculatedMinutesBeforeDate;
	}

	/**
	 * utility for parsing the row retrieved from dataset to
	 * EventDispatcherTracker.
	 * 
	 * @param eventDispatcherTracker
	 * @param trackerTable
	 * @param row
	 * @return
	 */
	private EventDispatcherTracker parseRowToEventDispatcherTracker(EventDispatcherTracker eventDispatcherTracker,
			Table trackerTable, Row row) {
		eventDispatcherTracker
				.setTenantId((String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.TENANT_ID)));
		eventDispatcherTracker
				.setSiteId((String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.SITE_ID)));
		eventDispatcherTracker.setRequestId(
				(String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.REQUEST_ID)));
		eventDispatcherTracker.setEventStoreId(
				(String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.REQUEST_ID)));
		eventDispatcherTracker.setEventCreatedDTM((java.util.Date) row
				.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.EVENT_CREATED_DTM)));
		eventDispatcherTracker.setLastFailureDTM((java.util.Date) row
				.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.LAST_FAILURE_DTM)));
		eventDispatcherTracker.setStatus(
				(String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.PROCESSING_STATUS)));
		eventDispatcherTracker.setFailureReason(
				(String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.FAILURE_REASON)));
		eventDispatcherTracker.setRetryCount(
				(Integer) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.RETRY_COUNT)));

		return eventDispatcherTracker;
	}

	/**
	 * validating weather processing status belongs the status registered.
	 * 
	 * @param processingStatus
	 * @return validationStatus
	 */
	private boolean validateProcessingStatus(String processingStatus) {
		List<String> processingStatusList = new ArrayList<>();
		processingStatusList.add(EventTrackerTableConstants.STATUS_NEW);
		processingStatusList.add(EventTrackerTableConstants.STATUS_IN_PROCESS);
		processingStatusList.add(EventTrackerTableConstants.STATUS_FAILED);
		processingStatusList.add(EventTrackerTableConstants.STATUS_RETRY_FAILED);
		processingStatusList.add(EventTrackerTableConstants.STATUS_RETRY_IN_PROCESS);
		processingStatusList.add(EventTrackerTableConstants.STATUS_COMPLETE);
		for (String status : processingStatusList) {
			if (status.equalsIgnoreCase(processingStatus))
				return true;
		}
		return false;
	}

	/**
	 * validating weather processing status belongs the status registered.
	 * 
	 * @param processingStatus
	 * @return validationStatus
	 */
	private boolean validateInProcessProcessingStatus(String processingStatus) {
		List<String> processingStatusList = new ArrayList<>();
		processingStatusList.add(EventTrackerTableConstants.STATUS_IN_PROCESS);
		processingStatusList.add(EventTrackerTableConstants.STATUS_RETRY_IN_PROCESS);
		for (String status : processingStatusList) {
			if (status.equalsIgnoreCase(processingStatus))
				return true;
		}
		return false;
	}

	/**
	 * Using the context lookup approach to get datasource bean instance.
	 * Utility to return JdbcDataContext - eventDipsatcher
	 * 
	 * 
	 * @param exchange
	 * @return dataContext
	 * @throws Exception
	 */
	private JdbcDataContext getJdbcDataContext(Exchange exchange, String dbRefName) throws Exception {

		logger.debug("Logging DbReferenceName: " + dbRefName);
		CamelContext camelContext = exchange.getContext();
		DataSource dataSourcegetting = (DataSource) camelContext.getRegistry().lookupByName(dbRefName);
		logger.debug("dataSourcegetting object: " + dataSourcegetting);
		/** setting DataSource **/
		setDataSource(dataSourcegetting);
		/** Getting the instance of JdbcDataContext **/
		JdbcDataContext jdbcDataContext = getLocalDataContext(exchange);
		return jdbcDataContext;
	}

}
