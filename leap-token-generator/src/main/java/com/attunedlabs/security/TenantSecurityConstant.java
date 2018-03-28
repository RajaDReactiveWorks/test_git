package com.attunedlabs.security;

public class TenantSecurityConstant {

	private TenantSecurityConstant() {
	}

	public static final String DB_CONFIG = "globalAppDeploymentConfig.properties";
	public static final String DRIVER_CLASS = "DB_DRIVER_CLASS";
	public static final String DB_URL = "DB_URL";
	public static final String DB_USER = "DB_USER";
	public static final String DB_PASS = "DB_PASSWORD";

	public static final String SEC_ALG = "SHA1PRNG";
	public static final String SEC_DIGEST = "MD5";
	public static final int DEFAULT_INTERVAL = 1200;

	public static final String ACCOUNT_ID = "account_id";
	public static final String TENANT_ID = "tenantId";
	public static final String SITE_ID = "siteId";
	public static final String EXPIRATION_TIME = "expiration_time";
	public static final String TENANT_TOKEN = "tenant_token";
	public static final String PARTITION_KEY = "partitionKey";
	
	public static final String TABLE_CUSTOMERSITE = "customersite";
	public static final String TABLE_CUSTOMERACCOUNT = "customeraccount";
	public static final String ACCOUNT_NAME = "accountName";
	public static final String INTERNAL_TENANT = "internalTenantId";

}
