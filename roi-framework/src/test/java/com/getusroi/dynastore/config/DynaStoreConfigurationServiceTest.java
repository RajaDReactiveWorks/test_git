package com.getusroi.dynastore.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.config.persistence.ConfigNodeData;
import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.getusroi.config.util.GenericTestConstant;
import com.getusroi.core.datagrid.DataGridService;
import com.getusroi.dynastore.config.impl.DynaStoreConfigXmlParser;
import com.getusroi.dynastore.config.impl.DynaStoreConfigurationService;
import com.getusroi.dynastore.config.jaxb.DynastoreConfiguration;
import com.getusroi.dynastore.config.jaxb.DynastoreConfigurations;
import com.getusroi.dynastore.config.jaxb.DynastoreName;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class DynaStoreConfigurationServiceTest {

	final Logger logger = LoggerFactory.getLogger(DynaStoreConfigurationServiceTest.class);
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

	@Test
	public void addDynaStoreConfiguration() throws DynaStoreConfigurationException, ConfigPersistenceException {
		DynastoreConfiguration dynastoreConfiguration = null;
		for (Iterator iterator = dynaStolreConfigList.iterator(); iterator.hasNext();) {
			DynastoreConfiguration dynastoreConfig = (DynastoreConfiguration) iterator.next();
			if (dynastoreConfig.getDynastoreName().getValue().equalsIgnoreCase("PicArea")) {
				dynastoreConfiguration = dynastoreConfig;
				break;
			}

		}
		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynastoreConfiguration);
		ConfigurationContext configurationContext = getConfigurationContext(5);
		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynastoreConfiguration);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(),
				dynastoreConfiguration.getDynastoreName().getValue(), DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);

		Assert.assertNotNull("dynastoreConfiguration in the DB should not be null ", loadedConfigNodeData);
		Assert.assertEquals("Access Scope must be Feature ", "Feature", dynastoreConfiguration.getAccessScope().getGetter());
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ DynaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861

		DynastoreName dynastoreName = dynastoreConfiguration.getDynastoreName();
		logger.debug("map datat ==========" + map);
		String dynaStoreKey = getDynaStoreKey(dynastoreName);
		DynaStoreConfigurationUnit dynastoreConfigurationUnitCacheObject = (DynaStoreConfigurationUnit) map.get(dynaStoreKey);

		Assert.assertNotNull("the configuration objcet in cache should be not be null ", dynastoreConfigurationUnitCacheObject);
		DynastoreConfiguration dynastoreConfigurationCacheObject = (DynastoreConfiguration) dynastoreConfigurationUnitCacheObject.getDynastoreConfiguration();
		Assert.assertEquals("the configuration name should be same ", "PicArea", dynastoreConfigurationCacheObject.getDynastoreName().getValue());
		
		Serializable object=dynastoreConfigurationUnitCacheObject.getConfigData();
		Assert.assertNotNull("Intialized object in dynastore should not be null", object);
	}

	@Test
	public void TestgetDynaStoreConfiguration() throws DynaStoreConfigurationException, ConfigPersistenceException, DynaStoreConfigRequestContextException {
		DynastoreConfiguration dynastoreConfiguration = null;
		for (Iterator iterator = dynaStolreConfigList.iterator(); iterator.hasNext();) {
			DynastoreConfiguration dynastoreConfig = (DynastoreConfiguration) iterator.next();
			if (dynastoreConfig.getDynastoreName().getValue().equalsIgnoreCase("PicArea")) {
				dynastoreConfiguration = dynastoreConfig;
				break;
			}

		}
		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynastoreConfiguration);
		ConfigurationContext configurationContext = getConfigurationContext(5);
		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynastoreConfiguration);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(),
				dynastoreConfiguration.getDynastoreName().getValue(), DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);
		RequestContext dynaStoreConfigRequestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(), configurationContext.getFeatureName(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		Assert.assertNotNull("dynastoreConfiguration in the DB should not be null ", loadedConfigNodeData);
		DynastoreConfiguration dynastoreConfigurationCacheObject = iDynaStoreConfigurationService.getDynaStoreConfiguration(dynaStoreConfigRequestContext,
				dynastoreConfiguration.getDynastoreName().getValue(),GenericTestConstant.TEST_VERSION);

		Assert.assertEquals("the configuration name should be same ", "PicArea", dynastoreConfigurationCacheObject.getDynastoreName().getValue());
	}

	@Test
	public void TestChangeStatusOfDynaStoreConfigurationToDisable() throws DynaStoreConfigurationException, ConfigPersistenceException {
		DynastoreConfiguration dynastoreConfiguration = null;
		for (Iterator iterator = dynaStolreConfigList.iterator(); iterator.hasNext();) {
			DynastoreConfiguration dynastoreConfig = (DynastoreConfiguration) iterator.next();
			if (dynastoreConfig.getDynastoreName().getValue().equalsIgnoreCase("PicArea")) {
				dynastoreConfiguration = dynastoreConfig;
				break;
			}

		}
		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynastoreConfiguration);
		ConfigurationContext configurationContext = getConfigurationContext(5);
		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynastoreConfiguration);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(),
				dynastoreConfiguration.getDynastoreName().getValue(), DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);

		Assert.assertNotNull("dynastoreConfiguration in the DB should not be null ", loadedConfigNodeData);
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ DynaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861

		DynastoreName dynastoreName = dynastoreConfiguration.getDynastoreName();
		String dynaStoreKey = getDynaStoreKey(dynastoreName);
		DynaStoreConfigurationUnit dynastoreConfigurationUnitCacheObject = (DynaStoreConfigurationUnit) map.get(dynaStoreKey);

		Assert.assertNotNull("the configuration objcet in cache should be not be null ", dynastoreConfigurationUnitCacheObject);
		DynastoreConfiguration dynastoreConfigurationCacheObject = (DynastoreConfiguration) dynastoreConfigurationUnitCacheObject.getDynastoreConfiguration();
		Assert.assertEquals("the configuration name should be same ", "PicArea", dynastoreConfigurationCacheObject.getDynastoreName().getValue());
		RequestContext dynaStoreConfigRequestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(), configurationContext.getFeatureName(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);

		boolean isDisabled = iDynaStoreConfigurationService.changeStatusOfDynaStoreConfiguration(dynaStoreConfigRequestContext, dynastoreName.getValue(),
				dynastoreName.getVersion(), false);

		Assert.assertTrue("the configuration must be disabled ", isDisabled);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), dynastoreConfiguration
				.getDynastoreName().getValue(), DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);

		Assert.assertFalse("the configuration must be disabled In DB ", loadedConfigNodeData.isEnabled());
		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ DynaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861

		dynastoreConfigurationUnitCacheObject = (DynaStoreConfigurationUnit) map.get(dynaStoreKey);

		Assert.assertNull("the configuration objcet in cache should  be null ", dynastoreConfigurationUnitCacheObject);

	}

	@Test
	public void TestChangeStatusOfDynaStoreConfigurationToEnable() throws DynaStoreConfigurationException, ConfigPersistenceException {
		DynastoreConfiguration dynastoreConfiguration = null;
		for (Iterator iterator = dynaStolreConfigList.iterator(); iterator.hasNext();) {
			DynastoreConfiguration dynastoreConfig = (DynastoreConfiguration) iterator.next();
			if (dynastoreConfig.getDynastoreName().getValue().equalsIgnoreCase("PicAreaTwo")) {
				dynastoreConfiguration = dynastoreConfig;
				break;
			}

		}

		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynastoreConfiguration);
		ConfigurationContext configurationContext = getConfigurationContext(5);
		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynastoreConfiguration);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(),
				dynastoreConfiguration.getDynastoreName().getValue(), DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);

		Assert.assertNotNull("dynastoreConfiguration in the DB should not be null ", loadedConfigNodeData);
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ DynaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861

		DynastoreName dynastoreName = dynastoreConfiguration.getDynastoreName();
		String dynaStoreKey = getDynaStoreKey(dynastoreName);
		DynaStoreConfigurationUnit dynastoreConfigurationUnitCacheObject = (DynaStoreConfigurationUnit) map.get(dynaStoreKey);

		Assert.assertNull("the configuration objcet in cache should be null ", dynastoreConfigurationUnitCacheObject);

		RequestContext dynaStoreConfigRequestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(), configurationContext.getFeatureName(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);

		boolean isEnable = iDynaStoreConfigurationService.changeStatusOfDynaStoreConfiguration(dynaStoreConfigRequestContext, dynastoreName.getValue(),
				dynastoreName.getVersion(), true);

		Assert.assertTrue("the configuration must be enabled ", isEnable);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), dynastoreConfiguration
				.getDynastoreName().getValue(), DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);

		Assert.assertTrue("the configuration must be enabled In DB ", loadedConfigNodeData.isEnabled());
		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ DynaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861

		dynastoreConfigurationUnitCacheObject = (DynaStoreConfigurationUnit) map.get(dynaStoreKey);

		Assert.assertNotNull("the configuration objcet in cache should not  be null ", dynastoreConfigurationUnitCacheObject);

		DynastoreConfiguration dynastoreConfigurationCacheObject = (DynastoreConfiguration) dynastoreConfigurationUnitCacheObject.getDynastoreConfiguration();
		Assert.assertEquals("the configuration name should be same ", "PicAreaTwo", dynastoreConfigurationCacheObject.getDynastoreName().getValue());

	}

	@Test
	public void TestDeleteDynaStoreConfigurationIfConfigisEnable() throws DynaStoreConfigurationException, ConfigPersistenceException {
		DynastoreConfiguration dynastoreConfiguration = null;
		for (Iterator iterator = dynaStolreConfigList.iterator(); iterator.hasNext();) {
			DynastoreConfiguration dynastoreConfig = (DynastoreConfiguration) iterator.next();
			if (dynastoreConfig.getDynastoreName().getValue().equalsIgnoreCase("PicArea")) {
				dynastoreConfiguration = dynastoreConfig;
				break;
			}

		}

		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynastoreConfiguration);
		ConfigurationContext configurationContext = getConfigurationContext(5);
		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynastoreConfiguration);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(),
				dynastoreConfiguration.getDynastoreName().getValue(), DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);

		Assert.assertNotNull("dynastoreConfiguration in the DB should not be null ", loadedConfigNodeData);
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ DynaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861

		DynastoreName dynastoreName = dynastoreConfiguration.getDynastoreName();
		String dynaStoreKey = getDynaStoreKey(dynastoreName);
		DynaStoreConfigurationUnit dynastoreConfigurationUnitCacheObject = (DynaStoreConfigurationUnit) map.get(dynaStoreKey);

		Assert.assertNotNull("the configuration objcet in cache should be not be null ", dynastoreConfigurationUnitCacheObject);
		DynastoreConfiguration dynastoreConfigurationCacheObject = (DynastoreConfiguration) dynastoreConfigurationUnitCacheObject.getDynastoreConfiguration();
		Assert.assertEquals("the configuration name should be same ", "PicArea", dynastoreConfigurationCacheObject.getDynastoreName().getValue());
		RequestContext dynaStoreConfigRequestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(), configurationContext.getFeatureName(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);

		boolean isDeleted = iDynaStoreConfigurationService.deleteDynaStoreConfiguration(dynaStoreConfigRequestContext, dynastoreName.getValue(),
				dynastoreName.getVersion());

		Assert.assertTrue("the configuration must be deleted ", isDeleted);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), dynastoreConfiguration
				.getDynastoreName().getValue(), DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);

		Assert.assertNull("the configuration must be deleted In DB ", loadedConfigNodeData);

		map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ DynaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861

		dynastoreConfigurationUnitCacheObject = (DynaStoreConfigurationUnit) map.get(dynaStoreKey);

		Assert.assertNull("the configuration objcet in cache should  be null ", dynastoreConfigurationUnitCacheObject);

	}

	@Test
	public void TestDeleteDynaStoreConfigurationIfConfigisDisable() throws DynaStoreConfigurationException, ConfigPersistenceException {
		DynastoreConfiguration dynastoreConfiguration = null;
		for (Iterator iterator = dynaStolreConfigList.iterator(); iterator.hasNext();) {
			DynastoreConfiguration dynastoreConfig = (DynastoreConfiguration) iterator.next();
			if (dynastoreConfig.getDynastoreName().getValue().equalsIgnoreCase("PicAreaTwo")) {
				dynastoreConfiguration = dynastoreConfig;
				break;
			}

		}

		Assert.assertNotNull("requested dynastoreconfiguration in list should be Exist", dynastoreConfiguration);
		ConfigurationContext configurationContext = getConfigurationContext(5);
		iDynaStoreConfigurationService.addDynaStoreConfiguration(configurationContext, dynastoreConfiguration);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(),
				dynastoreConfiguration.getDynastoreName().getValue(), DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);

		Assert.assertNotNull("dynastoreConfiguration in the DB should not be null ", loadedConfigNodeData);
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ DynaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861

		DynastoreName dynastoreName = dynastoreConfiguration.getDynastoreName();
		String dynaStoreKey = getDynaStoreKey(dynastoreName);
		DynaStoreConfigurationUnit dynastoreConfigurationUnitCacheObject = (DynaStoreConfigurationUnit) map.get(dynaStoreKey);

		Assert.assertNull("the configuration objcet in cache should be null ", dynastoreConfigurationUnitCacheObject);

		RequestContext dynaStoreConfigRequestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(), configurationContext.getFeatureName(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);

		boolean isDeleted = iDynaStoreConfigurationService.deleteDynaStoreConfiguration(dynaStoreConfigRequestContext, dynastoreName.getValue(),
				dynastoreName.getVersion());

		Assert.assertTrue("the configuration must be deleted ", isDeleted);

		loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), dynastoreConfiguration
				.getDynastoreName().getValue(), DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);

		Assert.assertNull("the configuration must be deleted In DB ", loadedConfigNodeData);

	}

	private ConfigurationContext getConfigurationContext(int level) {
		ConfigurationContext configContext = null;		
		if (level == 5)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), GenericTestConstant.getFeatureGroup(),
					GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		if (level == 2)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite());
		return configContext;

	}

	private static String getDynaStoreKey(DynastoreName dynastoreName) {

		String dynaStoreName = dynastoreName.getValue();

		if (dynastoreName.getVersion() != null && !dynastoreName.getVersion().isEmpty())
			return dynaStoreName + "-" + dynastoreName.getVersion();

		return dynaStoreName + "-" + DynaStoreConfigurationUnit.DYNASTORECONFIG_DEFULAT_VERSION;
	}
}
