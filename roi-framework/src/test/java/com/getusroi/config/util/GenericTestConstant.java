package com.getusroi.config.util;

/**
 * This class is for Generic Test Constant used in all test class
 * @author bizruntime
 *
 */
public class GenericTestConstant {

		//Valid tree "gap","site1","featuregroup1","feature1"
		public static String TEST_TENANTID="gap";
		public static String TEST_SITEID="site1";
		public static String TEST_FEATUREGROUP="featuregroup1";
		public static String TEST_FEATURE="feature1";
		public static String TEST_IMPL_NAME="bizruntime";
		public static final  String TEST_VENDOR="feature1vendor";
		public static final String TEST_VERSION="1.0";
		//public static int TEST_NODEID=26;
		//public static int TEST_NODEID=1225;
		public static int TEST_NODEID=1230;
		//public static int TEST_SITE_NODEID=23;
		public static int TEST_SITE_NODEID=23;
		//public static int TEST_FEATUREGROUP_NODEID=25;
		//public static int TEST_FEATUREGROUP_NODEID=1223;
		public static int TEST_FEATUREGROUP_NODEID=1228;
		//public static int TEST_VENDOR_NODEID=861;
		//public static int TEST_VENDOR_NODEID=1226;
		public static int TEST_VENDOR_NODEID=1231;
		public static String TEST_REQUEST_ID="123456789";
		
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
			return TEST_NODEID;
		}
		public static int  getSiteConfigNodeId(){
			return TEST_SITE_NODEID;
		}
		public static int  getFeatureGroupConfigNodeId(){
			return TEST_FEATUREGROUP_NODEID;
		}
		public static int  getFeatureConfigNodeId(){
			return TEST_NODEID;
		}
		
		public static int getVendorConfigNodeId() {
			return TEST_VENDOR_NODEID;
		}
		
		
}
