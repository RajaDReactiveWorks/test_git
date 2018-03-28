package com.attunedlabs.security.service.impl;

import java.util.List;

import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.AccountRegistrationException;
import com.attunedlabs.security.exception.SecretKeyGenException;
import com.attunedlabs.security.exception.SiteRegistrationException;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.pojo.CustomerDetail;
import com.attunedlabs.security.pojo.SiteDetatils;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.dao.AccountRegistryDao;
import com.attunedlabs.security.utils.TenantSecurityUtil;

public class AccountRegistryServiceImpl implements IAccountRegistryService {

	private AccountRegistryDao accReg;

	public AccountRegistryServiceImpl() {
		this.accReg = new AccountRegistryDao();
	}

	@Override
	public int getDomainIdByDomain(String domain) throws AccountFetchException {
		return accReg.getDomainIdByDomain(domain);
	}

	@Override
	public void addNewAccount(AccountDetails accountDetails) throws AccountRegistrationException {
		if (accountDetails == null) {
			throw new AccountRegistrationException("Empty account details requested to register! ");
		}
		try {
			accountDetails.setSecretKey(TenantSecurityUtil.getSalt());
		} catch (SecretKeyGenException e) {
			throw new AccountRegistrationException("Unable to set SecretKey while registration! " + e.getMessage(), e);
		}
		List<SiteDetatils> siteDetatils = accountDetails.getSiteDetails();
		if (siteDetatils.isEmpty()) {
			throw new AccountRegistrationException("Empty site details requested to register! ");
		}
		this.accReg.addNewAccount(accountDetails);
	}// ..end of the method

	@Override
	public AccountDetails getAccountDetailsByAccountName(String accountName, String internalSiteId)
			throws AccountFetchException {
		return this.accReg.getAccountByName(accountName.trim(), internalSiteId.trim());
	}// ..end of the method

	@Override
	public String getInternalTenantIdByAccount(String accountName) throws AccountFetchException {
		return this.accReg.getTenantByAccountName(accountName.trim());
	}// ..end of the method

	@Override
	public void addNewSites(List<SiteDetatils> siteDetails, String accountName) throws SiteRegistrationException {
		int accountId;
		try {
			accountId = this.accReg.getAccountIdByName(accountName);
			for (SiteDetatils site : siteDetails) {
				this.accReg.addNewSite(site, accountId);
			}
		} catch (AccountFetchException | NumberFormatException e) {
			for (SiteDetatils site : siteDetails) {
				try {
					this.accReg.removeSite(site);
				} catch (AccountRegistrationException e1) {
					throw new SiteRegistrationException(e.getMessage() + " : " + e1.getMessage());
				}
			}
			throw new SiteRegistrationException(e.getMessage());
		}
	}

	@Override
	public void addNewAccount(String accountName, String internalTenantId, long tenantTokenExpiration)
			throws AccountRegistrationException {
		this.accReg.addNewAccount(accountName, internalTenantId, tenantTokenExpiration);
	}

	@Override
	public void addNewSite(SiteDetatils siteDetails, String accountName, String tenantId)
			throws SiteRegistrationException {
		int accountId;
		try {
			accountId = this.accReg.getAccountIdByNameAndTenantId(accountName, tenantId);
			this.accReg.addNewSite(siteDetails, accountId);
		} catch (AccountFetchException | NumberFormatException e) {
			throw new SiteRegistrationException(e.getMessage());
		}
	}

	@Override
	public List<String> getDomainNamesByAccountName(String accountName) throws AccountFetchException {
		int accountIdByName = this.accReg.getAccountIdByName(accountName);
		return this.accReg.getDominByAccountId(accountIdByName);
	}

	@Override
	public String getDomainNameByTenantId(String tenantId) throws AccountFetchException {
		return this.accReg.getDomainNameByTenantId(tenantId);
	}

	@Override
	public String getTenantByDomain(String domain) throws AccountFetchException {
		return this.accReg.getTenantByDomain(domain);
	}

	@Override
	public String getSiteIdByDomain(String domain) throws AccountFetchException {
		return this.accReg.getSiteIdByDomain(domain);
	}

	@Override
	public int getAccountIdByDomain(String domain) throws AccountFetchException {
		return this.accReg.getAccountIdByDomain(domain);
	}

	@Override
	public AccountDetails getAccountByAccountId(int accountId) throws AccountFetchException {
		return accReg.getAccountByAccountId(accountId);
	}
	
	@Override
	public List<CustomerDetail> getAllCustomerDetails() throws AccountFetchException {
		return accReg.getAllCustomerDetails();
	}

	@Override
	public String getTimeZoneBySite(String accountId, String siteId) throws AccountFetchException {
		return accReg.getTimeZoneBySite(accountId, siteId);
	}

	@Override
	public String getAccountIdByTenant(String tenantId) throws AccountFetchException {
		return accReg.getAccountIdByTenant(tenantId);
	}
}