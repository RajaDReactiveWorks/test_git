package com.attunedlabs.dynastore.persistence.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.attunedlabs.dynastore.config.impl.ConfigDynaStoreInitializationException;
import com.attunedlabs.dynastore.persistence.DynaStoreLog;
import com.attunedlabs.dynastore.persistence.DynaStorePersistenceException;
import com.attunedlabs.dynastore.persistence.IDynaStorePersistenceService;
import com.attunedlabs.dynastore.persistence.dao.DynastoreLogDAO;

public class DynaStorePersistenceServiceMySqlImpl implements IDynaStorePersistenceService {

	private DynastoreLogDAO dynastoreLogDAO;

	public DynaStorePersistenceServiceMySqlImpl() {
		dynastoreLogDAO = new DynastoreLogDAO();
	}

	public int insertDynastoreLog(int siteNodeId, String sessionId, String status, String info)
			throws DynaStorePersistenceException {
		int isInserted = 0;

		try {
			isInserted = dynastoreLogDAO.insertDynastoreLog(siteNodeId, sessionId, status, info);
		} catch (ConfigDynaStoreInitializationException e) {
			throw new DynaStorePersistenceException(
					"Failed insert into DynastoreLog with siteNodeId = " + siteNodeId + " sessionID= " + sessionId);
		}

		return isInserted;
	}

	public int updateDynastoreLog(int siteNodeId, String sessionId, String status)
			throws DynaStorePersistenceException {
		int isUpdated = 0;

		try {
			isUpdated = dynastoreLogDAO.updateDynastoreLog(siteNodeId, sessionId, status);
		} catch (ConfigDynaStoreInitializationException e) {
			throw new DynaStorePersistenceException(
					"Failed updateDynastoreLog with siteNodeId = " + siteNodeId + " sessionID= " + sessionId);

		}
		return isUpdated;
	}

	@Override
	public String getDynaStoreLog(int siteNodeId, String sessionId) throws DynaStorePersistenceException {
		String status = "";
		try {
			status = dynastoreLogDAO.getDynaStoreLog(siteNodeId, sessionId);
		} catch (ConfigDynaStoreInitializationException e) {
			// TODO Auto-generated catch block
			throw new DynaStorePersistenceException("Failed to get DynastorelOg Details ");

		}
		return status;
	}

	/*
	 * @Override public void deleteDynaStoreLog() throws
	 * DynaStorePersistenceException { try {
	 * dynastoreLogDAO.deleteDynaStoreLog(); } catch (SQLException | IOException
	 * e) { throw new
	 * DynaStorePersistenceException("Failed to Delete Dynastore Log "); } }
	 */
	@Override
	public List<DynaStoreLog> getDynaStoreLogByStatusAndOpenDate(int siteId, String status, Date openedDate)
			throws DynaStorePersistenceException {
		ArrayList<DynaStoreLog> listOfDynaStoreLogs = null;

		try {
			listOfDynaStoreLogs = (ArrayList<DynaStoreLog>) dynastoreLogDAO.getDynaStoreLogByStatusAndOpenDate(siteId,
					status, openedDate);
		} catch (ConfigDynaStoreInitializationException e) {
			throw new DynaStorePersistenceException("Failed to Get Dynastore Log LIST with status=" + status
					+ " and openDate=" + openedDate + " siteId=" + siteId);
		}

		return listOfDynaStoreLogs;
	}

	@Override
	public List<DynaStoreLog> getDynaStoreLogByStatusAndClosedDate(int siteId, String status, Date closedDate)
			throws DynaStorePersistenceException {
		ArrayList<DynaStoreLog> listOfDynaStoreLogs = null;

		try {
			listOfDynaStoreLogs = (ArrayList<DynaStoreLog>) dynastoreLogDAO.getDynaStoreLogByStatusAndClosedDate(siteId,
					status, closedDate);
		} catch (ConfigDynaStoreInitializationException e) {
			throw new DynaStorePersistenceException("Failed to Get Dynastore Log LIST with status=" + status
					+ " and closedDate=" + closedDate + " siteId=" + siteId);
		}

		return listOfDynaStoreLogs;
	}

	@Override
	public List<DynaStoreLog> getDynaStoreLogByStatus(int siteId, String status) throws DynaStorePersistenceException {
		ArrayList<DynaStoreLog> listOfDynaStoreLogs = null;

		try {
			listOfDynaStoreLogs = (ArrayList<DynaStoreLog>) dynastoreLogDAO.getDynaStoreLogByStatus(siteId, status);
		} catch (ConfigDynaStoreInitializationException e) {
			throw new DynaStorePersistenceException(
					"Failed to Get Dynastore Log LIST with status=" + status + " and siteId=" + siteId);
		}

		return listOfDynaStoreLogs;
	}

	@Override
	public List<DynaStoreLog> getDynaStoreLogByGreaterThanGivenDate(int siteId, Date date)
			throws DynaStorePersistenceException {
		ArrayList<DynaStoreLog> listOfDynaStoreLogs = null;

		try {
			listOfDynaStoreLogs = (ArrayList<DynaStoreLog>) dynastoreLogDAO
					.getDynaStoreLogByGreaterThanGivenDate(siteId, date);
		} catch (ConfigDynaStoreInitializationException e) {
			// TODO Auto-generated catch block
			throw new DynaStorePersistenceException(
					"Failed to Get Dynastore Log LIST with date=" + date + " and siteID=" + siteId);
		}

		return listOfDynaStoreLogs;
	}

	@Override
	public List<DynaStoreLog> getDynaStoreLogBySiteId(int siteId) throws DynaStorePersistenceException {

		ArrayList<DynaStoreLog> listOfDynaStoreLogs = null;

		try {
			listOfDynaStoreLogs = (ArrayList<DynaStoreLog>) dynastoreLogDAO.getDynaStoreLogBySiteId(siteId);
		} catch (ConfigDynaStoreInitializationException e) {
			// TODO Auto-generated catch block
			throw new DynaStorePersistenceException("Failed to Get Dynastore Log LIST with siteNodeId=" + siteId);
		}
		return listOfDynaStoreLogs;
	}

	@Override
	public List<DynaStoreLog> getDynaStoreLogByGreaterThanGivenDateAndStatus(int siteId, Date date, String status)
			throws DynaStorePersistenceException {
		ArrayList<DynaStoreLog> listOfDynaStoreLogs = null;

		try {
			listOfDynaStoreLogs = (ArrayList<DynaStoreLog>) dynastoreLogDAO
					.getDynaStoreLogByGreaterThanGivenDateAndStatus(siteId, date, status);
		} catch (ConfigDynaStoreInitializationException e) {
			// TODO Auto-generated catch block
			throw new DynaStorePersistenceException("Failed to Get Dynastore Log LIST with siteNodeId=" + siteId);
		}
		return listOfDynaStoreLogs;
	}

}
