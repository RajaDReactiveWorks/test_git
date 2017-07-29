package com.getusroi.eventframework.camel;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.eventframework.camel.eventbuilder.EventBuilderHelper;
import com.getusroi.eventframework.camel.eventbuilder.OgnlEventBuilder;
import com.getusroi.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.getusroi.eventframework.camel.eventproducer.ServiceCompletionFailureEventBuilder;
import com.getusroi.eventframework.camel.eventproducer.ServiceCompletionSuccessEventBuilder;
import com.getusroi.eventframework.config.EventFrameworkConfigurationException;
import com.getusroi.eventframework.config.IEventFrameworkConfigService;
import com.getusroi.eventframework.config.impl.EventFrameworkConfigService;
import com.getusroi.eventframework.dispatcher.EventFrameworkDispatcherException;
import com.getusroi.eventframework.dispatcher.chanel.DispatchChanelService;
import com.getusroi.eventframework.dispatcher.chanel.IDispatchChanelService;
import com.getusroi.eventframework.event.IROIEventService;
import com.getusroi.eventframework.event.InvalidEventException;
import com.getusroi.eventframework.event.ROIEvent;
import com.getusroi.eventframework.event.ROIEventService;
import com.getusroi.eventframework.jaxb.CamelEventBuilder;
import com.getusroi.eventframework.jaxb.CamelEventProducer;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.mesh.MeshHeader;
import com.getusroi.mesh.MeshHeaderConstant;

/**
 * This class is responsible for generating component internal events, route
 * completion internal event and system events
 * 
 * @author ubuntu
 *
 */
public class CamelEventSynchProducer {
	final Logger logger = LoggerFactory.getLogger(CamelEventSynchProducer.class);

	// EventConfigurationService evtConfigService =
	// EventConfigurationService.getEventConfigurationService();
	IEventFrameworkConfigService evtConfigService = new EventFrameworkConfigService();
	IDispatchChanelService dispacherChanelService = DispatchChanelService.getDispatchChanelService();

	/**
	 * This method is used to publish component event
	 * 
	 * @param hcEventList
	 *            : list of component event in hazelcast
	 */
	public void publishComponentEvent(List<ROIEvent> hcEventList) {
		logger.debug("inside publishComponentEvent() in CamelEventSynchProducer bean");
		if (hcEventList == null || hcEventList.isEmpty()) {
			return;
		}

		IROIEventService eventService = new ROIEventService();
		for (ROIEvent roievent : hcEventList) {
			try {
				eventService.publishEvent(roievent);
				logger.debug("-checkHazelCastListValue" + roievent.toString());
			} catch (InvalidEventException | EventFrameworkDispatcherException insExp) {
				// #TODO Strategy for handling event calls for instantiating
				// issues beyond Logging
				insExp.printStackTrace();
				logger.error("Failed to Instantiate the EventBuilder", insExp);
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
		// MeshHeader data
		MeshHeader meshHeader = (MeshHeader) camelExchange.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);
		RequestContext reqCtx = meshHeader.getRequestContext();
		// service event are at site level
		ConfigurationContext configCtx1 = new ConfigurationContext(reqCtx.getTenantId(), reqCtx.getSiteId(), null,
				null,null);
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
			publishEventForEventProducerList(camelExchange, evtProdList);
		} catch (EventBuilderInstantiationException | InvalidEventException | EventFrameworkDispatcherException
				| EventFrameworkConfigurationException exp) {
		}
	}// end of method

	public void publishEventForFailedRouteCompletion(String serviceName, Exchange camelExchange) {
		logger.debug("inside publishEventForFailedRoute() in CamelEventSynchProducer");
		// MeshHeader data
		MeshHeader meshHeader = (MeshHeader) camelExchange.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);
		RequestContext reqCtx = meshHeader.getRequestContext();
		ConfigurationContext configCtx = new ConfigurationContext(reqCtx);
		// service event are at site level
		ConfigurationContext configCtx1 = new ConfigurationContext(configCtx.getTenantId(), configCtx.getSiteId(), null,
				null,null);
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
		} catch (EventBuilderInstantiationException | InvalidEventException | EventFrameworkDispatcherException
				| EventFrameworkConfigurationException exp) {
			// #TODO
		}
	}

	private void publishEventForEventProducerList(Exchange camelExchange, List<Event> evtProdList)
			throws EventBuilderInstantiationException, InvalidEventException, EventFrameworkDispatcherException {
		AbstractCamelEventBuilder evtBuilder = null;
		if (evtProdList == null || evtProdList.isEmpty())
			return;

		IROIEventService eventService = new ROIEventService();
		for (Event evt : evtProdList) {
			CamelEventProducer evtProd = evt.getCamelEventProducer();
			boolean isOgnl = EventBuilderHelper.isOgnlBuilderType(evtProd);
			if (isOgnl) {
				// EventBuilderHelper.constructSourceForOGNL(camelExchange);
				evtBuilder = getOGNLEventBuilderInstance();
			} else {
				evtBuilder = getEventBuilderInstance(evtProd.getCamelEventBuilder());
			}

			ROIEvent roievent = evtBuilder.buildEvent(camelExchange, evt);
			eventService.publishEvent(roievent);
			logger.debug("Event published for EventId{" + roievent.getEventId() + "}");
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
		ROIEvent evt = builder.buildEvent(camelExchange, null);
		// #TODO Handling in seperate Dispatcher
		// IExecutorService
		// disExeService=HCService.getHCInstance().getSystemEventDispatcherExecutor(tenantId)
		IROIEventService eventService = new ROIEventService();
		try {
			// .. logic to skip the WireTap exchang completion
			if ((!camelExchange.getPattern().name().equalsIgnoreCase("inonly")
					&& !checkforWireTap(camelExchange.getProperties()))
					|| (!camelExchange.getPattern().name().equalsIgnoreCase("inonly")
							&& checkforWireTap(camelExchange.getProperties()))) {
				eventService.publishSystemEvent(evt);
			}else{
				logger.info("Skipping Exchange completion System-Event for Copies");
			}
		} catch (InvalidEventException | EventFrameworkDispatcherException invalidEvtExp) {
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
		ROIEvent evt = builder.buildEvent(camelExchange, null);
		IROIEventService eventService = new ROIEventService();
		try {
			eventService.publishSystemEvent(evt);
		} catch (InvalidEventException | EventFrameworkDispatcherException invalidEvtExp) {
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
