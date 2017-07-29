package com.getusroi.eventframework.event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.getusroi.config.util.GenericTestConstant;
import com.getusroi.core.datagrid.DataGridService;
import com.getusroi.eventframework.config.EventFrameworkConfigParserException;
import com.getusroi.eventframework.config.EventFrameworkConfigurationException;
import com.getusroi.eventframework.config.EventFrameworkTestConstant;
import com.getusroi.eventframework.config.EventFrameworkXmlHandler;
import com.getusroi.eventframework.config.IEventFrameworkConfigService;
import com.getusroi.eventframework.config.impl.EventFrameworkConfigService;
import com.getusroi.eventframework.dispatcher.EventFrameworkDispatcherException;
import com.getusroi.eventframework.jaxb.DispatchChanel;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.eventframework.jaxb.EventFramework;
import com.getusroi.eventframework.jaxb.EventSubscription;
import com.getusroi.eventframework.jaxb.Subscriber;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class ROIEventServiceTest {
	final Logger logger = LoggerFactory.getLogger(ROIEventServiceTest.class);

	private static EventFramework eventFrameworkConfig;
	private IEventFrameworkConfigService eventConfigService = new EventFrameworkConfigService();
	private IROIEventService eventService = new ROIEventService();

	private EventFramework getEventFrameworkConfiguration() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(EventFrameworkTestConstant.configfileToParse);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		String evtFrmeworkXmlStr = out1.toString();
		EventFramework evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);

		return evtFramework;
	}

	@Before
	public void loadConfigurations() throws EventFrameworkConfigParserException, ConfigPersistenceException {
		if (eventFrameworkConfig == null) {
			eventFrameworkConfig = getEventFrameworkConfiguration();
		}
		// Clear all DB Data First for SITE=23 FG=25,FEATURE-26
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getConfigNodeId());
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getSiteConfigNodeId());
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getFeatureGroupConfigNodeId());
	}
	
	@Test
	public void testDispatchEvent() throws EventFrameworkConfigurationException, InvalidEventException, EventFrameworkDispatcherException, InterruptedException{
		ConfigurationContext configContext = getConfigurationContext(2);
		// Add PRINTSERVICE Event Config
		configContext = getConfigurationContext(2);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEvt = event;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);

		//Add Chanel
		
		List<EventSubscription> evenSubscriptiontList=eventFrameworkConfig.getEventSubscriptions().getEventSubscription();
		EventSubscription eventSubsConfig=null;
		for (EventSubscription eventSubs : evenSubscriptiontList) {
			if (eventSubs.getEventId().equalsIgnoreCase("PRINT_SERVICE")) {
				eventSubsConfig = eventSubs;
				break;
			}
		}	
		List<DispatchChanel> disChanelList = eventFrameworkConfig.getDispatchChanels().getDispatchChanel();
		DispatchChanel disChanelConfig = null;
		for (DispatchChanel disChanel : disChanelList) {
			if (disChanel.getId().equalsIgnoreCase("TEST_CHANEL")) {
				disChanelConfig = disChanel;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, eventSubsConfig);

		eventConfigService.addEventFrameworkConfiguration(configContext, disChanelConfig);
		
		//Chanel and Event are added in config so Build an Event
		ROIEvent printEvent=new ROIEvent("PRINT_SERVICE",getRequestContext());
		//publish The Event
		eventService.publishEvent(printEvent);
		logger.debug(".testDispatchEvent() Waiting to Receive the Event on TestChanel");
		Thread.sleep(3000);
		logger.debug(".testDispatchEvent() Woken from Sleep to get msg from Chanel TestChanel");
		//Check the Hazelcast Queue if msg is received or not
		
		IEventFrameworkConfigService evtFwkConfiService=new EventFrameworkConfigService();
		EventSubscription eventSubscription=evtFwkConfiService.getEventSubscriptionConfiguration(configContext,"PRINT_SERVICE");
		List<Subscriber> subscriberList=eventSubscription.getSubscriber();
		HazelcastInstance hazelcastInstance=DataGridService.getDataGridInstance().getHazelcastInstance();

		for(Subscriber subscribe:subscriberList){
			if(subscribe.isEnabled()){
				logger.debug("key : "+eventSubscription.getEventId().trim()+"-"+subscribe.getId().trim());
				IQueue<Object> queue = hazelcastInstance.getQueue(eventSubscription.getEventId().trim()+"-"+subscribe.getId().trim());
				logger.debug(".dispatchMsg() Got Queue from Hazelcast Instance");
				String str=(String)queue.take();
				logger.debug("String : "+str);
				Assert.assertNotNull("HazelcastQueueChanel failed to dispatch the test MSG",str);
			}//end of if(subscribe.isEnabled())
		}
		
	}
	
	
	private ConfigurationContext getConfigurationContext(int level) {
		ConfigurationContext configContext = null;
		if (level == 4)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), GenericTestConstant.getFeatureGroup(),
					GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME);
		if (level == 2)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite());
		return configContext;

	}
	private RequestContext getRequestContext(){
		RequestContext reqContext=new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), GenericTestConstant.getFeatureGroup(),
					GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME);
		return reqContext;
	}
}
