package com.attunedlabs.featuremaster;

import com.attunedlabs.config.ConfigurationContext;

public interface IFeatureMasterService {
	
	
	
	public boolean checkFeatureExistInFeatureMasterOrNot(ConfigurationContext configContext) throws FeatureMasterServiceException;
	public boolean insertFeatureDetailsIntoFeatureMaster(FeatureMaster featureMaster)throws FeatureMasterServiceException;
	public boolean deleteFeatureDetailsInFeatureMaster(String featureName,int siteId) throws FeatureMasterServiceException;
}
