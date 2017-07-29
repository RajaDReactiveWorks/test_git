package com.getusroi.config.persistence.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigNode;
import com.getusroi.config.persistence.ConfigNodeData;
import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.ConfigurationTreeNode;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.ITenantConfigTreeService;
import com.getusroi.config.persistence.InvalidNodeTreeException;
import com.getusroi.config.persistence.dao.ConfigFeatureMasterDAO;
import com.getusroi.config.persistence.dao.ConfigNodeDAO;
import com.getusroi.config.persistence.dao.ConfigNodeDataDAO;
import com.getusroi.config.persistence.dao.FeatureDeploymentDAO;
import com.getusroi.featuredeployment.FeatureDeployment;
import com.getusroi.featuremaster.FeatureMaster;

public class ConfigPersistenceServiceMySqlImpl implements IConfigPersistenceService {
	final Logger logger = LoggerFactory.getLogger(ConfigPersistenceServiceMySqlImpl.class);
	private ConfigNodeDAO configNodeDAO;
	private ConfigNodeDataDAO configDataDao;
	private ITenantConfigTreeService tenantConfigTreeService;
	private ConfigFeatureMasterDAO configFeatureMasterDao;
	private FeatureDeploymentDAO featureDeploymentDAO;

	public ConfigPersistenceServiceMySqlImpl() {
		configNodeDAO = new ConfigNodeDAO();
		configDataDao = new ConfigNodeDataDAO();
		// change to singleton
		// tenantConfigTreeService=new TenantConfigTreeServiceImpl();
		tenantConfigTreeService = TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
		configFeatureMasterDao = new ConfigFeatureMasterDAO();
		featureDeploymentDAO = new FeatureDeploymentDAO();
	}

	public ConfigurationTreeNode getConfigPolicyNodeTree() throws ConfigPersistenceException {
		logger.debug(".getConfigPolicyNodeTree method");
		ConfigurationTreeNode configTreeNode = tenantConfigTreeService.getAllConfigTreeNode();
		if (configTreeNode == null) {
			logger.debug("Configuration tree Node is null");
			// DataGrid not initialized
			ConfigNodeDAO configNodeDAO = new ConfigNodeDAO();
			try {
				configTreeNode = configNodeDAO.getNodeTree();
				logger.debug("configTreeNode : " + configTreeNode);
				tenantConfigTreeService.initialize(configTreeNode);
			} catch (SQLException | IOException sqlexp) {
				throw new ConfigPersistenceException("Failed to get node tree", sqlexp);
			}

		}
		return configTreeNode;
	}

	public String getConfigPolicyNodeTreeAsJson() throws ConfigPersistenceException {
		ConfigurationTreeNode treeNode = getConfigPolicyNodeTree();

		StringBuffer jsonBuffer = new StringBuffer();
		treeNode.getConfigTreeNodeAsJSONString(jsonBuffer);
		return jsonBuffer.toString();
	}

	public int insertConfigNode(ConfigNode node) throws ConfigPersistenceException {
		try {
			int genNodeId = configNodeDAO.insertConfigNode(node);
			node.setNodeId(genNodeId);
			ConfigurationTreeNode configTreeNode = buildConfigTreeNode(node);
			// Add to DataGrid Representation of this Tree
			tenantConfigTreeService.addConfigurationTreeNode(configTreeNode);
			logger.debug("insertConfigNode()--Generated NodeId is=" + genNodeId);
			return genNodeId;
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert ConfigNode with ConfigNode=" + node, sqlexp);
		}

	}

	public int insertConfigNodeWithVersion(ConfigurationTreeNode node) throws ConfigPersistenceException {
		try {
			int genNodeId = configNodeDAO.insertConfigNodeWithVersion(node);
			node.setNodeId(genNodeId);

			// Add to DataGrid Representation of this Tree
			tenantConfigTreeService.addConfigurationTreeNode(node);
			logger.debug("insertConfigNode()--Generated NodeId is=" + genNodeId);
			return genNodeId;
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert ConfigNode with ConfigNode=" + node, sqlexp);
		}

	}

	@Override
	public int updateConfigNodeWithPrimaryFeatureId(int nodeId, int primaryFeatureId)
			throws ConfigPersistenceException {

		try {
			new ConfigNodeDAO().updateNodeWithPrimaryFeatureId(nodeId, primaryFeatureId);
		} catch (SQLException | IOException e) {
			throw new ConfigPersistenceException("Error in Updating primaryFeatureId in configNode with nodeId : "
					+ nodeId + " primaryFeatureId : " + primaryFeatureId, e);

		}
		return 0;
	}

	public List<ConfigNode> getChildNodes(Integer parentNodeId) throws ConfigPersistenceException {
		List<ConfigNode> childNodeList = null;
		try {
			configNodeDAO = new ConfigNodeDAO();
			childNodeList = configNodeDAO.getChildNodes(parentNodeId);
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to get Child Nodes parentNodeId=" + parentNodeId, sqlexp);
		}

		return childNodeList;
	}

	public ConfigNode getNodeById(Integer nodeId) throws ConfigPersistenceException {
		// configNodeDAO = new ConfigNodeDAO();
		ConfigNode configNode = null;
		try {
			configNode = configNodeDAO.getNodeById(nodeId);
		} catch (SQLException | IOException sqlexp) {

			throw new ConfigPersistenceException("Failed to get Node by Id=" + nodeId, sqlexp);
		}
		return configNode;
	}

	public boolean deleteNodeByNodeId(Integer nodeId) throws ConfigPersistenceException {
		// Delete the node Next
		try {
			ConfigNode configNode = configNodeDAO.getNodeById(nodeId);
			int numRows = configNodeDAO.deleteNodeByNodeId(nodeId);
			// Remove it from the cacahe
			ConfigurationTreeNode configTreeNode = buildConfigTreeNode(configNode);
			tenantConfigTreeService.deleteConfigurationTreeNode(configTreeNode);
			if (numRows > 0)
				return true;
		} catch (SQLException | IOException sqlexp) {

			throw new ConfigPersistenceException("Failed to Delete ConfigNode by Id=" + nodeId, sqlexp);
		}
		return false;
	}

	private ConfigurationTreeNode buildConfigTreeNode(ConfigNode node) {
		ConfigurationTreeNode nodeTree = new ConfigurationTreeNode();
		nodeTree.setNodeId(node.getNodeId());
		nodeTree.setLevel(node.getLevel());
		nodeTree.setNodeName(node.getNodeName());
		nodeTree.setParentNodeId(node.getParentNodeId());
		nodeTree.setType(node.getType());
		return nodeTree;
	}

	// -----------------------------TenantConfigTree---
	public ConfigurationTreeNode getConfigTreeNodeForFeatureGroup(String tenantName, String siteName,
			String featureGroup) throws ConfigPersistenceException {
		if (!tenantConfigTreeService.isInitialized()) {
			getConfigPolicyNodeTree();
		}
		ConfigurationTreeNode configtreeNode = tenantConfigTreeService.getConfigTreeNodeForFeatureGroup(tenantName,
				siteName, featureGroup);

		return configtreeNode;
	}

	public ConfigurationTreeNode getConfigTreeNodeForTenantById(Integer tenantId) throws ConfigPersistenceException {
		if (!tenantConfigTreeService.isInitialized()) {
			getConfigPolicyNodeTree();
		}
		ConfigurationTreeNode tenantConfigNodeTree = tenantConfigTreeService.getConfigTreeNodeForTenantById(tenantId);
		return tenantConfigNodeTree;
	}

	public Integer getApplicableNodeId(String tenantId, String siteId, String featureGroup, String featureName,
			String implName, String vendorName, String version)
			throws InvalidNodeTreeException, ConfigPersistenceException {
		if (!tenantConfigTreeService.isInitialized()) {
			logger.debug("config tree is not initialiazed");
			getConfigPolicyNodeTree();
		}
		Integer applicableNodeId = tenantConfigTreeService.getApplicableNodeId(tenantId, siteId, featureGroup,
				featureName, implName, vendorName, version);
		logger.debug("applicable node Id ->: " + applicableNodeId);
		return applicableNodeId;
	}

	@Override
	public Integer getApplicableNodeId(String tenantId, String siteId)
			throws InvalidNodeTreeException, ConfigPersistenceException {
		if (!tenantConfigTreeService.isInitialized()) {
			getConfigPolicyNodeTree();
		}
		Integer applicableNodeId = tenantConfigTreeService.getApplicableNodeId(tenantId, siteId);

		return applicableNodeId;
	}

	@Override
	public Integer getApplicableNodeId(String tenantId) throws InvalidNodeTreeException, ConfigPersistenceException {
		if (!tenantConfigTreeService.isInitialized()) {
			getConfigPolicyNodeTree();
		}
		Integer applicableNodeId = tenantConfigTreeService.getApplicableNodeId(tenantId);

		return applicableNodeId;
	}

	// ------Node Data calls------
	public List<ConfigNodeData> getConfigNodeDataByNodeId(Integer nodeId) throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		List<ConfigNodeData> nodeDataList = null;
		try {
			nodeDataList = configDataDao.getConfigNodeDataByNodeId(nodeId);
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to get Config Node data by  Node Id=" + nodeId, sqlexp);
		}
		return nodeDataList;
	}

	public ConfigNodeData getConfigNodeDatabyNameAndNodeId(Integer nodeId, String configName, String configType)
			throws ConfigPersistenceException {
		logger.debug("getConfigNodeDatabyNameAndNodeId mehtod");
		configDataDao = new ConfigNodeDataDAO();
		try {
			return configDataDao.getConfigNodeDatabyNameAndNodeId(nodeId, configName, configType);
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException(
					"Failed to get Config Node data by Name=" + configName + " and Node Id=" + nodeId, sqlexp);
		}
	}

	public boolean updateConfigdataInConfigNodeData(String xmlString, Integer nodeId, String configName,
			String configType) throws ConfigPersistenceException {
		boolean configdataSuccessUpdate;
		try {
			configDataDao.updateConfigDataByNameAndNodeId(xmlString, nodeId, configName, configType);
			configdataSuccessUpdate = true;
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to get Node by Id=" + nodeId, sqlexp);
		}

		return configdataSuccessUpdate;
	}

	public boolean deleteConfigNodeData(Integer configNodeDataId) throws ConfigPersistenceException {
		try {
			configDataDao = new ConfigNodeDataDAO();
			int numofRows = configDataDao.deleteConfigNodeData(configNodeDataId);
			if (numofRows > 0)
				return true;
			return false;
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException(
					"Failed to delete ConfigNodeData with configNodeDataId=" + configNodeDataId, sqlexp);
		}
	}

	public int deleteConfigNodeDataByNodeId(Integer nodeId) throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		int numofRows;
		try {
			numofRows = configDataDao.deleteConfigNodeDataByNodeId(nodeId);
			return numofRows;
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to delete ConfigNodeData with nodeId=" + nodeId, sqlexp);
		}

	}

	public int insertConfigNodeData(ConfigNodeData nodeData) throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		try {
			return configDataDao.insertConfigNodeData(nodeData);
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert ConfigNodeData with nodeId="
					+ nodeData.getParentConfigNodeId() + ",configName=" + nodeData.getConfigName(), sqlexp);
		}
	}

	public void enableConfigNodeData(boolean setEnable, Integer nodeDataId) throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		try {
			configDataDao.enableConfigNodeData(setEnable, nodeDataId);
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to enable/disable nodeDataId=" + nodeDataId, sqlexp);
		}
	}

	public List<ConfigNodeData> getConfigNodeDataByNodeIdAndByType(Integer nodeId, String type)
			throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		List<ConfigNodeData> nodeDataList = null;
		try {
			nodeDataList = configDataDao.getConfigNodeDataByNodeIdByConfigType(nodeId, type);
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to get Config Node data by  Node Id=" + nodeId, sqlexp);
		}
		return nodeDataList;
	}

	/**
	 * @param configName
	 * @param nodeId
	 * 
	 */
	public int deleteConfigNodeDataByNodeIdAndConfigName(String configName, int nodeId)
			throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		int numofRows = 0;

		try {
			numofRows = configDataDao.deleteConfigNodeDataByNodeIdAndByConfigName(configName, nodeId);
		} catch (SQLException | IOException sqlexp) {
			// TODO Auto-generated catch block
			throw new ConfigPersistenceException(
					"Failed to delete ConfigNodeData with nodeId=" + nodeId + " and configName=" + configName, sqlexp);
		}
		return numofRows;
	}

	@Override
	public int getNodeIdByNodeNameAndByTypeNotWithGivenNodeId(String nodeName, String type, int parentNodeId,
			int updatingNodeId) throws ConfigPersistenceException {
		int nodeId = 0;
		try {
			nodeId = configNodeDAO.getNodeIdByNodeNameAndByTypeNotNodeId(nodeName, type, parentNodeId, updatingNodeId);
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert ConfigNodeData with nodeName=" + nodeName + ",type="
					+ type + ", parentnode id= " + parentNodeId + ", updateNodeID= " + updatingNodeId, sqlexp);
		}

		return nodeId;
	}

	public int updateConfigNodeData(ConfigNodeData nodeData) throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		try {
			return configDataDao.updateConfigNodeData(nodeData);
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert ConfigNodeData with nodeId="
					+ nodeData.getParentConfigNodeId() + ",configName=" + nodeData.getConfigName(), sqlexp);
		}
	}

	/**
	 * getting of nodeId from DB by nodeName , nodeType and parentNodeId
	 * 
	 * @param nodeName
	 * @param type
	 * @param parentNodeId
	 * @return int
	 * @throws ConfigPersistenceException
	 * @throws SQLException
	 */
	public int getNodeIdByNodeNameAndByType(String nodeName, String type, int parentNodeId)
			throws ConfigPersistenceException {

		int nodeId = 0;
		try {
			nodeId = configNodeDAO.getNodeIdByNodeNameAndByType(nodeName, type, parentNodeId);
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert ConfigNodeData with nodeName=" + nodeName + ",type="
					+ type + ", parentnode id= " + parentNodeId, sqlexp);
		}
		return nodeId;
	}

	@Override
	public int getNodeIDByNameAndType(String nodename, String nodeType) throws ConfigPersistenceException {
		int nodeId = 0;
		try {
			nodeId = configNodeDAO.getNodeIdByNodeNameAndByType(nodename, nodeType);
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to get Node by Name=" + nodename + " Type = " + nodeType,
					sqlexp);
		}
		return nodeId;
	}

	public int getFeatureMasterIdByFeatureAndFeaturegroup(String featureName, String featureGroup, String version,
			int siteId) throws ConfigPersistenceException {

		int featureMasterNodeId = 0;
		try {
			featureMasterNodeId = configFeatureMasterDao.getFeatureMasterIdByFeatureAndFeaturegroup(featureName,
					featureGroup, version, siteId);
		} catch (SQLException | IOException sqlexp) {
			throw new ConfigPersistenceException("Failed to get featureMasterNodeId  by featureName=" + featureName
					+ " featureGroup = " + featureGroup, sqlexp);
		}
		return featureMasterNodeId;
	}

	@Override
	public boolean insertFeatureMasterDetails(FeatureMaster featureMaster) throws ConfigPersistenceException {
		try {
			configFeatureMasterDao.insertFeatureMasterDetails(featureMaster);
			return true;
		} catch (SQLException | IOException e) {
			throw new ConfigPersistenceException(
					"Failed to insert featureMaster Details for give featureMaster : " + featureMaster, e);

		}
	}

	@Override
	public boolean deleteFeatureMasterDetails(String featureName, int siteId) throws ConfigPersistenceException {
		try {
			configFeatureMasterDao.deleteFeatureMasterDetails(featureName, siteId);
			return true;
		} catch (SQLException | IOException e) {
			throw new ConfigPersistenceException("Failed to delete featureMaster Details From Db  : " + featureName, e);

		}

	}

	@Override
	public boolean insertFeatureDeploymentDetails(FeatureDeployment featureDeployment)
			throws ConfigPersistenceException {
		logger.debug(".insertFeatureDeploymentDetails method of ConfogPersistenceMysqlImpl");
		try {
			boolean isInserted = featureDeploymentDAO.insertFeatureDeploymentDetails(featureDeployment);
			return isInserted;
		} catch (SQLException | IOException e) {
			throw new ConfigPersistenceException(
					"Failed to insert FeatureDeployment Details for give featureDeployment : " + featureDeployment, e);

		}
	}

	@Override
	public FeatureDeployment getFeatureDeploymentDetails(int featureMasterId, String featureName, String implName,
			String vendorName, String version) throws ConfigPersistenceException {
		logger.debug(".getFeatureDeploymentDetails method of ConfogPersistenceMysqlImpl");
		FeatureDeployment featureDeployment;
		try {
			featureDeployment = featureDeploymentDAO.getFeatureDeploymentByFeatureAndImplName(featureMasterId,
					featureName, implName, vendorName, version);
			return featureDeployment;
		} catch (SQLException | IOException e) {
			throw new ConfigPersistenceException("Failed to get FeatureDeployment Details for give featureName : "
					+ featureName + ", implementaion name : " + implName + ", vendor name : " + vendorName
					+ ", version : " + version, e);

		}
	}

	@Override
	public boolean updateFeatureDeployment(int featureMasterId, String featureName, String implName, String vendorName,
			String version, boolean isPrimary, boolean isActive) throws ConfigPersistenceException {
		logger.debug(".updateFeatureDeployment method of ConfogPersistenceMysqlImpl");
		boolean isUpdated = false;
		try {
			isUpdated = featureDeploymentDAO.updateFeatureDeployment(featureMasterId, featureName, implName, vendorName,
					version, isPrimary, isActive);
		} catch (SQLException | IOException e) {
			throw new ConfigPersistenceException("Failed to update FeatureDeployment Details for give featureName : "
					+ featureName + ", implementaion name : " + implName + ", vendor name : " + vendorName
					+ ", version : " + version + ", and isPrimary : " + isPrimary, e);

		}
		return isUpdated;
	}

	@Override
	public boolean deleteFeatureDeployment(int featureMasterId, String featureName, String implName, String vendorName,
			String version) throws ConfigPersistenceException {
		logger.debug(".deleteFeatureDeployment method of ConfogPersistenceMysqlImpl");
		try {
			boolean isDeleted = featureDeploymentDAO.deleteFeatureDeployment(featureMasterId, featureName, implName,
					vendorName, version);
			return isDeleted;
		} catch (SQLException | IOException e) {
			throw new ConfigPersistenceException("Failed to get FeatureDeployment Details for give featureName : "
					+ featureName + ", implementaion name : " + implName + ", vendor name : " + vendorName
					+ ", version : " + version, e);

		}
	}
	@Override
	public List<Map<String, Object>> getRequestContextList(String feature) throws ConfigPersistenceException {
		logger.debug(".getRequestContextList method of ConfogPersistenceMysqlImpl");
		try {
			return configDataDao.getRequestContextList(feature);
		} catch (SQLException | IOException e) {
			throw new ConfigPersistenceException(
					"Failed to get the List of RequestContext objects from ConfigNode Table ", e);
		}

	}

}
