package com.getusroi.test.suite;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.util.ConfigurationTestUtil;
import com.getusroi.dynastore.DynaStoreSessionTest;
import com.getusroi.dynastore.config.ConfigurableDynaStoreSessionTest;
import com.getusroi.dynastore.config.DynaStoreConfigXmlParserTest;
import com.getusroi.dynastore.config.DynaStoreConfigurationServiceTest;
import com.getusroi.dynastore.config.DynaStoreInitializationTest;



@RunWith(Suite.class)
@Suite.SuiteClasses({
	DynaStoreSessionTest.class,
	DynaStoreConfigXmlParserTest.class,
	ConfigurableDynaStoreSessionTest.class,
	DynaStoreConfigurationServiceTest.class,
	DynaStoreInitializationTest.class
	})
public class DynaStoreTestSuit {
	final static Logger logger = LoggerFactory.getLogger(DynaStoreTestSuit.class);
	
	@BeforeClass
	public static void init() throws ConfigPersistenceException {
		logger.debug("Inside init() of RoiTestSuite to check Test tree exist or not");
		ConfigurationTestUtil.checkAndAddTestNode();
		}
}
