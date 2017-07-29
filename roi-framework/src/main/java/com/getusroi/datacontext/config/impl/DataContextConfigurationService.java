package com.getusroi.datacontext.config.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.getusroi.datacontext.config.DataContextConstant.*;

import java.io.Serializable;

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
import com.getusroi.datacontext.config.DataContextConfigurationException;
import com.getusroi.datacontext.config.DataContextConfigurationUnit;
import com.getusroi.datacontext.config.DataContextConstant;
import com.getusroi.datacontext.config.DataContextParserException;
import com.getusroi.datacontext.config.IDataContextConfigurationService;
import com.getusroi.datacontext.jaxb.DataContext;
import com.getusroi.datacontext.jaxb.DataContexts;
import com.getusroi.datacontext.jaxb.FeatureDataContext;
import com.getusroi.feature.config.FeatureConfigurationUnit;
import com.getusroi.permastore.config.PermaStoreConfigParserException;
import com.getusroi.permastore.config.PermaStoreConfigRequestException;
import com.getusroi.permastore.config.PermaStoreConfigurationConstant;
import com.getusroi.permastore.config.PermaStoreConfigurationException;
import com.getusroi.permastore.config.PermaStoreConfigurationUnit;
import com.getusroi.permastore.config.impl.PermaStoreConfigBuilderHelper;
import com.getusroi.permastore.config.impl.PermaStoreConfigXMLParser;
import com.getusroi.permastore.config.jaxb.PermaStoreConfiguration;
import com.getusroi.permastore.config.jaxb.PermaStoreConfigurations;

public class DataContextConfigurationService extends GenericApplicableNode implements IDataContextConfigurationService {
	final Logger logger = LoggerFactory.getLogger(DataContextConfigurationService.class);

	/**
	 * This method is used to add dataContext into the db and dataGrid
	 * 
	 * @param configContext
	 *            : configuration Context contain the detail of
	 *            tenant,site,featuregroup,feature,vendor,version
	 * @param featureDataContext
	 *            : FeatureDataContext need to be added
	 * @throws DataContextConfigurationException
	 *             : Unable to add data context into the db/datagrid
	 */
	public void addDataContext(ConfigurationContext configContext, FeatureDataContext featureDataContext)
			throws DataContextConfigurationException {
		logger.debug(".addDataContext method of DataContextConfigurationService");
		Integer configNodeId = 0;
		String configKey = getConfigname(configContext.getFeatureGroup(), configContext.getFeatureName());
		try {
			logger.debug("Config key =" + configKey);
			configNodeId = getApplicableNodeId(configContext);
			logger.debug("Applicable Config Node Id is =" + configNodeId);
			String xmlString = convertDataContextObjectXmlToString(featureDataContext);
			// Update DB for this configuration
			ConfigNodeData configNodeData = new ConfigNodeData();
			configNodeData.setConfigName(configKey);
			configNodeData.setEnabled(true);
			boolean isConfigEnabled = true;
			configNodeData.setConfigLoadStatus("Sucess");
			configNodeData.setConfigType(DataContextConstant.DATACONTEXT_CONFIG_TYPE);
			configNodeData.setParentConfigNodeId(configNodeId);
			configNodeData.setConfigData(xmlString);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					configNodeId, configKey, DataContextConstant.DATACONTEXT_CONFIG_TYPE);
			int configDataId = 0;
			// Check if Configuration already exist in the DataBase or not
			if (loadedConfigNodeData == null) {
				configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
			} else {
				throw new DataContextConfigurationException("FeatureDataContext already exist for ConfigName="
						+ configKey + "--tree=" + configContext.getTenantId() + "/" + configContext.getSiteId() + "/"
						+ configContext.getFeatureGroup() + "/" + configContext.getFeatureName() + "/"
						+ configContext.getImplementationName() + "/" + configContext.getVendorName() + "/"
						+ configContext.getVersion());
			}
			// UpDate Cache for this if config is enabled
			if (!isConfigEnabled)
				return;
			DataContextConfigurationUnit dataContextConfigUnit = new DataContextConfigurationUnit(
					configContext.getTenantId(), configContext.getSiteId(), configNodeId, isConfigEnabled, configKey,
					featureDataContext);
			dataContextConfigUnit.setGroupId(configContext.getFeatureGroup());
			dataContextConfigUnit.setDbconfigId(configDataId);
			loadConfigurationInDataGrid(dataContextConfigUnit);

		} catch (ConfigPersistenceException | InvalidNodeTreeException | DataContextParserException sqlExp) {

			throw new DataContextConfigurationException("Failed to insert ConfigData in DB for configName=" + configKey,
					sqlExp);
		}

	}// end of method

	/**
	 * This method is used to get The DataContext configuration unit
	 * 
	 * @param requestContext
	 *            : Request Context
	 * @return DataContextConfigurationUnit : Data context configuration data
	 * @throws DataContextConfigurationException
	 *             : Unable to get the configuration Data
	 */
	public DataContextConfigurationUnit getDataContextConfiguration(RequestContext requestContext)
			throws DataContextConfigurationException {
		logger.debug(".getDataContextConfiguration method of DataContextConfigurationService");
		// check if tenet/site/featuregroup are not null and not blank
		Integer nodeId = 0;
		Integer defaultNodeId = 0;
		String configName = getConfigname(requestContext.getFeatureGroup(), requestContext.getFeatureName());
		try {
			isFeatureRequestValid(requestContext);
			if (requestContext.getVendor() != null) {
				nodeId = getApplicableNodeIdVendorName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName(), requestContext.getVendor(),
						requestContext.getVersion());
				defaultNodeId = getApplicableNodeIdVendorName("all", "all", requestContext.getFeatureGroup(),
						requestContext.getFeatureName(), requestContext.getImplementationName(),
						requestContext.getVendor(), requestContext.getVersion());
			} else {
				nodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName());
				defaultNodeId = getApplicableNodeIdFeatureName("all", "all", requestContext.getFeatureGroup(),
						requestContext.getFeatureName(), requestContext.getImplementationName());
			}
			logger.debug(".getFeatureDataContextConfiguration(reqContext=" + requestContext + ",configName="
					+ configName + ")");
			ROIConfigurationServer configServer = ROIConfigurationServer.getConfigurationService();
			String fdcGroupKey = DataContextConfigurationUnit.getConfigGroupKey(nodeId);
			logger.debug(".getFeatureDataContextConfiguration (fdcGroupKey=" + fdcGroupKey);
			DataContextConfigurationUnit featureDataContextConfigUnit = (DataContextConfigurationUnit) configServer
					.getConfiguration(requestContext.getTenantId() + "-" + requestContext.getSiteId(), fdcGroupKey,
							configName);
			// if not found at the FeatureName level find it at Feature Group
			// Level.
			logger.info("featureDataContextConfigUnit :" + featureDataContextConfigUnit);
			/*
			 * if (featureDataContextConfigUnit == null &&
			 * requestContext.getFeatureName() != null) { nodeId =
			 * getApplicableNodeIdFeatureName(requestContext.getTenantId(),
			 * requestContext.getSiteId(), requestContext.getFeatureGroup(),
			 * null, null); featureDataContextConfigUnit =
			 * (DataContextConfigurationUnit) configServer
			 * .getConfiguration(requestContext.getTenantId(), fdcGroupKey,
			 * configName); }
			 */

			if (featureDataContextConfigUnit == null && requestContext.getFeatureName() != null) {
				logger.info("featureDataContextConfigUnit==null");
				fdcGroupKey = DataContextConfigurationUnit.getConfigGroupKey(defaultNodeId);
				logger.debug(".getFeatureDataContextConfiguration (fdcGroupKey=" + fdcGroupKey);
				 featureDataContextConfigUnit = (DataContextConfigurationUnit) configServer
						.getConfiguration("all" + "-" + "all", fdcGroupKey,
								configName);
			}
			logger.info("featureDataContextConfigUnit :" + featureDataContextConfigUnit);
			return featureDataContextConfigUnit;
		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new DataContextConfigurationException(
					"Unable to get the feature for tenant : " + requestContext.getTenantId() + ", site : "
							+ requestContext.getSiteId() + ", feature group : " + requestContext.getFeatureGroup()
							+ ", feature : " + requestContext.getFeatureName() + ", implementation Name : "
							+ requestContext.getImplementationName() + ", vendor : " + requestContext.getVendor()
							+ "version : " + requestContext.getVersion() + ", and configname : " + configName,
					e);
		}
	}

	/**
	 * Finds the Respective DataContextConfiguration and Deletes it from the
	 * DataGrid as well as from the DataBase<BR>
	 * Note:-To purge/remove only from DataGrid use
	 * .changeStatusOfFeatureConfig() marking status as disabled.
	 * 
	 * @param requestContext
	 * @return boolean : dataContext configuration deleted successfully or not
	 * @throws DataContextConfigurationException
	 * 
	 */
	public boolean deleteDataContextConfiguration(ConfigurationContext configContext)
			throws DataContextConfigurationException {
		logger.debug(".deleteDataContextConfiguration(configContext=" + configContext + ")");
		RequestContext reqContext = new RequestContext(configContext.getTenantId(), configContext.getSiteId(),
				configContext.getFeatureGroup(), configContext.getFeatureName(), configContext.getImplementationName(),
				configContext.getVendorName(), configContext.getVersion());
		String configName = getConfigname(reqContext.getFeatureGroup(), reqContext.getFeatureName());
		try {
			// First get the configuration from the dataGrid so that we can get
			// the
			// NodeDataId
			DataContextConfigurationUnit fdcConfigUnit = getDataContextConfiguration(reqContext);
			logger.debug("DataContextCOnfiguration Unit : " + fdcConfigUnit);
			if (fdcConfigUnit == null) {
				logger.warn("Delete request for Non Cache DataContextConfig=" + configName);
				// delete from DB
				Integer configNodeId = getApplicableNodeId(reqContext);
				return deleteFeatureConfigurationFromDb(configName, configNodeId);
			}
			// Delete from the DB First so that configVerifier should not
			// revitalise the config in dataGrid
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			boolean isDeleted = configPersistenceService.deleteConfigNodeData(fdcConfigUnit.getDbconfigId());
			String fdcGroupKey = FeatureConfigurationUnit.getConfigGroupKey(fdcConfigUnit.getAttachedNodeId());
			logger.debug(
					".deleteDataContextConfiguration() deleted from db NodeDataId=" + fdcConfigUnit.getDbconfigId());

			// Now remove from DataGrid
			ROIConfigurationServer configServer = ROIConfigurationServer.getConfigurationService();
			configServer.deleteConfiguration(fdcConfigUnit.getTenantId(), fdcGroupKey, configName);
			logger.debug(".deleteDataContextConfiguration() deleted from DataGrid fsGroupKey=" + fdcGroupKey
					+ " configName=" + configName);
			DataContextConfigurationUnit fsconfigUnit1 = getDataContextConfiguration(reqContext);
			logger.debug("DataContextConfiguration Unit after delete : " + fsconfigUnit1);
			return true;
		} catch (ConfigPersistenceException | ConfigServerInitializationException | InvalidNodeTreeException e) {
			throw new DataContextConfigurationException(
					"Failed to Delete DataContextConfiguration with name " + configName, e);
		}
	}// end of method

	/**
	 * This method is used to convert FeatureDataContext Object into xml string
	 * 
	 * @param featureDataContext
	 *            : FeatureDataContext Object
	 * @return xmlString : FeatureDataContext Object into xml string
	 * @throws DataContextParserException
	 *             : Unable to Convert FeatureDataContext into xml String
	 */
	private String convertDataContextObjectXmlToString(FeatureDataContext featureDataContext)
			throws DataContextParserException {
		logger.debug("inside convertFeatureObjectXmlToString of DataContextConfigurationService");
		// Convert configTo Valid XML to store independent inDataBase
		DataContextConfigXMLParser builder = new DataContextConfigXMLParser();
		String xmlString = builder.unmarshallObjecttoXML(featureDataContext);
		logger.debug("xsml string : " + xmlString);
		return xmlString;

	}

	/**
	 * This method is used to give config name
	 * 
	 * @param featureGroup
	 *            : feature group in String
	 * @param featureName
	 *            : feature name in String
	 * @return configName : configname in string
	 */
	private String getConfigname(String featureGroup, String featureName) {
		logger.debug(".getConfigname method of DataContextConfiguration Service");
		String configName = featureGroup + "-" + featureName + "-" + DataContextConstant.DATACONTEXT_SUFFIX_KEY;
		return configName;
	}

	/**
	 * This method is used to load configuration into data grid
	 * 
	 * @param featureConfigUnit
	 *            : Configuration unit for feature
	 * @throws DataContextConfigurationException
	 */
	private void loadConfigurationInDataGrid(DataContextConfigurationUnit dataContextConfigUnit)
			throws DataContextConfigurationException {
		logger.debug(".loadConfigurationInDataGrid() DataContextConfigurationUnit=" + dataContextConfigUnit);
		try {
			ROIConfigurationServer configServer = ROIConfigurationServer.getConfigurationService();
			configServer.addConfiguration(dataContextConfigUnit);

		} catch (ConfigServerInitializationException e) {
			throw new DataContextConfigurationException(
					"Failed to Upload in DataGrid configName=" + dataContextConfigUnit.getKey(), e);
		}
	}// end of method loadConfigurationInDataGrid

	private void isFeatureRequestValid(RequestContext requestContext) throws DataContextConfigurationException {
		if (requestContext == null || !requestContext.isValid()) {
			throw new DataContextConfigurationException("RequestContext is null or has required data as null or empty");
		}
	}

	/**
	 * delete the DataContextconfigaration by configName and NodeId
	 * 
	 * @param configName
	 * @param nodeId
	 * @return boolean
	 * @throws DataContextConfigurationException
	 */
	private boolean deleteFeatureConfigurationFromDb(String configName, int nodeId)
			throws DataContextConfigurationException {
		// Delete from the DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		logger.debug(".deleteFeatureConfigurationFromDb() deleted from db configName=" + configName);
		try {
			configPersistenceService.deleteConfigNodeDataByNodeIdAndConfigName(configName, nodeId);
		} catch (ConfigPersistenceException e) {
			logger.error("Persistance exception deleting the node cause: " + e);
			throw new DataContextConfigurationException("Persistance exception deleting the node cause " + e);
		}
		return true;
	}// end of method

	@Override
	public boolean checkDataContextConfigExistOrNot(ConfigurationContext configurationContext, String configName)
			throws DataContextParserException, DataContextConfigurationException {
		boolean isEnabled = false;
		logger.debug("Inside checkDataContextConfigExistOrNot method with configurationContext = "
				+ configurationContext + " configName = " + configName);
		RequestContext requestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(),
				configurationContext.getFeatureName(), configurationContext.getImplementationName(),
				configurationContext.getVendorName(), configurationContext.getVersion());
		String vendorName = requestContext.getVendor();
		String version = requestContext.getVersion();
		DataContextConfigurationUnit dataContextConfigurationUnit = null;
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
					configName, DATACONTEXT_CONFIG_TYPE);

			// if confignodedata not Exist
			if (configNodeData == null)
				return false;

			isEnabled = configNodeData.isEnabled();
			if (isEnabled) {
				dataContextConfigurationUnit = getDataContextConfiguration(requestContext);
				if (dataContextConfigurationUnit == null) {
					enableAndLoadDataContextConfig(requestContext, configNodeData);
				}
			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new DataContextParserException("Error in Searching the Feature with FeatureName = " + configName
					+ " and with requestContext = " + requestContext);
		}
		return true;
	}
	
	/**
	 * Re-loads the DataContext into cache from the configured source.
	 * @throws DataContextConfigurationException 
	 * @throws  
	 * @throws DataContextParserException 
	 */
	@Override
	public boolean reloadDataContextCacheObject(RequestContext requestContext, String configName) throws DataContextConfigurationException 
			{
		logger.debug("reloadDataContextCacheObject method");
		if(requestContext==null && configName==null)
			throw new DataContextConfigurationException("requestContext and configName both should not be null");
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();

		DataContextConfigurationUnit dataContextConfigurationUnit = null;
		try {
			dataContextConfigurationUnit = getDataContextConfiguration(requestContext);
			if (dataContextConfigurationUnit == null) {
				Integer applicableNodeId = getApplicableNodeId(requestContext);
				ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
						applicableNodeId, configName, DATACONTEXT_CONFIG_TYPE);
				if(configNodeData==null){
					return false;
				}
				String psconfigStr = configNodeData.getConfigData();
				DataContextConfigXMLParser builder = new DataContextConfigXMLParser();
				FeatureDataContext dsConfigs = builder.marshallConfigXMLtoObject(psconfigStr);
				
				DataContextConfigurationUnit dcConfigUnit = new DataContextConfigurationUnit(requestContext.getTenantId(),
						requestContext.getSiteId(), configNodeData.getParentConfigNodeId(), true, configNodeData.getConfigName(),
						dsConfigs);
				dcConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
				loadConfigurationInDataGrid(dcConfigUnit);
				return true;
			} else {
				return true;
			}
		} catch (DataContextParserException e) {
			logger.error("Failed to reLoad ConfigurationUnit from cache it either not exist or is disabled with Name="
					+ configName, e);
			throw new DataContextConfigurationException(
					"Failed to reLoad ConfigurationUnit from cache it either not exist or is disabled with Name="
							+ configName,
					e);
		} catch (ConfigPersistenceException e) {
			logger.error("Failed to reLoad Config from DB with Name=" + configName, e);
			throw new DataContextConfigurationException("Failed to reLoad Config from DB with Name=" + configName, e);
		} catch (InvalidNodeTreeException e) {
			logger.error("Failed to find the applicable NodeId ", e);
			throw new DataContextConfigurationException("Failed to find the applicable NodeId ", e);
		}

	}

	private void enableAndLoadDataContextConfig(RequestContext reqCtx, ConfigNodeData configNodeData)
			throws ConfigPersistenceException, DataContextParserException, DataContextConfigurationException {
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		configPersistenceService.enableConfigNodeData(true, configNodeData.getNodeDataId());
		String dsConfigStr = configNodeData.getConfigData();
		DataContextConfigXMLParser builder = new DataContextConfigXMLParser();
		FeatureDataContext dsConfigs = builder.marshallConfigXMLtoObject(dsConfigStr);
		DataContext dsConfig = dsConfigs.getDataContexts().getDataContext().get(0);
		DataContextConfigurationUnit dcConfigUnit = new DataContextConfigurationUnit(reqCtx.getTenantId(),
				reqCtx.getSiteId(), configNodeData.getParentConfigNodeId(), true, configNodeData.getConfigName(),
				dsConfigs);
		dcConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
		loadConfigurationInDataGrid(dcConfigUnit);
	}

}
