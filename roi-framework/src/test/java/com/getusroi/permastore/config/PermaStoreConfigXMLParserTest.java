package com.getusroi.permastore.config;

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

import com.getusroi.permastore.config.impl.PermaStoreConfigXMLParser;
import com.getusroi.permastore.config.jaxb.PermaStoreConfiguration;
import com.getusroi.permastore.config.jaxb.PermaStoreConfigurations;

public class PermaStoreConfigXMLParserTest {
	final Logger logger = LoggerFactory.getLogger(PermaStoreConfigXMLParserTest.class);
	//private static String configfileToParse="PermaStoreConfig.xml";
	//private static String invalidconfigfileToParse="BadPermaStoreConfig.xml";
	
	private String goodConfigFile;
	private String invalidConfigFile;
	PermaStoreConfigXMLParser parser;
	
	@Before
	public void getFileObject() throws PermaStoreConfigParserException{
		parser=new PermaStoreConfigXMLParser();
		InputStream inputstream= PermaStoreConfigXMLParser.class.getClassLoader().getResourceAsStream(PermaStoreTestConstant.configfileToParse);
		
		
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
		
		goodConfigFile=out1.toString();
		
		 inputstream= PermaStoreConfigXMLParser.class.getClassLoader().getResourceAsStream(PermaStoreTestConstant.invalidconfigfileToParse);
		
		
		 reader = new BufferedReader(new InputStreamReader(inputstream));
		 out1 = new StringBuilder();
		 line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			    
			}
			reader.close();
		} catch (IOException e) {
		throw new PermaStoreConfigParserException("permastore file doesnot exist in classpath",e);
		}
		
		invalidConfigFile=out1.toString();
	}
	
	@Test
	public void testPermaStoreConfigReading() throws PermaStoreConfigParserException{
		
		PermaStoreConfigurations permaStoreConfig=parser.marshallConfigXMLtoObject(goodConfigFile);
		Assert.assertNotNull("PermastoreConfig must not be Null",permaStoreConfig);
		List<PermaStoreConfiguration> permaStoreConfigList=permaStoreConfig.getPermaStoreConfiguration();
		PermaStoreConfiguration config=(PermaStoreConfiguration)permaStoreConfigList.get(0);
		Assert.assertEquals("PermastoreConfig name must be AreaList",config.getName(),"AreaList");
		
	}
	

	@Test(expected=com.getusroi.permastore.config.PermaStoreConfigParserException.class)
	public void testBadPermaStoreConfigReadingFailure() throws PermaStoreConfigParserException {
		parser.marshallConfigXMLtoObject(invalidConfigFile);
	}
	
	@Test
	public void testUnmarshallObjecttoXML() throws PermaStoreConfigParserException{
		PermaStoreConfigurations permaStoreConfig=parser.marshallXMLtoObject(goodConfigFile);
		List<PermaStoreConfiguration> permaStoreConfigList=permaStoreConfig.getPermaStoreConfiguration();
		PermaStoreConfiguration config=(PermaStoreConfiguration)permaStoreConfigList.get(0);
		String configXml=parser.unmarshallObjecttoXML(config);
		Assert.assertNotNull("Configuration xml should not be null and should be valid xml String",configXml);
	}
	
	@Test
	public void testMarshallXMLtoObject() throws PermaStoreConfigParserException{
		PermaStoreConfigurations permaStoreConfig=parser.marshallXMLtoObject(goodConfigFile);
		List<PermaStoreConfiguration> permaStoreConfigList=permaStoreConfig.getPermaStoreConfiguration();
		PermaStoreConfiguration config=(PermaStoreConfiguration)permaStoreConfigList.get(0);
		String configXml=parser.unmarshallObjecttoXML(config);
		
		PermaStoreConfigurations psconfigs=parser.marshallXMLtoObject(configXml);
		PermaStoreConfiguration psconfig2=psconfigs.getPermaStoreConfiguration().get(0);
		logger.debug("sent="+config.getName()+"--received="+psconfig2.getName());
		Assert.assertEquals(config.getName(), psconfig2.getName());
		
	}
	
}
