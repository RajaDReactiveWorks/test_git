package com.attunedlabs.security.service.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Table;

import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.AccountUpdateException;
import com.attunedlabs.security.exception.SecretKeyGenException;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.pojo.SiteDetatils;
import com.attunedlabs.security.utils.TenantSecurityUtil;

public class AccountSecurityDao {

	static final String TABLE_CUSTOMER_ACCOUNT = "customeraccount";
	static final String TABLE_CUSTOMER_SITE = "customersite";
	private Connection connection;
	private DataContext dataContext;
	private UpdateableDataContext updatableDataContext;

	public AccountSecurityDao() {
		if (this.connection == null)
			this.connection = TenantSecurityUtil.getDBConnection();
		if (this.dataContext == null)
			this.dataContext = new JdbcDataContext(connection);
		this.updatableDataContext = (UpdateableDataContext) this.dataContext;
	}

	/**
	 * 
	 * @param accountName
	 * @param siteId
	 * @return
	 * @throws AccountFetchException
	 */
	public AccountDetails getAccountByTenantSite(String accountName, String siteId) {
		Table customerAccountTable = dataContext.getTableByQualifiedLabel(TABLE_CUSTOMER_ACCOUNT);
		Table customerSiteTable = dataContext.getTableByQualifiedLabel(TABLE_CUSTOMER_SITE);
		Query query = new Query();
		query.from(TABLE_CUSTOMER_ACCOUNT).from(TABLE_CUSTOMER_SITE);
		query.select(customerAccountTable.getColumnByName("accountName"),
				customerAccountTable.getColumnByName("accountId"),
				customerAccountTable.getColumnByName("saltSecretKey"),
				customerAccountTable.getColumnByName("tenantTokenExpiration"));
		query.select(customerSiteTable.getColumnByName("accountId"), customerSiteTable.getColumnByName("description"),
				customerSiteTable.getColumnByName("domain"));
		query.where(customerSiteTable.getColumnByName("accountId"), OperatorType.EQUALS_TO,
				customerAccountTable.getColumnByName("accountId"));
		query.where(customerAccountTable.getColumnByName("accountName"), OperatorType.EQUALS_TO, accountName);
		query.where(customerSiteTable.getColumnByName("siteId"), OperatorType.EQUALS_TO, siteId);
		DataSet dataSet = dataContext.executeQuery(query);
		Iterator<Row> iterator = dataSet.iterator();
		AccountDetails accountDetails = new AccountDetails();
		String secret = null;
		int expTime = 0;
		String desc;
		String accountName1 = null;
		List<SiteDetatils> list = new ArrayList<>();
		SiteDetatils siteDetatils = new SiteDetatils();
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			accountName1 = row.getValue(0).toString();
			secret = row.getValue(2).toString();
			expTime = Integer.valueOf(row.getValue(3).toString());
			desc = row.getValue(4).toString();
			siteDetatils.setInternalSiteId(siteId.trim());
			siteDetatils.setDescription(desc);
			siteDetatils.setDomain(row.getValue(5).toString());
			list.add(siteDetatils);
		}
		accountDetails.setInternalAccountId(accountName1.trim());
		accountDetails.setAccountName(accountName);
		accountDetails.setExpirationCount(expTime);
		accountDetails.setExpirationTime(TenantSecurityUtil.getExpirationTime(expTime));
		accountDetails.setSecretKey(secret);
		accountDetails.setSiteDetails(list);
		return accountDetails;
	}// ..end of the method

	/**
	 * 
	 * @param internalTenant
	 * @throws AccountUpdateException
	 */
	public void updateSaltForAccount(final String internalTenant) {
		this.updatableDataContext.executeUpdate(new UpdateScript() {
			public void run(UpdateCallback callback) {
				try {
					callback.update(TABLE_CUSTOMER_ACCOUNT).where("internalTenantId").eq(internalTenant.trim())
							.value("saltSecretKey", TenantSecurityUtil.getSalt()).execute();
				} catch (MetaModelException | IllegalArgumentException | IllegalStateException
						| UnsupportedOperationException | SecretKeyGenException e) {
					e.printStackTrace();
				}
			};
		});
	}// ..end of the method

}