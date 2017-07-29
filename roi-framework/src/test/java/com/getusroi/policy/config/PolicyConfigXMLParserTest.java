package com.getusroi.policy.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.policy.PolicyTestConstant;
import com.getusroi.policy.jaxb.Policies;
import com.getusroi.policy.jaxb.Policy;

public class PolicyConfigXMLParserTest {
	final Logger logger = LoggerFactory.getLogger(PolicyConfigXMLParserTest.class);
	
	private String goodConfigFile;
	private String invalidConfigFile;
	PolicyConfigXMLParser parser;
	
	@Before
	public void getFileObject() throws PolicyConfigXMLParserException{
		parser=new PolicyConfigXMLParser();
		InputStream inputstream= PolicyConfigXMLParserTest.class.getClassLoader().getResourceAsStream(PolicyTestConstant.policyconfigfileToParse);
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
		
		
		goodConfigFile=policyConfigxml;
		
			 inputstream= PolicyConfigXMLParserTest.class.getClassLoader().getResourceAsStream(PolicyTestConstant.invalidconfigfileToParse);
	
		 reader = new BufferedReader(new InputStreamReader(inputstream));
		 out1 = new StringBuilder();
		 line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			    
			}
			reader.close();
		} catch (IOException e) {
		throw new PolicyConfigXMLParserException("policy file doesnot exist in classpath",e);
		}
		
		 policyConfigxml=out1.toString();
		invalidConfigFile=policyConfigxml;
	}
	
	@Test
	public void testPolicyConfigReading() throws PolicyConfigXMLParserException{
		
		Policies policiesConfig=parser.marshallXMLtoObject(goodConfigFile);
		Assert.assertNotNull("Policies  must not be Null",policiesConfig);
		List<Policy> polcyConfigList=policiesConfig.getPolicy();
		Policy config=(Policy)polcyConfigList.get(0);
		Assert.assertEquals("Policy Config name must be PolicyDefinedFactTest",config.getPolicyName(),"PolicyDefinedFactTest");
		
	}
	

	@Test(expected=com.getusroi.policy.config.PolicyConfigXMLParserException.class)
	public void testBadPolicyConfigReadingFailure() throws PolicyConfigXMLParserException {
		parser.marshallConfigXMLtoObject(invalidConfigFile);
	}
	
	@Test
	public void testUnmarshallObjecttoXML() throws PolicyConfigXMLParserException{
		Policies policiesConfig=parser.marshallXMLtoObject(goodConfigFile);
		List<Policy> policyList=policiesConfig.getPolicy();
		Policy config=(Policy)policyList.get(0);
		String configXml=parser.unmarshallObjecttoXML(config);
		Assert.assertNotNull("Policy Configuration xml should not be null and should be valid xml String",configXml);
	}
	
	@Test
	public void testMarshallXMLtoObject() throws PolicyConfigXMLParserException{
		Policies policiesConfig=parser.marshallXMLtoObject(goodConfigFile);
		List<Policy> permaStoreConfigList=policiesConfig.getPolicy();
		Policy config=(Policy)permaStoreConfigList.get(0);
		String configXml=parser.unmarshallObjecttoXML(config);
		
		Policies psconfigs=parser.marshallXMLtoObject(configXml);
		Policy psconfig2=psconfigs.getPolicy().get(0);
		logger.debug("sent="+config.getPolicyName()+"--received="+psconfig2.getPolicyName());
		Assert.assertEquals(config.getPolicyName(), psconfig2.getPolicyName());
		
	}
}
