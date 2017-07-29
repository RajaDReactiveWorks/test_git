package com.getusroi.integrationfwk.activities.bean;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.eventframework.camel.eventproducer.ICamelEventBuilder;
import com.getusroi.eventframework.config.EventFrameworkConfigurationException;
import com.getusroi.eventframework.config.IEventFrameworkConfigService;
import com.getusroi.eventframework.config.impl.EventFrameworkConfigService;
import com.getusroi.eventframework.event.ROIEvent;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.eventframework.jaxb.EventPipeline;
import com.getusroi.integrationfwk.activities.event.PipelineEventBuilder;
import com.getusroi.integrationfwk.config.jaxb.EventPublishActivity;
import com.getusroi.integrationfwk.config.jaxb.PipeActivity;
import com.getusroi.mesh.MeshHeader;
import com.getusroi.mesh.MeshHeaderConstant;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.TransactionalList;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;

public class EventPublishActivityProcessor {
	Logger logger = LoggerFactory.getLogger(EventPublishActivityProcessor.class);

	/**
	 * This method is used to process the event publish pipeline activity, by
	 * associating the pipeline activity data with event param of event
	 * configuration.
	 * 
	 * @param exchange
	 *            : Camel Exchange Object
	 * @throws EventPublishActivityException
	 */
	public void processPipelineEvent(Exchange exchange) throws EventPublishActivityException {
		logger.debug(".processPipelineEvent method of EventPublishActivityProcessor");
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		EventPublishActivity eventPublishActivity = pipeactivity.getEventPublishActivity();
		String eventname = eventPublishActivity.getEventName();
		try {
			Event event = searchEventConfigurationByEventName(eventname, exchange);
			if (event != null) {
				logger.debug("Event is not null for event name : " + eventname);
				createAndStorePipelineEventInCache(event, exchange);
			} else {
				throw new EventPublishActivityException(
						"event configuration defined by event name " + eventname + " is null");
			}
		} catch (EventFrameworkConfigurationException e) {
			logger.error("No event configuration defined by event name " + eventname, e.getMessage());
			throw new EventPublishActivityException("No event configuration defined by event name " + eventname, e);
		}
	}// end of method processPipelineEvent

	/**
	 * This method is usd to create pipeline activity event and store in
	 * cache,so that it should be dispatched later
	 * 
	 * @param event
	 *            : Event Object
	 * @param exchange
	 *            : Camel Exchange object
	 * @throws JSONException
	 */

	private void createAndStorePipelineEventInCache(Event event, Exchange exchange) {
		logger.debug(".addEventParamFromEventPipeActivity method of EventPublishActivityProcessor");
		MeshHeader meshHeader = (MeshHeader) exchange.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);
		String requestId = meshHeader.getRequestUUID();
		TransactionContext hcTransactionContext = meshHeader.getHazelcastTransactionalContext();
		logger.debug("addEventParamFromEventPipeActivity requestId=" + requestId + "--hcTransactionContext="
				+ hcTransactionContext);
		if (hcTransactionContext != null) {
			TransactionalList<ROIEvent> eventList = hcTransactionContext.getList(requestId);
			logger.debug("addEventParamFromEventPipeActivity transactionalList=" + eventList);
			ICamelEventBuilder evtBuilder = new PipelineEventBuilder();
			ROIEvent roiEvent = evtBuilder.buildEvent(exchange, event);
			TransactionalMap<String, ROIEvent> genericEvent = hcTransactionContext.getMap(requestId);
			if (eventList != null) {
				if (roiEvent.getEventId().equalsIgnoreCase("ServiceRequest-WorkOrderUpdated")) {
					if (genericEvent.isEmpty()) {
						genericEvent.putIfAbsent("genericEvent", roiEvent);
						eventList.add(genericEvent.get("genericEvent"));
					}
				} else
					eventList.add(roiEvent);
			}
			logger.debug("hazelcast transactional event size : " + eventList.size());
		} else {
			HazelcastInstance hazelcastInstance = meshHeader.getHazelcastNonTransactionalContext();
			IList<ROIEvent> eventList = hazelcastInstance.getList(requestId);
			logger.debug("addEventParamFromEventPipeActivity transactionalList=" + eventList);
			ICamelEventBuilder evtBuilder = new PipelineEventBuilder();
			ROIEvent roiEvent = evtBuilder.buildEvent(exchange, event);
			IMap<String, ROIEvent> genericEvent = hazelcastInstance.getMap(requestId);
			if (eventList != null) {
				if (roiEvent.getEventId().equalsIgnoreCase("ServiceRequest-WorkOrderUpdated")) {
					if (genericEvent.isEmpty()) {
						genericEvent.putIfAbsent("genericEvent", roiEvent);
						eventList.add(genericEvent.get("genericEvent"));
					}
				} else
					eventList.add(roiEvent);
			}
			logger.debug("hazelcast instance event size : " + eventList.size());
		}

		
		// build an event
	}// end of method addEventParamFromEventPipeActivity



	/**
	 * This method is used to search event configuration defined in
	 * configuration file from cache using event name
	 * 
	 * @param eventname
	 *            : Event Name in String
	 * @param exchange
	 *            : Camel Exchange Object
	 * @return Event Object
	 * @throws EventFrameworkConfigurationException
	 * @throws EventPublishActivityException
	 */
	private Event searchEventConfigurationByEventName(String eventname, Exchange exchange)
			throws EventFrameworkConfigurationException, EventPublishActivityException {
		logger.debug(".searchEventConfigurationByEventName method of EventPublishActivityProcessor");
		MeshHeader meshHeader = (MeshHeader) exchange.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);
		RequestContext requestContext = meshHeader.getRequestContext();
		ConfigurationContext configContext = requestContext.getConfigurationContext();
		logger.debug("configuarion context : " + configContext);
		IEventFrameworkConfigService evtFwkConfigService = new EventFrameworkConfigService();
		Event event = evtFwkConfigService.getEventConfiguration(configContext, eventname);
		// check if event is pipeline enabled or not
		EventPipeline eventpipline = event.getEventPipeline();
		if (eventpipline != null) {
			boolean enabled = eventpipline.isEnabled();
			if (enabled) {
				return event;
			} else {
				throw new EventPublishActivityException("event : " + eventname + " is pipeline event but not enabled");
			}
		} else {
			throw new EventPublishActivityException("event : " + eventname + " is not a pipeline event");
		}
	}// end of method searchEventConfigurationByEventName

}
