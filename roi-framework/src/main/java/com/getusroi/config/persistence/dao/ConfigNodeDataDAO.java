package com.getusroi.config.persistence.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.RequestContext;
import com.getusroi.config.persistence.ConfigNodeData;

/**
 * 
 * @author amit #TODO Proper Exception Handling is Pending
 */
public class ConfigNodeDataDAO {
	final Logger logger = LoggerFactory.getLogger(ConfigNodeDataDAO.class);
	// public void getConfigNodeDataByNodeId()
	// public void insertConfigNodeData(ConfigNodeData)
	// public void updateConfigNodeData()
	// public void getConfigNodeDataById()
	// public void deleteNodeDataById()
	// public void deleteNodeDataBiNodeId();

	public static final String SELECTCONFIGDATABY_NODEIDSQL = "SELECT * FROM confignodedata WHERE nodeId =?";
	public static final String SELECTCONFIGDATABY_NODEID_CONFIGNAME_SQL = "SELECT * FROM confignodedata WHERE nodeId =? and configName=? and configType=?";
	public static final String SELECTCONFIGDATABY_NODEDATAID_SQL = "SELECT * FROM confignodedata WHERE nodedataId=?";
	public static final String INSERTSQL = "INSERT INTO confignodedata(nodeId, configName, configData, configType, configStatus, createdDTM, failureMsg,isEnabled) 	VALUES (?,?,?, ?,?, NOW(), ?,?)";
	public static final String DELETE_CONFIGDATABY_NODEID_SQL = "DELETE FROM confignodedata WHERE nodeId =?";
	public static final String DELETE_CONFIGDATABY_CONFIGDATAID_SQL = "DELETE FROM confignodedata WHERE nodeDataId =?";
	public static final String UPDATE_ENABLED_SQL = "UPDATE confignodedata SET isEnabled =? WHERE nodeDataId =?";

	public static final String SELECTCONFIGDATABY_NODEIDANDTYPE_SQL = "SELECT * FROM confignodedata WHERE nodeId=? and  configType=?";
	public static final String DELETE_CONFIGDATABY_NODEID_CONFIGNAME_SQL = "DELETE FROM confignodedata WHERE nodeId =? and configName=?";
	public static final String UPDATE_CONFIG_BYNODEIDSQL = "update confignodedata set nodeId=?, configName=?, configData=?, configType=?, configStatus=?, createdDTM=NOW(), failureMsg=?,isEnabled=?  where nodedataId=?";
	public static final String UPDATECONFIGDATABY_NODENAME_NODEID_SQL = "Update confignodedata set configData=? WHERE nodeId =? and configName=? and configType=?";
	public static final String SELECTCONFIG_NODEID_SQL = "SELECT nodeId,configName,configType FROM confignodedata where isEnabled=1";
	public static final String SELECT_REQUEST_CONETEXT_SQL = "SELECT nodeName,parentNodeId,type,version FROM confignode where nodeId=?";

	public int insertConfigNodeData(ConfigNodeData nodeData) throws SQLException, IOException {
		Connection con = null;
		PreparedStatement pstmt = null;
		int generatedNodeDataId = 0;
		try {
			con = DataBaseUtil.getConnection();
			pstmt = (PreparedStatement) con.prepareStatement(INSERTSQL, Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, nodeData.getParentConfigNodeId());
			pstmt.setString(2, nodeData.getConfigName());
			pstmt.setString(3, nodeData.getConfigData());
			pstmt.setString(4, nodeData.getConfigType());
			pstmt.setString(5, nodeData.getConfigLoadStatus());
			if (nodeData.getFailureMsg() == null) {
				pstmt.setNull(6, java.sql.Types.VARCHAR);
			} else {
				pstmt.setString(6, nodeData.getFailureMsg());
			}
			if (nodeData.isEnabled()) {
				pstmt.setInt(7, 1);
			} else {
				pstmt.setInt(7, 0);
			}

			pstmt.executeUpdate();
			ResultSet rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
				generatedNodeDataId = rs.getInt(1);
				logger.debug("insertConfigNodeData-generatedKeyIs=" + generatedNodeDataId);
			} else {
				logger.debug("insertConfigNodeData-generatedKey Resultset not found");
			}
		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
			// #TODO Exception Handling
		} finally {
			DataBaseUtil.dbCleanUp(con, pstmt);
		}
		return generatedNodeDataId;
	}

	public void enableConfigNodeData(boolean setEnable, Integer nodeDataId) throws SQLException, IOException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getConnection();
			pstmt = (PreparedStatement) con.prepareStatement(UPDATE_ENABLED_SQL);
			if (setEnable) {
				pstmt.setInt(1, 1);
			} else {
				pstmt.setInt(1, 0);
			}
			pstmt.setInt(2, nodeDataId);

			pstmt.executeUpdate();
		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
			// #TODO Exception Handling
		} finally {
			DataBaseUtil.dbCleanUp(con, pstmt);
		}
	}

	public void updateConfigDataByNameAndNodeId(String xmlString, Integer nodeId, String configName, String configType)
			throws SQLException, IOException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ConfigNodeData configData = null;

		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(UPDATECONFIGDATABY_NODENAME_NODEID_SQL);
			ps.setString(1, xmlString);
			ps.setInt(2, nodeId);
			ps.setString(3, configName);
			ps.setString(4, configType);
			ps.execute();

		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the DB Driver", cnfe);
			// #TODO Exception Handling

		} finally {
			DataBaseUtil.dbCleanUp(conn, ps);
		}
	}

	public ConfigNodeData getConfigNodeDatabyNameAndNodeId(Integer nodeId, String configName, String configType)
			throws SQLException, IOException {
		logger.debug("getConfigNodeDatabyNameAndNodeId method");
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ConfigNodeData configData = null;

		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(SELECTCONFIGDATABY_NODEID_CONFIGNAME_SQL);
			ps.setInt(1, nodeId);
			ps.setString(2, configName);
			ps.setString(3, configType);
			rs = ps.executeQuery();

			if (rs.next()) {
				configData = new ConfigNodeData();
				parseRS(configData, rs);

			}
		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the DB Driver", cnfe);
			// #TODO Exception Handling

		} finally {
			DataBaseUtil.dbCleanup(conn, ps, rs);
		}
		return configData;
	}

	public ConfigNodeData getConfigNodeDatabyId(Integer configNodeDataId) throws SQLException, IOException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ConfigNodeData configData = null;

		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(SELECTCONFIGDATABY_NODEDATAID_SQL);
			ps.setInt(1, configNodeDataId);

			rs = ps.executeQuery();

			if (rs.next()) {
				configData = new ConfigNodeData();
				parseRS(configData, rs);
			}
		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the DB Driver", cnfe);
			// #TODO Exception Handling

		}

		finally {
			DataBaseUtil.dbCleanup(conn, ps, rs);
		}
		return configData;
	}

	public List<ConfigNodeData> getConfigNodeDataByNodeId(Integer nodeId) throws SQLException, IOException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ConfigNodeData configData = null;
		List<ConfigNodeData> list = new ArrayList<ConfigNodeData>();
		logger.debug(".getConfigNodeDataByNodeId() NodeId=" + nodeId);
		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(SELECTCONFIGDATABY_NODEIDSQL);
			ps.setInt(1, nodeId);
			rs = ps.executeQuery();

			while (rs.next()) {
				configData = new ConfigNodeData();
				parseRS(configData, rs);
				list.add(configData);
			}
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}

		finally {
			DataBaseUtil.dbCleanup(conn, ps, rs);
		}

		return list;
	}

	public int deleteConfigNodeData(Integer configNodeDataId) throws SQLException, IOException {
		// DELETE_CONFIGDATABY_CONFIGDATAID_SQL
		logger.debug(".deleteConfigNodeData() configNodeDataId=" + configNodeDataId);
		Connection con = null;
		PreparedStatement pstmt = null;
		int rowsImpacted = 0;
		try {
			con = DataBaseUtil.getConnection();
			pstmt = (PreparedStatement) con.prepareStatement(DELETE_CONFIGDATABY_CONFIGDATAID_SQL);
			pstmt.setInt(1, configNodeDataId);
			rowsImpacted = pstmt.executeUpdate();
			logger.debug(
					"cofignodedata is deleted  for configNodeDataId= " + configNodeDataId + " sucess=" + rowsImpacted);
		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
			// #TODO Exception Handling
		} finally {
			DataBaseUtil.dbCleanUp(con, pstmt);
		}
		return rowsImpacted;
	}

	/**
	 * 
	 * @param nodeId
	 * @param configType
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<ConfigNodeData> getConfigNodeDataByNodeIdByConfigType(Integer nodeId, String configType)
			throws SQLException, IOException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ConfigNodeData configData = null;
		List<ConfigNodeData> list = new ArrayList<ConfigNodeData>();
		logger.debug(".getConfigNodeDataByNodeId() NodeId=" + nodeId);
		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(SELECTCONFIGDATABY_NODEIDANDTYPE_SQL);
			ps.setInt(1, nodeId);
			ps.setString(2, configType);

			rs = ps.executeQuery();

			while (rs.next()) {
				configData = new ConfigNodeData();
				parseRS(configData, rs);
				list.add(configData);
			}
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}

		finally {
			DataBaseUtil.dbCleanup(conn, ps, rs);
		}

		return list;
	}

	/**
	 * 
	 * @param nodeData
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public int updateConfigNodeData(ConfigNodeData nodeData) throws SQLException, IOException {
		Connection con = null;
		PreparedStatement pstmt = null;
		int generatedNodeDataId = 0;

		try {
			con = DataBaseUtil.getConnection();
			pstmt = (PreparedStatement) con.prepareStatement(UPDATE_CONFIG_BYNODEIDSQL);
			pstmt.setInt(1, nodeData.getParentConfigNodeId());
			pstmt.setString(2, nodeData.getConfigName());
			pstmt.setString(3, nodeData.getConfigData());
			pstmt.setString(4, nodeData.getConfigType());
			pstmt.setString(5, nodeData.getConfigLoadStatus());
			if (nodeData.getFailureMsg() == null) {
				pstmt.setNull(6, java.sql.Types.VARCHAR);
			} else {
				pstmt.setString(6, nodeData.getFailureMsg());
			}
			if (nodeData.isEnabled()) {
				pstmt.setInt(7, 1);
			} else {
				pstmt.setInt(7, 0);
			}
			pstmt.setInt(8, nodeData.getNodeDataId());
			int rs = pstmt.executeUpdate();

			logger.debug("updated sucessfullled with sucess flag " + rs);
			generatedNodeDataId = rs;
			if (rs == 1) {
				logger.debug("Information updated successfully with nodeDataId = " + nodeData.getNodeDataId());
			} else {
				logger.debug("Failed update");
			}
		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
			// #TODO Exception Handling
		} finally {
			DataBaseUtil.dbCleanUp(con, pstmt);
		}
		return generatedNodeDataId;
	}

	public int deleteConfigNodeDataByNodeId(Integer nodeId) throws SQLException, IOException {
		Connection con = null;
		PreparedStatement pstmt = null;
		int rowsImpacted = 0;
		try {
			con = DataBaseUtil.getConnection();
			pstmt = (PreparedStatement) con.prepareStatement(DELETE_CONFIGDATABY_NODEID_SQL);
			pstmt.setInt(1, nodeId);
			rowsImpacted = pstmt.executeUpdate();

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
			// #TODO Exception Handling
		} finally {
			DataBaseUtil.dbCleanUp(con, pstmt);
		}
		return rowsImpacted;
	}

	/**
	 * 
	 * @param configName
	 * @param nodeId
	 * @return int
	 * @throws SQLException
	 * @throws IOException
	 */
	public int deleteConfigNodeDataByNodeIdAndByConfigName(String configName, int nodeId)
			throws SQLException, IOException {
		Connection con = null;
		PreparedStatement pstmt = null;
		int rowsImpacted = 0;
		try {
			con = DataBaseUtil.getConnection();
			pstmt = (PreparedStatement) con.prepareStatement(DELETE_CONFIGDATABY_NODEID_CONFIGNAME_SQL);
			pstmt.setInt(1, nodeId);
			pstmt.setString(2, configName);
			rowsImpacted = pstmt.executeUpdate();

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
			// #TODO Exception Handling
		} finally {
			DataBaseUtil.dbCleanUp(con, pstmt);
		}
		return rowsImpacted;
	}

	/**
	 * this method is used to get the List of RequestContext object , configName
	 * and configType.
	 * 
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<Map<String, Object>> getRequestContextList(String feature) throws SQLException, IOException {
		Connection con = null;
		PreparedStatement pstmt = null;
		Statement stmt = null;
		ResultSet rs = null;
		Map<String, Object> requestContextMap = null;
		List<Map<String, Object>> requestContextList = new ArrayList<>();
		try {
			con = DataBaseUtil.getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(SELECTCONFIG_NODEID_SQL);
			while (rs.next()) {
				requestContextMap = getRequestContext(rs.getInt("nodeId"), rs.getString("configName"),
						rs.getString("configType"), feature, con);
				if (requestContextMap != null)
					requestContextList.add(requestContextMap);
			}

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
			// #TODO Exception Handling
		} finally {
			DataBaseUtil.dbCleanUp(con, pstmt);
		}

		return requestContextList;

	}

	private Map<String, Object> getRequestContext(int nodeId, String configName, String configType, String feature,
			Connection con) throws SQLException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int parentNodeId = nodeId;
		RequestContext requestContext = new RequestContext();
		Map<String, Object> requestContextMap = new HashMap<>();
		pstmt = (PreparedStatement) con.prepareStatement(SELECT_REQUEST_CONETEXT_SQL);
		while (parentNodeId > 0) {
			pstmt.setInt(1, parentNodeId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				String type = rs.getString("type").trim().toLowerCase();
				switch (type) {
				case "vendor":
					requestContext.setVendor(rs.getString("nodeName"));
					break;
				case "implementation":
					requestContext.setImplementationName(rs.getString("nodeName"));
					break;
				case "feature":
					String featureInDB = rs.getString("nodeName");
					if (featureInDB.trim().toLowerCase().equals(feature.trim().toLowerCase()))
						requestContext.setFeatureName(featureInDB);

					else
						return null;

				case "feature_group":
					requestContext.setFeatureGroup(rs.getString("nodeName"));
					break;
				case "site":
					requestContext.setSiteId(rs.getString("nodeName"));
					break;
				case "tenant":
					requestContext.setTenantId(rs.getString("nodeName"));
					break;
				}
				requestContext.setVersion(rs.getString("version"));
				parentNodeId = rs.getInt("parentNodeId");

			}

		}
		requestContextMap.put("requestContext", requestContext);
		requestContextMap.put("configName", configName);
		requestContextMap.put("configType", configType);

		return requestContextMap;

	}

	private void parseRS(ConfigNodeData configData, ResultSet rs) throws SQLException {
		configData.setNodeDataId(rs.getInt(1));
		configData.setParentConfigNodeId(rs.getInt(2));
		configData.setConfigName(rs.getString(3));
		configData.setConfigData(rs.getString(4));
		configData.setConfigType(rs.getString(5));
		configData.setConfigLoadStatus(rs.getString(6));
		int isEnabled = rs.getInt(7);
		if (isEnabled > 0)
			configData.setEnabled(true);
		// bydefault its false any ways

		configData.setCreatedDTM(rs.getDate(8));
		configData.setFailureMsg(rs.getString(9));

	}

	

}