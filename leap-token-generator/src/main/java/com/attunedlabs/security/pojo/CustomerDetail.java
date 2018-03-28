package com.attunedlabs.security.pojo;

public class CustomerDetail {
	private String account_Id;
	private String siteId;

	public String getAccount_Id() {
		return account_Id;
	}

	public void setAccount_Id(String account_Id) {
		this.account_Id = account_Id;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	@Override
	public String toString() {
		return "CustomerDetails [ account_Id=" + account_Id + ", siteId=" + siteId
				+ "]";
	}

}
