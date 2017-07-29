package com.getusroi.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.getusroi.feature.config.*;



@RunWith(Suite.class)
@Suite.SuiteClasses({
	FeatureConfigXMLParserTest.class,
	FeatureConfigurationServiceTest.class
	})
public class FeatureTestSuit {

}
