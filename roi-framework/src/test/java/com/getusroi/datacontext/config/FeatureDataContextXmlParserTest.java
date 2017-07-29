package com.getusroi.datacontext.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.datacontext.config.impl.DataContextConfigXMLParser;
import com.getusroi.datacontext.jaxb.FeatureDataContext;
import com.getusroi.feature.config.FeatureConfigXMLParserTest;

public class FeatureDataContextXmlParserTest {
	final Logger logger = LoggerFactory.getLogger(FeatureDataContextXmlParserTest.class);
	private String goodFeatureConfigFile;
	private DataContextConfigXMLParser dataContextXmlParser;
	
	@Before
	public void getFeatureFileObject() throws DataContextParserException {
		dataContextXmlParser=new DataContextConfigXMLParser();
		InputStream  inputstream= FeatureConfigXMLParserTest.class.getClassLoader().getResourceAsStream(FeatureDataContextConstant.configfileToParse);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			    
			}
			reader.close();
		} catch (IOException e) {
		throw new DataContextParserException("data context file doesnot exist in classpath",e);
		}
		
		goodFeatureConfigFile=out1.toString();
		
		
	}
	
	@Test
	public void testFeatureConfigReading() throws DataContextParserException  {
		FeatureDataContext featureDataContext=dataContextXmlParser.marshallConfigXMLtoObject(goodFeatureConfigFile);
		Assert.assertNotNull("data context config must not be Null",featureDataContext);
		String dataContextName=featureDataContext.getDataContexts().getContextName();
		Assert.assertEquals("data context config name must be test-TestService-DataContext",dataContextName,"test-TestService-DataContext");
		
	}
	
	//@Test
	public void testUnmarshallObjecttoXML() throws DataContextParserException {
		FeatureDataContext featureDataContext=dataContextXmlParser.marshallConfigXMLtoObject(goodFeatureConfigFile);
		String configXml=dataContextXmlParser.unmarshallObjecttoXML(featureDataContext);
		Assert.assertNotNull("Configuration xml should not be null and should be valid xml String",configXml);
		
	}
	
	//@Test
	public void testMarshallXMLtoObject() throws DataContextParserException{
		FeatureDataContext featureDataContext=dataContextXmlParser.marshallConfigXMLtoObject(goodFeatureConfigFile);
		String configXml=dataContextXmlParser.unmarshallObjecttoXML(featureDataContext);		
		FeatureDataContext featureDataContext1=dataContextXmlParser.marshallXMLtoObject(configXml);		
		String name1=featureDataContext.getDataContexts().getContextName();
		String name2=featureDataContext1.getDataContexts().getContextName();
		Assert.assertEquals(name1, name2);
		
	}

}
