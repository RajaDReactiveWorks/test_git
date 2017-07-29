package com.getusroi.integrationfwk.config.impl;

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

import com.getusroi.integrationfwk.config.IntegrationPipelineConfigParserException;
import com.getusroi.integrationfwk.config.jaxb.IntegrationPipe;
import com.getusroi.integrationfwk.config.jaxb.IntegrationPipes;

public class IntegrationPipelineConfigXmlParser {

	private final Logger log = LoggerFactory.getLogger(IntegrationPipelineConfigXmlParser.class.getName());
	public static final String INTEGRATION_PIPELINE_CONFIG_SCHEMA = "integration-fwk-pipeline.xsd";

	/**
	 * unmarshallConfigXMLtoObject to convert the xml config file to the
	 * respective Java objects
	 * 
	 * @param configXMLFile
	 * @return List of IntegrationPipe
	 * @throws IntegrationPipelineConfigParserException
	 */
	public IntegrationPipes unmarshallConfigXMLtoObject(String configXMLFile)
			throws IntegrationPipelineConfigParserException {
		try {
			validateXml(configXMLFile);
			JAXBContext jaxbContext = JAXBContext.newInstance(IntegrationPipes.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));
			return (IntegrationPipes) jaxbUnmarshaller.unmarshal(inputSourceConfigXml);

		} catch (JAXBException exp) {
			log.error("IntegrationPipeLineConfig XMLParsing Failed for file ", exp);
			throw new IntegrationPipelineConfigParserException("IntegrationPipeLineConfig XMLParsing Failed for file ",
					exp);
		}
	}// ..end of the method

	/**
	 * marshalling is done in this method in order to convert the JaxBobjects to
	 * Xml String, to store it into the database
	 * 
	 * @param integrationPipe
	 * @return marshalled IntegrationPipe
	 * @throws IntegrationPipelineConfigParserException
	 */
	public String marshallObjectToXml(IntegrationPipe integrationPipe) throws IntegrationPipelineConfigParserException {
		log.debug(".marshallObjectToXml()..");
		IntegrationPipes pipes = new IntegrationPipes();
		StringWriter writer = null;
		List<IntegrationPipe> listOfConfig = pipes.getIntegrationPipe();
		listOfConfig.add(integrationPipe);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(IntegrationPipes.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			writer = new StringWriter();
			marshaller.marshal(pipes, writer);
			String xmlString = writer.toString();
			log.debug("ObjecttoXML()=[" + xmlString + "]");
			return xmlString;
		} catch (JAXBException jaxbException) {
			throw new IntegrationPipelineConfigParserException(
					"Unable to marshall IntegrationPipe object to Xml String.. ", jaxbException);
		}
	}// ..end of the method

	/**
	 * invoked with in the unmarshalling method, in order to validate alongside
	 * the schema, if invalid, it will throw exception
	 * 
	 * @param configXMLFile
	 * @throws IntegrationPipelineConfigParserException
	 */
	private void validateXml(String configXMLFile) throws IntegrationPipelineConfigParserException {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try {
			schema = factory.newSchema(IntegrationPipelineConfigXmlParser.class.getClassLoader()
					.getResource(INTEGRATION_PIPELINE_CONFIG_SCHEMA));
			Validator validator = schema.newValidator();
			StringReader stringReader = new StringReader(configXMLFile);
			validator.validate(new StreamSource(stringReader));
			log.debug("SchemaValidation - Integration Pipeline Successful..");
		} catch (SAXException | IOException e) {
			throw new IntegrationPipelineConfigParserException("Validation of the IntegrationPipeline has been failed",
					e);
		}
	}// ..end of the method

}
