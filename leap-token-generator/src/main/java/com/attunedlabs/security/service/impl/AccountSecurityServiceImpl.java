package com.attunedlabs.security.service.impl;

import java.util.HashMap;
import java.util.Map;

import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.DigestMakeException;
import com.attunedlabs.security.exception.TenantTokenValidationException;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.service.IAccountSecurityService;
import com.attunedlabs.security.service.dao.AccountRegistryDao;
import com.attunedlabs.security.service.dao.AccountSecurityDao;
import com.attunedlabs.security.utils.TenantSecurityUtil;

public class AccountSecurityServiceImpl implements IAccountSecurityService {

	private AccountSecurityDao securityDao;
	private AccountRegistryDao accountRegistryDao;

	public AccountSecurityServiceImpl() {
		if (this.securityDao == null)
			this.securityDao = new AccountSecurityDao();
		if (this.accountRegistryDao == null)
			this.accountRegistryDao = new AccountRegistryDao();
	}

	@Override
	public Map<String, Object> getTenantTokenAttributes(String accountName, String siteId)
			throws AccountFetchException, DigestMakeException {
		AccountDetails accountRetails = this.securityDao.getAccountByTenantSite(accountName, siteId);
		String secret = accountRetails.getSecretKey();
		if (TenantSecurityUtil.isEmpty(secret)) {
			throw new DigestMakeException("Unable to find out secret-key of the requested tenant/site! " + siteId);
		}
		Map<String, Object> resultMap = new HashMap<>();
		long expiration = TenantSecurityUtil.getExpirationTime(accountRetails.getExpirationCount());
		resultMap.put("expiration", expiration);
		resultMap.put("account", accountName.trim());
		resultMap.put("siteId", siteId.trim());
		resultMap.put("tenantToken", TenantSecurityUtil.getMD5(accountName, siteId, secret, expiration));
		return resultMap;
	}// ..end of the method

	@Override
	public boolean validateTenantToken(String accountName, String internalSite, long expirationTime, String tenantToken)
			throws TenantTokenValidationException {

		if (expirationTime > TenantSecurityUtil.getCurrentEpoch()) {
			AccountDetails accountDetails;
			try {
				accountDetails = accountRegistryDao.getAccountByName(accountName, internalSite);
				String tokenGen = TenantSecurityUtil.getMD5(accountName.trim(), internalSite,
						accountDetails.getSecretKey(), expirationTime);
				return TenantSecurityUtil.isValid(tenantToken, tokenGen);
			} catch (AccountFetchException | DigestMakeException e) {
				throw new TenantTokenValidationException(
						"Unable to validate the tenantToken! " + e.getMessage() + " - " + e.getCause(), e);
			}
		}
		return false;
	}// ..end of the method

}