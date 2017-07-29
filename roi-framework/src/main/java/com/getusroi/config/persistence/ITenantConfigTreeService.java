package com.getusroi.config.persistence;


/**
 * 
 * @author bizruntime
 *
 */
public interface ITenantConfigTreeService {
	public ConfigurationTreeNode getAllConfigTreeNode();
	public ConfigurationTreeNode getConfigTreeNodeForTenantByName(String tenantName);
	public ConfigurationTreeNode getConfigTreeNodeForTenantById(Integer tenantNodeId);
	public  ConfigurationTreeNode getConfigTreeNodeForFeature(String tenantName,String siteName,String featureGroup,String feature);
	public ConfigurationTreeNode getPrimaryVendorForFeature(String tenantName, String siteName, String featureGroup,String feature) throws UndefinedPrimaryVendorForFeature;
	public  ConfigurationTreeNode getConfigTreeNodeForFeatureGroup(String tenantName,String siteName,String featureGroup);
	public String getConfigTreeNodeAsJson();
	public void addConfigurationTreeNode(ConfigurationTreeNode configNode);
	public void deleteConfigurationTreeNode(ConfigurationTreeNode configNode);
	public void initialize(ConfigurationTreeNode treeNode);
	public boolean isInitialized();
	public Integer getApplicableNodeId(String tenantId, String siteId, String featureGroup, String featureName,String implName,String vendorName,String version)throws InvalidNodeTreeException, ConfigPersistenceException;
	public Integer getApplicableNodeId(String tenantId, String siteId)throws InvalidNodeTreeException, ConfigPersistenceException;
	public Integer getApplicableNodeId(String tenantId) throws InvalidNodeTreeException, ConfigPersistenceException;

}
