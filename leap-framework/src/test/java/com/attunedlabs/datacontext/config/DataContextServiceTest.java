package com.attunedlabs.datacontext.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.config.util.GenericTestConstant;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.datacontext.config.impl.DataContextConfigXMLParser;
import com.attunedlabs.datacontext.config.impl.DataContextConfigurationService;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.feature.config.FeatureConfigParserException;
import com.attunedlabs.feature.config.FeatureConfigurationException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class DataContextServiceTest {
	final Logger logger=LoggerFactory.getLogger(DataContextServiceTest.class);
	private FeatureDataContext featureDataContext;
	private IDataContextConfigurationService dataContextConfiService;
	
	/**
	 * This method is used to marshal featureDataContext.xml to pojo and return root
	 * object
	 * 
	 * @return FeaturesServiceInfo : return a FeatureDataContext object
	 * @throws DataContextParserException 
	 */
	private FeatureDataContext getDataContextConfiguration() throws DataContextParserException  {
		DataContextConfigXMLParser parser = new DataContextConfigXMLParser();
		InputStream inputstream = DataContextServiceTest.class.getClassLoader().getResourceAsStream(FeatureDataContextConstant.configfileToParse);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			    
			}
			reader.close();
		} catch (IOException e) {
		throw new DataContextParserException("datacontext file doesnot exist in classpath",e);
		}
		
		String featureConfigxml=out1.toString();
		FeatureDataContext featureDataContext = parser.marshallConfigXMLtoObject(featureConfigxml);
		return featureDataContext;
	}// end of getDataContextConfiguration method
	/**
	 * This method is called before any other test can executed, purpose is to
	 * load configuration in database and cache
	 * 
	 * @throws FeatureConfigParserException
	 * @throws ConfigPersistenceException
	 * @throws DataContextParserException 
	 * @throws FeatureConfigurationException
	 */
	@Before
	public void loadConfiguration() throws ConfigPersistenceException, DataContextParserException  {
		featureDataContext = getDataContextConfiguration();
		dataContextConfiService = new DataContextConfigurationService();
		// Clear all DB Data First for nodeId parcel and label service feature
		IConfigPersistenceService pesrsistence1 = new ConfigPersistenceServiceMySqlImpl();		
		pesrsistence1.deleteConfigNodeDataByNodeId(FeatureDataContextConstant.TEST_VENDOR_NODEID);
	}// end of loadConfiguration test method
	
	
	/**
	 * This method is used to the the dataContext uploaded in configuration for
	 * specific tenant or not
	 * @throws DataContextConfigurationException 
	 * 
	 * @throws FeatureConfigurationException
	 */
	@Test
	public void testDataContextUpload() throws DataContextConfigurationException  {
	
		String configName=getConfigname(FeatureDataContextConstant.TEST_FEATUREGROUP,FeatureDataContextConstant.TEST_FEATURE);
		
		ConfigurationContext confiContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),FeatureDataContextConstant.TEST_FEATUREGROUP,FeatureDataContextConstant.TEST_FEATURE,FeatureDataContextConstant.TEST_IMPL_NAME,FeatureDataContextConstant.TEST_FEATURE_VENDOR,FeatureDataContextConstant.TEST_FEATURE_VERSION);
		
		dataContextConfiService.addDataContext(confiContext, featureDataContext);
		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		// gap-FeatueNodeId-FSC
		IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-" + GenericTestConstant.getSite() + "-"
				+ DataContextConfigurationUnit.getConfigGroupKey(new Integer(FeatureDataContextConstant.TEST_VENDOR_NODEID)));// 860
		logger.debug("GroupLevel Map=" + map);
		DataContextConfigurationUnit configunit = (DataContextConfigurationUnit) map.get(configName);
		Object cachedata=configunit.getConfigData();
		logger.debug("ConfigObject is =" + configunit);
		Assert.assertNotNull("Cached DataContextConfiguration  should not be null", configunit);
		Assert.assertNotNull("Serilizable Object is not null : "+cachedata);
		Assert.assertTrue(cachedata instanceof FeatureDataContext);
		FeatureDataContext featureDC=(FeatureDataContext)cachedata;
		Assert.assertNotNull("FeatureDataContext not null", featureDC);
	}// end of testFeatureUpload method
	
	@Test
	public void testGetDataContextConfiguration() throws DataContextConfigurationException {
		String configName=getConfigname(FeatureDataContextConstant.TEST_FEATUREGROUP,FeatureDataContextConstant.TEST_FEATURE);
		ConfigurationContext confiContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), FeatureDataContextConstant.TEST_FEATUREGROUP,FeatureDataContextConstant.TEST_FEATURE,FeatureDataContextConstant.TEST_IMPL_NAME,FeatureDataContextConstant.TEST_FEATURE_VENDOR,FeatureDataContextConstant.TEST_FEATURE_VERSION);
		
		dataContextConfiService.addDataContext(confiContext, featureDataContext);
		RequestContext requestContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				FeatureDataContextConstant.TEST_FEATUREGROUP,FeatureDataContextConstant.TEST_FEATURE,FeatureDataContextConstant.TEST_IMPL_NAME,FeatureDataContextConstant.TEST_FEATURE_VENDOR,FeatureDataContextConstant.TEST_FEATURE_VERSION);
		
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		DataContextConfigurationUnit dataContextConfigUnit = dataContextConfiService.getDataContextConfiguration(requestContext);
		logger.debug("data context configuration unit : " + dataContextConfigUnit);
		Object cachedata=dataContextConfigUnit.getConfigData();
		logger.debug("ConfigObject is =" + dataContextConfigUnit);
		Assert.assertNotNull("Cached FeatureConfiguration  should not be null", dataContextConfigUnit);
		Assert.assertNotNull("Serilizable Object is not null : "+cachedata);
		Assert.assertTrue(cachedata instanceof FeatureDataContext);
		FeatureDataContext feat=(FeatureDataContext)cachedata;
		Assert.assertNotNull("FeatureDataContext is not null ",feat);
	}// end of testGetDataContextConfiguration method
	
	
	/**
	 * This test method is used to remove data from cache as well as from
	 * database
	 * 
	 * @throws ConfigPersistenceException
	 * @throws DataContextConfigurationException 
	 */
	@Test
	public void testDeleteFeatureConfiguration() throws ConfigPersistenceException, DataContextConfigurationException {
		String configName=getConfigname(FeatureDataContextConstant.TEST_FEATUREGROUP,FeatureDataContextConstant.TEST_FEATURE);
		ConfigurationContext confiContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(), FeatureDataContextConstant.TEST_FEATUREGROUP,FeatureDataContextConstant.TEST_FEATURE,FeatureDataContextConstant.TEST_IMPL_NAME,FeatureDataContextConstant.TEST_FEATURE_VENDOR,FeatureDataContextConstant.TEST_FEATURE_VERSION);
		dataContextConfiService.addDataContext(confiContext, featureDataContext);
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData nodeData = pesrsistence.getConfigNodeDatabyNameAndNodeId(FeatureDataContextConstant.TEST_VENDOR_NODEID, configName,
				DataContextConstant.DATACONTEXT_CONFIG_TYPE);
		if (nodeData != null) {

			RequestContext requestContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
					FeatureDataContextConstant.TEST_FEATUREGROUP, FeatureDataContextConstant.TEST_FEATURE,FeatureDataContextConstant.TEST_IMPL_NAME,FeatureDataContextConstant.TEST_FEATURE_VENDOR,FeatureDataContextConstant.TEST_FEATURE_VERSION);
			ConfigurationContext configContext=requestContext.getConfigurationContext();
			dataContextConfiService.deleteDataContextConfiguration(configContext);

			// Now Check DB and Cache
			Object cachedObject = checkCacheForConfig(nodeData.getConfigName());
			Assert.assertNull("Object from Cache should be Deleted", cachedObject);
			ConfigNodeData configData = checkDBForConfig(nodeData.getConfigName(), nodeData.getParentConfigNodeId());
			Assert.assertNull("Object from DataBase should be Deleted", configData);

		} else {
			Assert.fail("No ConfigNodeData found in DB for NodeId=" + FeatureDataContextConstant.TEST_VENDOR_NODEID);
		}

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
				+ DataContextConfigurationUnit.getConfigGroupKey(new Integer(FeatureDataContextConstant.TEST_VENDOR_NODEID)));// 860
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
		ConfigNodeData configData = pesrsistence.getConfigNodeDatabyNameAndNodeId(nodeId, configName, DataContextConstant.DATACONTEXT_CONFIG_TYPE);
		return configData;
	}
	
	/**
	 * This method is used to give config name
	 * @param featureGroup : feature group in String
	 * @param featureName : feature name in String
	 * @return configName : configname in string
	 */
	private String getConfigname(String featureGroup,String featureName){
		logger.debug(".getConfigname method of DataContextConfiguration Service");
		String configName=featureGroup+"-"+featureName+"-"+DataContextConstant.DATACONTEXT_SUFFIX_KEY;
		return configName;
	}

}
