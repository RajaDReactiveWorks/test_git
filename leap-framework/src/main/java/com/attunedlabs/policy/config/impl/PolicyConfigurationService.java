package com.attunedlabs.policy.config.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.GenericApplicableNode;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.beans.ConfigurationUnit;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.permastore.config.PermaStoreConfigurationException;
import com.attunedlabs.policy.config.IPolicyConfigurationService;
import com.attunedlabs.policy.config.PolicyConfigXMLParser;
import com.attunedlabs.policy.config.PolicyConfigXMLParserException;
import com.attunedlabs.policy.config.PolicyConfigurationException;
import com.attunedlabs.policy.config.PolicyConfigurationUnit;
import com.attunedlabs.policy.config.PolicyConstant;
import com.attunedlabs.policy.config.PolicyRequestContext;
import com.attunedlabs.policy.config.PolicyRequestException;
import com.attunedlabs.policy.jaxb.Policies;
import com.attunedlabs.policy.jaxb.Policy;

public class PolicyConfigurationService extends GenericApplicableNode implements IPolicyConfigurationService {
	final Logger logger = LoggerFactory.getLogger(PolicyConfigurationService.class);
	private PolicyConfigurationUnitBuilder configUnitBuilder;
	
	public PolicyConfigurationService(){
		this.configUnitBuilder=new PolicyConfigurationUnitBuilder();
	}
	
	public void addPolicyConfiguration(ConfigurationContext configurationContext, Policy policyConfig) throws PolicyConfigurationException {
		logger.debug(".addPolicyConfiguration() tree is " + configurationContext.getTenantId() + "/" + configurationContext.getSiteId() + " for policy=" + policyConfig.getPolicyName());
		// Check and get ConfigNodeId for this
		String featureGrp = policyConfig.getFeature().getFeatureGroup();
		String featureName = policyConfig.getFeature().getFeatureName();
		String policyName = policyConfig.getPolicyName();
		String tenantId=configurationContext.getTenantId();
		String siteId=configurationContext.getSiteId();
		String vendorName=configurationContext.getVendorName();
		String version=configurationContext.getVersion();
		String implName=configurationContext.getImplementationName();
		try {
			Integer configNodeId =0;
			if((vendorName != null && !(vendorName.isEmpty()) && !(vendorName.equalsIgnoreCase("")))&&(version != null && !(version.isEmpty()) && !(version.equalsIgnoreCase("")))){
				configNodeId = getApplicableNodeIdVendorName(tenantId, siteId, featureGrp, featureName,implName,vendorName,version);
			}else{
				configNodeId = getApplicableNodeIdFeatureName(tenantId, siteId, featureGrp, featureName,implName);	
			}
			logger.debug("Applicable Config Node Id is =" + configNodeId);
			
			//Build the Policy Expression first
			ConfigNodeData configNodeData=buildConfigNodeData(policyConfig,configNodeId);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(configNodeId, policyName,PolicyConstant.POLICY_CONFIG_TYPE);

			int configDataId = 0;
			// Check if Configuration already exist in the DataBase or not
			if (loadedConfigNodeData == null) {
				configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
			} else {
				throw new PolicyConfigurationException("pOlicy already exist for policyName=" + policyName + "--tree=" + tenantId + "/" + siteId + "/" + featureGrp
						+ "/" + featureName+"/"+configurationContext.getImplementationName());
			}

			// UpDate Cache for this only if config is enabled
			if (!policyConfig.isEnabled())
				return;

			PolicyConfigurationUnit psConfigUnit = new PolicyConfigurationUnit(tenantId, siteId, configNodeId, true, policyConfig, null);
			psConfigUnit.setDbconfigId(configDataId);
			this.configUnitBuilder.buildPolicyConfigUnit(policyConfig, psConfigUnit);
			loadConfigurationInDataGrid(psConfigUnit);

		} catch (InvalidNodeTreeException | ConfigPersistenceException | PolicyConfigXMLParserException | PolicyFactBuilderException e) {
			logger.error("Failed to add Policy=" + policyName, e);
			throw new PolicyConfigurationException("Failed to add Policy=" + policyName, e);
		}

	}
	
	public PolicyConfigurationUnit getPolicyConfigurationUnit(PolicyRequestContext polRequestContext,String policyName)throws PolicyConfigurationException{
		logger.debug(".getPolicyConfigurationUnit() PolicyName="+policyName+"+-RequestContext="+polRequestContext);
		try {
			Integer nodeId=getApplicableNodeId(polRequestContext);
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			String policyGrpKey=PolicyConfigurationUnit.getConfigGroupKey(nodeId);
			ConfigurationUnit configUnit=configServer.getConfiguration(polRequestContext.getTenantId(), policyGrpKey, policyName);
			return (PolicyConfigurationUnit)configUnit;
		
		} catch (InvalidNodeTreeException |ConfigPersistenceException e) {
			logger.error(".getPolicyConfigurationUnit() Invalid Config Node Tree",e); 
			throw new PolicyConfigurationException("Invalid Config Node Tree",e);
			
		} catch (ConfigServerInitializationException e) {
			logger.error(".getPolicyConfigurationUnit() Failed to connect to Config Server",e); 
			throw new PolicyConfigurationException("Failed to connect to Config Server",e);
		}
	}
	
	
	public Object getPolicyResponseData(PolicyRequestContext policyRequestContext,String policyName)throws PolicyRequestException, PolicyInvalidRegexExpception{
		logger.debug(".getPolicyResponseData() PolicyName="+policyName+"+-RequestContext="+policyRequestContext);
		PolicyConfigurationUnit configUnit;
		try {
			configUnit = getPolicyConfigurationUnit( policyRequestContext,policyName);
			logger.debug("evaluation list : "+configUnit.getEvaluationUnitList());
			if(configUnit==null)
				throw new PolicyRequestException("Policy {"+policyName+"} is disabled or does not exist");
			PolicyEvaluationRequestHandler reqHandler=new PolicyEvaluationRequestHandler();
			boolean bol=reqHandler.evaluatePolicy(configUnit, policyRequestContext);
			if(bol){
				return reqHandler.getPolicyResponseData(configUnit, policyRequestContext);
			}else{
				return null;
			}
				
		} catch (PolicyConfigurationException e) {
			logger.error("Failed to retreive Policy "+policyName,e);
			throw new PolicyRequestException("Failed to retreive Policy "+policyName,e);
		}
		
	}
	
	
	
	
	
	private ConfigNodeData buildConfigNodeData(Policy policy,Integer configNodeId) throws PolicyConfigXMLParserException{
		ConfigNodeData configNodeData = new ConfigNodeData();
		configNodeData.setConfigName(policy.getPolicyName());
		configNodeData.setEnabled(policy.isEnabled());
		configNodeData.setConfigLoadStatus("Sucess");
		configNodeData.setConfigType(PolicyConstant.POLICY_CONFIG_TYPE);
		configNodeData.setParentConfigNodeId(configNodeId);
		//Build xml from policy so that it can be stored in DB
		PolicyConfigXMLParser builder = new PolicyConfigXMLParser();
		String xmlString = builder.unmarshallObjecttoXML(policy);
		configNodeData.setConfigData(xmlString);
		return configNodeData;
	}
	
	private void loadConfigurationInDataGrid(PolicyConfigurationUnit polConfigUnit) throws PolicyConfigurationException {
		logger.debug(".loadConfigurationInDataGrid() PolicyConfigurationUnit=" + polConfigUnit);
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			configServer.addConfiguration(polConfigUnit);

		} catch (ConfigServerInitializationException e) {
			throw new PolicyConfigurationException("Failed to Upload in DataGrid configName=" + polConfigUnit.getKey(), e);
		}
	}

	
	
	/**
	 * Changes the status of the Policy to enable or disable
	 *  @param policyRequestContext
	 * @param policyName
	 * @param enable true=enable,false=disable
	 * @throws PolicyConfigurationException
	 * @throws PolicyConfigXMLParserException 
	 */

	
	public boolean changePolicyStatus(ConfigurationContext configurationContext, String policyName, boolean isEnable) throws PolicyConfigurationException, PolicyConfigXMLParserException {
		
		Integer applicableNodeId;
		PolicyRequestContext policyRequestContext=new PolicyRequestContext(configurationContext.getTenantId(), configurationContext.getSiteId(), configurationContext.getFeatureGroup(),configurationContext.getFeatureName(),configurationContext.getImplementationName(),configurationContext.getVendorName(),configurationContext.getVersion());
		try {
			applicableNodeId = getApplicableNodeId(policyRequestContext);
		
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData configNodeData=configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableNodeId, policyName, PolicyConstant.POLICY_CONFIG_TYPE);
		if(configNodeData==null){
			//Not in DB so it does not exist throw exception
			throw new PolicyConfigurationException("Policy with Name( "+policyName+") does not exist in DB");

		
		}
			
		//Disable Request	
		if(!isEnable){
			//We have to Disable policyConfig hence remove from DataGrid and update DB as disabled Configuration
			configPersistenceService.enableConfigNodeData(false,configNodeData.getNodeDataId());
						
			//Now remove from DataGrid
			String psGroupKey= PolicyConfigurationUnit.getConfigGroupKey(configNodeData.getParentConfigNodeId());
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			configServer.deleteConfiguration(policyRequestContext.getTenantId(), psGroupKey, policyName);
		
		}else{
		//Enable Request-Load Config from DataBase and update the DataGrid
			enableAndLoadPolicyConfig(policyRequestContext, configNodeData)	;	
			}
		} catch (ConfigPersistenceException | PermaStoreConfigurationException |ConfigServerInitializationException | PolicyFactBuilderException e) {
			throw new PolicyConfigurationException("Failed to Enable/Disable Policy with name "+policyName,e);
		}
		return false;
	}

	private void enableAndLoadPolicyConfig(PolicyRequestContext reqCtx,ConfigNodeData configNodeData) throws ConfigPersistenceException, PolicyConfigXMLParserException, PolicyConfigurationException, PolicyFactBuilderException{
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		//Update Enable in the Database
		configPersistenceService.enableConfigNodeData(true,configNodeData.getNodeDataId());
		//Get XML from DB and Load in the DataGrid
		String psconfigStr=configNodeData.getConfigData();
		PolicyConfigXMLParser builder = new PolicyConfigXMLParser();
		Policies psConfigs = builder.marshallXMLtoObject(psconfigStr);
		//As it is loaded from DB I know there will always be one config only
		Policy policyConfig=psConfigs.getPolicy().get(0);
		
		if (configUnitBuilder == null)
			configUnitBuilder = new PolicyConfigurationUnitBuilder();
		
		PolicyConfigurationUnit policyConfigUnit = new PolicyConfigurationUnit(reqCtx.getTenantId(), reqCtx.getSiteId(),configNodeData.getParentConfigNodeId(), true, policyConfig, null);
		policyConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
		configUnitBuilder.buildPolicyConfigUnit(policyConfig, policyConfigUnit);

		loadConfigurationInDataGrid(policyConfigUnit);
		
	}
	
	@Override
	public boolean deletePolicy(ConfigurationContext configurationContext, String policyName) throws PolicyConfigurationException {
		
		logger.debug(".deletePolicy(configurationContext=" + configurationContext + ",policyName="+policyName+")");
		PolicyRequestContext policyRequestContext=new PolicyRequestContext(configurationContext.getTenantId(), configurationContext.getSiteId(), configurationContext.getFeatureGroup(),configurationContext.getFeatureName(),configurationContext.getImplementationName(),configurationContext.getVendorName(),configurationContext.getVersion());

		try {
			//First get the configuration from the dataGrid so that we can get the NodeDataId
			PolicyConfigurationUnit psconfigUnit = getPolicyConfigurationUnit(policyRequestContext, policyName);
			
			if(psconfigUnit==null){
				logger.warn("Delete request for Non Cache PolicyConfig="+policyName);
				//delete from DB 
				Integer configNodeId = getApplicableNodeId(policyRequestContext);
				return	deletePolicyFromDb(policyName, configNodeId);
				}
				
			
			//Delete from the DB First so that configVerifier should not revitalise the config in dataGrid
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			configPersistenceService.deleteConfigNodeData(psconfigUnit.getDbconfigId());
			String psGroupKey= PolicyConfigurationUnit.getConfigGroupKey(psconfigUnit.getAttachedNodeId());
			logger.debug(".deletePolicy() deleted from db NodeDataId="+psconfigUnit.getDbconfigId());
			
			//Now remove from DataGrid
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			configServer.deleteConfiguration(psconfigUnit.getTenantId(), psGroupKey, policyName);
			logger.debug(".deletePolicy() deleted from DataGrid psGroupKey="+psGroupKey+" policyName="+policyName);
			
			return true;
		} catch (ConfigPersistenceException |InvalidNodeTreeException | ConfigServerInitializationException e) {
			throw new PolicyConfigurationException("Failed to Delete Policy with name "+policyName,e);
		} 
	}
	/**
	 * delete  the Policy by configName and NodeId
	 * @param  configName
	 * @param nodeId
	 * @return boolean 
	 * @throws PolicyConfigurationException
	 */
	private boolean deletePolicyFromDb(String configName,
			int nodeId) throws PolicyConfigurationException {
		// Delete from the DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
				logger.debug(".deletePolicyFromDb() deleted from db configName="
				+ configName);
		try {
			configPersistenceService.deleteConfigNodeDataByNodeIdAndConfigName(
					configName, nodeId);
		} catch (ConfigPersistenceException e) {
			logger.error("Persistance exception deleting the node cause: " + e);

		}
		// Now remove from DataGrid
		return true;
	}

	/**
	 * based on the PolicyRequestContext and policy name searching Policy ,if policy Exist in DB check wether it enabled or not ,
	 * if enabled check exist in cache or not , if not Exist load the data to cache and return true else false
	 * @param requestContext
	 * @param policyName
	 * @throws PolicyRequestException
	 * @throws PolicyConfigurationException
	 * 
	 */
	public boolean checkPolicyExistInDbAndCache(ConfigurationContext configurationContext, String policyName)throws PolicyConfigurationException, PolicyRequestException {
		
		logger.debug("Inside checkPolicyExistInDbAndCache  with configurationContext= "+configurationContext +" policyname="+policyName);
		PolicyRequestContext requestContext=new PolicyRequestContext(configurationContext.getTenantId(), configurationContext.getSiteId(), configurationContext.getFeatureGroup(),configurationContext.getFeatureName(),configurationContext.getImplementationName(),configurationContext.getVendorName(),configurationContext.getVersion());
		PolicyConfigurationUnit policyConfigurationUnit=null;
		boolean isEnabled=false;
		int featureNodeId;
		try {
			featureNodeId = getApplicableNodeId(requestContext);
		
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData configNodeData=configPersistenceService.getConfigNodeDatabyNameAndNodeId(featureNodeId, policyName,PolicyConstant.POLICY_CONFIG_TYPE);
		
		//if confignodedata not Exist
		if(configNodeData==null)
			return false;
		
		isEnabled=configNodeData.isEnabled();
		if(isEnabled){
			try {
			policyConfigurationUnit=getPolicyConfigurationUnit(requestContext, policyName);
			if(policyConfigurationUnit==null){
					enableAndLoadPolicyConfig(requestContext, configNodeData);	
			}
			} catch (PolicyConfigXMLParserException | PolicyFactBuilderException e) {
				throw new PolicyConfigurationException("Error in loading policy data into cache with policyName="+policyName +" requestContext="+requestContext);
			}
		}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new PolicyRequestException("Error in Searching the policy with policyName="+policyName +" requestContext="+requestContext);
		}
		return true;
	}
	
	/**
	 * this method is used to reload policy into cache
	 * @param requestContext
	 * @param policyName
	 * @return
	 * @throws PolicyConfigurationException
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 * @throws PolicyConfigXMLParserException
	 * @throws PolicyFactBuilderException
	 */
		@Override
		public boolean reloadPolicyCacheObject(RequestContext requestContext, String policyName) throws PolicyConfigurationException  {
			logger.debug("inside reloadPolicyCacheObject()");
			if(requestContext==null && policyName==null)
				throw new PolicyConfigurationException("requestContext and policyName both should not be null");
			try{
			PolicyRequestContext policyRequestContext = new PolicyRequestContext(requestContext.getTenantId(),
					requestContext.getSiteId(), requestContext.getFeatureGroup(), requestContext.getFeatureName(),
					requestContext.getImplementationName(), requestContext.getVendor(), requestContext.getVersion());
			PolicyConfigurationUnit policyConfigurationUnit = getPolicyConfigurationUnit(policyRequestContext, policyName);
			if(policyConfigurationUnit==null){
				int featureNodeId = getApplicableNodeId(requestContext);

				IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
				ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(featureNodeId,
						policyName, PolicyConstant.POLICY_CONFIG_TYPE);
				if(configNodeData==null)
					return false;
				String psconfigStr = configNodeData.getConfigData();
				PolicyConfigXMLParser builder = new PolicyConfigXMLParser();
				Policies psConfigs = builder.marshallXMLtoObject(psconfigStr);
				// As it is loaded from DB I know there will always be one config only
				Policy policyConfig = psConfigs.getPolicy().get(0);

				if (configUnitBuilder == null)
					configUnitBuilder = new PolicyConfigurationUnitBuilder();

				PolicyConfigurationUnit policyConfigUnit = new PolicyConfigurationUnit(requestContext.getTenantId(), requestContext.getSiteId(),
						configNodeData.getParentConfigNodeId(), true, policyConfig, null);
				policyConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
				configUnitBuilder.buildPolicyConfigUnit(policyConfig, policyConfigUnit);

				loadConfigurationInDataGrid(policyConfigUnit);
				return true;
			}
			else{
				return true;
			}
			}
			catch(ConfigPersistenceException e){
				logger.error("Failed to reLoad policy from DB with Name=" + policyName, e);
				throw new PolicyConfigurationException("Failed to reLoad policy from DB with Name=" + policyName, e);
			}
			catch(InvalidNodeTreeException | PolicyConfigXMLParserException | PolicyFactBuilderException e){
				logger.error("Failed to xml-parse policy from DB with Name=" + policyName, e);
				throw new PolicyConfigurationException("Failed to xml-parse policy from DB with Name=" + policyName, e);
			}
		}
	
}
