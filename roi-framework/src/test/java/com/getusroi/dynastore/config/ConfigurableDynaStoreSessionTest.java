package com.getusroi.dynastore.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import com.getusroi.dynastore.ConfigurableDynaStoreSession;
import com.getusroi.dynastore.DynaStoreRequestException;
import com.getusroi.dynastore.config.impl.DynaStoreConfigXmlParser;
import com.getusroi.dynastore.config.impl.DynaStoreConfigurationService;
import com.getusroi.dynastore.config.jaxb.DynastoreConfiguration;
import com.getusroi.dynastore.config.jaxb.DynastoreConfigurations;
import com.getusroi.eventframework.config.EventFrameworkConfigParserException;
import com.getusroi.eventframework.config.EventFrameworkConfigurationException;
import com.getusroi.eventframework.config.EventFrameworkConfigurationUnit;
import com.getusroi.eventframework.config.EventFrameworkXmlHandler;
import com.getusroi.eventframework.config.IEventFrameworkConfigService;
import com.getusroi.eventframework.config.impl.EventFrameworkConfigService;
import com.getusroi.eventframework.jaxb.DispatchChanel;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.eventframework.jaxb.EventFramework;
import com.getusroi.eventframework.jaxb.Events;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.transaction.TransactionContext;

public class ConfigurableDynaStoreSessionTest {
	final Logger logger = LoggerFactory.getLogger(DynaStoreInitializationTest.class);
	List<DynastoreConfiguration> dynaStolreConfigList;
	IDynaStoreConfigurationService iDynaStoreConfigurationService;
	IConfigPersistenceService configPersistenceService;

	private static EventFramework eventFrameworkConfig;
	private IEventFrameworkConfigService eventConfigService = new EventFrameworkConfigService();

	private EventFramework getEventFrameworkConfiguration() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(DynaStoreTestConstant.EVENT_FOR_DYNASRORE);
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

	private DynastoreConfigurations getDynaStoreconfiguration() throws DynaStoreConfigurationException, DynaStoreConfigParserException {

		DynaStoreConfigXmlParser parser = new DynaStoreConfigXmlParser();
		InputStream inputstream = DynaStoreConfigurationServiceTest.class.getClassLoader().getResourceAsStream(DynaStoreTestConstant.configfileToParse);

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);

			}
			reader.close();
		} catch (IOException e) {
			throw new DynaStoreConfigurationException("dynastoreTest file doesnot exist in classpath", e);
		}

		String configXMLFile = out1.toString();
		DynastoreConfigurations dynastoreConfigurations = parser.marshallConfigXMLtoObject(configXMLFile);

		return dynastoreConfigurations;
	}

	@Before
	public void loadConfigurations() throws DynaStoreConfigurationException, DynaStoreConfigParserException, ConfigPersistenceException,
			EventFrameworkConfigParserException {
		//Load EventConfig First
		loadEventConfigurations();
		
		DynastoreConfigurations dynastoreConfigurations = getDynaStoreconfiguration();
		if (iDynaStoreConfigurationService == null)
			iDynaStoreConfigurationService = new DynaStoreConfigurationService();

		if (configPersistenceService == null)
			configPersistenceService = new ConfigPersistenceServiceMySqlImpl();

		configPersistenceService.deleteConfigNodeDataByNodeId(GenericTestConstant.getConfigNodeId());
		configPersistenceService.deleteConfigNodeDataByNodeId(GenericTestConstant.getVendorConfigNodeId());
		dynaStolreConfigList = dynastoreConfigurations.getDynastoreConfiguration();
		eventFrameworkConfig = getEventFrameworkConfiguration();

	}

	public void loadEventConfigurations() throws EventFrameworkConfigParserException, ConfigPersistenceException {
		if (eventFrameworkConfig == null) {
			eventFrameworkConfig = getEventFrameworkConfiguration();
		}
		// Clear all DB Data First for SITE=23 FG=25,FEATURE-26
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getConfigNodeId());
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getSiteConfigNodeId());
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getFeatureGroupConfigNodeId());
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getVendorConfigNodeId());
		// clear hazelcast as well for Event
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap producerMap = hazelcastInstance.getMap(EventFrameworkConfigurationUnit.getEventProcucerForServiceGroupKey(GenericTestConstant.getConfigNodeId()));// 26
		producerMap.clear();
	}

	
	
	@Test
	public void testAddPicToDynaStoreForEventGeneration() throws DynaStoreConfigurationException, DynaStoreConfigRequestContextException, EventFrameworkConfigurationException, DynaStoreRequestException{
		ConfigurationContext configurationContext = getConfigurationContext(5);
		//Seup Eventing first Chanel than the Event
		eventConfigService.addEventFrameworkConfiguration(configurationContext, this.getDispatchChanel("DynaStoreQueueChanel"));
		eventConfigService.addEventFrameworkConfiguration(configurationContext, this.getEventConfig("PicAreaAdded"));
		
		DynastoreConfiguration dynaConfig=getDynaStoreConfiguration("PicArea");
		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynaConfig);
		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynaConfig);
		
		//Verify that dynastore is created.
		DynaStoreConfigurationUnit dynaConfigUnit=iDynaStoreConfigurationService.getDynaStoreConfigurationUnit(getRequestContext(), "PicArea", "1.0");
		Assert.assertNotNull("ConfigurationUnit for PicAreaAdded should not be null ", dynaConfigUnit);
		
		//Get Dynastore session.
		ConfigurableDynaStoreSession session=new ConfigurableDynaStoreSession(getRequestContext(),"PicArea", "1.0");
		HazelcastInstance hcIns=DataGridService.getDataGridInstance().getHazelcastInstance();
		TransactionContext hcTransactionalContext=hcIns.newTransactionContext();
		hcTransactionalContext.beginTransaction();
		RequestContext reqCtx=getRequestContext();
		reqCtx.setHcTransactionalContext(hcTransactionalContext);
		session.addSessionData(reqCtx, "test", "Test Area");
		logger.debug("get the session data : "+session.getSessionData(reqCtx, "test"));
		logger.debug(" requestContext : "+reqCtx);		
		hcTransactionalContext.commitTransaction();
		// Check the Mapdata
		String dynaCollectionId = dynaConfigUnit.getDynaCollectionId();
		logger.debug("dynaCollectionId : " + dynaCollectionId);
		Map map = hcIns.getMap(dynaCollectionId);
		logger.debug("map : " + map);
		Assert.assertTrue("ConfigDynaStore must have added key for key=test", map.containsKey("test"));
		// Check the EventList
		String requestId = reqCtx.getRequestId();
		IList eventList = hcIns.getList(requestId);
		Assert.assertTrue("ConfigDynaStore must have added Event and size must be 1", eventList.size() == 1);
	}
	
	@Test
	public void testDeletePicToDynaStoreForEventGeneration() throws DynaStoreConfigurationException, DynaStoreConfigRequestContextException, EventFrameworkConfigurationException, DynaStoreRequestException{
		ConfigurationContext configurationContext = getConfigurationContext(5);
		//Seup Eventing first Chanel than the Event
//		eventConfigService.addEventFrameworkConfiguration(configurationContext, this.getDispatchChanel("DynaStoreQueueChanel"));
//		eventConfigService.addEventFrameworkConfiguration(configurationContext, this.getEventConfig("PicAreaAdded"));
//		
//		DynastoreConfiguration dynaConfig=getDynaStoreConfiguration("PicArea");
//		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynaConfig);
//		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynaConfig);
//		//Verify that dynastore is created.
		DynaStoreConfigurationUnit dynaConfigUnit=iDynaStoreConfigurationService.getDynaStoreConfigurationUnit(getRequestContext(), "PicArea", "1.0");
//		Assert.assertNotNull("ConfigurationUnit for PicAreaAdded should not be null ", dynaConfigUnit);
		
		//Get Dynastore session.
		ConfigurableDynaStoreSession session=new ConfigurableDynaStoreSession(getRequestContext(),"PicArea", "1.0");
		HazelcastInstance hcIns=DataGridService.getDataGridInstance().getHazelcastInstance();
		TransactionContext hcTransactionalContext=hcIns.newTransactionContext();
		hcTransactionalContext.beginTransaction();
		RequestContext reqCtx=getRequestContext();
		reqCtx.setHcTransactionalContext(hcTransactionalContext);
		session.removeSessionData(reqCtx, "LaptopPic");//LaptopPic is added at the Initialization Time
		hcTransactionalContext.commitTransaction();
		//Check the Mapdata
		String dynaCollectionId=dynaConfigUnit.getDynaCollectionId();
		Map map=hcIns.getMap(dynaCollectionId);
		logger.debug("After Removal Map is ="+map.entrySet());
		
		Assert.assertFalse("ConfigDynaStore must not have added key for key=LaptopPic", map.containsKey("LaptopPic"));
		//Check the EventList
		String requestId=reqCtx.getRequestId();
		IList eventList=hcIns.getList(requestId);
		Assert.assertTrue("ConfigDynaStore must have added Event and size must be 1", eventList.size()==1);
	}
	
	private DynastoreConfiguration getDynaStoreConfiguration(String dynaConfigName) {
		for (DynastoreConfiguration dynaConfig : dynaStolreConfigList) {
			if (dynaConfig.getDynastoreName().getValue().equalsIgnoreCase(dynaConfigName)) {
				return dynaConfig;
			}
		}
		return null;
	}
	
	private Event getEventConfig(String eventId) {
		Events events=eventFrameworkConfig.getEvents();
		List<Event> eventList=events.getEvent();
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase(eventId)) {
				return event;
			}
		}
		return null;
	}
	private DispatchChanel getDispatchChanel(String chanelId) {
			List<DispatchChanel> chanelList=eventFrameworkConfig.getDispatchChanels().getDispatchChanel();
		for (DispatchChanel chanel : chanelList) {
			if (chanel.getId().equalsIgnoreCase(chanelId)) {
				return chanel;
			}
		}
		return null;
	}
	
	private ConfigurationContext getConfigurationContext(int level) {
		ConfigurationContext configContext = null;
		if (level == 5)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), GenericTestConstant.getFeatureGroup(),
					GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		if (level == 4)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), GenericTestConstant.getFeatureGroup(),
					GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME);
		if (level == 2)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite());
		return configContext;

	}
	private RequestContext getRequestContext(){
		RequestContext configContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), GenericTestConstant.getFeatureGroup(),
					GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		configContext.setRequestId(getUUID());
		return configContext;
	}
	private String getUUID(){
		//Not using UUID class as UID is too long as a Map Key
		char[] chars = "abcdefghijklmnopqrstuvwxyzABSDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
		Random r = new Random(System.currentTimeMillis());
		char[] id = new char[12];
		for (int i = 0;  i < 12;  i++) {
		    id[i] = chars[r.nextInt(chars.length)];
		}
		logger.debug("UUID is "+new String(id));
		return new String(id);
	}
}
