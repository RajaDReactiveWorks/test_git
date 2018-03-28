package com.attunedlabs.leap.eventsubscription.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.jaxb.InvokeCamelRoute;
import com.attunedlabs.eventsubscription.exception.NonRetryableException;
import com.attunedlabs.eventsubscription.exception.RouteInvocationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;

/**
 * <code>SubscriberRoutingRuleEvaluationProcessor</code> will apply the
 * routing-rule specified by the particular subscriber and forms the new
 * exchange which is forwarded to the routeEndpoint configured by the subscriber
 * if the rule evaluates successfully else drops the message for the following
 * subscriber.
 * 
 * @author Reactiveworks42
 *
 */
public class InvokeCamelRouteProcessor implements Processor {
	final static Logger log = LoggerFactory.getLogger(InvokeCamelRouteProcessor.class);

	private SubscriptionUtil subscriptionUtil;

	public InvokeCamelRouteProcessor(SubscriptionUtil subscriptionUtil) {
		this.subscriptionUtil = subscriptionUtil;
	}

	/**
	 * adds some extra headers based on the result of rule evaluation on the
	 * event message present the exchange and based on the attributes configured
	 * by Subscriber to invoke feature specific camel endpoint.
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		log.debug("processor invocation for evaluating invokeCamelRoute" + exchange.getProperties());
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		String subscriptionId = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, String.class);

		String tenant = leapHeader.getTenant();
		String site = leapHeader.getSite();

		List<String> configParams = Arrays
				.asList(subscriptionId.split(EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER));

		if (configParams.size() - 1 == 5) {
			String featureGroup = configParams.get(0);
			String featureName = configParams.get(1);
			String implementation = configParams.get(2);
			String vendor = configParams.get(3);
			String version = configParams.get(4);

			if (tenant == null || site == null || tenant.trim().isEmpty() || site.trim().isEmpty())
				throw new RouteInvocationException(
						"TENANT/SITE DOESN'T EXISTS :tenantId and siteId not found in eventHeaders of "
								+ "route invokation failed to load...");

			leapHeader.setFeatureGroup(featureGroup);
			leapHeader.setFeatureName(featureName);
			leapHeader.setImplementationName(implementation);
			leapHeader.setVendor(vendor);
			leapHeader.setVersion(version);
			leapHeader.setEndpointType("HTTP-JSON");
		}

		try {
			if (exchange.getIn() != null) {

				// get the data from the exchange.
				JSONObject eventBody = subscriptionUtil.identifyContentType(exchange.getIn().getBody(String.class));
				String topicName = exchange.getIn().getHeader(KafkaConstants.TOPIC, String.class);

				InvokeCamelRoute invokeCamelRoute = exchange.getIn()
						.getHeader(SubscriptionConstant.INVOKE_CAMEL_ROUTE_KEY, InvokeCamelRoute.class);

				// holds the feature-info about the
				// particular route.
				Map<String, HashMap<String, String>> routeInfoMap = new HashMap<>();
				routeInfoMap = subscriptionUtil.addExtraHeadersToEndpoint(routeInfoMap, invokeCamelRoute);

				if (invokeCamelRoute != null) {
					String routeEndpoint = invokeCamelRoute.getValue().trim();
					log.debug("camel endpoint need to verify: " + routeEndpoint);
					HashMap<String, String> invokeCamelRouteMap = routeInfoMap
							.get(SubscriptionConstant.INVOKE_ENDPOINT_KEY);

					boolean containsFeatureCallAttr = subscriptionUtil.mapCheck(invokeCamelRouteMap);
					Endpoint endpoint = null;

					if (routeEndpoint != null && !routeEndpoint.isEmpty())
						endpoint = exchange.getContext().hasEndpoint(routeEndpoint);

					log.debug("camel endpoint verified to call : " + endpoint);

					Message outMessage = exchange.getOut();

					// default attribute adding on every exchange for
					// the subscriber to identify topic.
					outMessage.setHeader(KafkaConstants.TOPIC, topicName);
					outMessage.setHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, subscriptionId);
					outMessage.setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);

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
					outMessage.setHeader(LeapHeaderConstant.TIMEZONE, timeZoneId);

					outMessage.setBody(eventBody);

					// if the routeInvocation
					// contains feature-attributes than add that in header
					// else.
					if (containsFeatureCallAttr) {
						outMessage.setHeader(LeapHeaderConstant.SERVICETYPE_KEY,
								invokeCamelRouteMap.get(LeapHeaderConstant.SERVICETYPE_KEY));
						outMessage.setHeader(LeapHeaderConstant.FEATURE_GROUP_KEY,
								invokeCamelRouteMap.get(LeapHeaderConstant.FEATURE_GROUP_KEY));
						outMessage.setHeader(LeapHeaderConstant.FEATURE_KEY,
								invokeCamelRouteMap.get(LeapHeaderConstant.FEATURE_KEY));
						// if entry route is not mentioned than add as
						// default.
						if (endpoint != null)
							outMessage.setHeader(SubscriptionConstant.ROUTE_ENDPOINT_KEY, routeEndpoint);
						else
							outMessage.setHeader(SubscriptionConstant.ROUTE_ENDPOINT_KEY,
									SubscriptionConstant.ENTRY_ROUTE_FOR_SUBSCIBER);

					} else {
						if (endpoint != null)
							outMessage.setHeader(SubscriptionConstant.ROUTE_ENDPOINT_KEY, routeEndpoint);
						else
							throw new RouteInvocationException(
									"NO CONSUMER-ENDPOINT AVAILABLE:- the route endpoint mentioned to invoke doesn't exist ==> "
											+ routeEndpoint);
					}
				} else
					throw new RouteInvocationException(
							"NO INVOKE_CAMEL_ROUTE ACTION FOUND :- No route to invoke the either specify serviceAttributes or mention endpointConsumer correctly");
				log.info("CamelExchange before call: Headers => " + exchange.getOut().getHeaders());
				log.info("CamelExchange before call: BODY => " + exchange.getOut().getBody());
			}
		} catch (Exception e) {
			if (exchange.hasOut())
				exchange.getOut().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
			else
				exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
			throw new NonRetryableException("NON-RETRYABLE[" + e.getMessage() + "]", e);

		}
	}
}
