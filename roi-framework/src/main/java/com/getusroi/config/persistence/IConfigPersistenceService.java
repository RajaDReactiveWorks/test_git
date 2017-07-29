package com.getusroi.config.persistence;

import java.util.List;
import java.util.Map;

import com.getusroi.featuredeployment.FeatureDeployment;
import com.getusroi.featuremaster.FeatureMaster;

public interface IConfigPersistenceService {
	public ConfigurationTreeNode getConfigPolicyNodeTree()throws ConfigPersistenceException ;
	public String getConfigPolicyNodeTreeAsJson()throws ConfigPersistenceException ;
	public int insertConfigNode(ConfigNode node)throws ConfigPersistenceException ;
	public int insertConfigNodeWithVersion(ConfigurationTreeNode node) throws ConfigPersistenceException ;
	public int updateConfigNodeWithPrimaryFeatureId(int nodeId, int primaryFeatureId) throws ConfigPersistenceException ;
	
	public List<ConfigNode> getChildNodes(Integer parentNodeId)throws ConfigPersistenceException ;
	public ConfigNode getNodeById(Integer nodeId)throws ConfigPersistenceException ;
	public boolean deleteNodeByNodeId(Integer nodeId)throws ConfigPersistenceException ;
	
	public int getNodeIdByNodeNameAndByType(String nodeName, String type,int parentNodeId) throws ConfigPersistenceException;

	
	public int getNodeIdByNodeNameAndByTypeNotWithGivenNodeId(String nodeName,String type, int parentNodeId, int updatingNodeId)throws ConfigPersistenceException;
	//TenantTreeService Related
	public  ConfigurationTreeNode getConfigTreeNodeForFeatureGroup(String tenantName,String siteName,String featureGroup) throws ConfigPersistenceException;
	public ConfigurationTreeNode getConfigTreeNodeForTenantById(Integer tenantId) throws ConfigPersistenceException;
	
	/** Based on Tenant,Site,FeatureGroup,Feature finds the applicable NodeId
	 * to Tag any Configuration <BR>
	 * Note :- 1.) Does not support tagging PermaStore above Feature Group.<br>
	 * 2.) Does not support Tagging of Configuration to Vendor with in a Feature.
	 */
	public Integer getApplicableNodeId(String tenantId, String siteId, String featureGroup, String featureName,String implName,String vendorName,String version)throws InvalidNodeTreeException, ConfigPersistenceException;
	public Integer getApplicableNodeId(String tenantId, String siteId)throws InvalidNodeTreeException, ConfigPersistenceException;
	public Integer getApplicableNodeId(String tenantId) throws InvalidNodeTreeException, ConfigPersistenceException;
	
	//ConfigPolicy Data /NodeData
	public List<ConfigNodeData> getConfigNodeDataByNodeId(Integer nodeId) throws ConfigPersistenceException;
	public int insertConfigNodeData(ConfigNodeData nodeData)throws ConfigPersistenceException;
	public ConfigNodeData getConfigNodeDatabyNameAndNodeId(Integer nodeId,String configName,String configType) throws ConfigPersistenceException;
	public int getNodeIDByNameAndType(String nodename,String nodeType) throws ConfigPersistenceException;
	public boolean updateConfigdataInConfigNodeData(String xmlString,Integer nodeId,String configName,String configType) throws ConfigPersistenceException;
	public boolean deleteConfigNodeData(Integer configNodeDataId)throws ConfigPersistenceException;
	public int deleteConfigNodeDataByNodeId(Integer nodeId) throws ConfigPersistenceException;
	public void enableConfigNodeData(boolean setEnable,Integer nodeDataId) throws ConfigPersistenceException;
	
	public int deleteConfigNodeDataByNodeIdAndConfigName(String configName,
			int nodeId) throws ConfigPersistenceException;
	
	public List<ConfigNodeData> getConfigNodeDataByNodeIdAndByType(
			Integer nodeId, String type) throws ConfigPersistenceException ;
	public int updateConfigNodeData(ConfigNodeData nodeData)
			throws ConfigPersistenceException;
	
	/**
	 * 	Featuremaster DB  Oprations 
	 */
	public int getFeatureMasterIdByFeatureAndFeaturegroup(String featureName,String featureGroup,String version,int siteId) throws ConfigPersistenceException;
	public boolean insertFeatureMasterDetails(FeatureMaster featureMaster)throws ConfigPersistenceException;
	public boolean deleteFeatureMasterDetails(String featureName,int siteId)throws ConfigPersistenceException;
	public boolean insertFeatureDeploymentDetails(FeatureDeployment featureDeployment) throws ConfigPersistenceException;
	public FeatureDeployment getFeatureDeploymentDetails(int featureMasterId,String featureName, String implName, String vendorName,
			String version) throws ConfigPersistenceException;
	public boolean updateFeatureDeployment(int featureMasterId,String featureName,String implName,String vendorName,String version,boolean isPrimary,boolean isActive) throws ConfigPersistenceException;
	public boolean deleteFeatureDeployment(int featureMasterId,String featureName, String implName, String vendorName, String version)
			throws ConfigPersistenceException;
	/**
	 * this method is used to get the list of RequestContext object
	 * @return
	 * @throws ConfigPersistenceException
	 */
	public List<Map<String, Object>> getRequestContextList(String feature) throws ConfigPersistenceException;
	
}
