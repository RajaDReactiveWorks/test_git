package com.getusroi.config.persistence.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.ConfigurationTreeNode;
import com.getusroi.config.persistence.ITenantConfigTreeService;
import com.getusroi.config.persistence.InvalidNodeTreeException;
import com.getusroi.config.persistence.UndefinedPrimaryVendorForFeature;
import com.getusroi.core.datagrid.ConfigurationTreeNodeListener;
import com.getusroi.core.datagrid.DataGridService;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class TenantConfigTreeServiceImpl extends ConfigurationTreeNodeListener implements ITenantConfigTreeService  {
	final Logger logger = LoggerFactory.getLogger(ITenantConfigTreeService.class);
	// flag that checks if the dataGrid is loaded/initialized with the
	// ConfigurationTreeNode
	private static boolean isDGInitialized = false;
	private  Map<String,Serializable> localTreeConfigmap=new HashMap<>();
	//for performance reason we are going like this
	private  ConfigurationTreeNode configTreeNode=null;
	
	private static TenantConfigTreeServiceImpl tenantConfigTreeServiceImpl;
	//creating a singleton instance of TenantConfigTreeServiceImpl
	private TenantConfigTreeServiceImpl(){
		
	}
	
	/**
	 * Singleton to ensure only one TenantConfigTreeServiceImpl Exist
	 * @return TenantConfigTreeServiceImpl object
	 */
	public static synchronized TenantConfigTreeServiceImpl getTenantConfigTreeServiceImpl(){
		if(tenantConfigTreeServiceImpl==null){			
			DataGridService dataGridService=DataGridService.getDataGridInstance();			
			tenantConfigTreeServiceImpl=new TenantConfigTreeServiceImpl();			
			dataGridService.addConfigListener("GlobalConfigProp",tenantConfigTreeServiceImpl);			
		}
		return tenantConfigTreeServiceImpl;
	}
	
	public void initialize(ConfigurationTreeNode treeNode) {
		logger.debug(".initialize() method of TenantConfigTreeServiceImpl");
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalConfigDataGridKey());
		synchronized (TenantConfigTreeServiceImpl.class) {
			map.put(getAllTenantConfigTreeDataGridKey(), treeNode);	
			configTreeNode=treeNode;
			localTreeConfigmap=map;
			isDGInitialized = true;
		}

	}

	public boolean isInitialized() {
		return isDGInitialized;
	}
	
	

	/**
	 * 
	 */
	public ConfigurationTreeNode getAllConfigTreeNode() {
		logger.debug(".getALLCOnfiTreeNode method of TenantConfigTreeServiceImpl");		
		/*HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		//logger.error("before getting global map from hazelcast : "+System.currentTimeMillis());
		IMap map = hazelcastInstance.getMap(getGlobalConfigDataGridKey());*/
		//logger.error("after getting global map from hazelcast : "+System.currentTimeMillis());
		/*ConfigurationTreeNode treeNode = null;
		if (localTreeConfigmap == null || localTreeConfigmap.isEmpty() || !localTreeConfigmap.containsKey(getAllTenantConfigTreeDataGridKey())) {
			return null;
		} else {
			logger.debug(".getAllConfigTreeNode() CacheHit returning from the Cache");
			//logger.debug("before configuratin tree global map from hazelcast : "+System.currentTimeMillis());
			configTreeNode = (ConfigurationTreeNode) localTreeConfigmap.get(getAllTenantConfigTreeDataGridKey());
			logger.debug("confi tree node : "+configTreeNode);
		//	logger.debug("after configuation tree global map from hazelcast : "+System.currentTimeMillis());

		}*/
		//logger.error("getAllConfigTreeNode exit : "+System.currentTimeMillis());
		return configTreeNode;
	}

	/**
 *   	
 */
	public ConfigurationTreeNode getConfigTreeNodeForTenantByName(String tenantName) {
		List<ConfigurationTreeNode> childNodes = configTreeNode.getChildNodes();
		for (ConfigurationTreeNode configTreeNode : childNodes) {
			if (configTreeNode.getNodeName().equalsIgnoreCase(tenantName))
				return configTreeNode;
		}

		return null;
	}

	public ConfigurationTreeNode getConfigTreeNodeForTenantById(Integer tenantId) {
		logger.debug("getConfigTreeNodeForTenantById( tenantId=" + tenantId + ") treeNode=" + configTreeNode);
		List<ConfigurationTreeNode> childNodes = configTreeNode.getChildNodes();
		for (ConfigurationTreeNode configTreeNode : childNodes) {
			if (configTreeNode.getNodeId().intValue() == tenantId.intValue())
				return configTreeNode;
		}

		return null;
	}

	public String getConfigTreeNodeAsJson() {		
		StringBuffer jsonBuffer = new StringBuffer();
		configTreeNode.getConfigTreeNodeAsJSONString(jsonBuffer);
		return jsonBuffer.toString();
	}
	
	
	
	/**
	 * This method is used to get tree node structure till feature
	 * @param tenantName
	 * @param siteName
	 * @param featureGroup
	 * @param feature
	 * @return
	 */
	public ConfigurationTreeNode getConfigTreeNodeForFeature(String tenantName, String siteName, String featureGroup,String feature) {		
		logger.debug(".getConfigTreeNodeForFeature method of TenantConfigTreeServiceImpl");		
		List<ConfigurationTreeNode> tenantNodes = configTreeNode.getChildNodes();
		for (ConfigurationTreeNode configTenantTreeNode : tenantNodes) {
			if (configTenantTreeNode.getNodeName().equalsIgnoreCase(tenantName)) {
				List<ConfigurationTreeNode> siteNodes = configTenantTreeNode.getChildNodes();
				for (ConfigurationTreeNode configSiteTreeNode : siteNodes) {
					if (configSiteTreeNode.getNodeName().equalsIgnoreCase(siteName)) {
						List<ConfigurationTreeNode> featureGroupTreeNodeList = configSiteTreeNode.getChildNodes();
						for (ConfigurationTreeNode featureGroupTreeNode : featureGroupTreeNodeList) {
							if (featureGroupTreeNode.getNodeName().equalsIgnoreCase(featureGroup)) {
								//return featureGroupTreeNode;
								List<ConfigurationTreeNode> featureTreeNodeList = featureGroupTreeNode.getChildNodes();
								for (ConfigurationTreeNode featureTreeNode : featureTreeNodeList) {
									if (featureTreeNode.getNodeName().equalsIgnoreCase(feature)) {
										return featureTreeNode;
									}
								}
							}
						}
					}
				}
			}

		}
		return null;
	}
	
	/**
	 * This method is used to get tree node structure till feature
	 * @param tenantName
	 * @param siteName
	 * @param featureGroup
	 * @param feature
	 * @return
	 * @throws UndefinedPrimaryVendorForFeature 
	 */
	public ConfigurationTreeNode getPrimaryVendorForFeature(String tenantName, String siteName, String featureGroup,String feature) throws UndefinedPrimaryVendorForFeature {
		logger.debug(".getVendorTreeNodesForFeature method of TenantConfigTreeServiceImpl");
		ConfigurationTreeNode featureConfigTreeNode=getConfigTreeNodeForFeature(tenantName,siteName,featureGroup,feature);
		int primaryKeyNodeId=featureConfigTreeNode.getPrimaryFeatureId();
		logger.debug("primary feature key : "+primaryKeyNodeId);
		List<ConfigurationTreeNode> vendorConfigTreeNodeList=featureConfigTreeNode.getChildNodes();
		if(vendorConfigTreeNodeList != null && !(vendorConfigTreeNodeList.isEmpty())){
			//if feature has only one vendor node then return the vendor
			if(vendorConfigTreeNodeList.size()==1){
				for(ConfigurationTreeNode vendorConfigTreeNode:vendorConfigTreeNodeList){
					return vendorConfigTreeNode;
				}
			}
		}//end of outter if
		for(ConfigurationTreeNode vendorConfigTreeNode:vendorConfigTreeNodeList){
			if(primaryKeyNodeId==vendorConfigTreeNode.getNodeId()){
				return vendorConfigTreeNode;
			}
		}
		throw new UndefinedPrimaryVendorForFeature("No primary vendor is defined for feature : "+feature+", under feature group : "+featureGroup+", site : "+siteName+", tenant : "+tenantName);
	}
	
	public ConfigurationTreeNode getConfigTreeNodeForFeatureGroup(String tenantName, String siteName, String featureGroup) {
		logger.debug(".getConfigTreeNodeForFeatureGroup method of TenantConfigTreeServiceImpl");
		List<ConfigurationTreeNode> tenantNodes = configTreeNode.getChildNodes();
		for (ConfigurationTreeNode configTenantTreeNode : tenantNodes) {
			if (configTenantTreeNode.getNodeName().equalsIgnoreCase(tenantName)) {
				List<ConfigurationTreeNode> siteNodes = configTenantTreeNode.getChildNodes();
				for (ConfigurationTreeNode configSiteTreeNode : siteNodes) {
					if (configSiteTreeNode.getNodeName().equalsIgnoreCase(siteName)) {
						List<ConfigurationTreeNode> featureGroupTreeNodeList = configSiteTreeNode.getChildNodes();
						for (ConfigurationTreeNode featureGroupTreeNode : featureGroupTreeNodeList) {
							if (featureGroupTreeNode.getNodeName().equalsIgnoreCase(featureGroup)) {
								return featureGroupTreeNode;
							}
						}
					}
				}
			}

		}
		return null;
	}

	public ConfigurationTreeNode getConfigTreeNodeForSite(String tenantName, String siteName) {		
		List<ConfigurationTreeNode> tenantNodes = configTreeNode.getChildNodes();
		for (ConfigurationTreeNode configTenantTreeNode : tenantNodes) {
			if (configTenantTreeNode.getNodeName().equals(tenantName)) {
				List<ConfigurationTreeNode> siteNodes = configTenantTreeNode.getChildNodes();
				for (ConfigurationTreeNode configSiteTreeNode : siteNodes) {
					if (configSiteTreeNode.getNodeName().equals(siteName)) {
						return configSiteTreeNode;
					}
				}
			}

		}
		return null;
	}
	
	public ConfigurationTreeNode getConfigTreeNodeForTenant(String tenantName) {		
		List<ConfigurationTreeNode> tenantNodes = configTreeNode.getChildNodes();
		for (ConfigurationTreeNode configTenantTreeNode : tenantNodes) {
			if (configTenantTreeNode.getNodeName().equalsIgnoreCase(tenantName)) {
				return configTenantTreeNode;
			}

		}
		return null;
	}

	public void addConfigurationTreeNode(ConfigurationTreeNode configNode) {
		logger.debug(".addConfigurationTreeNode of TenantConfigTreeServiceImpl");
		// if datagrid not intilized , intilize the data Grid
		if (configTreeNode == null) {
			initialize(configNode);
			/*treeNode = getAllConfigTreeNode();
			configTreeNode=treeNode;
*/
		}
		configTreeNode.addChildren(configNode);
		updateDataGridforChange(configTreeNode);
	}

	public void deleteConfigurationTreeNode(ConfigurationTreeNode configNode) {
		logger.debug(".deleteConfigurationTreeNode of TenantConfigTreeServiceImpl");
		
		configTreeNode.deleteChildren(configNode);
		updateDataGridforChange(configTreeNode);
	}

	
	public Integer getApplicableNodeId(String tenantId, String siteId, String featureGroup, String featureName,
			String implName, String vendorName, String version)
			throws InvalidNodeTreeException, ConfigPersistenceException {
		logger.debug(
				"Finding ParentNodeId for Tenant=" + tenantId + "-siteId=" + siteId + "-featureGroup=" + featureGroup
						+ "-featureName=" + featureName +", impl name : "+implName+", vendor name : " + vendorName + ", version : " + version);
		ConfigurationTreeNode fgconfigNodeTree = getConfigTreeNodeForFeatureGroup(tenantId, siteId, featureGroup);
		logger.info("fgconfigNodeTree :"+fgconfigNodeTree);
		if (fgconfigNodeTree != null && fgconfigNodeTree.getNodeName().equalsIgnoreCase(featureGroup)) {
			if (featureName == null) {
				// Config is mapped to Feature Group return Feature Group NodeId
				return fgconfigNodeTree.getNodeId();
			} else {
				// ConfigIs Mapped to a Feature
				ConfigurationTreeNode fconfigNodeTree = getConfigTreeNodeForFeature(fgconfigNodeTree, featureName);
				if (fconfigNodeTree != null && fconfigNodeTree.getNodeName().equalsIgnoreCase(featureName)) {
					if (implName == null) {
						return fconfigNodeTree.getNodeId();
					} else {
						// ConfigIs Mapped to a Feature
						ConfigurationTreeNode implconfigNodeTree = getConfigTreeNodeForImplementation(fconfigNodeTree,
								implName);
						if (implconfigNodeTree != null && implconfigNodeTree.getNodeName().equalsIgnoreCase(implName)) {
							if (vendorName == null) {
								return implconfigNodeTree.getNodeId();
							} else {
								List<ConfigurationTreeNode> featureTreeNodeList = implconfigNodeTree.getChildNodes();
								Integer nodeId = getVendorTreeNodeId(featureTreeNodeList, vendorName, version);
								// we are getting the node if for specific
								// tenant,site,featuregroup,feature,vendor,version(optional)
								if (nodeId != null && nodeId != 0) {
									return nodeId;
								} // end of if(nodeId !=null && nodeId !=0)

							}
						} // end of if,which return node id till implementation
							// level
					}

				} // end of if,which return node id till feature level
				else{
					return 0;
				}

			}
		} // end of if,which return node id till feature group level
		throw new InvalidNodeTreeException("Not able to find NodeTree in DB for Tree=" + tenantId + "/" + siteId + "/"
				+ featureGroup + "/" + featureName + "/" + implName + "/" + vendorName + "/" + version);
	}

	/**
	 * This method is used to get tree node structure till feature
	 * @param tenantName
	 * @param siteName
	 * @param featureGroup
	 * @param feature
	 * @return
	 */
	public ConfigurationTreeNode getConfigTreeNodeForFeature(ConfigurationTreeNode fgconfigNodeTree,String feature) {
		logger.debug(".getConfigTreeNodeForFeature method of TenantConfigTreeServiceImpl");
		List<ConfigurationTreeNode> featureChildConfigList=fgconfigNodeTree.getChildNodes();
		for(ConfigurationTreeNode featureTreeNode:featureChildConfigList){
			if(featureTreeNode.getNodeName().trim().equalsIgnoreCase(feature)){
				return featureTreeNode;
			}
		}
		return null;
	}
	
	/**
	 * This method is used to get tree node structure till Implementation
	 * @param tenantName
	 * @param siteName
	 * @param featureGroup
	 * @param feature
	 * @return
	 */
	public ConfigurationTreeNode getConfigTreeNodeForImplementation(ConfigurationTreeNode fconfigNodeTree,String implName) {
		logger.debug(".getConfigTreeNodeForImplementation method of TenantConfigTreeServiceImpl "+implName);
		List<ConfigurationTreeNode> featureChildConfigList=fconfigNodeTree.getChildNodes();
		for(ConfigurationTreeNode featureTreeNode:featureChildConfigList){
			if(featureTreeNode.getNodeName().trim().equalsIgnoreCase(implName.trim())){
				logger.debug("found implementation in config tree node : "+featureTreeNode);
				return featureTreeNode;
			}
		}
		return null;
	}
	
	/**
	 * This method is used to get the nodeId for of vendor for specific feature
	 * @param featureTreeNodeList : List of ConfigurationTreeNode
	 * @param vendorName : vendor name in String
	 * @param version : version in String (optional)
	 * @return Integer : Node Id of vendor 
	 */
	private Integer getVendorTreeNodeId(List<ConfigurationTreeNode> featureTreeNodeList,String vendorName,String version){
		logger.debug(".getVendorTreeNodeId method of TenantConfigTreeServiceImpl");
		float latestVersion=0;
		Integer nodeId=0;
		for (ConfigurationTreeNode vendorTreeNode : featureTreeNodeList) {
			if (vendorName.equalsIgnoreCase(vendorTreeNode.getNodeName().trim())) {
				//check if version is null/empty string, if yes then return the latest version data stored db
				if(vendorTreeNode.getVersion().trim().equalsIgnoreCase(version)){
					logger.debug("vendor name is specified");
					return vendorTreeNode.getNodeId();
				}else{
					logger.debug("vendor name is not specified");
					//convert version  into float
					float vendorVersionFromdb=Float.parseFloat(vendorTreeNode.getVersion().trim());
					//trying to get node id of vendor with latest version
					if(vendorVersionFromdb >=latestVersion){
						latestVersion=vendorVersionFromdb;
						nodeId=vendorTreeNode.getNodeId();
						logger.debug("latest version : "+latestVersion+", nodeId : "+nodeId);
					}
				//return vendorTreeNode.getNodeId();
				}
			}
		}
		return nodeId;
	}//end of method

	public Integer getApplicableNodeId(String tenantId, String siteId) throws InvalidNodeTreeException, ConfigPersistenceException {
		logger.debug("Finding ParentNodeId for Tenant=" + tenantId + "-siteId=" + siteId);
		ConfigurationTreeNode siteConfigNodeTree = getConfigTreeNodeForSite(tenantId, siteId);
		if (siteConfigNodeTree != null && siteConfigNodeTree.getNodeName().equalsIgnoreCase(siteId)) {
			return siteConfigNodeTree.getNodeId();
		}
		return null;
	}
	
	public Integer getApplicableNodeId(String tenantId) throws InvalidNodeTreeException, ConfigPersistenceException {
		logger.debug("Finding ParentNodeId for Tenant=" + tenantId);
		ConfigurationTreeNode tenantConfigNodeTree = getConfigTreeNodeForTenant(tenantId);
		if (tenantConfigNodeTree != null && tenantConfigNodeTree.getNodeName().equalsIgnoreCase(tenantId)) {
			return tenantConfigNodeTree.getNodeId();
		}
		return null;
	}
	
	@Override
	public void configNodeTreeAdded(EntryEvent<String, Serializable> addEvent) {
		logger.debug(".configNodeTreeAdded method of TenantConfigTreeServiceImpl");
		ConfigurationTreeNode configNode = (ConfigurationTreeNode)addEvent.getValue();
		logger.debug("configNode : "+configNode);
		// if datagrid not intilized , intilize the data Grid
		if (configTreeNode == null) {
			initialize(configNode);			

		}
		logger.debug("treeNode : "+configTreeNode);
		if(configTreeNode!=null){
		boolean bool=configTreeNode.addChildren(configNode);
		logger.debug("bool : "+bool);
		updateDataGridforChange(configTreeNode);
		}
		
	}

	@Override
	public void configNodeTreeUpdated(EntryEvent<String, Serializable> updateEvent) {
		logger.debug(".configNodeTreeUpdated method of TenantConfigTreeServiceImpl");
		configTreeNode = (ConfigurationTreeNode)updateEvent.getValue();
		logger.debug("config tree node update: "+configTreeNode);

	}

	@Override
	public void configNodeTreeRemoved(EntryEvent<String, Serializable> removedEvent) {
		logger.debug(".configNodeTreeRemoved method of TenantConfigTreeServiceImpl");
		ConfigurationTreeNode configNode = (ConfigurationTreeNode)removedEvent.getValue();
		configTreeNode.deleteChildren(configNode);
		updateDataGridforChange(configTreeNode);

	}

	private void updateDataGridforChange(ConfigurationTreeNode configNode) {
		logger.debug(".updateDataGridforChange method of TenantConfigTreeServiceImpl");
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap map = hazelcastInstance.getMap(getGlobalConfigDataGridKey());
		map.put(getAllTenantConfigTreeDataGridKey(), configNode);
		configTreeNode=configNode;
	}

	private String getGlobalConfigDataGridKey() {
		return "GlobalConfigProp";
	}

	private String getAllTenantConfigTreeDataGridKey() {
		return "AllTenantConfigTree";
	}

	

}
