package com.attunedlabs.security.pojo;

public class UserDetails {

	private String userName;
	private String accountName;
	private String internalSite;
	private String domain;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getInternalSite() {
		return internalSite;
	}

	public void setInternalSite(String internalSite) {
		this.internalSite = internalSite;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

}
