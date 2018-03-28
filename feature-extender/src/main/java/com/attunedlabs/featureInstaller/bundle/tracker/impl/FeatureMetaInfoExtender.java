package com.attunedlabs.featureInstaller.bundle.tracker.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.featureInstaller.bundle.tracker.BundleTracker;
import com.attunedlabs.featureInstaller.constant.FeatureMetaInfoConstant;
import com.attunedlabs.featureInstaller.util.FeatureMetaInfoResourceException;
import com.attunedlabs.featureInstaller.util.FeatureMetaInfoResourceUtil;
import com.attunedlabs.featuremaster.FeatureMasterServiceException;
import com.attunedlabs.featuremaster.IFeatureMasterService;
import com.attunedlabs.featuremaster.impl.FeatureMasterService;
import com.attunedlabs.featuremetainfo.FeatureMetaInfoConfigParserException;
import com.attunedlabs.featuremetainfo.impl.FeatureMetaInfoConfigXmlParser;
import com.attunedlabs.featuremetainfo.jaxb.Feature;
import com.attunedlabs.featuremetainfo.jaxb.FeatureGroup;
import com.attunedlabs.featuremetainfo.jaxb.FeatureMetainfo;

public class FeatureMetaInfoExtender extends BundleTracker{
	final static Logger logger = LoggerFactory.getLogger(FeatureMetaInfoExtender.class);
	private BundleContext context;
	public FeatureMetaInfoExtender(BundleContext context) throws FeatureMetaInfoExtenderException {
	super(context);
	this.context=context;
}

	/**
	 * This methid is used to add configuration defined in featureMetaINfo.xml file when bundle is installed and started
	 * @param bundle: Bundle Object
	 */
	@Override
	protected void addedBundle(Bundle bundle) throws FeatureMetaInfoExtenderException {
		logger.debug(".addedBundle of FeatureMetaInfoExtender for bundle id : "+bundle.getBundleId()+" bundle name : "+bundle.getSymbolicName());
			List<FeatureGroup> featureGroupList=parseAndGetFeatureMetaInfo(bundle);
			logger.debug("List in add : "+featureGroupList);
			if(featureGroupList != null && !featureGroupList.isEmpty()){
			for(FeatureGroup featureGroup:featureGroupList){
				String featureGroupName=featureGroup.getName();
				List<Feature> featureList=featureGroup.getFeatures().getFeature();
				for(Feature feature:featureList){
					logger.debug("Feature group Name : "+featureGroupName+", Feature Name : "+feature.getName());
					boolean isAvailable=checkFeatureExitInFeatureMaster(featureGroupName,feature.getName());
					if(isAvailable){
					FeatureMetaInfoResourceUtil featureMetaInfoUtil=new FeatureMetaInfoResourceUtil();
					try {
						featureMetaInfoUtil.checkResourceAvailableAndload(feature, bundle,featureGroupName);
					} catch (FeatureMetaInfoResourceException e) {
						throw new FeatureMetaInfoExtenderException("Unable to get the resources :  ",e);
					}
					}else{
						logger.debug("feature doesn't avaliable in master table with feature group name : "+featureGroupName+" and feature name : "+feature.getName());
					}
				}//end of for loop
			}//end of outer for loop
		
			}//check if feature group is not null
		
	}//end of method

	/**
	 * This method is used to remove all configuration of resource defined in featureMetaInfo.xml
	 * @param bundle : bundle
	 */
	@Override
	protected void removedBundle(Bundle bundle) throws FeatureMetaInfoExtenderException {
		logger.debug(".removedBundle of FeatureMetaInfoExtender for bundle id : "+bundle.getBundleId()+" bundle name : "+bundle.getSymbolicName());
		List<FeatureGroup> featureGroupList=parseAndGetFeatureMetaInfo(bundle);
		logger.debug("List in add : "+featureGroupList);
		if(featureGroupList != null && !featureGroupList.isEmpty()){
		for(FeatureGroup featureGroup:featureGroupList){
			String featureGroupName=featureGroup.getName();
			List<Feature> featureList=featureGroup.getFeatures().getFeature();			
			for(Feature feature:featureList){
				logger.debug("Feature group Name : "+featureGroupName+", Feature Name : "+feature.getName());
				boolean isAvailable=checkFeatureExitInFeatureMaster(featureGroupName,feature.getName());
				if(isAvailable){
				FeatureMetaInfoResourceUtil featureMetaInfoUtil=new FeatureMetaInfoResourceUtil();
				try {
					featureMetaInfoUtil.checkResourceAvailableAndDelete(feature, bundle,featureGroupName);
				} catch (FeatureMetaInfoResourceException e) {
					throw new FeatureMetaInfoExtenderException("Unable to get the resources :  ",e);
				}
				}else{
					logger.debug("feature doesn't avaliable in master table with feature group name : "+featureGroupName+" and feature name : "+feature.getName());
				}
			}//end of for loop
		}//end of outer for loop
		}//check if feature group is not null	
		logger.debug(".removedBundle of FeatureMetaInfoExtender for bundle id : "+bundle.getBundleId()+" bundle name : "+bundle.getSymbolicName());

	}//end of method
	
	/**
	 * This method is to parse and get FeatureMetaInfo object
	 * @param bundle : Bundle Object
	 * @return List<FeatureGroup>
	 * @throws FeatureMetaInfoExtenderException
	 */
	private List<FeatureGroup>  parseAndGetFeatureMetaInfo(Bundle bundle) throws FeatureMetaInfoExtenderException{
		logger.debug(".parseAndgetFeatureMetaInfo of FeatureMetaInfoExtender for bundle id : "+bundle.getBundleId()+" bundle name : "+bundle.getSymbolicName());
		URL featureMetaInfoXmlUrl=bundle.getResource(FeatureMetaInfoConstant.FEATUREMETAINFO_XML_FILE_NAME);
		logger.debug("featureMetaInfoXmlUrl : "+featureMetaInfoXmlUrl);
		String featurexmlAsString=convertFeatureMetaInfoXmlToString(featureMetaInfoXmlUrl,bundle);
		List<FeatureGroup> featureGroupList=null;
		if(featurexmlAsString!=null){
		FeatureMetaInfoConfigXmlParser featureMetaInfoParser=new FeatureMetaInfoConfigXmlParser();
		try {
			FeatureMetainfo featureMetaInfo=featureMetaInfoParser.marshallConfigXMLtoObject(featurexmlAsString);
			featureGroupList=featureMetaInfo.getFeatureGroup();			
			} catch (FeatureMetaInfoConfigParserException e) {
				throw new FeatureMetaInfoExtenderException("Unable to parse featureMetaInfo xml string into object ");
			}
		}//end of if(featurexmlAsString!=null)		
		logger.debug("exiting parseAndgetFeatureMetaInfo of FeatureMetaInfoExtender for bundle id : "+bundle.getBundleId()+" bundle name : "+bundle.getSymbolicName());
		return featureGroupList;
		
	}
	
	/**
	 * This method is used to check if feature exist in feature Master or not
	 * @param featureGroup : feature group
	 * @param featureName : feature name
	 * @return boolean : true/false
	 * @throws FeatureMetaInfoExtenderException
	 */
	private boolean checkFeatureExitInFeatureMaster(String featureGroup,String featureName) throws FeatureMetaInfoExtenderException{
		logger.debug(".checkFeatureExitInFeatureMaster of FeatureMetaInfoExtender");
		IFeatureMasterService featureMasterServie=new FeatureMasterService();
		boolean isAvailable;
		ConfigurationContext configurationContext=new ConfigurationContext(FeatureMetaInfoConstant.TENANT_KEY,FeatureMetaInfoConstant.SITE_KEY, featureGroup,featureName);
		try {
			isAvailable=featureMasterServie.checkFeatureExistInFeatureMasterOrNot(configurationContext);
		} catch (FeatureMasterServiceException e) {
			throw new FeatureMetaInfoExtenderException("Unable  find out Feature with feature group name : "+featureGroup+" and feature name : "+featureName+" in Feature master ",e);
		}
		return isAvailable;
	}
	
	
	/**
	 * This method is used to convert featureMetaInfo.xml file to Object	
	 * @param featureMetaInfoXmlUrl : URL of featureMetaInfo.xml file
	 * @param bundle : Bundle Object
	 * @return String : featureMetaInfo xml in string format
	 * @throws FeatureMetaInfoExtenderException
	 */
	private String convertFeatureMetaInfoXmlToString(URL featureMetaInfoXmlUrl,Bundle bundle) throws FeatureMetaInfoExtenderException{
		logger.debug(".convertFeatureMetaInfoXmlToString of FeatureMetaInfoExtender");
		InputStream featureMetaInfoXmlInput=null;
		String featurexmlAsString=null;
		StringBuilder out1 = new StringBuilder();
		if(featureMetaInfoXmlUrl!=null){
			try {
				featureMetaInfoXmlInput=featureMetaInfoXmlUrl.openConnection().getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(featureMetaInfoXmlInput));
				 String line;
		        try {
					while ((line = reader.readLine()) != null) {
					    out1.append(line);					}
				} catch (IOException e) {
					throw new FeatureMetaInfoExtenderException("Unable to open the read for the BufferedReader for the file : "+FeatureMetaInfoConstant.FEATUREMETAINFO_XML_FILE_NAME+" of bundle : "+bundle.getBundleId(),e);
				}
		        logger.debug(out1.toString());   //Prints the string content read from input stream
		        try {
					reader.close();
				} catch (IOException e) {
					throw new FeatureMetaInfoExtenderException("Unable to close the read for the BufferedReader for the file : "+FeatureMetaInfoConstant.FEATUREMETAINFO_XML_FILE_NAME+" of bundle : "+bundle.getBundleId(),e);
				}
			 featurexmlAsString=out1.toString();
			} catch (IOException e) {
				throw new FeatureMetaInfoExtenderException("Unable to open the input stream for the file : "+FeatureMetaInfoConstant.FEATUREMETAINFO_XML_FILE_NAME+"of bundle : "+bundle.getBundleId(),e);
			}
		}else{
			logger.debug("FeatureMetaInfo.xml file doesn't exist into then bundle with id : "+bundle.getBundleId()+", symbolic name : "+bundle.getSymbolicName());
		}		
		return featurexmlAsString;
	}//end of method

}
