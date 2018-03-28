package com.attunedlabs.security.service.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateSummary;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;

import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.AccountRegistrationException;
import com.attunedlabs.security.exception.AccountUpdateException;
import com.attunedlabs.security.exception.SecretKeyGenException;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.pojo.CustomerDetail;
import com.attunedlabs.security.pojo.SiteDetatils;
import com.attunedlabs.security.utils.TenantSecurityUtil;

public class AccountRegistryDao {
	static final String TABLE_CUSTOMER_ACCOUNT = "customeraccount";
	static final String TABLE_CUSTOMER_SITE = "customersite";
	static final String TABLE_ACCOUNT_SITE = "accountsite";
	private Connection connection;
	private DataContext dataContext;
	private UpdateableDataContext updatableDataContext;

	public AccountRegistryDao() {
		if (this.connection == null)
			this.connection = TenantSecurityUtil.getDBConnection();
		if (this.dataContext == null)
			this.dataContext = new JdbcDataContext(connection);
		this.updatableDataContext = (UpdateableDataContext) this.dataContext;
	}

	/**
	 * 
	 * @param accountDetails
	 * @throws AccountRegistrationException
	 */
	public void addNewAccount(final String accountName, final String internalTenantId, final long tenantTokenExpiration)
			throws AccountRegistrationException {
		try {
			final String saltKey = TenantSecurityUtil.getSalt();
			this.updatableDataContext.executeUpdate(new UpdateScript() {
				public void run(UpdateCallback callback) {
					callback.insertInto(TABLE_CUSTOMER_ACCOUNT).value("accountName", accountName)
							.value("internalTenantId", internalTenantId).value("saltSecretKey", saltKey.trim())
							.value("description", accountName + " & " + internalTenantId)
							.value("tenantTokenExpiration", tenantTokenExpiration).execute();
				};
			});
		} catch (SecretKeyGenException e) {
			throw new AccountRegistrationException(
					"Unable to register the account details! " + e.getMessage() + e.getCause(), e);
		}
	}// ..end of the method

	public void addNewAccount(final AccountDetails accountDetails) throws AccountRegistrationException {
		UpdateSummary summary = this.updatableDataContext.executeUpdate(new UpdateScript() {
			public void run(UpdateCallback callback) {
				int interval;
				try {
					interval = accountDetails.getExpirationCount();
					if (interval == 0)
						interval = TenantSecurityConstant.DEFAULT_INTERVAL;
				} catch (Exception e) {
					interval = TenantSecurityConstant.DEFAULT_INTERVAL;
				}
				callback.insertInto(TABLE_CUSTOMER_ACCOUNT).value("accountName", accountDetails.getAccountName().trim())
						.value("saltSecretKey", accountDetails.getSecretKey().trim())
						.value("internalTenantId", accountDetails.getInternalAccountId().trim())
						.value("tenantTokenExpiration", interval).execute();
			};
		});
		if ((summary.getInsertedRows().get() > 0) && (summary.getGeneratedKeys().isPresent())) {
			Optional<Iterable<Object>> generatedKeys = summary.getGeneratedKeys();
			for (SiteDetatils eachSiteDetails : accountDetails.getSiteDetails()) {
				addNewSite(eachSiteDetails, Integer.valueOf(generatedKeys.get().iterator().next().toString()));
			}
		}
	}// ..end
		// of
		// the
		// method

	/**
	 * 
	 * @param eachSiteDetails
	 * @param accountId
	 * @throws AccountRegistrationException
	 */
	public void addNewSite(final SiteDetatils eachSiteDetails, final int accountId) {
		this.updatableDataContext.executeUpdate(new UpdateScript() {
			public void run(UpdateCallback callback) {
				callback.insertInto(TABLE_CUSTOMER_SITE).value("accountId", accountId)
						.value("siteId", eachSiteDetails.getInternalSiteId().trim())
						.value("domain", eachSiteDetails.getDomain().trim())
						.value("description", eachSiteDetails.getDescription().trim())
						.value("timezone", eachSiteDetails.getTimezone().trim()).execute();
			};
		});
	}// ..end of the method

	/**
	 * 
	 * @param accountName
	 * @return
	 * @throws AccountFetchException
	 */
	public int getAccountIdByNameAndTenantId(String accountName, String tenantId) throws AccountFetchException {
		Table customerAccountTable = dataContext.getTableByQualifiedLabel(TABLE_CUSTOMER_ACCOUNT);
		Query query = new Query();
		query.from(TABLE_CUSTOMER_ACCOUNT);
		query.select(customerAccountTable.getColumnByName("accountId"));
		query.where(customerAccountTable.getColumnByName("accountName"), OperatorType.EQUALS_TO, accountName);
		query.where(customerAccountTable.getColumnByName("internalTenantId"), OperatorType.EQUALS_TO, tenantId);
		DataSet dataSet = dataContext.executeQuery(query);
		Iterator<Row> iterator = dataSet.iterator();
		int accountId = 0;
		while (iterator.hasNext()) {
			Row row = iterator.next();
			accountId = Integer.valueOf(row.getValue(0).toString());
		}
		if (accountId == 0)
			throw new AccountFetchException("No account associated with the given account name : " + accountName);
		return accountId;
	}// ..end of the method

	/**
	 * 
	 * @param accountName
	 * @return
	 * @throws AccountFetchException
	 */
	public int getAccountIdByName(String accountName) throws AccountFetchException {
		Table customerAccountTable = dataContext.getTableByQualifiedLabel(TABLE_CUSTOMER_ACCOUNT);
		Query query = new Query();
		query.from(TABLE_CUSTOMER_ACCOUNT);
		query.select(customerAccountTable.getColumnByName("accountId"));
		query.where(customerAccountTable.getColumnByName("accountName"), OperatorType.EQUALS_TO, accountName);
		DataSet dataSet = dataContext.executeQuery(query);
		Iterator<Row> iterator = dataSet.iterator();
		int accountId = 0;
		while (iterator.hasNext()) {
			Row row = iterator.next();
			accountId = Integer.valueOf(row.getValue(0).toString());
		}
		if (accountId == 0)
			throw new AccountFetchException("No account associated with the given account name : " + accountName);
		return accountId;
	}// ..end of the method

	/**
	 * 
	 * @param accountName
	 * @return
	 * @throws AccountFetchException
	 */
	public AccountDetails getAccountByName(String accountName) throws AccountFetchException {
		Table customerAccountTable = dataContext.getTableByQualifiedLabel(TABLE_CUSTOMER_ACCOUNT);
		Table accountSiteTable = dataContext.getTableByQualifiedLabel(TABLE_ACCOUNT_SITE);
		Query query = new Query();
		query.from(TABLE_CUSTOMER_ACCOUNT).from(TABLE_ACCOUNT_SITE);
		query.select(customerAccountTable.getColumnByName("id"), customerAccountTable.getColumnByName("saltSecretKey"),
				customerAccountTable.getColumnByName("internalTenantId"),
				customerAccountTable.getColumnByName("tenantTokenExpiration"));
		query.select(accountSiteTable.getColumnByName("accountId"), accountSiteTable.getColumnByName("description"));
		query.where(accountSiteTable.getColumnByName("accountId"), OperatorType.EQUALS_TO,
				customerAccountTable.getColumnByName("accountId"));
		query.where(customerAccountTable.getColumnByName("accountName"), OperatorType.EQUALS_TO, accountName);
		DataSet dataSet = dataContext.executeQuery(query);

		Iterator<Row> iterator = dataSet.iterator();
		AccountDetails accountDetails = new AccountDetails();
		// int accountId;
		String secret = null;
		String intId = null;
		int expTime = 0;
		// String site;
		String desc;
		List<SiteDetatils> list = new ArrayList<>();
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			// accountId = Integer.valueOf(row.getValue(0).toString());
			secret = row.getValue(1).toString();
			intId = row.getValue(2).toString();
			expTime = Integer.valueOf(row.getValue(3).toString());
			// site = row.getValue(4).toString();
			desc = row.getValue(5).toString();
			SiteDetatils siteDetatils = new SiteDetatils();
			siteDetatils.setDescription(desc);
			list.add(siteDetatils);
		}
		accountDetails.setInternalAccountId(intId);
		accountDetails.setAccountName(accountName.trim());
		accountDetails.setExpirationCount(expTime);
		accountDetails.setSecretKey(secret);
		accountDetails.setSiteDetails(list);
		return accountDetails;
	}// ..end of the method

	public AccountDetails getAccountByAccountId(int accountId) throws AccountFetchException {
		Table customerAccountTable = (Table) dataContext.getTableByQualifiedLabel(TABLE_CUSTOMER_ACCOUNT);
		Table accountSiteTable = (Table) dataContext.getTableByQualifiedLabel(TABLE_CUSTOMER_SITE);
		Query query = new Query();
		query.from(TABLE_CUSTOMER_ACCOUNT).from(TABLE_CUSTOMER_SITE);
		query.select(customerAccountTable.getColumnByName("accountName"),
				customerAccountTable.getColumnByName("saltSecretKey"),
				customerAccountTable.getColumnByName("internalTenantId"),
				customerAccountTable.getColumnByName("tenantTokenExpiration"));
		query.select(accountSiteTable.getColumnByName("accountId"), accountSiteTable.getColumnByName("description"));
		query.where(accountSiteTable.getColumnByName("accountId"), OperatorType.EQUALS_TO,
				customerAccountTable.getColumnByName("accountId"));
		query.where(customerAccountTable.getColumnByName("accountId"), OperatorType.EQUALS_TO, accountId);
		DataSet dataSet = dataContext.executeQuery(query);

		Iterator<Row> iterator = dataSet.iterator();
		AccountDetails accountDetails = new AccountDetails();
		String accountName = "";
		String secret = "";
		String intId = "";
		int expTime = 0;
		// String site;
		String desc;
		List<SiteDetatils> list = new ArrayList<>();
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			accountName = row.getValue(0).toString();
			secret = row.getValue(1).toString();
			intId = row.getValue(2).toString();
			expTime = Integer.valueOf(row.getValue(3).toString());
			// site = row.getValue(4).toString();
			desc = row.getValue(5).toString();
			SiteDetatils siteDetatils = new SiteDetatils();
			siteDetatils.setDescription(desc);
			list.add(siteDetatils);
		}
		accountDetails.setInternalAccountId(intId);
		accountDetails.setAccountName(accountName);
		accountDetails.setExpirationCount(expTime);
		accountDetails.setSecretKey(secret);
		accountDetails.setSiteDetails(list);
		return accountDetails;
	}// ..end of the method

	/**
	 * 
	 * @param accountName
	 * @return
	 * @throws AccountFetchException
	 */
	public String getTenantByAccountName(String accountName) throws AccountFetchException {
		String intId = null;
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_ACCOUNT).select("internalTenantId")
				.where("accountName").eq(accountName).execute();
		Iterator<Row> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			intId = row.getValue(0).toString();
		}
		return intId;
	}// ..end
		// of
		// the
		// method

	/**
	 * 
	 * @param accountName
	 * @return
	 * @throws AccountFetchException
	 */
	public AccountDetails getAccountByName(String accountName, String internalSiteId) throws AccountFetchException {
		try {
			Table customerAccountTable = dataContext.getTableByQualifiedLabel(TABLE_CUSTOMER_ACCOUNT);
			Table customerSiteTable = dataContext.getTableByQualifiedLabel(TABLE_CUSTOMER_SITE);
			Query query = new Query();
			query.from(TABLE_CUSTOMER_ACCOUNT).from(TABLE_CUSTOMER_SITE);
			query.select(customerAccountTable.getColumnByName("accountId"),
					customerAccountTable.getColumnByName("saltSecretKey"),
					customerAccountTable.getColumnByName("internalTenantId"),
					customerAccountTable.getColumnByName("tenantTokenExpiration"));
			query.select(customerSiteTable.getColumnByName("accountId"),
					customerSiteTable.getColumnByName("description"), customerSiteTable.getColumnByName("domain"));
			query.where(customerSiteTable.getColumnByName("accountId"), OperatorType.EQUALS_TO,
					customerAccountTable.getColumnByName("accountId"));
			query.where(customerAccountTable.getColumnByName("accountName"), OperatorType.EQUALS_TO, accountName);
			query.where(customerSiteTable.getColumnByName("siteId"), OperatorType.EQUALS_TO, internalSiteId);
			DataSet dataSet = dataContext.executeQuery(query);
			Iterator<Row> iterator = dataSet.iterator();
			String secret = null;
			int expTime = 0;
			String desc;
			String intTen = null;
			List<SiteDetatils> list = new ArrayList<>();
			SiteDetatils siteDetatils = new SiteDetatils();
			while (iterator.hasNext()) {
				Row row = iterator.next();
				secret = row.getValue(1).toString();
				intTen = row.getValue(2).toString();
				expTime = Integer.valueOf(row.getValue(3).toString());
				desc = row.getValue(5).toString();
				siteDetatils.setInternalSiteId(internalSiteId.trim());
				siteDetatils.setDescription(desc);
				siteDetatils.setDomain(row.getValue(6).toString());
				list.add(siteDetatils);
			}
			AccountDetails accountDetails = new AccountDetails();
			accountDetails.setInternalAccountId(intTen.trim());
			accountDetails.setAccountName(accountName);
			accountDetails.setExpirationCount(expTime);
			accountDetails.setExpirationTime(TenantSecurityUtil.getExpirationTime(expTime));
			accountDetails.setSecretKey(secret);
			accountDetails.setSiteDetails(list);
			return accountDetails;
		} catch (NullPointerException e) {
			throw new AccountFetchException("Unable to get the account details! " + e.getMessage() + e.getCause(), e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param internalTenantId
	 * @return
	 * @throws AccountFetchException
	 */
	public String getAccountSalt(String internalTenantId) throws AccountFetchException {
		String secret = null;
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_ACCOUNT).select("saltSecretKey")
				.where("internalTenantId").eq(internalTenantId.trim()).execute();
		Iterator<Row> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			secret = row.getValue(0).toString();
		}
		return secret;
	}// ..end of the method

	public void removeSite(final SiteDetatils site) throws AccountRegistrationException {
		this.updatableDataContext.executeUpdate(new UpdateScript() {
			public void run(UpdateCallback callback) {
				callback.deleteFrom(TABLE_CUSTOMER_SITE).where("siteId").eq(site.getInternalSiteId()).execute();
			};
		});
	}

	public List<String> getDominByAccountId(int accountIdByName) throws AccountFetchException {
		List<String> domains = new ArrayList<>();
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_SITE).select("domain").where("accountId")
				.eq(accountIdByName).execute();
		Iterator<Row> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			domains.add(row.getValue(0).toString());
		}
		return domains;
	}

	public String getDomainNameByTenantId(String tenantId) throws AccountFetchException {
		int accountId = getAccountIdByTenantId(tenantId);
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_SITE).select("domain").where("accountId")
				.eq(accountId).execute();
		Iterator<Row> iterator = dataSet.iterator();
		String domainName = "";
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			domainName = row.getValue(0).toString();
		}
		if (TenantSecurityUtil.isEmpty(domainName))
			throw new AccountFetchException("No account found for the tenantId");
		return domainName;
	}

	public int getAccountIdByTenantId(String tenantId) throws AccountFetchException {
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_ACCOUNT).select("accountId").where("internalTenantId")
				.eq(tenantId).execute();
		Iterator<Row> iterator = dataSet.iterator();
		int accountId = 0;
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			accountId = Integer.valueOf(row.getValue(0).toString());
		}
		if (accountId == 0)
			throw new AccountFetchException("No account found for the tenantId");
		return accountId;
	}

	public String getTenantByDomain(String domain) throws AccountFetchException {
		int accountId = getAccountIdByDomain(domain);
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_ACCOUNT).select("internalTenantId").where("accountId")
				.eq(accountId).execute();
		Iterator<Row> iterator = dataSet.iterator();
		String tenantId = "";
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			tenantId = row.getValue(0).toString();
		}
		if (TenantSecurityUtil.isEmpty(tenantId))
			throw new AccountFetchException("No tenant found for the domain");
		return tenantId;
	}

	public int getAccountIdByDomain(String domain) throws AccountFetchException {
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_SITE).select("accountId").where("domain").eq(domain)
				.execute();
		Iterator<Row> iterator = dataSet.iterator();
		int accountId = 0;
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			accountId = Integer.valueOf(row.getValue(0).toString());
		}
		if (accountId == 0)
			throw new AccountFetchException("No account found for the domain");
		return accountId;
	}

	public String getSiteIdByDomain(String domain) throws AccountFetchException {
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_SITE).select("siteId").where("domain").eq(domain)
				.execute();
		Iterator<Row> iterator = dataSet.iterator();
		String siteId = "";
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			siteId = row.getValue(0).toString();
		}
		if (TenantSecurityUtil.isEmpty(siteId))
			throw new AccountFetchException("No tenant found for the domain");
		return siteId;
	}

	public int getDomainIdByDomain(String domain) throws AccountFetchException {
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_SITE).select("id").where("domain").eq(domain)
				.execute();
		Iterator<Row> iterator = dataSet.iterator();
		int domainId = 0;
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			domainId = Integer.valueOf(row.getValue(0).toString());
		}
		if (domainId == 0)
			throw new AccountFetchException("No domainId found for the domain");
		return domainId;
	}

	public void removeSiteByDomainId(final int domainId) throws AccountFetchException {
		this.updatableDataContext.executeUpdate(new UpdateScript() {
			public void run(UpdateCallback callback) {
				callback.deleteFrom(TABLE_CUSTOMER_SITE).where("id").eq(domainId).execute();
			};
		});
	}

	public void removeAccountByDomainId(final String accountName) throws AccountUpdateException {
		this.updatableDataContext.executeUpdate(new UpdateScript() {
			public void run(UpdateCallback callback) {
				callback.deleteFrom(TABLE_CUSTOMER_ACCOUNT).where("accountName").eq(accountName).execute();
			};
		});
	}

	public List<CustomerDetail> getAllCustomerDetails() throws AccountFetchException {
		List<CustomerDetail> customerDetails = new ArrayList<CustomerDetail>();
		CustomerDetail customerDetail;
		try {
			initializeConnection();
			final Table customersite_table = dataContext
					.getTableByQualifiedLabel(TenantSecurityConstant.TABLE_CUSTOMERSITE);
			final Table customeraccount_table = dataContext
					.getTableByQualifiedLabel(TenantSecurityConstant.TABLE_CUSTOMERACCOUNT);
			DataSet dataSet = dataContext.query().from(customersite_table).innerJoin(customeraccount_table)
					.on("accountId", "accountId").selectAll().execute();
			Column siteId = customersite_table.getColumnByName("siteId");
			Column accountId = customeraccount_table.getColumnByName("accountName");
			while (dataSet.next()) {
				Row row = dataSet.getRow();
				customerDetail = new CustomerDetail();
				customerDetail.setAccount_Id((String) row.getValue(accountId));
				customerDetail.setSiteId((String) row.getValue(siteId));
				customerDetails.add(customerDetail);
			}
		} catch (Exception e) {
			throw new AccountFetchException("Unable to get all account details", e);
		} finally {
			TenantSecurityUtil.dbCleanUp(connection, null);
		}
		return customerDetails;
	}

	public String getTimeZoneBySite(String accountId, String siteId) throws AccountFetchException {
		String result = "";
		try {
			initializeConnection();
			UpdateableDataContext dataContext = (UpdateableDataContext) new JdbcDataContext(connection);
			final Table customersite_table = dataContext
					.getTableByQualifiedLabel(TenantSecurityConstant.TABLE_CUSTOMERSITE);
			final Table customeraccount_table = dataContext
					.getTableByQualifiedLabel(TenantSecurityConstant.TABLE_CUSTOMERACCOUNT);
			Column timeZone = customersite_table.getColumnByName("timezone");
			DataSet dataSet = dataContext.query().from(customersite_table).innerJoin(customeraccount_table)
					.on("accountId", "accountId").selectAll().where(TenantSecurityConstant.ACCOUNT_NAME).eq(accountId)
					.where(TenantSecurityConstant.SITE_ID).eq(siteId).execute();
			while (dataSet.next()) {
				Row row = dataSet.getRow();
				result = (String) row.getValue(timeZone);
			}
		} catch (Exception e) {
			throw new AccountFetchException("Unable to get timezone for the requested accountId and siteId", e);
		} finally {
			TenantSecurityUtil.dbCleanUp(connection, null);
		}
		return result;
	}

	public String getAccountIdByTenant(String tenantId) throws AccountFetchException {
		String accountId = null;
		try {
			initializeConnection();
			UpdateableDataContext updatableDataContext = (UpdateableDataContext) new JdbcDataContext(connection);
			final Table customeraccount_table = updatableDataContext
					.getTableByQualifiedLabel(TenantSecurityConstant.TABLE_CUSTOMERACCOUNT);
			Column accountId_col = customeraccount_table.getColumnByName(TenantSecurityConstant.ACCOUNT_NAME);
			DataSet dataSet = updatableDataContext.query().from(customeraccount_table).select(accountId_col)
					.where(TenantSecurityConstant.INTERNAL_TENANT).eq(tenantId).execute();
			if (dataSet.next()) {
				Row row = dataSet.getRow();
				accountId = (String) row.getValue(accountId_col);
			}
		} catch (Exception e) {
			throw new AccountFetchException("Unable to get accountId by tenantId", e);
		} finally {
			TenantSecurityUtil.dbCleanUp(connection, null);
		}
		return accountId;
	}

	private void initializeConnection() throws SQLException {
		if (connection == null) {
			connection = TenantSecurityUtil.getDBConnection();
			this.dataContext = new JdbcDataContext(connection);
			this.updatableDataContext = (UpdateableDataContext) this.dataContext;
		} else if (connection != null && connection.isClosed()) {
			connection = TenantSecurityUtil.getDBConnection();
			this.dataContext = new JdbcDataContext(connection);
			this.updatableDataContext = (UpdateableDataContext) this.dataContext;
		}
	}

}