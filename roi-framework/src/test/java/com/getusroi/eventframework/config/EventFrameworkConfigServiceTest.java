package com.getusroi.eventframework.config;

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
import com.getusroi.config.persistence.ConfigNodeData;
import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.InvalidNodeTreeException;
import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.getusroi.config.server.ConfigServerInitializationException;
import com.getusroi.config.server.ROIConfigurationServer;
import com.getusroi.config.util.GenericTestConstant;
import com.getusroi.core.datagrid.DataGridService;
import com.getusroi.eventframework.config.impl.EventFrameworkConfigHelper;
import com.getusroi.eventframework.config.impl.EventFrameworkConfigService;
import com.getusroi.eventframework.jaxb.CamelEventProducer;
import com.getusroi.eventframework.jaxb.CamelProducerConfig;
import com.getusroi.eventframework.jaxb.DispatchChanel;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.eventframework.jaxb.EventFramework;
import com.getusroi.eventframework.jaxb.EventSubscription;
import com.getusroi.eventframework.jaxb.Subscriber;
import com.getusroi.eventframework.jaxb.SystemEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class EventFrameworkConfigServiceTest {
	final Logger logger = LoggerFactory.getLogger(EventFrameworkConfigServiceTest.class);

	private static EventFramework eventFrameworkConfig;
	private IEventFrameworkConfigService eventConfigService = new EventFrameworkConfigService();

	private EventFramework getEventFrameworkConfiguration() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.configfileToParse);
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
		// clear hazelcast as well for Event
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap producerMap = hazelcastInstance.getMap(EventFrameworkConfigurationUnit
				.getEventProcucerForServiceGroupKey(GenericTestConstant.getConfigNodeId()));// 26
		producerMap.clear();
	}

	@Test
	public void testAddConfigurationEventWithServiceProducer() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEvt = event;
				break;
			}
		}
		Assert.assertNotNull("Issue with input provided to begin the testcase, Test Event loded from config is null",
				printSerEvt);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEvt.getId(),
				EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertEquals("Event{PRINTSERVICE} should be inserted in datbade at nodeId=26", "PRINT_SERVICE",
				loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(printSerEvt.getId());
		Event dgEvent = (Event) dgEvtConfigUnit.getConfigData();
		logger.debug(".testAddConfigurationEvent() DataGrid eventId is " + dgEvent.getId());
		Assert.assertEquals("Event{PRINTSERVICE} should be available in DG", "PRINT_SERVICE", dgEvent.getId());
		// Test that Event producer are added or not in DataGrid
		IMap producerMap = hazelcastInstance.getMap(EventFrameworkConfigurationUnit
				.getEventProcucerForServiceGroupKey(GenericTestConstant.TEST_SITE_NODEID));
		CamelProducerConfig prodConfig = printSerEvt.getCamelEventProducer().getCamelProducerConfig();
		/*String EventIdfromProducerMap = (String) producerMap
				.get(prodConfig.getServiceName() + "-" + prodConfig.getRaiseOn());
		Assert.assertEquals("Event{PRINTSERVICE} should be available in ProducerConfigService Map with ",
				"PRINT_SERVICE", EventIdfromProducerMap);*/
	}

	@Test
	public void testAddAndGetEventSubscriptionConfiguration() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		List<EventSubscription> eventSubscriptionList = eventFrameworkConfig.getEventSubscriptions()
				.getEventSubscription();
		EventSubscription printSerEventSubs = null;
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEvt = event;
				break;
			}
		}
		for (EventSubscription eventSubs : eventSubscriptionList) {
			if (eventSubs.getEventId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEventSubs = eventSubs;
				break;
			}
		}
		Assert.assertNotNull("Issue with input provided to begin the testcase, Test Event loded from config is null",
				printSerEvt);
		Assert.assertNotNull(
				"Issue with input provided to begin the testcase, Test EventSubscription loded from config is null",
				printSerEventSubs);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEventSubs);
		EventSubscription eventsubs = eventConfigService.getEventSubscriptionConfiguration(configContext,
				printSerEventSubs.getEventId());
		Assert.assertNotNull("EventSubscription is not null from event service getEvent Subscription : " + eventsubs);
		Assert.assertEquals("PRINT_SERVICE", eventsubs.getEventId());
		// Test starts check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEventSubs.getEventId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertEquals("Event{PRINTSERVICE} should be inserted in datbade at nodeId=26", "PRINT_SERVICE",
				loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-SUBSCRIPTION
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventSubscriptionConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		EventFrameworkConfigurationUnit dgEvtSubscriptionConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(printSerEventSubs.getEventId());
		EventSubscription dgEventSubscription = (EventSubscription) dgEvtSubscriptionConfigUnit.getConfigData();
		logger.debug(".testAddConfigurationEvent() DataGrid eventId is " + dgEventSubscription.getEventId());
		Assert.assertEquals("Event{PRINTSERVICE} should be available in DG", "PRINT_SERVICE",
				dgEventSubscription.getEventId());

	}

	@Test
	public void testChangeStatusOfConfigurationEventSubscription() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		List<EventSubscription> eventSubscriptionList = eventFrameworkConfig.getEventSubscriptions()
				.getEventSubscription();
		EventSubscription printSerEventSubs = null;
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEvt = event;
				break;
			}
		}
		for (EventSubscription eventSubs : eventSubscriptionList) {
			if (eventSubs.getEventId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEventSubs = eventSubs;
				break;
			}
		}
		Assert.assertNotNull("Issue with input provided to begin the testcase, Test Event loded from config is null",
				printSerEvt);
		Assert.assertNotNull(
				"Issue with input provided to begin the testcase, Test EventSubscription loded from config is null",
				printSerEventSubs);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEventSubs);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEventSubs.getEventId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertEquals("EventSubscription{PRINT_SERVICE} should be inserted in datbade at nodeId=26",
				"PRINT_SERVICE", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(printSerEventSubs.getEventId());

		Assert.assertNotNull("Event Configuration in Data Grid should be not Null ", dgEvtConfigUnit);

		// enable eventConfiguration

		boolean isEnable = eventConfigService.changeStatusOfEventSubscriptionConfiguration(configContext,
				printSerEventSubs.getEventId(), false);

		Assert.assertTrue("eventConfiguration should be enabled =False ", isEnable);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEventSubs.getEventId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertFalse("The eventConfiguration status in DB should be False", loadedConfigNodeData.isEnabled());

		hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventSubscriptionConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(printSerEventSubs.getEventId());

		Assert.assertNull(dgEvtConfigUnit);

	}

	@Test
	public void testChangeStatusOfEventSubscriber() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		List<EventSubscription> eventSubscriptionList = eventFrameworkConfig.getEventSubscriptions()
				.getEventSubscription();
		EventSubscription printSerEventSubs = null;
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEvt = event;
				break;
			}
		}
		for (EventSubscription eventSubs : eventSubscriptionList) {
			if (eventSubs.getEventId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEventSubs = eventSubs;
				break;
			}
		}
		Assert.assertNotNull("Issue with input provided to begin the testcase, Test Event loded from config is null",
				printSerEvt);
		Assert.assertNotNull(
				"Issue with input provided to begin the testcase, Test EventSubscription loded from config is null",
				printSerEventSubs);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEventSubs);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEventSubs.getEventId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertEquals("EventSubscription{PRINT_SERVICE} should be inserted in datbade at nodeId=26",
				"PRINT_SERVICE", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(printSerEventSubs.getEventId());

		Assert.assertNotNull("Event Subscription Configuration in Data Grid should be Null ", dgEvtConfigUnit);

		// enable eventConfiguration

		boolean isEnable = eventConfigService.changeStatusOfEventSubscriber(configContext,
				printSerEventSubs.getEventId(), "xyz", false);

		Assert.assertTrue("eventSubscriptionConfiguration should be enabled =true ", isEnable);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEventSubs.getEventId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertFalse("The eventSubscriptionConfiguration status in DB should be true",
				loadedConfigNodeData.isEnabled());

		hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventSubscriptionConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(printSerEventSubs.getEventId());

		EventSubscription dgEventSubscription = (EventSubscription) dgEvtConfigUnit.getConfigData();
		List<Subscriber> subscriberList = dgEventSubscription.getSubscriber();
		for (Subscriber subscriber : subscriberList) {
			if (subscriber.getId().equalsIgnoreCase("xyz")) {
				Assert.assertFalse("The eventSubscription subscriber status in DB should be false",
						loadedConfigNodeData.isEnabled());
			}
		}

	}

	@Test
	public void testAddConfigurationEventWithBeanProducer() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_BEANPRODUCER")) {
				printSerEvt = event;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEvt.getId(),
				EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertEquals("Event{PRINT_SERVICE_BEANPRODUCER} should be inserted in datbade at nodeId=26",
				"PRINT_SERVICE_BEANPRODUCER", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(printSerEvt.getId());
		Event dgEvent = (Event) dgEvtConfigUnit.getConfigData();
		logger.debug(".testAddConfigurationEvent() DataGrid eventId is " + dgEvent.getId());
		Assert.assertEquals("Event{PRINT_SERVICE_BEANPRODUCER} should be available in DG", "PRINT_SERVICE_BEANPRODUCER",
				dgEvent.getId());
		// Test that Event producer are added or not in DataGrid
		IMap producerMap = hazelcastInstance.getMap(
				EventFrameworkConfigurationUnit.getEventProcucerForBeanGroupKey(GenericTestConstant.TEST_SITE_NODEID));
		CamelProducerConfig prodConfig = printSerEvt.getCamelEventProducer().getCamelProducerConfig();
		// producerConfig.getComponent()+"-"+serviceName
		String EventIdfromProducerMap = (String) producerMap
				.get(prodConfig.getComponent() + "-" + prodConfig.getServiceName());
		Assert.assertEquals("Event{PRINT_SERVICE_BEANPRODUCER} should be available in BeanProducerConfig Map with ",
				"PRINT_SERVICE_BEANPRODUCER", EventIdfromProducerMap);
	}

	@Test
	public void testDeleteConfigurationEventWithBeanProducer() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_BEANPRODUCER")) {
				printSerEvt = event;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEvt.getId(),
				EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertEquals("Event{PRINT_SERVICE_BEANPRODUCER} should be inserted in datbade at nodeId=26",
				"PRINT_SERVICE_BEANPRODUCER", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(printSerEvt.getId());
		Event dgEvent = (Event) dgEvtConfigUnit.getConfigData();
		logger.debug(".testAddConfigurationEvent() DataGrid eventId is " + dgEvent.getId());
		Assert.assertEquals("Event{PRINT_SERVICE_BEANPRODUCER} should be available in DG", "PRINT_SERVICE_BEANPRODUCER",
				dgEvent.getId());
		// Test that Event producer are added or not in DataGrid
		IMap producerMap = hazelcastInstance.getMap(
				EventFrameworkConfigurationUnit.getEventProcucerForBeanGroupKey(GenericTestConstant.TEST_SITE_NODEID));
		CamelProducerConfig prodConfig = printSerEvt.getCamelEventProducer().getCamelProducerConfig();
		// producerConfig.getComponent()+"-"+serviceName
		String EventIdfromProducerMap = (String) producerMap
				.get(prodConfig.getComponent() + "-" + prodConfig.getServiceName());
		Assert.assertEquals("Event{PRINT_SERVICE_BEANPRODUCER} should be available in BeanProducerConfig Map with ",
				"PRINT_SERVICE_BEANPRODUCER", EventIdfromProducerMap);

		boolean isDeleted = eventConfigService.deleteEventConfiguration(configContext, dgEvent.getId());
		Assert.assertTrue("Eventconfiguration should be deleted = true ", isDeleted);
		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEvt.getId(),
				EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertNull("the EventConfiguration in DB should be Null ", loadedConfigNodeData);

		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(printSerEvt.getId());

		Assert.assertNull("the eventConfiguration in Data Grid Should be null ", dgEvtConfigUnit);

		producerMap = hazelcastInstance.getMap(
				EventFrameworkConfigurationUnit.getEventProcucerForBeanGroupKey(GenericTestConstant.TEST_SITE_NODEID));
		prodConfig = printSerEvt.getCamelEventProducer().getCamelProducerConfig();
		// producerConfig.getComponent()+"-"+serviceName
		EventIdfromProducerMap = (String) producerMap
				.get(prodConfig.getComponent() + "-" + prodConfig.getServiceName());
		Assert.assertNull("the eventId in eventProducerservice data Grid Should be not Exist ", EventIdfromProducerMap);
	}

	@Test
	public void testGetEventProducerForServiceSuccessCompletion() throws EventFrameworkConfigurationException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Event printSerEvt = null;
		Event printSerEvt2 = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEvt = event;
			}
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE-CEPSUCESS")) {
				printSerEvt2 = event;
			}
		}
		// Add two event for same Add-Label service with raiseOn=sucess
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt2);
		List<CamelEventProducer> evtProducerList = eventConfigService
				.getEventProducerForServiceSuccessCompletion(configContext, "AddLabel");
		Assert.assertNotNull("EventProducer must be returned for PRINT_SERVICE and PRINT_SERVICE-CEPSUCESS Event",
				evtProducerList);
		if (evtProducerList == null)
			return;
		Assert.assertEquals("There should be three Events returned", 3, evtProducerList.size());

	}

	@Test
	public void testAddConfigurationSystemEvent() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<SystemEvent> eventList = eventFrameworkConfig.getSystemEvents().getSystemEvent();
		SystemEvent serComFailureEvt = null;
		for (SystemEvent event : eventList) {
			if (event.getId().equalsIgnoreCase("SERVICE_COMPLETION_FAILURE")) {
				serComFailureEvt = event;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, serComFailureEvt);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), serComFailureEvt.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
		Assert.assertEquals(
				"System-Event{SERVICE_COMPLETION_FAILURE} should be inserted in datbade at nodeId=23 {site1}",
				"SERVICE_COMPLETION_FAILURE", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getSystemEventConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(serComFailureEvt.getId());
		SystemEvent dgSysEvent = (SystemEvent) dgEvtConfigUnit.getConfigData();
		logger.debug(".testAddConfigurationEvent() DataGrid eventId is " + dgSysEvent.getId());
		Assert.assertEquals("Event{SERVICE_COMPLETION_FAILURE} should be available in DG", "SERVICE_COMPLETION_FAILURE",
				dgSysEvent.getId());
	}

	@Test
	public void testChangeStatusOfConfigurationEventWithServiceProducerToEnable()
			throws EventFrameworkConfigParserException, EventFrameworkConfigurationException,
			ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("POLINEITEM_PROCESSED")) {
				printSerEvt = event;
				break;
			}
		}
		Assert.assertNotNull("Issue with input provided to begin the testcase, Test Event loded from config is null",
				printSerEvt);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEvt.getId(),
				EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertEquals("Event{POLINEITEM_PROCESSED} should be inserted in datbade at nodeId=26",
				"POLINEITEM_PROCESSED", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(printSerEvt.getId());

		Assert.assertNull("Event Configuration in Data Grid should be Null ", dgEvtConfigUnit);
		// Test that Event producer are added or not in DataGrid
		IMap producerMap = hazelcastInstance.getMap(EventFrameworkConfigurationUnit
				.getEventProcucerForServiceGroupKey(GenericTestConstant.TEST_SITE_NODEID));
		CamelProducerConfig prodConfig = printSerEvt.getCamelEventProducer().getCamelProducerConfig();
		String EventIdfromProducerMap = (String) producerMap
				.get(prodConfig.getServiceName() + "-" + prodConfig.getRaiseOn());
		Assert.assertNull("Event Configuration eventId in ProducerMap Data Grid should Be  ", null);

		// enable eventConfiguration

		boolean isEnable = eventConfigService.changeStatusOfEventConfiguration(configContext, printSerEvt.getId(),
				true);

		Assert.assertTrue("eventConfiguration should be enabled =true ", isEnable);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEvt.getId(),
				EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertTrue("The eventConfiguration status in DB should be true", loadedConfigNodeData.isEnabled());

		hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(printSerEvt.getId());
		Event dgEvent = (Event) dgEvtConfigUnit.getConfigData();

		Assert.assertEquals("Event{POLINEITEM_PROCESSED} should be available in DG", "POLINEITEM_PROCESSED",
				dgEvent.getId());
		// Test that Event producer are added or not in DataGrid
		producerMap = hazelcastInstance.getMap(EventFrameworkConfigurationUnit
				.getEventProcucerForServiceGroupKey(GenericTestConstant.TEST_SITE_NODEID));
		prodConfig = printSerEvt.getCamelEventProducer().getCamelProducerConfig();
		EventIdfromProducerMap = (String) producerMap.get(prodConfig.getServiceName() + "-" + prodConfig.getRaiseOn());
		logger.debug("the EventIdfromProducerMap is = " + EventIdfromProducerMap);
		Assert.assertEquals("Event{POLINEITEM_PROCESSED} should be available in ProducerConfigService Map with ",
				"POLINEITEM_PROCESSED", EventIdfromProducerMap);

	}

	@Test
	public void testChangeStatusConfigurationEventWithBeanProducerToEnable() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("INVENTORY_UPDATED")) {
				printSerEvt = event;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEvt.getId(),
				EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertEquals("Event{INVENTORY_UPDATED} should be inserted in datbade at nodeId=26", "INVENTORY_UPDATED",
				loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(printSerEvt.getId());

		Assert.assertNull("EventFrameworkConfigurationUnit should be null  ", dgEvtConfigUnit);

		// Test that Event producer are added or not in DataGrid
		IMap producerMap = hazelcastInstance.getMap(
				EventFrameworkConfigurationUnit.getEventProcucerForBeanGroupKey(GenericTestConstant.TEST_SITE_NODEID));
		CamelProducerConfig prodConfig = printSerEvt.getCamelEventProducer().getCamelProducerConfig();
		// producerConfig.getComponent()+"-"+serviceName
		String EventIdfromProducerMap = (String) producerMap
				.get(prodConfig.getComponent() + "-" + prodConfig.getServiceName());
		Assert.assertNull("Event{INVENTORY_UPDATED} should not be available in BeanProducerConfig Map  ",
				EventIdfromProducerMap);

		boolean isEnabled = eventConfigService.changeStatusOfEventConfiguration(configContext, printSerEvt.getId(),
				true);

		Assert.assertTrue("eventFrameworkConfiguration should be enabled==ture", isEnabled);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEvt.getId(),
				EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);

		Assert.assertTrue("eventFrameworkConfiguration in the database should enabled",
				loadedConfigNodeData.isEnabled());

		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(printSerEvt.getId());
		Event dgEvent = (Event) dgEvtConfigUnit.getConfigData();

		Assert.assertEquals("Event{INVENTORY_UPDATED} should be available in DG", "INVENTORY_UPDATED", dgEvent.getId());
		// Test that Event producer are added or not in DataGrid
		producerMap = hazelcastInstance.getMap(
				EventFrameworkConfigurationUnit.getEventProcucerForBeanGroupKey(GenericTestConstant.TEST_SITE_NODEID));
		prodConfig = printSerEvt.getCamelEventProducer().getCamelProducerConfig();
		// producerConfig.getComponent()+"-"+serviceName
		EventIdfromProducerMap = (String) producerMap
				.get(prodConfig.getComponent() + "-" + prodConfig.getServiceName());
		Assert.assertEquals("Event{INVENTORY_UPDATED} should be available in BeanProducerConfig Map with ",
				"INVENTORY_UPDATED", EventIdfromProducerMap);
	}

	@Test
	public void testChangeStatusConfigurationEventWithServiceProducerToDisable()
			throws EventFrameworkConfigParserException, EventFrameworkConfigurationException,
			ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEvt = event;
				break;
			}
		}
		Assert.assertNotNull("Issue with input provided to begin the testcase, Test Event loded from config is null",
				printSerEvt);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEvt.getId(),
				EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertEquals("Event{PRINTSERVICE} should be inserted in datbade at nodeId=26", "PRINT_SERVICE",
				loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(printSerEvt.getId());
		Event dgEvent = (Event) dgEvtConfigUnit.getConfigData();
		logger.debug(".testAddConfigurationEvent() DataGrid eventId is " + dgEvent.getId());
		Assert.assertEquals("Event{PRINTSERVICE} should be available in DG", "PRINT_SERVICE", dgEvent.getId());
		// Test that Event producer are added or not in DataGrid
		IMap producerMap = hazelcastInstance.getMap(EventFrameworkConfigurationUnit
				.getEventProcucerForServiceGroupKey(GenericTestConstant.TEST_SITE_NODEID));
		CamelProducerConfig prodConfig = printSerEvt.getCamelEventProducer().getCamelProducerConfig();
		String eventIdfromProducerMap = (String) producerMap
				.get(prodConfig.getServiceName() + "-" + prodConfig.getRaiseOn());
		/*Assert.assertEquals("Event{PRINTSERVICE} should be available in ProducerConfigService Map with ",
				"PRINT_SERVICE", eventIdfromProducerMap);*/

		// to disable the event configuration
		boolean isDisabled = eventConfigService.changeStatusOfEventConfiguration(configContext, printSerEvt.getId(),
				false);
		Assert.assertTrue("eventConfiguration is Disabled=true", isDisabled);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEvt.getId(),
				EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertFalse("in Db the eventConfiguration must be set to false ", loadedConfigNodeData.isEnabled());

		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(printSerEvt.getId());
		Assert.assertNull("The eventConfiguration data in Data Grid should be Null ", dgEvtConfigUnit);

		prodConfig = printSerEvt.getCamelEventProducer().getCamelProducerConfig();
		eventIdfromProducerMap = (String) producerMap.get(prodConfig.getServiceName() + "-" + prodConfig.getRaiseOn());

		Assert.assertNull("Event{PRINTSERVICE} should not be available in ProducerConfigService Map ",
				eventIdfromProducerMap);

	}

	@Test
	public void testChangeStatusConfigurationEventWithBeanProducerToDisable()
			throws EventFrameworkConfigParserException, EventFrameworkConfigurationException,
			ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_BEANPRODUCER")) {
				printSerEvt = event;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_SITE_NODEID, printSerEvt.getId(),
				EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertEquals("Event{PRINT_SERVICE_BEANPRODUCER} should be inserted in datbade at nodeId=26",
				"PRINT_SERVICE_BEANPRODUCER", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(printSerEvt.getId());
		Event dgEvent = (Event) dgEvtConfigUnit.getConfigData();
		logger.debug(".testAddConfigurationEvent() DataGrid eventId is " + dgEvent.getId());
		Assert.assertEquals("Event{PRINT_SERVICE_BEANPRODUCER} should be available in DG", "PRINT_SERVICE_BEANPRODUCER",
				dgEvent.getId());
		// Test that Event producer are added or not in DataGrid
		IMap producerMap = hazelcastInstance.getMap(
				EventFrameworkConfigurationUnit.getEventProcucerForBeanGroupKey(GenericTestConstant.TEST_SITE_NODEID));
		CamelProducerConfig prodConfig = printSerEvt.getCamelEventProducer().getCamelProducerConfig();
		// producerConfig.getComponent()+"-"+serviceName
		String EventIdfromProducerMap = (String) producerMap
				.get(prodConfig.getComponent() + "-" + prodConfig.getServiceName());
		Assert.assertEquals("Event{PRINT_SERVICE_BEANPRODUCER} should be available in BeanProducerConfig Map with ",
				"PRINT_SERVICE_BEANPRODUCER", EventIdfromProducerMap);

		boolean isDisabled = eventConfigService.changeStatusOfEventConfiguration(configContext, dgEvent.getId(), false);
		Assert.assertTrue("configuration should be disabled == true", isDisabled);

		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(printSerEvt.getId());

		Assert.assertNull("Event{PRINT_SERVICE_BEANPRODUCER} should be null", dgEvtConfigUnit);

		producerMap = hazelcastInstance.getMap(
				EventFrameworkConfigurationUnit.getEventProcucerForBeanGroupKey(GenericTestConstant.TEST_SITE_NODEID));
		prodConfig = printSerEvt.getCamelEventProducer().getCamelProducerConfig();
		// producerConfig.getComponent()+"-"+serviceName
		EventIdfromProducerMap = (String) producerMap
				.get(prodConfig.getComponent() + "-" + prodConfig.getServiceName());

		Assert.assertNull("Event{PRINT_SERVICE_BEANPRODUCER} should not available in BeanProducerConfig Map ",
				EventIdfromProducerMap);
	}

	

	@Test
	public void testAddConfigurationDispatchChanel() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<DispatchChanel> disChanelList = eventFrameworkConfig.getDispatchChanels().getDispatchChanel();
		DispatchChanel disChanelConfig = null;
		for (DispatchChanel disChanel : disChanelList) {
			if (disChanel.getId().equalsIgnoreCase("FILE_STORE")) {
				disChanelConfig = disChanel;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, disChanelConfig);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), disChanelConfig.getId(),
				EventFrameworkConstants.EF_DISPATCHCHANEL_CONFIG_TYPE);
		Assert.assertEquals("DispatchChanel{FILE_STORE} should be inserted in datbade at nodeId=23 {site1}",
				"FILE_STORE", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getDispatchChanelConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(disChanelConfig.getId());
		DispatchChanel dgDispatchChanel = (DispatchChanel) dgEvtConfigUnit.getConfigData();
		logger.debug(".testAddConfigurationEvent() DataGrid eventId is " + dgDispatchChanel.getId());
		Assert.assertEquals("DispatchChanel{FILE_STORE} should be available in DG", "FILE_STORE",
				dgDispatchChanel.getId());

		// HazelcastInstance hazelcastInstance2=Hazelcast.
	}

	@Test
	public void testGetEventConfiguration() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		// Test Adding at 4
		ConfigurationContext configContext = getConfigurationContext(4);

		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEvt = event;
				break;
			}
		}
		// add at level-4 feature
		printSerEvt.setId("PRINT_SERVICE-L4");
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		// add at level 3 feature group
		printSerEvt.setId("PRINT_SERVICE-L3");
		configContext.setFeatureName(null);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		// add at level 2 site level
		printSerEvt.setId("PRINT_SERVICE-L2");
		configContext.setFeatureGroup(null);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);

		ConfigurationContext searchConfigContext = getConfigurationContext(4);
		Event evt4 = eventConfigService.getEventConfiguration(searchConfigContext, "PRINT_SERVICE-L4");
		Assert.assertNotNull("Event -L4 Tagged at Feature is not found", evt4);
		Event evt3 = eventConfigService.getEventConfiguration(searchConfigContext, "PRINT_SERVICE-L3");
		Assert.assertNotNull("Event -L3 Tagged at FeatureGroup is not found", evt3);
		Event evt2 = eventConfigService.getEventConfiguration(searchConfigContext, "PRINT_SERVICE-L2");
		Assert.assertNotNull("Event -L2 Tagged at Site is not found", evt2);
	}

	@Test
	public void testGetDispatchChanelConfiguration() throws EventFrameworkConfigurationException {
		// Test Adding at 4
		ConfigurationContext configContext = getConfigurationContext(4);
		List<DispatchChanel> disChanelList = eventFrameworkConfig.getDispatchChanels().getDispatchChanel();
		DispatchChanel disChanelConfig = null;
		for (DispatchChanel disChanel : disChanelList) {
			if (disChanel.getId().equalsIgnoreCase("EVT_FILE_STORE")) {
				disChanelConfig = disChanel;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, disChanelConfig);
		// get it now
		DispatchChanel disChanel = eventConfigService.getDispatchChanelConfiguration(configContext, "EVT_FILE_STORE");
		Assert.assertNotNull("DispatchChanel {EVT_FILE_STORE} should not be null", disChanel);
	}

	@Test
	public void testApplicableNodeId() throws InvalidNodeTreeException, ConfigPersistenceException {
		EventFrameworkConfigHelper helper = new EventFrameworkConfigHelper();
		ConfigurationContext configContext = getConfigurationContext(4);
		Integer featureNodeId = helper.getApplicableNodeId(configContext);
		/*Assert.assertEquals("ApplicableNodeId at Feature should be", GenericTestConstant.getFeatureConfigNodeId(),
				featureNodeId.intValue());*/
		configContext.setFeatureName(null);
		Integer featureGrpNodeId = helper.getApplicableNodeId(configContext);
		Assert.assertEquals("ApplicableNodeId at FeatureGroup should be",
				GenericTestConstant.getFeatureGroupConfigNodeId(), featureGrpNodeId.intValue());
		configContext.setFeatureGroup(null);
		Integer siteNodeId = helper.getApplicableNodeId(configContext);
		Assert.assertEquals("ApplicableNodeId at Site should be", GenericTestConstant.getSiteConfigNodeId(),
				siteNodeId.intValue());
	}

	/**
	 * To test the Change Status Of DispactherChanelConfiguration To Enabled
	 * status, check wether in DB it enabled , to test configData is loaded into
	 * Cache or not
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void TestChangeStatusOfDispactherChanelConfigurationToEnable()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {

		ConfigurationContext configContext = getConfigurationContext(2);
		List<DispatchChanel> disChanelList = eventFrameworkConfig.getDispatchChanels().getDispatchChanel();
		DispatchChanel disChanelConfig = null;
		for (DispatchChanel disChanel : disChanelList) {
			if (disChanel.getId().equalsIgnoreCase("TEST_CHANELTWO")) {
				disChanelConfig = disChanel;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, disChanelConfig);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), disChanelConfig.getId(),
				EventFrameworkConstants.EF_DISPATCHCHANEL_CONFIG_TYPE);
		Assert.assertEquals("DispatchChanel{TEST_CHANELTWO} should be inserted in datbade at nodeId=23 {site1}",
				"TEST_CHANELTWO", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getDispatchChanelConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(disChanelConfig.getId());

		Assert.assertNull("EventFrameworkConfigurationUnit should not  null ", dgEvtConfigUnit);

		boolean isEnabled = eventConfigService.changeStatusOfDispactherChanelConfiguration(configContext,
				disChanelConfig.getId(), true);
		Assert.assertTrue("dispatchchanel must be enabled = true", isEnabled);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), disChanelConfig.getId(),
				EventFrameworkConstants.EF_DISPATCHCHANEL_CONFIG_TYPE);
		Assert.assertNotNull("configdata should not be null ", loadedConfigNodeData);
		Assert.assertTrue("dispatcherchanel should be enabled ", loadedConfigNodeData.isEnabled());
		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getDispatchChanelConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(disChanelConfig.getId());

		Assert.assertNotNull("Dispatchchanle cache data sholuld not be null ", dgEvtConfigUnit);
		DispatchChanel dgDispatchChanel = (DispatchChanel) dgEvtConfigUnit.getConfigData();
		Assert.assertEquals("DispatchChanel{TEST_CHANELTWO} should be available in DG", "TEST_CHANELTWO",
				dgDispatchChanel.getId());

	}

	/**
	 * change the status of DispactherChanelConfiguration to disable ,check
	 * wether in DB config is Disabled or not and check configData is deleted
	 * from the Data Grid
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void TestchangeStatusOfDispactherChanelConfigurationToDisable()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {

		ConfigurationContext configContext = getConfigurationContext(2);
		List<DispatchChanel> disChanelList = eventFrameworkConfig.getDispatchChanels().getDispatchChanel();
		DispatchChanel disChanelConfig = null;
		for (DispatchChanel disChanel : disChanelList) {
			if (disChanel.getId().equalsIgnoreCase("FILE_STORE")) {
				disChanelConfig = disChanel;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, disChanelConfig);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), disChanelConfig.getId(),
				EventFrameworkConstants.EF_DISPATCHCHANEL_CONFIG_TYPE);
		Assert.assertEquals("DispatchChanel{FILE_STORE} should be inserted in datbade at nodeId=23 {site1}",
				"FILE_STORE", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getDispatchChanelConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(disChanelConfig.getId());
		DispatchChanel dgDispatchChanel = (DispatchChanel) dgEvtConfigUnit.getConfigData();
		logger.debug(".testAddConfigurationEvent() DataGrid eventId is " + dgDispatchChanel.getId());
		Assert.assertEquals("DispatchChanel{FILE_STORE} should be available in DG", "FILE_STORE",
				dgDispatchChanel.getId());
		boolean isDiasbled = eventConfigService.changeStatusOfDispactherChanelConfiguration(configContext,
				dgDispatchChanel.getId(), false);
		Assert.assertTrue("dispatchchanel is muste be disabled = true", isDiasbled);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), disChanelConfig.getId(),
				EventFrameworkConstants.EF_DISPATCHCHANEL_CONFIG_TYPE);
		Assert.assertNotNull("configdata should not be null ", loadedConfigNodeData);
		Assert.assertFalse("dispatcherchanel should be disabled ", loadedConfigNodeData.isEnabled());
		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getDispatchChanelConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(disChanelConfig.getId());

		Assert.assertNull("Dispatchchanle cache data sholuld be null ", dgEvtConfigUnit);

	}

	/**
	 * Test wether EventSubscriptionConfigurationconfigData is deleted or not in
	 * both DB and Cache, if configuration in Enabled status
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void TestDeleteEventSubscriptionConfigurationIfEnabled()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(4);

		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		List<EventSubscription> eventSubscriptionList = eventFrameworkConfig.getEventSubscriptions()
				.getEventSubscription();
		EventSubscription printSerEventSubs = null;
		Event printSerEvt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEvt = event;
				break;
			}
		}
		for (EventSubscription eventSubs : eventSubscriptionList) {
			if (eventSubs.getEventId().equalsIgnoreCase("PRINT_SERVICE")) {
				printSerEventSubs = eventSubs;
				break;
			}
		}
		Assert.assertNotNull("Issue with input provided to begin the testcase, Test Event loded from config is null",
				printSerEvt);
		Assert.assertNotNull(
				"Issue with input provided to begin the testcase, Test EventSubscription loded from config is null",
				printSerEventSubs);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEvt);
		eventConfigService.addEventFrameworkConfiguration(configContext, printSerEventSubs);
		eventConfigService.deleteEventSubscriptionConfiguration(configContext, printSerEventSubs.getEventId());
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getConfigNodeId(), printSerEventSubs.getEventId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);

		Assert.assertNull("Event Subscription Configuration in Data base should be  Null ", loadedConfigNodeData);

		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getEventConfigGroupKey((new Integer(GenericTestConstant.TEST_SITE_NODEID))));// 26
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(printSerEventSubs.getEventId());

		Assert.assertNotNull("Event Subscription Configuration in Data Grid should be not Null ", dgEvtConfigUnit);

	}

	/**
	 * Test wether DispactherChanelConfigurationconfigData is deleted or not in
	 * both DB and Cache, if configuration in Enabled status
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void TestDeleteDispactherChanelConfigurationIfEnabled()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {

		ConfigurationContext configContext = getConfigurationContext(2);
		List<DispatchChanel> disChanelList = eventFrameworkConfig.getDispatchChanels().getDispatchChanel();
		DispatchChanel disChanelConfig = null;
		for (DispatchChanel disChanel : disChanelList) {
			if (disChanel.getId().equalsIgnoreCase("FILE_STORE")) {
				disChanelConfig = disChanel;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, disChanelConfig);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), disChanelConfig.getId(),
				EventFrameworkConstants.EF_DISPATCHCHANEL_CONFIG_TYPE);
		Assert.assertEquals("DispatchChanel{FILE_STORE} should be inserted in datbade at nodeId=23 {site1}",
				"FILE_STORE", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getDispatchChanelConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(disChanelConfig.getId());
		DispatchChanel dgDispatchChanel = (DispatchChanel) dgEvtConfigUnit.getConfigData();
		logger.debug(".testAddConfigurationEvent() DataGrid eventId is " + dgDispatchChanel.getId());
		Assert.assertEquals("DispatchChanel{FILE_STORE} should be available in DG", "FILE_STORE",
				dgDispatchChanel.getId());

		boolean isDeleted = eventConfigService.deleteDipatcherChanelConfiguration(configContext,
				dgDispatchChanel.getId());

		Assert.assertTrue("DeleteDispactherChanelConfiguration from Db and Cache isDeleted=true ", isDeleted);
		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), dgDispatchChanel.getId(),
				EventFrameworkConstants.EF_DISPATCHCHANEL_CONFIG_TYPE);
		Assert.assertNull("DispactherChanelConfiguration EVent data should be Null ", loadedConfigNodeData);

		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getDispatchChanelConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(dgDispatchChanel.getId());

		Assert.assertNull("DispactherChanelConfiguration in the DG(data Grid) should be null ", dgEvtConfigUnit);

	}

	/**
	 * To test delete of DispactherChanelConfiguration data in DB if config in
	 * Disabled status
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void TestDeleteDispactherChanelConfigurationIfDisabled()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {

		ConfigurationContext configContext = getConfigurationContext(2);
		List<DispatchChanel> disChanelList = eventFrameworkConfig.getDispatchChanels().getDispatchChanel();
		DispatchChanel disChanelConfig = null;
		for (DispatchChanel disChanel : disChanelList) {
			if (disChanel.getId().equalsIgnoreCase("TEST_CHANELTWO")) {
				disChanelConfig = disChanel;
				break;
			}
		}

		ROIConfigurationServer roiConfigurationServer;

		// To delete the configration if it already exist in DataGrid
		try {
			roiConfigurationServer = ROIConfigurationServer.getConfigurationService();

			roiConfigurationServer.deleteConfiguration(configContext.getTenantId(), EventFrameworkConfigurationUnit
					.getDispatchChanelConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))),
					disChanelConfig.getId());
		} catch (ConfigServerInitializationException e) {
			// TODO Auto-generated catch block
			logger.error("error in ConfigServerInitialization " + e);
		}

		eventConfigService.addEventFrameworkConfiguration(configContext, disChanelConfig);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), disChanelConfig.getId(),
				EventFrameworkConstants.EF_DISPATCHCHANEL_CONFIG_TYPE);
		Assert.assertEquals("DispatchChanel{FILE_STORE} should be inserted in datbade at nodeId=23 {site1}",
				"TEST_CHANELTWO", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getDispatchChanelConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(disChanelConfig.getId());

		Assert.assertNull("EventFrameworkConfigurationUnit should not  null ", dgEvtConfigUnit);

		boolean isDeleted = eventConfigService.deleteDipatcherChanelConfiguration(configContext,
				disChanelConfig.getId());

		Assert.assertTrue("DispactherChanelConfiguration from Db and Cache isDeleted=true ", isDeleted);
		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), disChanelConfig.getId(),
				EventFrameworkConstants.EF_DISPATCHCHANEL_CONFIG_TYPE);
		Assert.assertNull("DispactherChanelConfiguration EVent data should be Null ", loadedConfigNodeData);

	}

	/**
	 * To test the Change Status Of StatusOfSystemEventConfiguration To Enabled
	 * status, check wether in DB it enabled , to test configData is loaded into
	 * Cache or not
	 * 
	 * @throws EventFrameworkConfigParserException
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */

	@Test
	public void testchangeStatusOfSystemEventConfigurationToEnable() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<SystemEvent> eventList = eventFrameworkConfig.getSystemEvents().getSystemEvent();
		SystemEvent serComSucessEvt = null;
		for (SystemEvent event : eventList) {
			if (event.getId().equalsIgnoreCase("SERVICE_COMPLETION_SUCCESS")) {
				serComSucessEvt = event;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, serComSucessEvt);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), serComSucessEvt.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
		Assert.assertEquals(
				"System-Event{SERVICE_COMPLETION_SUCCESS} should be inserted in datbade at nodeId=23 {site1}",
				"SERVICE_COMPLETION_SUCCESS", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getSystemEventConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(serComSucessEvt.getId());

		Assert.assertNull("systemEvent config data shoul be null ", dgEvtConfigUnit);
		boolean isEnabled = eventConfigService.changeStatusOfSystemEventConfiguration(configContext,
				serComSucessEvt.getId(), true);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), serComSucessEvt.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);

		Assert.assertNotNull("the data in the DB should not be null ", loadedConfigNodeData);
		Assert.assertTrue("the SystemEventConfigarationData  data should be enabled", loadedConfigNodeData.isEnabled());
		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getSystemEventConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(serComSucessEvt.getId());
		Assert.assertNotNull("systemEvent data in DG should not be null ", dgEvtConfigUnit);
		Assert.assertEquals("Event{SERVICE_COMPLETION_SUCCESS} should be available in DG", "SERVICE_COMPLETION_SUCCESS",
				serComSucessEvt.getId());
	}

	/**
	 * change the status of SystemEventConfiguration to disable ,check wether in
	 * DB config is Disabled or not and check configData is deleted from the
	 * Data Grid
	 * 
	 * @throws EventFrameworkConfigParserException
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testchangeStatusOfSystemEventConfigurationToDisable() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<SystemEvent> eventList = eventFrameworkConfig.getSystemEvents().getSystemEvent();
		SystemEvent serComFailureEvt = null;
		for (SystemEvent event : eventList) {
			if (event.getId().equalsIgnoreCase("SERVICE_COMPLETION_FAILURE")) {
				serComFailureEvt = event;
				break;
			}
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, serComFailureEvt);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), serComFailureEvt.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
		Assert.assertEquals(
				"System-Event{SERVICE_COMPLETION_FAILURE} should be inserted in datbade at nodeId=23 {site1}",
				"SERVICE_COMPLETION_FAILURE", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getSystemEventConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(serComFailureEvt.getId());
		SystemEvent dgSysEvent = (SystemEvent) dgEvtConfigUnit.getConfigData();
		logger.debug(".testAddConfigurationEvent() DataGrid eventId is " + dgSysEvent.getId());
		Assert.assertEquals("Event{SERVICE_COMPLETION_FAILURE} should be available in DG", "SERVICE_COMPLETION_FAILURE",
				dgSysEvent.getId());
		boolean isDiabled = eventConfigService.changeStatusOfSystemEventConfiguration(configContext,
				serComFailureEvt.getId(), false);
		Assert.assertTrue("status of SystemEventFramework should be disabled=true", isDiabled);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), serComFailureEvt.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);

		Assert.assertNotNull("the data in the DB should not be null ", loadedConfigNodeData);
		Assert.assertFalse("the SystemEventConfigarationData  data should be disabled",
				loadedConfigNodeData.isEnabled());

		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getSystemEventConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(serComFailureEvt.getId());

		Assert.assertNull("the SystemEventConfigaration data  in the cache  should be null ", dgEvtConfigUnit);
	}

	/**
	 * To test ConfigurationSystemEvent is deleted from both DB and Cache if it
	 * is enabled status
	 * 
	 * @throws EventFrameworkConfigParserException
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testDeleteConfigurationSystemEventIfEnable() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<SystemEvent> eventList = eventFrameworkConfig.getSystemEvents().getSystemEvent();
		SystemEvent serComFailureEvt = null;
		for (SystemEvent event : eventList) {
			if (event.getId().equalsIgnoreCase("SERVICE_COMPLETION_FAILURE")) {
				serComFailureEvt = event;
				break;
			}
		}

		eventConfigService.addEventFrameworkConfiguration(configContext, serComFailureEvt);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), serComFailureEvt.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
		Assert.assertEquals(
				"System-Event{SERVICE_COMPLETION_FAILURE} should be inserted in datbade at nodeId=23 {site1}",
				"SERVICE_COMPLETION_FAILURE", loadedConfigNodeData.getConfigName());
		// Test start for Hazelcast gap-26-PSC
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getSystemEventConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		EventFrameworkConfigurationUnit dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map
				.get(serComFailureEvt.getId());
		SystemEvent dgSysEvent = (SystemEvent) dgEvtConfigUnit.getConfigData();
		logger.debug(".testAddConfigurationEvent() DataGrid eventId is " + dgSysEvent.getId());
		Assert.assertEquals("Event{SERVICE_COMPLETION_FAILURE} should be available in DG", "SERVICE_COMPLETION_FAILURE",
				dgSysEvent.getId());

		boolean isDeleted = eventConfigService.deleteSystemEventConfiguration(configContext, serComFailureEvt.getId());

		Assert.assertTrue("DeleteConfigurationSystemEvent from Db and Cache isDeleted=true ", isDeleted);
		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), serComFailureEvt.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
		Assert.assertNull("ConfigurationSystemEvent EVent data should be Null ", loadedConfigNodeData);

		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + EventFrameworkConfigurationUnit
				.getSystemEventConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))));// 23
		dgEvtConfigUnit = (EventFrameworkConfigurationUnit) map.get(serComFailureEvt.getId());

		Assert.assertNull("ConfigurationSystemEvent in the DG(data Grid) should be null ", dgEvtConfigUnit);
	}

	/**
	 * To test delete ConfigurationSystemEvent from DB if it is Disabled status
	 * 
	 * @throws EventFrameworkConfigParserException
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testDeleteConfigurationSystemEventIfDisable() throws EventFrameworkConfigParserException,
			EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = getConfigurationContext(2);
		List<SystemEvent> eventList = eventFrameworkConfig.getSystemEvents().getSystemEvent();
		SystemEvent serComSucessEvt = null;
		for (SystemEvent event : eventList) {
			if (event.getId().equalsIgnoreCase("SERVICE_COMPLETION_SUCCESS")) {
				serComSucessEvt = event;
				break;
			}
		}

		ROIConfigurationServer roiConfigurationServer;

		// To delete the configration if it already exist in DataGrid
		try {
			roiConfigurationServer = ROIConfigurationServer.getConfigurationService();

			roiConfigurationServer.deleteConfiguration(configContext.getTenantId(), EventFrameworkConfigurationUnit
					.getSystemEventConfigGroupKey((new Integer(GenericTestConstant.getSiteConfigNodeId()))),
					serComSucessEvt.getId());
		} catch (ConfigServerInitializationException e) {
			// TODO Auto-generated catch block
			logger.error("error in ConfigServerInitialization " + e);
		}
		eventConfigService.addEventFrameworkConfiguration(configContext, serComSucessEvt);
		// Test strats check in DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), serComSucessEvt.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
		Assert.assertEquals(
				"System-Event{SERVICE_COMPLETION_SUCCESS} should be inserted in datbade at nodeId=23 {site1}",
				"SERVICE_COMPLETION_SUCCESS", loadedConfigNodeData.getConfigName());

		boolean isDeleted = eventConfigService.deleteSystemEventConfiguration(configContext, serComSucessEvt.getId());

		Assert.assertTrue("DeleteConfigurationSystemEvent from Db and Cache isDeleted=true ", isDeleted);
		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.getSiteConfigNodeId(), serComSucessEvt.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
		Assert.assertNull("ConfigurationSystemEvent EVent data should be Null ", loadedConfigNodeData);

	}

	private ConfigurationContext getConfigurationContext(int level) {
		ConfigurationContext configContext = null;
		if (level == 4)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
					GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME);
		if (level == 2)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite());
		return configContext;

	}
}
