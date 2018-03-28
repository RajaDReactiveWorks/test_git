package com.attunedlabs.test.suite;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.util.ConfigurationTestUtil;
import com.attunedlabs.eventframework.config.EventFrameworkConfigServiceTest;
import com.attunedlabs.eventframework.config.EventFrameworkConfigXMLParserTest;
import com.attunedlabs.eventframework.dispatcher.chanel.DispatchChanelsTest;
import com.attunedlabs.eventframework.event.LeapEventServiceTest;



@RunWith(Suite.class)
@Suite.SuiteClasses({
	EventFrameworkConfigXMLParserTest.class,
	EventFrameworkConfigServiceTest.class,
	DispatchChanelsTest.class,
	LeapEventServiceTest.class
	
	})
public class EventingTestSuit {
	final static Logger logger = LoggerFactory.getLogger(EventingTestSuit.class);
	
	@BeforeClass
	public static void init() throws ConfigPersistenceException {
		logger.debug("Inside init() of RoiTestSuite to check Test tree exist or not");
		ConfigurationTestUtil.checkAndAddTestNode();
		}
}
