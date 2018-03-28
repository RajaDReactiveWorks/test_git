package com.attunedlabs.security.service;

import java.util.List;

import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.AccountRegistrationException;
import com.attunedlabs.security.exception.SiteRegistrationException;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.pojo.CustomerDetail;
import com.attunedlabs.security.pojo.SiteDetatils;

public interface IAccountRegistryService {

	/**
	 * 
	 * @param domain
	 * @return
	 * @throws AccountFetchException
	 */
	public int getDomainIdByDomain(String domain) throws AccountFetchException;

	/**
	 * API to register new account details
	 * 
	 * @param accountDetails
	 */
	public void addNewAccount(AccountDetails accountDetails) throws AccountRegistrationException;

	/**
	 * API to get Account details by account name
	 * 
	 * @param accountId
	 * @return
	 */
	public AccountDetails getAccountDetailsByAccountName(String accountName, String internalSiteId)
			throws AccountFetchException;

	/**
	 * 
	 * @param accountName
	 * @return
	 * @throws AccountFetchException
	 */
	public String getInternalTenantIdByAccount(String accountName) throws AccountFetchException;

	/**
	 * 
	 * @param siteDetatils
	 * @param accountName
	 * @return
	 * @throws SiteRegistrationException
	 */
	public void addNewSites(List<SiteDetatils> siteDetails, String accountName) throws SiteRegistrationException;

	/**
	 * 
	 * @param siteDetatils
	 * @param accountName
	 * @return
	 * @throws SiteRegistrationException
	 */
	public void addNewSite(SiteDetatils siteDetails, String accountName, String tenantId)
			throws SiteRegistrationException;

	/**
	 * 
	 * @param accountName
	 * @param internalTenantId
	 * @param tenantTokenExpiration
	 * @return
	 * @throws AccountRegistrationException
	 */
	public void addNewAccount(String accountName, String internalTenantId, long tenantTokenExpiration)
			throws AccountRegistrationException;

	/**
	 * 
	 * @param accountName
	 * @return
	 * @throws AccountFetchException
	 */
	public List<String> getDomainNamesByAccountName(String accountName) throws AccountFetchException;

	/**
	 * 
	 * @param tenantId
	 * @return
	 * @throws AccountFetchException
	 */
	public String getDomainNameByTenantId(String tenantId) throws AccountFetchException;

	/**
	 * 
	 * @param domain
	 * @return
	 * @throws AccountFetchException
	 */
	public String getTenantByDomain(String domain) throws AccountFetchException;

	/**
	 * 
	 * @param domain
	 * @return
	 * @throws AccountFetchException
	 */
	public String getSiteIdByDomain(String domain) throws AccountFetchException;

	/**
	 * 
	 * @param domain
	 * @return
	 * @throws AccountFetchException
	 */
	public int getAccountIdByDomain(String domain) throws AccountFetchException;

	/**
	 * 
	 * @param accountId
	 * @return
	 * @throws AccountFetchException
	 */
	public AccountDetails getAccountByAccountId(int accountId) throws AccountFetchException;

	/**
	 * 
	 * @param tenantId
	 * @return
	 * @throws AccountFetchException
	 */
	public String getAccountIdByTenant(String tenantId) throws AccountFetchException;

	/**
	 * 
	 * @param accountId
	 * @param siteId
	 * @return
	 * @throws AccountFetchException
	 */
	String getTimeZoneBySite(String accountId, String siteId) throws AccountFetchException;

	/**
	 * 
	 * @return
	 * @throws AccountFetchException
	 */
	List<CustomerDetail> getAllCustomerDetails() throws AccountFetchException;
}