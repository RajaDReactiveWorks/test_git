package com.attunedlabs.test.suite;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.TenantConfigTreeServiceTest;
import com.attunedlabs.config.util.ConfigurationTestUtil;
import com.attunedlabs.dynastore.DynaStoreSessionTest;
import com.attunedlabs.permastore.config.PermaStoreConfigXMLParserTest;
import com.attunedlabs.permastore.config.PermaStoreConfigurationServiceTest;
import com.attunedlabs.permastore.config.PermaStoreInLineCacheObjectBuilderTest;
import com.attunedlabs.permastore.policy.persistence.dao.ConfigNodeDAOTest;
import com.attunedlabs.permastore.policy.persistence.dao.ConfigNodeDataDAOTest;
import com.attunedlabs.policy.config.PolicyConfigXMLParserTest;
import com.attunedlabs.policy.config.PolicyConfigurationServiceTest;
import com.attunedlabs.policy.config.PolicyConfigurationUnitBuilderTest;
import com.attunedlabs.policy.config.PolicyREGEXExpressionBuilderTest;
import com.attunedlabs.policy.config.PolicySQLDialectExpressionBuilderTest;

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
