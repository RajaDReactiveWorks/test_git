package com.getusroi.config.persistence;



import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.ConfigurationTreeNode;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.ITenantConfigTreeService;
import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.getusroi.config.persistence.impl.TenantConfigTreeServiceImpl;
import com.getusroi.core.datagrid.DataGridService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class TenantConfigTreeServiceTest {
	final Logger logger = LoggerFactory.getLogger(TenantConfigTreeServiceTest.class);
	
	@Before
	public void initializeTheDataGrid() throws ConfigPersistenceException{
		IConfigPersistenceService perService=new ConfigPersistenceServiceMySqlImpl();
		perService.getConfigPolicyNodeTree();
	}
	
	@Test
	public void testgetAllConfigTreeNodeSucess(){
		ITenantConfigTreeService tenantTreeService=TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
		ConfigurationTreeNode configNodeTree=tenantTreeService.getAllConfigTreeNode();
		logger.debug(configNodeTree.toString());
		Assert.assertNotNull("ConfigurationTreeNode should not be Null",configNodeTree);
		Assert.assertEquals("The First Node in Node Tree should be Root","Root", configNodeTree.getNodeName());
	}
	
	@Test
	public void testgetAllConfigTreeNodeCaching(){
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
		}
		
	}
	
	@Test
	public void testAllTreeNodeAsJson() throws JSONException{
		ITenantConfigTreeService tenantTreeService=TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();;
		String jsonTreeNodeStr=tenantTreeService.getConfigTreeNodeAsJson();
		logger.debug("jsonTreeNodeStr : "+jsonTreeNodeStr);
		Assert.assertNotNull("ConfigTreeNode as JSON String should not be null",jsonTreeNodeStr);
		JSONParser parser = new JSONParser();
	    try{
		    parser.parse(jsonTreeNodeStr);
		  
		  }
		  catch(ParseException pe){
			  Assert.fail("ConfigTreeNode as JSON String failed to Parse as Json");
		  }
	}

	@Test
	public void testgetConfigNodeTreeForFeatureGroup(){
		ITenantConfigTreeService tenantTreeService=TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();;
		ConfigurationTreeNode fgconfigNodeTree=tenantTreeService.getConfigTreeNodeForFeatureGroup("Gap","site1","featuregroup1");
		Assert.assertNotNull("Feature Group ConfigurationTreeNode should not be Null",fgconfigNodeTree);
		Assert.assertEquals("Feature Group node not loaded Properly","featuregroup1", fgconfigNodeTree.getNodeName());
	}
}
