package com.getusroi.permastore.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.getusroi.permastore.config.jaxb.CustomBuilder;

	public class StageingAreaListBuilder implements IPermaStoreCustomCacheObjectBuilder{
		public Serializable loadDataForCache(CustomBuilder configBuilderConfig) {
			List<String> al=new ArrayList();
			al.add("STA1");al.add("STA2");al.add("STA3");al.add("STA4");al.add("STA5");
			return (Serializable)al;
		}
		
	}

