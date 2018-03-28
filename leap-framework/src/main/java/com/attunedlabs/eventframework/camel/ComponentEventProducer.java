package com.attunedlabs.eventframework.camel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventframework.camel.eventbuilder.EventBuilderHelper;
import com.attunedlabs.eventframework.camel.eventbuilder.OgnlEventBuilder;
import com.attunedlabs.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.attunedlabs.eventframework.camel.eventproducer.ServiceCompletionFailureEventBuilder;
import com.attunedlabs.eventframework.camel.eventproducer.ServiceCompletionSuccessEventBuilder;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.EventFrameworkDispatcherException;
import com.attunedlabs.eventframework.dispatcher.chanel.DispatchChanelService;
import com.attunedlabs.eventframework.dispatcher.chanel.IDispatchChanelService;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.event.ILeapEventService;
import com.attunedlabs.eventframework.event.InvalidEventException;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.event.LeapEventService;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerException;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.eventframework.eventtracker.impl.EventTrackerTableConstants;
import com.attunedlabs.eventframework.eventtracker.util.EventTrackerUtil;
import com.attunedlabs.eventframework.jaxb.CamelEventBuilder;
import com.attunedlabs.eventframework.jaxb.CamelEventProducer;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * This class is responsible for generating component internal events, route
 * completion internal event and system events
 * 
 * @author ubuntu
 *
 */
public class ComponentEventProducer {

	final Logger logger = LoggerFactory.getLogger(ComponentEventProducer.class);

	// EventConfigurationService evtConfigService =
	// EventConfigurationService.getEventConfigurationService();
	IEventFrameworkConfigService evtConfigService = new EventFrameworkConfigService();
	IDispatchChanelService dispacherChanelService = DispatchChanelService.getDispatchChanelService();
	IEventDispatcherTrackerService eventDispatcherTrackerService = new EventDispatcherTrackerImpl();

	/**
	 * This method is used to publish events present in the list.(i.e, service
	 * and component events.)
	 * 
	 * @param hcEventMap
	 *            : list of component and service event in hazelcast
	 * @throws EventDispatcherTrackerException
	 */
	public void publishComponentEvent(IMap<String, ArrayList<LeapEvent>> hcEventMap, final String tenantId,
			final String siteId, final String requestId, final String eventStoreKey, Exchange camelExchange)
			throws EventDispatcherTrackerException {
		logger.debug("inside publishComponentEvent() in CamelEventSynchProducer bean");
		if (hcEventMap == null || hcEventMap.isEmpty()) {
			return;
		}
		JSONObject failureJson = new JSONObject();
		boolean isFailure = false;

		ILeapEventService eventService = new LeapEventService();

		if ((ArrayList<LeapEvent>) hcEventMap.get(requestId) != null) {
			for (LeapEvent leapevent : (ArrayList<LeapEvent>) hcEventMap.get(requestId)) {
				try {
					eventService.publishEvent(leapevent);
					logger.debug("removing the event from the HazelCast list after successfullyDelivered event -- >"
							+ leapevent.toString());
					hcEventMap.remove(requestId);
					logger.debug("-checkHazelCastListValue" + leapevent.toString());
				} catch (InvalidEventException | EventFrameworkConfigurationException
						| LeapEventTransformationException insExp) {
					isFailure = true;
					String failureMsg = insExp.getMessage();
					EventTrackerUtil.setFailureJSONString(failureJson, insExp, failureMsg);
					logger.error("Failed to publish event for eventId : " + leapevent.getEventId() + "!", insExp);
					eventDispatcherTrackerService.updateEventStatus(tenantId, siteId, requestId, eventStoreKey,
							EventTrackerTableConstants.STATUS_FAILED, camelExchange, isFailure, failureJson.toString(),
							true, false);

				} catch (MessageDispatchingException mDispatchingException) {
					isFailure = true;
					String failureMsg = mDispatchingException.getMessage();
					EventTrackerUtil.setFailureJSONString(failureJson, mDispatchingException, failureMsg);
					logger.error("Failed to publish event for eventId : " + leapevent.getEventId() + "!",
							mDispatchingException);
					eventDispatcherTrackerService.updateEventStatus(tenantId, siteId, requestId, eventStoreKey,
							EventTrackerTableConstants.STATUS_FAILED, camelExchange, isFailure, failureJson.toString(),
							true, false);

				} catch (Exception e) {
					e.printStackTrace();
					isFailure = true;
					String failureMsg = e.getMessage();
					EventTrackerUtil.setFailureJSONString(failureJson, e, failureMsg);
					logger.error("Failed to publish event for eventId : " + leapevent.getEventId() + "!", e);
					eventDispatcherTrackerService.updateEventStatus(tenantId, siteId, requestId, eventStoreKey,
							EventTrackerTableConstants.STATUS_FAILED, camelExchange, isFailure, failureJson.toString(),
							true, false);
//					logger.error("skip the exception other the above catched...");
				}
			}
			if (!isFailure) {
				logger.debug("updating status to complete...!");
				eventDispatcherTrackerService.updateEventStatus(tenantId, siteId, requestId, eventStoreKey,
						EventTrackerTableConstants.STATUS_COMPLETE, camelExchange, isFailure, null, false, false);
			}
		}
	}

	/**
	 * This method is used to publish system event
	 * 
	 * @param hcEventList
	 *            : list of system event in hazelcast
	 */
	public void publishSystemEvent(List<LeapEvent> hcEventList) {
		logger.debug("inside publishSystemEvent() in CamelEventSynchProducer bean");
		if (hcEventList == null || hcEventList.isEmpty()) {
			return;
		}

		ILeapEventService eventService = new LeapEventService();
		for (LeapEvent leapevent : hcEventList) {
			try {
				logger.debug(".publishServiceCompletionSuccessEvent  EVENT=" + leapevent);
				eventService.publishSystemEvent(leapevent);
				logger.debug("-checkHazelCastListValue" + leapevent.toString());
			} catch (InvalidEventException | LeapEventTransformationException | MessageDispatchingException
					| EventFrameworkConfigurationException invalidEvtExp) {
				// #TODO Eating exception till I know what to do
				logger.error("InvalidSystemEvent", invalidEvtExp);
				break;
			}
		}
	}

	/**
	 * This method is to publish internal event for route completion for success
	 * 
	 * @param tenantId
	 * @param camelContextId
	 * @param serviceType
	 * @param camelExchange
	 */
	public void publishEventForRouteCompletion(String serviceName, Exchange camelExchange) {
		logger.debug("inside publishEventForRouteCompletion() in CamelEventSynchProducer bean");
		// LeapHeader data
		LeapHeader leapHeader = (LeapHeader) camelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		RequestContext reqCtx = leapHeader.getRequestContext();
		// service event are at site level
//		ConfigurationContext configCtx1 = new ConfigurationContext(reqCtx.getTenantId(), reqCtx.getSiteId(), null, null,
//				null);
		ConfigurationContext configCtx1 = new ConfigurationContext(reqCtx);
		// Events to be fired when service completed sucessfully only
		try {
			logger.debug("service name : " + serviceName + ", config context : " + configCtx1);
			List<Event> evtProdListSucess = evtConfigService
					.getEventConfigProducerForServiceSuccessCompletion(configCtx1, serviceName);

			// logger.info("CamelEventProducerList size in
			// CamelEventSynchProducer : " + evtProdListSucess.size());
			// Events to be fired when service completed for all condition
			// (Sucess or failure)
			List<Event> evtProdListAll = evtConfigService
					.getEventConfigProducerForServiceFailedAndSucessCompletion(configCtx1, serviceName);
			// Merge the two List into one and check for nulls
			List<Event> evtProdList = mergeEventProducerList(evtProdListSucess, evtProdListAll);
			if (evtProdList == null || evtProdList.isEmpty()) {
				logger.debug("No Event configured for SucessServiceCompletion for Service{" + serviceName
						+ "} and requestContext=" + configCtx1);
				return;
			}
			// else build the Event and Publish Them
			addServiceEventFromEventProducerMap(camelExchange, evtProdList);
		} catch (EventBuilderInstantiationException | InvalidEventException | EventFrameworkDispatcherException
				| EventFrameworkConfigurationException exp) {
		}
	}// end of method

	public void publishEventForFailedRouteCompletion(String serviceName, Exchange camelExchange) {
		logger.debug("inside publishEventForFailedRoute() in CamelEventSynchProducer");
		// LeapHeader data
		LeapHeader leapHeader = (LeapHeader) camelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		RequestContext reqCtx = leapHeader.getRequestContext();
		ConfigurationContext configCtx = new ConfigurationContext(reqCtx);
		// service event are at site level
//		ConfigurationContext configCtx1 = new ConfigurationContext(configCtx.getTenantId(), configCtx.getSiteId(), null,
//				null, null);
		try {
			List<Event> evtProdListFailure = evtConfigService
					.getEventConfigProducerForServiceFailedCompletion(configCtx, serviceName);
			List<Event> evtProdListAll = evtConfigService
					.getEventConfigProducerForServiceFailedAndSucessCompletion(configCtx, serviceName);
			// Merge the two List into one and check for nulls
			List<Event> evtProdList = mergeEventProducerList(evtProdListFailure, evtProdListAll);
			if (evtProdList == null || evtProdList.isEmpty()) {
				logger.debug("No Event configured for ServiceCompletionFailure for Service{" + serviceName
						+ "} and requestContext=" + reqCtx);
				return;
			}
			// else build the Event and Publish Them
			publishEventForEventProducerList(camelExchange, evtProdList);

			// else build the Event and add Them to serviceEventList
			// addServiceEventFromEventProducerList(camelExchange, evtProdList);
		} catch (EventBuilderInstantiationException | InvalidEventException | EventFrameworkConfigurationException
				| LeapEventTransformationException | MessageDispatchingException exp) {
			// #TODO
		}
	}

	private void addServiceEventFromEventProducerMap(Exchange camelExchange, List<Event> evtProdList)
			throws EventBuilderInstantiationException, InvalidEventException, EventFrameworkDispatcherException {
		AbstractCamelEventBuilder evtBuilder = null;
		LeapHeader leapHeader = (LeapHeader) camelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);

		if (evtProdList == null || evtProdList.isEmpty())
			return;
		for (Event evt : evtProdList) {
			CamelEventProducer evtProd = evt.getCamelEventProducer();
			boolean isOgnl = EventBuilderHelper.isOgnlBuilderType(evtProd);
			if (isOgnl) {
				// EventBuilderHelper.constructSourceForOGNL(camelExchange);
				evtBuilder = getOGNLEventBuilderInstance();
			} else {
				evtBuilder = getEventBuilderInstance(evtProd.getCamelEventBuilder());
			}

			LeapEvent leapevent = evtBuilder.buildEvent(camelExchange, evt);
			leapevent.addEventParam(LeapHeaderConstant.TENANT_KEY, leapHeader.getTenant());
			leapevent.addEventParam(LeapHeaderConstant.SITE_KEY, leapHeader.getSite());
			
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			String requestId = leapHeader.getRequestUUID();

			logger.debug(".getHazelCastListforServiceEvent() requestId: " + requestId);
			IMap<String, ArrayList<LeapEvent>> serviceEventMap = hazelcastInstance.getMap(requestId);
			ArrayList<LeapEvent> leapEventList = null;

			if (((ArrayList<LeapEvent>) serviceEventMap.get(requestId)) == null)
				leapEventList = new ArrayList<>();
			else
				leapEventList = ((ArrayList<LeapEvent>) serviceEventMap.get(requestId));

			logger.debug("eventlist before adding event : " + leapEventList);
			if (serviceEventMap != null && leapEventList != null) {
				leapEventList.add(leapevent);
				serviceEventMap.put(requestId, leapEventList);
			}
			logger.debug("Event added for EventId{" + leapevent.getEventId() + "}");
		}
	}

	/**
	 * Here instead of publishing we have added RoiEvent in service-evt-uuid
	 * list.
	 * 
	 * @param camelExchange
	 * @param evtProdList
	 * @throws EventBuilderInstantiationException
	 * @throws InvalidEventException
	 * @throws EventFrameworkDispatcherException
	 * @throws MessageDispatchingException
	 * @throws LeapEventTransformationException
	 * @throws EventFrameworkConfigurationException
	 */
	private void publishEventForEventProducerList(Exchange camelExchange, List<Event> evtProdList)
			throws EventBuilderInstantiationException, InvalidEventException, EventFrameworkConfigurationException,
			LeapEventTransformationException, MessageDispatchingException {
		AbstractCamelEventBuilder evtBuilder = null;
		if (evtProdList == null || evtProdList.isEmpty())
			return;

		ILeapEventService eventService = new LeapEventService();
		for (Event evt : evtProdList) {
			CamelEventProducer evtProd = evt.getCamelEventProducer();
			boolean isOgnl = EventBuilderHelper.isOgnlBuilderType(evtProd);
			if (isOgnl) {
				// EventBuilderHelper.constructSourceForOGNL(camelExchange);
				evtBuilder = getOGNLEventBuilderInstance();
			} else {
				evtBuilder = getEventBuilderInstance(evtProd.getCamelEventBuilder());
			}

			LeapEvent leapevent = evtBuilder.buildEvent(camelExchange, evt);
			eventService.publishEvent(leapevent);
			logger.debug("Event published for EventId{" + leapevent.getEventId() + "}");
		}
	}

	/**
	 * This method is to get OGNL Event Builder Instance
	 * 
	 * @return AbstractCamelEventBuilder
	 */
	private AbstractCamelEventBuilder getOGNLEventBuilderInstance() {
		logger.debug("inside getOGNLEventBuilderInstance() of CamelEventSynchProducer");
		AbstractCamelEventBuilder evtBuilderInstance = (AbstractCamelEventBuilder) new OgnlEventBuilder();
		return evtBuilderInstance;
	}

	private List<Event> mergeEventProducerList(List<Event> sucessFailureList, List<Event> allList) {
		if (sucessFailureList == null && allList == null) {
			return null;
		} else if (sucessFailureList != null && allList == null) {
			return sucessFailureList;
		} else if (sucessFailureList == null && allList != null) {
			return allList;
		} else {
			sucessFailureList.addAll(allList);
			return sucessFailureList;
		}
	}

	/**
	 * Publish the standard/mandatory ServiceCompletionSucessEvent.
	 * 
	 * @param camelExchange
	 */
	public void publishServiceCompletionSuccessSystemEvent(RequestContext reqCtx, Exchange camelExchange) {
		logger.debug("inside publishServiceCompletionSuccessEvent");
		ConfigurationContext configCtx = new ConfigurationContext(reqCtx);
		ServiceCompletionSuccessEventBuilder builder = (ServiceCompletionSuccessEventBuilder) evtConfigService
				.getServiceCompletionSuccessEventBuilder(configCtx);
		// Event configuration not required
		LeapEvent evt = builder.buildEvent(camelExchange, null);
		// #TODO Handling in separate Dispatcher
		// IExecutorService
		// disExeService=HCService.getHCInstance().getSystemEventDispatcherExecutor(tenantId)
		ILeapEventService eventService = new LeapEventService();
		try {
			// .. logic to skip the WireTap exchange completion
			if ((!camelExchange.getPattern().name().equalsIgnoreCase("inonly")
					&& !checkforWireTap(camelExchange.getProperties()))
					|| (!camelExchange.getPattern().name().equalsIgnoreCase("inonly")
							&& checkforWireTap(camelExchange.getProperties()))) {
				eventService.publishSystemEvent(evt);
			} else {
				logger.info("Skipping Exchange completion System-Event for Copies");
			}
		} catch (InvalidEventException | LeapEventTransformationException | MessageDispatchingException
				| EventFrameworkConfigurationException invalidEvtExp) {
			// #TODO Eating exception till I kno what to do
			logger.error("InvalidSystemEvent", invalidEvtExp);
		}
		logger.debug(".publishServiceCompletionSuccessEvent  EVENT=" + evt);
	}

	/**
	 * Publish the standard/mandatory ServiceCompletionFailureEvent.
	 * 
	 * @param camelExchange
	 */
	public void publishServiceCompletionFailureSystemEvent(RequestContext reqCtx, Exchange camelExchange) {
		logger.debug("inside publishServiceCompletionFailureEvent method of CamelEventSysnchProducer");
		ConfigurationContext configCtx = new ConfigurationContext(reqCtx);

		ServiceCompletionFailureEventBuilder builder = (ServiceCompletionFailureEventBuilder) evtConfigService
				.getServiceCompletionFailureEventBuilder(configCtx);
		// Event configuration not required
		LeapEvent evt = builder.buildEvent(camelExchange, null);
		ILeapEventService eventService = new LeapEventService();
		try {
			eventService.publishSystemEvent(evt);
		} catch (InvalidEventException | LeapEventTransformationException | MessageDispatchingException
				| EventFrameworkConfigurationException invalidEvtExp) {
			// #TODO Eating exception till I know what to do
			logger.error("InvalidSystemEvent", invalidEvtExp);
		}
		logger.debug(".publishServiceFaliureSuccessEvent  EVENT=" + evt);
	}

	/**
	 * 
	 * @param exchangeProperties
	 * @return
	 */
	private boolean checkforWireTap(Map<String, Object> exchangeProperties) {
		List list = (List) exchangeProperties.get("CamelMessageHistory");
		logger.debug("ListOf CamelProps: " + list.toString());
		String txt = list.toString();
		String re1 = ".*?"; // Non-greedy match on filler
		String re2 = "(wireTap)"; // Word 1
		boolean hasKey = false;
		try {
			Pattern p = Pattern.compile(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			Matcher m = p.matcher(txt);
			String word1 = null;
			if (m.find()) {
				word1 = m.group(1);
			}
			if (word1.toLowerCase().equalsIgnoreCase("wiretap")) {
				hasKey = true;
			}
		} catch (NullPointerException e) {
			hasKey = false;
		}
		return hasKey;
	}// ..end of method

	// #TODO THIS WILL NOT WORK IN osgi.TO CHANGED EITHER loaded spring or osgi
	// registry
	/**
	 * This method is used to create custom Event builder Instance
	 * 
	 * @param cEvtbuilder
	 * @return
	 * @throws EventBuilderInstantiationException
	 */
	private AbstractCamelEventBuilder getEventBuilderInstance(CamelEventBuilder cEvtbuilder)
			throws EventBuilderInstantiationException {

		logger.debug("inside getEventBuilderInstance method of CamelEventSynchProducer");
		String fqcn = cEvtbuilder.getEventBuilder().getFqcn();
		Class builderClass = null;
		try {
			// #TODO will not work in OSGI
			builderClass = Class.forName(fqcn);
			AbstractCamelEventBuilder evtBuilderInstance;
			evtBuilderInstance = (AbstractCamelEventBuilder) builderClass.newInstance();
			return evtBuilderInstance;
		} catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EventBuilderInstantiationException();
		}
	}

}
