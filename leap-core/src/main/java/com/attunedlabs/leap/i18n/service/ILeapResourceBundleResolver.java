package com.attunedlabs.leap.i18n.service;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.camel.Exchange;

import com.attunedlabs.leap.i18n.entity.LeapI18nText;
import com.attunedlabs.leap.i18n.entity.LeapValidPossibilities;
import com.attunedlabs.leap.i18n.exception.LocaleResolverException;

/**
 * All relative API's to be called for getting the LeapI18nObjects
 * 
 * @author GetUsRoi
 *
 */
public interface ILeapResourceBundleResolver {
	
	/**
	 * API, returns the JAVA default ResourceBundle object for the requested
	 * arguments
	 * 
	 * @return ResourceBundle
	 * @throws PropertyResolverException
	 */
	public ResourceBundle getLeapLocaleComboBundle(String tenantId, String siteId, String featureGroup, String featureName, String localeId, String vpType, Exchange exchange) throws LocaleResolverException;
	
	/**
	 * API, returns the JAVA default ResourceBundle object for the requested
	 * arguments
	 * 
	 * @return ResourceBundle
	 * @throws PropertyResolverException
	 */
	public ResourceBundle getLeapLocaleBundle(String tenantId, String siteId, String resourceType, String variant,
			String localeId, String featureGroup, String feature, Exchange exchange) throws LocaleResolverException;

	/**
	 * API, returns the JAVA default ResourceBundle object for the requested
	 * arguments
	 * 
	 * @return ResourceBundle
	 * @throws PropertyResolverException
	 */
	public ResourceBundle getLeapLocaleBundle(String tenantId, String siteId, String feature,
			String localeId, Exchange exchange) throws LocaleResolverException;

	/**
	 * To get unique object , by passing the respective arguments which will
	 * perform DAO to get the Message object constructed
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param localeId
	 * @return
	 * @throws PropertyResolverException
	 */
	public LeapI18nText getLeapLocaleObject(String tenantId, String siteId, String localeId, Exchange exchange)
			throws LocaleResolverException;

	/**
	 * API, to get all available message objects for given tenant, site.
	 * Internally it will get the respective applicable nodeId at vendor/version
	 * level by DAO
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param localeId
	 * @return
	 * @throws PropertyResolverException
	 */
	public List<LeapI18nText> getAllTenantLeapLocaleObjects(String tenantId, String siteId, Exchange exchange)
			throws LocaleResolverException;

	/**
	 * API, to get all available message objects from the DB, DAO operation
	 * Performed, to construct all the files on startup
	 * 
	 * @return
	 * @throws LocaleResolverException
	 */
	public List<LeapI18nText> getAllLeapLocaleObjects() throws LocaleResolverException;

	/**
	 * API, to get the attributes, such as weighUnits, lengthUnits etc from the
	 * setup as Map<K,V>
	 * 
	 * @param requestContext
	 * @return
	 * @throws LocaleResolverException
	 */
	public Map<String, String> getLocaleSetupContext(String tenantId, String siteId) throws LocaleResolverException;

	/**
	 * API, to get the default registered locale for a given tenant/site
	 * 
	 * @param tenantId
	 * @param siteId
	 * @return
	 * @throws LocaleResolverException
	 */
	public String getDefaultLocale(String tenantId, String siteId) throws LocaleResolverException;
	
	/**
	 *  API, to get all available message objects from the DB, DAO operation
	 * Performed, to construct all the files on startup
	 * 
	 * @return
	 * @throws LocaleResolverException
	 */
	public List<LeapValidPossibilities> getAllLeapLocaleComboObjects() throws LocaleResolverException;

}
