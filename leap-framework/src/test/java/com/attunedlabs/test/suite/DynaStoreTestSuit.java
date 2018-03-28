package com.attunedlabs.test.suite;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.util.ConfigurationTestUtil;
import com.attunedlabs.dynastore.DynaStoreSessionTest;
import com.attunedlabs.dynastore.config.ConfigurableDynaStoreSessionTest;
import com.attunedlabs.dynastore.config.DynaStoreConfigXmlParserTest;
import com.attunedlabs.dynastore.config.DynaStoreConfigurationServiceTest;
import com.attunedlabs.dynastore.config.DynaStoreInitializationTest;



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
