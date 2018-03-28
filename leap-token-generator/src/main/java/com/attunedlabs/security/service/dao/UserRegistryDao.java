package com.attunedlabs.security.service.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateSummary;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.jdbc.JdbcDataContext;

import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.AccountRegistrationException;
import com.attunedlabs.security.exception.DBConfigurationException;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.pojo.SiteDetatils;
import com.attunedlabs.security.pojo.UserDetails;
import com.attunedlabs.security.utils.TenantSecurityUtil;

@SuppressWarnings("unused")
public class UserRegistryDao {

	static final String TABLE_USER_DETAILS = "userdetails";
	static final String TABLE_CUSTOMER_ACCOUNT = "customeraccount";
	static final String TABLE_CUSTOMER_SITE = "customersite";

	/**
	 * 
	 * @param accountDetails
	 * @throws AccountRegistrationException
	 */
	public void addNewUser(final UserDetails userDetails) throws AccountRegistrationException {
		UpdateableDataContext dataContext = new JdbcDataContext(TenantSecurityUtil.getDBConnection());
		dataContext.executeUpdate(new UpdateScript() {
			public void run(UpdateCallback callback) {
				try {
					int accId = getAccountIdByName(userDetails.getAccountName().trim());
					if (accId == 0) {
						throw new AccountRegistrationException("Invalid account reference for the given accountName! "
								+ userDetails.getAccountName().trim());
					}
					int custId = getIdBySiteAccountDomain(accId, userDetails.getInternalSite().trim(),
							userDetails.getDomain().trim());
					if (custId == 0) {
						throw new AccountRegistrationException(
								"Invalid site reference for the given site " + userDetails.getInternalSite().trim()
										+ " & domain " + userDetails.getDomain().trim() + " ! ");
					}
					callback.insertInto(TABLE_USER_DETAILS).value("userName", userDetails.getUserName().trim())
							.value("domain", userDetails.getDomain().trim()).value("customerSiteId", custId).execute();
				} catch (AccountRegistrationException e) {
					e.printStackTrace();
				}
			};
		});
	}// ..end of the method

	/**
	 * 
	 * @param accountName
	 * @return
	 */
	public int getAccountIdByName(String accountName) {
		int id = 0;
		DataContext dataContext = new JdbcDataContext(TenantSecurityUtil.getDBConnection());
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_ACCOUNT).select("id").where("accountName")
				.eq(accountName.trim()).execute();
		Iterator<Row> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			id = Integer.valueOf(row.getValue(0).toString());
		}
		return id;
	}// ..end of the method

	/**
	 * 
	 * @param accId
	 * @param internalSite
	 * @param domain
	 * @return
	 */
	public int getIdBySiteAccountDomain(int accId, String internalSite, String domain) {
		int id = 0;
		DataContext dataContext = new JdbcDataContext(TenantSecurityUtil.getDBConnection());
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_SITE).select("id").where("siteAccountId").eq(accId)
				.where("internalSite").eq(internalSite).where("domain").eq(domain).execute();
		Iterator<Row> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			id = Integer.valueOf(row.getValue(0).toString());
		}
		return id;
	}// ..end of the method

	public AccountDetails getAccountDetatilsByUser(UserDetails userDetails) {
		SiteDetatils siteDetatils = getSiteDetatilsById(
				getCustomerSiteIdbyUser(userDetails.getUserName().trim(), userDetails.getDomain().trim()));
		String accointName = null;
		String secret = null;
		String intTenant = null;
		String desc = null;
		int expiration = 0;
		AccountDetails accountDetails = new AccountDetails();
		DataContext dataContext = new JdbcDataContext(TenantSecurityUtil.getDBConnection());
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_ACCOUNT)
				.select("accountName", "saltSecretKey", "internalTenantId", "description", "expiration").where("id")
				.eq(siteDetatils.getSiteAccountId()).execute();
		Iterator<Row> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			accointName = row.getValue(0).toString();
			secret = row.getValue(1).toString();
			intTenant = row.getValue(2).toString();
			desc = row.getValue(3).toString();
			expiration = Integer.valueOf(row.getValue(4).toString());
		}
		accountDetails.setAccountName(accointName);
		accountDetails.setExpirationCount(expiration);
		accountDetails.setInternalAccountId(intTenant);
		accountDetails.setSecretKey(secret);
		List<SiteDetatils> list = new ArrayList<SiteDetatils>();
		list.add(siteDetatils);
		accountDetails.setSiteDetails(list);
		return accountDetails;
	}

	public int getCustomerSiteIdbyUser(String userName, String domain) {
		int id = 0;
		DataContext dataContext = new JdbcDataContext(TenantSecurityUtil.getDBConnection());
		DataSet dataSet = dataContext.query().from(TABLE_USER_DETAILS).select("customerSiteId").where("userName")
				.eq(userName.trim()).where("domain").eq(domain.trim()).execute();
		Iterator<Row> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			id = Integer.valueOf(row.getValue(0).toString());
		}
		return id;
	}

	public SiteDetatils getSiteDetatilsById(int id) {
		SiteDetatils siteDetatils = new SiteDetatils();
		DataContext dataContext = new JdbcDataContext(TenantSecurityUtil.getDBConnection());
		DataSet dataSet = dataContext.query().from(TABLE_CUSTOMER_SITE)
				.select("internalSite", "domain", "description", "siteAccountId", "partitionKey").where("id").eq(id)
				.execute();
		Iterator<Row> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			Row row = (Row) iterator.next();
			siteDetatils.setInternalSiteId(row.getValue(0).toString());
			siteDetatils.setDomain(row.getValue(1).toString());
			siteDetatils.setDescription(row.getValue(2).toString());
			siteDetatils.setSiteAccountId(Integer.valueOf(row.getValue(3).toString()));
		}
		return siteDetatils;
	}
}