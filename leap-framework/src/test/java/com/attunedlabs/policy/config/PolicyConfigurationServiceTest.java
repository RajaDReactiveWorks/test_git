package com.attunedlabs.policy.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.beans.ConfigurationUnit;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.util.GenericTestConstant;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.permastore.config.IPermaStoreConfigurationService;
import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationConstant;
import com.attunedlabs.permastore.config.PermaStoreConfigurationException;
import com.attunedlabs.permastore.config.PermaStoreTestConstant;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigXMLParser;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigurationService;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfigurations;
import com.attunedlabs.policy.PolicyTestConstant;
import com.attunedlabs.policy.config.IPolicyConfigurationService;
import com.attunedlabs.policy.config.PolicyConfigXMLParser;
import com.attunedlabs.policy.config.PolicyConfigXMLParserException;
import com.attunedlabs.policy.config.PolicyConfigurationException;
import com.attunedlabs.policy.config.PolicyConfigurationUnit;
import com.attunedlabs.policy.config.PolicyConstant;
import com.attunedlabs.policy.config.PolicyRequestContext;
import com.attunedlabs.policy.config.PolicyRequestException;
import com.attunedlabs.policy.config.impl.PolicyConfigurationService;
import com.attunedlabs.policy.config.impl.PolicyEvaluationRequestHandler;
import com.attunedlabs.policy.config.impl.PolicyInvalidRegexExpception;
import com.attunedlabs.policy.jaxb.Policies;
import com.attunedlabs.policy.jaxb.Policy;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class PolicyConfigurationServiceTest {
	final Logger logger = LoggerFactory.getLogger(PolicyConfigurationServiceTest.class);
	private List<Policy> policyConfigList;
	IPolicyConfigurationService psConfigService;

	/**
	 * This method is to get the policy xml file and marshal to jaxb generated
	 * pojo class
	 * 
	 * @return Policies Object
	 * @throws PolicyConfigXMLParserException
	 */
	private Policies getPolicyConfiguration() throws PolicyConfigXMLParserException {
		PolicyConfigXMLParser parser = new PolicyConfigXMLParser();
		InputStream inputstream = PolicyConfigXMLParser.class.getClassLoader().getResourceAsStream(PolicyTestConstant.policyconfigfileToParse);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			    
			}
			reader.close();
		} catch (IOException e) {
		throw new PolicyConfigXMLParserException("policy file doesnot exist in classpath",e);
		}
		
		String policeConfigxml=out1.toString();
		Policies policies = parser.marshallXMLtoObject(policeConfigxml);
		return policies;
	}

	/**
	 * This method is used to load the policy configuration
	 * 
	 * @throws PolicyConfigurationException
	 * @throws ConfigPersistenceException
	 * @throws PolicyConfigXMLParserException
	 */
	@Before
	public void loadConfigurations() throws PolicyConfigurationException, ConfigPersistenceException, PolicyConfigXMLParserException {
		Policies policyConfig = getPolicyConfiguration();
		psConfigService = new PolicyConfigurationService();
		// Clear all DB Data First for nodeId 26
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getConfigNodeId());
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getVendorConfigNodeId());
		policyConfigList = policyConfig.getPolicy();
		/*
		 * for (Policy config : policyConfigList) {
		 * psConfigService.addPolicyConfiguration(GenericTestConstant.getTenant(),
		 * GenericTestConstant.getSite(), config); }
		 */
	}

	
	private List<PermaStoreConfiguration> permaStoreConfigList;
	private IPermaStoreConfigurationService permConfigService;

	private PermaStoreConfigurations getPermaStoreConfigurations() throws PermaStoreConfigParserException {
		PermaStoreConfigXMLParser parser = new PermaStoreConfigXMLParser();
InputStream inputstream = PermaStoreConfigXMLParser.class.getClassLoader().getResourceAsStream(PolicyTestConstant.policy_linked_permaStore_configfile);
		
		
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


	public void loadConfigurationsOfPemastore() throws PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigPersistenceException,
			ConfigServerInitializationException {

		PermaStoreConfigurations permaStoreConfigs = getPermaStoreConfigurations();
		permConfigService = new PermaStoreConfigurationService();
		// Clear all DB Data First for nodeId 26
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getVendorConfigNodeId());
	
		permaStoreConfigList = permaStoreConfigs.getPermaStoreConfiguration();
		logger.debug("permastoreconfig list size  ------ "+permaStoreConfigList.size());
		/*
		 * for (PermaStoreConfiguration config : permaStoreConfigList) {
		 * psConfigService
		 * .addPermaStoreConfiguration(GenericTestConstant.getTenant(),
		 * GenericTestConstant.getSite(), config); }
		 */
	}
	
	/**
	 * This method is to get policy by name
	 * 
	 * @param name
	 *           : Name of the policy
	 * @return Policy Object
	 */
	private Policy getPolicyByName(String name) {
		logger.debug("inside getPolicyByName() of PolicyConfigurationServiceTest");
		for (Policy pol : policyConfigList) {
			if (pol.getPolicyName().equalsIgnoreCase(name))
				return pol;
		}
		return null;
	}

	/**
	 * This method is used to test policy uploaded successfully or not
	 * 
	 * @throws PolicyConfigurationException
	 */
	@Test
	public void testPolicyUpload() throws PolicyConfigurationException {

		Policy policy = getPolicyByName("PolicyDefinedFactTest");
		ConfigurationContext configurationContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// added to DB and load to cache
		psConfigService.addPolicyConfiguration(configurationContext, policy);

		logger.debug(" inside testpolicy of PolicyConfigurationService");
		Policy config = getPolicyByName("PolicyDefinedFactTest");
		logger.debug("policy name : " + config.getPolicyName());
		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"+ PolicyConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861
		
		Assert.assertNotNull("Cached Map  should not be null", map);

		logger.debug("GroupLevel Map=" + map);
		ConfigurationUnit cachedObj = (ConfigurationUnit) map.get("PolicyDefinedFactTest");
		logger.debug("ConfigObject is =" + cachedObj);
		Assert.assertNotNull("Cached PolicyConfiguration  should not be null", cachedObj);
		Assert.assertNotNull("Cached data in PolicyConfiguration Unit   should not be null", cachedObj.getConfigData());

	}

	/**
	 * This method is used test if policy is disabled data should be deleted from
	 * cache ,not deleted from Db ,policy is disabled in Db
	 * 
	 * @throws PolicyConfigurationException
	 * @throws ConfigPersistenceException
	 * @throws PolicyConfigXMLParserException
	 */
	@Test
	public void testChangeStatusOfPolicyconfigDisable() throws PolicyConfigurationException, ConfigPersistenceException,
			PolicyConfigXMLParserException {
		IPolicyConfigurationService policyConfigurationService = new PolicyConfigurationService();
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();

		Policy policy = getPolicyByName("PolicyDefinedFactTest");
		ConfigurationContext configurationContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// added to DB and load to cache
		psConfigService.addPolicyConfiguration(configurationContext, policy);

		// Load from Database
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(),
				policy.getPolicyName(), PolicyConstant.POLICY_CONFIG_TYPE);
		if(nodeData!=null){

		PolicyRequestContext requestContext = new PolicyRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		ConfigurationContext configurationContextPolicyReq=requestContext.getConfigurationContext();
		policyConfigurationService.changePolicyStatus(configurationContextPolicyReq, policy.getPolicyName(), false);
		// Disabled should be removed from Cache
		Object cachedObj = checkCacheForConfig(policy.getPolicyName());
		Assert.assertNull("Object from Cache should be Deleted if Policy is Disabled", cachedObj);
		// Disabling should be marked as disable in Database
		ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
		Assert.assertNotNull("Object should exist in DataBase if config is disabled", configData);
		logger.debug("testChangeStatusOfPolicyconfigDisable " + configData.isEnabled());
		Assert.assertFalse("In the Database should be marked as Diabled", configData.isEnabled());
		}else{
			Assert.fail("No ConfigNodeData found in DB for NodeId=" + GenericTestConstant.getVendorConfigNodeId());

		}
	}

	/**
	 * This method is used test if policy is Enabled data should be reloaded to
	 * cache from Db (cache Data should not be null) ,policy is enabled in DB
	 * 
	 * @throws PolicyConfigurationException
	 * @throws ConfigPersistenceException
	 * @throws PolicyConfigXMLParserException
	 */
	@Test
	public void testChangeStatusOfPolicyconfigEnable() throws PolicyConfigurationException, ConfigPersistenceException,
			PolicyConfigXMLParserException {
		IPolicyConfigurationService policyConfigurationService = new PolicyConfigurationService();
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();

		Policy policy = getPolicyByName("FactMappingTest");
		ConfigurationContext configurationContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// added to DB and load to cache
		psConfigService.addPolicyConfiguration(configurationContext, policy);

		// Load from Database
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(),
				policy.getPolicyName(), PolicyConstant.POLICY_CONFIG_TYPE);

		if(nodeData!=null){
		PolicyRequestContext requestContext = new PolicyRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		ConfigurationContext configurationContextPolicyReq=requestContext.getConfigurationContext();
		policyConfigurationService.changePolicyStatus(configurationContextPolicyReq, policy.getPolicyName(), true);
		// Enable should be Reload policy From DB to Cache
		ConfigurationUnit cachedObj = checkCacheForConfig(policy.getPolicyName());
		Assert.assertNotNull("Object from Cache should be Reloaded if Policy is Enabled", cachedObj);
		Assert.assertNotNull(" Cache configdata of PolicyConfigaration UnIt  should be Reloaded if Policy is Enabled",cachedObj.getConfigData());

		// Enable should be marked as Enable in Database
		ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
		Assert.assertNotNull("Object should exist in DataBase if config is Enable", configData);
		logger.debug("testChangeStatusOfPolicyconfigEnable " + configData.isEnabled());
		Assert.assertTrue("In the Database should be marked as Enable", configData.isEnabled());

		// check cache object is instance of PolicyConfigurationUnit
		Assert.assertTrue(cachedObj instanceof PolicyConfigurationUnit);

		PolicyConfigurationUnit policyConfigurationUnit = (PolicyConfigurationUnit) cachedObj;

		// Testing after enabling policy the xml data contianing isEnable also
		// enabled To True or not
		Assert.assertTrue(policyConfigurationUnit.getIsEnabled());
		}else{
			Assert.fail("No ConfigNodeData found in DB for NodeId=" + GenericTestConstant.getVendorConfigNodeId());

		}
	}

	@Test
	public void testDeletePolicyIfPolicyNotExistinCache() throws PolicyConfigurationException, ConfigPersistenceException,
			PolicyConfigurationException {

		Policy policy = getPolicyByName("PolicyDefinedFactMapTest");
		ConfigurationContext configurationContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// added to DB and load to cache
		psConfigService.addPolicyConfiguration(configurationContext, policy);

		// Load from DB First
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(),policy.getPolicyName(), PolicyConstant.POLICY_CONFIG_TYPE);

		if (nodeData != null) {

			// Now Check Cache
			Object cachedObject = checkCacheForConfig(nodeData.getConfigName());
			Assert.assertNull("Object from Cache should be null", cachedObject);
			IPolicyConfigurationService policyConfigurationService = new PolicyConfigurationService();

			PolicyRequestContext requestContext = new PolicyRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
			ConfigurationContext configurationContextPolicyReq=requestContext.getConfigurationContext();
			// ConfigNodeData configNodeData=nodeDataList.get(0);
			policyConfigurationService.deletePolicy(configurationContextPolicyReq, nodeData.getConfigName());

			ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
			Assert.assertNull("Object from DataBase should be Deleted", configData);

		} else {
			Assert.fail("No ConfigNodeData found in DB for NodeId=" + GenericTestConstant.getVendorConfigNodeId());
		}

	}

	@Test
	public void testDeletePolicyIfPolicyExsistInCache() throws PolicyConfigurationException, ConfigPersistenceException,
			PolicyConfigurationException {

		Policy policy = getPolicyByName("PolicyDefinedFactTest");
		ConfigurationContext configurationContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// added to DB and load to cache
		psConfigService.addPolicyConfiguration(configurationContext, policy);

		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(),policy.getPolicyName(), PolicyConstant.POLICY_CONFIG_TYPE);
		if (nodeData != null) {
			IPolicyConfigurationService policyConfigurationService = new PolicyConfigurationService();

			PolicyRequestContext requestContext = new PolicyRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
			ConfigurationContext configurationContextPolicyReq=requestContext.getConfigurationContext();
			// ConfigNodeData configNodeData=nodeDataList.get(0);
			policyConfigurationService.deletePolicy(configurationContextPolicyReq, nodeData.getConfigName());

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
	public void testPolicyEvalutionRequestHandlerForSQLDailect() throws PolicyConfigurationException, ConfigPersistenceException, PolicyConfigXMLParserException, PolicyRequestException, PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigServerInitializationException, PolicyInvalidRegexExpception{
		loadConfigurationsOfPemastore();
			PolicyEvaluationRequestHandler pHandler=new PolicyEvaluationRequestHandler();
		Policy policy = getPolicyByName("PolicyDefinedFactTest");
		ConfigurationContext configurationContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// added to DB and load to cache
		psConfigService.addPolicyConfiguration(configurationContext, policy);
		ConfigurationUnit configurationUnit = checkCacheForConfig(policy.getPolicyName());

		Assert.assertNotNull("ConfigurationUnit should not be null ",configurationUnit);
		Assert.assertTrue(configurationUnit instanceof PolicyConfigurationUnit);
		Assert.assertNotNull("cache data in configurationUnit should not be null ",configurationUnit.getConfigData());
		PolicyConfigurationUnit pConfigurationUnit=(PolicyConfigurationUnit) configurationUnit;

		logger.debug("pConfigurationUnit "+pConfigurationUnit);
		//PolicyConfigurationUnit polConfigUnit=new PolicyConfigurationUnit(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), GenericTestConstant.getConfigNodeId(),policy.isEnabled() , policy, configurationUnit.getConfigData());
		PolicyRequestContext policyRequestContext=new PolicyRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		Map<String, Object> requestVaribleMap=new HashMap<String, Object>();
		requestVaribleMap.put("$dstare", "STA1");
		requestVaribleMap.put("$platform", "abcd");
		List<String> stageAreaList = new ArrayList<String>();
		stageAreaList.add("XYZ");
		stageAreaList.add("ABC");
		requestVaribleMap.put("$GetStagingAreas", stageAreaList);
		
		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("GetStagingAreas");
		Assert.assertNotNull("PermaStoreConfiguration should not be null ", permaStoreConfig);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		permConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);

		policyRequestContext.setRequestVariable(requestVaribleMap);
		boolean isTrue=	pHandler.evaluatePolicy(pConfigurationUnit, policyRequestContext);
	Assert.assertTrue("PolicyEvalutionRequestHandler for SqlDailect should Be true ", isTrue);
	
	}
	
	
	@Test
	public void testPolicyEvalutionRequestHandlerForREGEX() throws    PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigServerInitializationException, ConfigPersistenceException, PolicyConfigurationException, PolicyRequestException, PolicyInvalidRegexExpception{
		loadConfigurationsOfPemastore();

			PolicyEvaluationRequestHandler pHandler=new PolicyEvaluationRequestHandler();
		Policy policy = getPolicyByName("PolicyDefinedFactAttDateTestNine");
		ConfigurationContext configurationContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// added to DB and load to cache
		psConfigService.addPolicyConfiguration(configurationContext, policy);
		ConfigurationUnit configurationUnit = checkCacheForConfig(policy.getPolicyName());

		Assert.assertNotNull("ConfigurationUnit should not be null ",configurationUnit);
		Assert.assertTrue(configurationUnit instanceof PolicyConfigurationUnit);
		Assert.assertNotNull("cache data in configurationUnit should not be null ",configurationUnit.getConfigData());
		PolicyConfigurationUnit pConfigurationUnit=(PolicyConfigurationUnit) configurationUnit;

		logger.debug("pConfigurationUnit "+pConfigurationUnit);
		//PolicyConfigurationUnit polConfigUnit=new PolicyConfigurationUnit(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), GenericTestConstant.getConfigNodeId(),policy.isEnabled() , policy, configurationUnit.getConfigData());
		PolicyRequestContext policyRequestContext=new PolicyRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		Map<String, Object> requestVaribleMap=new HashMap<String, Object>();
		requestVaribleMap.put("$dstare", "STA1");
		requestVaribleMap.put("$platform", "28");
		
		
		PermaStoreConfiguration permaStoreConfig = getPermastoreConfigByName("GetStagingAreasOne");
		Assert.assertNotNull("PermaStoreConfiguration should not be null ", permaStoreConfig);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// add permastore config to DB and loaad To cache
		permConfigService.addPermaStoreConfiguration(configContext, permaStoreConfig);

		policyRequestContext.setRequestVariable(requestVaribleMap);
		boolean isTrue=	pHandler.evaluatePolicy(pConfigurationUnit, policyRequestContext);
	Assert.assertTrue("PolicyEvalutionRequestHandler for REGEX should Be true ", isTrue);
	
	}
	/**
	 * To check policy Exist or not in Db
	 * @throws PolicyConfigurationException
	 * @throws PolicyRequestException
	 */
	
	@Test
	public void testPolicyExistInDBAndCacheOrNot() throws PolicyConfigurationException, PolicyRequestException{
		
		Policy policy = getPolicyByName("PolicyDefinedFactMapTest");
		ConfigurationContext configurationContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// added to DB and load to cache
		psConfigService.addPolicyConfiguration(configurationContext, policy);
		PolicyRequestContext policyRequestContext=new PolicyRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		ConfigurationContext configurationContextPolicyeq=policyRequestContext.getConfigurationContext();
		ConfigurationUnit configurationUnit = checkCacheForConfig(policy.getPolicyName());
		Assert.assertNull("ConfigurationUnit should not be null ",configurationUnit);
		boolean isExist=psConfigService.checkPolicyExistInDbAndCache(configurationContextPolicyeq, "PolicyDefinedFactMapTest");

		Assert.assertTrue(isExist);
		
	}
	/**
	 * To check if policy Exist in DB and it is Enabled in DB  but not loaded into cache, so by loading the data to cache check loaded or not 
	 * @throws PolicyConfigurationException
	 * @throws PolicyRequestException
	 * @throws ConfigPersistenceException 
	 */
	
	@Test
	public void testPolicyExistInDBByLoadingCacheData() throws PolicyConfigurationException, PolicyRequestException, ConfigPersistenceException{
		
		Policy policy = getPolicyByName("PolicyDefinedFactAttDateTestTen");
		ConfigurationContext configurationContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		// added to DB and load to cache
		psConfigService.addPolicyConfiguration(configurationContext, policy);
		PolicyRequestContext policyRequestContext=new PolicyRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		ConfigurationContext configurationContextPolicyReq=policyRequestContext.getConfigurationContext();
		ConfigurationUnit configurationUnit = checkCacheForConfig(policy.getPolicyName());
		Assert.assertNull("ConfigurationUnit should not be null ",configurationUnit);
		
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		
		ConfigNodeData configNodeData =pesrsistence.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.getVendorConfigNodeId(), "PolicyDefinedFactAttDateTestTen", PolicyConstant.POLICY_CONFIG_TYPE);
			Assert.assertNotNull("configNodedata should Not be null ",configNodeData);

				pesrsistence.enableConfigNodeData(true,configNodeData.getNodeDataId());

		boolean isExist=psConfigService.checkPolicyExistInDbAndCache(configurationContextPolicyReq, "PolicyDefinedFactAttDateTestTen");

		Assert.assertTrue(isExist);
		 configurationUnit = checkCacheForConfig(policy.getPolicyName());
		 Assert.assertNotNull("Configaration  in cache should  not be null ",configurationUnit);
		 Assert.assertNotNull("ConfigData in configarationUnit of  cache should  not be null ",configurationUnit.getConfigData());

		
	}
	/**
	 * to test policy not EXist in DB
	 * @throws PolicyConfigurationException
	 * @throws PolicyRequestException
	 */
	@Test
	public void testPolicyIfNotExistInDB() throws PolicyConfigurationException, PolicyRequestException{
		
		PolicyRequestContext policyRequestContext=new PolicyRequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		ConfigurationContext configurationContextPolicyReq=policyRequestContext.getConfigurationContext();
		boolean isExist=psConfigService.checkPolicyExistInDbAndCache(configurationContextPolicyReq, "testvalue");

		Assert.assertFalse(isExist);
		
	}
	private ConfigurationUnit checkCacheForConfig(String configName) {
		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-26-PSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"+ PolicyConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861
		logger.debug("GroupLevel Map=" + map);
		ConfigurationUnit cachedObj = (ConfigurationUnit) map.get(configName);
		logger.debug("CheckCacheForConfig() ConfigObject is =" + cachedObj);
		return cachedObj;

	}

	private ConfigNodeData checkDBForConfig(String configName, int nodeId) throws ConfigPersistenceException {
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData configData = pesrsistence.getConfigNodeDatabyNameAndNodeId(nodeId, configName, PolicyConstant.POLICY_CONFIG_TYPE);
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
