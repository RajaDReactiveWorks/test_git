package com.attunedlabs.leap.eventsubscription.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.persistence.ITenantConfigTreeService;
import com.attunedlabs.config.persistence.UndefinedPrimaryVendorForFeature;
import com.attunedlabs.config.persistence.impl.TenantConfigTreeServiceImpl;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.jaxb.Pipeline;
import com.attunedlabs.eventsubscription.exception.NonRetryableException;
import com.attunedlabs.eventsubscription.exception.PipelineInvokationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.integrationfwk.config.IIntegrationPipeLineConfigurationService;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigException;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigUnit;
import com.attunedlabs.integrationfwk.config.impl.IntegrationPipelineConfigurationService;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.integrationpipeline.InitializingPipelineException;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;

public class PipelineProcessor implements Processor {
	final static Logger log = LoggerFactory.getLogger(PipelineProcessor.class);

	private SubscriptionUtil subscriptionUtil;

	public PipelineProcessor(SubscriptionUtil subscriptionUtil) {
		this.subscriptionUtil = subscriptionUtil;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		log.debug("processor invocation for evaluating pipeline invocation");
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		try {
			if (exchange.getIn() != null) {

				// get the data from the exchange.
				JSONObject eventBody = subscriptionUtil.identifyContentType(exchange.getIn().getBody(String.class));
				String topicName = exchange.getIn().getHeader(KafkaConstants.TOPIC, String.class);
				String subscriptionId = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY,
						String.class);

				Pipeline pipeline = exchange.getIn().getHeader(SubscriptionConstant.PIPELINE_KEY, Pipeline.class);

				List<String> configParams = Arrays
						.asList(subscriptionId.split(EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER));

				if (configParams.size() - 1 == 5) {
					if (pipeline != null && pipeline.getIntegrationPipeName() != null) {
						String pipeLineRoute = SubscriptionConstant.ENTRY_ROUTE_FOR_PIPLINE;
						String inetegerationPipeline = pipeline.getIntegrationPipeName().get(0);

						String featureGroup = configParams.get(0);
						String featureName = configParams.get(1);
						String implementation = configParams.get(2);
						String vendor = configParams.get(3);
						String version = configParams.get(4);
						log.debug("camel endpoint need to verify: " + pipeLineRoute);
						Endpoint endpoint = null;

						if (pipeLineRoute != null && !pipeLineRoute.isEmpty() && inetegerationPipeline != null)
							endpoint = exchange.getContext().hasEndpoint(pipeLineRoute);

						log.debug("camel endpoint verified to call : " + endpoint);

						// default attribute adding on every exchange for
						// the subscriber to identify topic.
						exchange.getOut().setHeader(KafkaConstants.TOPIC, topicName);
						exchange.getOut().setHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, subscriptionId);
						exchange.getOut().setHeader(LeapHeaderConstant.FEATURE_GROUP_KEY, configParams.get(0));
						exchange.getOut().setHeader(LeapHeaderConstant.FEATURE_KEY, configParams.get(1));

						String tenant = leapHeader.getTenant();
						String site = leapHeader.getSite();

						if (tenant == null || site == null || tenant.trim().isEmpty() || site.trim().isEmpty())
							throw new PipelineInvokationException(
									"TENANT/SITE DOESN'T EXISTS :tenantId and siteId not found in eventHeaders of "
											+ "event data pipeline configuration failed to load...");

						leapHeader.setFeatureGroup(featureGroup);
						leapHeader.setFeatureName(featureName);
						leapHeader.setImplementationName(implementation);
						leapHeader.setVendor(vendor);
						leapHeader.setVersion(version);
						leapHeader.setEndpointType("HTTP-JSON");

						// set time-zone for tenant
						IAccountRegistryService accountRegistryService = new AccountRegistryServiceImpl();
						String accountId = null;
						String timeZoneId = TimeZone.getDefault().getID();
						try {
							accountId = accountRegistryService.getAccountIdByTenant(tenant);
							timeZoneId = accountRegistryService.getTimeZoneBySite(accountId, site);
							if (timeZoneId == null || timeZoneId.isEmpty()) {
								log.debug("timezone not found for tenant and site" + tenant + " : " + site);
								timeZoneId = TimeZone.getDefault().getID();
							}
						} catch (Exception e) {
							log.error("timezone not found for tenant and site" + tenant + " : " + site + " due to  : "
									+ e.getMessage());
							e.printStackTrace();
						}
						exchange.getOut().setHeader(LeapHeaderConstant.TIMEZONE, timeZoneId);

						exchange.getOut().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
						exchange.getOut().setBody(eventBody.toString());

						// load pipe config
						loadPipeConfiguration(tenant.trim(), site.trim(), featureGroup.trim(), featureName.trim(),
								implementation.trim(), vendor.trim(), version.trim(), inetegerationPipeline.trim(),
								exchange);

						if (endpoint != null)
							exchange.getOut().setHeader(SubscriptionConstant.ROUTE_ENDPOINT_KEY, pipeLineRoute);
						else
							throw new PipelineInvokationException(
									"NO PIPELINE CONSUMER-ENDPOINT AVAILABLE:- the route endpoint mentioned  "
											+ pipeLineRoute + "   doesn't exist ==> " + pipeLineRoute);
					} else
						throw new PipelineInvokationException(
								"NO PIPELINE_INVOCATION  ACTION FOUND :- No pipeline to invoke either specify integerationPipeline or mention endpointConsumer correctly");
					log.info("CamelExchange before pipeline-call: Headers => " + exchange.getOut().getHeaders());
					log.info("CamelExchange before pipeline-call: BODY => " + exchange.getOut().getBody());
				}
			} else
				throw new PipelineInvokationException(
						"REQUEST CONTEXT BUILD FAILED :- Pipeline invokation cannot be done request context to fetch configuration failed to build");
		} catch (

		Exception e) {
			e.printStackTrace();
			if (exchange.hasOut())
				exchange.getOut().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
			else
				exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
			throw new NonRetryableException("NON-RETRYABLE[" + e.getMessage() + "]", e);

		}
	}

	/**
	 * to initialize a few things, like LeapHeader params, before propagation
	 * 
	 * @param exchange
	 * @throws InitializingPipelineException
	 */
	public void loadPipeConfiguration(String tenant, String site, String featureGroup, String featureName, String impl,
			String vendor, String version, String piplineConfigName, Exchange exchange)
			throws InitializingPipelineException {
		ITenantConfigTreeService tenantTreeService = TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
		ConfigurationTreeNode vendorTreeNode;
		LeapHeader leapHeader = exchange.getOut().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY, LeapHeader.class);

		RequestContext reqcontext;
		try {
			vendorTreeNode = tenantTreeService.getPrimaryVendorForFeature(leapHeader.getTenant(), leapHeader.getSite(),
					leapHeader.getFeatureGroup(), leapHeader.getFeatureName());
			version = vendorTreeNode.getVersion();
			leapHeader.setVendor(vendor);
			leapHeader.setVersion(version);
			reqcontext = new RequestContext(leapHeader.getTenant(), leapHeader.getSite(), leapHeader.getFeatureGroup(),
					leapHeader.getFeatureName(), leapHeader.getImplementationName(), vendor, version);
			leapHeader.setRequestContext(reqcontext);
		} catch (UndefinedPrimaryVendorForFeature e1) {
			throw new InitializingPipelineException(
					"Unable to load the VendorTree for initializing PipeConfiguration..", e1);
		}
		log.debug("To load we are calling: " + piplineConfigName + " " + leapHeader + " " + exchange.getExchangeId());
		try {
			if (piplineConfigName != null && !(piplineConfigName.isEmpty())) {
				getIntegrationPipelineConfiguration(piplineConfigName, reqcontext, leapHeader, exchange);
			} else {
				throw new InitializingPipelineException("pipeline configuration name is null");
			}
		} catch (IntegrationPipelineConfigException e) {
			throw new InitializingPipelineException("Unable to load the IntegrationPipeline from cache to leapHeader..",
					e);
		}
	}// ..end of the method

	/**
	 * the newly added integrationPipeline configuration object, retrieved from
	 * the leap header
	 * 
	 * @param configName
	 * @param exchange
	 * @throws IntegrationPipelineConfigException
	 */
	public void getIntegrationPipelineConfiguration(String configName, RequestContext reqcontext, LeapHeader leapHeader,
			Exchange exchange) throws IntegrationPipelineConfigException {
		log.debug(" (.)  getIntegrationPipelineConfiguration Method ");
		log.debug(" Request Context ;  " + reqcontext);

		IIntegrationPipeLineConfigurationService pipeLineConfigurationService = new IntegrationPipelineConfigurationService();
		IntegrationPipelineConfigUnit pipelineConfigUnit = pipeLineConfigurationService
				.getIntegrationPipeConfiguration(reqcontext, configName);
		RequestContext request = new RequestContext(reqcontext.getTenantId(), reqcontext.getSiteId(),
				reqcontext.getFeatureGroup(), reqcontext.getFeatureName(), reqcontext.getImplementationName(),
				reqcontext.getVendor(), reqcontext.getVersion());
		if (pipelineConfigUnit == null) {
			// tO GET PIPElINE dETAILS FROM DEFAULT TENANT GROUP, take it from
			// framework

			request.setTenantId(LeapHeaderConstant.tenant);
			request.setSiteId(LeapHeaderConstant.site);
			pipelineConfigUnit = pipeLineConfigurationService.getIntegrationPipeConfiguration(request, configName);
			log.debug("pipeLineConfiguration Unit After setting the tenant And Site as All : "
					+ pipelineConfigUnit.toString());
		}

		log.debug(".inIPipelineLeapConfigUtil.." + pipelineConfigUnit + "ConfigurationName in leapConfigUtil.."
				+ configName);
		// ......TestSnipet....
		exchange.getOut().setHeader("pipeActivityKey", pipelineConfigUnit);
		// .......TestSniper-Ends......

		log.debug(" Request Context ;  " + reqcontext);

		Map<String, Object> integrationCahedObject = leapHeader.getIntegrationpipelineData();
		if (integrationCahedObject == null)
			integrationCahedObject = new HashMap<String, Object>();
		integrationCahedObject.put(LeapHeaderConstant.PIPELINE_CONFIG_KEY, pipelineConfigUnit);
		log.debug(".inLeapUtil cachedObject..Check before putting" + integrationCahedObject);
	}// ..end of the method

}
