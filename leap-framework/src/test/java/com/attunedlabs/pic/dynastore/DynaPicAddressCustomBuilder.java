package com.attunedlabs.pic.dynastore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.dynastore.config.DynaStoreConfigurationServiceTest;
import com.attunedlabs.dynastore.config.IDynaStoreCustomInitializer;
import com.attunedlabs.dynastore.config.impl.ConfigDynaStoreInitializationException;
import com.attunedlabs.dynastore.config.jaxb.DynastoreInitializer;
import com.attunedlabs.permastore.config.PICArea;

public class DynaPicAddressCustomBuilder implements IDynaStoreCustomInitializer{
	final Logger logger = LoggerFactory.getLogger(DynaPicAddressCustomBuilder.class);
	
	public Map<String,Serializable> initializeDynastoreWithData(DynastoreInitializer configBuilderConfig)throws ConfigDynaStoreInitializationException {
		Map<String,Serializable> picMap=new HashMap();
		
		picMap.put("WoodPic",new PICArea("WoodPic","S3-WF-45"));
		picMap.put("LaptopPic",new PICArea("LaptopPic","S22-WF1-25"));
		picMap.put("LaptopAdaptorPic",new PICArea("LaptopAdaptorPic","S32-WF1-25"));
		logger.debug(".initializeDynastoreWithData() map is "+picMap);
		return picMap;
	}

}
