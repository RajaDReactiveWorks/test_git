package com.attunedlabs.leap.eventsubscription.processor;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.jaxb.EventRoutingRule;
import com.attunedlabs.eventframework.jaxb.EventRoutingRules;
import com.attunedlabs.eventframework.jaxb.HttpPostRequest;
import com.attunedlabs.eventframework.jaxb.InvokeCamelRoute;
import com.attunedlabs.eventframework.jaxb.Pipeline;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.exception.MissingConfigurationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;

/**
 * <code>SubscriberActionIndentificationProcessor</code> will apply the
 * routing-rule specified by the particular subscriber and identifies which
 * action should be executed if the rule is passed.
 * 
 * @author Reactiveworks42
 *
 */
public class SubscriberActionIndentificationProcessor implements Processor {
	final static Logger log = LoggerFactory.getLogger(SubscriberActionIndentificationProcessor.class);

	private SubscriptionUtil subscriptionUtil;

	public SubscriberActionIndentificationProcessor(SubscriptionUtil subscriptionUtil) {
		this.subscriptionUtil = subscriptionUtil;
	}

	/**
	 * adds some extra headers in the exchange propagated which define where to
	 * route based on then rule.
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		log.debug("processor invocation for identifying action..");
		if (exchange.getIn() != null) {

			// if tenant and site not found
			LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
			Map<String, Object> genricdata = leapHeader.getGenricdata();
			if (genricdata.containsKey(SubscriptionConstant.DEFAULT_ASSIGNED_TENANT_KEY))
				throw new MissingConfigurationException(
						"TENANT/SITE DOESN'T EXISTS :Event Data will not be processed because \n "
								+ "tenantId and siteId not found in " + EventFrameworkConstants.EVENT_HEADER_KEY
								+ " of event data \n" + "(Used Default GLOBAL tenant for logging purpose)...");
			if (genricdata.containsKey(SubscriptionConstant.NO_CONFIG_FOR_TENANT_KEY))
				throw new MissingConfigurationException(
						"NO SUBSCRIBER CONFIG FOUND FOR TENANT :Event Data will not be processed because \n "
								+ "No configuration found particular tenant in memory(Loaded the configuration from "
								+ "GLOBAL tenant for logging purpose)...");

			// get the data from the exchange.
			JSONObject eventBody = subscriptionUtil.identifyContentType(exchange.getIn().getBody(String.class));

			log.debug("all exchange headers: " + exchange.getIn().getHeaders());
			log.debug("event data used for identification: " + eventBody);

			SubscribeEvent eventSubscription = exchange.getIn()
					.getHeader(SubscriptionConstant.SUBSCIBER_EVENT_CONFIG_KEY, SubscribeEvent.class);
			EventRoutingRules eventRoutingRules = eventSubscription.getEventRoutingRules();
			List<EventRoutingRule> totalEventRoutingRule = eventRoutingRules.getEventRoutingRule();

			// getting the subscription event routing rule on index for
			// particular Subscriber.
			EventRoutingRule eventRoutingRule = totalEventRoutingRule
					.get(exchange.getProperty(SubscriptionConstant.LOOP_INDEX_KEY, Integer.class));

			log.debug("evaluating routing rule present at index "
					+ exchange.getProperty(SubscriptionConstant.LOOP_INDEX_KEY) + " for subsciber "
					+ eventSubscription.getSubscriptionId());

			String mvelExpressionRule = eventRoutingRule.getRule();

			// if the rule is not specified than directly evaluate the routing
			// action part.
			boolean evaluateRuleOrNot = false;
			if (mvelExpressionRule == null || mvelExpressionRule.trim().isEmpty())
				evaluateRuleOrNot = true;
			else
				evaluateRuleOrNot = subscriptionUtil.evaluateMVELForCriteriaMatch(mvelExpressionRule, eventBody);

			// decide where to route
			if (evaluateRuleOrNot) {
				HttpPostRequest httpPostRequest = eventRoutingRule.getHttpPostRequest();
				InvokeCamelRoute invokeCamelRoute = eventRoutingRule.getInvokeCamelRoute();
				Pipeline pipeline = eventRoutingRule.getPipeline();

				if (httpPostRequest != null) {
					exchange.getIn().setHeader(SubscriptionConstant.ACTION_KEY,
							SubscriptionConstant.HTTP_POST_REQUEST_KEY);
					exchange.getIn().setHeader(SubscriptionConstant.HTTP_POST_REQUEST_KEY, httpPostRequest);
					log.debug("ACTION : " + SubscriptionConstant.HTTP_POST_REQUEST_KEY);
				} else if (invokeCamelRoute != null) {
					exchange.getIn().setHeader(SubscriptionConstant.ACTION_KEY,
							SubscriptionConstant.INVOKE_CAMEL_ROUTE_KEY);
					exchange.getIn().setHeader(SubscriptionConstant.INVOKE_CAMEL_ROUTE_KEY, invokeCamelRoute);
					log.debug("ACTION : " + SubscriptionConstant.INVOKE_CAMEL_ROUTE_KEY);
				} else if (pipeline != null) {
					exchange.getIn().setHeader(SubscriptionConstant.ACTION_KEY, SubscriptionConstant.PIPELINE_KEY);
					exchange.getIn().setHeader(SubscriptionConstant.PIPELINE_KEY, pipeline);
					log.debug("ACTION : " + SubscriptionConstant.PIPELINE_KEY);
				}

			} else {
				exchange.getIn().setHeader(SubscriptionConstant.ACTION_KEY, SubscriptionConstant.DEFAULT_ACTION);
			}

		}

	}
}
