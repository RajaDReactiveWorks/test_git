package com.getusroi.integrationfwk;

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

import com.getusroi.integrationfwk.config.IntegrationPipelineConfigParserException;
import com.getusroi.integrationfwk.config.impl.IntegrationPipelineConfigXmlParser;
import com.getusroi.integrationfwk.config.jaxb.IntegrationPipe;
import com.getusroi.integrationfwk.config.jaxb.IntegrationPipes;

public class IntegrationPipeXmlParserTest {

	private String integrationPipelineConfigXmlFile;
	private final Logger logger = LoggerFactory.getLogger(IntegrationPipeXmlParserTest.class.getName());
	private final String CONFIG_FILE_TO_PARSE = "integration_pipelinev3.xml";
	IntegrationPipelineConfigXmlParser integrationPipelineConfigXmlParser;

	@Before
	public void getIntegrationPileFileObject() throws IntegrationPipelineConfigParserException {
		integrationPipelineConfigXmlParser = new IntegrationPipelineConfigXmlParser();
		InputStream inputstream = IntegrationPipelineConfigXmlParser.class.getClassLoader()
				.getResourceAsStream(CONFIG_FILE_TO_PARSE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);

			}
			reader.close();
		} catch (IOException e) {
			throw new IntegrationPipelineConfigParserException("feature MetaInfo file doesnot exist in classpath", e);
		}

		integrationPipelineConfigXmlFile = out1.toString();
		logger.debug("integrationPipelineConfigXml : " + integrationPipelineConfigXmlFile);
	}

	@Test
	public void integrationConfigExistance() throws IntegrationPipelineConfigParserException {
		IntegrationPipes pipes = integrationPipelineConfigXmlParser
				.unmarshallConfigXMLtoObject(integrationPipelineConfigXmlFile);
		Assert.assertNotNull("IntegrationPipes must not be Null", pipes);
	}

	@Test
	public void testMarshall() throws IntegrationPipelineConfigParserException {
		IntegrationPipes pipes = integrationPipelineConfigXmlParser
				.unmarshallConfigXMLtoObject(integrationPipelineConfigXmlFile);
		Assert.assertNotNull("IntegrationPipes must not be Null", pipes);
		List<IntegrationPipe> integrationPipes = pipes.getIntegrationPipe();
		String namePipeLine = integrationPipes.get(0).getName();
		Assert.assertEquals("IntegrationPipeLine name at first index is {SAC-SR} ", namePipeLine, "SAC-SR");
	}

	@Test
	public void testUnmarshall() throws IntegrationPipelineConfigParserException {
		IntegrationPipes pipes = integrationPipelineConfigXmlParser
				.unmarshallConfigXMLtoObject(integrationPipelineConfigXmlFile);
		Assert.assertNotNull("IntegrationPipes must not be Null", pipes);
		List<IntegrationPipe> integrationPipes = pipes.getIntegrationPipe();
		IntegrationPipe pipe = integrationPipes.get(0);
		String xmlString = integrationPipelineConfigXmlParser.marshallObjectToXml(pipe);
		Assert.assertNotNull("Configuration xml shouldn't be null..", xmlString);
		logger.debug("The xml converted: " + xmlString);
	}

}
