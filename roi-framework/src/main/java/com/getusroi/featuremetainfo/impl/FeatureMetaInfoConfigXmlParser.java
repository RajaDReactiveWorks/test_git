package com.getusroi.featuremetainfo.impl;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.getusroi.feature.config.impl.FeatureConfigXMLParser;
import com.getusroi.featuremetainfo.FeatureMetaInfoConfigParserException;
import com.getusroi.featuremetainfo.jaxb.*;

public class FeatureMetaInfoConfigXmlParser {
	final Logger logger = LoggerFactory.getLogger(FeatureMetaInfoConfigXmlParser.class);
	public static final String FEATURE_METAINFO_SCHEMA_NAME="featureMetaInfo.xsd";
	
	
	/** Validates File for against the XML SCHEMA FOR FeatureMetaInfo Configurations
	 * @throws FeatureMetaInfoConfigParserException 
	 */
	private  void validateXml(String configXMLFile) throws FeatureMetaInfoConfigParserException {
		try {
			
			logger.debug(".validateXml() Custom Error Handler while Validating XML against XSD");
	        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Schema schema = factory.newSchema( FeatureConfigXMLParser.class.getClassLoader().getResource(FEATURE_METAINFO_SCHEMA_NAME));
	        Validator validator = schema.newValidator();
	        StringReader stringReader=new StringReader(configXMLFile);
	        validator.validate(new StreamSource(stringReader));
	        logger.debug("Validation is successful");
	    } catch (IOException  | SAXException exp) {
	    	logger.error("Feature MetaInfo Config XML Schema Validation Failed for file ",exp);
	    	throw new FeatureMetaInfoConfigParserException("Feature MetaInfo Config XML Schema Validation Failed for file ",exp);
	    } 
	}
	
	
	public FeatureMetainfo marshallConfigXMLtoObject(String configXMLFile) throws FeatureMetaInfoConfigParserException  {
		try {
					validateXml(configXMLFile);
					JAXBContext jaxbContext = JAXBContext.newInstance(FeatureMetainfo.class);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					 InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));
					 FeatureMetainfo featureMetaInfoconfigurations=(FeatureMetainfo)jaxbUnmarshaller.unmarshal(inputSourceConfigXml);
					
					return featureMetaInfoconfigurations;
					
		  } catch (JAXBException exp) {
			  logger.error("FeatureMetaInfoConfig XMLParsing Failed for file ",exp);
		      throw new FeatureMetaInfoConfigParserException("FeatureMetaInfoConfig XMLParsing Failed for file ",exp);
		  }
	}
	
	
	public FeatureMetainfo marshallXMLtoObject(String featureConfigxml) throws FeatureMetaInfoConfigParserException{
		try {
			
			logger.debug(".marshallXMLtoObject() xmlString is ["+featureConfigxml+"]");
			JAXBContext jaxbContext = JAXBContext.newInstance(com.getusroi.featuremetainfo.jaxb.ObjectFactory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			FeatureMetainfo featureMetaInfoconfigurations=(FeatureMetainfo)jaxbUnmarshaller.unmarshal(new StringReader(featureConfigxml));
			
			return featureMetaInfoconfigurations;
			
		  } catch (Exception exp) {
			  logger.error("FeatureMetaInfoConfig XMLParsing Failed ",exp);
		      throw new FeatureMetaInfoConfigParserException("FeatureMetaInfoConfig XMLParsing Failed ",exp);
		  }
	}
}
