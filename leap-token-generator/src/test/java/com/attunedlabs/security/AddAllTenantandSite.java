package com.attunedlabs.security;

import java.util.ArrayList;
import java.util.List;

import com.attunedlabs.security.exception.AccountRegistrationException;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.pojo.SiteDetatils;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;

public class AddAllTenantandSite {
	public static void main(String[] args) throws AccountRegistrationException {
		AccountDetails accountDetails = new AccountDetails();
		accountDetails.setAccountName("ALL");
		accountDetails.setExpirationCount(3600);
		accountDetails.setInternalAccountId("all");
		List<SiteDetatils> siteDetails = new ArrayList<>();
		siteDetails.add(new SiteDetatils("all", "carbon.super", "all description", "IST"));
		accountDetails.setSiteDetails(siteDetails);
		IAccountRegistryService registryService = new AccountRegistryServiceImpl();
		registryService.addNewAccount(accountDetails);
	}
}
