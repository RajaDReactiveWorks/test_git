package com.attunedlabs.feature.config;

import com.attunedlabs.config.util.GenericTestConstant;

public class FeatureTestConstant extends GenericTestConstant{
	public static final String configfileToParse="featureService.xml";
	public static final String configfileToParse1="testfeature.xml";
	public static final String TEST_FEATURE_VENDOR="leap";
	public static final String TEST_FEATURE_VERSION="1.0";
	//public static final int TEST_VENDOR_NODEID=862;
	public static final int TEST_VENDOR_NODEID=1243;
	public static String TEST_FEATUREGROUP="label";
	public static String TEST_FEATURE="labelservice";
	public static int TEST_NODEID=241;
	
	//public static int TEST_NODEID1=324;
	public static int TEST_NODEID1=1220;
	
	public static String  getFeature(){
		return TEST_FEATURE;
	}
	public static int  getConfigNodeId(){
		return TEST_NODEID;
	}
	public static int  getConfigNodeId1(){
		return TEST_NODEID1;
	}
	
}
