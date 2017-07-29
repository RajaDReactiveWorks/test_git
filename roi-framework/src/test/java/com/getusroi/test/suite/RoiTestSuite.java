package com.getusroi.test.suite;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.persistence.TenantConfigTreeServiceTest;
import com.getusroi.config.util.ConfigurationTestUtil;
import com.getusroi.dynastore.DynaStoreSessionTest;
import com.getusroi.permastore.config.PermaStoreConfigXMLParserTest;
import com.getusroi.permastore.config.PermaStoreConfigurationServiceTest;
import com.getusroi.permastore.config.PermaStoreInLineCacheObjectBuilderTest;
import com.getusroi.permastore.policy.persistence.dao.ConfigNodeDAOTest;
import com.getusroi.permastore.policy.persistence.dao.ConfigNodeDataDAOTest;
import com.getusroi.policy.config.PolicyConfigXMLParserTest;
import com.getusroi.policy.config.PolicyConfigurationServiceTest;
import com.getusroi.policy.config.PolicyConfigurationUnitBuilderTest;
import com.getusroi.policy.config.PolicyREGEXExpressionBuilderTest;
import com.getusroi.policy.config.PolicySQLDialectExpressionBuilderTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({

	ConfigNodeDAOTest.class,
	ConfigNodeDataDAOTest.class,
	TenantConfigTreeServiceTest.class,	
	PermaStoreConfigurationServiceTest.class,
	PermaStoreConfigXMLParserTest.class,
	PermaStoreInLineCacheObjectBuilderTest.class,
	PolicyConfigurationServiceTest.class, 
	PolicyConfigurationUnitBuilderTest.class,
	PolicyConfigXMLParserTest.class,
	PolicySQLDialectExpressionBuilderTest.class,
	DynaStoreSessionTest.class,
	PolicyREGEXExpressionBuilderTest.class
	})
public class RoiTestSuite {
	final static Logger logger = LoggerFactory.getLogger(RoiTestSuite.class);

	@BeforeClass
	public static void init() throws ConfigPersistenceException {
		logger.debug("Inside init() of RoiTestSuite to check Test tree exist or not");
		ConfigurationTestUtil.checkAndAddTestNode();
		}
	
}
