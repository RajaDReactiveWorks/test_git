package com.attunedlabs.dynastore.config;

import java.io.Serializable;
import java.util.Map;

import com.attunedlabs.dynastore.config.impl.ConfigDynaStoreInitializationException;
import com.attunedlabs.dynastore.config.jaxb.DynastoreInitializer;

public interface IDynaStoreCustomInitializer {
	public Map<String,Serializable> initializeDynastoreWithData(DynastoreInitializer configBuilderConfig)throws ConfigDynaStoreInitializationException;
}
