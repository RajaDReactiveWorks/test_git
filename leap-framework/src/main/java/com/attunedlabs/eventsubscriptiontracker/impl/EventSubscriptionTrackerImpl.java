package com.attunedlabs.eventsubscriptiontracker.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.create.TableCreationBuilder;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.RowUpdationBuilder;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapDefaultRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapNoRetryStrategy;
import com.attunedlabs.eventsubscription.exception.EventSubscriptionTrackerException;
import com.attunedlabs.eventsubscription.exception.RetryableException;
import com.attunedlabs.eventsubscription.exception.SubscriptionTableExistenceException;
import com.attunedlabs.eventsubscription.retrypolicy.SubscriptionRetryPolicy;
import com.attunedlabs.eventsubscription.util.EventSubscriptionTrackerConstants;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;
import com.attunedlabs.eventsubscriptiontracker.IEventSubscriptionTrackerService;

public class EventSubscriptionTrackerImpl extends AbstractMetaModelBean implements IEventSubscriptionTrackerService {
	final Logger log = LoggerFactory.getLogger(EventSubscriptionTrackerImpl.class);
	private Map<String, Column> tableColumnMap = new ConcurrentHashMap<>();

	@Override
	protected void processBean(Exchange arg0) throws Exception {

	}

	public static void main(String[] args) throws SubscriptionTableExistenceException {
		new EventSubscriptionTrackerImpl().createTrackerTableForSubscription();
	}

	@Override
	public boolean createTrackerTableForSubscription() throws SubscriptionTableExistenceException {

		log.debug("inside createTrackerTableForSubscription()...");
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			dataContext.setIsInTransaction(false);
			final Schema tableSchema = dataContext.getDefaultSchema();

			dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					log.debug("creating tracker table for subscription....");
					final TableCreationBuilder createSuccessTable = callback.createTable(tableSchema,
							EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
					createSuccessTable.withColumn(EventSubscriptionTrackerConstants.TENANT_ID_COL)
							.ofType(ColumnType.VARCHAR).ofSize(100)
							.withColumn(EventSubscriptionTrackerConstants.SITE_ID_COL).ofType(ColumnType.VARCHAR)
							.ofSize(100).withColumn(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL)
							.ofType(ColumnType.VARCHAR).ofSize(100)
							.withColumn(EventSubscriptionTrackerConstants.TOPIC_COL).ofType(ColumnType.VARCHAR)
							.ofSize(45).withColumn(EventSubscriptionTrackerConstants.PARTITION_COL)
							.ofType(ColumnType.VARCHAR).ofSize(50)
							.withColumn(EventSubscriptionTrackerConstants.OFFEST_COL).ofType(ColumnType.VARCHAR)
							.ofSize(50).withColumn(EventSubscriptionTrackerConstants.EVENT_DATA_COL)
							.ofType(ColumnType.VARCHAR).ofSize(15000)
							.withColumn(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL)
							.ofType(ColumnType.TIMESTAMP)
							.withColumn(EventSubscriptionTrackerConstants.LAST_FAILURE_DTM_COL)
							.ofType(ColumnType.TIMESTAMP).withColumn(EventSubscriptionTrackerConstants.FAILURE_MSG_COL)
							.ofType(ColumnType.VARCHAR).ofSize(500)
							.withColumn(EventSubscriptionTrackerConstants.TRACK_STATUS).ofType(ColumnType.VARCHAR)
							.ofSize(45).withColumn(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL)
							.ofType(ColumnType.BOOLEAN).withColumn(EventSubscriptionTrackerConstants.RETRY_COUNT_COL)
							.ofType(ColumnType.INTEGER).execute();

				}
			});

		} catch (Exception e) {
			if (!e.getMessage().contains("already exists"))
				throw new SubscriptionTableExistenceException(
						"Failed to create  EventSubscriptionTracker table ..." + e.getMessage());
			else
				log.debug("EventSubscriptionTracker table already exist's!");

		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		return true;

	}

	@Override
	public boolean addNewSubscriptionRecord(Exchange exchange, Map<String, Object> recordsDetails) {
		log.debug("inside addNewSubscriptionRecord()...");
		AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = (AbstractSubscriptionRetryStrategy) recordsDetails
				.get(SubscriptionConstant.RETRY_STRATEGY_CLASS);
		final EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) recordsDetails
				.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);

		setSubscriptionDetailsFromConfig(abstractRetryStrategyBean, eventSubscriptionTracker);

		logDetails(eventSubscriptionTracker);
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			dataContext.setIsInTransaction(false);
			final Table subscriptionTable = dataContext
					.getTableByQualifiedLabel(EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
			initializeSubscriptionTableColumnMap(subscriptionTable);

			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(subscriptionTable);
					insert.value(tableColumnMap.get(EventSubscriptionTrackerConstants.TENANT_ID_COL),
							eventSubscriptionTracker.getTenantId())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.SITE_ID_COL),
									eventSubscriptionTracker.getSiteId())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL),
									eventSubscriptionTracker.getSubscriptionId())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.TOPIC_COL),
									eventSubscriptionTracker.getTopic())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.PARTITION_COL),
									eventSubscriptionTracker.getPartition())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.OFFEST_COL),
									eventSubscriptionTracker.getOffset())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_DATA_COL),
									eventSubscriptionTracker.getEventData())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL),
									new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()))
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS),
									EventSubscriptionTrackerConstants.STATUS_NEW)
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL),
									eventSubscriptionTracker.getIsRetryable())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.RETRY_COUNT_COL), 0);

					insert.execute();
				}
			});
			Integer totalInsertedRows = 0;

			if (insertSummary.getInsertedRows().isPresent()) {
				totalInsertedRows = (Integer) insertSummary.getInsertedRows().get();
				log.debug("total added rows to EventSubscriptionTracker: " + totalInsertedRows);
				return totalInsertedRows > 0;
			} else {
				log.debug("nothing added to EventSubscriptionTracker..");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("failed to add record to the EventSubscriptionTracker table ..." + e.getMessage());
			return false;
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
	}

	@Override
	public boolean updateSubscriptionRecordStatus(final Exchange exchange, final Map<String, Object> recordsDetails,
			final String trackStatus, final Exception exception, final JSONObject retryConfigurationJSON) {
		log.debug("inside updateSubscrptionRecordStatus()..." + retryConfigurationJSON);
		Integer totalRowsUpdated = 0;

		AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = (AbstractSubscriptionRetryStrategy) recordsDetails
				.get(SubscriptionConstant.RETRY_STRATEGY_CLASS);
		final EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) recordsDetails
				.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);
		// just to identify the retry because for incrementing retry
		// count(++retry) only if retry attempt is done because this method will
		// not be invoked from retry call.
		final Boolean isRetryTriggered = (Boolean) recordsDetails
				.get(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY);

		// if exception occurs and if retryAble(decided based on the config
		// retry count passes > 0 and previous retry)
		if (exception != null && eventSubscriptionTracker.getIsRetryable())
			eventSubscriptionTracker.setIsRetryable(exception instanceof RetryableException);

		logDetails(eventSubscriptionTracker);
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			dataContext.setIsInTransaction(false);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
			DefaultUpdateSummary updateSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				public void run(UpdateCallback callback) {
					final RowUpdationBuilder update = callback.update(trackerTable);
					int retryCount = 0;
					update.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL))
							.eq(eventSubscriptionTracker.getSubscriptionId())
							.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TOPIC_COL))
							.eq(eventSubscriptionTracker.getTopic())
							.where(tableColumnMap.get(EventSubscriptionTrackerConstants.PARTITION_COL))
							.eq(eventSubscriptionTracker.getPartition())
							.where(tableColumnMap.get(EventSubscriptionTrackerConstants.OFFEST_COL))
							.eq(eventSubscriptionTracker.getOffset());

					update.value(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS), trackStatus).value(
							tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL),
							eventSubscriptionTracker.getIsRetryable());

					if (exception != null) {
						update.value(tableColumnMap.get(EventSubscriptionTrackerConstants.LAST_FAILURE_DTM_COL),
								new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()))
								.value(tableColumnMap.get(EventSubscriptionTrackerConstants.FAILURE_MSG_COL),
										exception.getMessage());
					}

					// if exception has occured and this time getIsRetryable
					// will return whether (exception instanceof
					// RetryableException)
					if (exception != null && eventSubscriptionTracker.getIsRetryable()) {
						retryCount = eventSubscriptionTracker.getRetryCount();
						if (isRetryTriggered) {
							update.value(tableColumnMap.get(EventSubscriptionTrackerConstants.RETRY_COUNT_COL),
									++retryCount);

							// marking retryable as false if retry count exceeds
							eventSubscriptionTracker.setIsRetryable(
									!maxRetryExceeded(retryConfigurationJSON, eventSubscriptionTracker));
							update.value(tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL),
									eventSubscriptionTracker.getIsRetryable());
						}
					}
					update.execute();
				}
			});
			if (updateSummary.getUpdatedRows().isPresent()) {
				totalRowsUpdated = (Integer) updateSummary.getUpdatedRows().get();
				log.debug("total updated rows in EventSubscriptionTracker table: " + totalRowsUpdated);
				return totalRowsUpdated > 0;
			} else
				log.debug("updated rows in EventSubscriptionTracker table: " + totalRowsUpdated);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("failed to update record in EventSubscriptionTracker table " + e.getMessage());
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		return false;
	}

	@Override
	public List<EventSubscriptionTracker> getAllSubscriptionRecordsIntitializedForLongTime(Exchange camelExchange,
			String tenantId, String siteId, String subscriptionId, JSONObject retryConfigurationJSON)
			throws EventSubscriptionTrackerException {
		String trackStatus = EventSubscriptionTrackerConstants.STATUS_NEW;
		logger.debug("inside getAllSubscriptionRecordsIntitializedForLongTime()..");
		List<EventSubscriptionTracker> eventTrackingList = new ArrayList<EventSubscriptionTracker>();
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			dataContext.setIsInTransaction(false);
			Date pastTime = SubscriptionUtil.getPreviousDateInstance(retryConfigurationJSON);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
			initializeSubscriptionTableColumnMap(trackerTable);

			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(SubscriptionRetryPolicy.getMaxRetryRecordsCount(retryConfigurationJSON))
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TENANT_ID_COL)).eq(tenantId)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SITE_ID_COL)).eq(siteId)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL)).eq(subscriptionId)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL))
					.lessThanOrEquals(pastTime)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS)).eq(trackStatus)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL)).eq(true)
					.orderBy(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL)).asc()
					.execute();

			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventSubscriptionTracker eventSubscriptionTracker = new EventSubscriptionTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventSubscriptionTracker(eventSubscriptionTracker, row));
			}
			logger.debug("retrieved list of tracking for processingStatus : " + trackStatus + " for long time--> "
					+ eventTrackingList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventSubscriptionTrackerException(
					"Failed to get list of all the EventSubscriptionTrackingRecords which are intitialized for long time!",
					e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		return eventTrackingList;
	}

	@Override
	public List<EventSubscriptionTracker> getAllSubscriptionRecordsInProcessForLongTimeArrangedByRetry(
			Exchange camelExchange, String tenantId, String siteId, String subscriptionId, String processingStatus,
			JSONObject retryConfigurationJSON) throws EventSubscriptionTrackerException {
		// TODO Auto-generated method stub
		logger.debug("inside getAllSubscriptionRecordsInProcessForLongTimeArrangedByRetry()..for processingStatus : "
				+ processingStatus);
		List<EventSubscriptionTracker> eventTrackingList = new ArrayList<EventSubscriptionTracker>();
		Connection connection = null;
		try {
			if (SubscriptionUtil.validateInProcessProcessingStatus(processingStatus)) {
				connection = DataSourceInstance.getConnection();
				JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
				dataContext.setIsInTransaction(false);
				Date pastTime = SubscriptionUtil.getPreviousDateInstance(retryConfigurationJSON);
				final Table trackerTable = dataContext
						.getTableByQualifiedLabel(EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
				DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
						.limit(SubscriptionRetryPolicy.getMaxRetryRecordsCount(retryConfigurationJSON))
						.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TENANT_ID_COL)).eq(tenantId)
						.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SITE_ID_COL)).eq(siteId)
						.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL))
						.eq(subscriptionId)
						.where(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL))
						.lessThanOrEquals(pastTime)
						.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS)).eq(processingStatus)
						.where(tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL)).eq(true)
						.orderBy(tableColumnMap.get(EventSubscriptionTrackerConstants.RETRY_COUNT_COL)).asc()
						.orderBy(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL)).asc()
						.execute();
				Iterator<Row> itr = dataSet.iterator();
				while (itr.hasNext()) {
					EventSubscriptionTracker eventSubscriptionTracker = new EventSubscriptionTracker();
					Row row = itr.next();
					eventTrackingList.add(parseRowToEventSubscriptionTracker(eventSubscriptionTracker, row));
				}
				logger.debug("retrieved list of tracking for processingStatus : " + processingStatus + " --> "
						+ eventTrackingList);
			} else
				logger.warn("processingStatus : " + processingStatus
						+ " dosen't match should be either  IN_PROCESS, RETRY_INPROCESS");
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventSubscriptionTrackerException(
					"Failed to get list of all the EventSubscriptionTrackingRecords available for processingStatus : "
							+ processingStatus + "...!",
					e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		return eventTrackingList;
	}

	@Override
	public List<EventSubscriptionTracker> getAllFailedSubscriptionRecordsArrangedByFailureTimeAndRetryCount(
			Exchange camelExchange, String tenantId, String siteId, String subscriptionId, String failedStatus,
			JSONObject retryConfigurationJSON) throws EventSubscriptionTrackerException {
		logger.debug("inside getAllFailedSubscriptionRecordsArrangedByFailureTimeAndRetryCount()...processingStatus : "
				+ failedStatus);
		List<EventSubscriptionTracker> eventTrackingList = new ArrayList<EventSubscriptionTracker>();
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			dataContext.setIsInTransaction(false);
			final Table trackerTable = dataContext
					.getTableByQualifiedLabel(EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(SubscriptionRetryPolicy.getMaxRetryRecordsCount(retryConfigurationJSON))
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TENANT_ID_COL)).eq(tenantId)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SITE_ID_COL)).eq(siteId)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL)).eq(subscriptionId)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL)).eq(true)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS)).eq(failedStatus)
					.orderBy(tableColumnMap.get(EventSubscriptionTrackerConstants.RETRY_COUNT_COL)).asc()
					.orderBy(tableColumnMap.get(EventSubscriptionTrackerConstants.LAST_FAILURE_DTM_COL)).asc()
					.execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventSubscriptionTracker eventSubscriptionTracker = new EventSubscriptionTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventSubscriptionTracker(eventSubscriptionTracker, row));
			}
			logger.debug("retrieved list of tracking for processingStatus :  FAILED or RETRY_FAILED--> "
					+ eventTrackingList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventSubscriptionTrackerException(
					"Failed to get list of all the FAILED/RETRY_FAILED EventSubscriptionTrackingRecords available...!",
					e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		return eventTrackingList;
	}

	/**
	 * provides you the map of subscription table column's.
	 * 
	 * @param subscriptionTable
	 */
	private void initializeSubscriptionTableColumnMap(Table subscriptionTable) {
		if (tableColumnMap.isEmpty() || tableColumnMap.size() != 13) {
			tableColumnMap.put(EventSubscriptionTrackerConstants.TENANT_ID_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.TENANT_ID_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.SITE_ID_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.SITE_ID_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.TOPIC_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.TOPIC_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.PARTITION_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.PARTITION_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.OFFEST_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.OFFEST_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.EVENT_DATA_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.EVENT_DATA_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.LAST_FAILURE_DTM_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.LAST_FAILURE_DTM_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.FAILURE_MSG_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.FAILURE_MSG_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.TRACK_STATUS,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.TRACK_STATUS));
			tableColumnMap.put(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.RETRY_COUNT_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.RETRY_COUNT_COL));
		}
	}

	/**
	 * utility for parsing the row retrieved from dataset to
	 * EventSubscriptionTracker.
	 * 
	 * @param eventSubscriptionTracker
	 * @param row
	 * @return
	 */
	private EventSubscriptionTracker parseRowToEventSubscriptionTracker(
			EventSubscriptionTracker eventSubscriptionTracker, Row row) {
		System.out.println(tableColumnMap + " : tableColumnMap" + " ROW : \n" + row);
		eventSubscriptionTracker.setTenantId(
				(String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.TENANT_ID_COL)));
		eventSubscriptionTracker
				.setSiteId((String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.SITE_ID_COL)));
		eventSubscriptionTracker.setSubscriptionId(
				(String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL)));
		eventSubscriptionTracker
				.setTopic((String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.TOPIC_COL)));
		eventSubscriptionTracker.setPartition(
				(String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.PARTITION_COL)));
		eventSubscriptionTracker
				.setOffset((String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.OFFEST_COL)));
		eventSubscriptionTracker.setEventData(
				(String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_DATA_COL)));
		eventSubscriptionTracker.setEventFetchedDTM(
				(Date) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL)));
		eventSubscriptionTracker.setLastFailureDTM(
				(Date) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.LAST_FAILURE_DTM_COL)));
		eventSubscriptionTracker.setFailureMsg(
				(String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.FAILURE_MSG_COL)));
		eventSubscriptionTracker
				.setStatus((String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS)));

		Object isRetryObject = row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL));
		boolean isRetryAble = false;

		if (isRetryObject instanceof Boolean)
			isRetryAble = (Boolean) isRetryObject;
		if (isRetryObject instanceof Integer)
			isRetryAble = (Integer) isRetryObject != 0;

		eventSubscriptionTracker.setIsRetryable(isRetryAble);
		eventSubscriptionTracker.setRetryCount(
				(Integer) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.RETRY_COUNT_COL)));
		return eventSubscriptionTracker;
	}

	/**
	 * set retry count in EventSubscriptionTracker
	 * 
	 * @param abstractRetryStrategyBean
	 * @param eventSubscriptionTracker
	 */
	private void setSubscriptionDetailsFromConfig(AbstractSubscriptionRetryStrategy abstractRetryStrategyBean,
			EventSubscriptionTracker eventSubscriptionTracker) {
		if (abstractRetryStrategyBean instanceof LeapDefaultRetryStrategy) {
			JSONObject retryConfiguration = abstractRetryStrategyBean.getRetryConfiguration();
			try {
				Integer retryCount = SubscriptionRetryPolicy.getRetryCount(retryConfiguration);
				if (retryCount >= -1)
					eventSubscriptionTracker.setIsRetryable(true);
				else
					eventSubscriptionTracker.setIsRetryable(false);

			} catch (Exception e1) {
				log.error("failed to set rertycount " + e1.getLocalizedMessage());
			}
		} else if (abstractRetryStrategyBean instanceof LeapNoRetryStrategy)
			eventSubscriptionTracker.setIsRetryable(false);
	}

	private void logDetails(EventSubscriptionTracker eventSubscriptionTracker) {
		log.debug("SUBSCRIPTION " + eventSubscriptionTracker.getSubscriptionId());
		log.debug("TOPIC : " + eventSubscriptionTracker.getTopic());
		log.debug("PARTITION : " + eventSubscriptionTracker.getPartition());
		log.debug("OFFSET : " + eventSubscriptionTracker.getOffset());
		log.debug("EVENT DATA : " + eventSubscriptionTracker.getEventData());
	}

	/**
	 * Using the context lookup approach to get datasource bean instance.
	 * Utility to return JdbcDataContext - eventSubscription
	 * 
	 * 
	 * @param exchange
	 * @return dataContext
	 * @throws Exception
	 */
	private JdbcDataContext getJdbcDataContext(Exchange exchange, String dbRefName) throws Exception {

		log.debug("Logging DbReferenceName: " + dbRefName);
		CamelContext camelContext = exchange.getContext();
		DataSource dataSourcegetting = (DataSource) camelContext.getRegistry().lookupByName(dbRefName);
		log.debug("dataSourcegetting object: " + dataSourcegetting);
		/** setting DataSource **/
		setDataSource(dataSourcegetting);
		/** Getting the instance of JdbcDataContext **/
		JdbcDataContext jdbcDataContext = getLocalDataContext(exchange);
		return jdbcDataContext;
	}

	/**
	 * This method will check whether the retry count has been exceeded. added 1
	 * extra to mark that retryable as false so retry will not be able to fatch
	 * the data marked as nonretryable.
	 * 
	 * @param retryConfigurationJSON
	 * @param eventSubscriptionTracker
	 * @return
	 */
	private boolean maxRetryExceeded(JSONObject retryConfigurationJSON,
			EventSubscriptionTracker eventSubscriptionTracker) {
		if (SubscriptionRetryPolicy.getRetryCount(retryConfigurationJSON) == -1)
			return false;
		return !(SubscriptionRetryPolicy
				.getRetryCount(retryConfigurationJSON) > eventSubscriptionTracker.getRetryCount() + 1);
	}

}
