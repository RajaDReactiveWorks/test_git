package com.attunedlabs.leap.header.initializer;

import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.IS_AUTH;

import java.util.Map;
import java.util.Properties;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineExecutionUtil;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.attunedlabs.osgi.helper.OSGIEnvironmentHelper;
import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.dao.AccountRegistryDao;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.HazelcastXAResource;
import com.hazelcast.transaction.TransactionContext;

public class FeatureHeaderInitializer implements Processor {

	protected static final Logger logger = LoggerFactory.getLogger(FeatureHeaderInitializer.class);
	private UserTransactionManager userTransactionManager;

	/**
	 * This method is to set user transaction manager object
	 * 
	 * @param userTransactionManager
	 *            : UserTransactionManager Object
	 */
	public void setUserTransactionManager(UserTransactionManager userTransactionManager) {
		this.userTransactionManager = userTransactionManager;
	}

	private TransactionManager transactionManager;

	/**
	 * This method is to set user transaction manager object
	 * 
	 * @param TransactionManager
	 *            : UserTransactionManager Object
	 */
	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * This method is used to set header for request servicetype,tenantid and data
	 * 
	 * @param exchange
	 *            : Exchange Object
	 * @throws JsonParserException
	 * @throws InvalidXATransactionException
	 * @throws JSONException
	 * @throws AccountFetchException
	 * @throws FeatureHeaderInitialzerException
	 */
	public void process(Exchange exchange) throws JsonParserException, InvalidXATransactionException, JSONException,
			AccountFetchException, FeatureHeaderInitialzerException {
		try {

			// checking if subscription call or not set the extra property
			if (exchange.getProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY) == null)
				exchange.setProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY, false);

			logger.debug("=============================================================================");
			logger.debug("featureHeaderInit[start] : " + System.currentTimeMillis());
			logger.debug("inside Header Initializer bean process()");
			logger.debug("exchange messgae : " + exchange.getIn().getHeaders());
			String completdata = exchange.getIn().getBody(String.class);
			logger.debug("completdata : " + PipelineExecutionUtil.convertToSingleLine(completdata));
			String tenant = null;
			String site = null;
			String servicetype = (String) exchange.getIn().getHeader(LeapHeaderConstant.SERVICETYPE_KEY);
			String featureGroup = (String) exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY);
			String featureName = (String) exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY);
			String endpointType = (String) exchange.getIn().getHeader(LeapHeaderConstant.ENDPOINT_TYPE_KEY);
			LeapHeader leapHeaderAlreadyExists = (LeapHeader) exchange.getIn()
					.getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
			logger.debug("leapHeaderAlreadyExists : " + leapHeaderAlreadyExists);
			Properties properties = LeapConfigurationUtil.loadingPropertiesFile();
			logger.debug("properties : " + properties);
			String isAuthenticated = properties.getProperty(IS_AUTH);
			logger.debug("isAuthenticated : " + isAuthenticated);
			Boolean auth = Boolean.valueOf(isAuthenticated.trim());
			logger.debug("auth : " + auth);
			LeapHeader leapHeader = new LeapHeader();
			if (leapHeaderAlreadyExists != null) {
				logger.debug("Header already exist.....");
				tenant = leapHeaderAlreadyExists.getTenant();
				site = leapHeaderAlreadyExists.getSite();

				// for subscription set the leapHeader
				if (exchange.getProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY, Boolean.class)) {
					Map<String, Object> oldLeap = leapHeader.getOriginalLeapHeader();
					oldLeap.put(LeapHeaderConstant.ORIGINAL_LEAP_HEADER_KEY, leapHeaderAlreadyExists);
				}
				if (!tenant.equalsIgnoreCase("all") && featureGroup.equalsIgnoreCase("authentication")
						&& featureName.equalsIgnoreCase("authenticationservice")) {
					tenant = "all";
					site = "all";
				}
			} else if (isAuthenticated.equalsIgnoreCase("true")) {
				logger.debug("No header exixts.. featureName: " + featureName + "featureGroup : " + featureGroup);
				if (featureGroup.equalsIgnoreCase("authentication")
						&& featureName.equalsIgnoreCase("authenticationservice")) {
					tenant = "all";
					site = "all";
				} else if (!auth) {
					tenant = "all";
					site = "all";
				} else {
					IAccountRegistryService accountRegistryService = new AccountRegistryServiceImpl();
					tenant = accountRegistryService.getInternalTenantIdByAccount(
							(String) exchange.getIn().getHeader(TenantSecurityConstant.ACCOUNT_ID));
					site = (String) exchange.getIn().getHeader(TenantSecurityConstant.SITE_ID);
				}
				AccountRegistryDao accountRegistryDao = new AccountRegistryDao();
				String internalSiteId = (String) exchange.getIn().getHeader(LeapHeaderConstant.SITE_KEY);
				String accountName = (String) exchange.getIn().getHeader(LeapHeaderConstant.ACCOUNT_ID);
				AccountDetails accountByName = accountRegistryDao.getAccountByName(accountName, internalSiteId);
				String internalTenantId = accountByName.getInternalAccountId();
				logger.debug("internalTenantId : " + internalTenantId + ", internalSiteId : " + internalSiteId);
				if (internalTenantId == null || internalSiteId == null)
					throw new RuntimeException("siteId or account_id is missing in the header.");
			} else {
				logger.debug("inside else");
				if (RootDeployableConfiguration.isRootDeployableFeature(featureGroup, featureName)) {
					logger.debug("inside else if");
					tenant = "all";
					site = "all";
				} else {
					logger.debug("inside else else");
					IAccountRegistryService accountRegistryService = new AccountRegistryServiceImpl();
					tenant = accountRegistryService.getInternalTenantIdByAccount(
							(String) exchange.getIn().getHeader(TenantSecurityConstant.ACCOUNT_ID));
					site = (String) exchange.getIn().getHeader(TenantSecurityConstant.SITE_ID);
					logger.debug("tenant : " + tenant + ", site : " + site);
				}
			}
			leapHeader.setTenant(tenant);
			leapHeader.setSite(site);
			leapHeader.setServicetype(servicetype);
			leapHeader.setFeatureGroup(featureGroup);
			leapHeader.setFeatureName(featureName);
			if (endpointType != null) {
				leapHeader.setEndpointType(endpointType);
			} else {
				leapHeader.setEndpointType("HTTP-JSON");
			}
			if (completdata != null) {
				hazelcastTransaction(leapHeader);
				exchange.getIn().setBody(completdata);
			}
			exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
			logger.debug("leap header in meshInitializer class : " + leapHeader);
		} catch (SystemException e) {
			LeapConfigurationUtil.setResponseCode(422, exchange, e.getMessage());
			throw new InvalidXATransactionException("unable to build the xa hazelcast transactions ", e);
		} catch (PropertiesConfigException e) {
			LeapConfigurationUtil.setResponseCode(500, exchange, e.getMessage());
			throw new FeatureHeaderInitialzerException("unable to load the properties file", e);
		}

	}

	/**
	 * This method ised used to take xml request and convert it into json format
	 * requried
	 * 
	 * @param exchange
	 *            : Exchange Object
	 */
	public void processXmlRequest(Exchange exchange) {
		logger.debug(".processXmlRequest method of FeatureHeaderInitializer");
		String xmlRequest = exchange.getIn().getBody(String.class);
		logger.debug("xml request : " + xmlRequest);
		String jsonData = "{\"data\":[{\"xmldata\":" + xmlRequest + "}]";
		logger.debug("required jsondata : " + jsonData);
	}

	/**
	 * This methis is to start singleton hazelcast instance and set into header
	 * 
	 * @param exchange
	 *            :Exchange Object
	 * @throws SystemException
	 * @throws InvalidXATransactionException
	 */
	private void hazelcastTransaction(LeapHeader meshheader) throws SystemException, InvalidXATransactionException {

		logger.debug("inside service Processor bean hazelcast Transaction()");

		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		logger.debug(" hazelcast instance value in transactionalExecution() : " + hazelcastInstance);
		TransactionContext context1 = null;
		logger.debug("OSGIEnvironmentHelper.isOSGIEnabled in initializer : " + OSGIEnvironmentHelper.isOSGIEnabled);
		if (OSGIEnvironmentHelper.isOSGIEnabled) {
			context1 = hazelcastEnlistingInOsgi(hazelcastInstance);

		} else {

			context1 = hazelcastEnlistingInNonOsgi(hazelcastInstance);
		}

		logger.debug("hazelcastTransactionContext : " + context1);
		meshheader.setHazelcastTransactionalContext(context1);

		logger.debug("--------------------");

	}

	private TransactionContext hazelcastEnlistingInNonOsgi(HazelcastInstance hazelcastInstance)
			throws InvalidXATransactionException {
		HazelcastXAResource xaResource = hazelcastInstance.getXAResource();
		logger.debug("xa resource object in : " + xaResource);
		Transaction transaction = null;
		try {
			logger.debug("*******inside try block for enlist *********User transaction=" + userTransactionManager);
			transaction = userTransactionManager.getTransaction();
			logger.debug("*******inside try block for enlist *********transaction=" + transaction);
			transaction.enlistResource(xaResource);
			logger.debug("*******successfuly enlisted *********");

		} catch (Exception e) {
			logger.error("exception while enlisting in hazelcast transaction : " + e);
			throw new InvalidXATransactionException("exception while enlisting in hazelcast transaction ", e);
		}

		TransactionContext context1 = xaResource.getTransactionContext();

		return context1;
	}

	private TransactionContext hazelcastEnlistingInOsgi(HazelcastInstance hazelcastInstance)
			throws InvalidXATransactionException {
		logger.debug(".hazelcastEnlistingInOsgi of FeatureHeaderInitializer ");
		HazelcastXAResource xaResource = hazelcastInstance.getXAResource();
		logger.debug("xa resource object in : " + xaResource);
		Transaction transaction = null;
		try {
			logger.debug("*******inside try block for enlist *********User transaction=" + transactionManager);
			transaction = transactionManager.getTransaction();
			logger.debug("*******inside try block for enlist *********transaction=" + transaction);
			transaction.enlistResource(xaResource);
			logger.debug("*******successfuly enlisted *********");

		} catch (Exception e) {
			logger.error("exception while enlisting in hazelcast transaction : " + e);
			throw new InvalidXATransactionException("exception while enlisting in hazelcast transaction ", e);
		}

		TransactionContext context1 = xaResource.getTransactionContext();
		return context1;
	}
}