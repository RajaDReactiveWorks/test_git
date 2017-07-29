package com.getusroi.dynastore.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

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
import com.getusroi.dynastore.config.impl.DynaStoreConfigXmlParser;
import com.getusroi.dynastore.config.impl.DynaStoreConfigurationService;
import com.getusroi.dynastore.config.jaxb.DynastoreConfiguration;
import com.getusroi.dynastore.config.jaxb.DynastoreConfigurations;
import com.hazelcast.core.HazelcastInstance;

public class DynaStoreInitializationTest {
	final Logger logger = LoggerFactory.getLogger(DynaStoreInitializationTest.class);
	List<DynastoreConfiguration> dynaStolreConfigList;
	IDynaStoreConfigurationService iDynaStoreConfigurationService;
	IConfigPersistenceService configPersistenceService;

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
	public void loadConfigurations() throws DynaStoreConfigurationException, DynaStoreConfigParserException, ConfigPersistenceException {

		DynastoreConfigurations dynastoreConfigurations = getDynaStoreconfiguration();
		if (iDynaStoreConfigurationService == null)
			iDynaStoreConfigurationService = new DynaStoreConfigurationService();

		if (configPersistenceService == null)
			configPersistenceService = new ConfigPersistenceServiceMySqlImpl();

		configPersistenceService.deleteConfigNodeDataByNodeId(GenericTestConstant.getConfigNodeId());
		configPersistenceService.deleteConfigNodeDataByNodeId(GenericTestConstant.getVendorConfigNodeId());
		dynaStolreConfigList = dynastoreConfigurations.getDynastoreConfiguration();

	}
	
	private DynastoreConfiguration getDynaStoreConfiguration(String dynaConfigName){
			for(DynastoreConfiguration dynaConfig:dynaStolreConfigList){
				if(dynaConfig.getDynastoreName().getValue().equalsIgnoreCase(dynaConfigName)){
					return dynaConfig;
				}
			}
			return null;
	}

	@Test
	public void testNoInitializerConfiguration() throws DynaStoreConfigurationException, DynaStoreConfigRequestContextException{
		DynastoreConfiguration dynaConfig=getDynaStoreConfiguration("PicArea_NoInitializer");
		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynaConfig);
		
		ConfigurationContext configurationContext = getConfigurationContext(5);
		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynaConfig);
		
		DynaStoreConfigurationUnit dynaConfigUnit=iDynaStoreConfigurationService.getDynaStoreConfigurationUnit(getRequestContext(), "PicArea_NoInitializer", "1.0");
		Assert.assertNotNull("ConfigurationUnit for PicAreaTwo should not be null ", dynaConfigUnit);
		
		String dynaCollectionId=dynaConfigUnit.getDynaCollectionId();
		Assert.assertNotNull("dynaCollectionId for PicAreaTwo should not be null ", dynaCollectionId);
		
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		Map dynaCol=hazelcastInstance.getMap(dynaCollectionId);
		Assert.assertTrue("Map should be empty for uninitiazed Dynastroe",dynaCol.isEmpty());
		
	}
	@Test
	public void testCustomInitializerConfiguration() throws DynaStoreConfigurationException, DynaStoreConfigRequestContextException{
		DynastoreConfiguration dynaConfig=getDynaStoreConfiguration("PicArea");
		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynaConfig);
		
		ConfigurationContext configurationContext = getConfigurationContext(5);
		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynaConfig);
		
		DynaStoreConfigurationUnit dynaConfigUnit=iDynaStoreConfigurationService.getDynaStoreConfigurationUnit(getRequestContext(), "PicArea", "1.0");
		Assert.assertNotNull("ConfigurationUnit for PicAreaTwo should not be null ", dynaConfigUnit);
		
		String dynaCollectionId=dynaConfigUnit.getDynaCollectionId();
		Assert.assertNotNull("dynaCollectionId for PicAreaTwo should not be null ", dynaCollectionId);
		
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		Map dynaCol=hazelcastInstance.getMap(dynaCollectionId);
		int size=dynaCol.size();
		logger.debug("Map size{"+size+"} and Map is ="+dynaCol);
		Assert.assertTrue("Map should have entry more than one to signify it was initialized for Dynastroe{PicArea}",size>1);
		
	}
	
	@Test
	public void testInlineInitializerConfiguration() throws DynaStoreConfigurationException, DynaStoreConfigRequestContextException{
		DynastoreConfiguration dynaConfig=getDynaStoreConfiguration("PicArea-INLINE-json");
		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynaConfig);
		
		ConfigurationContext configurationContext = getConfigurationContext(5);
		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynaConfig);
		
		DynaStoreConfigurationUnit dynaConfigUnit=iDynaStoreConfigurationService.getDynaStoreConfigurationUnit(getRequestContext(), "PicArea-INLINE-json", "1.0");
		Assert.assertNotNull("ConfigurationUnit for PicArea-INLINE-json should not be null ", dynaConfigUnit);
		
		String dynaCollectionId=dynaConfigUnit.getDynaCollectionId();
		Assert.assertNotNull("dynaCollectionId for PicArea-INLINE-json should not be null ", dynaCollectionId);
		
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		Map dynaCol=hazelcastInstance.getMap(dynaCollectionId);
		int size=dynaCol.size();
		logger.debug("Map size{"+size+"} and Map is ="+dynaCol);
		Assert.assertTrue("Map should have entry more than one to signify it was initialized for Dynastroe",size>1);
		
	}
	
	@Test
	public void testSQLWithMapMaaperInitializerConfiguration() throws DynaStoreConfigurationException, DynaStoreConfigRequestContextException{
		DynastoreConfiguration dynaConfig=getDynaStoreConfiguration("PicAreaSQL-List-OF-Mapper");
		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynaConfig);
		
		ConfigurationContext configurationContext = getConfigurationContext(5);
		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynaConfig);
		
		DynaStoreConfigurationUnit dynaConfigUnit=iDynaStoreConfigurationService.getDynaStoreConfigurationUnit(getRequestContext(), "PicAreaSQL-List-OF-Mapper", "1.0");
		Assert.assertNotNull("ConfigurationUnit for PicAreaSQL-List-OF-Mapper should not be null ", dynaConfigUnit);
		
		String dynaCollectionId=dynaConfigUnit.getDynaCollectionId();
		Assert.assertNotNull("dynaCollectionId for PicAreaSQL-List-OF-Mapper should not be null ", dynaCollectionId);
		
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		Map dynaCol=hazelcastInstance.getMap(dynaCollectionId);
		int size=dynaCol.size();
		logger.debug("testSQLWithMapMaaperInitializerConfiguration Map size{"+size+"} and Map is ="+dynaCol.entrySet());
		Assert.assertTrue(".Map should have entry more than one to signify it was initialized for Dynastroe",size>1);
		
	}
	
	@Test
	public void testSQLWithCustomMaaperInitializerConfiguration() throws DynaStoreConfigurationException, DynaStoreConfigRequestContextException{
		DynastoreConfiguration dynaConfig=getDynaStoreConfiguration("PicAreaSQL-Custom-Mapper");
		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynaConfig);
		
		ConfigurationContext configurationContext = getConfigurationContext(5);
		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynaConfig);
		
		DynaStoreConfigurationUnit dynaConfigUnit=iDynaStoreConfigurationService.getDynaStoreConfigurationUnit(getRequestContext(), "PicAreaSQL-Custom-Mapper", "1.0");
		Assert.assertNotNull("ConfigurationUnit for PicAreaSQL-Custom-Mapper should not be null ", dynaConfigUnit);
		
		String dynaCollectionId=dynaConfigUnit.getDynaCollectionId();
		Assert.assertNotNull("dynaCollectionId for PicAreaSQL-Custom-Mapper should not be null ", dynaCollectionId);
		
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		Map dynaCol=hazelcastInstance.getMap(dynaCollectionId);
		int size=dynaCol.size();
		logger.debug("testSQLWithCustomMaaperInitializerConfiguration Map size{"+size+"} and Map is ="+dynaCol.entrySet());
		Assert.assertTrue(".Map should have entry more than one to signify it was initialized for Dynastroe",size>1);
		
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
		configContext.setRequestId(GenericTestConstant.TEST_REQUEST_ID);
		return configContext;
	}
}
