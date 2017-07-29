package com.getusroi.test.suite;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RoiTestSuiteRunner {
	final static Logger logger = LoggerFactory.getLogger(RoiTestSuiteRunner.class);

	public static void main(String[] args) {

		Result result = JUnitCore.runClasses(RoiTestSuite.class,FeatureTestSuit.class);
		for (Failure fail : result.getFailures()) {
			logger.debug(fail.toString());
		}
		if (result.wasSuccessful()) {
			logger.debug("All tests finished successfully...");
		}
	}
	
	
}
