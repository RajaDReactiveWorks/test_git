package com.getusroi.dynastore.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.getusroi.dynastore.config.impl.DynaStoreConfigXmlParser;
import com.getusroi.dynastore.config.jaxb.DynastoreConfiguration;
import com.getusroi.dynastore.config.jaxb.DynastoreConfigurations;

public class DynaStoreConfigXmlParserTest {
	
	private String invalidConfigFile;
	private String goodConfigFile;
	DynaStoreConfigXmlParser dynaStoreConfigXmlParser;

	@Before
	public void getFileObject() throws DynaStoreConfigParserException{
		InputStream inputstream= DynaStoreConfigXmlParserTest.class.getClassLoader().getResourceAsStream(DynaStoreTestConstant.configfileToParse);
	
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			  	}
			reader.close();
		} catch (IOException e) {
		throw new DynaStoreConfigParserException("dynastore file doesnot exist in classpath",e);
		}
		
		goodConfigFile=out1.toString();
		
		inputstream= DynaStoreConfigXmlParserTest.class.getClassLoader().getResourceAsStream(DynaStoreTestConstant.invalidconfigfileToParse);
		
		 reader = new BufferedReader(new InputStreamReader(inputstream));
		 out1 = new StringBuilder();
		 line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			    }
			reader.close();
		} catch (IOException e) {
		throw new DynaStoreConfigParserException("dynastore file doesnot exist in classpath",e);
		}
		
		invalidConfigFile=out1.toString();
	}
	
	@Test
	public void testReadingDynastoreConfig() throws DynaStoreConfigParserException{
		dynaStoreConfigXmlParser=new DynaStoreConfigXmlParser();
	DynastoreConfigurations dynastoreConfigurations=	dynaStoreConfigXmlParser.marshallConfigXMLtoObject(goodConfigFile);
	DynastoreConfiguration dynastoreConfiguration=dynastoreConfigurations.getDynastoreConfiguration().get(0);
	Assert.assertEquals("dynastoreconfiguration must be exist ","PicArea", dynastoreConfiguration.getDynastoreName().getValue());
	
		
	}
	@Test(expected=com.getusroi.dynastore.config.DynaStoreConfigParserException.class)
	public void testBadDynaStoreConfigXml() throws DynaStoreConfigParserException{
		dynaStoreConfigXmlParser=new DynaStoreConfigXmlParser();
		DynastoreConfigurations dynastoreConfigurations=	dynaStoreConfigXmlParser.marshallConfigXMLtoObject(invalidConfigFile);
		
	}
	@Test
	public void testMarshallingAndUnMarshallingOfXmlFile() throws DynaStoreConfigParserException{
		dynaStoreConfigXmlParser=new DynaStoreConfigXmlParser();
		DynastoreConfigurations dynastoreConfigurations=	dynaStoreConfigXmlParser.marshallConfigXMLtoObject(goodConfigFile);
		DynastoreConfiguration dynastoreConfiguration=dynastoreConfigurations.getDynastoreConfiguration().get(0);
		Assert.assertEquals("dynastoreconfiguration must be exist ","PicArea", dynastoreConfiguration.getDynastoreName().getValue());
		
		
		String xmlFile=	dynaStoreConfigXmlParser.unmarshallObjecttoXML(dynastoreConfiguration);
		Assert.assertNotNull("dynastoreconfiguration xml must be exist ",xmlFile );
		
	}
	
	
	
}
