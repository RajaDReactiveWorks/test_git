package com.attunedlabs.eventframework.dispatcher.chanel;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.event.LeapBaseConfigEvent;
import com.attunedlabs.config.event.LeapConfigurationListener;
import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.core.BeanDependencyResolverFactory;
import com.attunedlabs.core.IBeanDependencyResolver;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.dispatchchannel.exception.NonRetryableMessageDispatchingException;
import com.attunedlabs.eventframework.jaxb.DispatchChanel;

public class DispatchChanelService extends LeapConfigurationListener implements IDispatchChanelService {
	private final static Logger logger = LoggerFactory.getLogger(DispatchChanelService.class);

	private static DispatchChanelService dispatchService;
	EventFrameworkConfigService eventFwkConfigService;
	private HashMap<String, Map<String, AbstractDispatchChanel>> chanelMap;

	/**
	 * Singleton to ensure only one DispatchChanelService Exist
	 * 
	 * @return
	 */
	public static synchronized DispatchChanelService getDispatchChanelService() {
		if (dispatchService == null) {
			dispatchService = new DispatchChanelService();
		}
		return dispatchService;
	}

	/**
	 * Private Constructor for Singleton
	 */
	private DispatchChanelService() {
		eventFwkConfigService = new EventFrameworkConfigService();
		chanelMap = new HashMap();
	}

	/**
	 * This method is to get dispatcher channel
	 * 
	 * @param tenantId
	 *            :String
	 * @param chanelId
	 *            : String
	 * @return AbstractDispatchChanel
	 * @throws NonRetryableMessageDispatchingException 
	 */
	public AbstractDispatchChanel getDispatchChanel(RequestContext reqContext, String chanelId) throws NonRetryableMessageDispatchingException {
		String tenantId = reqContext.getTenantId();
		String siteId = reqContext.getSiteId();

		Map tenantChanelMap = chanelMap.get(tenantId + "-" + siteId);
		AbstractDispatchChanel dispatchChanel = null;
		if (tenantChanelMap != null)
			dispatchChanel = (AbstractDispatchChanel) tenantChanelMap.get(chanelId);
		// found in the local Map
		if (dispatchChanel != null) {
			logger.debug("channel already : " + dispatchChanel + " instance of service " + chanelMap);
			return dispatchChanel;
		}
		logger.debug("Chanel not found in local initialized Map. Getting it and initializing it ChanelId=" + chanelId);
		// Not found in the Map lookup the configuration and initialize the
		// Chanel
		ConfigurationContext configCtx = getConfigurationContext(reqContext);
		DispatchChanel disChanel;
		try {
			disChanel = eventFwkConfigService.getDispatchChanelConfiguration(configCtx, chanelId);
			dispatchChanel = getChanelInstance(disChanel.getChanelImplementation().getFqcn(),
					disChanel.getChanelConfiguration());
			addDispatchChanelToLocalMap(dispatchChanel, chanelId, configCtx);
			logger.debug(".getDispatchChanel() Chanel {" + chanelId + "} initialized and added to local Map");
			return dispatchChanel;
		} catch (EventFrameworkConfigurationException | DispatchChanelInstantiationException
				| DispatchChanelInitializationException e) {
			logger.error("Failure In getting DispatchChanel {" + chanelId + "}", e);
			throw new NonRetryableMessageDispatchingException("Failure In getting DispatchChanel {" + chanelId + "}",
					e);
		}

	}

	private void addDispatchChanelToLocalMap(AbstractDispatchChanel chanel, String chanelId,
			ConfigurationContext reqCtx) {
		Map<String, AbstractDispatchChanel> tenantChanelMap = chanelMap
				.get(reqCtx.getTenantId() + "-" + reqCtx.getSiteId());
		if (tenantChanelMap == null) {
			tenantChanelMap = new HashMap();
		}
		tenantChanelMap.put(chanelId, chanel);
		chanelMap.put(reqCtx.getTenantId() + "-" + reqCtx.getSiteId(), tenantChanelMap);
	}

	/**
	 * This method is to get the instance of chanel
	 * 
	 * @param fqcn
	 *            : String
	 * @param jsonConfig
	 *            : String
	 * @return AbstractDispatchChanel
	 * @throws DispatchChanelInstantiationException
	 * @throws DispatchChanelInitializationException
	 */
	private AbstractDispatchChanel getChanelInstance(String fqcn, String jsonConfig)
			throws DispatchChanelInstantiationException, DispatchChanelInitializationException {
		logger.debug("inside getChanelInstance : " + fqcn);
		AbstractDispatchChanel chanel = null;
		IBeanDependencyResolver beanResolver = BeanDependencyResolverFactory.getBeanDependencyResolver();
		try {

			chanel = (AbstractDispatchChanel) beanResolver.getBeanInstance(AbstractDispatchChanel.class, fqcn);
			chanel.setChaneljsonconfig(jsonConfig);
		} catch (IllegalArgumentException | BeanDependencyResolveException exp) {
			logger.error("Failed to initialize DispatchChanel {" + fqcn + "}", exp);
			throw new DispatchChanelInstantiationException("Failed to initialize DispatchChanel {" + fqcn + "}", exp);
		}
		return chanel;

	}

	public void configAdded(LeapBaseConfigEvent configAddedEvent) {
		// DO Nothing as we are not interested in ConfigAdded Event
	}

	public void configUpdated(LeapBaseConfigEvent configUpdatedEvent) {
		// #TODO Proper Coding is Required
		// logger.debug("configUpdated- configUpdatedEvent--in dispacher chanel
		// service" + configUpdatedEvent);
		// String tenantid = configUpdatedEvent.getTenantId();
		// String configGroup = configUpdatedEvent.getConfigGroup();
		// String key = configUpdatedEvent.getConfigName();
		// logger.info("tenatid : " + tenantid + " configGroup : " + configGroup
		// + " key : " + key);
		// // value in cache server
		// ConfigurationUnit updateconfigunit =
		// configUpdatedEvent.getNewConfigUnit();
		// boolean updatedIsEnabled = updateconfigunit.getIsEnabled();
		// local map value
		// ConfigurationUnit localmapvalue=configunitmap.get(key);
		// #TODO DO it Properly

	}

	public void configRemoved(LeapBaseConfigEvent configRemovedEvent) {
		// #TODO Proper Coding is required
		// logger.debug("configRemoved- configRemovedEvent--in dispacher chanel
		// service"+configRemovedEvent);
		// String tenantid=configRemovedEvent.getTenantId();
		//
		// String configGroup=configRemovedEvent.getConfigGroup();
		// String key=configRemovedEvent.getConfigName();
		//
		// logger.info("tenatid : "+tenantid+" configGroup : "+configGroup+" key
		// : "+key);
		//
		// configunitmap.remove(key);
	}

	public void configStatusChanged(LeapBaseConfigEvent configStatusChangedEvent) {
		// #TODO Proper coding is required
		// logger.debug("configStatusChanged- configStatusChangedEvent--in
		// dispacher chanel service" + configStatusChangedEvent);
		// String tenantid = configStatusChangedEvent.getTenantId();
		// String configGroup = configStatusChangedEvent.getConfigGroup();
		// String key = configStatusChangedEvent.getConfigName();
		// ConfigurationUnit statusChangeconfigunit =
		// configStatusChangedEvent.getNewConfigUnit();
		// logger.info("tenatid : " + tenantid + " configGroup : " + configGroup
		// + " key : " + key);
		// configunitmap.put(key, statusChangeconfigunit);
		// logger.debug("------------logic to see if status value updated :
		// ---------");
		// ConfigurationUnit getmaptest = configunitmap.get(key);
		// logger.info("config unit is enabled : " + getmaptest.getIsEnabled());
	}

	private ConfigurationContext getConfigurationContext(RequestContext rqCtx) {
		ConfigurationContext configContext = new ConfigurationContext(rqCtx.getTenantId(), rqCtx.getSiteId(),
				rqCtx.getFeatureGroup(), rqCtx.getFeatureName(), rqCtx.getImplementationName(),rqCtx.getVendor(),rqCtx.getVersion());
		return configContext;
	}

}
