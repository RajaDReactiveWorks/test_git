package com.getusroi.dynastore;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.RequestContext;
import com.getusroi.config.util.GenericTestConstant;
import com.getusroi.core.datagrid.DataGridService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.transaction.TransactionContext;

public class DynaStoreSessionTest {
	final Logger logger = LoggerFactory.getLogger(DynaStoreSessionTest.class);
	
	
	@Test
	public void testDynaStoreSessionDataSavingandReading() throws DynastoreLogConfigException{
		Map<String, Object> mapData = new HashMap<String, Object>();
		mapData.put("1", "One");
		mapData.put("2", "two");
		//added transactional scope for dynastore, but need to verify once
		HazelcastInstance hcIns=DataGridService.getDataGridInstance().getHazelcastInstance();
		TransactionContext hcTransactionalContext=hcIns.newTransactionContext();
		hcTransactionalContext.beginTransaction();
		RequestContext reqCtx=getRequestContext();
		reqCtx.setHcTransactionalContext(hcTransactionalContext);
		DynaStoreSession dynaSession=DynaStoreFactory.getDynaStoreSession(reqCtx,mapData);		
		String sessionId=dynaSession.getSessionId();
		logger.debug(".testDynaStoreSession() DynaSessionId="+sessionId);
		dynaSession.addSessionData("Val1", "MyData1");
		dynaSession.addSessionData("Val2", "MyData2");
		DynaStoreSession dynaSession2=DynaStoreFactory.getDynaStoreSession(reqCtx,sessionId);
		logger.debug("session id  "+dynaSession2);
		Assert.assertNotNull("Dynastore session for given RequestId should not be null");
		String val1=(String)dynaSession2.getSessionData("Val1");
		Assert.assertEquals("Data in dynastore session should be MyData1",val1,"MyData1");
		logger.debug("val1 : "+val1);
		hcTransactionalContext.commitTransaction();

		
		//clean it up
		dynaSession2.terminateSession();
	}

	@Test
	public void testDynaStoreSessionForTermination() throws DynastoreLogConfigException{
		Map<String, Object> mapData = new HashMap<String, Object>();
		mapData.put("1", "One");
		mapData.put("2", "two");
		//added transactional scope for dynastore, but need to verify once
		HazelcastInstance hcIns=DataGridService.getDataGridInstance().getHazelcastInstance();
		TransactionContext hcTransactionalContext=hcIns.newTransactionContext();
		hcTransactionalContext.beginTransaction();
		RequestContext reqCtx=getRequestContext();
		reqCtx.setHcTransactionalContext(hcTransactionalContext);
		DynaStoreSession dynaSession=DynaStoreFactory.getDynaStoreSession(reqCtx,mapData);
		String sessionId=dynaSession.getSessionId();
		logger.debug(".testDynaStoreSession() DynaSessionId="+sessionId);
		dynaSession.addSessionData("Val1", "MyData1");
		dynaSession.addSessionData("Val2", "MyData2");
		//check now
		Map sessionData=dynaSession.getAllSessionData();
		Assert.assertNotNull("Dynastore session was not terminated session map data shouldn't be null",sessionData);
		hcTransactionalContext.commitTransaction();
		// verify with Haelcast
		// Check if data is loaded in the Hazelcast or not
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap tenantUserStoreMap = hazelcastInstance.getMap("DS-" + GenericTestConstant.TEST_TENANTID);
		Map<String, Serializable> sessionMap = (Map) tenantUserStoreMap.get(sessionId);
		Assert.assertNotNull("Dynastore session was terminated map data in Hazelcast shouldn't be null", sessionMap);
		logger.debug("session Data : " + sessionMap);
		//terminate session.
		dynaSession.terminateSession();
		
		
	}
	
	private RequestContext getRequestContext(){
		RequestContext configContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),null,null,
					null);
		configContext.setRequestId(getUUID());
		return configContext;
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

}
