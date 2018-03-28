package com.attunedlabs.leap.i18n.service;

import java.util.List;

import com.attunedlabs.leap.i18n.entity.LeapI18nText;
import com.attunedlabs.leap.i18n.entity.LeapValidPossibilities;
import com.attunedlabs.leap.i18n.exception.LocaleRegistryException;
import com.attunedlabs.leap.i18n.exception.LocaleResolverException;

/**
 * API's conclude for initial setup of the i18n{{names}} tables for further
 * processing
 * 
 * @author GetUsRoi
 *
 */
public interface ILeapI18nSetup {

	/**
	 * API to add new LeapLocaleMessageContext, which includes the details for
	 * building the propertyBundles
	 * 
	 * @param localeContext
	 * @return
	 * @throws LocaleRegistryException
	 */
	public void addNewLocaleMessage(LeapI18nText localeContext) throws LocaleRegistryException;

	/**
	 * API to add multi-messages once
	 * 
	 * @param localeContexts
	 * @return
	 */
	public void addNewLocaleMessages(List<LeapI18nText> localeContexts) throws LocaleRegistryException;

	/**
	 * API to setup new resourceTypes at once, which in turn can be used to list
	 * out all available master-level resourceTypes
	 * 
	 * @param resourceTypeContexts
	 * @throws LocaleRegistryException
	 */
//	public void addNewResourceTypes(List<LeapI18nResourceType> resourceTypeContexts) throws LocaleRegistryException;

	/**
	 * API to setup new variantTypes at once, which in turn can be used to list
	 * out all available master-level table variantTypes
	 * 
	 * @param variantContexts
	 * @throws LocaleRegistryException
	 */
//	public void addNewVariantTypes(List<LeapI18nVariant> variantContexts) throws LocaleRegistryException;

	/**
	 * API, to add new locale along with tenant/site in 18nSetup table
	 * 
	 * @param leapLocaleSetupContext
	 * @throws LocaleRegistryException
	 */
//	public void addNewLocale(LeapI18nSetup leapLocaleSetupContext) throws LocaleRegistryException;

	/**
	 * API, to update the existing tenant site with new default locale
	 * 
	 * @param tenatantId
	 * @param siteId
	 * @param locale
	 * @throws LocaleRegistryException
	 *//*
	public void updateLocale(LeapI18nSetup leapLocaleSetupContext) throws LocaleRegistryException;*/

	/**
	 * API, to delete the existing locale of a given tenant and site
	 * 
	 * @param tenantId
	 * @param siteId
	 * @throws LocaleRegistryException
	 * @throws LocaleResolverException 
	 */
	public void deleteLocale(String tenantId, String siteId) throws LocaleRegistryException, LocaleResolverException;

	/**
	 * Api, to create i18n properties files in the class-path , which will be
	 * called once on load, so this is used for setup
	 * 
	 * @param localeMessageContexts
	 * @throws LocaleResolverException
	 */
	public void buildLocaleBundle(List<LeapI18nText> localeMessageContexts) throws LocaleResolverException;
	
	/**
	 * Api, to create i18n properties files in the class-path , which will be
	 * called once on load, so this is used for setup
	 * 
	 * @param localeMessageContexts
	 * @throws LocaleResolverException
	 */
	public void buildComboLocaleBundle(List<LeapValidPossibilities> localeMessageContexts) throws LocaleResolverException;

}
