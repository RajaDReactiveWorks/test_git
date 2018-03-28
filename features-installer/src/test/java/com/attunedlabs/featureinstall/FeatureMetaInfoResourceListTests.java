package com.attunedlabs.featureinstall;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.featureinstaller.util.FeatureMetaInfoResourceException;
import com.attunedlabs.featureinstaller.util.FeatureMetaInfoResourceUtil;

public class FeatureMetaInfoResourceListTests {
	final static Logger logger = LoggerFactory.getLogger(FeatureMetaInfoResourceListTests.class);
	private static final String GOOD_PATTERN_SEARCH_KEY="featureMetaInfo.xml";
	private static final String BAD_PATTERN_SEARCH_KEY="badFeatureMetaInfo.xml";
	
	@Test
	public void testGoodFeatureResourceFromClasspath() throws FeatureMetaInfoResourceException, IOException{
		FeatureMetaInfoResourceUtil fmiResList=new FeatureMetaInfoResourceUtil();
   	Pattern pattern = Pattern.compile(GOOD_PATTERN_SEARCH_KEY);
       Collection<String> resourcelist = fmiResList.getClassPathResources(pattern);
       Assert.assertTrue(resourcelist.contains(GOOD_PATTERN_SEARCH_KEY));
       for(String name : resourcelist){
      	 logger.debug("file name :"+name);
           Assert.assertEquals("expection file name as {featureMetaInfo.xml} ",GOOD_PATTERN_SEARCH_KEY, name);
       }
   }
	
	@Test
	public void testBadFeatureResourceFromClasspath() throws FeatureMetaInfoResourceException, IOException{
		FeatureMetaInfoResourceUtil fmiResList=new FeatureMetaInfoResourceUtil();
   	Pattern pattern = Pattern.compile(BAD_PATTERN_SEARCH_KEY);
       Collection<String> resourcelist = fmiResList.getClassPathResources(pattern);
       Assert.assertFalse(resourcelist.contains(BAD_PATTERN_SEARCH_KEY));
       for(String name : resourcelist){
      	 logger.debug("file name :"+name);
           Assert.assertNotEquals("Not expection file name as {featureMetaInfo.xml} ",BAD_PATTERN_SEARCH_KEY, name);
       }
   }
	

	
}
