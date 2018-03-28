package com.attunedlabs.permastore.policy.persistence.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.dao.ConfigNodeDataDAO;
import com.attunedlabs.config.persistence.exception.ConfigNodeDataConfigurationException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationConstant;

public class ConfigNodeDataDAOTest {
	final Logger logger = LoggerFactory.getLogger(ConfigNodeDataDAOTest.class);
	private static final int queryNodeId=1224;
	private static final String testConfigName="Test2";
	@Test
	public void testgetNodeDataByNodeId() throws SQLException, IOException, ConfigNodeDataConfigurationException{
		ConfigNodeDataDAO configNodeDataDAO=new ConfigNodeDataDAO();
		List<ConfigNodeData> configNodeDataList=configNodeDataDAO.getConfigNodeDataByNodeId(new Integer(queryNodeId));
		if(configNodeDataList!=null){
			logger.debug(configNodeDataList.toString());
		}
		Assert.assertNotNull("configNodeDataDAO should not return Empty Tree",configNodeDataList);
	}
	
	@Test
	public void testInsertNodeDataByObject(){
		ConfigNodeDataDAO configNodeDataDAO=new ConfigNodeDataDAO();
		ConfigNodeData nodeData=new ConfigNodeData();
		nodeData.setParentConfigNodeId(queryNodeId);
		nodeData.setConfigName(testConfigName);
		nodeData.setConfigType(PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
		nodeData.setEnabled(false);
		nodeData.setConfigLoadStatus("Sucess");
		nodeData.setConfigData("{Juniut:Test}");
		try{
			int generatedPk=configNodeDataDAO.insertConfigNodeData(nodeData);
			Assert.assertTrue("confignodeData inserted but generated Id is 0 hence failure", generatedPk>0);
		}catch(Exception exp){
			logger.error(".testInsertNodeDataByObject() failed", exp);
			Assert.fail("confignodeData failed to insert data");
		}
	}
	
	@Test
	public void testgetNodeDataByNameAndNodeId() throws SQLException, IOException, ConfigNodeDataConfigurationException{
		ConfigNodeDataDAO configNodeDataDAO=new ConfigNodeDataDAO();
		ConfigNodeData configNodeData=configNodeDataDAO.getConfigNodeDatabyNameAndNodeId(new Integer(queryNodeId),testConfigName,PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
		logger.debug("testgetNodeDataByNameAndNodeId()--returned Data"+configNodeData);
		Assert.assertNotNull("configNodeDataDAO should not return null",configNodeData);
	}
	//getConfigNodeDatabyNameAndNodeId(Integer nodeId,String configName,String configType)
	
}
