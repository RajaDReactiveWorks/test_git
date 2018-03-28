package com.attunedlabs.leap.i18n.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.i18n.dao.I18nTextDao;
import com.attunedlabs.leap.i18n.dao.I18nValidPossibilitiesDAO;
import com.attunedlabs.leap.i18n.entity.LeapI18nText;
import com.attunedlabs.leap.i18n.entity.LeapValidPossibilities;
import com.attunedlabs.leap.i18n.exception.LocaleResolverException;
import com.attunedlabs.leap.i18n.utils.LeapI18nUtil;

public class LeapResourceBundleResolverImpl implements ILeapResourceBundleResolver {

	private Connection dbConnection;
	private I18nTextDao localeMessagesDao;
	private I18nValidPossibilitiesDAO localeComboDao;
	private static final Logger logger = LoggerFactory.getLogger(LeapResourceBundleResolverImpl.class);

	/*@Override
	public Map<String, String> getLocaleSetupContext(String tenantId, String siteId) throws LocaleResolverException {
		localeSetUpDao = new I18nSetUpDao();
		return localeSetUpDao.selectDefaultSetup(tenantId, siteId);
	}// ..end of the method

	@Override
	public List<LeapI18nResourceType> getAllResourceType() throws LocaleResolverException {
		logger.debug(".getAllResourceTypeContext()..");
		localeResourceTypeDao = new I18nResourceTypeDao();
		return localeResourceTypeDao.selectAllResourceType();
	}// ..end of the method

	@Override
	public List<LeapI18nVariant> getAllVariantType() throws LocaleResolverException {
		logger.debug(".getAllVariantTypeContext()..");
		localeVariantTypeDao = new I18nVariantTypeDao();
		return localeVariantTypeDao.selectAllVariantTypes();
	}// ..end of the method
*/
	@Override
	public ResourceBundle getLeapLocaleComboBundle(String tenantId, String siteId, String featureGroup,
			String featureName, String localeId, String vpType, Exchange exchange) throws LocaleResolverException {
//		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
//		RequestContext oldRequestContext = leapHeader.getRequestContext();
//		logger.debug("oldRequestContext: " + oldRequestContext);
		// String featureGroup = oldRequestContext.getFeatureGroup();
		// String featureName = oldRequestContext.getFeatureName();
//		if (tenantId == null) {
//			tenantId = oldRequestContext.getTenantId();
//			if (siteId == null) {
//				siteId = oldRequestContext.getSiteId();
//			}
//		} else if (siteId == null) {
//			siteId = oldRequestContext.getSiteId();
//		}
		// int applicableNodeId = getActiveApplicableNodeId(tenantId, siteId,
		// featureGroup, featureName);
		ResourceBundle mybundle;
		if (!(LeapI18nUtil.isEmpty(tenantId) || LeapI18nUtil.isEmpty(siteId) || LeapI18nUtil.isEmpty(featureName))) {
			if (!(LeapI18nUtil.isEmpty(vpType))) {
				String replcdtenantId = LeapI18nUtil.buildString(tenantId);
				String replcdsiteId = LeapI18nUtil.buildString(siteId);
				String replcdVpType = LeapI18nUtil.buildString(vpType);
				String replcdLocaleId = LeapI18nUtil.buildString(localeId);
				String propertyFileName = replcdtenantId + "-" + replcdsiteId + "-" + featureName + "-" + replcdVpType
						+ "-" + replcdLocaleId + ".properties";
				logger.debug("propertyFileName : "+propertyFileName);
				logger.debug("class : "+this.getClass());
				try {
					mybundle = new PropertyResourceBundle(new InputStreamReader(
							this.getClass().getClassLoader().getResourceAsStream(propertyFileName), "UTF-8"));
				} catch (IOException e) {
					throw new LocaleResolverException(
							"Unable to load the properties file mentioned: " + propertyFileName, e);
				}
				return mybundle;
			} else {
				throw new LocaleResolverException("empty values requested to build the i18n properties file name ");
			}
		} else {
			throw new LocaleResolverException("empty values requested to build the i18n properties file name ");
		}
	}// ..end of the method

	@Override
	public ResourceBundle getLeapLocaleBundle(String tenantId, String siteId, String resourceType, String variant,
			String localeId, String featureGroup, String featureName, Exchange exchange)
			throws LocaleResolverException {
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		RequestContext oldRequestContext = leapHeader.getRequestContext();
		logger.debug("oldRequestContext: " + oldRequestContext);
		// String featureGroup = oldRequestContext.getFeatureGroup();
		// String featureName = oldRequestContext.getFeatureName();
		if (tenantId == null) {
			tenantId = oldRequestContext.getTenantId();
			if (siteId == null) {
				siteId = oldRequestContext.getSiteId();
			}
		} else if (siteId == null) {
			siteId = oldRequestContext.getSiteId();
		}
		// int applicableNodeId = getActiveApplicableNodeId(tenantId, siteId,
		// featureGroup, featureName);
		ResourceBundle mybundle;
		if (!(LeapI18nUtil.isEmpty(tenantId) || LeapI18nUtil.isEmpty(siteId) || LeapI18nUtil.isEmpty(featureName))) {
			if (!(LeapI18nUtil.isEmpty(resourceType) || LeapI18nUtil.isEmpty(variant)
					|| LeapI18nUtil.isEmpty(localeId))) {
				String replcdtenantId = LeapI18nUtil.buildString(tenantId);
				String replcdsiteId = LeapI18nUtil.buildString(siteId);
				String replcdlocaleId = LeapI18nUtil.buildString(localeId);
				String propertyFileName = replcdtenantId + "-" + replcdsiteId + "-" + featureName + "-" + replcdlocaleId
						+ ".properties";
				try {
					mybundle = new PropertyResourceBundle(new InputStreamReader(
							this.getClass().getClassLoader().getResourceAsStream(propertyFileName), "UTF-8"));
				} catch (IOException e) {
					throw new LocaleResolverException(
							"Unable to load the properties file mentioned: " + propertyFileName, e);
				}
				return mybundle;
			} else {
				throw new LocaleResolverException("empty values requested to build the i18n properties file name ");
			}
		} else {
			throw new LocaleResolverException("empty values requested to build the i18n properties file name ");
		}
	}// ..end of the method

	@Override
	public ResourceBundle getLeapLocaleBundle(String tenantId, String siteId, String feature, String localeId,
			Exchange exchange) throws LocaleResolverException {
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		RequestContext oldRequestContext = leapHeader.getRequestContext();
		logger.debug("oldRequestContext: " + oldRequestContext);
		// String featureGroup = oldRequestContext.getFeatureGroup();
		// String featureName = oldRequestContext.getFeatureName();
		if (tenantId == null) {
			tenantId = oldRequestContext.getTenantId();
			if (siteId == null) {
				siteId = oldRequestContext.getSiteId();
			}
		} else if (siteId == null) {
			siteId = oldRequestContext.getSiteId();
		}
		// int applicableNodeId = getActiveApplicableNodeId(tenantId, siteId,
		// featureGroup, featureName);
		ResourceBundle mybundle;
		if (!(LeapI18nUtil.isEmpty(tenantId) || LeapI18nUtil.isEmpty(siteId) || LeapI18nUtil.isEmpty(feature))) {
			if (!(LeapI18nUtil.isEmpty(localeId))) {
				String replcdtenantId = LeapI18nUtil.buildString(tenantId);
				String replcdsiteId = LeapI18nUtil.buildString(siteId);
				String featureName = LeapI18nUtil.buildString(feature);
				String replcdlocaleId = LeapI18nUtil.buildString(localeId);
				String propertyFileName = replcdtenantId + "-" + replcdsiteId + "-" + featureName + "-" + replcdlocaleId
						+ ".properties";
				try {
					mybundle = new PropertyResourceBundle(new InputStreamReader(
							this.getClass().getClassLoader().getResourceAsStream(propertyFileName), "UTF-8"));
				} catch (IOException e) {
					throw new LocaleResolverException(
							"Unable to load the properties file mentioned: " + propertyFileName, e);
				}
				return mybundle;
			} else {
				throw new LocaleResolverException("empty values requested to build the i18n properties file name ");
			}
		} else {
			throw new LocaleResolverException("empty values requested to build the i18n properties file name ");
		}
	}// ..end of the method

	/*@Override
	public String getDefaultLocale(String tenantId, String siteId) throws LocaleResolverException {
		localeSetUpDao = new I18nSetUpDao();
		return localeSetUpDao.selectDefaultLocale(tenantId, siteId);
	}// ..end of the method
*/
	@Override
	public List<LeapI18nText> getAllTenantLeapLocaleObjects(String tenantId, String siteId, Exchange exchange)
			throws LocaleResolverException {
		localeMessagesDao = new I18nTextDao();
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		RequestContext oldRequestContext = leapHeader.getRequestContext();
		String featureGroup = oldRequestContext.getFeatureGroup();
		String featureName = oldRequestContext.getFeatureName();

		if (tenantId == null) {
			tenantId = oldRequestContext.getTenantId();
			if (siteId == null) {
				siteId = oldRequestContext.getSiteId();
			}
		} else if (siteId == null) {
			siteId = oldRequestContext.getSiteId();
		}
		// int applicableNodeId = getActiveApplicableNodeId(tenantId, siteId,
		// featureGroup, featureName);
		List<LeapI18nText> localeMessageContexts = localeMessagesDao.selectAllTenantMessage(tenantId, siteId,
				featureName);
		return localeMessageContexts;
	}// ..end of the method

	/**
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param featureGroup
	 * @param featureName
	 * @param implementation
	 * @param vendor
	 * @param version
	 * @return
	 * @throws LocaleResolverException
	 */
	private int getActiveApplicableNodeId(String tenantId, String siteId, String featureGroup, String featureName)
			throws LocaleResolverException {
		IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();
		IConfigPersistenceService iConfigPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		FeatureDeployment featureDeployment;
		LeapHeader header = new LeapHeader();
		try {
			featureDeployment = featureDeploymentservice.getActiveAndPrimaryFeatureDeployedFromCache(tenantId, siteId,
					featureName, header);
		} catch (FeatureDeploymentServiceException e1) {
			throw new LocaleResolverException("Unable to get the featureDeploymentId: ", e1);
		}
		int applicableNodeId = 0;
		try {
			/*
			 * applicableNodeId =
			 * iConfigPersistenceService.getApplicableNodeId(tenantId, siteId,
			 * featureGroup, featureName,
			 * featureDeployment.getImplementationName(),
			 * featureDeployment.getVendorName(),
			 * featureDeployment.getFeatureVersion());
			 */
			applicableNodeId = iConfigPersistenceService.getApplicableNodeId(tenantId, siteId, featureGroup,
					featureName, null, null, null);
		} catch (InvalidNodeTreeException | ConfigPersistenceException e2) {
			throw new LocaleResolverException("Unable to get the applicableNodeId: ", e2);
		}
		return applicableNodeId;
	}// ..end of the method

	@Override
	public LeapI18nText getLeapLocaleObject(String tenantId, String siteId, String localeId, Exchange exchange)
			throws LocaleResolverException {
		localeMessagesDao = new I18nTextDao();
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		RequestContext oldRequestContext = leapHeader.getRequestContext();
		String featureGroup = oldRequestContext.getFeatureGroup();
		String featureName = oldRequestContext.getFeatureName();

		if (tenantId == null) {
			tenantId = oldRequestContext.getTenantId();
			if (siteId == null) {
				siteId = oldRequestContext.getSiteId();
			}
		} else if (siteId == null) {
			siteId = oldRequestContext.getSiteId();
		}
		// int applicableNodeId = getActiveApplicableNodeId(tenantId, siteId,
		// featureGroup, featureName);
		LeapI18nText messageContext = localeMessagesDao.selectMessage(tenantId, siteId, localeId, featureName);
		return messageContext;
	}

	@Override
	public List<LeapI18nText> getAllLeapLocaleObjects() throws LocaleResolverException {
		localeMessagesDao = new I18nTextDao();
		List<LeapI18nText> localeMessageContexts = localeMessagesDao.selectAllMessage();
		return localeMessageContexts;
	}// ..end of the method

	@Override
	public List<LeapValidPossibilities> getAllLeapLocaleComboObjects() throws LocaleResolverException {
		localeComboDao = new I18nValidPossibilitiesDAO();
		List<LeapValidPossibilities> localeComboRadioCheckboxContexts = localeComboDao
				.selectAllValidPossibilities();
		logger.debug("localeComboRadioCheckboxContexts : " + localeComboRadioCheckboxContexts);
		return localeComboRadioCheckboxContexts;
	}// ..end of the method

	@Override
	public Map<String, String> getLocaleSetupContext(String tenantId, String siteId) throws LocaleResolverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultLocale(String tenantId, String siteId) throws LocaleResolverException {
		// TODO Auto-generated method stub
		return null;
	}

}
