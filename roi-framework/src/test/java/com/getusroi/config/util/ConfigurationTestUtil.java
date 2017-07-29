package com.getusroi.config.util;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigNode;
import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;

/**
 * This is Util class to check test tree node
 * @author bizruntime
 *
 */
public class ConfigurationTestUtil {
	final static Logger logger = LoggerFactory
			.getLogger(ConfigurationTestUtil.class);

	/**
	 * This method is used to check if test node or not
	 * @throws ConfigPersistenceException
	 */
	public static void checkAndAddTestNode() throws ConfigPersistenceException {

		logger.debug("inside checkAndAddTestNode() of ParmastoreConfigurationTestUtil ");
		// check if we have any node with node id 26 or not
		int parentid = 0;
		

		IConfigPersistenceService configPersistence = new ConfigPersistenceServiceMySqlImpl();
		parentid = configPersistence.getNodeIdByNodeNameAndByType(GenericTestConstant.TEST_TENANTID,ConfigNode.NODETYPE_TENANT,0);
		logger.debug("tenantName  with " + GenericTestConstant.TEST_TENANTID+ " with nodeId " + parentid);

		//if test tenant doesnot exist
		if (parentid == 0) {
			logger.debug("creating tenant with nodname  "+ GenericTestConstant.TEST_TENANTID);

			String tenant = GenericTestConstant.TEST_TENANTID;
			ConfigNode confignodetenant = createConfigNodeForTest(tenant,"This is a " + tenant + "tenant", true, true, 0,ConfigNode.NODETYPE_TENANT,ConfigNode.NODELEVEL_TENANT);

			parentid = configPersistence.insertConfigNode(confignodetenant);

		}
		// checking if site already exist
		String site = GenericTestConstant.TEST_SITEID;
		int siteId = 0;
		siteId = configPersistence.getNodeIdByNodeNameAndByType(site,ConfigNode.NODETYPE_SITE,parentid);
		logger.debug("sitename  with " + GenericTestConstant.TEST_SITEID+ " with nodeId " + siteId);
		
		//if test site doesnot exist
		if (siteId == 0) {

			logger.debug("creating site with nodname  "+ GenericTestConstant.TEST_SITEID);
			ConfigNode confignodesite = createConfigNodeForTest(site,"This is a " + site + " site", false, true, parentid,
					ConfigNode.NODETYPE_SITE,ConfigNode.NODELEVEL_SITE);
			siteId = configPersistence.insertConfigNode(confignodesite);

		}

		// checking if featuregroup already exist
		String featureGroup = GenericTestConstant.TEST_FEATUREGROUP;

		int featureGroupId = 0;

		featureGroupId = configPersistence.getNodeIdByNodeNameAndByType(featureGroup, "feature_group", siteId);
		logger.debug("featureGroupName  with "+ GenericTestConstant.TEST_FEATUREGROUP + " with nodeId "+ featureGroupId);
		
		//test featuregroup desnot exist
		if (featureGroupId == 0) {

			logger.debug("creating featureGroup with nodname  "+ GenericTestConstant.TEST_FEATUREGROUP);
			ConfigNode confignodegroup = createConfigNodeForTest(featureGroup,"This is a " + featureGroup + " featureGroup", false, true,siteId, ConfigNode.NODETYPE_FEATUREGROUP,ConfigNode.NODELEVEL_FEATUREGROUP);
			featureGroupId = configPersistence.insertConfigNode(confignodegroup);
			logger.debug("created featureGroup with nodname  "+ GenericTestConstant.TEST_FEATUREGROUP  +"  featureGroup Id "+featureGroupId);
		}

		// checking if feature already exist
		String feature = GenericTestConstant.TEST_FEATURE;

		int featureId = 0;

		featureId = configPersistence.getNodeIdByNodeNameAndByType(feature,"feature", featureGroupId);
		logger.debug("featureName  with "+ GenericTestConstant.TEST_FEATURE + " with nodeId "+ featureId);
		
		//test feature group doesnot exist
		if (featureId == 0) {
			
			logger.debug("creating featureId with nodname  "+ GenericTestConstant.TEST_FEATURE);
			ConfigNode confignodefeature = createConfigNodeForTest(feature,"This is a " + feature + " feature", false, false,featureGroupId, ConfigNode.NODETYPE_FEATURE,ConfigNode.NODELEVEL_FEATURE);
			featureId = configPersistence.insertConfigNode(confignodefeature);
			GenericTestConstant.TEST_NODEID = featureId;

		} else {
			GenericTestConstant.TEST_NODEID = featureId;

		}
		logger.debug("testnodeid  is set with  "+	GenericTestConstant.TEST_NODEID);
		
	}// end of method

	
	/**
	 * This method is used to create confignode object for test
	 * @param nodeName : String type
	 * @param description : String type
	 * @param isRoot : boolean type
	 * @param hasChildren : boolean type
	 * @param parentNodeId : int type
	 * @param nodeType : String type
	 * @param nodeLevel : int type
	 * @return ConfigNode object
	 */
	private static ConfigNode createConfigNodeForTest(String nodeName,
			String description, boolean isRoot, boolean hasChildren,
			int parentNodeId, String nodeType, int nodeLevel) {
		logger.debug("inside createConfigNodeForTest() of ConfigurationTestUtil");
		ConfigNode confignode = new ConfigNode();
		confignode.setNodeName(nodeName);
		confignode.setDescription(description);
		confignode.setRoot(isRoot);
		confignode.setHasChildren(hasChildren);
		confignode.setParentNodeId(parentNodeId);
		confignode.setType(nodeType);
		confignode.setLevel(nodeLevel);

		return confignode;
	}
	
	
	public static void main(String[] args) throws ConfigPersistenceException, SQLException {
		checkAndAddTestNode();
	}
}
