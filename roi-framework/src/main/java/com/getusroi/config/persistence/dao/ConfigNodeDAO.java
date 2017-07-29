package com.getusroi.config.persistence.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigNode;
import com.getusroi.config.persistence.ConfigurationTreeNode;

/**
 * 
 * @author bizruntime #TODO Proper Exception Handling is Pending
 */
public class ConfigNodeDAO {
	final Logger logger = LoggerFactory.getLogger(ConfigNodeDAO.class);
	public static final String INSERTSQL = "INSERT INTO confignode (nodeName, isRoot, hasChildren, parentNodeId, description, type, level) VALUES (?, ?, ?, ?, ?, ?, ?)";
	public static final String INSERTSQLWITHVERSION = "INSERT INTO confignode (nodeName, isRoot, hasChildren, parentNodeId, description, type, level,version,primaryFeatureId) VALUES (?, ?,?,?, ?, ?, ?, ?, ?)";

	public static final String SELECTBYNODEIDSQL = "SELECT * FROM confignode WHERE nodeId=?";
	public static final String SELECTBYPARENTNODEIDSQL = "SELECT * FROM confignode WHERE parentNodeId=?";
	public static final String SELECTBYCONFIGDATASQL = "select NodeId,nodeName,type,parentNodeId,level,version,primaryFeatureId from confignode order by parentNodeId ASC ";
	public static final String DELETENODESQl = "delete from confignode where nodeId=? ";
	public static final String UPDATENODESQl = "update  confignode  set nodeName= ? where nodeId=? ";
	public static final String SELECTBYNODeBYNAMEANDTYPEESQL = "select nodeId from confignode where nodeName=? and type=? ";
	public static final String UPDATE_NODE_BY_PRIMARY_FEATURE_ID_SQl = "update  confignode  set primaryFeatureId= ? where nodeId=? ";

	public static final String SELECTBYNODENAMESQL = "select nodeId from confignode where nodeName=? and type=? and parentNodeId=?";
	public static final String SELECTBYNODENAMEANDPARENTIDSQL = "select nodeId from confignode where nodeName=? and type=? and parentNodeId=? and nodeId!=?";

	/**
	 * Inserts ConfigNode in the DB
	 * 
	 * @param node
	 *            = set the values from POJO to MySQL.
	 * @return true
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 * 
	 */
	public int insertConfigNode(ConfigNode node) throws SQLException, IOException {

		Connection con = null;
		PreparedStatement pstmt = null;
		int generatedNodeId = 0;
		try {
			con = DataBaseUtil.getConnection();
			pstmt = (PreparedStatement) con.prepareStatement(INSERTSQL, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, node.getNodeName());
			pstmt.setBoolean(2, node.isRoot());
			pstmt.setBoolean(3, node.isHasChildren());
			if (node.getParentNodeId() != null) {
				pstmt.setInt(4, node.getParentNodeId());
			} else {
				pstmt.setNull(4, java.sql.Types.NULL);
			}
			pstmt.setString(5, node.getDescription());
			pstmt.setString(6, node.getType());
			pstmt.setInt(7, node.getLevel());

			pstmt.executeUpdate();
			ResultSet rs = pstmt.getGeneratedKeys();

			if (rs.next()) {
				generatedNodeId = rs.getInt(1);
				logger.debug("insertConfigNode-generatedKeyIs=" + generatedNodeId);
			} else {
				logger.debug("insertConfigNode-generatedKey Resultset not found");
			}

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
			// #TODO Exception Handling
		} finally {
			DataBaseUtil.dbCleanUp(con, pstmt);
		}
		return generatedNodeId;
	}

	/**
	 * Inserts ConfigNode in the DB with Version
	 * 
	 * @param node
	 *            = set the values from POJO to MySQL.
	 * @return true
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 * 
	 */
	public int insertConfigNodeWithVersion(ConfigurationTreeNode treeNode) throws SQLException, IOException {

		Connection con = null;
		PreparedStatement pstmt = null;
		int generatedNodeId = 0;
		try {
			con = DataBaseUtil.getConnection();
			pstmt = (PreparedStatement) con.prepareStatement(INSERTSQLWITHVERSION, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, treeNode.getNodeName());
			pstmt.setBoolean(2, false);
			pstmt.setBoolean(3, treeNode.isHasChildern());
			if (treeNode.getParentNodeId() != null) {
				pstmt.setInt(4, treeNode.getParentNodeId());
			} else {
				pstmt.setNull(4, java.sql.Types.NULL);
			}
			pstmt.setString(5, treeNode.getDescription());
			pstmt.setString(6, treeNode.getType());
			pstmt.setInt(7, treeNode.getLevel());
			pstmt.setString(8, treeNode.getVersion());
			pstmt.setInt(9, treeNode.getPrimaryFeatureId());

			pstmt.executeUpdate();
			ResultSet rs = pstmt.getGeneratedKeys();

			if (rs.next()) {
				generatedNodeId = rs.getInt(1);
				logger.debug("insertConfigNode-generatedKeyIs=" + generatedNodeId);
			} else {
				logger.debug("insertConfigNode-generatedKey Resultset not found");
			}

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
			// #TODO Exception Handling
		} finally {
			DataBaseUtil.dbCleanUp(con, pstmt);
		}
		return generatedNodeId;
	}

	public boolean updateNodeWithPrimaryFeatureId(int nodeId, int primaryFeatureId) throws SQLException, IOException {

		Connection conn = null;
		PreparedStatement ps = null;
		boolean isUpdated = false;
		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(UPDATE_NODE_BY_PRIMARY_FEATURE_ID_SQl);
			ps.setInt(1, primaryFeatureId);
			ps.setInt(2, nodeId);
			if (ps.executeUpdate() > 0)
				isUpdated = true;
		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the MySqlDriver. shame", cnfe);
		} finally {
			DataBaseUtil.dbCleanUp(conn, ps);
		}
		return isUpdated;
	}

	/**
	 * 
	 * @param nodeId
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * 
	 */
	public ConfigNode getNodeById(Integer nodeId) throws SQLException, IOException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ConfigNode config = null;

		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(SELECTBYNODEIDSQL);
			ps.setInt(1, nodeId);
			rs = ps.executeQuery();

			if (rs.next()) {
				config = new ConfigNode();
				parseRS(config, rs);
			}
		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the MySqlDriver. shame", cnfe);
		} finally {
			DataBaseUtil.dbCleanup(conn, ps, rs);
		}
		return config;
	}

	/**
	 * 
	 * @param nodeId
	 * @param nodeName
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public int updateNodeByName(Integer nodeId, String nodeName) throws SQLException, IOException {
		Connection conn = null;
		PreparedStatement ps = null;
		int sucess = 0;

		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(UPDATENODESQl);
			ps.setString(1, nodeName);
			ps.setInt(2, nodeId);
			sucess = ps.executeUpdate();

		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the MySqlDriver. shame", cnfe);
		} finally {
			DataBaseUtil.dbCleanUp(conn, ps);
		}
		return sucess;
	}

	// TO search node exsist with given nodename and type
	public int getNodeIdByNodeNameAndByType(String nodeName, String type) throws SQLException, IOException {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int nodeId = 0;
		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(SELECTBYNODeBYNAMEANDTYPEESQL);
			ps.setString(1, nodeName);
			ps.setString(2, type);
			rs = ps.executeQuery();

			while (rs.next()) {
				nodeId = rs.getInt("nodeId");
			}
		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the MySqlDriver. shame", cnfe);
		} finally {
			DataBaseUtil.dbCleanup(conn, ps, rs);
		}

		return nodeId;

	}

	/**
	 * 
	 * @param nodeName
	 * @param type
	 * @param parentNodeId
	 * @param updatingNodeId
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */

	// TO search nodeId of given nodename , type and ParnetNodeId not with
	// updateNodeId
	public int getNodeIdByNodeNameAndByTypeNotNodeId(String nodeName, String type, int parentNodeId, int updatingNodeId)
			throws SQLException, IOException {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int nodeId = 0;
		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(SELECTBYNODENAMEANDPARENTIDSQL);
			ps.setString(1, nodeName);
			ps.setString(2, type);
			ps.setInt(3, parentNodeId);
			ps.setInt(4, updatingNodeId);

			rs = ps.executeQuery();

			while (rs.next()) {
				nodeId = rs.getInt("nodeId");
			}
		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the MySqlDriver. shame", cnfe);
		} finally {
			DataBaseUtil.dbCleanup(conn, ps, rs);
		}
		return nodeId;
	}

	/**
	 * 
	 * @param parentNodeId
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<ConfigNode> getChildNodes(Integer parentNodeId) throws SQLException, IOException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ConfigNode config = null;
		List<ConfigNode> list = new ArrayList<ConfigNode>();

		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(SELECTBYPARENTNODEIDSQL);
			ps.setInt(1, parentNodeId);
			rs = ps.executeQuery();

			while (rs.next()) {
				config = new ConfigNode();
				parseRS(config, rs);
				list.add(config);
			}
		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the MySqlDriver. shame", cnfe);
		} finally {
			DataBaseUtil.dbCleanup(conn, ps, rs);
		}

		return list;
	}

	public ConfigurationTreeNode getNodeTree() throws SQLException, IOException {
		logger.debug(".getNodeTree method of ConfigNodeDAO ");
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		// PolicyTreeNode node = null;
		ConfigurationTreeNode rootNode = new ConfigurationTreeNode(new Integer(0), "Root", "Root", 0, "1.0", 0);
		try {

			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(SELECTBYCONFIGDATASQL);
			rs = ps.executeQuery();
			// Handling Root Node
			if (rs.next()) {
				ConfigurationTreeNode node = new ConfigurationTreeNode();
				parseRSNode(node, rs);
				rootNode.addChildren(node);
				logger.debug("Got the Root Node");
			}

			int currentLevel = 1;
			while (rs.next()) {
				ConfigurationTreeNode node = new ConfigurationTreeNode();
				parseRSNode(node, rs);
				// System.out.println(".getNodeTree()
				// currentLevel="+currentLevel+" --Node Data Is"+node);
				rootNode.addChildren(node);
				// addToParentNode(node,rootNode);
			}

		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the MySqlDriver. shame", cnfe);
		} finally {
			DataBaseUtil.dbCleanup(conn, ps, rs);
		}

		logger.debug("--------Final Node=" + rootNode);
		return rootNode;
	}

	public int deleteNodeByNodeId(int nodeID) throws SQLException, IOException {
		Connection conn = null;
		PreparedStatement ps = null;
		int sucess = 0;
		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(DELETENODESQl);
			ps.setInt(1, nodeID);
			sucess = ps.executeUpdate();
			// now delete Node Data for this node
			// #TODO Note it should be in transaction will fix when move to
			// Container managed transaction
			ConfigNodeDataDAO configPolicyDao = new ConfigNodeDataDAO();

		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the MySqlDriver. shame", cnfe);
		} finally {
			DataBaseUtil.dbCleanUp(conn, ps);
		}

		return sucess;
	}

	/**
	 * 
	 * @param nodeName
	 * @param type
	 * @param parentNodeId
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	// TO search node exsist with given nodename , type and ParnetNodeId
	public int getNodeIdByNodeNameAndByType(String nodeName, String type, int parentNodeId)
			throws SQLException, IOException {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int nodeId = 0;
		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(SELECTBYNODENAMESQL);
			ps.setString(1, nodeName);
			ps.setString(2, type);
			ps.setInt(3, parentNodeId);

			rs = ps.executeQuery();

			while (rs.next()) {
				nodeId = rs.getInt("nodeId");
			}
		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the MySqlDriver. shame", cnfe);
		} finally {
			DataBaseUtil.dbCleanup(conn, ps, rs);
		}

		return nodeId;

	}

	public int getVendorId(String feature_group, String feature, String implementation, String vendor, int parentNodeId)
			throws SQLException, IOException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int nodeId = 0;
		try {
			conn = DataBaseUtil.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(SELECTBYNODENAMESQL);
			ps.setString(1, feature_group);
			ps.setString(2, "feature_group");
			ps.setInt(3, parentNodeId);

			rs = ps.executeQuery();

			while (rs.next()) {
				nodeId = rs.getInt("nodeId");
			}
			ps.setString(1, feature);
			ps.setString(2, "feature");
			ps.setInt(3, nodeId);
			rs = ps.executeQuery();

			while (rs.next()) {
				nodeId = rs.getInt("nodeId");
			}
			ps.setString(1, implementation);
			ps.setString(2, "implementation");
			ps.setInt(3, nodeId);
			rs = ps.executeQuery();

			while (rs.next()) {
				nodeId = rs.getInt("nodeId");
			}
			ps.setString(1, vendor);
			ps.setString(2, "vendor");
			ps.setInt(3, nodeId);
			rs = ps.executeQuery();

			while (rs.next()) {
				nodeId = rs.getInt("nodeId");
			}

		} catch (ClassNotFoundException cnfe) {
			logger.error("Failed to Load the MySqlDriver. shame", cnfe);
		} finally {
			DataBaseUtil.dbCleanup(conn, ps, rs);
		}

		return nodeId;

	}

	/**
	 * 
	 * @param configNode
	 * @param rs
	 * @throws SQLException
	 */
	private void parseRS(ConfigNode configNode, ResultSet rs) throws SQLException {
		configNode.setNodeId(rs.getInt(1));
		configNode.setNodeName(rs.getString(2));
		configNode.setRoot(rs.getBoolean(3));
		configNode.setHasChildren(rs.getBoolean(4));
		configNode.setParentNodeId(rs.getInt(5));
		configNode.setDescription(rs.getString(6));
		configNode.setType(rs.getString(7));
		configNode.setLevel(rs.getInt(8));
		configNode.setVersion(rs.getString(9));
	}

	private void parseRSNode(ConfigurationTreeNode node, ResultSet rs) throws SQLException {
		node.setNodeId(rs.getInt(1));
		node.setNodeName(rs.getString(2));
		node.setType(rs.getString(3));
		node.setParentNodeId(rs.getInt(4));
		node.setLevel(rs.getInt(5));
		node.setVersion(rs.getString(6));
		node.setPrimaryFeatureId(rs.getInt(7));
	}

}
