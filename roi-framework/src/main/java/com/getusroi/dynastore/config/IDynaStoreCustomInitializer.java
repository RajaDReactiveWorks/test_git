package com.getusroi.dynastore.config;

import java.io.Serializable;
import java.util.Map;

import com.getusroi.dynastore.config.impl.ConfigDynaStoreInitializationException;
import com.getusroi.dynastore.config.jaxb.DynastoreInitializer;

public interface IDynaStoreCustomInitializer {
	public Map<String,Serializable> initializeDynastoreWithData(DynastoreInitializer configBuilderConfig)throws ConfigDynaStoreInitializationException;
}
