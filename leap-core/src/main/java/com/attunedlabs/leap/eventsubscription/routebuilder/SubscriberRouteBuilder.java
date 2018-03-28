
package com.attunedlabs.leap.eventsubscription.routebuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.IEventSubscriptionTrackerService;
import com.attunedlabs.eventsubscriptiontracker.impl.EventSubscriptionTrackerImpl;
import com.attunedlabs.leap.eventsubscription.processor.SubscriberRoutingRuleCalculationProcessor;
import com.attunedlabs.leap.eventsubscription.processor.SubscriptionCriteriaEvaluationProcessor;

/**
 * <code>SubscriberRouteBuilder</code> route builder implementation for
 * consuming message from topic and decide whether to process parallely or
 * sequentially based on subscriber condfiuration.
 * {@link SubscriptionCriteriaEvaluationProcessor},{@link SubscriberRoutingRuleCalculationProcessor}.
 * 
 * @author Reactiveworks42
 *
 */
public class SubscriberRouteBuilder extends RouteBuilder {

	final static Logger log = LoggerFactory.getLogger(SubscriberRouteBuilder.class);
	protected final IEventFrameworkConfigService eventFrameworkConfigService = new EventFrameworkConfigService();
	protected final IEventSubscriptionTrackerService eventSubscriptionLogService = new EventSubscriptionTrackerImpl();
	protected final static SubscriptionUtil subscriptionUtil = new SubscriptionUtil();
	int i = 0;
	private static Properties props = new Properties();
	static {
		InputStream inputStream;
		try {
			inputStream = SubscriberRouteBuilder.class.getClassLoader()
					.getResourceAsStream(SubscriptionConstant.KAFKA_CONSUMER_CONFIGS);
			props.load(inputStream);
		} catch (IOException e) {
			log.error("failed to load consumer properties..." + SubscriptionConstant.KAFKA_CONSUMER_CONFIGS);
		}
	}// ..end of static block to load the ConsumerProperties

	/**
	 * Given below is the sample incoming message and headers consumed from the
	 * subscribed topic on which the subscription based on subscriber is
	 * applied. <br>
	 * <br>
	 * <u><b>SAMPLE EVENT MESSAGE HEADERS</b></u><br>
	 * breadcrumbId=ID-DESKTOP-BT1LPN7-60753-1509603822231-0-4<br>
	 * kafka.CONTENT_TYPE=roiEvents<br>
	 * kafka.EXCHANGE_NAME=0<br>
	 * kafka.OFFSET=2 <br>
	 * kafka.TOPIC=testTopic<br>
	 * <br>
	 * <u><b>SAMPLE EVENT MESSAGE BODY </u></b><br>
	 * {<br>
	 * <i><b>"eventId"</i></b>: "PRINT_EVENT",<br>
	 * <i><b>"eventParam"</i></b>: {<br>
	 * &nbsp;"tenantId": "all",<br>
	 * &nbsp;"siteId": "all",<br>
	 * &nbsp;"labelformat": "ZPL"<br>
	 * },<br>
	 * <i><b>"eventHeader"</i></b>: {<br>
	 * &nbsp;"TenantId": null,<br>
	 * &nbsp;"CamelTimeStamp": "Mon Nov 06 11:47:27 IST 2017",<br>
	 * &nbsp;<i>"EVT_CONTEXT"</i>: {<br>
	 * &nbsp;&nbsp; &nbsp;"implementationName": "leapImpl",<br>
	 * &nbsp;&nbsp; &nbsp;"featureName": "helloworldservice",<br>
	 * &nbsp;&nbsp; &nbsp;"featureGroup": "sample",<br>
	 * &nbsp;&nbsp; &nbsp;"tenantId": "all",<br>
	 * &nbsp;&nbsp; &nbsp;"siteId": "all",<br>
	 * &nbsp;&nbsp; &nbsp;"vendorName": "leapVen",<br>
	 * &nbsp;&nbsp; &nbsp;"version": "1.0"},<br>
	 * &nbsp;"CamelContextId": "baseroute", <br>
	 * &nbsp;"CamelRouterId": "rest-get-post",<br>
	 * &nbsp;"requestGUUID": null } <br>
	 * }<br>
	 */
	@Override
	public void configure() throws Exception {

		final LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
		final Random randomClientId = new Random();

		// get the topics names from the subscription configured by
		// feature developer and subscribe to all the topics.
		Set<String> subscribers = leapConfigurationServer.getAllSubscribersAvailable();
		if (!subscribers.isEmpty()) {
			log.debug("these are all the subscribers available " + subscribers);

			// subscribers are present check the table exists or not.
			eventSubscriptionLogService.createTrackerTableForSubscription();

			boolean isRetryRouteBuild = false;
			for (final String subscriptionId : subscribers) {
				// got all the subscriberTopics based on subscibeId
				String topicNames = eventFrameworkConfigService.getSubscriptionTopicsbySubscriptionId(subscriptionId);
				log.debug("subscriber processing " + subscriptionId + " subscribing topic's " + topicNames);

				RouteDefinition startSubscriberRouteEndpoint = null;
				/********** KAFKA ENDPOINT AND RETRY ***********/
				if (!isRetryRouteBuild) {
					startSubscriberRouteEndpoint = from(SubscriptionUtil.constructKafkaURI(topicNames, subscriptionId,
							subscriptionUtil, randomClientId, props))
									.from(SubscriptionUtil.constructSedaURIForRetry(props));
					isRetryRouteBuild = true;
				} else
					startSubscriberRouteEndpoint = from(SubscriptionUtil.constructKafkaURI(topicNames, subscriptionId,
							subscriptionUtil, randomClientId, props));

				startSubscriberRouteEndpoint
						.process(new MessageProcessingWayDecider(eventFrameworkConfigService, subscriptionUtil))
						.toD("${header." + SubscriptionConstant.PROCESSING_DECISION_KEY + "}");

			}

		} else {
			log.info("There is no subsciber feature found to load...");
		}

	}

}