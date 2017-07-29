package com.getusroi.permastore.config;

import java.io.Serializable;


import com.getusroi.permastore.config.jaxb.CustomBuilder;

public class StageingAreaStringBuilder implements IPermaStoreCustomCacheObjectBuilder {

	public Serializable loadDataForCache(CustomBuilder configBuilderConfig) {
		String  val=" STA1  STA2 STA3 STA4 STA5 29";
		return (Serializable) val;
	}

}
