package com.attunedlabs.leap.feature.routing;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.feature.config.FeatureConfigRequestContext;
import com.attunedlabs.feature.config.FeatureConfigRequestException;
import com.attunedlabs.feature.config.FeatureConfigurationException;
import com.attunedlabs.feature.config.FeatureConfigurationUnit;
import com.attunedlabs.feature.config.impl.FeatureConfigurationService;
import com.attunedlabs.feature.jaxb.Feature;
import com.attunedlabs.feature.jaxb.Service;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.feature.FeatureErrorPropertyConstant;
import com.attunedlabs.leap.generic.UnableToLoadPropertiesException;
import com.attunedlabs.leap.util.LeapConfigurationUtil;

public class ExecutionFeatureDynamic {
	private static final Logger logger = LoggerFactory.getLogger(ExecutionFeatureDynamic.class);

	private static Properties errorcodeprop = null;
	private static Properties errormessageprop = null;

	static {
		logger.debug("static block");
		try {
			errorcodeprop = loadingPropertiesFile(FeatureErrorPropertyConstant.ERROR_CODE_FILE);
			errormessageprop = loadingPropertiesFile(FeatureErrorPropertyConstant.ERROR_MESSAGE_FILE);
		} catch (UnableToLoadPropertiesException e) {
			logger.error("Uable to read error code and error message property file");
		}
	}

	/**
	 * This method is for dynamically routing to Implementation route
	 * 
	 * @param exchange
	 *            : Exchange Object
	 * @return : String
	 * 
	 * @throws UnableToLoadPropertiesException
	 * @throws DynamicallyImplRoutingFailedException
	 * @throws JSONException
	 *
	 */
	public void route(Exchange exchange)
			throws UnableToLoadPropertiesException, DynamicallyImplRoutingFailedException, JSONException {
		logger.debug("=========================================================================================");
		logger.debug("ExecutionFeatureDynamic-route[start]: " + System.currentTimeMillis());
		logger.debug("inside route() of ExecutionFeatureDynamic");
		String implName = null;
		String vendor = null;
		String version = null;
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		FeatureConfigRequestContext featureRequestContext = null;
		String servicetype = leapHeader.getServicetype();
		String tenant = leapHeader.getTenant();
		String siteId = leapHeader.getSite();
		String featureGroup = leapHeader.getFeatureGroup();
		String featureName = leapHeader.getFeatureName();
		String provider = null;
		if (exchange.getIn().getHeaders().containsKey("provider")) {
			provider = (String) exchange.getIn().getHeader("provider");
			logger.debug("provider in if : " + provider);
		} 
		logger.debug("tenant : " + tenant + ", featuregroup key  : " + featureGroup + ", feature : " + featureName
				+ ",site id : " + siteId + ", service name : " + servicetype);
		IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();
		try {
			FeatureDeployment featureDeployment;
			if (provider != null) {
				logger.debug("provider is not null");
				featureDeployment = featureDeploymentservice.getActiveAndPrimaryFeatureDeployedFromCache(tenant, siteId,
						featureName, provider);
			} else {
				logger.debug("provider is Null");
				featureDeployment = featureDeploymentservice.getActiveAndPrimaryFeatureDeployedFromCache(tenant, siteId,
						featureName, leapHeader);
			}
			logger.debug("featureDeployment : "+featureDeployment);
			if (featureDeployment != null) {
				// providing implementation, vendor and version support
				implName = featureDeployment.getImplementationName();
				vendor = featureDeployment.getVendorName();
				version = featureDeployment.getFeatureVersion();
				leapHeader.setImplementationName(implName);
				leapHeader.setVendor(vendor);
				leapHeader.setVersion(version);
				logger.debug("Impl Name :" + implName + "vendor : " + vendor
						+ ", version : " + version+ ", provider : " + provider);
				RequestContext reqcontext;
				if (provider != null) {
					reqcontext = new RequestContext(tenant, siteId, featureGroup, featureName, implName, vendor,
							version, provider);
					featureRequestContext = new FeatureConfigRequestContext(tenant, siteId, featureGroup, featureName,
							implName, vendor, version, provider);
				} else {
					reqcontext = new RequestContext(tenant, siteId, featureGroup, featureName, implName, vendor,
							version);
					featureRequestContext = new FeatureConfigRequestContext(tenant, siteId, featureGroup, featureName,
							implName, vendor, version);
				}
				leapHeader.setRequestContext(reqcontext);
				// create a feature RequestCOntext
				featureRequestContext = new FeatureConfigRequestContext(tenant, siteId, featureGroup, featureName,
						implName, vendor, version);
			} else {
				throw new DynamicallyImplRoutingFailedException(
						"unable to get the active and primary feature deployement : " + featureDeployment);
			}
			// creating a featureConfigurationUnit
			FeatureConfigurationUnit featureConfigurationUnit;
			try {
				featureConfigurationUnit = getFeatureConfigurationUnit(featureRequestContext, featureName);
				logger.debug("feature config unit : " + featureConfigurationUnit);
				if (featureConfigurationUnit != null) {
					String routeToRedirect = routeToRedirect(featureConfigurationUnit, leapHeader, errorcodeprop,
							errormessageprop, exchange);
					if (routeToRedirect != null && !(routeToRedirect.isEmpty())) {
						exchange.getIn().setHeader("implroute", routeToRedirect.trim());
					} else {
						throw new DynamicallyImplRoutingFailedException(
								"No implementation route name is configured for the service in feature  : "
										+ featureRequestContext);
					}
				} else {
					// if you dont find feature tenant specific search on global
					// level
					logger.debug("searching for feature service on global level");
					featureRequestContext = new FeatureConfigRequestContext(LeapHeaderConstant.tenant,
							LeapHeaderConstant.site, featureGroup, featureName, implName, vendor, version);
					featureConfigurationUnit = getFeatureConfigurationUnit(featureRequestContext, featureName);
					if (featureConfigurationUnit != null) {
						String routeToRedirect = routeToRedirect(featureConfigurationUnit, leapHeader, errorcodeprop,
								errormessageprop, exchange);
						if (routeToRedirect != null && !(routeToRedirect.isEmpty())) {
							exchange.getIn().setHeader("implroute", routeToRedirect.trim());
						} else {
							throw new DynamicallyImplRoutingFailedException(
									"No implementation route name is configured for the service in feature  : "
											+ featureRequestContext);
						}
					} else {
						throw new DynamicallyImplRoutingFailedException(
								"No feature is configured with request context : " + featureRequestContext);
					}
				}
			} catch (InvalidNodeTreeException | FeatureConfigRequestException | FeatureConfigurationException e) {
				LeapConfigurationUtil.setResponseCode(404, exchange,
						"Unable to load the configuraion feature with request conetxt : " + featureRequestContext + ":"
								+ e.getMessage());
				throw new DynamicallyImplRoutingFailedException(
						"Unable load the configuraion feature with request conetxt : " + featureRequestContext);
			}
		} catch (FeatureDeploymentServiceException | JSONException e) {
			LeapConfigurationUtil.setResponseCode(404, exchange, e.getMessage());
			throw new DynamicallyImplRoutingFailedException(
					"Unable to route to Implementation route because no feature with request conetxt : "
							+ featureRequestContext + " is marked as primary");
		}
		logger.debug("ExecutionFeatureDynamic-route[stop]: " + System.currentTimeMillis());
		logger.debug("=========================================================================================");
	}

	/**
	 * This method is to load the properties file from the classpath
	 * 
	 * @param filetoload
	 *            : name of the file to be loaded
	 * @return Properties Object
	 * @throws UnableToLoadPropertiesException
	 */
	private static Properties loadingPropertiesFile(String filetoload) throws UnableToLoadPropertiesException {
		logger.debug("inside loadingPropertiesFile method of ExecutionFeatureDynamic Bean");
		Properties prop = new Properties();
		InputStream input1 = ExecutionFeatureDynamic.class.getClassLoader().getResourceAsStream(filetoload);
		try {
			prop.load(input1);
		} catch (IOException e) {
			throw new UnableToLoadPropertiesException(
					"unable to load property file = " + FeatureErrorPropertyConstant.ERROR_CODE_FILE, e);
		}
		return prop;
	}

	/**
	 * This method is used to get the feature configuration unit
	 * 
	 * @param featureRequestContext
	 *            : Request context object contain tenant,site,featuregroup and
	 *            feature name
	 * @param configname
	 *            : configuration name
	 * @return FeatureConfigurationUnit Object
	 * @throws InvalidNodeTreeException
	 * @throws FeatureConfigRequestException
	 * @throws FeatureConfigurationException
	 */
	private FeatureConfigurationUnit getFeatureConfigurationUnit(FeatureConfigRequestContext featureRequestContext,
			String configname)
			throws InvalidNodeTreeException, FeatureConfigRequestException, FeatureConfigurationException {
		logger.debug("inside getFeatureConfigurationUnit method of ExecutionFeatureDynamic Bean");
		FeatureConfigurationService featureConfigurationService = new FeatureConfigurationService();
		FeatureConfigurationUnit featureConfigurationUnit = featureConfigurationService
				.getFeatureConfiguration(featureRequestContext, configname);
		logger.debug("feature configuration unit in ExecutionFeature Dynamic  bean : " + featureConfigurationUnit);

		return featureConfigurationUnit;
	}

	/**
	 * This method is to find the route for the implementation route
	 * 
	 * @param featureConfigurationUnit
	 *            : FeatureCOnfigurationUnit Object
	 * @param leapHeader
	 *            : leapHeader Object
	 * @param errorcodeprop
	 *            : Properties file for error code
	 * @param errormessageprop
	 *            : Properties file for error message
	 * @param exchange
	 *            : exchange to set body with error code and error message
	 * @return String route to Implementation
	 * @throws DynamicallyImplRoutingFailedException
	 * @throws JSONException
	 */
	private String routeToRedirect(FeatureConfigurationUnit featureConfigurationUnit, LeapHeader leapHeader,
			Properties errorcodeprop, Properties errormessageprop, Exchange exchange)
			throws DynamicallyImplRoutingFailedException, JSONException {
		logger.debug("inside routeToRedirect method of ExecutionFeatureDynamic Bean");
		String implRouteEndpoint = null;
		boolean status = false;
		if (featureConfigurationUnit.getIsEnabled()) {
			status = true;
		}
		if (status) {
			Feature feature = (Feature) featureConfigurationUnit.getConfigData();
			List<Service> serviceList = feature.getService();
			logger.debug("no of feature service in list : " + serviceList.size());
			for (Service service : serviceList) {
				if (service.getName().equalsIgnoreCase(leapHeader.getServicetype().trim())) {
					logger.debug("service name from cache : " + service.getName()
							+ ", and service store in leap header : " + leapHeader.getServicetype().trim());
					if (service.isEnabled() == true) {
						implRouteEndpoint = setImplEndPointForService(service, leapHeader, exchange, errorcodeprop,
								errormessageprop);
						if (implRouteEndpoint != null && !(implRouteEndpoint.isEmpty())) {
							return implRouteEndpoint.trim();
						} else {
							throw new DynamicallyImplRoutingFailedException(
									"No implementation route name is configured service : " + service.getName());
						}
					} else {
						logger.debug("Not-supported : " + FeatureErrorPropertyConstant.UNAVAILABLE_KEY);
						String errorcode = errorcodeprop.getProperty(FeatureErrorPropertyConstant.UNAVAILABLE_KEY);
						logger.debug("errorcode : " + errorcode);
						String errormsg = errormessageprop.getProperty(errorcode);
						logger.debug("error : " + errormsg);
						LeapConfigurationUtil.setResponseCode(503, exchange,
								" for feature " + leapHeader.getFeatureName());
						exchange.getIn().setBody("status code : " + errorcode + " for feature "
								+ leapHeader.getFeatureName() + " \nReason : " + errormsg);
						implRouteEndpoint = "featureServiceNotExist";
					}
				} // end of if(servicename == leap core service name)

			} // end of for (Service service : serviceList)
		}
		return implRouteEndpoint;

	}// end of method

	/**
	 * This method is used to set the Implementation route from service
	 * 
	 * @param service
	 *            : Service Object specify the request for the type of service
	 * @param leapHeader
	 *            : LeapHeader Object
	 * @param exchange
	 *            : Camel Exchange
	 * @param errorcodeprop
	 *            : Get the error code
	 * @param errormessageprop
	 *            : get the error message for the error code
	 * @return endpoint in String
	 * @throws JSONException
	 */
	private String setImplEndPointForService(Service service, LeapHeader leapHeader, Exchange exchange,
			Properties errorcodeprop, Properties errormessageprop) throws JSONException {
		logger.debug("inside setImplEndPointForService method of ExecutionFeatureDynamic Bean");
		// logger.error("before setting service endpoint
		// type"+System.currentTimeMillis());
		String implRouteEndpoint = null;
		String endpointtype = leapHeader.getEndpointType();
		switch (endpointtype) {
		case LeapHeaderConstant.HTTP_JSON_KEY:
			logger.debug("Endpoint type is HTTP JSON type");
			implRouteEndpoint = service.getGenericRestEndpoint().getValue();
			break;
		case LeapHeaderConstant.HTTP_XML_KEY:
			logger.debug("Endpoint type is HTTP XML type");
			implRouteEndpoint = service.getGenericRestEndpoint().getValue();
			break;
		case LeapHeaderConstant.CXF_ENDPOINT_KEY:
			logger.debug("Endpoint type is cxf type");
			implRouteEndpoint = service.getConcreteSoapEndpoint().getValue();
			break;
		default: {
			logger.debug("Not-supported : " + FeatureErrorPropertyConstant.NOT_SUPPORTED_KEY);
			String errorcode = errorcodeprop.getProperty(FeatureErrorPropertyConstant.NOT_SUPPORTED_KEY);
			logger.debug("errorcode : " + errorcode);
			String errormsg = errormessageprop.getProperty(errorcode);
			logger.debug("error : " + errormsg);
			LeapConfigurationUtil.setResponseCode(503, exchange,
					" for feature " + leapHeader.getFeatureName() + "with endpoint type : " + endpointtype);
			exchange.getIn().setBody("status code : " + errorcode + " for feature " + leapHeader.getFeatureName()
					+ "with endpoint type : " + endpointtype + " \nReason : " + errormsg);
			implRouteEndpoint = "direct:noFeature";
		}
			break;
		}// end of switch
			// logger.error("after setting service endpoint
			// type"+System.currentTimeMillis());

		return implRouteEndpoint.trim();
	}// end of method

}
