package com.attunedlabs.dynastore.persistence;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;



public interface IDynaStorePersistenceService {
	
	
	public int insertDynastoreLog(int siteNodeId, String sessionId,String status, String info) throws DynaStorePersistenceException;
	
	public int updateDynastoreLog(int siteNodeId, String sessionId,String status) throws DynaStorePersistenceException ;

	public String getDynaStoreLog(int siteNodeId,String sessionId) throws DynaStorePersistenceException ;

/*	public void deleteDynaStoreLog() throws DynaStorePersistenceException;
*/	
	public List<DynaStoreLog> getDynaStoreLogByStatusAndOpenDate(int siteId,String status,Date openDate)  throws DynaStorePersistenceException ;
	
	public List<DynaStoreLog> getDynaStoreLogByStatusAndClosedDate(int siteId,String status,Date closedDate)  throws DynaStorePersistenceException ;

	public List<DynaStoreLog> getDynaStoreLogByStatus(int siteId,String status)  throws DynaStorePersistenceException ;
	
	public List<DynaStoreLog> getDynaStoreLogByGreaterThanGivenDate(int siteId,Date date) throws DynaStorePersistenceException;

	public List<DynaStoreLog> getDynaStoreLogBySiteId(int siteId) throws DynaStorePersistenceException;
	
	public List<DynaStoreLog> getDynaStoreLogByGreaterThanGivenDateAndStatus(int siteId,Date date,String status) throws DynaStorePersistenceException;


}
