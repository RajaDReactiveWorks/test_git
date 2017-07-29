package com.getusroi.eventframework.dispatcher.chanel;

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
import com.getusroi.eventframework.event.IROIEventService;
import com.getusroi.eventframework.event.ROIEventService;
import com.getusroi.eventframework.jaxb.DispatchChanel;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.eventframework.jaxb.EventFramework;
import com.getusroi.eventframework.jaxb.EventSubscription;
import com.getusroi.eventframework.jaxb.Subscriber;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class DispatchChanelsTest {
	String jsonConfigStr="{\"queueName\":\"testQueue\"}";
	final static Logger logger = LoggerFactory.getLogger(DispatchChanelsTest.class);

	
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
	public void testHazelcastQueueDispatchChanel() throws DispatchChanelInitializationException, InterruptedException, MessageDispatchingException, EventFrameworkConfigurationException{
		ConfigurationContext configContext = getConfigurationContext(4);
		List<Event> eventList=eventFrameworkConfig.getEvents().getEvent();
		List<EventSubscription> evenSubscriptiontList=eventFrameworkConfig.getEventSubscriptions().getEventSubscription();
		List<DispatchChanel> disChanelList = eventFrameworkConfig.getDispatchChanels().getDispatchChanel();
		
		EventSubscription eventSubsConfig=null;
		for (EventSubscription eventSubs : evenSubscriptiontList) {
			if (eventSubs.getEventId().equalsIgnoreCase("PRINT_SERVICE")) {
				eventSubsConfig = eventSubs;
				break;
			}
		}	
		Event eventConfig=null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE")) {
				eventConfig = event;
				break;
			}
		}		
		DispatchChanel disChanelConfig = null;
		for (DispatchChanel disChanel : disChanelList) {
			if (disChanel.getId().equalsIgnoreCase("TEST_CHANEL")) {
				disChanelConfig = disChanel;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, disChanelConfig);
		eventConfigService.addEventFrameworkConfiguration(configContext, eventConfig);
		eventConfigService.addEventFrameworkConfiguration(configContext, eventSubsConfig);

		HazelcastQueueDispatchChanel hcDisChanel=new HazelcastQueueDispatchChanel(jsonConfigStr);
		RequestContext requestContext=getRequestContext();
		hcDisChanel.dispatchMsg("Test Msg",requestContext,"PRINT_SERVICE");
		
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
				Assert.assertEquals("HazelcastQueueChanel failed to dispatch the test MSG", "Test Msg", str);
			}//end of if(subscribe.isEnabled())
		}
		
		
	}//end of method
	
	
	private ConfigurationContext getConfigurationContext(int level) {
		ConfigurationContext configContext = null;
		if (level == 4)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
					GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME);
		if (level == 2)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite());
		return configContext;

	}
	
	private RequestContext getRequestContext() {
		RequestContext reqContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME);
		
		return reqContext;

	}
}
