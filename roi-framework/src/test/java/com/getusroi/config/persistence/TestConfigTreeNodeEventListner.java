package com.getusroi.config.persistence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.getusroi.config.persistence.impl.TenantConfigTreeServiceImpl;
import com.getusroi.core.datagrid.DataGridService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class TestConfigTreeNodeEventListner {
	final Logger logger = LoggerFactory.getLogger(TestConfigTreeNodeEventListner.class);
	IConfigPersistenceService perService;
	
	//@Before
	public void initializeTheDataGrid() throws ConfigPersistenceException{
		perService=new ConfigPersistenceServiceMySqlImpl();
		perService.getConfigPolicyNodeTree();
	}
	
	//@Test
	public void testAddConfigNodeTreeListner(){
		logger.debug(".testAddConfigNodeTreeListner method of TestConfigTreeNodeEventListner");
		HazelcastInstance hazelcastInstance=DataGridService.getDataGridInstance().getHazelcastInstance();
		ITenantConfigTreeService tenantTreeService=TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
		ConfigurationTreeNode configNodeTree=tenantTreeService.getAllConfigTreeNode();
		IMap map=hazelcastInstance.getMap("GlobalConfigProp");
		ConfigurationTreeNode treeNode=null;
		if(map==null || map.isEmpty()|| !map.containsKey("AllTenantConfigTree")){
			Assert.fail("ConfigurationTreeNode not updated in Cache failure");
		}else{
			treeNode=(ConfigurationTreeNode)map.get("AllTenantConfigTree");
			Assert.assertEquals("Cached ConfigTreeNode should have Root as FirstElement","Root", configNodeTree.getNodeName());
			
			//add new confignode tree
			ConfigurationTreeNode configTreeNode=new ConfigurationTreeNode();
			configNodeTree.setNodeId(123);
			configNodeTree.setNodeName("Root");
			configNodeTree.setLevel(0);
			configNodeTree.setType("Root");
			map.put("NewTreeConfigTree", configTreeNode);
		}
	}
	
	//@Test
	public void testUpdateConfigNodeTreeListner(){
		logger.debug(".testAddConfigNodeTreeListner method of TestConfigTreeNodeEventListner");
		HazelcastInstance hazelcastInstance=DataGridService.getDataGridInstance().getHazelcastInstance();
		ITenantConfigTreeService tenantTreeService=TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
		ConfigurationTreeNode configNodeTree=tenantTreeService.getAllConfigTreeNode();
		IMap map=hazelcastInstance.getMap("GlobalConfigProp");
		ConfigurationTreeNode treeNode=null;
		if(map==null || map.isEmpty()|| !map.containsKey("AllTenantConfigTree")){
			Assert.fail("ConfigurationTreeNode not updated in Cache failure");
		}else{
			treeNode=(ConfigurationTreeNode)map.get("AllTenantConfigTree");
			Assert.assertEquals("Cached ConfigTreeNode should have Root as FirstElement","Root", configNodeTree.getNodeName());
			
			//add new confignode tree
			ConfigurationTreeNode configTreeNode=new ConfigurationTreeNode();
			configNodeTree.setNodeId(123);
			configNodeTree.setNodeName("Root");
			configNodeTree.setLevel(0);
			configNodeTree.setType("Root");
			map.put("AllTenantConfigTree", configTreeNode);
		}
	}//end of method
	
	//@Test
	public void testDeleteConfigNodeTreeListner(){
		logger.debug(".testAddConfigNodeTreeListner method of TestConfigTreeNodeEventListner");
		HazelcastInstance hazelcastInstance=DataGridService.getDataGridInstance().getHazelcastInstance();
		ITenantConfigTreeService tenantTreeService=TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
		ConfigurationTreeNode configNodeTree=tenantTreeService.getAllConfigTreeNode();
		IMap map=hazelcastInstance.getMap("GlobalConfigProp");
		ConfigurationTreeNode treeNode=null;
		if(map==null || map.isEmpty()|| !map.containsKey("AllTenantConfigTree")){
			Assert.fail("ConfigurationTreeNode not updated in Cache failure");
		}else{
			treeNode=(ConfigurationTreeNode)map.get("AllTenantConfigTree");
			Assert.assertEquals("Cached ConfigTreeNode should have Root as FirstElement","Root", configNodeTree.getNodeName());
			
			//add new confignode tree
			ConfigurationTreeNode configTreeNode=new ConfigurationTreeNode();
			configNodeTree.setNodeId(123);
			configNodeTree.setNodeName("Root");
			configNodeTree.setLevel(0);
			configNodeTree.setType("Root");
			map.put("NewTreeConfigTree", configTreeNode);
			
			logger.debug("removing");
			map.remove("NewTreeConfigTree");
		}
	}//end of method
}
