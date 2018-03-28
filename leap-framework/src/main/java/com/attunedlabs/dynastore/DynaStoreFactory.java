package com.attunedlabs.dynastore;

import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.dynastore.persistence.DynaStorePersistenceException;
import com.attunedlabs.dynastore.persistence.IDynaStorePersistenceService;
import com.attunedlabs.dynastore.persistence.impl.DynaStorePersistenceServiceMySqlImpl;



public class DynaStoreFactory {
	
	final static Logger logger=LoggerFactory.getLogger(DynaStoreFactory.class);
	
	/**
	 * To Get New dynastoreSession Object
	 * @param tenantId
	 * @param siteId
	 * @param mapData
	 * @return DynaStoreSession Object
	 * @throws IOException 
	 * @throws DynastoreLogConfigException 
	 */
	public static DynaStoreSession getDynaStoreSession(RequestContext req,Map mapData) throws DynastoreLogConfigException{
		DynaStoreSession dynaSession=new DynaStoreSession(req,mapData);
		return dynaSession;
	}
	
/**
 * To get Existing dynastoreSession Object
 * @param tenantId
 * @param siteId
 * @param sessionId
 * @return DynaStoreSession
 */
	public static DynaStoreSession getDynaStoreSession(RequestContext req,String sessionId){
		DynaStoreSession dynaSession=new DynaStoreSession(req,sessionId);
		return dynaSession;
	}
	
	/**
	 * To Get new dynastoreSession Object for given session Id 
	 * @param tenantId
	 * @param siteId
	 * @param sessionId
	 * @param mapData
	 * @return DynaStoreSession Object
	 * @throws IOException 
	 * @throws DynastoreLogConfigException 
	 */
	public static DynaStoreSession getDynaStoreSession(RequestContext req,String sessionId,Map mapData) throws DynastoreLogConfigException{
		DynaStoreSession dynaSession=new DynaStoreSession(req,sessionId,mapData);
		return dynaSession;
	}
	
	
}
