package com.getusroi.feature.config.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.getusroi.feature.config.FeatureConfigParserException;
import com.getusroi.feature.jaxb.Feature;
import com.getusroi.feature.jaxb.Features;
import com.getusroi.feature.jaxb.FeaturesServiceInfo;


public class FeatureConfigXMLParser {

	final Logger logger = LoggerFactory.getLogger(FeatureConfigXMLParser.class);
	public static final String SCHEMA_NAME="featureService.xsd";
	
	/** Validates File for against the XML SCHEMA FOR feature Configurations*/
	private  void validateXml(String configXMLFile) throws FeatureConfigParserException {
		//String fileName=configXMLFile.getName();
		try {
			
			logger.debug(".validateXml() Custom Error Handler while Validating XML against XSD");
	        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Schema schema = factory.newSchema( FeatureConfigXMLParser.class.getClassLoader().getResource(SCHEMA_NAME));
	        Validator validator = schema.newValidator();
	        StringReader stringReader=new StringReader(configXMLFile);
	        validator.validate(new StreamSource(stringReader));
	        logger.debug("Validation is successful");
	    } catch (IOException  | SAXException exp) {
	    	logger.error("Feature Config XML Schema Validation Failed for file ",exp);
	    	throw new FeatureConfigParserException("Feature Config XML Schema Validation Failed for file ",exp);
	    } 
	}
	
	public FeaturesServiceInfo marshallConfigXMLtoObject(String configXMLFile) throws FeatureConfigParserException{
		//String fileName=configXMLFile.getName();
		try {
					validateXml(configXMLFile);
					JAXBContext jaxbContext = JAXBContext.newInstance(FeaturesServiceInfo.class);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					 InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));
					FeaturesServiceInfo featureconfigurations=(FeaturesServiceInfo)jaxbUnmarshaller.unmarshal(inputSourceConfigXml);
					return featureconfigurations;					
		  } catch (JAXBException exp) {
			  logger.error("FeaturesServiceInfoConfig XMLParsing Failed for file ",exp);
		      throw new FeatureConfigParserException("FeaturesServiceInfoConfig XMLParsing Failed for file ",exp);
		  }
	}

	public FeaturesServiceInfo marshallXMLtoObject(String featureConfigxml) throws FeatureConfigParserException{
		try {			
			logger.debug(".marshallXMLtoObject() xmlString is ["+featureConfigxml+"]");
			JAXBContext jaxbContext = JAXBContext.newInstance(com.getusroi.feature.jaxb.ObjectFactory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			FeaturesServiceInfo featureconfigurations=(FeaturesServiceInfo)jaxbUnmarshaller.unmarshal(new StringReader(featureConfigxml));
			return featureconfigurations;			
		  } catch (Exception exp) {
			  logger.error("FeatureConfig XMLParsing Failed ",exp);
		      throw new FeatureConfigParserException("FeatureConfig XMLParsing Failed ",exp);
		  }
	}
	
	public String unmarshallObjecttoXML(Feature feature,String featureGroup) throws FeatureConfigParserException {
		try {
			FeaturesServiceInfo featureServiceconfigs=new FeaturesServiceInfo();
			Features features=new Features();
			features.setFeatureGroup(featureGroup);
			features.setFeature(feature);
			featureServiceconfigs.setFeatures(features);
			logger.debug("features object :"+features);
			JAXBContext jaxbContext = JAXBContext.newInstance(FeaturesServiceInfo.class);
			Marshaller marshaller = jaxbContext.createMarshaller();	
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(featureServiceconfigs,writer);			
			String theXML = writer.toString();
			logger.debug("xmlObjecttoXML()=["+theXML+"]");
			return theXML;
		} catch (JAXBException e) {
			logger.error("Failed to convert featureConfiguration back to XML", e);
			throw new FeatureConfigParserException("Failed to convert FeatureConfiguration back to XML ",e);
		}
	}
	
}
