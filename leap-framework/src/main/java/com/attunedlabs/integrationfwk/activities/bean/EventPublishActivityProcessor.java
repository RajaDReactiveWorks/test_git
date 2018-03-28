package com.attunedlabs.integrationfwk.activities.bean;

import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventframework.camel.eventproducer.ICamelEventBuilder;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventPipeline;
import com.attunedlabs.integrationfwk.activities.event.PipelineEventBuilder;
import com.attunedlabs.integrationfwk.config.jaxb.EventPublishActivity;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.hazelcast.core.HazelcastInstance;
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
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		String requestId = leapHeader.getRequestUUID();

		ICamelEventBuilder evtBuilder = new PipelineEventBuilder();
		LeapEvent leapEvent = evtBuilder.buildEvent(exchange, event);
		logger.debug("leapEvent : : " + evtBuilder.toString());

		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();

		logger.debug(".getHazelCastListforServiceEvent() requestId: " + requestId);
		IMap<String, ArrayList<LeapEvent>> serviceEventMap = hazelcastInstance.getMap(requestId);
		ArrayList<LeapEvent> leapEventList = null;

		if (((ArrayList<LeapEvent>) serviceEventMap.get(requestId)) == null)
			leapEventList = new ArrayList<>();
		else
			leapEventList = ((ArrayList<LeapEvent>) serviceEventMap.get(requestId));

		logger.debug("eventlist before adding event : " + leapEventList);
		if (serviceEventMap != null && leapEventList != null) {
			leapEventList.add(leapEvent);
			serviceEventMap.put(requestId, leapEventList);
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
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		RequestContext requestContext = leapHeader.getRequestContext();
		ConfigurationContext configContext = requestContext.getConfigurationContext();
		logger.debug("configuarion context : " + configContext);
		IEventFrameworkConfigService evtFwkConfigService = new EventFrameworkConfigService();
		Event event = evtFwkConfigService.getEventConfiguration(configContext, eventname);

		// check if event is pipeline enabled or not
		EventPipeline eventpipline = event.getEventPipeline();

		logger.debug("eventpipeline joga : " + eventpipline.toString());
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
