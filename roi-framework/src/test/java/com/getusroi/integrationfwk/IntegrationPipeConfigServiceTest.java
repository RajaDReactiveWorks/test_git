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

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.config.persistence.ConfigNodeData;
import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.getusroi.config.util.GenericTestConstant;
import com.getusroi.integrationfwk.config.IIntegrationPipeLineConfigurationService;
import com.getusroi.integrationfwk.config.IntegrationPipelineConfigException;
import com.getusroi.integrationfwk.config.IntegrationPipelineConfigParserException;
import com.getusroi.integrationfwk.config.IntegrationPipelineConfigUnit;
import com.getusroi.integrationfwk.config.IntegrationPipelineConfigurationConstant;
import com.getusroi.integrationfwk.config.InvalidIntegrationPipelineConfigException;
import com.getusroi.integrationfwk.config.impl.IntegrationPipelineConfigXmlParser;
import com.getusroi.integrationfwk.config.impl.IntegrationPipelineConfigurationService;
import com.getusroi.integrationfwk.config.jaxb.IntegrationPipe;
import com.getusroi.integrationfwk.config.jaxb.IntegrationPipes;
import com.getusroi.integrationfwk.config.jaxb.PipeActivity;

public class IntegrationPipeConfigServiceTest {

	private String integrationPipelineConfigXmlFile;
	private final Logger logger = LoggerFactory
			.getLogger(IntegrationPipeConfigServiceTest.class.getName());
	private final String CONFIG_FILE_TO_PARSE = "integration_pipelinev3.xml";
	private static final String TEST_INTEGRATION_CONFIG_NAME = "SAC-SR";
	private static final String TEST_INTEGRATION_CONFIG_NAME_UPDATE = "SAC-SR1";
	private static final String TEST_IMAP_KEY = "861-IPC";
	private static final String TEST_INTEGRATION_CONFIG_ATTRIBUTE_XSLT_NAME = "testxsltTransform1";
	IntegrationPipelineConfigXmlParser integrationPipelineConfigXmlParser;
	IntegrationPipelineConfigurationService pipelineConfigurationService;
	ConfigurationContext configurationContext;
	RequestContext requestContext;
	IntegrationPipe integrationPipe;
	IConfigPersistenceService pesrsistence1;

	@Before
	public void loadConfiguration() throws ConfigPersistenceException,
			IntegrationPipelineConfigParserException {
		IntegrationPipes integrationPipes = getIntegrationPileFileObject();
		pipelineConfigurationService = new IntegrationPipelineConfigurationService();
		IConfigPersistenceService pesrsistence1 = new ConfigPersistenceServiceMySqlImpl();
		pesrsistence1
				.deleteConfigNodeDataByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
		integrationPipe = integrationPipes.getIntegrationPipe().get(0);
		logger.debug("Loading pipeConfig befor testStarts: " + integrationPipe);
	}

	// @Test
	public void testAddIntegrationConfigurationNotnull()
			throws IntegrationPipelineConfigParserException,
			IntegrationPipelineConfigException, ConfigPersistenceException {
		pesrsistence1 = new ConfigPersistenceServiceMySqlImpl();
		configurationContext = new ConfigurationContext(
				GenericTestConstant.TEST_TENANTID,
				GenericTestConstant.TEST_SITEID,
				GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL_NAME,
				GenericTestConstant.TEST_VENDOR,
				GenericTestConstant.TEST_VERSION);
		pipelineConfigurationService = new IntegrationPipelineConfigurationService();
		pipelineConfigurationService.addIntegrationPipelineConfiguration(
				configurationContext, integrationPipe);
		Assert.assertNotNull("ConfigNodeId returned shouldn't be null..",
				integrationPipe);
		logger.debug("Test configNodeId returned..", integrationPipe);
	}//..end  of the method

	// @Test
	public void testDeleteIntegrationConfiguration()
			throws IntegrationPipelineConfigException,
			InvalidIntegrationPipelineConfigException {
		logger.debug("inside the testDeleteIntegrationConfiguration()");
		pesrsistence1 = new ConfigPersistenceServiceMySqlImpl();
		configurationContext = new ConfigurationContext(
				GenericTestConstant.TEST_TENANTID,
				GenericTestConstant.TEST_SITEID,
				GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL_NAME,
				GenericTestConstant.TEST_VENDOR,
				GenericTestConstant.TEST_VERSION);
		pipelineConfigurationService = new IntegrationPipelineConfigurationService();
		pipelineConfigurationService.addIntegrationPipelineConfiguration(
				configurationContext, integrationPipe);
		pipelineConfigurationService.deleteIntegrationPipelineConfiguration(
				configurationContext, TEST_INTEGRATION_CONFIG_NAME);
		logger.debug("CONFIGNAME:" + TEST_INTEGRATION_CONFIG_NAME);
		Assert.assertNotNull("configName returnd should not be null:",
				TEST_INTEGRATION_CONFIG_NAME);
		logger.debug("Test ConfigName returned", TEST_INTEGRATION_CONFIG_NAME);
	}//..end  of the method

	@Test
	public void testUpdateIntegrationConfiguration()
			throws ConfigPersistenceException,
			IntegrationPipelineConfigException,
			IntegrationPipelineConfigParserException,
			IntegrationPipelineConfigException, InvalidIntegrationPipelineConfigException {
		logger.debug(".testUpdateIntegrationConfiguration");
		pesrsistence1 = new ConfigPersistenceServiceMySqlImpl();
		configurationContext = new ConfigurationContext(
				GenericTestConstant.TEST_TENANTID,
				GenericTestConstant.TEST_SITEID,
				GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL_NAME,
				GenericTestConstant.TEST_VENDOR,
				GenericTestConstant.TEST_VERSION);
		IIntegrationPipeLineConfigurationService integrationPipelineConfigService = new IntegrationPipelineConfigurationService();
		integrationPipelineConfigService.addIntegrationPipelineConfiguration(
				configurationContext, integrationPipe);
		ConfigNodeData configNodeData = pesrsistence1
				.getConfigNodeDatabyNameAndNodeId(
						GenericTestConstant.getVendorConfigNodeId(),
						TEST_INTEGRATION_CONFIG_NAME,
						IntegrationPipelineConfigurationConstant.INTEGRATION_PIPELINE_CONFIG_TYPE);
		logger.debug("ConfigNodeData:" + configNodeData);
		ConfigurationContext configurationContextUpdate = new ConfigurationContext(
				GenericTestConstant.TEST_TENANTID,
				GenericTestConstant.TEST_SITEID,
				GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL_NAME,
				GenericTestConstant.TEST_VENDOR,
				GenericTestConstant.TEST_VERSION);

		if (configNodeData != null) {
			logger.debug("integrationPipeName:" + integrationPipe.getName());
			List<PipeActivity> pipeActivity = integrationPipe.getPipeActivity();
			logger.debug("pipeActivity:" + pipeActivity);
			logger.debug("XSLTEnricherActivity:"
					+ pipeActivity.get(0).getXSLTEnricherActivity().getName());
			integrationPipe.setName(TEST_INTEGRATION_CONFIG_NAME_UPDATE);
			String integrationPipeActivitiyName = integrationPipe.getName();
			logger.debug("integrationPipeActivitiyName"
					+ integrationPipeActivitiyName);
			integrationPipe.setIsEnabled(true);
			integrationPipelineConfigService
					.updateIntegrationPipelineConfiguration(
							configurationContextUpdate, integrationPipe,
							configNodeData.getNodeDataId());
			Assert.assertNotNull("Object should be Updated ",
					TEST_INTEGRATION_CONFIG_NAME_UPDATE);
			logger.debug("Test ConfigName returned",
					TEST_INTEGRATION_CONFIG_NAME_UPDATE);
			ConfigNodeData configData = checkDBForConfig(
					configNodeData.getConfigName(),
					configNodeData.getParentConfigNodeId());
			logger.debug("configData:" + configData);

		}
	}//..end  of the method

	// @Test
	public void testChangeStatusOfIntegrationPipelineConfigDisable()
			throws IntegrationPipelineConfigException,
			IntegrationPipelineConfigException,
			InvalidIntegrationPipelineConfigException {
		logger.debug(".testChangeStatusOfIntegrationPipelineConfigDisable");
		pesrsistence1 = new ConfigPersistenceServiceMySqlImpl();
		configurationContext = new ConfigurationContext(
				GenericTestConstant.TEST_TENANTID,
				GenericTestConstant.TEST_SITEID,
				GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL_NAME,
				GenericTestConstant.TEST_VENDOR,
				GenericTestConstant.TEST_VERSION);
		pipelineConfigurationService = new IntegrationPipelineConfigurationService();
		pipelineConfigurationService.changeStatusOfIntegrationPipelineConfig(
				configurationContext, TEST_INTEGRATION_CONFIG_NAME_UPDATE, false);
		Assert.assertNotNull(
				"Object should be reloaded in DataGrid if Config is Disabled:",
				TEST_INTEGRATION_CONFIG_NAME);
		logger.debug("Test ConfigName returned", TEST_INTEGRATION_CONFIG_NAME);
	}//..end  of the method

	//@Test
	public void testChangeStatusOfIntegrationPipelineConfigEnable()
			throws IntegrationPipelineConfigException,
			IntegrationPipelineConfigException,
			InvalidIntegrationPipelineConfigException {
		logger.debug(".testChangeStatusOfIntegrationPipelineConfigEnsable");
		pesrsistence1 = new ConfigPersistenceServiceMySqlImpl();
		configurationContext = new ConfigurationContext(
				GenericTestConstant.TEST_TENANTID,
				GenericTestConstant.TEST_SITEID,
				GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL_NAME,
				GenericTestConstant.TEST_VENDOR,
				GenericTestConstant.TEST_VERSION);
		pipelineConfigurationService = new IntegrationPipelineConfigurationService();
		pipelineConfigurationService.changeStatusOfIntegrationPipelineConfig(
				configurationContext, TEST_INTEGRATION_CONFIG_NAME_UPDATE, true);
		Assert.assertNotNull(
				"Object should be reloaded in DataGrid if Config is Enabled:",
				TEST_INTEGRATION_CONFIG_NAME);
		logger.debug("Test ConfigName returned", TEST_INTEGRATION_CONFIG_NAME);
	}//..end  of the method

	// @Test
	public void getIntegrationConfigDatagrid()
			throws IntegrationPipelineConfigParserException,
			IntegrationPipelineConfigException, ConfigPersistenceException {

		pesrsistence1 = new ConfigPersistenceServiceMySqlImpl();
		configurationContext = new ConfigurationContext(
				GenericTestConstant.TEST_TENANTID,
				GenericTestConstant.TEST_SITEID,
				GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL_NAME,
				GenericTestConstant.TEST_VENDOR,
				GenericTestConstant.TEST_VERSION);
		requestContext = new RequestContext(GenericTestConstant.TEST_TENANTID,
				GenericTestConstant.TEST_SITEID,
				GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL_NAME,
				GenericTestConstant.TEST_VENDOR,
				GenericTestConstant.TEST_VERSION);
		pipelineConfigurationService = new IntegrationPipelineConfigurationService();
		pipelineConfigurationService.addIntegrationPipelineConfiguration(
				configurationContext, integrationPipe);
		IntegrationPipelineConfigUnit pipelineConfigUnit = pipelineConfigurationService
				.getIntegrationPipeConfiguration(requestContext,
						integrationPipe.getName());
		logger.debug("The pipeline configUnit in test.." + pipelineConfigUnit);
		Assert.assertNotNull(pipelineConfigUnit);
		Assert.assertEquals(true, pipelineConfigUnit.getIsEnabled());
		Assert.assertEquals(TEST_IMAP_KEY, pipelineConfigUnit
				.getConfigGroupKey(GenericTestConstant.TEST_VENDOR_NODEID));
		Assert.assertEquals(TEST_INTEGRATION_CONFIG_NAME,
				pipelineConfigUnit.getKey());
		IntegrationPipe pipe = (IntegrationPipe) pipelineConfigUnit
				.getConfigData();
		Assert.assertEquals(TEST_INTEGRATION_CONFIG_NAME, pipe.getName());
		Assert.assertEquals(TEST_INTEGRATION_CONFIG_ATTRIBUTE_XSLT_NAME, pipe
				.getPipeActivity().get(0).getXSLTEnricherActivity().getName());
	}//..end  of the method

	/**
	 * locally accessed to get the JaxB Object .getIntegrationPileFileObject
	 * 
	 * @return list of IntegrationPipe objects
	 * @throws IntegrationPipelineConfigParserException
	 */
	private IntegrationPipes getIntegrationPileFileObject()
			throws IntegrationPipelineConfigParserException {
		integrationPipelineConfigXmlParser = new IntegrationPipelineConfigXmlParser();
		InputStream inputstream = IntegrationPipelineConfigXmlParser.class
				.getClassLoader().getResourceAsStream(CONFIG_FILE_TO_PARSE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);

			}
			reader.close();
		} catch (IOException e) {
			throw new IntegrationPipelineConfigParserException(
					"feature MetaInfo file doesnot exist in classpath", e);
		}
		integrationPipelineConfigXmlFile = out1.toString();
		logger.debug("integrationPipelineConfigXml : "
				+ integrationPipelineConfigXmlFile);
		IntegrationPipes integrationPipes = integrationPipelineConfigXmlParser
				.unmarshallConfigXMLtoObject(integrationPipelineConfigXmlFile);
		return integrationPipes;
	}// ..end of the method

	private ConfigNodeData checkDBForConfig(String configName, int nodeId)
			throws ConfigPersistenceException {
		logger.debug(".ConfigNodeData checkDBForConfig(" + configName + ")");
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData configData = pesrsistence
				.getConfigNodeDatabyNameAndNodeId(
						nodeId,
						configName,
						IntegrationPipelineConfigurationConstant.INTEGRATION_PIPELINE_CONFIG_TYPE);
		return configData;
	}//..end  of the method
}
