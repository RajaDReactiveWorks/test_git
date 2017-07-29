package com.getusroi.dynastore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.RequestContext;
import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.IConfigPersistenceService;
import com.getusroi.config.persistence.InvalidNodeTreeException;
import com.getusroi.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.getusroi.config.util.GenericTestConstant;
import com.getusroi.dynastore.persistence.DynaStorePersistenceException;
import com.getusroi.dynastore.persistence.IDynaStorePersistenceService;
import com.getusroi.dynastore.persistence.impl.DynaStorePersistenceServiceMySqlImpl;

public class DynaStoreLogTest {
	final Logger logger = LoggerFactory.getLogger(DynaStoreLogTest.class);

	IDynaStorePersistenceService ipDynaStorePersistenceService = new DynaStorePersistenceServiceMySqlImpl();

	IConfigPersistenceService perService;

	@Before
	public void initializeTheDataGrid() throws ConfigPersistenceException {
		perService = new ConfigPersistenceServiceMySqlImpl();
		perService.getConfigPolicyNodeTree();
		/*
		 * try { ipDynaStorePersistenceService.deleteDynaStoreLog(); } catch
		 * (DynaStorePersistenceException e) { // TODO Auto-generated catch
		 * block logger.error("error delete dynastore Log"); }
		 */
	}

	@Test
	public void testDynaStoreSessionAndLog() throws DynastoreLogConfigException, InvalidNodeTreeException,
			ConfigPersistenceException, DynaStorePersistenceException {
		Map<String, Object> mapData = new HashMap<String, Object>();
		mapData.put("1", "One");
		mapData.put("2", "two");
		DynaStoreSession dynaStoreSession = DynaStoreFactory.getDynaStoreSession(getRequestContext(), mapData);
		Assert.assertNotNull("SessionId Should Not be null ", dynaStoreSession.getSessionId());
		
	}

	// @Test
	public void testDynaStoreSessionCreationAndTerminationLog() throws InvalidNodeTreeException,
			ConfigPersistenceException, DynaStorePersistenceException, DynastoreLogConfigException {

		Map<String, Object> mapData = new HashMap<String, Object>();
		mapData.put("1", "One");
		mapData.put("2", "two");

		DynaStoreSession dynaStoreSession = DynaStoreFactory.getDynaStoreSession(getRequestContext(), mapData);
		Assert.assertNotNull("SessionId Should Not be null ", dynaStoreSession.getSessionId());
		int siteNodeId = perService.getApplicableNodeId(GenericTestConstant.TEST_TENANTID,
				GenericTestConstant.TEST_SITEID);
		Assert.assertTrue(siteNodeId > 0);
		String dynastoreSessionStatus = ipDynaStorePersistenceService.getDynaStoreLog(siteNodeId,
				dynaStoreSession.getSessionId());
		Assert.assertNotNull("status should not Be null ", dynastoreSessionStatus);
		Assert.assertEquals(DynaStoreSession.SESSION_OPENED_STATUS, dynastoreSessionStatus);

		dynaStoreSession.terminateSession();
		dynastoreSessionStatus = ipDynaStorePersistenceService.getDynaStoreLog(siteNodeId,
				dynaStoreSession.getSessionId());
		Assert.assertEquals(DynaStoreSession.SESSION_CLOSED_STATUS, dynastoreSessionStatus);
		dynaStoreSession = DynaStoreFactory.getDynaStoreSession(getRequestContext(), dynaStoreSession.getSessionId());
		Assert.assertNotNull("sessionId  should Be null ", dynaStoreSession.getSessionId());

	}

	private RequestContext getRequestContext() {
		RequestContext configContext = new RequestContext(GenericTestConstant.getTenant(),
				GenericTestConstant.getSite(), GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),GenericTestConstant.TEST_IMPL_NAME);
		configContext.setRequestId(getUUID());
		return configContext;
	}

	private String getUUID() {
		// Not using UUID class as UID is too long as a Map Key
		char[] chars = "abcdefghijklmnopqrstuvwxyzABSDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
		Random r = new Random(System.currentTimeMillis());
		char[] id = new char[12];
		for (int i = 0; i < 12; i++) {
			id[i] = chars[r.nextInt(chars.length)];
		}
		logger.debug("UUID is " + new String(id));
		return new String(id);
	}

}
