package com.attunedlabs.datacontext.config.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

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

import com.attunedlabs.datacontext.config.DataContextParserException;
import com.attunedlabs.datacontext.jaxb.DataContextDef;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.feature.config.FeatureConfigParserException;
import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;
import com.attunedlabs.feature.jaxb.Feature;
import com.attunedlabs.feature.jaxb.Features;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;

/**
 * This class is used for datacontext xml marshaling and Unmarshaling
 * @author bizruntime
 *
 */
public class DataContextConfigXMLParser {
	final Logger logger = LoggerFactory.getLogger(DataContextConfigXMLParser.class);
	public static final String SCHEMA_NAME="featureDataContext.xsd";
	
	/** Validates File for against the XML SCHEMA FOR data context Configurations
	 * @throws DataContextParserException */
	private  void validateXml(String configXMLFile) throws DataContextParserException {
		try {			
			logger.debug(".validateXml() Custom Error Handler while Validating XML against XSD");
	        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Schema schema = factory.newSchema( FeatureConfigXMLParser.class.getClassLoader().getResource(SCHEMA_NAME));
	        Validator validator = schema.newValidator();
	        StringReader stringReader=new StringReader(configXMLFile);
	        validator.validate(new StreamSource(stringReader));
	        logger.debug("Validation is successful");
	    } catch (IOException  | SAXException exp) {
	    	logger.error("datacontext Config XML Schema Validation Failed for file ",exp);
	    	throw new DataContextParserException("datacontext Config XML Schema Validation Failed for file ",exp);
	    } 
	}//end of method
	
	public FeatureDataContext marshallConfigXMLtoObject(String configXMLFile) throws DataContextParserException{
		try {
				validateXml(configXMLFile);
				JAXBContext jaxbContext = JAXBContext.newInstance(FeatureDataContext.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));
				FeatureDataContext featureDataContext=(FeatureDataContext)jaxbUnmarshaller.unmarshal(inputSourceConfigXml);					
				return featureDataContext;					
		  } catch (JAXBException exp) {
			  logger.error("datacontext Config XMLParsing Failed for file ",exp);
		      throw new DataContextParserException("datacontext config XMLParsing Failed for file ",exp);
		  }
	}//end of method
	
	public FeatureDataContext marshallXMLtoObject(String featureDataContextConfigxml) throws DataContextParserException{
		try {			
			logger.debug(".marshallXMLtoObject() xmlString is ["+featureDataContextConfigxml+"]");
			JAXBContext jaxbContext = JAXBContext.newInstance(com.attunedlabs.datacontext.jaxb.ObjectFactory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			FeatureDataContext featureDataContext=(FeatureDataContext)jaxbUnmarshaller.unmarshal(new StringReader(featureDataContextConfigxml));
			
			return featureDataContext;
			
		  } catch (Exception exp) {
			  logger.error("FeatureDataContextConfig XMLParsing Failed ",exp);
		      throw new DataContextParserException("FeatureDataContextConfig XMLParsing Failed ",exp);
		  }
	}
	
	public String unmarshallObjecttoXML(FeatureDataContext featureDataContext) throws DataContextParserException {
		try {			
			JAXBContext jaxbContext = JAXBContext.newInstance(FeatureDataContext.class);
			Marshaller marshaller = jaxbContext.createMarshaller();	
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(featureDataContext,writer);			
			String theXML = writer.toString();
			logger.debug("xmlObjecttoXML()=["+theXML+"]");
			return theXML;
		} catch (JAXBException e) {
			logger.error("Failed to convert feature data context back to XML", e);
			throw new DataContextParserException("Failed to convert feature data context back to XML ",e);
		}
	}//end of method
	
}
