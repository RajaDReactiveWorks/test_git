
package com.getusroi.eventframework.config;

import java.util.List;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.getusroi.eventframework.jaxb.DispatchChanel;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.eventframework.jaxb.EventSubscription;
import com.getusroi.eventframework.jaxb.SystemEvent;
import com.getusroi.eventframework.jaxb.CamelEventProducer;

/**
 * EventFramework Configuration Service for handling of configurations related
 * to Event Framework
 */

public interface IEventFrameworkConfigService {
	public void addEventFrameworkConfiguration(ConfigurationContext configContext, Event evtConfig) throws EventFrameworkConfigurationException;

	public void addEventFrameworkConfiguration(ConfigurationContext configContext, SystemEvent sysEvtConfig) throws EventFrameworkConfigurationException;

	public void addEventFrameworkConfiguration(ConfigurationContext configContext, DispatchChanel disChanelConfig) throws EventFrameworkConfigurationException;
	public void addEventFrameworkConfiguration(ConfigurationContext configContext, EventSubscription eventSubscriptionConfig) throws EventFrameworkConfigurationException; 
	public Event getEventConfiguration(ConfigurationContext configContext, String forEventId) throws EventFrameworkConfigurationException;

	public DispatchChanel getDispatchChanelConfiguration(ConfigurationContext configContext, String dispatchChanelId)
			throws EventFrameworkConfigurationException;
	public EventSubscription getEventSubscriptionConfiguration(ConfigurationContext configContext, String eventSubscriptionId)
			throws EventFrameworkConfigurationException;
	public CamelEventProducer getEventProducerForBean(ConfigurationContext configContext, String serviceName, String beanFQCN)
			throws EventFrameworkConfigurationException;

	public List<CamelEventProducer> getEventProducerForServiceSuccessCompletion(ConfigurationContext configContext, String serviceName)
			throws EventFrameworkConfigurationException;

	public List<CamelEventProducer> getEventProducerForServiceFailedCompletion(ConfigurationContext configContext, String serviceName)
			throws EventFrameworkConfigurationException;

	public List<CamelEventProducer> getEventProducerForServiceFailedAndSucessCompletion(ConfigurationContext configContext, String serviceName)
			throws EventFrameworkConfigurationException;
	
	public List<Event> getEventConfigProducerForServiceSuccessCompletion(ConfigurationContext configContext, String serviceName)
			throws EventFrameworkConfigurationException ;
	public List<Event> getEventConfigProducerForServiceFailedCompletion(ConfigurationContext configContext, String serviceName)
			throws EventFrameworkConfigurationException ;
	public List<Event> getEventConfigProducerForServiceFailedAndSucessCompletion(ConfigurationContext configContext, String serviceName)
			throws EventFrameworkConfigurationException ;
	public AbstractCamelEventBuilder getServiceCompletionSuccessEventBuilder(ConfigurationContext configContext);

	public AbstractCamelEventBuilder getServiceCompletionFailureEventBuilder(ConfigurationContext configContext);
	public Event getEventConfigProducerForBean(ConfigurationContext configContext,String serviceName ,String beanFQCN)throws EventFrameworkConfigurationException ;
	
	public SystemEvent getSystemEventConfiguration(ConfigurationContext configContext, String systemEventId)throws EventFrameworkConfigurationException;
  public boolean changeStatusOfDispactherChanelConfiguration(ConfigurationContext configurationContext,String dispatchChanelId,boolean isEnable) throws  EventFrameworkConfigurationException;
  public boolean changeStatusOfEventSubscriptionConfiguration(ConfigurationContext configurationContext,String eventSubscriptionId,boolean isEnable) throws  EventFrameworkConfigurationException;
  public boolean changeStatusOfEventSubscriber(ConfigurationContext configurationContext,String eventSubscriptionId,String subscriberId,boolean isEnable) throws  EventFrameworkConfigurationException;
  public boolean changeStatusOfSystemEventConfiguration(ConfigurationContext configurationContext,String systemEventId,boolean isEnable) throws  EventFrameworkConfigurationException; 
	
	public boolean changeStatusOfEventConfiguration(ConfigurationContext configurationContext,String eventId,boolean isEnable)throws  EventFrameworkConfigurationException; 
	public boolean deleteDipatcherChanelConfiguration(ConfigurationContext configContext,String dispatchChanelId) throws EventFrameworkConfigurationException;
	public boolean deleteSystemEventConfiguration(ConfigurationContext configContext,String systemEventId) throws EventFrameworkConfigurationException;
	public boolean deleteEventSubscriptionConfiguration(ConfigurationContext configContext, String subscriptionEventId)throws EventFrameworkConfigurationException;
	public boolean deleteEventConfiguration(ConfigurationContext configContext,String eventId) throws EventFrameworkConfigurationException;

	boolean reloadSystemEventCacheObject(RequestContext requestContext, String systemEventId)
			throws EventFrameworkConfigurationException;

	boolean reloadEventCacheObject(RequestContext requestContext, String eventId)
			throws EventFrameworkConfigurationException;

	boolean reloadDispatchChanelCacheObject(RequestContext requestContext, String dispatchChanelId)
			throws EventFrameworkConfigurationException;


}
