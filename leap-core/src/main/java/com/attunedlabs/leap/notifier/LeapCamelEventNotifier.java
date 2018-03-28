package com.attunedlabs.leap.notifier;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.apache.camel.management.event.ExchangeSendingEvent;
import org.apache.camel.management.event.ExchangeSentEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventframework.camel.ComponentEventProducer;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.eventframework.eventtracker.impl.EventTrackerTableConstants;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class LeapCamelEventNotifier extends EventNotifierSupport {
	final Logger logger = LoggerFactory.getLogger(LeapCamelEventNotifier.class);

	// EventNotifierHelper evtNotHelper=new EventNotifierHelper();
	ComponentEventProducer evtProducer = new ComponentEventProducer();
	IEventDispatcherTrackerService eventDispatcherTrackerService = new EventDispatcherTrackerImpl();

	public boolean isIgnoreCamelContextEvents() {
		return true;
	}

	public boolean isIgnoreExchangeEvents() {
		return false;
	}

	public boolean isIgnoreExchangeCompletedEvent() {
		return false;
	}

	public boolean isIgnoreExchangeCreatedEvent() {
		return true;
	}

	public boolean isIgnoreExchangeFailedEvents() {
		return false;
	}

	public boolean isIgnoreExchangeRedeliveryEvents() {
		return true;
	}

	public boolean isIgnoreExchangeSendingEvents() {
		return true;
	}

	public boolean isIgnoreExchangeSentEvents() {
		return true;
	}

	public boolean isIgnoreRouteEvents() {
		return true;
	}

	public boolean isIgnoreServiceEvents() {
		return true;
	}

	/**
	 * This method is to check notifier component is enabled or disabled.
	 * 
	 * @param event
	 *            : EventObject
	 * @return boolean
	 */
	public boolean isEnabled(EventObject event) {
		logger.debug("isEnabled -- EventObject=" + event);
		// AbstractExchangeEvent compEvent = (AbstractExchangeEvent) event;
		// Exchange exchange = compEvent.getExchange();
		// String routeId=exchange.getFromRouteId();
		// String contextStr=exchange.getContext().getName();
		// logger.info("isEnabled -- Exchange="+exchange);
		if (event instanceof ExchangeSendingEvent) {
			// logger.info("isEnabled -- CompletedEvent=");
			return true;
			// return
			// evtNotHelper.hasEventForRoute("default",contextStr,routeId);
		} else if (event instanceof ExchangeSentEvent) {
			// logger.info("isEnabled -- ExchangeSentEvent="+exchange);
			return false;
		}

		return true;
	}

	/**
	 * This method is to notify Exchange Completed (success or failure)
	 * 
	 * @param event
	 *            : EventObject
	 * @return boolean
	 */

	@Override
	public void notify(EventObject event) throws Exception {
		logger.debug("notify -- EventObject=" + event.getClass());
		if (event instanceof ExchangeCompletedEvent) {
			// Exchange/complete route from base till Impl route can be
			// Completed with Failure or Success
			logger.debug("when event is ExchangeComplete Event :");
			ExchangeCompletedEvent compEvent = (ExchangeCompletedEvent) event;
			Exchange exchange = compEvent.getExchange();
			if (exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY) != null) {
				logger.debug("Quartz triggered  successfully...");
				return;
			}
			if (exchange.getIn().getHeader("quartzTrigger") != null) {
				logger.debug("Quartz triggered  successfully...");
				return;
			}
			if (exchange.getProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY, Boolean.class)) {
				logger.debug("Subscription invoked  successfully...");
				return;
			}

			LeapHeader leapHeader = null;
			if (exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY) instanceof LeapHeader)
				leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
			if (leapHeader != null && leapHeader.getTenant() != null && leapHeader.getSite() != null
					&& leapHeader.getRequestUUID() != null) {
				RequestContext reqCtx = leapHeader.getRequestContext();
				logger.debug("leapHeader in notifier : " + leapHeader);
				// tenat and site
				String tenantid = leapHeader.getTenant();
				String siteid = leapHeader.getSite();
				String requestId = leapHeader.getRequestUUID();
				String eventStoreKey = leapHeader.getRequestUUID();
				String featureName = leapHeader.getFeatureName();
				String serviceName = leapHeader.getServicetype();

				logger.debug("data into notifier exchngeCompleteEvent : serviceName : " + serviceName + ", tenantid : "
						+ tenantid + ", feature Name : " + featureName);
				Map<String, Object> exchangeProp = exchange.getProperties();
				logger.debug("exchnage property values  in LeapCamelEventNotifier : " + exchangeProp);
				if (!checkMultipleExchange(exchangeProp)) {
					boolean iscompletedWithFailure = isCompletedWithFailure(exchangeProp);
					logger.debug("value of isCompletedWithFailure : " + iscompletedWithFailure);
					if (iscompletedWithFailure) {
						logger.debug("notify --FialedCompletedEvent-- EventObject=" + featureName);
						// Build Event for Route Failed Condition and publish to
						// event Service
						evtProducer.publishEventForFailedRouteCompletion(serviceName, exchange);
						// Build standard mandatory event for Failure and
						// publish to eventService
						evtProducer.publishServiceCompletionFailureSystemEvent(reqCtx, exchange);
						// Just debug logs check to see if hazelcast rolledback
						// or not
						checkHazelCastMapValue(leapHeader);
						// Clean Hazelcast EventList. We are done publishing all
						// events.
						deleteHazelCastMapForRequest(leapHeader);
						// closing all datasource connection on success
						closeAllDataSourceConnection(leapHeader);
					} else {
						logger.debug("notify --Entered CompletedEvent (SuccessEvents)--Feature=" + featureName);
						IMap<String, ArrayList<LeapEvent>> hzEventMap = getHazelCastMapForEvent(leapHeader);

						// Add the event generated by the service during
						// camel Route and gather it same HazelCastList where
						// component events are added.
						evtProducer.publishEventForRouteCompletion(serviceName, exchange);

						// if the HazelCastList of events is empty than mark
						// dispatching status as complete.
						boolean eventMapStatus = hzEventMap.isEmpty();
						if (eventMapStatus) {
							eventDispatcherTrackerService.updateEventStatus(tenantid, siteid, requestId, eventStoreKey,
									EventTrackerTableConstants.STATUS_COMPLETE, exchange, false, null, false, false);
							logger.debug("notify --No CompletedEvent--found for Feature=" + serviceName);
						} else {
							eventDispatcherTrackerService.updateEventStatus(tenantid, siteid, requestId, eventStoreKey,
									EventTrackerTableConstants.STATUS_IN_PROCESS, exchange, false, null, false, false);
							// publish component and service event and updates
							// status of event track table.
							evtProducer.publishComponentEvent(hzEventMap, tenantid, siteid, requestId, eventStoreKey,
									exchange);
							logger.debug("notify --No CompletedEvent--found for Feature=" + serviceName);
						}

						logger.debug("removing track record after dispatching track status is COMPLETE...!");
						boolean removedTrackRecord = eventDispatcherTrackerService.removeEventTrackRecord(tenantid,
								siteid, requestId, eventStoreKey, exchange);
						// Clean Hazelcast EventList. We are done publishing all
						// events only by checking track record is deleted from
						// tracktable for specific request on status COMPLETE .
						if (removedTrackRecord)
							deleteHazelCastMapForRequest(leapHeader);

						// closing all datasource connection on success
						closeAllDataSourceConnection(leapHeader);

						// Build <Configured> Events for Route Sucecss Condition
						// and publish to event Service
						// Build standard mandatory event for serviceName/Route
						// sucess and publish to eventService
						evtProducer.publishServiceCompletionSuccessSystemEvent(reqCtx, exchange);

					}
					logger.debug("notify -- EventObject=" + exchange.getIn());
				}
			}
		} else if (event instanceof ExchangeFailedEvent) {
			// Cases were Exchange failed specifically
			logger.debug("inside exchanged failed event : ");
			ExchangeFailedEvent compEvent = (ExchangeFailedEvent) event;
			Exchange exchange = compEvent.getExchange();
			LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
			if (leapHeader != null && leapHeader.getTenant() != null && leapHeader.getSite() != null
					&& leapHeader.getRequestUUID() != null) {
				logger.debug("leapHeader in notifier : " + leapHeader);
				RequestContext reqCtx = leapHeader.getRequestContext();
				String servicetype = leapHeader.getServicetype();

				// Build Event for Route Failed Condition and publish to event
				// Service
				evtProducer.publishEventForFailedRouteCompletion(servicetype, exchange);
				// Build standard mandatory event for Failure and publish to
				// eventService
				evtProducer.publishServiceCompletionFailureSystemEvent(reqCtx, exchange);
				// Just debug logs check to see if hazelcast rolledback or not
				checkHazelCastMapValue(leapHeader);
				// Clean Hazelcast EventList. We are done publishing all events.
				deleteHazelCastMapForRequest(leapHeader);
				// closing all datasource connection on success
				closeAllDataSourceConnection(leapHeader);
			}
		} else {
			logger.debug("notify --OtherEventType--Event=" + event);
		}
	}

	/**
	 * to check the multiplicity of exchanges, for the Multicast-EIP
	 * 
	 * @param exchangeProp
	 * @return true if chances of multipleExchangeExists
	 */
	private boolean checkMultipleExchange(Map<String, Object> exchangeProp) {
		if ((!exchangeProp.containsKey(Exchange.MULTICAST_INDEX))) {
			return false;
		} else {
			exchangeProp.remove(Exchange.MULTICAST_INDEX);
			return true;
		}
	}// ..end of the method

	/**
	 * This method is to get List of all LeapEvents Store in hazelcast
	 * 
	 * @param exchange
	 *            : Exchange
	 * @return List
	 */
	private IMap<String, ArrayList<LeapEvent>> getHazelCastMapForEvent(LeapHeader leapHeader) {
		logger.debug("inside getHazelCastMapForEvent() in LeapCamelNotifier bean");
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		String requestId = leapHeader.getRequestUUID();
		logger.debug(".getHazelCastMapForEvent() requestId: " + requestId);
		IMap<String, ArrayList<LeapEvent>> eventMap = hazelcastInstance.getMap(requestId);
		logger.debug("eventList id in getHazelcast : " + eventMap);

		if (eventMap == null || eventMap.isEmpty() || (ArrayList<LeapEvent>) eventMap.get(requestId) == null) {
			logger.debug("notify  ------getHazelCastListValue-List Is Empty");
			return eventMap;
		}
		for (LeapEvent event : (ArrayList<LeapEvent>) eventMap.get(requestId)) {
			logger.debug("notify  ------getHazelCastListValue" + event.getEventId());

		}

		return eventMap;
	}

	/**
	 * Destroy Hazelcast List having events for the given route/camel exhange based
	 * on unique RequestId generated in the baseImpl Route.<br>
	 * 
	 * @param exchange
	 *            : Exchange
	 */
	private void deleteHazelCastMapForRequest(LeapHeader leapHeader) {
		logger.debug("inside deleteHazelCastMapForRequest() in LeapCamelNotifier bean");

		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		String requestId = leapHeader.getRequestUUID();
		logger.debug("rquestId in deletehazelcastEventList : " + requestId);
		IMap<String, ArrayList<LeapEvent>> eventMap = hazelcastInstance.getMap(requestId);
		logger.debug("event list in delete hazelcast map : " + eventMap);
		if (eventMap != null)
			eventMap.destroy();
	}

	/**
	 * This method is to check LeapEvents in hazelcast or not
	 * 
	 * @param exchange
	 *            : Exchange
	 */
	private void checkHazelCastMapValue(LeapHeader leapHeader) {

		logger.debug("inside checkHazelcastListValue() in LeapCamelNotifier bean");
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		String requestId = leapHeader.getRequestUUID();
		logger.debug("request id in checkHazelCastListValue : " + requestId);
		IMap<String, ArrayList<LeapEvent>> eventMap = hazelcastInstance.getMap(requestId);
		logger.debug("eventList id in checkHazelCastListValue : " + eventMap);

		if (eventMap == null || eventMap.isEmpty() || eventMap.get(requestId) == null) {
			logger.debug("notify  ------checkHazelCastListValue-List Is Empty");
			return;
		}
		for (LeapEvent event : (ArrayList<LeapEvent>) eventMap.get(requestId)) {
			logger.debug("notify  ------checkHazelCastListValue" + event.toString());
		}
	}

	/**
	 * This method is to check Exchanged finised success or failure
	 * 
	 * @param exchangeProp
	 * @return
	 */
	private boolean isCompletedWithFailure(Map<String, Object> exchangeProp) {

		logger.debug("inside isCompleteWithFailure() in LeapCamelNotifier bean");

		boolean isfailure = exchangeProp.containsKey("CamelFailureRouteId");
		logger.debug("isFailure is  : " + isfailure);

		return isfailure;
	}

	/**
	 * This method is used close all open data source connection
	 * 
	 * @param leapHeader
	 *            : leapHeader Object
	 * @throws SQLException
	 */
	private void closeAllDataSourceConnection(LeapHeader leapHeader) throws SQLException {
		logger.debug(".closeAllConnection method of LeapCamelEventNotifier");
		Map<Object, Object> mapResourceHolder = leapHeader.getResourceHolder();
		for (int idx = 0; idx < mapResourceHolder.size(); idx++) {
			Connection connection = (Connection) mapResourceHolder.get(idx);
			if (connection != null) {
				if (!(connection.isClosed())) {
					connection.close();
				}
			}
		} // end of for
	}// end of method closeAllConnection

}
