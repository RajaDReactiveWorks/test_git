package com.attunedlabs.security.service;

import java.text.ParseException;
import java.util.Map;

import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.AccountUpdateException;
import com.attunedlabs.security.exception.DigestMakeException;
import com.attunedlabs.security.exception.TenantTokenValidationException;

public interface IAccountSecurityService {

	/**
	 * Prepares tenantToken, and the respective attributes and returns as
	 * HashMap
	 * 
	 * @param intTenantId
	 * @param siteId
	 * @return
	 * @throws AccountFetchException
	 * @throws DigestMakeException
	 */
	public Map<String, Object> getTenantTokenAttributes(String intTenantId, String siteId)
			throws AccountFetchException, DigestMakeException;

	/**
	 * Used to validate the tenantToken
	 * 
	 * @param internalTenant
	 * @param internalSite
	 * @param saltSecretKey
	 * @param expiration
	 * @param tenantToken
	 * @return
	 * @throws DigestMakeException
	 * @throws ParseException
	 * @throws AccountUpdateException
	 */
	public boolean validateTenantToken(String internalTenant, String internalSite, long expirationTime,
			String tenantToken) throws TenantTokenValidationException;

}
