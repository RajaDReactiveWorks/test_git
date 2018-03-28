package com.attunedlabs.policy.config;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.policy.config.impl.PolicyInvalidRegexExpception;
import com.attunedlabs.policy.jaxb.Policy;

/**
 * PolicyConfigurationService works will all the configurations aspects of the Policy
 * 
 * @author bizruntime
 *
 */
public interface IPolicyConfigurationService {
	/**
	 * Adds Policy Configuration to the system. Does the following
	 * 1.) Validates and Updates the Policy XML file to the database in confignodedata table
	 * 2.) Based on the dialect converts the evaluation expression into MVEL and cacjes it.
	 * 3.) Generates and caches the Response object for the PolicyDefined Fact.
	 * @See PolicyConfigurationUnit
	 * @param tenantId
	 * @param siteId
	 * @param policyConfig
	 * @throws PolicyConfigurationException
	 */
	public void addPolicyConfiguration(ConfigurationContext configurationContext, Policy policyConfig)throws PolicyConfigurationException;
	
	/**
	 * Gets you the Raw processed policy configuration Unit
	 * @param policyRequestContext
	 * @param policyName
	 * @return
	 * @throws PolicyConfigurationException
	 */
	public PolicyConfigurationUnit getPolicyConfigurationUnit(PolicyRequestContext policyRequestContext,String policyName)throws PolicyConfigurationException;
	/**
	 * Evaluates the policy
	 * @param policyRequestContext
	 * @param policyName
	 * @throws PolicyConfigurationException
	 */
	public Object getPolicyResponseData(PolicyRequestContext policyRequestContext,String policyName)throws PolicyRequestException,PolicyInvalidRegexExpception;
	
	/**
	 * Changes the status of the Policy to enable or disable
	 * <BR>Pseudo code
	 * Based on the PolicyRequest{tenant/site/featureGroup/feature} first get the applicable NodeId
	 * For enabling a policy{Disabled policy will not be in the Cache}
	 * 	1.) Based on the applicable NodeId search for the ConfigNodeDataId from the PersistenceService based on configName and Type as Policy
	 * 	2.) Parse the config xml from the database into the Policy object.
	 *    3.) Build the ConfigurationUnit by calling the PolicyConfigurationUnitBuilder.buildPolicyConfigUnit
	 *    4.) load configurationUnit in the cache by calling private method loadConfigurationInDataGrid(){calls the ConfigurationService}
	 *    5.) Update the database flag for this configuration as enabled.
	 * For disabling a policy {Disabled policy should not be in the cache}
	 *    1.) Get PolicyconfigurationUnit from the cache by calling .getPolicyConfigurationUnit()
	 *    2.) take the configNodeDataId from PolicyconfigurationUnit.dbconfigId and update the status as disabled from the Database using persistence service
	 *    3.) Remove config from cache by calling LeapConfigurationServer.deleteConfiguration
	 *    
	 * @param policyRequestContext
	 * @param policyName
	 * @param enable true=enable,false=disable
	 * @throws PolicyConfigurationException
	 */
	public boolean changePolicyStatus(ConfigurationContext configurationContext, String policyName, boolean isEnable) throws PolicyConfigurationException, PolicyConfigXMLParserException ;

	/**
	 * Deletes the policy from Database and cache
	 * <BR>Pseudo code
	 * Based on the PolicyRequest{tenant/site/featureGroup/feature} first get the applicable NodeId
	 *  Note:- it is possible that the policy is disabled and hence not in cache.
	 *  1.) Get PolicyconfigurationUnit from the cache by calling .getPolicyConfigurationUnit(). If its null than get the ConfigNodeDataId by quering the DB for name and policyType
	 *  2.) take the configNodeDataId and delete it from the database.
	 *  3.) Remove config from cache by calling LeapConfigurationServer.deleteConfiguration
	 * @param policyRequestContext
	 * @param policyName
	 * @return
	 * @throws PolicyConfigurationException
	 */
	public boolean deletePolicy(ConfigurationContext configurationContext,String policyName)throws PolicyConfigurationException;

    public boolean checkPolicyExistInDbAndCache(ConfigurationContext configurationContext,String policyName) throws PolicyConfigurationException,PolicyRequestException;
    
    public boolean reloadPolicyCacheObject(RequestContext requestContext, String policyName)
			throws PolicyConfigurationException;


}
