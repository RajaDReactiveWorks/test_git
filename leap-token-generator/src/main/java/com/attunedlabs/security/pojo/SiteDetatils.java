package com.attunedlabs.security.pojo;

public class SiteDetatils {

	private String internalSiteId;
	private int siteAccountId;
	private String domain;
	private String description;
	private String timezone;

	public SiteDetatils() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SiteDetatils [internalSiteId=" + internalSiteId + ", siteAccountId=" + siteAccountId + ", domain="
				+ domain + ", description=" + description + ", timezone=" + timezone + "]";
	}

	public SiteDetatils(String internalSiteId, String domain, String description, String timezone) {
		super();
		this.internalSiteId = internalSiteId;
		this.domain = domain;
		this.description = description;
		this.timezone = timezone;
	}

	public String getInternalSiteId() {
		return internalSiteId;
	}

	public void setInternalSiteId(String internalSiteId) {
		this.internalSiteId = internalSiteId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public int getSiteAccountId() {
		return siteAccountId;
	}

	public void setSiteAccountId(int siteAccountId) {
		this.siteAccountId = siteAccountId;
	}

	/**
	 * @return the timezone
	 */
	public String getTimezone() {
		return timezone;
	}

	/**
	 * @param timezone the timezone to set
	 */
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

}
