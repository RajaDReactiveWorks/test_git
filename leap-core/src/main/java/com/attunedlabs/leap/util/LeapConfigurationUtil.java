package com.attunedlabs.leap.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.datacontext.jaxb.DataContext;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.datacontext.jaxb.RefDataContext;
import com.attunedlabs.datacontext.jaxb.RefDataContexts;
import com.attunedlabs.eventframework.config.EventRequestContext;
import com.attunedlabs.integrationfwk.config.IIntegrationPipeLineConfigurationService;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigException;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigUnit;
import com.attunedlabs.integrationfwk.config.impl.IntegrationPipelineConfigurationService;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.generic.LeapGenericConstant;
import com.attunedlabs.leap.header.initializer.JsonParserException;
import com.attunedlabs.permastore.config.IPermaStoreConfigurationService;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigurationService;
import com.attunedlabs.policy.config.IPolicyConfigurationService;
import com.attunedlabs.policy.config.PolicyConfigurationException;
import com.attunedlabs.policy.config.PolicyConfigurationUnit;
import com.attunedlabs.policy.config.PolicyEvaluationConfigurationUnit;
import com.attunedlabs.policy.config.PolicyRequestContext;
import com.attunedlabs.policy.config.impl.PolicyConfigurationService;
import com.google.common.reflect.TypeToken;
import org.apache.metamodel.factory.DataContextFactoryRegistryImpl;
import org.apache.metamodel.factory.DataContextPropertiesImpl;
import com.attunedlabs.config.util.PropertiesConfigException;

public class LeapConfigurationUtil {

	final static Logger logger = LoggerFactory.getLogger(LeapConfigurationUtil.class);
	private static final String APPS_DEPLOYMENT_ENV_CONFIG = "globalAppDeploymentConfig.properties";
	private static final String CASSANDRA_ENV_CONFIG = "cassandraDBConfig.properties";

	/**
	 * This method is used to get the perma store from the configName
	 * 
	 * @param configName
	 * @param exchange
	 * @throws PermaStoreConfigRequestException
	 * @throws JSONException
	 */
	public void getPermastoreConfiguration(String configName, Exchange exchange)
			throws PermaStoreConfigRequestException, JSONException {
		logger.debug(".getPermastoreConfigurationFromCamel() of LeapConfigurationUtil");
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		try {
			if (leapHeader != null || (configName != null && !(configName.isEmpty()))) {
				String tenant = leapHeader.getTenant();
				String site = leapHeader.getSite();
				String featureGroup = leapHeader.getFeatureGroup();
				String featureName = leapHeader.getFeatureName();
				String vendor = leapHeader.getVendor();
				String version = leapHeader.getVersion();
				String implName = leapHeader.getImplementationName();
				logger.debug("vendor : " + vendor + ", version : " + version);
				RequestContext requestContext = new RequestContext(tenant, site, featureGroup, featureName, implName,
						vendor, version);
				IPermaStoreConfigurationService permaConfigService = new PermaStoreConfigurationService();
				Object obj;

				obj = permaConfigService.getPermaStoreCachedObject(requestContext, configName);
				Map<String, Object> permaCacheObjectInMap = leapHeader.getPermadata();
				permaCacheObjectInMap.put(configName, obj);
			} else {
				throw new PermaStoreConfigRequestException(
						"unable to get the permastore configuration from the configName " + configName
								+ "for the service" + exchange.getIn().getHeader(LeapHeaderConstant.SERVICETYPE_KEY)
								+ ", feature : " + exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY)
								+ " and feature group : "
								+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY));
			}
		} catch (PermaStoreConfigRequestException e) {
			LeapConfigurationUtil.setResponseCode(503, exchange, e.getMessage());
			throw new PermaStoreConfigRequestException("unable to get the permastore configuration from the service "
					+ exchange.getIn().getHeader(LeapHeaderConstant.SERVICETYPE_KEY) + ", feature : "
					+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY) + " and feature group : "
					+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY));
		}
	}

	/**
	 * This method is used to get the policy configuration
	 * 
	 * @param configName
	 * @param exchange
	 * @throws PolicyConfigurationException
	 * @throws JSONException
	 */
	public void getPolicyConfiguration(String configName, Exchange exchange)
			throws PolicyConfigurationException, JSONException {
		logger.debug(".getPolicyConfigurationFromCamel() of LeapConfigurationUtil");
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		// create the policy request context
		try {
			if (leapHeader != null || (configName != null && !(configName.isEmpty()))) {
				String tenant = leapHeader.getTenant();
				String site = leapHeader.getSite();
				String featureGroup = leapHeader.getFeatureGroup();
				String featureName = leapHeader.getFeatureName();
				String vendor = leapHeader.getVendor();
				String version = leapHeader.getVersion();
				String implName = leapHeader.getImplementationName();
				logger.debug("vendor : " + vendor + ", version : " + version);
				Map<String, Object> policyReqVariableInMap = leapHeader.getPolicydata();

				PolicyRequestContext requestContext = new PolicyRequestContext(tenant, site, featureGroup, featureName,
						implName, vendor, version);
				IPolicyConfigurationService policyConfigService = new PolicyConfigurationService();
				PolicyConfigurationUnit policyConfigUnit = policyConfigService
						.getPolicyConfigurationUnit(requestContext, configName);
				List<PolicyEvaluationConfigurationUnit> policyEvalConfigUnitList = policyConfigUnit
						.getEvaluationUnitList();
				for (PolicyEvaluationConfigurationUnit policyEvalConfigUnit : policyEvalConfigUnitList) {
					List<String> reqVarList = policyEvalConfigUnit.getReqVarList();
					for (String reqVar : reqVarList) {
						Object reqValue = getRequestVariableValueFromExchange(exchange, reqVar);
						policyReqVariableInMap.put("$" + reqVar, reqValue);
					}

				} // end of for on list of PolicyEvaluationConfigurationUnit
			} // end of if checking the config name value exists or it is empty
			else {
				throw new PolicyConfigurationException("unable to get the permastore configuration from the configName "
						+ configName + "for the service"
						+ exchange.getIn().getHeader(LeapHeaderConstant.SERVICETYPE_KEY) + ", feature : "
						+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY) + " and feature group : "
						+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY));
			}
		} catch (PolicyConfigurationException e) {
			LeapConfigurationUtil.setResponseCode(503, exchange, e.getMessage());
			throw new PolicyConfigurationException("unable to get the permastore configuration from the service "
					+ exchange.getIn().getHeader(LeapHeaderConstant.SERVICETYPE_KEY) + ", feature : "
					+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY) + " and feature group : "
					+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY));
		}
	}

	/**
	 * This method is used to initialize EventRequestContext object with data store
	 * in leapHeader
	 * 
	 * @param exchange
	 *            : Exchange object to getleapHeader
	 */
	public EventRequestContext initializeEventRequestContext(Exchange exchange) {
		logger.debug(".initializeEventRequestContext method");
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		// initialize EventRequestContext with data in leapHeader
		EventRequestContext eventRequestContext = new EventRequestContext();
		eventRequestContext.setTenant(leapHeader.getTenant());
		eventRequestContext.setServicetype(leapHeader.getServicetype());
		eventRequestContext.setFeatureGroup(leapHeader.getFeatureGroup());
		eventRequestContext.setFeatureName(leapHeader.getFeatureName());
		eventRequestContext.setHazelcastTransactionalContext(leapHeader.getHazelcastTransactionalContext());
		eventRequestContext.setRequestUUID(leapHeader.getRequestUUID());
		return eventRequestContext;
	}

	/**
	 * This method is used to compare datacontext for this feature and other
	 * feature. If same then create Apache metamodel datacontext else create
	 * composite datacontext
	 * 
	 * @param requestContext
	 *            : Feature Request Context Object
	 * @param featureDataContext
	 *            : FeatureDataContext Object of current feature
	 * @param refFeatureDataContext
	 *            : FeatureDataContext Object of reference feature
	 * @return
	 */
	public boolean compareDataContext(RequestContext requestContext, DataContext featureDataContext,
			FeatureDataContext refFeatureDataContext) {
		logger.debug(".compareDataContext method of LeapConfigUtil");
		boolean flag = false;
		String dbBeanRefName = featureDataContext.getDbBeanRefName();
		String dbType = featureDataContext.getDbType();
		String dbHost = featureDataContext.getDbHost();
		String dbPort = featureDataContext.getDbPort();
		String dbSchema = featureDataContext.getDbSchema();
		List<RefDataContexts> refDataContextsList = refFeatureDataContext.getRefDataContexts();
		for (RefDataContexts refDataContexts : refDataContextsList) {
			String featureGroup = refDataContexts.getFeatureGroup();
			String featureName = refDataContexts.getFeatureName();
			if (featureGroup.equalsIgnoreCase(requestContext.getFeatureGroup())
					&& featureName.equalsIgnoreCase(requestContext.getFeatureName())) {
				List<RefDataContext> refDataContextList = refDataContexts.getRefDataContext();
				for (RefDataContext refDataContext : refDataContextList) {
					if (refDataContext.getDbBeanRefName().equalsIgnoreCase(dbBeanRefName)
							&& refDataContext.getDbType().equalsIgnoreCase(dbType)
							&& refDataContext.getDbHost().equalsIgnoreCase(dbHost)
							&& refDataContext.getDbPort().equalsIgnoreCase(dbPort)
							&& refDataContext.getDbSchema().equalsIgnoreCase(dbSchema)) {
						flag = true;
					} else {
						flag = false;
					}
				}
			} // end of if matching fetaureGroup and featureName
		} // end of for(RefDataContexts refDataContexts:refDataContextsList)

		return flag;
	}

	/**
	 * method to get IntegrationPipeContext
	 * 
	 * @param exchange
	 */
	public void getIntegrationPipeContext(Exchange exchange) {
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		Map<String, Object> body = leapHeader.getPipeContext();
		String exchangeBody = (String) body.get(LeapGenericConstant.PIPE_CONTEXT_KEY);
		exchange.getIn().setBody(exchangeBody);
	}// end of method

	/**
	 * method to set IntegrationPipeContext
	 * 
	 */
	public void setIntegrationPipeContext(String str, Exchange exchange) {
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		Map<String, Object> body = leapHeader.getPipeContext();
		String exchangeBody = str;
		body.put(LeapGenericConstant.PIPE_CONTEXT_KEY, exchangeBody);
	}

	/**
	 * Getting the oldleapHeader key's value from the existing leapHeader and
	 * setting it into the exchange's leap header
	 * 
	 * @param exchange
	 */
	public void getOldleapHeader(Exchange exchange) {
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		Map<String, Object> oldLeap = leapHeader.getOriginalLeapHeader();
		exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY,
				oldLeap.get(LeapHeaderConstant.ORIGINAL_LEAP_HEADER_KEY));
		// String exchangeBody = (String)
		// body.get(LeapGenericConstant.PIPE_CONTEXT_KEY);
	}

	/**
	 * Setting the Leap header Object inside leapHeader itself in a key.
	 * 
	 * @param exchange
	 */
	public void setOldleapHeader(Exchange exchange) {
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		logger.debug("setting the leap header in exchange : " + leapHeader.toString());
		logger.debug("list of  header in exchange : " + exchange.getIn().getHeaders());
		Map<String, Object> oldLeap = leapHeader.getOriginalLeapHeader();
		oldLeap.put(LeapHeaderConstant.ORIGINAL_LEAP_HEADER_KEY, leapHeader);
	}

	/**
	 * the newly added integrationPipeline configuration object, retrieved from the
	 * leap header
	 * 
	 * @param configName
	 * @param exchange
	 * @throws IntegrationPipelineConfigException
	 */
	public void getIntegrationPipelineConfiguration(String configName, RequestContext reqcontext, LeapHeader leapHeader,
			Exchange exchange) throws IntegrationPipelineConfigException {
		logger.debug(" (.)  getIntegrationPipelineConfiguration Method ");
		logger.debug(" Request Context ;  " + reqcontext);

		IIntegrationPipeLineConfigurationService pipeLineConfigurationService = new IntegrationPipelineConfigurationService();
		IntegrationPipelineConfigUnit pipelineConfigUnit = pipeLineConfigurationService
				.getIntegrationPipeConfiguration(reqcontext, configName);
		RequestContext request = new RequestContext(reqcontext.getTenantId(), reqcontext.getSiteId(),
				reqcontext.getFeatureGroup(), reqcontext.getFeatureName(), reqcontext.getImplementationName(),
				reqcontext.getVendor(), reqcontext.getVersion());
		if (pipelineConfigUnit == null) {
			// tO GET PIPElINE dETAILS FROM DEFAULT TENANT GROUP, take it from framework

			request.setTenantId(LeapGenericConstant.DEFAULT_TENANTID);
			request.setSiteId(LeapGenericConstant.DEFAULT_SITEID);
			pipelineConfigUnit = pipeLineConfigurationService.getIntegrationPipeConfiguration(request, configName);
			logger.debug("pipeLineConfiguration Unit After setting the tenant And Site as All : "
					+ pipelineConfigUnit.toString());
		}

		logger.debug(".inIPipelineLeapConfigUtil.." + pipelineConfigUnit + "ConfigurationName in leapConfigUtil.."
				+ configName);
		// ......TestSnipet....
		exchange.getIn().setHeader(PipelineServiceConstant.PIPE_ACTIVITY_KEY_HEADER_KEY, pipelineConfigUnit);
		// .......TestSniper-Ends......

		logger.debug(" Request Context ;  " + reqcontext);

		Map<String, Object> integrationCahedObject = leapHeader.getIntegrationpipelineData();
		if (integrationCahedObject == null)
			integrationCahedObject = new HashMap<String, Object>();
		integrationCahedObject.put(LeapHeaderConstant.PIPELINE_CONFIG_KEY, pipelineConfigUnit);
		logger.debug(".inLeapUtil cachedObject..Check before putting" + integrationCahedObject);
	}// ..end of the method

	private Object getRequestVariableValueFromExchange(Exchange exchange, String reqVar) {
		logger.debug(".getRequestVariableValueFromExchange() of LeapConfigurationUtil");
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);

		// checking in exchange header
		logger.debug("checking in exchange header using " + reqVar);
		Object reqvalue = (Object) exchange.getIn().getHeader(reqVar);
		if (reqvalue == null) {
			logger.debug("checking in exchange header using " + "$" + reqVar);
			reqvalue = (Object) exchange.getIn().getHeader("$" + reqVar);
		}

		// checking into leapHeader data value
		if (reqvalue == null) {
			logger.debug(reqVar + " not found in exchange so checking in leapHeader data ");
			Map<String, Object> geniricdata = leapHeader.getGenricdata();
			try {
				JSONArray jsonArray = (JSONArray) geniricdata.get(LeapHeaderConstant.DATA_KEY);
				int jsonLen = jsonArray.length();
				logger.debug("data's in json array for key data is : " + jsonLen);
				for (int i = 0; i < jsonLen; i++) {
					JSONObject jobj = (JSONObject) jsonArray.get(i);
					logger.debug("checking in  leapHeader using " + reqVar);
					reqvalue = (Object) jobj.get(reqVar);
					if (reqvalue == null) {
						logger.debug("checking in  leapHeader using " + "$" + reqVar);
						reqvalue = (Object) jobj.get("$" + reqVar);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		// checking in exchange.property
		if (reqvalue == null) {
			logger.debug(reqVar + " not found in exchange so checking in exchange property data ");
			reqvalue = (String) exchange.getProperty(reqVar);
			logger.debug("checking in  leapHeader using " + reqVar);
			if (reqvalue == null) {
				logger.debug("checking in  leapHeader using " + "$" + reqVar);
				reqvalue = (String) exchange.getProperty("$" + reqVar);
			}

		}

		return reqvalue;
	}// end of method

	/**
	 * To load the properties file
	 * 
	 * @return Properties Object
	 * @throws PropertiesConfigException
	 */
	public static Properties loadingPropertiesFile() throws PropertiesConfigException {
		logger.debug(".getLoadProperties method of LeapConfigurationUtil");
		Properties properties = new Properties();
		logger.debug("before InputStream inputStream");
		InputStream inputStream = LeapConfigurationUtil.class.getClassLoader()
				.getResourceAsStream(APPS_DEPLOYMENT_ENV_CONFIG);
		logger.debug("after InputStream inputStream : " + inputStream);
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			throw new PropertiesConfigException("unable to load property file = " + APPS_DEPLOYMENT_ENV_CONFIG);
		}

		return properties;

	}// end of method loadingPropertiesFile

	/**
	 * This method is used to set the response code for the exchange
	 * 
	 * @param key
	 *            :key String
	 * @param exchange
	 *            :exchange Object
	 * @param responseMessage
	 *            :responseMessage String
	 * @throws JsonParserException
	 * @throws JSONException
	 */

	public static void setResponseCode(int key, Exchange exchange, String responseMessage) throws JSONException {
		logger.debug(" (.) setResponseCode() of LeapConfigurationUtil");
		exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, key);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("http response code ", exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE));
		jsonObj.put("response message ", responseMessage);
		exchange.getIn().setHeader("Access-Control-Allow-Origin", "*");
		exchange.getIn().setHeader("Access-Control-Allow-Methods", "POST");
		exchange.getIn().setHeader("Access-Control-Allow-Headers", "*");
		exchange.getIn().setBody(jsonObj);
	}// end of method setResponseCode

	public org.apache.metamodel.DataContext getDataContext(Exchange exchange, String operation) {

		HashMap<String, TypeToken<?>> tyHashMap = new HashMap<>();
		tyHashMap.put("eventkeys", new TypeToken<List<String>>() {
			private static final long serialVersionUID = 1353193293707810284L;
		});
		Properties prop = null;
		try {
			prop = loadingCassandraPropertiesFile();
		} catch (PropertiesConfigException e) {
			e.printStackTrace();
		}
		DataContextPropertiesImpl properties = new DataContextPropertiesImpl();
		// main foctor for deciding implementation
		properties.setDataContextType(prop.getProperty("datacontexttype"));
		properties.put(DataContextPropertiesImpl.PROPERTY_HOSTNAME, prop.getProperty("host"));
		properties.put(DataContextPropertiesImpl.PROPERTY_PORT, prop.getProperty("port"));
		properties.put(DataContextPropertiesImpl.PROPERTY_URL, prop.getProperty("url"));
		properties.put(DataContextPropertiesImpl.PROPERTY_DRIVER_CLASS, prop.getProperty("driver_class"));
		properties.put(DataContextPropertiesImpl.PROPERTY_DATABASE, prop.getProperty("keyspace"));
		properties.put("type-token", tyHashMap);
		org.apache.metamodel.DataContext dataContext = DataContextFactoryRegistryImpl.getDefaultInstance()
				.createDataContext(properties);
		return dataContext;
	}

	/**
	 * To load the properties file
	 * 
	 * @return Properties Object
	 * @throws PropertiesConfigException
	 */
	public static Properties loadingCassandraPropertiesFile() throws PropertiesConfigException {
		logger.debug(".getLoadProperties method of LeapConfigurationUtil");
		Properties properties = new Properties();
		logger.debug("before InputStream inputStream");
		InputStream inputStream = LeapConfigurationUtil.class.getClassLoader()
				.getResourceAsStream(CASSANDRA_ENV_CONFIG);
		logger.debug("after InputStream inputStream : " + inputStream);
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			throw new PropertiesConfigException("unable to load property file = " + CASSANDRA_ENV_CONFIG);
		}

		return properties;

	}// end of method loadingPropertiesFile
}
