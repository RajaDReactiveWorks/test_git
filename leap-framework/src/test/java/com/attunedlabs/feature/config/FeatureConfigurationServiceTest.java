package com.attunedlabs.feature.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.config.util.GenericTestConstant;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.feature.config.FeatureConfigParserException;
import com.attunedlabs.feature.config.FeatureConfigRequestContext;
import com.attunedlabs.feature.config.FeatureConfigRequestException;
import com.attunedlabs.feature.config.FeatureConfigurationConstant;
import com.attunedlabs.feature.config.FeatureConfigurationException;
import com.attunedlabs.feature.config.FeatureConfigurationUnit;
import com.attunedlabs.feature.config.IFeatureConfigurationService;
import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;
import com.attunedlabs.feature.config.impl.FeatureConfigurationService;
import com.attunedlabs.feature.jaxb.Feature;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.feature.jaxb.GenericRestEndpoint;
import com.attunedlabs.feature.jaxb.Service;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class FeatureConfigurationServiceTest {
	final Logger logger = LoggerFactory.getLogger(FeatureConfigurationServiceTest.class);
	private Feature featureConfigurationList;
	private IFeatureConfigurationService featureConfigService;
	

	/**
	 * This method is used to marshal featureserivce.xml to pojo and return root
	 * object
	 * 
	 * @return FeaturesServiceInfo : return a FeaturesServiceInfo object
	 * @throws FeatureConfigParserException
	 */
	private FeaturesServiceInfo getFeatureConfiguration() throws FeatureConfigParserException {
		FeatureConfigXMLParser parser = new FeatureConfigXMLParser();
		InputStream inputstream = FeatureConfigurationServiceTest.class.getClassLoader().getResourceAsStream(FeatureTestConstant.configfileToParse1);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			    
			}
			reader.close();
		} catch (IOException e) {
		throw new FeatureConfigParserException("feature file doesnot exist in classpath",e);
		}
		
		String featureConfigxml=out1.toString();
		FeaturesServiceInfo featureConfiguration = parser.marshallConfigXMLtoObject(featureConfigxml);
		return featureConfiguration;
	}// end of getFeatureConfiguration method

	/**
	 * This method is called before any other test can executed, purpose is to
	 * load configuration in database and cache
	 * 
	 * @throws FeatureConfigParserException
	 * @throws ConfigPersistenceException
	 * @throws FeatureConfigurationException
	 */
	@Before
	public void loadConfiguration() throws FeatureConfigParserException, ConfigPersistenceException, FeatureConfigurationException {
		FeaturesServiceInfo featureConfiguration = getFeatureConfiguration();
		featureConfigService = new FeatureConfigurationService();
		// Clear all DB Data First for nodeId parcel and label service feature
		IConfigPersistenceService pesrsistence1 = new ConfigPersistenceServiceMySqlImpl();
		pesrsistence1.deleteConfigNodeDataByNodeId(FeatureTestConstant.getConfigNodeId());
		
		pesrsistence1.deleteConfigNodeDataByNodeId(FeatureTestConstant.getConfigNodeId1());
		
		pesrsistence1.deleteConfigNodeDataByNodeId(FeatureTestConstant.TEST_VENDOR_NODEID);
		
		featureConfigurationList = featureConfiguration.getFeatures().getFeature();


	}// end of loadConfiguration test method

	@AfterClass
	public static void removeAllConfigurations() {
		// #TODO logic to write remove all test configuration will be done here,
		// if needed
	}

	/**
	 * This method is used to the the feature uploaded in configuration for
	 * specific tenanat or not
	 * 
	 * @throws FeatureConfigurationException
	 */
	@Test
	public void testFeatureUpload() throws FeatureConfigurationException {
	
		Feature feature=getFeatureByName(FeatureTestConstant.TEST_FEATURE);
		featureConfigService = new FeatureConfigurationService();
		ConfigurationContext confiContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),FeatureTestConstant.TEST_FEATUREGROUP,FeatureTestConstant.TEST_FEATURE,GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
		
		featureConfigService.addFeatureConfiguration(confiContext, feature);
		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-FeatueNodeId-FSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + GenericTestConstant.getSite() + "-"
				+ FeatureConfigurationUnit.getConfigGroupKey(new Integer(FeatureTestConstant.TEST_VENDOR_NODEID)));// 26
		logger.debug("GroupLevel Map=" + map);
		FeatureConfigurationUnit configunit = (FeatureConfigurationUnit) map.get(FeatureTestConstant.TEST_FEATURE);
		Object cachedata=configunit.getConfigData();
		logger.debug("ConfigObject is =" + configunit);
		Assert.assertNotNull("Cached FeatureConfiguration  should not be null", configunit);
		Assert.assertNotNull("Serilizable Object is not null : "+cachedata);
		Assert.assertTrue(cachedata instanceof Feature);
		Feature feat=(Feature)cachedata;
		Assert.assertEquals("feature name is labelservice", feat.getFeatureName(),FeatureTestConstant.TEST_FEATURE);
	}// end of testFeatureUpload method

	@Test
	public void testGetFeatureConfiguration() throws FeatureConfigurationException {
		Feature feature=getFeatureByName(FeatureTestConstant.TEST_FEATURE);
		featureConfigService = new FeatureConfigurationService();
		ConfigurationContext confiContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), FeatureTestConstant.TEST_FEATUREGROUP,FeatureTestConstant.TEST_FEATURE,GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
		
		featureConfigService.addFeatureConfiguration(confiContext, feature);
		FeatureConfigRequestContext requestContext = new FeatureConfigRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				FeatureTestConstant.TEST_FEATUREGROUP,FeatureTestConstant.TEST_FEATURE,GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
		IFeatureConfigurationService fsConfigService = new FeatureConfigurationService();
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		FeatureConfigurationUnit fcu = fsConfigService.getFeatureConfiguration(requestContext,FeatureTestConstant.TEST_FEATURE);
		logger.debug("feature configuration unit : " + fcu);
		Object cachedata=fcu.getConfigData();
		logger.debug("ConfigObject is =" + fcu);
		Assert.assertNotNull("Cached FeatureConfiguration  should not be null", fcu);
		Assert.assertNotNull("Serilizable Object is not null : "+cachedata);
		Assert.assertTrue(cachedata instanceof Feature);
		Feature feat=(Feature)cachedata;
		Assert.assertEquals("feature name is labelservice", feat.getFeatureName(),FeatureTestConstant.TEST_FEATURE);
	}// end of testFeatureUpload method

	/**
	 * This test method is used to remove data from cache as well as from
	 * database
	 * 
	 * @throws ConfigPersistenceException
	 * @throws FeatureConfigurationException
	 */
	@Test
	public void testDeleteFeatureConfiguration() throws ConfigPersistenceException, FeatureConfigurationException {
		Feature feature=getFeatureByName(FeatureTestConstant.TEST_FEATURE);
		featureConfigService = new FeatureConfigurationService();
		ConfigurationContext confiContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), FeatureTestConstant.TEST_FEATUREGROUP,FeatureTestConstant.TEST_FEATURE,GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
		featureConfigService.addFeatureConfiguration(confiContext, feature);
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(FeatureTestConstant.TEST_VENDOR_NODEID, feature.getFeatureName(),
				FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
		if (nodeData != null) {
			IFeatureConfigurationService fsConfigService = new FeatureConfigurationService();

			FeatureConfigRequestContext requestContext = new FeatureConfigRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
					FeatureTestConstant.TEST_FEATUREGROUP, FeatureTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
			ConfigurationContext configContext=requestContext.getConfigurationContext();
			fsConfigService.deleteFeatureConfiguration(configContext, nodeData.getConfigName());

			// Now Check DB and Cache
			Object cachedObject = checkCacheForConfig(nodeData.getConfigName());
			Assert.assertNull("Object from Cache should be Deleted", cachedObject);
			ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
			Assert.assertNull("Object from DataBase should be Deleted", configData);

		} else {
			Assert.fail("No ConfigNodeData found in DB for NodeId=" + FeatureTestConstant.getConfigNodeId());
		}

	}

	/**
	 * This method is used to the the feature uploaded in configuration for
	 * specific tenant or not
	 * 
	 * @throws FeatureConfigurationException
	 */
	@Test
	public void testAddNewVendor() throws FeatureConfigurationException {
		Feature feature=getFeatureByName(FeatureTestConstant.TEST_FEATURE);
		featureConfigService = new FeatureConfigurationService();
		ConfigurationContext confiContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), FeatureTestConstant.TEST_FEATUREGROUP,FeatureTestConstant.TEST_FEATURE,GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
		featureConfigService.addFeatureConfiguration(confiContext, feature);
		IFeatureConfigurationService fsConfigService = new FeatureConfigurationService();
		FeatureConfigRequestContext requestContext = new FeatureConfigRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				FeatureTestConstant.TEST_FEATUREGROUP, FeatureTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
		ConfigurationContext configContextReq=requestContext.getConfigurationContext();
		Service service = new Service();
		service.setName("Draft");
		service.setVendorName("pro-fed");
		service.setEnabled(true);
		service.setDescription("This is pro-fed vendor");
		GenericRestEndpoint restEndpoint=new GenericRestEndpoint();
		restEndpoint.setHttpMethod("POST");
		restEndpoint.setValue("label-parcel-IR");
		service.setGenericRestEndpoint(restEndpoint);
		List<Service>serviceList =feature.getService();
		serviceList.add(service);
		fsConfigService.addNewServiceInFeatureConfiguration(configContextReq, service);
		FeatureConfigurationUnit fcu = fsConfigService.getFeatureConfiguration(requestContext,FeatureTestConstant.TEST_FEATURE);
		logger.debug("feature configuration unit : " + fcu);
		Object cachedata=fcu.getConfigData();
		logger.debug("ConfigObject is =" + fcu);
		Assert.assertNotNull("Cached FeatureConfiguration  should not be null", fcu);
		Assert.assertNotNull("Serilizable Object is not null : "+cachedata);
		Assert.assertTrue(cachedata instanceof Feature);
		Feature feat=(Feature)cachedata;
		Assert.assertEquals("feature name is labelservice", feat.getFeatureName(),FeatureTestConstant.TEST_FEATURE);
		List<Service> serviceList1=feat.getService();
		List<String> serviceNames=new ArrayList<>();
		for(Service service1:serviceList1){
			serviceNames.add(service1.getName());
		}
		Assert.assertTrue(serviceNames.contains("Draft"));
	}// end of testFeatureUpload method
	/**
	 * This test method is used to enabled feature which is disabled, now we
	 * don't have disabled feature defined in feature.xml
	 * 
	 * @throws FeatureConfigurationException
	 * @throws ConfigPersistenceException
	 */
	 //@Test
	public void testChangeStatusOfFeatureConfigEnable() throws FeatureConfigurationException, ConfigPersistenceException {
		 Feature featureConfig=getFeatureByName(FeatureTestConstant.TEST_FEATURE);
			featureConfigService = new FeatureConfigurationService();
			ConfigurationContext confiContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), FeatureTestConstant.TEST_FEATUREGROUP,FeatureTestConstant.TEST_FEATURE,GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
			featureConfigService.addFeatureConfiguration(confiContext, featureConfig);
		 
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		// inLineBuilder Enabling
		// Load from Database
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(FeatureTestConstant.TEST_VENDOR_NODEID, featureConfig.getFeatureName(),
				FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);

		FeatureConfigRequestContext requestContext = new FeatureConfigRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				FeatureTestConstant.TEST_FEATUREGROUP, FeatureTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
		ConfigurationContext configContextReq=requestContext.getConfigurationContext();
		featureConfigService.changeStatusOfFeatureConfig(configContextReq, featureConfig.getFeatureName(), true);
		// Enabled should be reloaded into the Cache
		FeatureConfigurationUnit confiunit =(FeatureConfigurationUnit) checkCacheForConfig(featureConfig.getFeatureName());		
		
		Assert.assertNotNull("Object should be reloaded in DataGrid if Config is Enabled", confiunit);
		Object cachedata=confiunit.getConfigData();
		logger.debug("ConfigObject is =" + confiunit);
		Assert.assertNotNull("Cached FeatureConfiguration  should not be null", confiunit);
		Assert.assertNotNull("Serilizable Object is not null : "+cachedata);
		Assert.assertTrue(cachedata instanceof Feature);
		Feature feat=(Feature)cachedata;
		Assert.assertEquals("feature name is labelservice", feat.getFeatureName(),FeatureTestConstant.TEST_FEATURE);
		// Enable should be marked as Enable in Database
		logger.debug("configNodeName : "+nodeData.getConfigName()+", parentConfigNodeId : "+nodeData.getParentConfigNodeId());
		ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
		Assert.assertNotNull("Object should exist in DataBase if config is enabled", configData);
		logger.debug("testChangeStatusOfPermaStoreConfigEnable() " + configData.isEnabled());
		Assert.assertTrue("In the Database should be marked as Enabled", configData.isEnabled());
	}

	/**
	 * This method is used to disable the enabled feature defined in xml
	 * 
	 * @throws FeatureConfigurationException
	 * @throws ConfigPersistenceException
	 */

	@Test
	public void testChangeStatusOfFeatureConfigDisable() throws FeatureConfigurationException, ConfigPersistenceException {
		Feature featureConfig=getFeatureByName(FeatureTestConstant.TEST_FEATURE);
		featureConfigService = new FeatureConfigurationService();
		ConfigurationContext confiContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), FeatureTestConstant.TEST_FEATUREGROUP,FeatureTestConstant.TEST_FEATURE,GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
		featureConfigService.addFeatureConfiguration(confiContext, featureConfig);
	 
		IFeatureConfigurationService fsConfigService = new FeatureConfigurationService();
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		// Load from Database
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(FeatureTestConstant.TEST_VENDOR_NODEID, featureConfig.getFeatureName(),
				FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);

		FeatureConfigRequestContext requestContext = new FeatureConfigRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				FeatureTestConstant.TEST_FEATUREGROUP, FeatureTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
		ConfigurationContext configContextReq=requestContext.getConfigurationContext();
		fsConfigService.changeStatusOfFeatureConfig(configContextReq, featureConfig.getFeatureName(), false);
		// Disabled should be removed from Cache
		FeatureConfigurationUnit confiunit =(FeatureConfigurationUnit) checkCacheForConfig(featureConfig.getFeatureName());
		Assert.assertNull("Object from Cache should be Deleted if Config is Disabled", confiunit);
		// Disabling should be marked as disable in Database
		ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
		Assert.assertNotNull("Object should exist in DataBase if config is disabled", configData);
		logger.debug("testChangeStatusOfPermaStoreConfigDisable() " + configData.isEnabled());
		Assert.assertFalse("In the Database should be marked as Diabled", configData.isEnabled());
	}

	//@Test
	public void testChangeStatusOfFeatureService() throws ConfigPersistenceException, FeatureConfigurationException {
		 Feature featureConfig=getFeatureByName(FeatureTestConstant.TEST_FEATURE);
			featureConfigService = new FeatureConfigurationService();
			ConfigurationContext confiContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), FeatureTestConstant.TEST_FEATUREGROUP,FeatureTestConstant.TEST_FEATURE,GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
			featureConfigService.addFeatureConfiguration(confiContext, featureConfig);
		 
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		// Load from Database
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(FeatureTestConstant.getConfigNodeId(), featureConfig.getFeatureName(),
				FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);

		FeatureConfigRequestContext requestContext = new FeatureConfigRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				FeatureTestConstant.TEST_FEATUREGROUP, FeatureTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
		ConfigurationContext confiContextReq=requestContext.getConfigurationContext();
		
		Map<String, Boolean> vendStatus = new HashMap<>();
		vendStatus.put("startlabel", false);
		featureConfigService.changeStatusOfFeatureService(confiContextReq, requestContext.getFeatureName(), vendStatus);

		FeatureConfigurationUnit configunit = featureConfigService.getFeatureConfiguration(requestContext, requestContext.getFeatureName());
		Object cachedata = configunit.getConfigData();
		Feature feature = null;
		if (cachedata instanceof Feature) {
			feature = (Feature) cachedata;
		}
		boolean enabled=false;
		List<Service> serviceList= feature.getService();
		for(Service service:serviceList){
			if(service.getName().equalsIgnoreCase(FeatureTestConstant.TEST_FEATURE))
				enabled=service.isEnabled();
		}
		logger.debug(" value changed or not : " +enabled);
		Assert.assertFalse(enabled);
		
	}

	/**
	 * This test method is used when feature is disabled and doesnot exist in
	 * cache but data exist in cache
	 * 
	 * @throws ConfigPersistenceException
	 * @throws FeatureConfigurationException
	 */
	// @Test
	public void testDeleteFeatureConfigurationIfNull() throws ConfigPersistenceException, FeatureConfigurationException {
		// Load from DB First
		// #TODO I need to add feature in xml whose value for enabled is false.
		Feature featureConfig = getFeatureByName(FeatureTestConstant.TEST_FEATURE);
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(FeatureTestConstant.getConfigNodeId(), featureConfig.getFeatureName(),
				FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
		if (nodeData != null) {
			IFeatureConfigurationService fsConfigService = new FeatureConfigurationService();
			// Now Check DB and Cache
			Object cachedObject = checkCacheForConfig(nodeData.getConfigName());
			Assert.assertNull("Object from Cache should be Deleted", cachedObject);

			FeatureConfigRequestContext requestContext = new FeatureConfigRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
					FeatureTestConstant.TEST_FEATUREGROUP, FeatureTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME);
			ConfigurationContext confiContext=requestContext.getConfigurationContext();

			// ConfigNodeData configNodeData=nodeDataList.get(0);
			fsConfigService.deleteFeatureConfiguration(confiContext, nodeData.getConfigName());

			ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
			Assert.assertNull("Object from DataBase should be Deleted", configData);

		} else {
			Assert.fail("No ConfigNodeData found in DB for NodeId=" + FeatureTestConstant.getConfigNodeId());
		}
	}
	/**
	 * To check if feature in DB if found check in cache if it is enabled , 
	 * @throws FeatureConfigurationException 
	 * @throws FeatureConfigRequestException 
	 * 
	 */
	//@Test
	public void checkFeatureConfigExistInDbAndCacheAndIfconfigEnabled() throws FeatureConfigurationException, FeatureConfigRequestException{
	
		 Feature featureConfig=getFeatureByName(FeatureTestConstant.TEST_FEATURE);
			featureConfigService = new FeatureConfigurationService();
			ConfigurationContext confiContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), FeatureTestConstant.TEST_FEATUREGROUP,FeatureTestConstant.TEST_FEATURE,GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
			featureConfigService.addFeatureConfiguration(confiContext, featureConfig);
		 
		FeatureConfigRequestContext requestContext = new FeatureConfigRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				FeatureTestConstant.TEST_FEATUREGROUP, FeatureTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
		ConfigurationContext configContextReq=requestContext.getConfigurationContext();
	boolean sucess=	featureConfigService.checkFeatureExistInDBAndCache(configContextReq,FeatureTestConstant.TEST_FEATURE);
	
	Assert.assertTrue(sucess);
	}
	
	/**
	 * To check Feature not exist in the DB and cache
	 * @throws FeatureConfigurationException
	 * @throws FeatureConfigRequestException
	 */
	@Test
	public void checkFeatureConfigNotExist() throws FeatureConfigurationException, FeatureConfigRequestException{
	
		
		FeatureConfigRequestContext requestContext = new FeatureConfigRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				FeatureTestConstant.TEST_FEATUREGROUP,FeatureTestConstant.TEST_FEATURE,GenericTestConstant.TEST_IMPL_NAME,FeatureTestConstant.TEST_FEATURE_VENDOR,FeatureTestConstant.TEST_FEATURE_VERSION);
		ConfigurationContext confiContext=requestContext.getConfigurationContext();
	boolean sucess=	featureConfigService.checkFeatureExistInDBAndCache(confiContext, "Test");
	logger.debug("boolean val : "+sucess);
	Assert.assertFalse(sucess);
	}

	/**
	 * This method is used to check if configuration data exist in cache or not
	 * 
	 * @param configName
	 *           : Name of the feature
	 * @return Cache Object
	 */
	private Object checkCacheForConfig(String configName) {
		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
				+ FeatureConfigurationUnit.getConfigGroupKey(new Integer(FeatureTestConstant.getConfigNodeId())));
		/*IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"+GenericTestConstant.getSite()+"-"
				+ FeatureConfigurationUnit.getConfigGroupKey(new Integer(FeatureTestConstant.TEST_VENDOR_NODEID)));*/
		logger.debug("GroupLevel Map=" + map);
		Object cachedObj = map.get(configName);
		logger.debug("CheckCacheForConfig() ConfigObject is =" + cachedObj);
		return cachedObj;

	}

	/**
	 * This method is used to check any entry exist in database with specified
	 * configname and feature nodeId
	 * 
	 * @param configName
	 *           : Name of the feature
	 * @param nodeId
	 *           : Feature Node id
	 * @return ConfigNodeData Object
	 * @throws ConfigPersistenceException
	 */
	private ConfigNodeData checkDBForConfig(String configName, int nodeId) throws ConfigPersistenceException {
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData configData = pesrsistence.getConfigNodeDatabyNameAndNodeId(nodeId, configName, FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
		return configData;
	}

	/**
	 * This is used to get the feature object by name
	 * 
	 * @param name
	 *           : name of the feature whose object we want to get
	 * @return Feature object
	 */
	private Feature getFeatureByName(String name) {		
			if (featureConfigurationList.getFeatureName().equalsIgnoreCase(name))
				return featureConfigurationList;
		
		return null;
	}

}
