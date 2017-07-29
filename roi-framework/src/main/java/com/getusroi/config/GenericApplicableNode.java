package com.getusroi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.InvalidNodeTreeException;
import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.getusroi.feature.config.FeatureConfigRequestContext;
import com.getusroi.feature.jaxb.Feature;

public class GenericApplicableNode {
	final Logger logger = LoggerFactory.getLogger(GenericApplicableNode.class);

	
	/**
	 * This method is used to get the node Id for a feature based on which
	 * tenant,site ,group , implementation name it belongs to
	 * 
	 * @param tenantId
	 *           : tenant for the feature
	 * @param siteId
	 *           : site for the feature
	 * @param featureGroup
	 *           : feature group for the feature
	 * @param : implName : Implementation Name
	 * @param feature
	 *           : Feature object
	 * @return Integer : get the id for feature store in database
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */
	protected Integer getApplicableNodeId(String tenantId, String siteId, String featureGroup, String implName,
			Feature feature) throws InvalidNodeTreeException, ConfigPersistenceException {

		String featureName = feature.getFeatureName();
		logger.debug("Finding ParentNodeId for Tenant=" + tenantId + "-siteId=" + siteId + "-featureGroup="
				+ featureGroup + "-featureName=" + featureName+" ,impl="+implName);

		return getApplicableNodeIdFeatureName(tenantId, siteId, featureGroup, featureName, implName);
	}// end of method getApplicableNodeId
	/**
	 * This method is used to get the node Id for a feature based on which
	 * tenant,site ,group, implementation Name,vendor and version it belongs to
	 * 
	 * @param tenantId
	 *           : tenant for the feature
	 * @param siteId
	 *           : site for the feature
	 * @param featureGroup
	 *           : feature group for the feature
	 * @param implName : Implementation Name
	 * @param vendorName : Vendor Name
	 * @param version : Version
	 * @param feature
	 *           : Feature object
	 * @return Integer : get the id for feature store in database
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */
	protected Integer getApplicableNodeId(String tenantId, String siteId, String featureGroup, String implName,
			String vendorName, String version, Feature feature)
			throws InvalidNodeTreeException, ConfigPersistenceException {

		String featureName = feature.getFeatureName();
		logger.debug("Finding ParentNodeId for Tenant=" + tenantId + "-siteId=" + siteId + "-featureGroup="
				+ featureGroup + "-featureName=" + featureName + " impl name : " + implName + ", vendore : "
				+ vendorName + ", version : " + version);

		return getApplicableNodeIdVendorName(tenantId, siteId, featureGroup, featureName, implName, vendorName,
				version);
	}// end of method getApplicableNodeId


	/**
	 * This method is used to get the node Id for a feature based on which
	 * tenant,site, group, feature name, Implementation, vendor and version it belongs to
	 * 
	 * @param tenantId
	 *           : tenant for the feature
	 * @param siteId
	 *           : site for the feature
	 * @param featureGroup
	 *           : feature group for the feature
	 * @param featureName
	 *           : Feature Name
	 * @param implName : Implementation Name
	 * @param vendorName : Vendor Name
	 * @param version : Version
	 * @return Integer : get the id for feature store in database
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */
	protected Integer getApplicableNodeIdVendorName(String tenantId, String siteId, String featureGroup, String featureName,String implName,String vendorName,String version)
			throws InvalidNodeTreeException, ConfigPersistenceException {
		logger.debug("Finding ParentNodeId for Tenant=" + tenantId + "-siteId=" + siteId + "-featureGroup=" + featureGroup + "-featureName="
				+ featureName+", impl name ="+implName+", vendor Name : "+vendorName+", version : "+version);

		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		return configPersistenceService.getApplicableNodeId(tenantId, siteId, featureGroup, featureName,implName,vendorName,version);
	}// end of method getApplicableNodeIdVendorName
	
	/**
	 * This method is used to get the node Id for a feature based on which
	 * tenant,site ,group, feature name and implementation  it belongs to
	 * 
	 * @param tenantId
	 *           : tenant for the feature
	 * @param siteId
	 *           : site for the feature
	 * @param featureGroup
	 *           : feature group for the feature
	 * @param featureName
	 *           : Feature Name
	 * @param implName : Implementation name in String
	 * @return Integer : get the id for feature store in database
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */
	protected Integer getApplicableNodeIdFeatureName(String tenantId, String siteId, String featureGroup, String featureName,String implName)
			throws InvalidNodeTreeException, ConfigPersistenceException {
		logger.debug("Finding ParentNodeId for Tenant=" + tenantId + "-siteId=" + siteId + "-featureGroup=" + featureGroup + "-featureName="
				+ featureName+", impl name="+implName);

		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		return configPersistenceService.getApplicableNodeId(tenantId, siteId, featureGroup, featureName,implName,null,null);
	}// end of method getApplicableNodeIdFeatureName

	/**
	 * Based on Tenant,Site,FeatureGroup,Feature finds the applicable NodeId to
	 * Tag FeatureConfiguration <BR>
	 *
	 * @param reqContext : RequestContext Object
	 * @return Integer : node Id
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */
	protected Integer getApplicableNodeId(RequestContext reqContext) throws InvalidNodeTreeException, ConfigPersistenceException {

		String featureGroup = reqContext.getFeatureGroup();
		String featureName = reqContext.getFeatureName();
		String tenantId = reqContext.getTenantId();
		String siteId = reqContext.getSiteId();
		String implName=reqContext.getImplementationName();
		String vendorName=reqContext.getVendor();
		String version=reqContext.getVersion();
		if(vendorName != null && !(vendorName.isEmpty())){
			return getApplicableNodeIdVendorName(tenantId, siteId, featureGroup, featureName,implName,vendorName,version);
		}else{
		return getApplicableNodeIdFeatureName(tenantId, siteId, featureGroup, featureName,implName);
		}
	}//end of method getApplicableNodeId(RequestContext reqContext)
	
	/**
	 * Based on Tenant,Site,FeatureGroup,Feature finds the applicable NodeId to
	 * Tag FeatureConfiguration <BR>
	 *
	 * @param configContext : ConfigurationContext Object
	 * @return Integer : NodeId
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 * */
	protected Integer getApplicableNodeId(ConfigurationContext configContext) throws InvalidNodeTreeException, ConfigPersistenceException {
		String tenantId = configContext.getTenantId();
		String siteId = configContext.getSiteId();
		String featureGroup = configContext.getFeatureGroup();
		String featureName = configContext.getFeatureName();
		String implName=configContext.getImplementationName();
		
		//added vendor and version support
		String vendorName=configContext.getVendorName();
		String version=configContext.getVersion();

		logger.debug("Finding ParentNodeId for Tenant=" + tenantId + "-siteId=" + siteId + "-featureGroup=" + featureGroup + "-featureName=" + featureName+",impl name : "+implName+"- vendor name : "+vendorName+" -version"+version);
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		if (featureName == null && featureGroup == null) {
			// if featureName and feature group are null than we want to tag it
			// to a Site
			return configPersistenceService.getApplicableNodeId(tenantId, siteId);
		}
		//get applicableNodeId using vendor name and version
		return configPersistenceService.getApplicableNodeId(tenantId, siteId, featureGroup, featureName,implName,vendorName,version);
	}
}
