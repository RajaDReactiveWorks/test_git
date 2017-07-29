package com.getusroi.permastore.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.getusroi.permastore.config.jaxb.CustomBuilder;

	public class PICAddressBuilder implements IPermaStoreCustomCacheObjectBuilder{
		public Serializable loadDataForCache(CustomBuilder configBuilderConfig) {
			List<PICArea> al=new ArrayList<>();
			al.add(new PICArea("WoodPic","S3-WF-45"));
			al.add(new PICArea("LaptopPic","S22-WF1-25"));
			return (Serializable)al;
		}
		
	}

