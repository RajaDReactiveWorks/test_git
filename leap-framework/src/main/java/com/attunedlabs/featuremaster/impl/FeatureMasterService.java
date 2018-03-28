package com.attunedlabs.featuremaster.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.featuremaster.FeatureMaster;
import com.attunedlabs.featuremaster.FeatureMasterServiceException;
import com.attunedlabs.featuremaster.IFeatureMasterService;

public class FeatureMasterService implements IFeatureMasterService {

	final Logger logger = LoggerFactory.getLogger(FeatureMasterService.class);

	/**
	 * check wether the feature is Exist in Feature MAster or not
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param featureGroup
	 * @param featureName
	 */
	public boolean checkFeatureExistInFeatureMasterOrNot(ConfigurationContext configContext)
			throws FeatureMasterServiceException {

		logger.debug("inside checkFeatureExistInFeatureMasterOrNot method  ");
		logger.debug("configurationContext inside checkFeatureExistInFeatureMasterOrNot : " + configContext.toString()
				+ "version = " + configContext.getVersion());

		int featureMasterId = 0;
		try {
			int siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			logger.debug("configPersistenceService : " + configPersistenceService.toString());
			logger.debug("Datas fetched from configurationContext : " + configContext.getFeatureName());
			logger.debug("Config : " + configContext.getFeatureGroup());
			logger.debug("Config : " + configContext.getVersion());
			logger.debug("Config : " + siteNodeId);
			featureMasterId = configPersistenceService.getFeatureMasterIdByFeatureAndFeaturegroup(
					configContext.getFeatureName(), configContext.getFeatureGroup(), configContext.getVersion(),
					siteNodeId);
			logger.debug("feature found in FeatureMaster with MasterNodeId : " + featureMasterId);
			if (featureMasterId == 0)
				return false;

		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {

			throw new FeatureMasterServiceException("Failed find out Feature in Feature master " + e);

		}
		return true;

	}

	/**
	 * to get siteNodId By tenant name,site name,feature group
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param featureGroup
	 * @return siteNodeID
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */

	private Integer getApplicableNodeIdForSite(String tenantId, String siteId)
			throws InvalidNodeTreeException, ConfigPersistenceException {
		logger.debug("Finding ParentNodeId for Tenant=" + tenantId + "-siteId=" + siteId);

		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		return configPersistenceService.getApplicableNodeId(tenantId, siteId);
	}//

	/**
	 * to insert featureMasterdetails into featureMaster
	 */
	public boolean insertFeatureDetailsIntoFeatureMaster(FeatureMaster featureMaster)
			throws FeatureMasterServiceException {

		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		try {
			return configPersistenceService.insertFeatureMasterDetails(featureMaster);
		} catch (ConfigPersistenceException e) {
			throw new FeatureMasterServiceException(e);
		}
	}

	@Override
	public boolean deleteFeatureDetailsInFeatureMaster(String featureName, int siteId)
			throws FeatureMasterServiceException {

		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		try {
			return configPersistenceService.deleteFeatureMasterDetails(featureName, siteId);
		} catch (ConfigPersistenceException e) {
			throw new FeatureMasterServiceException(e);
		}

	}

}
