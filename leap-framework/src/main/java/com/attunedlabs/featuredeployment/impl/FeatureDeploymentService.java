package com.attunedlabs.featuredeployment.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuremetainfo.util.FeatureMetaInfoResourceUtil;
import com.attunedlabs.leap.LeapHeader;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class FeatureDeploymentService implements IFeatureDeployment {

	final Logger logger = LoggerFactory.getLogger(FeatureDeploymentService.class);

	/**
	 * This method is used to add new feature deployment detail
	 * 
	 * @param configContext
	 *            : Configuration Context Object
	 * @param ImplName
	 *            : Implementation name
	 * @param isActive
	 *            : feature is active(true/false)
	 * @param isPrimary
	 *            : feature is primary feature (true/false)
	 * @param isCustomized
	 *            : feature is customizable (true/false)
	 * @throws FeatureDeploymentServiceException
	 */
	public void addFeatureDeployement(ConfigurationContext configContext, boolean isActive, boolean isPrimary,
			boolean isCustomized) throws FeatureDeploymentServiceException {
		logger.debug(".addFeatureDeployement method of FeatureDeploymentService");
		int featureMasterId = 0;
		int siteNodeId;
		try {
			siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			featureMasterId = configPersistenceService.getFeatureMasterIdByFeatureAndFeaturegroup(
					configContext.getFeatureName(), configContext.getFeatureGroup(), configContext.getVersion(),
					siteNodeId);
			logger.debug("feature master id : " + featureMasterId);
			if (featureMasterId != 0) {
				FeatureDeployment featureDeployment = new FeatureDeployment(featureMasterId,
						configContext.getFeatureName(), configContext.getImplementationName(),
						configContext.getVendorName(), configContext.getVersion(), isActive, isPrimary, isCustomized,
						configContext.getProvider());
				FeatureDeployment isInsertedFeatureDeployment = configPersistenceService
						.insertFeatureDeploymentDetails(featureDeployment);
				if (isInsertedFeatureDeployment.getId() != 0) {
					addfeatureDeploymentInCache(isInsertedFeatureDeployment, siteNodeId);
				}
			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new FeatureDeploymentServiceException(
					"Unable to add feature with config : " + configContext + " in feature deployemnt  is active : "
							+ isActive + ", isPrimary : " + isPrimary + ", is customizable: " + isCustomized,
					e);
		}

	}// end of method addFeatureDeployement
	
	
	/**
	 * This method is used to add new feature deployment detail
	 * 
	 * @param configContext
	 *            : Configuration Context Object
	 * @param ImplName
	 *            : Implementation name
	 * @param isActive
	 *            : feature is active(true/false)
	 * @param isPrimary
	 *            : feature is primary feature (true/false)
	 * @param isCustomized
	 *            : feature is customizable (true/false)
	 * @throws FeatureDeploymentServiceException
	 */
	public void CheckAndaddFeatureDeployementInCache(ConfigurationContext configContext, boolean isActive, boolean isPrimary,
			boolean isCustomized) throws FeatureDeploymentServiceException {
		logger.debug(".addFeatureDeployementInCache method of FeatureDeploymentService");
		int featureMasterId = 0;
		int siteNodeId;
		try {
			siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			featureMasterId = configPersistenceService.getFeatureMasterIdByFeatureAndFeaturegroup(
					configContext.getFeatureName(), configContext.getFeatureGroup(), configContext.getVersion(),
					siteNodeId);
			logger.debug("feature master id : " + featureMasterId);
			if (featureMasterId != 0) {
				FeatureDeployment featureDeployment=configPersistenceService.getFeatureDeploymentDetails(featureMasterId,configContext.getFeatureName(),configContext.getImplementationName(),configContext.getVendorName(),configContext.getVersion());
				if(featureDeployment==null){
					featureDeployment= new FeatureDeployment(featureMasterId,
							configContext.getFeatureName(), configContext.getImplementationName(),
							configContext.getVendorName(), configContext.getVersion(), isActive, isPrimary, isCustomized);
					FeatureDeployment isInsertedFeatureDeployment = configPersistenceService.insertFeatureDeploymentDetails(featureDeployment);
					if (isInsertedFeatureDeployment.getId() != 0) {
						addfeatureDeploymentInCache(featureDeployment, siteNodeId);
					}
				}
				else{
				addfeatureDeploymentInCache(featureDeployment, siteNodeId);
				}

			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new FeatureDeploymentServiceException(
					"Unable to add feature with config : " + configContext + " in feature deployemnt  is active : "
							+ isActive + ", isPrimary : " + isPrimary + ", is customizable: " + isCustomized,
					e);
		}

	}// end of method addFeatureDeployement

	/**
	 * This method is used to get the featureDeployment deatiled based on
	 * featureName,impl name, vendor and version
	 * 
	 * @param configContext
	 *            : Configuration Object
	 * @param implName
	 *            : Implementation Name
	 * @return
	 * @throws FeatureDeploymentServiceException
	 */
	public FeatureDeployment getFeatureDeployedDeatils(ConfigurationContext configContext)
			throws FeatureDeploymentServiceException {
		logger.debug(".getFeatureDeployedDeatils method of FeatureDeploymentService");
		FeatureDeployment featureDeployment = null;
		int featureMasterId = 0;
		try {
			int siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			logger.debug("siteNodeId: " + siteNodeId);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			featureMasterId = configPersistenceService.getFeatureMasterIdByFeatureAndFeaturegroup(
					configContext.getFeatureName(), configContext.getFeatureGroup(), configContext.getVersion(),
					siteNodeId);
			logger.debug("feature master id : " + featureMasterId);
			if (featureMasterId != 0) {
				featureDeployment = configPersistenceService.getFeatureDeploymentDetails(featureMasterId,
						configContext.getFeatureName(), configContext.getImplementationName(),
						configContext.getVendorName(), configContext.getVersion());
				return featureDeployment;
			}
		} catch (ConfigPersistenceException | InvalidNodeTreeException e) {
			throw new FeatureDeploymentServiceException(
					"Unable to get feature with config : " + configContext + " in feature deployemnt ");
		}
		return featureDeployment;

	}// end of method getFeatureDeployedDeatils

	/**
	 * This method is used to delete feature deployed for specific
	 * featurename,implementation name,vendor name, version name
	 * 
	 * @param configContext
	 *            : Configuration Context object
	 * @param implName
	 *            : Implementation Name
	 * @return isDeleted : boolean
	 * @throws FeatureDeploymentServiceException
	 */
	public boolean deleteFeatureDeployed(ConfigurationContext configContext) throws FeatureDeploymentServiceException {
		logger.debug(".deleteFeatureDeployed method of FeatureDeploymentService");
		int featureMasterId = 0;
		try {
			int siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			featureMasterId = configPersistenceService.getFeatureMasterIdByFeatureAndFeaturegroup(
					configContext.getFeatureName(), configContext.getFeatureGroup(), configContext.getVersion(),
					siteNodeId);
			logger.debug("feature master id : " + featureMasterId);
			if (featureMasterId != 0) {

				boolean isDeleted = configPersistenceService.deleteFeatureDeployment(featureMasterId,
						configContext.getFeatureName(), configContext.getImplementationName(),
						configContext.getVendorName(), configContext.getVersion());
				if (isDeleted) {
					deleteFeatureDeploymentFromCache(configContext, siteNodeId);
				}
				return isDeleted;
			}
		} catch (ConfigPersistenceException | InvalidNodeTreeException e) {
			throw new FeatureDeploymentServiceException(
					"Unable to delete feature with config : " + configContext + " in feature deployemnt ");
		}
		return false;

	}// end of method deleteFeatureDeployed

	/**
	 * This method is used to delete feature deployed for specific
	 * featurename,implementation name,vendor name, version name
	 * 
	 * @param configContext
	 *            : Configuration Context object
	 * @param implName
	 *            : Implementation Name
	 * @return isDeleted : boolean
	 * @throws FeatureDeploymentServiceException
	 */
	public boolean updateFeatureDeployed(ConfigurationContext configContext, boolean isPrimary, boolean isActive)
			throws FeatureDeploymentServiceException {
		logger.debug(".updateFeatureDeployed method of FeatureDeploymentService");
		int featureMasterId = 0;
		try {
			int siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			featureMasterId = configPersistenceService.getFeatureMasterIdByFeatureAndFeaturegroup(
					configContext.getFeatureName(), configContext.getFeatureGroup(), configContext.getVersion(),
					siteNodeId);
			logger.debug("feature master id : " + featureMasterId);
			if (featureMasterId != 0) {
				boolean isUpdated = configPersistenceService.updateFeatureDeployment(featureMasterId,
						configContext.getFeatureName(), configContext.getImplementationName(),
						configContext.getVendorName(), configContext.getVersion(), isPrimary, isActive);
				logger.debug("is updated : " + isUpdated);
				if (isUpdated) {
					updateFeatureDeploymentFromCache(configContext, isPrimary, isActive, siteNodeId);
				}
				return isUpdated;
			}
		} catch (ConfigPersistenceException | InvalidNodeTreeException e) {
			throw new FeatureDeploymentServiceException("Unable to update feature with config : " + configContext
					+ " in feature deployemnt with is primary :  " + isPrimary);
		}
		return false;

	}// end of method updateFeatureDeployed

	/**
	 * This method is used to find out if feature is already deployed
	 * irrespective of its implementation
	 * 
	 * @param configContext
	 *            : Configuration Context Object
	 * @return true if exist else false
	 * @throws FeatureDeploymentServiceException
	 */
	public boolean checkIfFeatureIsAlreadyDeployed(ConfigurationContext configContext)
			throws FeatureDeploymentServiceException {
		logger.debug(".checkIfFeatureIsAlreadyDeployed method of FeatureDeploymentService");
		boolean alreadyExist = false;
		List<FeatureDeployment> copyOfFeatureDeployment = new ArrayList<>();
		try {
			int siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
			String cacheMapKey = createKeyForFeatureNameAndSiteId(configContext.getFeatureName(), siteNodeId);
			logger.debug("cacheMapKey : " + cacheMapKey + ", ConfigurationContext : " + configContext);
			List<FeatureDeployment> featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
			if (featureDeploymentList != null && !(featureDeploymentList.isEmpty()) && featureDeploymentList.size() > 0)
				return true;

		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new FeatureDeploymentServiceException("Unable to check feature is already deployed with config : "
					+ configContext + " in feature deployemnt  ");

		}

		return alreadyExist;

	}

	private void updateFeatureDeploymentFromCache(ConfigurationContext configContext, boolean isPrimary,
			boolean isActive, int siteNodeId) {
		logger.debug(".updateFeatureDeploymentFromCache method of FeatureDeploymentService");
		List<FeatureDeployment> copyOfFeatureDeployment = new ArrayList<>();
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
		String cacheMapKey = createKeyForFeatureNameAndSiteId(configContext.getFeatureName(), siteNodeId);
		logger.debug("cacheMapKey : " + cacheMapKey + ", ConfigurationContext : " + configContext);
		List<FeatureDeployment> featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
		if (featureDeploymentList != null && !(featureDeploymentList.isEmpty())) {
			for (FeatureDeployment featureDeployment : featureDeploymentList) {
				if (featureDeployment.getFeatureName().equalsIgnoreCase(configContext.getFeatureName())
						&& featureDeployment.getVendorName().equalsIgnoreCase(configContext.getVendorName())
						&& featureDeployment.getImplementationName()
								.equalsIgnoreCase(configContext.getImplementationName())
						&& featureDeployment.getFeatureVersion().equalsIgnoreCase(configContext.getVersion())) {
					featureDeployment.setPrimary(isPrimary);
					featureDeployment.setActive(isActive);
					logger.debug("featureDeploymentList after update : " + featureDeploymentList);
					map.put(cacheMapKey, (Serializable) featureDeploymentList);
				}
			}
		} // end of if

	}// end of method updateFeatureDeploymentFromCache
	
	/**
	 * This method is used to get the active and primary feature deployed in
	 * feature deployemnt
	 * 
	 * @param tenant
	 *            : tenant in string
	 * @param site
	 *            : site in string
	 * @param feature
	 *            name : feature name in string
	 */
	public FeatureDeployment getActiveAndPrimaryFeatureDeployedFromCache(String tenant, String site, String featureName,String provider)
			throws FeatureDeploymentServiceException {
		logger.debug(".getFeatureDeploymentListFromCache method of FeatureDeploymentService");
		int siteNodeId;
		try {
			siteNodeId = getApplicableNodeIdForSite(tenant, site);
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
			logger.debug("Map getActiveAndPrimaryFeatureDeployedFromCache with provider : "+map);
			String cacheMapKey = createKeyForFeatureNameAndSiteId(featureName, siteNodeId);
			logger.debug("cacheMapKey : " + cacheMapKey + ", tenant  : " + tenant + ", site : " + site
					+ ", feature Name : " + featureName + ", siteNodeId : " + siteNodeId);
			List<FeatureDeployment> featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
			logger.debug("feature deployment list : " + featureDeploymentList);
			logger.debug("-------------------------------->");
			if (featureDeploymentList != null)
				for (FeatureDeployment featureDeployment : featureDeploymentList) {
					logger.debug("feature deployemnt : " + featureDeployment);
					if (featureDeployment.getFeatureName().equalsIgnoreCase(featureName)) {
						logger.debug("feature name : " + featureName + ", impl name : "
								+ featureDeployment.getImplementationName() + ", active : "
								+ featureDeployment.isActive() + ", feature deployemnt primary : "
								+ featureDeployment.isPrimary());
						if (featureDeployment.isActive() && featureDeployment.getProvider().equals(provider)) {
							logger.debug("feature deployment : " + featureDeployment);
							return featureDeployment;
						}
					}
				}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new FeatureDeploymentServiceException();
		}
		return null;

	}
	
	/**
	 * This method is used to get the active and primary feature deployed in
	 * feature deployemnt
	 * 
	 * @param tenant
	 *            : tenant in string
	 * @param site
	 *            : site in string
	 * @param feature
	 *            name : feature name in string
	 */
	public FeatureDeployment getActiveAndPrimaryFeatureDeployedFromCache(String tenant, String site, String featureName, LeapHeader leapHeader)
			throws FeatureDeploymentServiceException {
		logger.debug(".getFeatureDeploymentListFromCache method of FeatureDeploymentService");
		int siteNodeId;
		try {
			siteNodeId = getApplicableNodeIdForSite(tenant, site);
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
			String cacheMapKey = createKeyForFeatureNameAndSiteId(featureName, siteNodeId);
			logger.debug("cacheMapKey : " + cacheMapKey + ", tenant  : " + tenant + ", site : " + site
					+ ", feature Name : " + featureName + ", siteNodeId : " + siteNodeId);
			List<FeatureDeployment> featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
			logger.debug("feature deployment list : " + featureDeploymentList);
			logger.debug("-------------------------------->");
			if (featureDeploymentList == null) {
				Pattern pattern = Pattern.compile("featureMetaInfo.xml");
				FeatureMetaInfoResourceUtil fmiResList = new FeatureMetaInfoResourceUtil(leapHeader.getTenant(),
						leapHeader.getSite(), leapHeader.getFeatureGroup(), leapHeader.getFeatureName(),
						leapHeader.getImplementationName(), leapHeader.getVendor(), leapHeader.getVersion());
				fmiResList.getClassPathResources(pattern);
				map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
				cacheMapKey = createKeyForFeatureNameAndSiteId(featureName, siteNodeId);
				featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
			}
			if (featureDeploymentList != null)
				for (FeatureDeployment featureDeployment : featureDeploymentList) {
					logger.debug("feature deployemnt : " + featureDeployment);
					if (featureDeployment.getFeatureName().equalsIgnoreCase(featureName)) {
						logger.debug("feature name : " + featureName + ", impl name : "
								+ featureDeployment.getImplementationName() + ", active : "
								+ featureDeployment.isActive() + ", feature deployemnt primary : "
								+ featureDeployment.isPrimary());
						if (featureDeployment.isActive() && featureDeployment.isPrimary()) {
							logger.debug("feature deployment : " + featureDeployment);
							return featureDeployment;
						}
					}
				}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new FeatureDeploymentServiceException();
		}
		return null;

	}// end of getActiveAndPrimaryFeatureDeployedFromCache

	/**
	 * This method is used to delete feature deployment detail from cache
	 * 
	 * @param configContext
	 *            : Configuration context Object
	 * @param siteNodeId
	 *            : site id in int
	 */
	private void deleteFeatureDeploymentFromCache(ConfigurationContext configContext, int siteNodeId) {
		logger.debug(".deleteFeatureDeploymentFromCache method of FeatureDeploymentService");
		List<FeatureDeployment> copyOfFeatureDeployment = new ArrayList<>();
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
		String cacheMapKey = createKeyForFeatureNameAndSiteId(configContext.getFeatureName(), siteNodeId);
		logger.debug("cacheMapKey : " + cacheMapKey + ", ConfigurationContext : " + configContext);
		List<FeatureDeployment> featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
		if (featureDeploymentList != null && !(featureDeploymentList.isEmpty())) {
			for (FeatureDeployment featureDeployment : featureDeploymentList) {
				if (featureDeployment.getFeatureName().equalsIgnoreCase(configContext.getFeatureName())
						&& featureDeployment.getVendorName().equalsIgnoreCase(configContext.getVendorName())
						&& featureDeployment.getImplementationName()
								.equalsIgnoreCase(configContext.getImplementationName())
						&& featureDeployment.getFeatureVersion().equalsIgnoreCase(configContext.getVersion())) {
					copyOfFeatureDeployment.add(featureDeployment);
				}
			}
		} // end of if
		featureDeploymentList.removeAll(copyOfFeatureDeployment);
		logger.debug("feature deployment after removal : " + featureDeploymentList);
		map.put(cacheMapKey, (Serializable) featureDeploymentList);

	}// end of method deleteFeatureDeploymentFromCache

	/**
	 * This method is used to add feature deployment detail in cache
	 * 
	 * @param featureDeployment
	 *            : FeatureDeployment Object need to be cached
	 * @param siteId
	 *            : site id in int
	 */
	private void addfeatureDeploymentInCache(FeatureDeployment featureDeployment, int siteId) {
		logger.debug(".addfeatureDeploymentInCache method of FeatureDeploymentService");
		logger.debug("featureDeployment in add feature : " + featureDeployment);
		List<FeatureDeployment> newfeatureDeploymentList = new ArrayList<>();
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
		String cacheMapKey = createKeyForFeatureNameAndSiteId(featureDeployment.getFeatureName(), siteId);
		logger.debug("cacheMapKey : " + cacheMapKey + ", featureDeployment : " + featureDeployment);
		List<FeatureDeployment> existingFeatureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
		if (existingFeatureDeploymentList != null) {
			logger.debug("feature deployment existing list in add " + existingFeatureDeploymentList);
			existingFeatureDeploymentList.add(featureDeployment);
			map.put(cacheMapKey, (Serializable) existingFeatureDeploymentList);
		} else {
			logger.debug("feature deployment new list in add " + newfeatureDeploymentList);
			newfeatureDeploymentList.add(featureDeployment);
			map.put(cacheMapKey, (Serializable) newfeatureDeploymentList);
		}

	}// end of method addfeatureDeploymentInCache

	private String createKeyForFeatureNameAndSiteId(String featureName, int siteId) {
		return (featureName + "-" + siteId).trim();
	}// end of method createKeyForFeatureNameAndSiteId

	private String getGlobalFeatureDeploymentKey() {
		return "GlobalFeatureDeployment".trim();
	}// end of method getGlobalFeatureDeploymentKey

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

}
