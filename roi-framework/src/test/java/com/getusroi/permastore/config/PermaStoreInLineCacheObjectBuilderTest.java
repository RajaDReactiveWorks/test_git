package com.getusroi.permastore.config;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.permastore.config.PermaStoreConfigurationBuilderException;
import com.getusroi.permastore.config.PermaStoreConfigurationConstant;
import com.getusroi.permastore.config.impl.PermaStoreInLineCacheObjectBuilder;
import com.getusroi.permastore.config.jaxb.InlineBuilder;

public class PermaStoreInLineCacheObjectBuilderTest {
	final Logger logger = LoggerFactory.getLogger(PermaStoreInLineCacheObjectBuilderTest.class);
	@Test
	public void testInlineConfigBuilderSimpleJson(){
		InlineBuilder inlineconfigBuilderConfig=new InlineBuilder();
		inlineconfigBuilderConfig.setType(PermaStoreConfigurationConstant.INLINE_CONFIGBUILDER_JSONTOMAP);
		inlineconfigBuilderConfig.setValue("{\"name\":\"abd\",\"desig\":\"def\"}");
		
		PermaStoreInLineCacheObjectBuilder inlineBuilder=new PermaStoreInLineCacheObjectBuilder();
		try {
			Map map=(Map)inlineBuilder.loadDataForCache(inlineconfigBuilderConfig);
			Assert.assertEquals("JSON should Match","abd",map.get("name"));
		} catch (PermaStoreConfigurationBuilderException e) {
			logger.error("ParsingError", e);
			Assert.fail("PermaStoreConfigurationBuilderException should not come");
		}
	}
	
	@Test(expected=com.getusroi.permastore.config.PermaStoreConfigurationBuilderException.class)
	public void testInlineConfigBuilderBadJson() throws PermaStoreConfigurationBuilderException{
		InlineBuilder inlineconfigBuilderConfig=new InlineBuilder();
		inlineconfigBuilderConfig.setType(PermaStoreConfigurationConstant.INLINE_CONFIGBUILDER_JSONTOMAP);
		inlineconfigBuilderConfig.setValue("{\"name\"=\"abd\",\"desig\":\"def\"}");
		
		PermaStoreInLineCacheObjectBuilder inlineBuilder=new PermaStoreInLineCacheObjectBuilder();
		Map map=(Map)inlineBuilder.loadDataForCache(inlineconfigBuilderConfig);
	}
}
