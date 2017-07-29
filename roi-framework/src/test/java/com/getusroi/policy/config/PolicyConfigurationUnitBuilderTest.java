package com.getusroi.policy.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.util.GenericTestConstant;
import com.getusroi.permastore.config.PermaStoreConfigParserException;
import com.getusroi.policy.config.impl.PolicyConfigurationUnitBuilder;
import com.getusroi.policy.config.impl.PolicyFactBuilderException;
import com.getusroi.policy.jaxb.Policies;
import com.getusroi.policy.jaxb.Policy;

/**
 * Policy Configuration Builder Test Class
 * @author bizruntime
 *
 */
public class PolicyConfigurationUnitBuilderTest {
	final Logger logger = LoggerFactory.getLogger(PolicyConfigurationUnitBuilderTest.class);
	private static final String TEST_POLICYXML="Policy-test1.xml";
	
	private  List<Policy> policyConfigList;
	private  PolicyConfigurationUnitBuilder policyConfigUnitBuilder;
	
	
	/**
	 * This method is to get the policy xml file and marshal to jaxb generated pojo class
	 * @return Policies object
	 * @throws PolicyConfigXMLParserException
	 */
	private  Policies getPolicyConfiguration() throws PolicyConfigXMLParserException {
		logger.debug("inside getPolicyConfiguration() of PolicyConfigurationUnitBuilderTest");
		PolicyConfigXMLParser parser=new PolicyConfigXMLParser();
		InputStream inputstream= PolicyConfigXMLParser.class.getClassLoader().getResourceAsStream(TEST_POLICYXML);
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
		
		String policyConfigxml=out1.toString();
		Policies policies=parser.marshallXMLtoObject(policyConfigxml);
		
		return policies;
	}
	
	/**
	 * This method is to get policy by name
	 * @param name : Name of the policy
	 * @return Policy Object
	 */
	private Policy getPolicyByName(String name){
		logger.debug("inside getPolicyByName() of PolicyConfigurationUnitBuilderTest");
		for(Policy pol:policyConfigList){
			if(pol.getPolicyName().equalsIgnoreCase(name))
				return pol;
		}
		return null;
	}
	
	
	/**
	 * This method is used to load the policy configuration into cache
	 * @throws PolicyConfigurationException
	 * @throws ConfigPersistenceException
	 * @throws PolicyConfigXMLParserException
	 */
	@Before
	public  void loadConfigurations() throws PolicyConfigurationException, ConfigPersistenceException, PolicyConfigXMLParserException {
		logger.debug("inside loadConfigurations() of PolicyConfigurationUnitBuilderTest");
		Policies policyConfig=getPolicyConfiguration();
		this.policyConfigList=policyConfig.getPolicy();
		this.policyConfigUnitBuilder=new PolicyConfigurationUnitBuilder();
	}
	
	
	/**
	 * This method is used to test when policyResponse type is PolicyDefinedFact 
	 * @throws PolicyConfigurationException
	 * @throws PolicyFactBuilderException
	 */
	@Test
	public void testSimplePolicyDefinedFact() throws PolicyConfigurationException, PolicyFactBuilderException {
		logger.debug("inside testSimplePolicyDefinedFact() of PolicyConfigurationUnitBuilderTest");
		Policy policy=getPolicyByName("PolicyDefinedFactTest");
		PolicyConfigurationUnit psConfigUnit = new PolicyConfigurationUnit(GenericTestConstant.TEST_TENANTID,GenericTestConstant.getSite(),GenericTestConstant.TEST_NODEID, true, policy, null);
		policyConfigUnitBuilder.buildPolicyConfigUnit(policy, psConfigUnit);
		Assert.assertNotNull("Configuration policy is not null",psConfigUnit);
		Serializable responseToCache=psConfigUnit.getConfigData();
		
		logger.debug("policyDefideFact --------------  "+responseToCache);
		Assert.assertNotNull("responseToCache is not null",responseToCache);

	}
	
	
	/**
	 * This method is used to test when policyResponse type is PolicyDefinedFact  and factDescription type is Map
	 * @throws PolicyConfigurationException
	 * @throws PolicyFactBuilderException
	 */
	@Test
	public void testSimplePolicyDefinedFactMapTest() throws PolicyConfigurationException, PolicyFactBuilderException {
		logger.debug("inside testSimplePolicyDefinedFactMapTest() of PolicyConfigurationUnitBuilderTest");
		Policy policy=getPolicyByName("PolicyDefinedFactMapTest");
		PolicyConfigurationUnit psConfigUnit = new PolicyConfigurationUnit(GenericTestConstant.TEST_TENANTID,GenericTestConstant.getSite(),GenericTestConstant.TEST_NODEID, true, policy, null);
		policyConfigUnitBuilder.buildPolicyConfigUnit(policy, psConfigUnit);
		Serializable responseToCache=psConfigUnit.getConfigData();
		Assert.assertNotNull("responseToCache is not null",responseToCache);
		Assert.assertTrue(responseToCache instanceof HashMap);

		if(responseToCache instanceof HashMap){
			logger.debug("response to cache is instance of HashMap");
			HashMap map=(HashMap)responseToCache;
			String daysValue=(String)map.get("days");
			logger.debug("policyDefideFact --------------  "+daysValue);

			Assert.assertEquals("PolicyDefinedFactMap days key should get {12345}",daysValue,"12345");

		}
	
	}
	
	
	/**
	 * This method is used to test when policyResponse type is PolicyDefinedFact  and factDescription type is List
	 * @throws PolicyConfigurationException
	 * @throws PolicyFactBuilderException
	 */
	@Test
	public void testSimplePolicyDefinedFactListTest() throws PolicyConfigurationException, PolicyFactBuilderException {
		logger.debug("inside testSimplePolicyDefinedFactListTest() of PolicyConfigurationUnitBuilderTest");
		Policy policy=getPolicyByName("PolicyDefinedFactListTest");
		PolicyConfigurationUnit psConfigUnit = new PolicyConfigurationUnit(GenericTestConstant.TEST_TENANTID,GenericTestConstant.getSite(),GenericTestConstant.TEST_NODEID, true, policy, null);
		policyConfigUnitBuilder.buildPolicyConfigUnit(policy, psConfigUnit);
		Serializable responseToCache=psConfigUnit.getConfigData();
		Assert.assertNotNull("responseToCache is not null",responseToCache);
		Assert.assertTrue(responseToCache instanceof ArrayList);
		if(responseToCache instanceof ArrayList){
			logger.debug("response to cache is instance of List");
			ArrayList list=(ArrayList)responseToCache;
			String daysValue=(String)list.get(0);
			List listofData=(List)list.get(1);
			
			Assert.assertEquals("PolicyDefinedFactList 0th index should get {12345}",daysValue,"12345");
			Assert.assertNotNull("PolicyDefinedFactMap 1st index should get List Object",listofData);
			Assert.assertTrue(listofData.contains("PCK"));

		}
	
	}
	
	
	/**
	 * This method is used to test when policyResponse type is PolicyDefinedFact, factDescription type is Map and
	 * fact attributes are primitive 
	 * @throws PolicyConfigurationException
	 * @throws PolicyFactBuilderException
	 */
	@Test
	public void testSimplePolicyDefinedFactAttributePrimitiveTest() throws PolicyConfigurationException, PolicyFactBuilderException {
		logger.debug("inside testSimplePolicyDefinedFactAttributePrimitiveTest() of PolicyConfigurationUnitBuilderTest");
		Policy policy=getPolicyByName("PolicyDefinedFactAttPrimitiveTest");
		PolicyConfigurationUnit psConfigUnit = new PolicyConfigurationUnit(GenericTestConstant.TEST_TENANTID,GenericTestConstant.getSite(),GenericTestConstant.TEST_NODEID, true, policy, null);
		policyConfigUnitBuilder.buildPolicyConfigUnit(policy, psConfigUnit);
		Serializable responseToCache=psConfigUnit.getConfigData();
		Assert.assertNotNull("responseToCache is not null",responseToCache);
		
		Assert.assertTrue(responseToCache instanceof HashMap);
		if(responseToCache instanceof HashMap){
			logger.debug("response to cache is instance of HashMap for primitive");
			HashMap map=(HashMap)responseToCache;
			int daysValue=(Integer)map.get("days");
			List listData=(List)map.get("validoperation");
			boolean booleanValue=(Boolean)map.get("day");
			long longValue=(Long)map.get("seconds");
			double doubleValue=(Double)map.get("price");
			float floatvalue=(Float)map.get("amount");
			Assert.assertEquals("PolicyDefinedFactMap days key should get {12345}",daysValue,34);
			Assert.assertNotNull("PolicyDefinedFactMap validoperation key should should not be null",listData);
			Assert.assertTrue(listData.contains("PCK"));
			Assert.assertEquals("PolicyDefinedFactMap day key should get {false}",booleanValue,false);
			Assert.assertEquals("PolicyDefinedFactMap seconds key should get {78799}",longValue,78799);
			Assert.assertEquals("PolicyDefinedFactMap price key should get {34.56}",doubleValue,34.56,0.00);
			Assert.assertEquals("PolicyDefinedFactMap amount key should get {34.5}",floatvalue,34.5,0.0);

		}
		
	}
		
		/**
		 * This method is used to test when policyResponse type is PolicyDefinedFact, factDescription type is Map and
		 * fact attributes are Date 
		 * @throws PolicyConfigurationException
		 * @throws PolicyFactBuilderException
		 */
		@Test
		public void testSimplePolicyDefinedFactAttributeDateTest() throws PolicyConfigurationException, PolicyFactBuilderException {
			logger.debug("inside testSimplePolicyDefinedFactAttributeDateTest() of PolicyConfigurationUnitBuilderTest");
			Policy policy=getPolicyByName("PolicyDefinedFactAttDateTest");
			PolicyConfigurationUnit psConfigUnit = new PolicyConfigurationUnit(GenericTestConstant.TEST_TENANTID,GenericTestConstant.getSite(),GenericTestConstant.TEST_NODEID, true, policy, null);
			policyConfigUnitBuilder.buildPolicyConfigUnit(policy, psConfigUnit);
			Serializable responseToCache=psConfigUnit.getConfigData();
			Assert.assertNotNull("responseToCache is not null",responseToCache);
			logger.debug("policy configuration for date fact attribute in not null");
			
			
			Assert.assertTrue(responseToCache instanceof HashMap);
			if(responseToCache instanceof HashMap){
				logger.debug("response to cache is instance of HashMap for date");
				HashMap map=(HashMap)responseToCache;
				String dateValue=(String)map.get("date");
				logger.debug("date value : "+dateValue);
				Assert.assertNotNull("PolicyDefinedFactMap date key nut null",dateValue);
				Assert.assertEquals("PolicyDefinedFactMap date key should get {12/14/2015}",dateValue,"12/14/2015");


			}
	
	}
	
	
	/**
	 * This method is used to test when policyResponse type is mappedFact
	 * @throws PolicyConfigurationException
	 * @throws PolicyFactBuilderException
	 */
	@Test
	public void testSimplePSMappedFact() throws PolicyConfigurationException, PolicyFactBuilderException {
		logger.debug("inside testSimplePSMappedFact() of PolicyConfigurationUnitBuilderTest");
		Policy policy=getPolicyByName("FactMappingTest");
		//logger.debug("policy="+policy.getPolicyResponse().toString());
		PolicyConfigurationUnit psConfigUnit = new PolicyConfigurationUnit(GenericTestConstant.TEST_TENANTID,GenericTestConstant.getSite(),GenericTestConstant.TEST_NODEID, true, policy, null);
		policyConfigUnitBuilder.buildPolicyConfigUnit(policy, psConfigUnit);
		String mappedPSVarName=(String)psConfigUnit.getConfigData();
		
		logger.debug("MappedFact value --------- "+mappedPSVarName);
		Assert.assertNotNull("MappedFact should not be null",mappedPSVarName);
		Assert.assertEquals("MappedFact PS Variable should be {GetStagingAreas}",mappedPSVarName,"GetStagingAreas");
	}
}
