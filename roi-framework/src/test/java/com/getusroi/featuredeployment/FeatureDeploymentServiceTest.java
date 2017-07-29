package com.getusroi.featuredeployment;

import java.io.Serializable;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.util.GenericTestConstant;
import com.getusroi.core.datagrid.DataGridService;
import com.getusroi.featuredeployment.impl.FeatureDeploymentService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;



public class FeatureDeploymentServiceTest {
	final Logger logger = LoggerFactory.getLogger(FeatureDeploymentServiceTest.class);
	
	
	
	@Test
	public void testAddFeatureDeployment() throws FeatureDeploymentServiceException{
		logger.debug(".testAddFeatureDeployment method of FeatureDeploymentServiceTest");
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.TEST_TENANTID, GenericTestConstant.TEST_SITEID, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE, GenericTestConstant.TEST_IMPL_NAME, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		IFeatureDeployment featureDeploymentService=new FeatureDeploymentService();
		featureDeploymentService.addFeatureDeployement(configContext, true, false, true);
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
		String cacheMapKey=createKeyForFeatureNameAndSiteId(GenericTestConstant.TEST_FEATURE,23);
		List<FeatureDeployment> featureDeploymentList=(List<FeatureDeployment>)map.get(cacheMapKey);
		Assert.assertNotNull("featureDeployment List", featureDeploymentList);
		featureDeploymentService.deleteFeatureDeployed(configContext);
	}
	
	@Test
	public void testActiveAndPrimaryFeatureFromFeatureDeployment() throws FeatureDeploymentServiceException{
		logger.debug(".testActiveAndPrimaryFeatureFromFeatureDeployment method of FeatureDeploymentServiceTest");
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.TEST_TENANTID, GenericTestConstant.TEST_SITEID, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE, GenericTestConstant.TEST_IMPL_NAME, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		ConfigurationContext configContext1=new ConfigurationContext(GenericTestConstant.TEST_TENANTID, GenericTestConstant.TEST_SITEID, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE,"key2act", GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		IFeatureDeployment featureDeploymentService=new FeatureDeploymentService();
		featureDeploymentService.addFeatureDeployement(configContext, true, false, true);
		featureDeploymentService.addFeatureDeployement(configContext1, true, true, true);
		FeatureDeployment featuredeployment =featureDeploymentService.getActiveAndPrimaryFeatureDeployedFromCache(GenericTestConstant.TEST_TENANTID, GenericTestConstant.TEST_SITEID, GenericTestConstant.TEST_FEATURE);
		logger.debug("active feature deployement : "+featuredeployment);
		Assert.assertNotNull("active feature deployment is not null : ",featuredeployment);
		Assert.assertEquals("active feature deployment is not null and impl is key2act : ","key2act",featuredeployment.getImplementationName().trim());
		featureDeploymentService.deleteFeatureDeployed(configContext);
		featureDeploymentService.deleteFeatureDeployed(configContext1);
	}
	
	@Test
	public void updateFeatureDeploymentFromNotPrimaryToPrimaryTest() throws FeatureDeploymentServiceException{
		logger.debug(".updateFeatureDeploymentFromNotPrimaryToPrimaryTest method of FeatureDeploymentServiceTest");
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.TEST_TENANTID, GenericTestConstant.TEST_SITEID, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE, GenericTestConstant.TEST_IMPL_NAME, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		IFeatureDeployment featureDeploymentService=new FeatureDeploymentService();
		featureDeploymentService.addFeatureDeployement(configContext, true, false, true);
		FeatureDeployment featuredeployment =featureDeploymentService.getActiveAndPrimaryFeatureDeployedFromCache(GenericTestConstant.TEST_TENANTID, GenericTestConstant.TEST_SITEID, GenericTestConstant.TEST_FEATURE);
		logger.debug("active feature deployement : "+featuredeployment);
		Assert.assertNull("active feature deployment is  null : ",featuredeployment);
		featureDeploymentService.updateFeatureDeployed(configContext, true,true);
		FeatureDeployment featuredeploymentAfterUpdate =featureDeploymentService.getActiveAndPrimaryFeatureDeployedFromCache(GenericTestConstant.TEST_TENANTID, GenericTestConstant.TEST_SITEID, GenericTestConstant.TEST_FEATURE);
		logger.debug("active feature deployement : "+featuredeploymentAfterUpdate);
		Assert.assertNotNull("active feature deployment is not null : ",featuredeploymentAfterUpdate);
		featureDeploymentService.deleteFeatureDeployed(configContext);
	}
	
	@Test
	public void updateFeatureDeploymentFromPrimaryToNotPrimaryTest() throws FeatureDeploymentServiceException{
		logger.debug(".updateFeatureDeploymentFromPrimaryToNotPrimaryTest method of FeatureDeploymentServiceTest");
		ConfigurationContext configContext=new ConfigurationContext(GenericTestConstant.TEST_TENANTID, GenericTestConstant.TEST_SITEID, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE, GenericTestConstant.TEST_IMPL_NAME, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		IFeatureDeployment featureDeploymentService=new FeatureDeploymentService();
		featureDeploymentService.addFeatureDeployement(configContext, true, true, true);
		FeatureDeployment featuredeployment =featureDeploymentService.getActiveAndPrimaryFeatureDeployedFromCache(GenericTestConstant.TEST_TENANTID, GenericTestConstant.TEST_SITEID, GenericTestConstant.TEST_FEATURE);
		logger.debug("active feature deployement : "+featuredeployment);
		Assert.assertNotNull("active feature deployment is  not null : ",featuredeployment);
		featureDeploymentService.updateFeatureDeployed(configContext, false,true);
		FeatureDeployment featuredeploymentAfterUpdate =featureDeploymentService.getActiveAndPrimaryFeatureDeployedFromCache(GenericTestConstant.TEST_TENANTID, GenericTestConstant.TEST_SITEID, GenericTestConstant.TEST_FEATURE);
		logger.debug("active feature deployement : "+featuredeploymentAfterUpdate);
		Assert.assertNull("active feature deployment is  null : ",featuredeploymentAfterUpdate);
		featureDeploymentService.deleteFeatureDeployed(configContext);
	}
	
	@After
	public void testGetALLDataFromCache(){
		logger.debug(".getALLDataFromCache method of FeatureDeploymentServiceTest");
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
		String cacheMapKey=createKeyForFeatureNameAndSiteId(GenericTestConstant.TEST_FEATURE,23);
		List<FeatureDeployment> featureDeploymentList=(List<FeatureDeployment>)map.get(cacheMapKey);
		logger.debug("featureDeploymentList in test : "+featureDeploymentList);
	}
	
	private String createKeyForFeatureNameAndSiteId(String featureName, int siteId) {		
		return (featureName+"-"+siteId).trim();
	}

	private String getGlobalFeatureDeploymentKey() {
		return "GlobalFeatureDeployment".trim();
	}
}
