package com.attunedlabs.permastore.config;

import java.io.Serializable;

import com.attunedlabs.permastore.config.jaxb.CustomBuilder;

/**
 * 
 * Implementation of this Interface are responsible for loading the data
 * from the source like DB/NOSQL/XML/CSV etc 
 * @author amits@bizruntime
 *
 */
public interface IPermaStoreCustomCacheObjectBuilder {
	/**
	 * Loads data from the source  DB/NOSQL/XML/CSV and returns it as an object to be cached
	 * @param configBuilder
	 * @return Serializable object to be cached
	 */
	public Serializable loadDataForCache(CustomBuilder configBuilderConfig);
}
