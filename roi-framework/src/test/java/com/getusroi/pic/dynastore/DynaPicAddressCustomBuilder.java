package com.getusroi.pic.dynastore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.dynastore.config.DynaStoreConfigurationServiceTest;
import com.getusroi.dynastore.config.IDynaStoreCustomInitializer;
import com.getusroi.dynastore.config.impl.ConfigDynaStoreInitializationException;
import com.getusroi.dynastore.config.jaxb.DynastoreInitializer;
import com.getusroi.permastore.config.PICArea;

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
