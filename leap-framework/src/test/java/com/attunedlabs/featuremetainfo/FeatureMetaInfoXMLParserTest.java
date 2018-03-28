package com.attunedlabs.featuremetainfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.featuremetainfo.FeatureMetaInfoConfigParserException;
import com.attunedlabs.featuremetainfo.impl.FeatureMetaInfoConfigXmlParser;
import com.attunedlabs.featuremetainfo.jaxb.DataContexts;
import com.attunedlabs.featuremetainfo.jaxb.Feature;
import com.attunedlabs.featuremetainfo.jaxb.FeatureGroup;
import com.attunedlabs.featuremetainfo.jaxb.FeatureMetainfo;

/**
 * This class is to test if featureMetaInfoXML parser is working properly or not
 * @author bizruntime
 *
 */
public class FeatureMetaInfoXMLParserTest {

final Logger logger = LoggerFactory.getLogger(FeatureMetaInfoXMLParserTest.class);
	
	private String featureMetaInfoFile;
	FeatureMetaInfoConfigXmlParser featureMetaInfoXmlParser;
	
	@Before
	public void getFeatureMetaInfoFileObject() throws FeatureMetaInfoConfigParserException{
		featureMetaInfoXmlParser=new FeatureMetaInfoConfigXmlParser();
		InputStream  inputstream= FeatureMetaInfoConfigXmlParser.class.getClassLoader().getResourceAsStream(FeatureMetaInfoTestConstant.configfileToParse);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			    
			}
			reader.close();
		} catch (IOException e) {
		throw new FeatureMetaInfoConfigParserException("feature MetaInfo file doesnot exist in classpath",e);
		}
		
		featureMetaInfoFile=out1.toString();
		logger.debug("featureMetaInfoFile : "+featureMetaInfoFile);
		
		
	}
	
	//@Test
	public void testFeatureConfigReading() throws  FeatureMetaInfoConfigParserException{
		FeatureMetainfo featureMetaInfo=featureMetaInfoXmlParser.marshallConfigXMLtoObject(featureMetaInfoFile);
		Assert.assertNotNull("FeatureMetaInfo must not be Null",featureMetaInfo);
		List<FeatureGroup> featureGroupList=featureMetaInfo.getFeatureGroup();
		String groupName=featureGroupList.get(0).getName();
		Assert.assertEquals("Feature Metainfo group name at first index is {Inventory} ", groupName,"Inventory");
		String featureName=featureMetaInfo.getFeatureGroup().get(0).getFeatures().getFeature().get(0).getName();
		Assert.assertEquals("Feature Metainfo feature name at first index of feature group is {Inventory} ", featureName,"Inventory");
		List<DataContexts> dataContextsList=featureMetaInfo.getFeatureGroup().get(0).getFeatures().getFeature().get(0).getFeatureDataContexts().getDataContexts();
		logger.debug("dataContextsList : "+dataContextsList);
				/*logger.debug("datacontext : "+dataContexts);
		logger.debug(dataContexts.getResourceName());*/
	}
	
	@Test
	public void testFeatureConfigDdlUtilReading() throws  FeatureMetaInfoConfigParserException{
		FeatureMetainfo featureMetaInfo=featureMetaInfoXmlParser.marshallConfigXMLtoObject(featureMetaInfoFile);
		Assert.assertNotNull("FeatureMetaInfo must not be Null",featureMetaInfo);
		List<FeatureGroup> featureGroupList=featureMetaInfo.getFeatureGroup();
		String groupName=featureGroupList.get(0).getName();
		String featureName=featureMetaInfo.getFeatureGroup().get(0).getFeatures().getFeature().get(0).getName();
		logger.debug("FeatureGroupName: "+groupName+" FeatureName: "+featureName);
		Feature featureAtIndexOne = featureMetaInfo.getFeatureGroup().get(0).getFeatures().getFeature().get(0);
		logger.debug("Name of DB: "+featureAtIndexOne.getDBConfiguration().getDatabase().getName());
	
	}
	
	
}
