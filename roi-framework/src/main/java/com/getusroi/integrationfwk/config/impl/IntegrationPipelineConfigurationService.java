package com.getusroi.integrationfwk.config.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.GenericApplicableNode;
import com.getusroi.config.RequestContext;
import com.getusroi.config.persistence.ConfigNodeData;
import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.InvalidNodeTreeException;
import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.getusroi.config.server.ConfigServerInitializationException;
import com.getusroi.config.server.ROIConfigurationServer;
import com.getusroi.feature.config.FeatureConfigParserException;
import com.getusroi.feature.config.FeatureConfigRequestException;
import com.getusroi.feature.config.FeatureConfigurationConstant;
import com.getusroi.feature.config.FeatureConfigurationException;
import com.getusroi.integrationfwk.config.IIntegrationPipeLineConfigurationService;
import com.getusroi.integrationfwk.config.IntegrationPipelineConfigException;
import com.getusroi.integrationfwk.config.IntegrationPipelineConfigParserException;
import com.getusroi.integrationfwk.config.IntegrationPipelineConfigUnit;
import com.getusroi.integrationfwk.config.IntegrationPipelineConfigurationConstant;
import com.getusroi.integrationfwk.config.InvalidIntegrationPipelineConfigException;
import com.getusroi.integrationfwk.config.jaxb.IntegrationPipe;
import com.getusroi.integrationfwk.config.jaxb.IntegrationPipes;
import com.getusroi.permastore.config.PermaStoreConfigParserException;
import com.getusroi.permastore.config.PermaStoreConfigRequestException;
import com.getusroi.permastore.config.PermaStoreConfigurationConstant;
import com.getusroi.permastore.config.PermaStoreConfigurationException;

public class IntegrationPipelineConfigurationService extends GenericApplicableNode
		implements IIntegrationPipeLineConfigurationService {

	private final Logger logger = LoggerFactory.getLogger(IntegrationPipelineConfigurationService.class.getName());

	@Override
	public void addIntegrationPipelineConfiguration(ConfigurationContext configurationContext,
			IntegrationPipe integrationPipe) throws IntegrationPipelineConfigException {
		logger.debug(".addIntegrationPipelineConfiguration().." + integrationPipe);
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		IntegrationPipelineConfigXmlParser configXmlParser = new IntegrationPipelineConfigXmlParser();
		ConfigNodeData configNodeData = new ConfigNodeData();
		String tenantId = configurationContext.getTenantId();
		String siteId = configurationContext.getSiteId();
		String vendorName = configurationContext.getVendorName();
		String version = configurationContext.getVersion();
		String featureGroup = configurationContext.getFeatureGroup();
		String featureName = configurationContext.getFeatureName();
		String implementation = configurationContext.getImplementationName();
		int configDataId = 0;
		logger.debug("ConfigurationContext-Object: tenantId-" + tenantId + ", siteId-" + siteId + ", vendorName-"
				+ vendorName + ", version-" + version + ", featureGroup-" + featureGroup + ", featureName-"
				+ featureName + ", impl name : " + implementation);
		int configNodeId = getConfigNodeId(tenantId, siteId, vendorName, implementation, version, featureGroup,
				featureName);
		String xmlString;
		try {
			xmlString = configXmlParser.marshallObjectToXml(integrationPipe);

			configNodeData.setConfigName(integrationPipe.getName());
			configNodeData.setEnabled(integrationPipe.isIsEnabled());
			configNodeData.setConfigLoadStatus(IntegrationPipelineConfigurationConstant.LOAD_STATUS);
			configNodeData.setConfigType(IntegrationPipelineConfigurationConstant.INTEGRATION_PIPELINE_CONFIG_TYPE);
			configNodeData.setParentConfigNodeId(configNodeId);
			configNodeData.setConfigData(xmlString);

			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					configNodeId, integrationPipe.getName(),
					IntegrationPipelineConfigurationConstant.INTEGRATION_PIPELINE_CONFIG_TYPE);
			if (loadedConfigNodeData == null) {
				configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
			} // ..end of if, condition check if the configuration exists or not
			else {
				throw new IntegrationPipelineConfigException(
						"IntegrationPipeline Configuration already exist..in the index: " + configNodeId);
			}
			if (!integrationPipe.isIsEnabled()) {
				return;
			}
			IntegrationPipelineConfigUnit configUnit = new IntegrationPipelineConfigUnit(tenantId, siteId, configNodeId,
					integrationPipe.isIsEnabled(), integrationPipe);
			logger.debug("configUnit whenAdding.." + configUnit);
			loadConfigurationInDataGrid(configUnit);
		} catch (IntegrationPipelineConfigParserException | ConfigPersistenceException parsePersistanceException) {
			throw new IntegrationPipelineConfigException(
					"Unable to insert IntegrationPipeline Congig into the data table..for tenantId-" + tenantId
							+ ", siteId-" + siteId + ", vendorName-" + vendorName + ", version-" + version
							+ ", featureGroup-" + featureGroup + ", featureName-" + featureName + ", impl name-"
							+ implementation,
					parsePersistanceException);
		}

	}// ..end of the method

	@Override
	public boolean deleteIntegrationPipelineConfiguration(ConfigurationContext configurationContext, String configName)
			throws IntegrationPipelineConfigException, InvalidIntegrationPipelineConfigException {
		logger.debug(".deleteIntegrationPipelineConfiguration() " + configName);
		if (configName == null) {
			throw new InvalidIntegrationPipelineConfigException(
					"configuartion with empty name cant be deleted: " + configName);
		} else {
			String tenantID = configurationContext.getTenantId();
			String siteId = configurationContext.getSiteId();
			String vendorName = configurationContext.getVendorName();
			String version = configurationContext.getVersion();
			String featureGroup = configurationContext.getFeatureGroup();
			String featureName = configurationContext.getFeatureName();
			logger.debug("tenantId:" + tenantID + "::" + "siteId:" + siteId + "::" + "vendorName:" + vendorName + "::"
					+ "version:" + version + "::" + "featureGroup:" + featureGroup + "::" + "featureName:"
					+ featureName);
			RequestContext reqContext = new RequestContext(tenantID, siteId, featureGroup, featureName,
					configurationContext.getImplementationName(), vendorName, version);
			try {
				IntegrationPipelineConfigUnit integrationConfigUnit = getIntegrationPipeConfiguration(reqContext,
						configName);
				if (integrationConfigUnit != null) {
					logger.warn("Delete request for Non Cache IntegarionPiplelineConfiguration=" + configName);

					Integer configNodeId = getApplicableNodeId(reqContext);
					return deleteIntegrationPipelineConfigurationFromDb(configName, configNodeId);
				} // ..end of if ,condition check if integrationConfigUnit
					// exists
					// delete from the database
				IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
				boolean isDeleted = configPersistenceService
						.deleteConfigNodeData(integrationConfigUnit.getDbconfigId());
				logger.debug("isDeleted:" + isDeleted);
				String integrationPiplelineGroupKey = IntegrationPipelineConfigUnit
						.getConfigGroupKey(integrationConfigUnit.getAttachedNodeId());
				ROIConfigurationServer configServer = ROIConfigurationServer.getConfigurationService();
				configServer.deleteConfiguration(tenantID, integrationPiplelineGroupKey, configName);
				logger.debug(
						".deleteIntegrationPipelineConfiguration() deleted from DataGrid integartionPiplelineGroupKey="
								+ integrationPiplelineGroupKey + " configName=" + configName);

			} catch (IntegrationPipelineConfigException | InvalidNodeTreeException | ConfigPersistenceException
					| ConfigServerInitializationException | NullPointerException e) {
				throw new IntegrationPipelineConfigException(
						"Failed to Delete IntegartionPipelineConfig with name" + configName, e);
			}
		}
		return true;
	}// .. end of method

	@Override
	public int updateIntegrationPipelineConfiguration(ConfigurationContext configContext,
			IntegrationPipe integrationPipe, int configNodeDataId)
			throws IntegrationPipelineConfigException, InvalidIntegrationPipelineConfigException {
		int sucess = 0;
		logger.debug(".updateIntegrationPipelineConfiguration (" + configNodeDataId + ")");
		if (configNodeDataId == 0) {
			throw new InvalidIntegrationPipelineConfigException(
					"invalid configNodeDataId does not exists in db" + configNodeDataId);
		} else {
			String tenantID = configContext.getTenantId();
			String siteId = configContext.getSiteId();
			String vendorName = configContext.getVendorName();
			String version = configContext.getVersion();
			String featureGroup = configContext.getFeatureGroup();
			String featureName = configContext.getFeatureName();
			logger.debug("tenantId:" + tenantID + "::" + "siteId:" + siteId + "::" + "vendorName:" + vendorName + "::"
					+ "version:" + version + "::" + "featureGroup:" + featureGroup + "::" + "featureName:"
					+ featureName);
			try {
				Integer configNodeId = 0;
				// checking and getting the configNodeId
				if ((vendorName != null && !(vendorName.isEmpty()) && !(vendorName.equalsIgnoreCase("")))
						&& (version != null && !(version.isEmpty()) && !(version.equalsIgnoreCase("")))) {
					configNodeId = getApplicableNodeIdVendorName(tenantID, siteId, configContext.getFeatureGroup(),
							configContext.getFeatureName(), configContext.getImplementationName(), vendorName, version);
				} else {
					configNodeId = getApplicableNodeIdFeatureName(tenantID, siteId, configContext.getFeatureGroup(),
							configContext.getFeatureName(), configContext.getImplementationName());
				}
				logger.debug("Applicable Config Node Id is =" + configNodeId);
				// Convert configTo Valid XML to store independent inDataBase
				IntegrationPipelineConfigXmlParser builder = new IntegrationPipelineConfigXmlParser();
				String xmlString = builder.marshallObjectToXml(integrationPipe);
				// update DB for these configuration
				ConfigNodeData configNodeData = new ConfigNodeData();
				configNodeData.setConfigName(integrationPipe.getName());
				// #TODO Enable or Disable should come from config need to add
				// it in
				// XMLSchema
				configNodeData.setEnabled(integrationPipe.isIsEnabled());
				boolean isConfigEnabled = integrationPipe.isIsEnabled();
				configNodeData.setConfigLoadStatus("Sucess");
				configNodeData.setConfigType(IntegrationPipelineConfigurationConstant.INTEGRATION_PIPELINE_CONFIG_TYPE);
				configNodeData.setParentConfigNodeId(configNodeId);
				configNodeData.setConfigData(xmlString);
				configNodeData.setNodeDataId(configNodeDataId);
				RequestContext requestContext = null;
				IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
				logger.debug("before updating the permastore configId " + configNodeDataId);
				sucess = configPersistenceService.updateConfigNodeData(configNodeData);
				requestContext = new RequestContext(tenantID, siteId, featureGroup, featureName,
						configContext.getImplementationName(), vendorName, version);
				changeStatusOfIntegrationPipelineConfig(configContext, integrationPipe.getName(), isConfigEnabled);
			} catch (InvalidNodeTreeException | ConfigPersistenceException
					| IntegrationPipelineConfigParserException e) {
				throw new IntegrationPipelineConfigException(
						"Failed to insert ConfigData in DB for configName=" + integrationPipe.getName(), e);
			}
		}
		return sucess;
	}// ..end of method

	@Override
	public void changeStatusOfIntegrationPipelineConfig(ConfigurationContext configContext, String configName,
			boolean isEnabled) throws IntegrationPipelineConfigException, IntegrationPipelineConfigException,
			InvalidIntegrationPipelineConfigException {
		logger.debug(".changeStatusOfIntegrationPipelineConfig configName:" + configName);
		if (configName == null) {
			throw new InvalidIntegrationPipelineConfigException(
					"configuration name does not exists in db:" + configName);
		} else {
			String tenantID = configContext.getTenantId();
			String siteId = configContext.getSiteId();
			String vendorName = configContext.getVendorName();
			String version = configContext.getVersion();
			String featureGroup = configContext.getFeatureGroup();
			String featureName = configContext.getFeatureName();
			String implName = configContext.getImplementationName();
			logger.debug("tenantId:" + tenantID + "::" + "siteId:" + siteId + "::" + "vendorName:" + vendorName + "::"
					+ "version:" + version + "::" + "featureGroup:" + featureGroup + "::" + "featureName:"
					+ featureName);

			RequestContext reqContext = new RequestContext(tenantID, siteId, featureGroup, featureName, implName,
					vendorName, version);
			try {
				Integer applicableNodeId = getApplicableNodeId(configContext);
				logger.debug("applicableNodeId:" + applicableNodeId);
				IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
				ConfigNodeData configNodedata = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
						applicableNodeId, configName,
						IntegrationPipelineConfigurationConstant.INTEGRATION_PIPELINE_CONFIG_TYPE);
				if (configNodedata == null) {
					throw new IntegrationPipelineConfigException(
							"IntegrationPipelineConfig with Name( " + configName + ") does not exist in DB");
				} // .. end of if,the condition checks if configNodeData is null
					// then the pipeline config does not exists in db.

				if (!isEnabled) {
					logger.debug("integartion pipeline status is enabled , disabling it again");
					configPersistenceService.enableConfigNodeData(false, configNodedata.getNodeDataId());

					String integartionPipelineGroupkey = IntegrationPipelineConfigUnit
							.getConfigGroupKey(configNodedata.getNodeDataId());
					ROIConfigurationServer configServer = ROIConfigurationServer.getConfigurationService();
					configServer.deleteConfiguration(reqContext.getTenantId(), integartionPipelineGroupkey, configName);
				} // ..end of if
				else {
					logger.debug("integration pipeline status is disabled, enabling it again");
					enableAndLoadIntegrationPipelineConfig(reqContext, configNodedata);
				}
			} catch (InvalidNodeTreeException | ConfigPersistenceException | ConfigServerInitializationException e) {
				throw new IntegrationPipelineConfigException(
						"Failed to Enable IntegrationPiplelineConfig with name " + configName, e);
			}
		}
	}// .. end of method

	@Override
	public IntegrationPipelineConfigUnit getIntegrationPipeConfiguration(RequestContext requestContext,
			String configName) throws IntegrationPipelineConfigException {
		int nodeId = 0;
		try {
			if (!requestContext.getVendor().isEmpty()) {

				nodeId = getApplicableNodeIdVendorName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName(), requestContext.getVendor(),
						requestContext.getVersion());
			} else {
				nodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName());
			}
			String ipcgroupKey = IntegrationPipelineConfigUnit.getConfigGroupKey(nodeId);
			logger.debug("key to search in map: " + ipcgroupKey);
			ROIConfigurationServer configServer = ROIConfigurationServer.getConfigurationService();
			return (IntegrationPipelineConfigUnit) configServer.getConfiguration(ipcgroupKey, configName);
		} catch (InvalidNodeTreeException | ConfigPersistenceException
				| ConfigServerInitializationException getObjectException) {
			throw new IntegrationPipelineConfigException(
					"Unable to parse the tree in DataGrid to get the configurationUnit..", getObjectException);

		}
	}// ..end of the method

	/**
	 * locally invoked to get the configurationNodeId , once insertion is
	 * success full, checks for the version availability and when not available
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param vendorName
	 * @param version
	 * @param featureGroup
	 * @param featureName
	 * @return ConfigurationNodeId, after inserting the data
	 * @throws IntegrationPipelineConfigException
	 */
	private int getConfigNodeId(String tenantId, String siteId, String vendorName, String implName, String version,
			String featureGroup, String featureName) throws IntegrationPipelineConfigException {
		int configNodeId = 0;
		try {
			if (!vendorName.isEmpty() && !version.isEmpty()) {
				configNodeId = getApplicableNodeIdVendorName(tenantId, siteId, featureGroup, featureName, implName,
						vendorName, version);
				logger.debug("Applicable nodeId is.." + configNodeId);
			} else if (vendorName.isEmpty() && version.isEmpty()) {
				configNodeId = getApplicableNodeIdFeatureName(tenantId, siteId, featureGroup, featureName, implName);
				logger.debug("Applicable nodeId is.." + configNodeId);
			} // ..end of if-else, conditional check with vendor-version support
		} catch (InvalidNodeTreeException | ConfigPersistenceException persistanceException) {
			throw new IntegrationPipelineConfigException(
					"Failed loading nodeId, when version and vendor is empty for tenantId-" + tenantId + ", siteId-"
							+ siteId + ", vendorName-" + vendorName + ", version-" + version + ", featureGroup-"
							+ featureGroup + ", featureName-" + featureName + ", impl name : " + implName,
					persistanceException);
		}
		return configNodeId;
	}// ..end of the method

	/**
	 * locally called to set the mapKey, into DataGrid
	 * 
	 * @param configUnit
	 * @throws IntegrationPipelineConfigException
	 */
	private void loadConfigurationInDataGrid(IntegrationPipelineConfigUnit configUnit)
			throws IntegrationPipelineConfigException {
		logger.debug(".loadConfigurationInDataGrid() IntegrationPipelineConfigUnit=" + configUnit);
		try {
			ROIConfigurationServer configServer = ROIConfigurationServer.getConfigurationService();
			configServer.addConfigurationWithoutTenant(configUnit);

		} catch (ConfigServerInitializationException e) {
			throw new IntegrationPipelineConfigException(
					"Failed to Upload in DataGrid configName=" + configUnit.getKey(), e);
		}
	}// ..end of the method

	/**
	 * delete the integration pipeline from db by using configName and configId
	 * 
	 * @param configName
	 * @param configNodeId
	 * @return
	 * @throws IntegrationPipelineConfigException
	 */
	private boolean deleteIntegrationPipelineConfigurationFromDb(String configName, int configNodeId)
			throws IntegrationPipelineConfigException {
		logger.debug(".deleteIntegrationPipelineConfigurationFromDb() deleted from db configName:" + configName);
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();

		try {
			configPersistenceService.deleteConfigNodeDataByNodeIdAndConfigName(configName, configNodeId);

		} catch (ConfigPersistenceException e) {
			logger.error("Persistance exception deleting the node cause: " + e);
			throw new IntegrationPipelineConfigException("Persistance exception deleting the node cause: " + e);
		}
		return true;
	}// .. end of the method

	/**
	 * locally invoked when pipeline is disable and to enable the pipeline into
	 * db
	 * 
	 * @param reqContext
	 * @param configNodedata
	 * @throws IntegrationPipelineConfigException
	 */
	private void enableAndLoadIntegrationPipelineConfig(RequestContext reqContext, ConfigNodeData configNodedata)
			throws IntegrationPipelineConfigException {
		logger.debug("enableAndLoadIntegartionPipelineConfig()");
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		try {
			configPersistenceService.enableConfigNodeData(true, configNodedata.getNodeDataId());

			String integrationPipelineConfigStr = configNodedata.getConfigData();
			logger.debug("IntegrationPipelineConfigStr:" + integrationPipelineConfigStr);
			IntegrationPipelineConfigXmlParser builder = new IntegrationPipelineConfigXmlParser();
			IntegrationPipes integrationPipes = builder.unmarshallConfigXMLtoObject(integrationPipelineConfigStr);
			// #TODO it is loaded from db where it will have only one
			// integration pipeline
			IntegrationPipe integrationPipe = integrationPipes.getIntegrationPipe().get(0);
			IntegrationPipelineConfigUnit integartionPipelineConfigUnit = new IntegrationPipelineConfigUnit(
					reqContext.getTenantId(), reqContext.getSiteId(), configNodedata.getParentConfigNodeId(), true,
					integrationPipe);
			integartionPipelineConfigUnit.setDbconfigId(configNodedata.getNodeDataId());
			loadConfigurationInDataGrid(integartionPipelineConfigUnit);
		} catch (ConfigPersistenceException | IntegrationPipelineConfigParserException
				| IntegrationPipelineConfigException e) {
			throw new IntegrationPipelineConfigException(
					"Failed To enableAndLoad the data using confignodedata and requestcontext");
		}
	} // .. end of the method

	@Override
	public boolean checkIntegrationPipelineConfigExistOrNot(ConfigurationContext configurationContext,
			String configName) throws IntegrationPipelineConfigException, IntegrationPipelineConfigParserException {
		boolean isEnabled = false;
		logger.debug("Inside checkIntegrationPipelineConfigExistOrNot method with configurationContext = "
				+ configurationContext + " configName = " + configName);
		RequestContext requestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(),
				configurationContext.getFeatureName(), configurationContext.getImplementationName(),
				configurationContext.getVendorName(), configurationContext.getVersion());
		String vendorName = requestContext.getVendor();
		String version = requestContext.getVersion();
		IntegrationPipelineConfigUnit integrationPipelineConfigUnit = null;
		try {
			int featureNodeId = 0;
			if (vendorName != null && !(vendorName.isEmpty())) {
				logger.debug("getting the node id till vendor level");
				featureNodeId = getApplicableNodeIdVendorName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName(), requestContext.getVendor(),
						requestContext.getVersion());

			} else {
				logger.debug("getting the node id till feature level");
				featureNodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName());
			}
			logger.debug("featureNodeId : " + featureNodeId);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(featureNodeId,
					configName, IntegrationPipelineConfigurationConstant.INTEGRATION_PIPELINE_CONFIG_TYPE);

			// if confignodedata not Exist
			if (configNodeData == null)
				return false;

			isEnabled = configNodeData.isEnabled();
			if (isEnabled) {
				integrationPipelineConfigUnit = getIntegrationPipeConfiguration(requestContext, configName);
				if (integrationPipelineConfigUnit == null) {
					enableAndLoadIntegrationPipelineConfig(requestContext, configNodeData);
				}

			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new IntegrationPipelineConfigParserException("Error in Searching the Feature with FeatureName = "
					+ configName + " and with requestContext = " + requestContext);
		}
		return true;
	}

	/**
	 * this method is used to reload integration pipeline into cache.
	 * 
	 * @param requestContext
	 * @param configName
	 * @throws IntegrationPipelineConfigException
	 * @throws ConfigPersistenceException 
	 * @throws InvalidNodeTreeException 
	 * @throws IntegrationPipelineConfigParserException 
	 */
	@Override
	public boolean reloadIntegrationPipelineCacheObject(RequestContext requestContext, String configName)
			throws IntegrationPipelineConfigException  {
		logger.debug("inside reloadIntegrationPipelineCacheObject()");
		if(requestContext==null && configName==null)
			throw new IntegrationPipelineConfigException("requestContext and configName both should not be null");
		try{
		IntegrationPipelineConfigUnit integrationPipeline = getIntegrationPipeConfiguration(requestContext, configName);
		if (integrationPipeline == null) {
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			Integer applicableNodeId = getApplicableNodeId(requestContext);
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableNodeId,
					configName, IntegrationPipelineConfigurationConstant.INTEGRATION_PIPELINE_CONFIG_TYPE);
			if(configNodeData==null)
				return false;
			String integrationPipelineConfigStr = configNodeData.getConfigData();
			logger.debug("IntegrationPipelineConfigStr:" + integrationPipelineConfigStr);
			IntegrationPipelineConfigXmlParser builder = new IntegrationPipelineConfigXmlParser();
			IntegrationPipes integrationPipes = builder.unmarshallConfigXMLtoObject(integrationPipelineConfigStr);
			// #TODO it is loaded from db where it will have only one
			// integration pipeline
			IntegrationPipe integrationPipe = integrationPipes.getIntegrationPipe().get(0);
			IntegrationPipelineConfigUnit integartionPipelineConfigUnit = new IntegrationPipelineConfigUnit(
					requestContext.getTenantId(), requestContext.getSiteId(), configNodeData.getParentConfigNodeId(), true,
					integrationPipe);
			integartionPipelineConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
			loadConfigurationInDataGrid(integartionPipelineConfigUnit);
			return true;

		}
		else{
			return true;
		}
		}
		catch(ConfigPersistenceException e){
			logger.error("Failed to reLoad Config from DB with Name=" + configName, e);
			throw new IntegrationPipelineConfigException("Failed to reLoad Config from DB with Name=" + configName, e);
		}
		catch(IntegrationPipelineConfigParserException | InvalidNodeTreeException e){
			logger.error("Failed to xml-parse Config from DB with Name=" + configName, e);
			throw new IntegrationPipelineConfigException("Failed to xml-parse Config from DB with Name=" + configName, e);
		}
		

	}
}
