package com.getusroi.permastore.policy.persistence.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.getusroi.config.persistence.ConfigNodeData;
import com.getusroi.config.persistence.ConfigurationTreeNode;
import com.getusroi.config.persistence.dao.ConfigNodeDAO;
import com.getusroi.config.persistence.dao.ConfigNodeDataDAO;


public class ConfigNodeDAOTest {

	public void testConfigNodeData() throws IOException{
		
		ConfigNodeDataDAO configNodeDataDAO = new ConfigNodeDataDAO();
    	
    	List<ConfigNodeData> configNodeDataList=null;
		try {
			configNodeDataList = configNodeDataDAO.getConfigNodeDataByNodeId(22);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println(configNodeDataList);
	}
	
	@Test
	public void getNodeTree() throws SQLException, IOException{
		ConfigNodeDAO configNodeDAO=new ConfigNodeDAO();
		ConfigurationTreeNode policyNode=configNodeDAO.getNodeTree();
		Assert.assertNotNull("configNodeDataDAO should not return Empty Tree",policyNode);
	}
}
