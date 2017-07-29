package com.getusroi.permastore.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.config.beans.ConfigurationUnit;
import com.getusroi.config.persistence.ConfigNode;
import com.getusroi.config.persistence.ConfigNodeData;
import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.InvalidNodeTreeException;
import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.getusroi.config.server.ConfigServerInitializationException;
import com.getusroi.config.util.GenericTestConstant;
import com.getusroi.core.datagrid.DataGridService;
import com.getusroi.datacontext.config.DataContextConfigurationException;
import com.getusroi.datacontext.config.DataContextConfigurationUnit;
import com.getusroi.datacontext.config.impl.DataContextConfigurationService;
import com.getusroi.eventframework.config.EventFrameworkConfigurationException;
import com.getusroi.eventframework.config.impl.EventFrameworkConfigService;
import com.getusroi.eventframework.jaxb.DispatchChanel;
import com.getusroi.eventframework.jaxb.SystemEvent;
import com.getusroi.feature.config.FeatureConfigRequestContext;
import com.getusroi.feature.config.FeatureConfigRequestException;
import com.getusroi.feature.config.FeatureConfigurationException;
import com.getusroi.feature.config.FeatureConfigurationUnit;
import com.getusroi.feature.config.impl.FeatureConfigurationService;
import com.getusroi.integrationfwk.config.IntegrationPipelineConfigException;
import com.getusroi.integrationfwk.config.IntegrationPipelineConfigUnit;
import com.getusroi.integrationfwk.config.impl.IntegrationPipelineConfigurationService;
import com.getusroi.permastore.config.impl.PermaStoreConfigXMLParser;
import com.getusroi.permastore.config.impl.PermaStoreConfigurationService;
import com.getusroi.permastore.config.jaxb.Event;
import com.getusroi.permastore.config.jaxb.PermaStoreConfiguration;
import com.getusroi.permastore.config.jaxb.PermaStoreConfigurations;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ReLoadTest {
	final Logger logger = LoggerFactory.getLogger(ReLoadTest.class);
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
	
	
	public void TestReload() throws PermaStoreConfigurationException{
		PermaStoreConfigurationService service=new PermaStoreConfigurationService();
		/*PermaStoreConfiguration permaStoreConfiguration = getPermastoreConfigByName("EntryTypeConfiguration");
		Assert.assertNotNull("PermaStoreConfiguration should not be null", permaStoreConfiguration);
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),GenericTestConstant.getFeatureGroup(),GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME,GenericTestConstant.TEST_VENDOR,GenericTestConstant.TEST_VERSION);
		psConfigService.addPermaStoreConfiguration(configContext, permaStoreConfiguration);*/

		
		RequestContext requestContext = new RequestContext();
		requestContext.setFeatureGroup("Clock");
		requestContext.setFeatureName("ClockService");
		requestContext.setImplementationName("key2act");
		requestContext.setSiteId("all");
		requestContext.setTenantId("all");
		requestContext.setVendor("key2act");
		requestContext.setVersion("1.0");
		service.reloadPerStoreCacheObject(requestContext,"EntryTypeConfiguration");
		// Check if data is loaded in the Hazelcast or not
				HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
				// gap-26-PSC
				IMap map = hazelcastInstance.getMap(GenericTestConstant.getTenant() + "-"
						+ PermaStoreConfigurationUnit.getConfigGroupKey(new Integer(GenericTestConstant.getVendorConfigNodeId())));// 861
				Assert.assertNotNull("Cache Map should not be null", map);
				ConfigurationUnit configUnit =(ConfigurationUnit) map.get("EntryTypeConfiguration");
				Assert.assertNotNull("ConfigurationUnit should not be null", configUnit);
		
		
	}
	
	
	//@Test
	public void testReloadPermastore() throws ConfigPersistenceException, PermaStoreConfigurationException, PermaStoreConfigRequestException{
		PermaStoreConfigurationService service=new PermaStoreConfigurationService();
		RequestContext requestContext = new RequestContext();
		requestContext.setFeatureGroup("Clock");
		requestContext.setFeatureName("ClockService");
		requestContext.setImplementationName("key2act");
		requestContext.setSiteId("all");
		requestContext.setTenantId("all");
		requestContext.setVendor("key2act");
		requestContext.setVersion("1.0");
		
		ConfigurationContext context=new ConfigurationContext("all","all","Clock","ClockService","key2act","key2act","1.0");
		service.changeStatusOfPermaStoreConfig(context,"EntryTypeConfiguration",true);
		PermaStoreConfigurationUnit permaUnit=service.getPermaStoreConfiguration(requestContext,"EntryTypeConfiguration");
		if(permaUnit!=null)
			logger.debug("config data in  EntryTypeConfiguration permastore is "+permaUnit.getConfigData().toString());
		else{
			logger.debug("permaUnit is null");
		}
		service.changeStatusOfPermaStoreConfig(context,"EntryTypeConfiguration",false);
		service.reloadPerStoreCacheObject(requestContext, "EntryTypeConfiguration");
		permaUnit=service.getPermaStoreConfiguration(requestContext,"EntryTypeConfiguration");
		if(permaUnit!=null)
			logger.debug("config data is after disable "+permaUnit.getConfigData().toString());
		else{
			logger.debug("permaUnit is null disable");
		}
		
		
	}
	
	//@Test
	public void testReloadDataContext() throws DataContextConfigurationException{
		RequestContext requestContext = new RequestContext();
		requestContext.setFeatureGroup("Clock");
		requestContext.setFeatureName("ClockService");
		requestContext.setImplementationName("key2act");
		requestContext.setSiteId("all");
		requestContext.setTenantId("all");
		requestContext.setVendor("key2act");
		requestContext.setVersion("1.0");
		DataContextConfigurationService service =new DataContextConfigurationService();
		DataContextConfigurationUnit dataContext=service.getDataContextConfiguration(requestContext);
		if(dataContext==null){
			logger.debug("data conetxt before relaod is null");
		}
		else{
			logger.debug("dataconetxt is "+dataContext);
		}
		
		service.reloadDataContextCacheObject(requestContext, "Clock-ClockService-FDC");
		dataContext=service.getDataContextConfiguration(requestContext);
		if(dataContext==null){
			logger.debug("data conetxt after reload is null");
		}
		else{
			logger.debug("dataconetxt after reload is "+dataContext);
		}
	}
	//@Test
	public void testReloadFeatureContext() throws DataContextConfigurationException, FeatureConfigRequestException, FeatureConfigurationException{
		RequestContext requestContext = new RequestContext();
		requestContext.setFeatureGroup("Clock");
		requestContext.setFeatureName("ClockService");
		requestContext.setImplementationName("key2act");
		requestContext.setSiteId("all");
		requestContext.setTenantId("all");
		requestContext.setVendor("key2act");
		requestContext.setVersion("1.0");
		FeatureConfigurationService service =new FeatureConfigurationService();
		FeatureConfigRequestContext featureRequestContext = new FeatureConfigRequestContext(
				"all", requestContext.getSiteId(), requestContext.getFeatureGroup(),
				requestContext.getFeatureName(), requestContext.getImplementationName(), requestContext.getVendor(),
				requestContext.getVersion());
		FeatureConfigurationUnit featureConfigUnit=service.getFeatureConfiguration(featureRequestContext,"ClockService");
		if(featureConfigUnit==null){
			logger.debug("data conetxt before relaod is null");
		}
		else{
			logger.debug("dataconetxt is "+featureConfigUnit);
		}
		
		service.reloadFeatureCacheObject(requestContext, "ClockService");
		featureConfigUnit=service.getFeatureConfiguration(featureRequestContext,"ClockService");
		if(featureConfigUnit==null){
			logger.debug("data conetxt after reload is null");
		}
		else{
			logger.debug("dataconetxt after reload is "+featureConfigUnit.getConfigData().toString());
		}
	}
	//@Test
	public void testIntegrationPipeLineReload() throws IntegrationPipelineConfigException{
		RequestContext requestContext = new RequestContext();
		requestContext.setFeatureGroup("sacGroup");
		requestContext.setFeatureName("sac");
		requestContext.setImplementationName("key2act");
		requestContext.setSiteId("all");
		requestContext.setTenantId("all");
		requestContext.setVendor("key2act");
		requestContext.setVersion("1.0");
		IntegrationPipelineConfigurationService service=new IntegrationPipelineConfigurationService();
		IntegrationPipelineConfigUnit configUnit=service.getIntegrationPipeConfiguration(requestContext, "SR-WONEW-handler-pipeline");
		if(configUnit==null)
		{
			logger.debug("config unit is null before reload");
		}
		else{
			logger.debug("config unit before reload "+configUnit.getConfigData().toString());
		}
		service.reloadIntegrationPipelineCacheObject(requestContext, "SR-WONEW-handler-pipeline");
		configUnit=service.getIntegrationPipeConfiguration(requestContext, "SR-WONEW-handler-pipeline");
		if(configUnit==null)
		{
			logger.debug("config unit is null after reload");
		}
		else{
			logger.debug("config unit after reload "+configUnit.getConfigData());
		}
		
	}

	//@Test
	public void testSystemEventReload() throws EventFrameworkConfigurationException{
		RequestContext requestContext = new RequestContext();
		requestContext.setSiteId("TWO");
		requestContext.setTenantId("Two, Inc.");
		ConfigurationContext configContext=new ConfigurationContext(requestContext);
		EventFrameworkConfigService service=new EventFrameworkConfigService();
		SystemEvent systemEvent=service.getSystemEventConfiguration(configContext, "SERVICE_COMPLETION_SUCCESS");
		if(systemEvent==null){
			logger.debug("system event before reload in null");
		}
		else{
			logger.debug("System event before reload is "+systemEvent.toString());
		}
		service.reloadSystemEventCacheObject(requestContext, "SERVICE_COMPLETION_SUCCESS");
		systemEvent=service.getSystemEventConfiguration(configContext, "SERVICE_COMPLETION_SUCCESS");
		if(systemEvent==null){
			logger.debug("system event after reload in null");
		}
		else{
			logger.debug("System event after reload is "+systemEvent.getDescription());
		}
		
	}
	//@Test
	public void testEventReload() throws EventFrameworkConfigurationException{
		RequestContext requestContext = new RequestContext();
		requestContext.setSiteId("TWO");
		requestContext.setTenantId("Two, Inc.");
		ConfigurationContext configContext=new ConfigurationContext(requestContext);
		EventFrameworkConfigService service=new EventFrameworkConfigService();
		com.getusroi.eventframework.jaxb.Event event=service.getEventConfiguration(configContext, "ServiceRequest-Processed");
		if(event==null){
			logger.debug(" event before reload in null");
		}
		else{
			logger.debug(" event before reload is "+event.getDescription());
		}
		service.reloadEventCacheObject(requestContext, "ServiceRequest-Processed");
		event=service.getEventConfiguration(configContext, "ServiceRequest-Processed");
		if(event==null){
			logger.debug(" event after reload in null");
		}
		else{
			logger.debug(" event after reload is "+event.getType());
		}
	}
	
	@Test
	public void testDispatchChanelReload() throws EventFrameworkConfigurationException{
		RequestContext requestContext = new RequestContext();
		requestContext.setSiteId("TWO");
		requestContext.setTenantId("Two, Inc.");
		ConfigurationContext configContext=new ConfigurationContext(requestContext);
		EventFrameworkConfigService service=new EventFrameworkConfigService();
		DispatchChanel disChanel=service.getDispatchChanelConfiguration(configContext, "FILE_STORE");
		if(disChanel==null){
			logger.debug(" disChanel before reload in null");
		}
		else{
			logger.debug(" disChanel before reload is "+disChanel.getDescription());
		}
		service.reloadDispatchChanelCacheObject(requestContext, "FILE_STORE");
		disChanel=service.getDispatchChanelConfiguration(configContext, "FILE_STORE");
		if(disChanel==null){
			logger.debug(" disChanel after reload in null");
		}
		else{
			logger.debug(" disChanel after reload is "+disChanel.getChanelConfiguration());
		}
		
	}
	
	
	
	private PermaStoreConfiguration getPermastoreConfigByName(String name) {
		for (PermaStoreConfiguration permaStoreConfig : permaStoreConfigList) {
			if (permaStoreConfig.getName().equalsIgnoreCase(name))
				return permaStoreConfig;
		}
		return null;
	}
	
	protected Integer getApplicableNodeId(RequestContext reqContext) throws InvalidNodeTreeException, ConfigPersistenceException {

		String featureGroup = reqContext.getFeatureGroup();
		String featureName = reqContext.getFeatureName();
		String tenantId = reqContext.getTenantId();
		String siteId = reqContext.getSiteId();
		String implName=reqContext.getImplementationName();
		String vendorName=reqContext.getVendor();
		String version=reqContext.getVersion();
		if(vendorName != null && !(vendorName.isEmpty())){
			return getApplicableNodeIdVendorName(tenantId, siteId, featureGroup, featureName,implName,vendorName,version);
		}else{
		return getApplicableNodeIdFeatureName(tenantId, siteId, featureGroup, featureName,implName);
		}
	}//end of method getApplicableNodeId(RequestContext reqContext)
	
	protected Integer getApplicableNodeIdVendorName(String tenantId, String siteId, String featureGroup, String featureName,String implName,String vendorName,String version)
			throws InvalidNodeTreeException, ConfigPersistenceException {
		logger.debug("Finding ParentNodeId for Tenant=" + tenantId + "-siteId=" + siteId + "-featureGroup=" + featureGroup + "-featureName="
				+ featureName+", impl name ="+implName+", vendor Name : "+vendorName+", version : "+version);

		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		return configPersistenceService.getApplicableNodeId(tenantId, siteId, featureGroup, featureName,implName,vendorName,version);
	}// end of method getApplicableNodeIdVendorName
	
	protected Integer getApplicableNodeIdFeatureName(String tenantId, String siteId, String featureGroup, String featureName,String implName)
			throws InvalidNodeTreeException, ConfigPersistenceException {
		logger.debug("Finding ParentNodeId for Tenant=" + tenantId + "-siteId=" + siteId + "-featureGroup=" + featureGroup + "-featureName="
				+ featureName+", impl name="+implName);

		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		return configPersistenceService.getApplicableNodeId(tenantId, siteId, featureGroup, featureName,implName,null,null);
	}// end of method getApplicableNodeIdFeatureName

}
