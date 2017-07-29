package com.getusroi.featuredeployment;

import com.getusroi.config.ConfigurationContext;

public interface IFeatureDeployment {

	public void addFeatureDeployement(ConfigurationContext configContext,boolean isActive,boolean isPrimary,boolean isCustomized) throws FeatureDeploymentServiceException;
	public void CheckAndaddFeatureDeployementInCache(ConfigurationContext configContext,boolean isActive,boolean isPrimary,boolean isCustomized) throws FeatureDeploymentServiceException;
	public FeatureDeployment getFeatureDeployedDeatils(ConfigurationContext configContext) throws FeatureDeploymentServiceException;
	public boolean deleteFeatureDeployed(ConfigurationContext configContext) throws FeatureDeploymentServiceException;
	public FeatureDeployment getActiveAndPrimaryFeatureDeployedFromCache(String tenant,String site,String featureName) throws FeatureDeploymentServiceException;
	public boolean updateFeatureDeployed(ConfigurationContext configContext,boolean isPrimary,boolean isActive) throws FeatureDeploymentServiceException;
	public boolean checkIfFeatureIsAlreadyDeployed(ConfigurationContext configContext) throws FeatureDeploymentServiceException;
}