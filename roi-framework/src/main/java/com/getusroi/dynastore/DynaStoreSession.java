package com.getusroi.dynastore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.RequestContext;
import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.InvalidNodeTreeException;
import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.getusroi.core.datagrid.DataGridService;
import com.getusroi.dynastore.persistence.DynaStorePersistenceException;
import com.getusroi.dynastore.persistence.IDynaStorePersistenceService;
import com.getusroi.dynastore.persistence.impl.DynaStorePersistenceServiceMySqlImpl;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;


/**
 * @author Bizruntime
 * DynaStoreSession  is wrapper on top of HazelCast DataStore abstract the way data is stored.<br>
 * DynaStoreSession is using the HazelCast IMap to Store the Data<br>
 * IMap-Key is "US-tenantId" that is one dynastore created per tenant<br>
 * within it There is Map with key as the session Id.<br>
 */
public class DynaStoreSession implements IDynaStoreSession{

	final static public String SESSION_OPENED_STATUS="Opened";
	final static public String SESSION_CLOSED_STATUS="Closed";
	final static public String DYNA_LOG_ENABLED_STATUS_KEY = "enablelogs";
	final static public String DYNA_LOG_PROPERTIES = "dynastorelog.properties";
	protected static final Logger logger = LoggerFactory.getLogger(DynaStoreSession.class);
	static boolean isLogEnabled;
	/**
	 * To load the properties once
	 */
	static {
	    Properties props = new Properties();
	    InputStream inputStream = DynaStoreSession.class.getClassLoader().getResourceAsStream(DYNA_LOG_PROPERTIES);
	    if (inputStream != null) {
				try {
					props.load(inputStream);
					isLogEnabled = Boolean.parseBoolean((String) props.get(DYNA_LOG_ENABLED_STATUS_KEY));
				} catch (IOException e) {
					logger.error("Error in loading the DynastoreLog properties: "+e);
				}
	  }}// ..end of static propertiesLoader
	
	private HazelcastInstance hazelcastInstance;
	private String sessionId;
	private String tenantId;
	private String siteId;
	private RequestContext req;
	public String getSessionId() {
		return sessionId;
	}

	public String getTenantId() {
		return tenantId;
	}
	/**
	 * intailizing new sessionId , tenantId,siteId,mapData and store dynastoreSessionLog To DB
	 * @param tenantId
	 * @param siteId
	 * @param mapData
	 * @throws IOException 
	 * @throws DynastoreLogConfigException 
	 */
	public DynaStoreSession(RequestContext req,Map mapData) throws DynastoreLogConfigException{
		this.hazelcastInstance=DataGridService.getDataGridInstance().getHazelcastInstance();
		this.sessionId=getUUID();
		this.tenantId=req.getTenantId();
		this.siteId=req.getSiteId();
		this.req=req;
		if(isLogEnabled){
		storeDynastoreLog(tenantId, siteId, mapData, sessionId);
		}
	}
	
	/**
	 * intailizing  Existing sessionId , hazelcast instance,siteId,tenantId
	 * @param tenantId
	 * @param siteId
	 * @param sessionId
	 */
	public DynaStoreSession(RequestContext req,String sessionId){
		this.hazelcastInstance=DataGridService.getDataGridInstance().getHazelcastInstance();
		this.sessionId=sessionId;
		this.tenantId=req.getTenantId();
		this.siteId=req.getSiteId();
		this.req=req;

	}
	/**
	 * Intailizing  given sessionId and store dynastoreSessionLogTo Db
	 * @param tenantId
	 * @param siteId
	 * @param sessionId
	 * @param mapData
	 * @throws IOException 
	 * @throws DynastoreLogConfigException 
	 */
	public DynaStoreSession(RequestContext req,String sessionId,Map mapData) throws DynastoreLogConfigException{
		this.hazelcastInstance=DataGridService.getDataGridInstance().getHazelcastInstance();
		this.sessionId=sessionId;
		this.tenantId=req.getTenantId();
		this.siteId=req.getSiteId();
		this.req=req;
		if(isLogEnabled){
		storeDynastoreLog(tenantId, siteId, mapData, sessionId);
		}

	}
	
	/**
	 * To get  all sessionData 
	 * @return map 
	 */
	public Map getAllSessionData(){
		TransactionContext hcTransactionalContext=req.getHcTransactionalContext();
		logger.debug("hcTransactionalContext : "+hcTransactionalContext);
		TransactionalMap<String,Serializable>  dynaMap=hcTransactionalContext.getMap(getUserStoreKey());
		logger.debug("getSessionData() tenantUserStoreMap="+dynaMap);
		Map<String,Serializable> sessionMap=(Map)dynaMap.get(getSessionKey());		
		return sessionMap;
	}
	
	/**
	 * terminating Session and storing TeminationSessionLOg
	 */
	public void terminateSession(){
		logger.debug("inside terminationsession Mathod ");
		IMap tenantUserStoreMap=hazelcastInstance.getMap(getUserStoreKey());
		tenantUserStoreMap.remove(getSessionKey());
		updateTerminateSessionDyanaStoreLog(tenantId, siteId, sessionId);
		
	}
	
	/**
	 * To  add Session data 
	 * @param key
	 * @param value
	 */
	public void addSessionData(String key,Serializable value){
		logger.debug("reqContext in addSssion: "+req);
		TransactionContext hcTransactionalContext=req.getHcTransactionalContext();
		logger.debug("hcTransactionalContext : "+hcTransactionalContext);
		TransactionalMap<String,Serializable>  dynaMap=hcTransactionalContext.getMap(getUserStoreKey());
		logger.debug("addSessionData() tenantUserStoreMap="+dynaMap);
		Map<String,Serializable> sessionMap=(Map)dynaMap.get(getSessionKey());	
		logger.debug("addSessionData() sessionMap="+sessionMap);		
		if(sessionMap==null){
			sessionMap=new HashMap<String,Serializable>();
			sessionMap.put(key, value);
			dynaMap.put(getSessionKey(), (Serializable) sessionMap);		
		}else{
			sessionMap.put(key, value);
			dynaMap.put(getSessionKey(), (Serializable) sessionMap);
		}
		
	}
	
	public Serializable getSessionData(String key){
		TransactionContext hcTransactionalContext=req.getHcTransactionalContext();
		logger.debug("hcTransactionalContext : "+hcTransactionalContext);
		TransactionalMap<String,Serializable>  dynaMap=hcTransactionalContext.getMap(getUserStoreKey());
		logger.debug("getSessionData() tenantUserStoreMap="+dynaMap);
		Map<String,Serializable> sessionMap=(Map)dynaMap.get(getSessionKey());	
		return sessionMap.get(key);
	}
	
	public void removeSessionData(String key){
		IMap tenantUserStoreMap=hazelcastInstance.getMap(getUserStoreKey());
		Map<String,Serializable> sessionMap=(Map)tenantUserStoreMap.get(getSessionKey());
		if(sessionMap!=null){
			sessionMap.remove(key);
			tenantUserStoreMap.put(getSessionKey(), (Serializable) sessionMap);
		}
	}
	public void updateSessionData(String key,Serializable value){
		TransactionContext hcTransactionalContext=req.getHcTransactionalContext();
		logger.debug("hcTransactionalContext : "+hcTransactionalContext);
		TransactionalMap<String,Serializable>  dynaMap=hcTransactionalContext.getMap(getUserStoreKey());
		logger.debug("getSessionData() tenantUserStoreMap="+dynaMap);
		Map<String,Serializable> sessionMap=(Map)dynaMap.get(getSessionKey());		
		if(sessionMap!=null){
			sessionMap.put(key, value);
			dynaMap.put(key, value);
		}else{
			addSessionData(key,value);
		}
	}
	
	private String getUserStoreKey(){
		//DS=DynaStore
		return "DS-"+tenantId;
	}
	private String getSessionKey(){
		return sessionId;
	}
	
	
	private void getPolicy(){
		//Hardcoding as Policy Store and Framework is not ready yet
		DynaStoreCachePolicy policy=new DynaStoreCachePolicy(100);
	}
	
	private String getUUID(){
		//Not using UUID class as UID is too long as a Map Key
		char[] chars = "abcdefghijklmnopqrstuvwxyzABSDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
		Random r = new Random(System.currentTimeMillis());
		char[] id = new char[12];
		for (int i = 0;  i < 12;  i++) {
		    id[i] = chars[r.nextInt(chars.length)];
		}
		logger.debug("UUID is "+new String(id));
		return new String(id);
	}
	
	/**
	 * AS map contains reference to other objects on the heap its quite difficult to calculate the size
	 * The way is objectOutputStream although it will be little expensive from performance point of view.
	 * @param map
	 * @return
	 */
	private Long getMapMemorySize(Map map){
		try{		        
		        ByteArrayOutputStream baos=new ByteArrayOutputStream();
		        ObjectOutputStream oos=new ObjectOutputStream(baos);
		        oos.writeObject(map);
		        oos.close();
		        long memorySize=baos.size();
		        logger.debug("sessionId="+sessionId+"Data Size: " + baos.size());
		        return memorySize;
		    }catch(IOException e){
		        e.printStackTrace();
		    }
		return 0L;
	}

	/**
	 * getting Dynatsore sessio Id and Inserting  Log to DB
	 * @param tenantId
	 * @param siteId
	 * @param mapData
	 * @return DynaStoreSession
	 */
	private void storeDynastoreLog(String tenantId,String siteId,Map mapData,String sessionId){
		logger.debug("Inside getDynaStoreSession session mathod with siteId = "+siteId+" Map data = "+mapData);
		IConfigPersistenceService iConfigPersistenceService=new ConfigPersistenceServiceMySqlImpl();
		IDynaStorePersistenceService iDynaStorePersistenceService=new DynaStorePersistenceServiceMySqlImpl();

		//converting Map to json
		String	 mapToJson=null;
		try{
		if(mapData!=null&&!mapData.isEmpty()){
			JSONObject	mapToJsonObject=new JSONObject(mapData);
			mapToJson=mapToJsonObject.toString();
		}
		}catch(Exception e){
			logger.error("error in converting map to json"+e);
		}
		try {
			//to get siteNodeId
			int siteNodeId=iConfigPersistenceService.getApplicableNodeId(tenantId, siteId);
			//storeing the DynastoreSession Log to DB 
			iDynaStorePersistenceService.insertDynastoreLog(siteNodeId,sessionId, SESSION_OPENED_STATUS, mapToJson);
		} catch (ConfigPersistenceException | InvalidNodeTreeException | DynaStorePersistenceException e) {
			logger.error("error in storing DyanstoreLog data To DB with tenantId= "+tenantId +" site= "+siteId +" sessionId = "+sessionId,e);
		}
		
	}
	
	/**
	 * Terminate session and storeDynastoreLog To DB 
	 * @param tenantId
	 * @param siteId
	 * @param sessionId
	 */
	private  void updateTerminateSessionDyanaStoreLog(String tenantId,String siteId,String sessionId){		
		logger.debug("inside updateterminateSessionDyanaStoreLog method  with tenant Name = "+tenantId +" sitename = "+siteId  +" sessionId = "+sessionId);
		IConfigPersistenceService iConfigPersistenceService=new ConfigPersistenceServiceMySqlImpl();
		IDynaStorePersistenceService iDynaStorePersistenceService=new DynaStorePersistenceServiceMySqlImpl();
		try {
			//to get siteNodeId
			int siteNodeId=iConfigPersistenceService.getApplicableNodeId(tenantId, siteId);
			//storeing the DynastoreSession Log to DB 
			iDynaStorePersistenceService.updateDynastoreLog(siteNodeId, sessionId, SESSION_CLOSED_STATUS);
		} catch ( ConfigPersistenceException | InvalidNodeTreeException | DynaStorePersistenceException e) {
			logger.error("error in storing DyanstoreLog data To DB with tenantId= "+tenantId +" site= "+siteId +" sessionId = "+sessionId,e);
		}
		
	}
	
	
}
