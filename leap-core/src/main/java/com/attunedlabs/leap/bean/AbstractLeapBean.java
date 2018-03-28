package com.attunedlabs.leap.bean;


import org.apache.camel.Exchange;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;
import com.attunedlabs.policy.config.PolicyConfigurationException;

public abstract  class AbstractLeapBean {
	final Logger logger = LoggerFactory.getLogger(AbstractLeapBean.class);


	
	public void callGetPermaStoreConfigFromUtil(Exchange exchange,String configName) throws PermaStoreConfigRequestException, JSONException{
		logger.debug(".callGetPermaStoreConfigFromUtil() of  AbstractLeapBean");
		LeapConfigurationUtil leapConfigUtil=new LeapConfigurationUtil();
		leapConfigUtil.getPermastoreConfiguration(configName,exchange);
		
	}
	
	public void callGetPolicyConfigFromUtil(Exchange exchange,String configName) throws PermaStoreConfigRequestException, PolicyConfigurationException, JSONException{
		logger.debug(".callGetPermaStoreConfigFromUtil() of  AbstractLeapBean");
		LeapConfigurationUtil leapConfigUtil=new LeapConfigurationUtil();
		leapConfigUtil.getPolicyConfiguration(configName,exchange);
		
	}

	
}
