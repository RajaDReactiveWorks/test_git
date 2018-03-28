package com.attunedlabs.security.pojo;

import java.util.List;

public class AccountDetails {

	private String accountName;
	private String secretKey;
	private String internalAccountId;
	private int expirationCount;
	private long expirationTime;
	private List<SiteDetatils> siteDetails;

	@Override
	public String toString() {
		return "AccountDetails [accountName=" + accountName + ", internalAccountId="
				+ internalAccountId + ", expirationTime=" + expirationTime + ", siteDetails=" + siteDetails + "]";
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getInternalAccountId() {
		return internalAccountId;
	}

	public void setInternalAccountId(String internalAccountId) {
		this.internalAccountId = internalAccountId;
	}

	public List<SiteDetatils> getSiteDetails() {
		return siteDetails;
	}

	public void setSiteDetails(List<SiteDetatils> siteDetails) {
		this.siteDetails = siteDetails;
	}

	public int getExpirationCount() {
		return expirationCount;
	}

	public void setExpirationCount(int expirationCount) {
		this.expirationCount = expirationCount;
	}

	public void setExpirationTime(long l) {
		this.expirationTime = l;
	}
	public long getExpirationTime() {
		return expirationTime;
	}

}
