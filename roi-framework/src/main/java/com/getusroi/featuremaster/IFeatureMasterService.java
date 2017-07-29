package com.getusroi.featuremaster;

import com.getusroi.config.ConfigurationContext;

public interface IFeatureMasterService {
	
	
	
	public boolean checkFeatureExistInFeatureMasterOrNot(ConfigurationContext configContext) throws FeatureMasterServiceException;
	public boolean insertFeatureDetailsIntoFeatureMaster(FeatureMaster featureMaster)throws FeatureMasterServiceException;
	public boolean deleteFeatureDetailsInFeatureMaster(String featureName,int siteId) throws FeatureMasterServiceException;
}
