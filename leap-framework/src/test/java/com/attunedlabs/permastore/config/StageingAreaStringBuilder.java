package com.attunedlabs.permastore.config;

import java.io.Serializable;

import com.attunedlabs.permastore.config.IPermaStoreCustomCacheObjectBuilder;
import com.attunedlabs.permastore.config.jaxb.CustomBuilder;

public class StageingAreaStringBuilder implements IPermaStoreCustomCacheObjectBuilder {

	public Serializable loadDataForCache(CustomBuilder configBuilderConfig) {
		String  val=" STA1  STA2 STA3 STA4 STA5 29";
		return (Serializable) val;
	}

}
