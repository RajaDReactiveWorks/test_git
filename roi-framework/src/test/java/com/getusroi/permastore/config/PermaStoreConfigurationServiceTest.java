package com.getusroi.permastore.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.config.beans.ConfigurationUnit;
import com.getusroi.config.persistence.ConfigNodeData;
import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.getusroi.config.server.ConfigServerInitializationException;
import com.getusroi.config.util.GenericTestConstant;
import com.getusroi.core.datagrid.DataGridService;
import com.getusroi.permastore.config.impl.PermaStoreConfigXMLParser;
import com.getusroi.permastore.config.impl.PermaStoreConfigurationService;
import com.getusroi.permastore.config.jaxb.ConfigurationBuilder;
import com.getusroi.permastore.config.jaxb.ConfigurationBuilderType;
import com.getusroi.permastore.config.jaxb.CustomBuilder;
import com.getusroi.permastore.config.jaxb.InlineBuilder;
import com.getusroi.permastore.config.jaxb.PermaStoreConfiguration;
import com.getusroi.permastore.config.jaxb.PermaStoreConfigurations;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class PermaStoreConfigurationServiceTest {
	final Logger logger = LoggerFactory.getLogger(PermaStoreConfigurationServiceTest.class);
	private List<PermaStoreConfiguration> permaStoreConfigList;
	private IPermaStoreConfigurationService psConfigService;

	private PermaStoreConfigurations getPermaStoreConfigurations() throws PermaStoreConfigParserException {
		PermaStoreConfigXMLParser parser = new PermaStoreConfigXMLParser();
		InputStream inputstream = PermaStoreConfigXMLParser.class.getClassLoader().getResourceAsStream(PermaStoreTestConstant.configfileToParse);
		
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			    
			}
			reader.close();
		} catch (IOException e) {
		throw new PermaStoreConfigParserException("permastore file doesnot exist in classpath",e);
		}
		
		String permastoreConfigxml=out1.toString();
		PermaStoreConfigurations permaStoreConfigs = parser.marshallConfigXMLtoObject(permastoreConfigxml);
		
		
		
		
		return permaStoreConfigs;
	}

	@Before
	public void loadConfigurations() throws PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigPersistenceException,
			ConfigServerInitializationException {

		PermaStoreConfigurations permaStoreConfigs = getPermaStoreConfigurations();
		psConfigService = new PermaStoreConfigurationService();
		// Clear all DB Data First for nodeId 26
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getConfigNodeId());
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getVendorConfigNodeId());
		// No need to Clear Cached Configurations as it will be overridden

		// ROIConfigurationServer configServer =
		// ROIConfigurationServer.getConfigurationService();
		// PermaStoreConfigurationUnit permaStoreConfigUnit =
		// (PermaStoreConfigurationUnit)
		// configServer.getConfiguration(requestContext.getTenantId(),
		// psGroupKey, configName);
		// configServer.deleteConfiguration(PermaStoreTestConstant.getTenant(),
		// configGroup, configKey);

		permaStoreConfigList = permaStoreConfigs.getPermaStoreConfiguration();
		/*
		 * for (PermaStoreConfiguration config : permaStoreConfigList) {
		 * psConfigService
		 * .addPermaStoreConfiguration(GenericTestConstant.getTenant(),
		 * GenericTestConstant.getSite(), config); }
		 */
	}

	@AfterClass
	public static void removeAllConfigurations() {

	}

	@Test
	public void testCustomBuilder() throws PermaStoreConfigurationException, PermaStoreConfigParserException {

		PermaStoreConfiguration permaStoreConfiguration = getPermastoreConfigByName("AreaList");
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfiguration);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfiguration);

		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ PermaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 26
		Assert.assertNotNull("Cache Map should not be null", map);
		Object cachedObj = map.get("AreaList");
		logger.debug("ConfigObject is =" + cachedObj);
		Assert.assertNotNull("Cached PSConfiguration for CustomBuilder should not be null", cachedObj);

	}

	@Test
	public void testCustomBuilderIfNull() throws PermaStoreConfigurationException, PermaStoreConfigParserException {

		PermaStoreConfiguration permaStoreConfiguration = getPermastoreConfigByName("AreaListSecond");
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfiguration);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfiguration);

		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ PermaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861
		Assert.assertNotNull("Cache Map should not be null", map);

		Object cachedObj = map.get("AreaListSecond");
		logger.debug("ConfigObject is =" + cachedObj);
		Assert.assertNull("Cached PSConfiguration for CustomBuilder should  be null", cachedObj);

	}

	@Test
	public void testInLineBuilder() throws PermaStoreConfigParserException, PermaStoreConfigurationException {

		PermaStoreConfiguration permaStoreConfiguration = getPermastoreConfigByName("lodnum");
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfiguration);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfiguration);

		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ PermaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861
		Assert.assertNotNull("Cache Map should not be null", map);

		logger.debug("GroupLevel Map=" + map);
		Object cachedObj = map.get("lodnum");
		logger.debug("ConfigObject is =" + cachedObj);
		Assert.assertNotNull("Cached PSConfiguration for InLine Builder should not be null", cachedObj);

	}

	@Test
	public void testInLineBuilderIdIfNull() throws PermaStoreConfigParserException, PermaStoreConfigurationException {

		PermaStoreConfiguration permaStoreConfiguration = getPermastoreConfigByName("lodnumSecond");
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfiguration);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfiguration);

		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ PermaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861
		logger.debug("GroupLevel Map=" + map);
		Assert.assertNotNull("Cache Map should not be null", map);

		Object cachedObj = map.get("lodnumSecond");
		logger.debug("ConfigObject is =" + cachedObj);
		Assert.assertNull("Cached PSConfiguration for InLine Builder should be null", cachedObj);

	}

	/** <!-- Test SQLBuilder with Custom Mapper--> */
	@Test
	public void testSQLBuilderWithCustomMapper() throws PermaStoreConfigParserException, PermaStoreConfigurationException {
		PermaStoreConfiguration permaStoreConfiguration = getPermastoreConfigByName("TestSQLBuilderCustomMapper");
		
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfiguration);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfiguration);

		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ PermaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861
		Assert.assertNotNull("Cache Map should not be null", map);

		ConfigurationUnit configUnit =(ConfigurationUnit) map.get("TestSQLBuilderCustomMapper");
		Assert.assertNotNull("ConfigurationUnit should not be null", configUnit);

		Object cachedObj=configUnit.getConfigData();
		logger.debug(".testSQLBuilderWithCustomMapper cachedObject is "+configUnit);
		List list=(List)cachedObj;
		Assert.assertNotNull("SQLBuilder with custom Mapper should not be null", cachedObj);
		Assert.assertTrue("SQLBuilder with custom Mapper should be instance of List",cachedObj instanceof List);
		Assert.assertTrue("SQLBuilder with custom Mapper should be instance of StageArea",((List)cachedObj).get(0) instanceof StageArea);
	
	}

	/** Test SQLBuilder with List-OF-Map builder */
	@Test
	public void testSQLBuilderWithListOfMap() throws PermaStoreConfigParserException, PermaStoreConfigurationException {
		PermaStoreConfiguration permaStoreConfiguration = getPermastoreConfigByName("TestSQLBuilderList-OF-MapMapper");
	
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfiguration);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfiguration);

		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ PermaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861
		Assert.assertNotNull("Cache Map should not be null", map);

		ConfigurationUnit configUnit =(ConfigurationUnit) map.get("TestSQLBuilderList-OF-MapMapper");
		Assert.assertNotNull("ConfigurationUnit should not be null", configUnit);

		Object cachedObj=configUnit.getConfigData();
		Assert.assertNotNull("SQLBuilder with List-OF-Map builder should not be null", cachedObj);
		Assert.assertTrue("SQLBuilder with List-OF-Map builder should be instance of List",cachedObj instanceof List);
		Assert.assertTrue("SQLBuilder withList-OF-Map builder should be instance of StageArea",((List)cachedObj).get(0) instanceof Map);
	
	}
	
	/** Test SQLBuilder with List-OF-Map builder */
	@Test
	public void testReLoadCacheWithSQLBuilderListOfMap() throws PermaStoreConfigParserException, PermaStoreConfigurationException {
		PermaStoreConfiguration permaStoreConfiguration = getPermastoreConfigByName("TestSQLBuilderList-OF-MapMapper");
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfiguration);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfiguration);

		
		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ PermaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861
		Assert.assertNotNull("Cache Map should not be null", map);

		ConfigurationUnit configUnit =(ConfigurationUnit) map.get("TestSQLBuilderList-OF-MapMapper");
		Assert.assertNotNull("ConfigurationUnit should not be null", configUnit);

		Object cachedObj=configUnit.getConfigData();
		Assert.assertNotNull("SQLBuilder with List-OF-Map builder should not be null", cachedObj);
		Assert.assertTrue("SQLBuilder with List-OF-Map builder should be instance of List",cachedObj instanceof List);
		Assert.assertTrue("SQLBuilder withList-OF-Map builder should be instance of StageArea",((List)cachedObj).get(0) instanceof Map);
	
	}
	
	@Test
	public void testGetPermaStorePolicy() throws PermaStoreConfigRequestException, PermaStoreConfigurationException {

		PermaStoreConfiguration permaStoreConfiguration = getPermastoreConfigByName("AreaList");
		
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfiguration);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfiguration);

		// Check retreiving data using the
		IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
		RequestContext requestContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		PermaStoreConfigurationUnit configUnit = psConfigService.getPermaStoreConfiguration(requestContext, "AreaList");
		Assert.assertNotNull("Configuration unit should not be Null", configUnit);
		Assert.assertNotNull("Cached Data in Configuration unit should not be Null", configUnit.getConfigData());
	}

	@Test
	public void testChangeStatusOfPermaStoreConfigDisable() throws PermaStoreConfigurationException, ConfigPersistenceException {
		IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("AreaList");
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfig);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);

		// Load from Database
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), permaStoreConfig.getName(),
				PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);

		RequestContext requestContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		ConfigurationContext configurationContextReq=requestContext.getConfigurationContext();
		psConfigService.changeStatusOfPermaStoreConfig(configurationContextReq, permaStoreConfig.getName(), false);
		
		Assert.assertNotNull("ConfigurationUnit should not be null", permaStoreConfig);

		// Disabled should be removed from Cache
		ConfigurationUnit cachedObj = checkCacheForConfig(permaStoreConfig.getName());
		Assert.assertNull("Object from Cache should be Deleted if Config is Disabled", cachedObj);
		

		// Disabling should be marked as disable in Database
		ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
		Assert.assertNotNull("Object should exist in DataBase if config is disabled", configData);
		logger.debug("testChangeStatusOfPermaStoreConfigDisable() " + configData.isEnabled());
		Assert.assertFalse("In the Database should be marked as Diabled", configData.isEnabled());
	}

	@Test
	public void testChangeStatusOfPermaStoreConfigDisableForConfigTwo() throws PermaStoreConfigurationException, ConfigPersistenceException {
		IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();

		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("lodnum");
		
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfig);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);

		// Load from Database
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), permaStoreConfig.getName(),
				PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);

		RequestContext requestContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		ConfigurationContext configurationContextReq=requestContext.getConfigurationContext();
		psConfigService.changeStatusOfPermaStoreConfig(configurationContextReq, permaStoreConfig.getName(), false);
	
		// Disabled should be removed from Cache
		ConfigurationUnit cachedObj = checkCacheForConfig(permaStoreConfig.getName());
		Assert.assertNull("Object from Cache should be Deleted if Config is Disabled", cachedObj);
		
		// Disabling should be marked as disable in Database
		ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
		Assert.assertNotNull("Object should exist in DataBase if config is disabled", configData);
		logger.debug("testChangeStatusOfPermaStoreConfigDisable() " + configData.isEnabled());
		Assert.assertFalse("In the Database should be marked as Diabled", configData.isEnabled());
	}

	@Test
	public void testChangeStatusOfPermaStoreConfigEnable() throws PermaStoreConfigurationException, ConfigPersistenceException {
		IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();

		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("AreaList");
		
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfig);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);

		// Load from Database
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), permaStoreConfig.getName(),
				PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);

		RequestContext requestContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		ConfigurationContext configurationContextReq=requestContext.getConfigurationContext();
		psConfigService.changeStatusOfPermaStoreConfig(configurationContextReq, permaStoreConfig.getName(), true);

		// Enabled should be reloaded into the Cache
		ConfigurationUnit cachedObj = checkCacheForConfig(permaStoreConfig.getName());
		Assert.assertNotNull("Object should be reloaded in DataGrid if Config is Enabled", cachedObj);
		
		Assert.assertNotNull("Cached Data in Configuration unit should not be Null", cachedObj.getConfigData());

		// Enable should be marked as Enable in Database
		ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
		Assert.assertNotNull("Object should exist in DataBase if config is enabled", configData);
		logger.debug("testChangeStatusOfPermaStoreConfigEnable() " + configData.isEnabled());
		Assert.assertTrue("In the Database should be marked as Enabled", configData.isEnabled());
	}

	@Test
	public void testChangeStatusOfPermaStoreConfigEnableConfigTwo() throws PermaStoreConfigurationException, ConfigPersistenceException {
		IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();

		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("lodnum");
		
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfig);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);
		// Load from Database
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), permaStoreConfig.getName(),
				PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
		Assert.assertNotNull("ConfigNodeData should not be null", nodeData);

		RequestContext requestContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		ConfigurationContext configurationContextReq=requestContext.getConfigurationContext();
		psConfigService.changeStatusOfPermaStoreConfig(configurationContextReq, permaStoreConfig.getName(), true);
		

		// Enabled should be reloaded into the Cache
		ConfigurationUnit cachedObj = checkCacheForConfig(permaStoreConfig.getName());
		Assert.assertNotNull("Object should be reloaded in DataGrid if Config is Enabled", cachedObj);
		Assert.assertNotNull("Cached Data in Configuration unit should not be Null", cachedObj.getConfigData());

		// Enable should be marked as Enable in Database
		ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
		Assert.assertNotNull("Object should exist in DataBase if config is enabled", configData);
		logger.debug("testChangeStatusOfPermaStoreConfigEnable() " + configData.isEnabled());
		Assert.assertTrue("In the Database should be marked as Enabled", configData.isEnabled());
	}

	@Test
	public void testUpdatePermStoreConfigurationIsEnablefalse() throws ConfigPersistenceException, PermaStoreConfigurationException,
			PermaStoreConfigParserException {

		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("lodnum");
		Assert.assertNotNull("permaStoreConfig should not be null", permaStoreConfig);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);

		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), permaStoreConfig.getName(),
				PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
		ConfigurationContext configContextUpdate=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);

		IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
		if (nodeData != null) {
			ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

			CustomBuilder customBuilder = new CustomBuilder();

			customBuilder.setBuilder("com.getusroi.permastore.config.IPermaStoreCustomCacheObjectBuilder");
			configurationBuilder.setType(ConfigurationBuilderType.valueOf(PermaStoreConfigurationConstant.CONFIGBUILDER_CUSTOM));
			configurationBuilder.setCustomBuilder(customBuilder);
			permaStoreConfig.setConfigurationBuilder(configurationBuilder);
			permaStoreConfig.setIsEnabled(false);
			int i = psConfigService.updatePermaStoreConfiguration(configContextUpdate, permaStoreConfig,
					nodeData.getNodeDataId());

			Assert.assertTrue(i == 1);

			// disable should be delete from the Cache
			ConfigurationUnit cachedObj = checkCacheForConfig(permaStoreConfig.getName());

			Assert.assertNull("Object from Cache should be Deleted if Config is Disabled ", cachedObj);

			// to checck updated data
			ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
			Assert.assertNotNull("Object should exist in DataBase if config is disabled", configData);
			// check config isenabled is set false
			Assert.assertFalse(configData.isEnabled());

		} else {
			Assert.fail("No ConfigNodeData found in DB for NodeId=" + GenericTestConstant.getVendorConfigNodeId());
		}
	}

	@Test
	public void testUpdatePermStoreConfigurationForCustomBuilder() throws ConfigPersistenceException, PermaStoreConfigurationException,
			PermaStoreConfigParserException {

		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("lodnum");
		
		Assert.assertNotNull("permaStoreConfig should not be null", permaStoreConfig);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), permaStoreConfig.getName(),
				PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);

		IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
		if (nodeData != null) {
			ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

			CustomBuilder customBuilder = new CustomBuilder();

			customBuilder.setBuilder("com.getusroi.permastore.config.PICAddressBuilder");
			configurationBuilder.setType(ConfigurationBuilderType.valueOf(PermaStoreConfigurationConstant.CONFIGBUILDER_CUSTOM));
			configurationBuilder.setCustomBuilder(customBuilder);
			permaStoreConfig.setConfigurationBuilder(configurationBuilder);
			permaStoreConfig.setDataType("Map");
			int i = psConfigService.updatePermaStoreConfiguration(configContext, permaStoreConfig,
					nodeData.getNodeDataId());

			Assert.assertTrue(i == 1);

			// check builder type is updated or not
			// to checck updated data
			ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
			Assert.assertNotNull("Object should exist in DataBase if config is enabled", configData);
			// check config isenabled is set true
			Assert.assertTrue(configData.isEnabled());

			// disable should be delete from the Cache
			ConfigurationUnit cachedObj = checkCacheForConfig(permaStoreConfig.getName());

			Assert.assertNotNull("Object from Cache should be reloaded if Config is enabled", cachedObj);
			Assert.assertNotNull("Cached Data in Configuration unit should not be Null", cachedObj.getConfigData());

			Assert.assertTrue(cachedObj instanceof PermaStoreConfigurationUnit);
			PermaStoreConfigurationUnit permaStoreConfigurationUnit = (PermaStoreConfigurationUnit) cachedObj;

			Assert.assertNull("permastoreonfigration should be null in Cache  (permaStoreConfigurationUnit.getPermaStoreConfig()) ",
					permaStoreConfigurationUnit.getPermaStoreConfig());

			PermaStoreConfigXMLParser builder = new PermaStoreConfigXMLParser();
			PermaStoreConfigurations permaStoreConfigurations = builder.marshallXMLtoObject(configData.getConfigData());

			Assert.assertNotNull("permaStoreConfiguration is not null  ", permaStoreConfigurations.getPermaStoreConfiguration());

			PermaStoreConfiguration permaStoreConfiguration = (PermaStoreConfiguration) permaStoreConfigurations.getPermaStoreConfiguration().get(0);

			// check updating value
			Assert.assertEquals(configurationBuilder.getCustomBuilder().getBuilder().trim(), permaStoreConfiguration.getConfigurationBuilder().getCustomBuilder()
					.getBuilder().trim());
			Assert.assertEquals(configurationBuilder.getType(), permaStoreConfiguration.getConfigurationBuilder().getType());

		} else {
			Assert.fail("No ConfigNodeData found in DB for NodeId=" + GenericTestConstant.getVendorConfigNodeId());
		}
	}

	@Test
	public void testUpdatePermStoreConfigurationForinilneBuilder() throws ConfigPersistenceException, PermaStoreConfigurationException,
			PermaStoreConfigParserException {

		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("lodnum");
		
		Assert.assertNotNull("permaStoreConfig should not be null", permaStoreConfig);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);

		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), permaStoreConfig.getName(),
				PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);

		IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
		if (nodeData != null) {
			ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

			InlineBuilder inlineBuilder = new InlineBuilder();

			configurationBuilder.setType(ConfigurationBuilderType.valueOf(PermaStoreConfigurationConstant.CONFIGBUILDER_INLINE));

			String inlineBuilderValue = "   {\"variable\" :\"" + permaStoreConfig.getName() + "\",\"locale_id\":\"US_ENGLISH\"}";

			inlineBuilder.setType(PermaStoreConfigurationConstant.INLINE_CONFIGBUILDER_JSONTOMAP);
			inlineBuilder.setValue(inlineBuilderValue);
			configurationBuilder.setInlineBuilder(inlineBuilder);
			permaStoreConfig.setConfigurationBuilder(configurationBuilder);
			permaStoreConfig.setDataType("List");
			int i = psConfigService.updatePermaStoreConfiguration(configContext, permaStoreConfig,
					nodeData.getNodeDataId());

			Assert.assertTrue(i == 1);

			// check builder type is updated or not
			// to checck updated data
			ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
			Assert.assertNotNull("Object should exist in DataBase if config is enabled", configData);
			// check config isenabled is set true
			Assert.assertTrue(configData.isEnabled());

			ConfigurationUnit cachedObj = checkCacheForConfig(permaStoreConfig.getName());

			Assert.assertNotNull("Object from Cache should be reloaded if Config is enabled", cachedObj);
			Assert.assertNotNull("Cached Data in Configuration unit should not be Null", cachedObj.getConfigData());

			Assert.assertTrue(cachedObj instanceof PermaStoreConfigurationUnit);

			PermaStoreConfigurationUnit permaStoreConfigurationUnit = (PermaStoreConfigurationUnit) cachedObj;

			Assert.assertNull("permastoreonfigration should be null in Cache  (permaStoreConfigurationUnit.getPermaStoreConfig()) ",
					permaStoreConfigurationUnit.getPermaStoreConfig());

			PermaStoreConfigXMLParser builder = new PermaStoreConfigXMLParser();
			PermaStoreConfigurations permaStoreConfigurations = builder.marshallXMLtoObject(configData.getConfigData());

			Assert.assertNotNull("permaStoreConfiguration is not null  ", permaStoreConfigurations.getPermaStoreConfiguration());

			PermaStoreConfiguration permaStoreConfiguration = (PermaStoreConfiguration) permaStoreConfigurations.getPermaStoreConfiguration().get(0);

			// check updating value
			Assert.assertEquals(configurationBuilder.getInlineBuilder().getType(), permaStoreConfiguration.getConfigurationBuilder().getInlineBuilder().getType());
			Assert.assertEquals(configurationBuilder.getType(), permaStoreConfiguration.getConfigurationBuilder().getType());

		} else {
			Assert.fail("No ConfigNodeData found in DB for NodeId=" + GenericTestConstant.getVendorConfigNodeId());
		}
	}

	@Test
	public void testDeletePermaStoreConfiguration() throws PermaStoreConfigurationException, ConfigPersistenceException {
		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("lodnum");
		
		Assert.assertNotNull("permaStoreConfig should not be null", permaStoreConfig);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), permaStoreConfig.getName(),
				PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
		if (nodeData != null) {
			IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();

			RequestContext requestContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
					GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
			ConfigurationContext configurationContextDel=requestContext.getConfigurationContext();
			// ConfigNodeData configNodeData=nodeDataList.get(0);
			psConfigService.deletePermaStoreConfiguration(configurationContextDel, nodeData.getConfigName());

			// Now Check DB and Cache
			Object cachedObject = checkCacheForConfig(nodeData.getConfigName());
			Assert.assertNull("Object from Cache should be Deleted", cachedObject);
			ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
			Assert.assertNull("Object from DataBase should be Deleted", configData);

		} else {
			Assert.fail("No ConfigNodeData found in DB for NodeId=" + GenericTestConstant.getVendorConfigNodeId());
		}

	}

	@Test
	public void testDeletePermaStoreConfigurationIfNull() throws PermaStoreConfigurationException, ConfigPersistenceException {
		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("AreaListSecond");
		Assert.assertNotNull("PermaStoreConfiguration should not be null ", permaStoreConfig);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);
		logger.debug(permaStoreConfig.getName());
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), permaStoreConfig.getName(),
				PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
		if (nodeData != null) {
			IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
			// Now Check DB and Cache
			Object cachedObject = checkCacheForConfig(nodeData.getConfigName());
			Assert.assertNull("Object from Cache should be Deleted", cachedObject);

			RequestContext requestContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
					GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
			ConfigurationContext configurationContextDel=requestContext.getConfigurationContext();
			// ConfigNodeData configNodeData=nodeDataList.get(0);
			psConfigService.deletePermaStoreConfiguration(configurationContextDel, nodeData.getConfigName());
			
			Assert.assertNotNull("ConfigName  Should Not Be Null ", nodeData.getConfigName());

			// Now Check DB and Cache
			cachedObject = checkCacheForConfig(nodeData.getConfigName());
			Assert.assertNull("Object from Cache should be Deleted", cachedObject);

			ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
			Assert.assertNull("Object from DataBase should be Deleted", configData);

		} else {
			Assert.fail("No ConfigNodeData found in DB for NodeId=" + GenericTestConstant.getVendorConfigNodeId());
		}

	}

	//to check givenConfig Exist in DB or not 
    @Test	
	public void checkPermastoreExistorinDbAndCacheAndIfConfigEnabled() throws PermaStoreConfigurationException, ConfigPersistenceException, PermaStoreConfigRequestException{
    		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("lodnum");
    		
    		Assert.assertNotNull("permaStoreConfig should not be null", permaStoreConfig);
    		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
    		// add permastore config to DB and loaad To cache
    		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);
    		RequestContext permaStoreRequestContext=new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
    		ConfigurationContext configurationContextCheckPerma=permaStoreRequestContext.getConfigurationContext();
    		boolean sucuss=	psConfigService.checkPermaStoreConfigarationExistOrNot(configurationContextCheckPerma, "lodnum");
    	
    	Assert.assertTrue(sucuss);
	}
     
     
     /**
      * to test if data Exist Database and it in enabled status , but data not Exist in cache so load the data to cache , check wether data loaded to cache or not
      * @throws PermaStoreConfigurationException
      * @throws ConfigPersistenceException
      * @throws PermaStoreConfigRequestException
      */
    @Test	
 	public void checkPermastoreExistorinDbAndCache() throws PermaStoreConfigurationException, ConfigPersistenceException, PermaStoreConfigRequestException{
     		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("AreaListThree");
     		
     		Assert.assertNotNull("permaStoreConfig should not be null", permaStoreConfig);
     		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
     		// add permastore config to DB and loaad To cache
     		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);
     		RequestContext permaStoreRequestContext=new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
         	ConfigurationContext configurationContextCheckPerma=permaStoreRequestContext.getConfigurationContext();
     		ConfigurationUnit cachedObj = checkCacheForConfig(permaStoreConfig.getName());
     		Assert.assertNull("configaraution in cache Should Be Null ",cachedObj);
     		//To enable config in database
    		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
    		
    		ConfigNodeData configNodeData =pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), permaStoreConfig.getName(), PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
    			Assert.assertNotNull("configNodedata should Not be null ",configNodeData);

    				pesrsistence.enableConfigNodeData(true,configNodeData.getNodeDataId());
    		
     		boolean sucuss=	psConfigService.checkPermaStoreConfigarationExistOrNot(configurationContextCheckPerma, "AreaListThree");
     		Assert.assertTrue(sucuss);
         	 cachedObj = checkCacheForConfig(permaStoreConfig.getName());
      		Assert.assertNotNull("configaraution in cache Should not Be Null ",cachedObj);





 	}
     
     //To check permastoreConfigNotExist In DB 
     @Test	
  	public void checkIfPermastoreConfigNotExists() throws PermaStoreConfigurationException, ConfigPersistenceException, PermaStoreConfigRequestException{
      		RequestContext permaStoreRequestContext=new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME);
          	ConfigurationContext configurationContextCheckPerma=permaStoreRequestContext.getConfigurationContext();

      		boolean sucuss=	psConfigService.checkPermaStoreConfigarationExistOrNot(configurationContextCheckPerma, "AreaListSecond");
      		//to check config not EXist in Db and Cache
      		Assert.assertFalse(sucuss);
          	





  	}
	private ConfigurationUnit checkCacheForConfig(String configName) {
		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ PermaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 261
		logger.debug("GroupLevel Map=" + map);
		ConfigurationUnit configUnit =(ConfigurationUnit) map.get(configName);

		
		logger.debug("CheckCacheForConfig() ConfigObject is =" + configUnit);
		return configUnit;

	}

	private ConfigNodeData checkDBForConfig(String configName, int nodeId) throws ConfigPersistenceException {
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData configData = pesrsistence.getConfigNodeDatabyNameAndNodeId(nodeId, configName, PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
		return configData;
	}

	private PermaStoreConfiguration getPermastoreConfigByName(String name) {
		for (PermaStoreConfiguration permaStoreConfig : permaStoreConfigList) {
			if (permaStoreConfig.getName().equalsIgnoreCase(name))
				return permaStoreConfig;
		}
		return null;
	}

}
