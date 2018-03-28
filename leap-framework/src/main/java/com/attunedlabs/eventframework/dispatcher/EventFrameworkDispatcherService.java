package com.attunedlabs.eventframework.dispatcher;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventDispatcher;
import com.attunedlabs.eventframework.jaxb.SystemEvent;

/**
 * 
 * @author bizruntime
 *
 */
public class EventFrameworkDispatcherService implements IEventFrameworkIDispatcherService {
	protected static final Logger logger = LoggerFactory.getLogger(EventFrameworkDispatcherService.class);
	private IEventFrameworkConfigService eventFrameworkConfigService;

	public EventFrameworkDispatcherService() {
		eventFrameworkConfigService = new EventFrameworkConfigService();
	}

	/**
	 * This method is to dispatch event to executor service
	 * 
	 * @param :
	 *            LeapEvent
	 * @throws EventFrameworkConfigurationException
	 * @throws EventFrameworkDispatcherException
	 * @throws MessageDispatchingException
	 * @throws LeapEventTransformationException
	 */
	public void dispatchforEvent(LeapEvent leapEvent)
			throws EventFrameworkConfigurationException, LeapEventTransformationException, MessageDispatchingException {
		logger.debug("inside dispatchforEvent() in LeapDispachterService : ");
		String eventId = leapEvent.getEventId();

		ConfigurationContext configContext = getConfigurationContext(leapEvent.getRequestContext());
		logger.debug("config context : " + configContext);

		Event eventConfig = null;
		try {
			eventConfig = eventFrameworkConfigService.getEventConfiguration(configContext, eventId);
			if (eventConfig == null) {
				throw new EventFrameworkConfigurationException(
						"DispatcherService failed to get EventConfiguration for EventId{" + eventId + "} and context="
								+ configContext);
			}
		} catch (EventFrameworkConfigurationException e) {
			throw new EventFrameworkConfigurationException(
					"DispatcherService failed to get EventConfiguration for EventId{" + eventId + "} and context="
							+ configContext,
					e);
		}

		// Event can have more than One Dispatchers
		List<EventDispatcher> evtDispatcherConfigList = eventConfig.getEventDispatchers().getEventDispatcher();
		for (EventDispatcher evtDispatchConfig : evtDispatcherConfigList) {
			getDispatcherTask(evtDispatchConfig, leapEvent).dispatchToChannel();
		} // end of For
	}// end of method

	/**
	 * This method is to dispatch event to executor service
	 * 
	 * @param :
	 *            LeapEvent
	 * @throws MessageDispatchingException
	 * @throws LeapEventTransformationException
	 * @throws EventFrameworkConfigurationException
	 * @throws TimeoutException
	 */
	public void dispatchforSystemEvent(LeapEvent leapEvent)
			throws LeapEventTransformationException, MessageDispatchingException, EventFrameworkConfigurationException {
		logger.debug("inside dispatchforEvent() in LeapDispachterService : ");
		String eventId = leapEvent.getEventId();
		logger.debug("event in EventFramework dispatcher service : " + leapEvent.toString());

		logger.debug("request context in EventFrameworkDispatcherService : " + leapEvent.getRequestContext());
		ConfigurationContext configContext = getConfigurationContext(leapEvent.getRequestContext());
		logger.debug("config context : " + configContext);

		SystemEvent systemEventConfig = null;
		try {
			systemEventConfig = eventFrameworkConfigService.getSystemEventConfiguration(configContext, eventId);
			// code should not give any error in the because it must generate
			// system event, But its giving so commenting
			if (systemEventConfig == null) {
				logger.warn("DispatcherService failed to get SystemEventConfiguration for EventId{" + eventId
						+ "} and context=" + configContext);
				// throw new
				// EventFrameworkDispatcherException("DispatcherService failed
				// to get SystemEventConfiguration for EventId{"+eventId+"} and
				// context="+configContext);
			}
		} catch (EventFrameworkConfigurationException e) {
			throw new EventFrameworkConfigurationException(
					"DispatcherService failed to get EventConfiguration for EventId{" + eventId + "} and context="
							+ configContext,
					e);
		}

		// Event can have more than One Dispatchers
		if (systemEventConfig != null)
			if (systemEventConfig.getEventDispatchers() != null) {
				List<EventDispatcher> evtDispatcherConfigList = systemEventConfig.getEventDispatchers()
						.getEventDispatcher();
				for (EventDispatcher evtDispatchConfig : evtDispatcherConfigList) {
					getDispatcherTask(evtDispatchConfig, leapEvent).dispatchToChannel();
				}
			} // end of For
	}// end of method

	/**
	 * getDispatcherTask will built transformationType for specified
	 * <code>EventDispatcher</code> and form the LeapEventDispatchTask for
	 * dispatching events to channel.
	 * 
	 * @param evtDispatchConfig
	 *            configuration to get transformation type and dispatchId.
	 * @param leapEvent
	 * @return instance of LeapEventDispatchTask.
	 */
	private LeapEventDispatchTask getDispatcherTask(EventDispatcher evtDispatchConfig, LeapEvent leapEvent) {
		String chanelId = evtDispatchConfig.getDispatchChanelId();
		String transformationType = evtDispatchConfig.getEventTransformation().getType();
		String tranformationBeanFQCN = null;
		String xslname = null;
		String xsltAsString = null;

		switch (transformationType) {
		case "CUSTOM":
			logger.debug("transformation type is CUSTOM");
			tranformationBeanFQCN = evtDispatchConfig.getEventTransformation().getCustomTransformer().getFqcn().trim();
			break;

		case "XML-XSLT":
			logger.debug("transformation type is XML-XSLT");
			xslname = evtDispatchConfig.getEventTransformation().getXSLTName().trim();
			xsltAsString = evtDispatchConfig.getEventTransformation().getXsltAsString().trim();

			break;

		case "JSON":
			logger.debug("transformation type is JSON");
			// setting as null because JSON is default transformer
			tranformationBeanFQCN = null;
			break;
		default:
			logger.debug("no matching transformation type found in the system setting JSON as Default");
			// JSON is deFAULT TRANSFORMER
			tranformationBeanFQCN = null;
			break;
		}// end of switch

		logger.debug(
				" LeapEventDispatchTask sending for execution for EventId=" + leapEvent.getEventId() + "--ChanelId="
						+ chanelId + ", transformation bean : " + tranformationBeanFQCN + ", xslname : " + xslname);

		return new LeapEventDispatchTask(leapEvent, chanelId, tranformationBeanFQCN, xslname, xsltAsString);
	}

	private ConfigurationContext getConfigurationContext(RequestContext rqCtx) {
		logger.debug("request context in EventFramework dispacher serice : " + rqCtx.getTenantId() + ", site : "
				+ rqCtx.getSiteId());
		ConfigurationContext configContext = new ConfigurationContext(rqCtx);
		logger.debug("config context before return : " + configContext);

		return configContext;
	}

}
