package com.getusroi.feature.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.feature.config.FeatureConfigParserException;
import com.getusroi.feature.config.impl.FeatureConfigXMLParser;
import com.getusroi.feature.jaxb.Feature;
import com.getusroi.feature.jaxb.FeaturesServiceInfo;


/**
 * This class is to test the featureservice.xml validation and parsing with its respective xsd
 * @author bizruntime
 *
 */
public class FeatureConfigXMLParserTest {
	final Logger logger = LoggerFactory.getLogger(FeatureConfigXMLParserTest.class);
	
	private String goodFeatureConfigFile;
	FeatureConfigXMLParser featureConfigXmlParser;
	
	@Before
	public void getFeatureFileObject() throws FeatureConfigParserException{
		featureConfigXmlParser=new FeatureConfigXMLParser();
		InputStream  inputstream= FeatureConfigXMLParserTest.class.getClassLoader().getResourceAsStream(FeatureTestConstant.configfileToParse1);
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
		
		goodFeatureConfigFile=out1.toString();
		
		
	}
	
	@Test
	public void testFeatureConfigReading() throws FeatureConfigParserException{
		FeaturesServiceInfo featureConfig=featureConfigXmlParser.marshallConfigXMLtoObject(goodFeatureConfigFile);
		Assert.assertNotNull("FeatureConfig must not be Null",featureConfig);
		Feature config=featureConfig.getFeatures().getFeature();
		Assert.assertEquals("FeatureConfig name must be labelservice",config.getFeatureName(),FeatureTestConstant.TEST_FEATURE);
		
	}
	
	@Test
	public void testUnmarshallObjecttoXML() throws FeatureConfigParserException{
		FeaturesServiceInfo featureConfig=featureConfigXmlParser.marshallConfigXMLtoObject(goodFeatureConfigFile);
		String featureGroup=featureConfig.getFeatures().getFeatureGroup();
		Feature config=featureConfig.getFeatures().getFeature();
		logger.debug("feataure name : "+config.getFeatureName());
		String configXml=featureConfigXmlParser.unmarshallObjecttoXML(config,featureGroup);
		Assert.assertNotNull("Configuration xml should not be null and should be valid xml String",configXml);
		
	}
	
	@Test
	public void testMarshallXMLtoObject() throws FeatureConfigParserException{
		FeaturesServiceInfo featureConfig=featureConfigXmlParser.marshallConfigXMLtoObject(goodFeatureConfigFile);
		String featureGroup=featureConfig.getFeatures().getFeatureGroup();
		Feature config=featureConfig.getFeatures().getFeature();
		String configXml=featureConfigXmlParser.unmarshallObjecttoXML(config,featureGroup);
		
		FeaturesServiceInfo featureServiceInfo=featureConfigXmlParser.marshallXMLtoObject(configXml);
		Feature feature=featureServiceInfo.getFeatures().getFeature();
		logger.debug("sent="+config.getFeatureName()+"--received="+feature.getFeatureName());
		Assert.assertEquals(config.getFeatureName(), feature.getFeatureName());
		
	}

	
}
