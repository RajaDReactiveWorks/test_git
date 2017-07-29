package com.getusroi.permastore.config.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.getusroi.permastore.config.PermaStoreConfigParserException;
import com.getusroi.permastore.config.jaxb.PermaStoreConfiguration;
import com.getusroi.permastore.config.jaxb.PermaStoreConfigurations;

public class PermaStoreConfigXMLParser {
	final Logger logger = LoggerFactory.getLogger(PermaStoreConfigXMLParser.class);
	public static final String SCHEMA_NAME="permastoreconfig.xsd";
	
	/** Validates File for against the XML SCHEMA FOR Permastore Configurations*/
	private  void validateXml(String configXMLFile) throws PermaStoreConfigParserException {
		//String fileName=configXMLFile.getName();
		try {
			
			logger.debug(".validateXml() Custom Error Handler while Validating XML against XSD");
	        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Schema schema = factory.newSchema( PermaStoreConfigXMLParser.class.getClassLoader().getResource(SCHEMA_NAME));
	        Validator validator = schema.newValidator();
	        
	        StringReader stringReader=new StringReader(configXMLFile);
	        validator.validate(new StreamSource(stringReader));
	        logger.debug("Validation is successful");
	    } catch (IOException  | SAXException exp) {
	    	logger.error("Perma Store Config XML Schema Validation Failed for file ",exp);
	    	throw new PermaStoreConfigParserException("Permastore Config XML Schema Validation Failed for file ",exp);
	    } 
	}
	
	public PermaStoreConfigurations marshallConfigXMLtoObject(String configXMLFile) throws PermaStoreConfigParserException{
		//String fileName=configXMLFile.getName();
		try {
					validateXml(configXMLFile);
					JAXBContext jaxbContext = JAXBContext.newInstance(PermaStoreConfigurations.class);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					 InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));

					PermaStoreConfigurations configurations=(PermaStoreConfigurations)jaxbUnmarshaller.unmarshal(inputSourceConfigXml);
					
					return configurations;
					
		  } catch (JAXBException exp) {
			  logger.error("PermaStoreConfig XMLParsing Failed for file ",exp);
		      throw new PermaStoreConfigParserException("PermaStoreConfig XMLParsing Failed for file ",exp);
		  }
	}

	public PermaStoreConfigurations marshallXMLtoObject(String psConfigxml) throws PermaStoreConfigParserException{
		try {
			
			//psConfigxml="<PermaStoreConfigurations>"+psConfigxml+"<PermaStoreConfigurations>";
			logger.debug(".marshallXMLtoObject() xmlString is ["+psConfigxml+"]");
			JAXBContext jaxbContext = JAXBContext.newInstance(com.getusroi.permastore.config.jaxb.ObjectFactory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			//PermaStoreConfiguration configuration=(PermaStoreConfiguration)jaxbUnmarshaller.unmarshal(new StringReader(psConfigxml));
			PermaStoreConfigurations configurations=(PermaStoreConfigurations)jaxbUnmarshaller.unmarshal(new StringReader(psConfigxml));
			
			return configurations;
			
		  } catch (Exception exp) {
			  logger.error("PermaStoreConfig XMLParsing Failed ",exp);
		      throw new PermaStoreConfigParserException("PermaStoreConfig XMLParsing Failed ",exp);
		  }
	}
	
	public String unmarshallObjecttoXML(PermaStoreConfiguration permaStoreConfiguration) throws PermaStoreConfigParserException {
		try {
			PermaStoreConfigurations configs=new PermaStoreConfigurations();
			List<PermaStoreConfiguration> configList=configs.getPermaStoreConfiguration();
			configList.add(permaStoreConfiguration);
					
			JAXBContext jaxbContext = JAXBContext.newInstance(PermaStoreConfigurations.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
	
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(configs,writer);
			//marshaller.marshal(new JAXBElement<PermaStoreConfiguration>(new QName("", "PermaStoreConfiguration"),
			//		PermaStoreConfiguration.class, permaStoreConfiguration), writer);
			String theXML = writer.toString();
			logger.debug("xmlObjecttoXML()=["+theXML+"]");
			return theXML;
		} catch (JAXBException e) {
			logger.error("Failed to convert PermaStoreConfiguration back to XML", e);
			throw new PermaStoreConfigParserException("Failed to convert PermaStoreConfiguration back to XML ",e);
		}
	}
	
	
}
