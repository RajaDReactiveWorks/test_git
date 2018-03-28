package com.attunedlabs.permastore.config.impl;

public class PermaStoreTestConstant {
	public static String configfileToParse="PermaStoreConfig.xml";
	public static String invalidconfigfileToParse="BadPermaStoreConfig.xml";
	//Valid tree "gap","site1","featuregroup1","feature1"
	public static String TEST_TENANTID="gap";
	public static String TEST_SITEID="site1";
	public static String TEST_FEATUREGROUP="featuregroup1";
	public static String TEST_FEATURE="feature1";
	public static int TEST_NODEID=26;
	public static String  getTenant(){
		return TEST_TENANTID;
	}
	public static String  getSite(){
		return TEST_SITEID;
	}
	public static String  getFeatureGroup(){
		return TEST_FEATUREGROUP;
	}
	public static String  getFeature(){
		return TEST_FEATURE;
	}
	public static int  getConfigNodeId(){
		return 26;
	}
	
}

