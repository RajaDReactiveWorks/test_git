package com.attunedlabs.permastore.policy.persistence.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.persistence.dao.ConfigNodeDAO;
import com.attunedlabs.config.persistence.dao.ConfigNodeDataDAO;
import com.attunedlabs.config.persistence.exception.ConfigNodeConfigurationException;
import com.attunedlabs.config.persistence.exception.ConfigNodeDataConfigurationException;


public class ConfigNodeDAOTest {

	public void testConfigNodeData() throws IOException, ConfigNodeDataConfigurationException{
		
		ConfigNodeDataDAO configNodeDataDAO = new ConfigNodeDataDAO();
    	
    	List<ConfigNodeData> configNodeDataList=null;
		configNodeDataList = configNodeDataDAO.getConfigNodeDataByNodeId(22);
    	System.out.println(configNodeDataList);
	}
	
	@Test
	public void getNodeTree() throws SQLException, IOException, ConfigNodeConfigurationException{
		ConfigNodeDAO configNodeDAO=new ConfigNodeDAO();
		ConfigurationTreeNode policyNode=configNodeDAO.getNodeTree();
		Assert.assertNotNull("configNodeDataDAO should not return Empty Tree",policyNode);
	}
}
