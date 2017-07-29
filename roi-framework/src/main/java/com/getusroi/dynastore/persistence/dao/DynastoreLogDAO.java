package com.getusroi.dynastore.persistence.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.dao.DataBaseUtil;
import com.getusroi.dynastore.persistence.DynaStoreLog;

public class DynastoreLogDAO {

	final Logger logger = LoggerFactory.getLogger(DynastoreLogDAO.class);

	public static final String INSERTSQL = "INSERT INTO  dynastorelog (siteNodeId,sessionId,status,openedDTM,info) values (?,?,?,NOW(),?) ";
	public static final String UPDATESQL = "UPDATE   dynastorelog set status=?,closedDTM=NOW() where siteNodeId=? and sessionId=?";
	public static final String SELECTSTATUBYSITEIDANDSESSIONIDSQL = "select * from   dynastorelog  where siteNodeId=? and sessionId=?";
	public static final String DELETEDYNASTORELOGSQL = "delete from   dynastorelog ";
	public static final String SELECTBYSTATUSANDOPENDDATESQL = "select * from   dynastorelog  where siteNodeId=? and status=? and openedDTM=?";
	public static final String SELECTBYSTATUSANDCLOSEDDATESQL = "select * from   dynastorelog  where siteNodeId=? and status=? and closedDTM=?";
	public static final String SELECTBYSTATUSSQL = "select * from   dynastorelog  where siteNodeId=? and status=?";
	public static final String SELECTBYGREATERTHANGIVENDATESQL = "select * from   dynastorelog  where siteNodeId=? and openedDTM>=?";

	public static final String SELECTBYSITEIDSQL = "select * from   dynastorelog  where siteNodeId=?";

	public static final String SELECTBYSTATUSANDDDATEANDSTATUSSQL = "select * from   dynastorelog  where siteNodeId=? and status=? and openedDTM>=?";

	/**
	 * to insert DynastoreLog into DB
	 * 
	 * @param siteNodeId
	 * @param sessionId
	 * @param status
	 * @param info
	 * @return if updated return 1 else 0
	 * @throws SQLException
	 * @throws IOException
	 */
	public int insertDynastoreLog(int siteNodeId, String sessionId,
			String status, String info) throws SQLException, IOException {
		Connection con = null;
		PreparedStatement pstmt = null;
		int isInserted = 0;
		try {
			con = DataBaseUtil.getConnection();

			pstmt = (PreparedStatement) con.prepareStatement(INSERTSQL);
			pstmt.setInt(1, siteNodeId);
			pstmt.setString(2, sessionId);
			pstmt.setString(3, status);
			pstmt.setString(4, info);
			isInserted = pstmt.executeUpdate();

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
		} finally {
			DataBaseUtil.dbCleanUp(con, pstmt);
		}
		return isInserted;

	}

	/**
	 * updating dynastoreLog status ,closedDTM in DB
	 * 
	 * @param siteNodeId
	 * @param sessionId
	 * @param status
	 * @return if updated return 1 else 0
	 * @throws SQLException
	 * @throws IOException
	 */
	public int updateDynastoreLog(int siteNodeId, String sessionId,
			String status) throws SQLException, IOException {
		Connection con = null;
		PreparedStatement pstmt = null;
		int isUpdated = 0;

		try {
			con = DataBaseUtil.getConnection();

			pstmt = (PreparedStatement) con.prepareStatement(UPDATESQL);

			pstmt.setString(1, status);
			pstmt.setInt(2, siteNodeId);
			pstmt.setString(3, sessionId);
			isUpdated = pstmt.executeUpdate();

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
		} finally {
			DataBaseUtil.dbCleanUp(con, pstmt);
		}
		return isUpdated;
	}

	/**
	 * To get status of dynastoreLog fromDB By siteId and sessionId
	 * 
	 * @param siteNodeId
	 * @param sessionId
	 * @return status (closed or opened)
	 * @throws SQLException
	 * @throws IOException
	 */
	public String getDynaStoreLog(int siteNodeId, String sessionId)
			throws SQLException, IOException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String status = "";
		try {
			con = DataBaseUtil.getConnection();

			pstmt = (PreparedStatement) con
					.prepareStatement(SELECTSTATUBYSITEIDANDSESSIONIDSQL);

			pstmt.setInt(1, siteNodeId);
			pstmt.setString(2, sessionId);

			rs = pstmt.executeQuery();
			if (rs.next()) {
				status = rs.getString("status");
			}

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
		} finally {
			DataBaseUtil.dbCleanup(con, pstmt, rs);
		}
		return status;
	}

	/**
	 * To delete all dynastoreLog
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	/*
	 * public void deleteDynaStoreLog() throws SQLException, IOException{
	 * Connection con = null; PreparedStatement pstmt = null;
	 * 
	 * try { con = DataBaseUtil.getConnection();
	 * 
	 * pstmt = (PreparedStatement) con.prepareStatement(DELETEDYNASTORELOGSQL);
	 * 
	 * 
	 * pstmt.executeUpdate();
	 * 
	 * 
	 * } catch (ClassNotFoundException e) {
	 * logger.error("Failed to Load the DB Driver", e); } finally {
	 * DataBaseUtil.dbCleanUp(con,pstmt); } }
	 */

	/**
	 * get list of dynastoreLogs fromDb By siteId, status , openedDate
	 * 
	 * @param siteId
	 * @param status
	 * @param opnedDate
	 * @return listof DynastoreLogs
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<DynaStoreLog> getDynaStoreLogByStatusAndOpenDate(int siteId,
			String status, Date opnedDate) throws SQLException, IOException {

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<DynaStoreLog> listOfDynaStoreLogs = new ArrayList<DynaStoreLog>();
		try {
			con = DataBaseUtil.getConnection();

			pstmt = (PreparedStatement) con
					.prepareStatement(SELECTBYSTATUSANDOPENDDATESQL);
			pstmt.setInt(1, siteId);

			pstmt.setString(2, status);
			pstmt.setDate(3, opnedDate);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				DynaStoreLog dynaStoreLog = new DynaStoreLog();
				parsRs(dynaStoreLog, rs);
				listOfDynaStoreLogs.add(dynaStoreLog);

			}

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
		} finally {
			DataBaseUtil.dbCleanup(con, pstmt, rs);
		}
		return listOfDynaStoreLogs;
	}

	/**
	 * to get list of dynastoreLog fromDB by siteid,status,closeddate
	 * 
	 * @param siteId
	 * @param status
	 * @param closedDate
	 * @return list Of dynastoreLog
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<DynaStoreLog> getDynaStoreLogByStatusAndClosedDate(int siteId,
			String status, Date closedDate) throws SQLException, IOException {

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<DynaStoreLog> listOfDynaStoreLogs = new ArrayList<DynaStoreLog>();
		try {
			con = DataBaseUtil.getConnection();

			pstmt = (PreparedStatement) con
					.prepareStatement(SELECTBYSTATUSANDCLOSEDDATESQL);

			pstmt.setInt(1, siteId);

			pstmt.setString(2, status);
			pstmt.setDate(3, closedDate);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				DynaStoreLog dynaStoreLog = new DynaStoreLog();
				parsRs(dynaStoreLog, rs);
				listOfDynaStoreLogs.add(dynaStoreLog);

			}

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
		} finally {
			DataBaseUtil.dbCleanup(con, pstmt, rs);
		}
		return listOfDynaStoreLogs;
	}

	/**
	 * to get list of dynastoreLog from DB where opendate greater than given
	 * date, siteid
	 * 
	 * @param siteId
	 * @param date
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */

	public List<DynaStoreLog> getDynaStoreLogByGreaterThanGivenDate(int siteId,
			Date date) throws SQLException, IOException {

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<DynaStoreLog> listOfDynaStoreLogs = new ArrayList<DynaStoreLog>();
		try {
			con = DataBaseUtil.getConnection();

			pstmt = (PreparedStatement) con
					.prepareStatement(SELECTBYGREATERTHANGIVENDATESQL);
			pstmt.setInt(1, siteId);
			pstmt.setDate(2, date);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				DynaStoreLog dynaStoreLog = new DynaStoreLog();
				parsRs(dynaStoreLog, rs);
				listOfDynaStoreLogs.add(dynaStoreLog);

			}

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
		} finally {
			DataBaseUtil.dbCleanup(con, pstmt, rs);
		}
		return listOfDynaStoreLogs;
	}

	/**
	 * to get list of dynastoreLog from DB by sitedId and status
	 * 
	 * @param siteId
	 * @param status
	 * @return list of dynastoreLogs
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<DynaStoreLog> getDynaStoreLogByStatus(int siteId, String status)
			throws SQLException, IOException {

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<DynaStoreLog> listOfDynaStoreLogs = new ArrayList<DynaStoreLog>();
		try {
			con = DataBaseUtil.getConnection();

			pstmt = (PreparedStatement) con.prepareStatement(SELECTBYSTATUSSQL);
			pstmt.setInt(1, siteId);
			pstmt.setString(2, status);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				DynaStoreLog dynaStoreLog = new DynaStoreLog();
				parsRs(dynaStoreLog, rs);
				listOfDynaStoreLogs.add(dynaStoreLog);

			}

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
		} finally {
			DataBaseUtil.dbCleanup(con, pstmt, rs);
		}
		return listOfDynaStoreLogs;
	}

	/**
	 * to get list of dynastoreLog from DB where opendate greater than given
	 * date, siteid, status
	 * 
	 * @param siteId
	 * @param date
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<DynaStoreLog> getDynaStoreLogByGreaterThanGivenDateAndStatus(
			int siteId, Date date, String status) throws SQLException,
			IOException {
		logger.debug("inside getDynaStoreLogByGreaterThanGivenDateAndStatus   method with siteId="
				+ siteId + " date =" + date + " status =" + status);
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<DynaStoreLog> listOfDynaStoreLogs = new ArrayList<DynaStoreLog>();
		try {
			con = DataBaseUtil.getConnection();

			pstmt = (PreparedStatement) con
					.prepareStatement(SELECTBYSTATUSANDDDATEANDSTATUSSQL);
			pstmt.setInt(1, siteId);
			pstmt.setString(2, status);
			pstmt.setDate(3, date);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				DynaStoreLog dynaStoreLog = new DynaStoreLog();
				parsRs(dynaStoreLog, rs);
				listOfDynaStoreLogs.add(dynaStoreLog);

			}

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
		} finally {
			DataBaseUtil.dbCleanup(con, pstmt, rs);
		}
		return listOfDynaStoreLogs;
	}

	/**
	 * to get list of dynastoreLog from DB siteid
	 * 
	 * @param siteId
	 * @param date
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<DynaStoreLog> getDynaStoreLogBySiteId(int siteId)
			throws SQLException, IOException {

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<DynaStoreLog> listOfDynaStoreLogs = new ArrayList<DynaStoreLog>();
		try {
			con = DataBaseUtil.getConnection();

			pstmt = (PreparedStatement) con.prepareStatement(SELECTBYSITEIDSQL);
			pstmt.setInt(1, siteId);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				DynaStoreLog dynaStoreLog = new DynaStoreLog();
				parsRs(dynaStoreLog, rs);
				listOfDynaStoreLogs.add(dynaStoreLog);

			}

		} catch (ClassNotFoundException e) {
			logger.error("Failed to Load the DB Driver", e);
		} finally {
			DataBaseUtil.dbCleanup(con, pstmt, rs);
		}
		return listOfDynaStoreLogs;
	}

	private void parsRs(DynaStoreLog dynaStoreLog, ResultSet rs)
			throws SQLException {
		dynaStoreLog.setSiteId(rs.getInt(1));
		dynaStoreLog.setSessionId(rs.getString(2));
		dynaStoreLog.setStatus(rs.getString(3));
		dynaStoreLog.setOpendDTM(rs.getDate(4));
		dynaStoreLog.setClosedDTM(rs.getDate(5));
		dynaStoreLog.setInfo(rs.getString(6));

	}

}
