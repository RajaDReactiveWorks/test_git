package com.getusroi.test.suite;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.util.ConfigurationTestUtil;
import com.getusroi.eventframework.config.EventFrameworkConfigServiceTest;
import com.getusroi.eventframework.config.EventFrameworkConfigXMLParserTest;
import com.getusroi.eventframework.dispatcher.chanel.DispatchChanelsTest;
import com.getusroi.eventframework.event.ROIEventServiceTest;



@RunWith(Suite.class)
@Suite.SuiteClasses({
	EventFrameworkConfigXMLParserTest.class,
	EventFrameworkConfigServiceTest.class,
	DispatchChanelsTest.class,
	ROIEventServiceTest.class
	
	})
public class EventingTestSuit {
	final static Logger logger = LoggerFactory.getLogger(EventingTestSuit.class);
	
	@BeforeClass
	public static void init() throws ConfigPersistenceException {
		logger.debug("Inside init() of RoiTestSuite to check Test tree exist or not");
		ConfigurationTestUtil.checkAndAddTestNode();
		}
}
