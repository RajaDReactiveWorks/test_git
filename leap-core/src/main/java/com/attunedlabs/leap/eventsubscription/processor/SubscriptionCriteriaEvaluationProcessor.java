package com.attunedlabs.leap.eventsubscription.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.exception.MissingConfigurationException;
import com.attunedlabs.eventsubscription.exception.NonRetryableException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;

/**
 * <code>SubsciptionCriteriaEvaluationProcessor</code> will evaluate the event
 * subscription criteria on consumed event message from the subscribed topic and
 * will decide whether to process these event message present in exchange
 * further to apply EventRoutingRule.
 * 
 * @author Reactiveworks42
 *
 */
public class SubscriptionCriteriaEvaluationProcessor implements Processor {
	final static Logger log = LoggerFactory.getLogger(SubscriptionCriteriaEvaluationProcessor.class);

	private IEventFrameworkConfigService eventFrameworkConfigService;
	private SubscriptionUtil subscriptionUtil;

	public SubscriptionCriteriaEvaluationProcessor(IEventFrameworkConfigService eventFrameworkConfigService,
			SubscriptionUtil subscriptionUtil) {
		this.eventFrameworkConfigService = eventFrameworkConfigService;
		this.subscriptionUtil = subscriptionUtil;

	}

	/**
	 * adds some extra headers such as configuration-context to the exchange
	 * such as loop based on the subscribers who matches the
	 * subscription-criteria.
	 * 
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		log.debug("processor invocation for evaluating subscription criteria");

		try {
			if (exchange.getIn() != null) {
			
				Message message = exchange.getIn();
				Object data = message.getBody();

				String topicName = null;
				String partition = null;
				String offset = null;
				String body = null;
				String subscriberId = null;

				// converting eventbody consumed into jsonObj
				// and build the configuration Context.
				body = exchange.getIn().getBody(String.class);

				// will convert the xml to json
				JSONObject eventBody = subscriptionUtil.identifyContentType(body);

				ConfigurationContext configCtx = null;

				// preserve topic metadata & keeping group Id as
				// subscriberid
				LeapHeader leapHeader = new LeapHeader();
				topicName = exchange.getIn().getHeader(KafkaConstants.TOPIC, String.class);
				partition = exchange.getIn().getHeader(KafkaConstants.PARTITION, String.class);
				offset = exchange.getIn().getHeader(KafkaConstants.OFFSET, String.class);
				// call from retry-mechanism
				subscriberId = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, String.class);

				if (subscriberId == null || subscriberId.trim().isEmpty())
					throw new MissingConfigurationException("SUBSCRIBER ID DOESN'T EXISTS : '"
							+ SubscriptionConstant.SUBSCRIPTION_ID_KEY + "' key not found in exchange Header...");

				// storeLeap and strategy class
				setGenericLocalData(exchange, leapHeader, subscriberId, topicName, partition, offset, body);
				exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);

				log.debug("topic name retrived: " + topicName);
				log.debug("subscribers for topic " + subscriberId);

				Map<String, Object> genricdata = leapHeader.getGenricdata();
				EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) genricdata
						.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);

				String tenant = eventSubscriptionTracker.getTenantId();
				String site = eventSubscriptionTracker.getSiteId();
				if (tenant == null || site == null || tenant.trim().isEmpty() || site.trim().isEmpty()) {
					try {
						// building configuration context to fetch eventing
						// configuration
						JSONObject eventHeadersJSON = (JSONObject) eventBody
								.get(EventFrameworkConstants.EVENT_HEADER_KEY);
						// getting tenant, site from event data.
						if (eventHeadersJSON != null) {
							tenant = eventHeadersJSON.getString(LeapHeaderConstant.TENANT_KEY);
							site = eventHeadersJSON.getString(LeapHeaderConstant.SITE_KEY);
							// avoided boiler plate code by throwing exception
							if (tenant == null || site == null || tenant.trim().isEmpty() || site.trim().isEmpty())
								throw new Exception();
						}
					} catch (Exception e) {
						// will use global tenant for now, just atleast we
						// can log by getting subsciber configuration.
						tenant = LeapHeaderConstant.tenant;
						site = LeapHeaderConstant.site;

						// just to make non-retry as false because of default
						// assignation
						genricdata.put(SubscriptionConstant.DEFAULT_ASSIGNED_TENANT_KEY, true);

						// logic handled in
						// SubscriberActionIndentificationProcessor
					}
					eventSubscriptionTracker.setTenantId(tenant);
					eventSubscriptionTracker.setSiteId(site);
				}

				configCtx = subscriptionUtil.buildConfigContext(tenant, site, subscriberId);
				// setting tenant,site
				leapHeader.setTenant(tenant);
				leapHeader.setSite(site);
				exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
				exchange.getIn().setHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, subscriberId);

				log.debug(
						"setting the leap header in exchange of subscriber : " + subscriberId + " == > " + leapHeader);

				log.debug("EXCHANGE :" + data + "\nSUBSCRIBER-GROUP-ID : " + subscriberId + " \nHeaders"
						+ exchange.getIn().getHeaders());

				// assuming the context coming through the event body.
				// ConfigurationContext configCtx =
				// subscriptionUtil.getConfigContextFromEventBody(eventBody);

				log.debug("ConfigurationContext formed " + configCtx);
				if (configCtx == null)
					throw new MissingConfigurationException(
							"MISSING CONFIGURATION PARAMETERS: for subscriber with Id : " + subscriberId
									+ " ==> expected parameters are not present to build ConfigurationContext!");

				SubscribeEvent eventSubscription = null;
				try {
					eventSubscription = eventFrameworkConfigService.getEventSubscriptionConfiguration(configCtx,
							subscriptionUtil.getActualSubscriberId(subscriberId));
				} catch (Exception e) {
					eventSubscription = null;

				}

				if (eventSubscription != null) {
					String eventSubscriptionCriteria = eventSubscription.getEventSubscriptionCriteria();

					log.debug("updating the retries leap header in exchange of subscriber : " + subscriberId + " == > "
							+ leapHeader);

					// if the criteria is specified than add ,if not
					// specified then directly add subscriber in exchange
					// header.
					if (eventSubscriptionCriteria == null || eventSubscriptionCriteria.trim().isEmpty())
						exchange.getIn().setHeader(SubscriptionConstant.SUBSCIBER_EVENT_CONFIG_KEY, eventSubscription);
					else if (subscriptionUtil.evaluateMVELForCriteriaMatch(eventSubscriptionCriteria.trim(), eventBody))
						exchange.getIn().setHeader(SubscriptionConstant.SUBSCIBER_EVENT_CONFIG_KEY, eventSubscription);

				} else {
					eventSubscription = reloadConfigurationForGlobalTenant(configCtx,
							subscriptionUtil.getActualSubscriberId(subscriberId));
					if (eventSubscription == null)
						throw new MissingConfigurationException(
								"NO CONFIGURATION FOUND : Fetched empty SubscriptionConfiguration  "
										+ "in DataGrid for requested subscriber : " + subscriberId);
					exchange.getIn().setHeader(SubscriptionConstant.SUBSCIBER_EVENT_CONFIG_KEY, eventSubscription);
					// just to make non-retry as false because of default
					// assignation
					genricdata.put(SubscriptionConstant.NO_CONFIG_FOR_TENANT_KEY, true);
				}

				// setting required config in camel Exchange
				// Headers
				exchange.getIn().setBody(eventBody, String.class);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new NonRetryableException("PRE-FAILED NON-RETRYABLE[" + e.getMessage() + "]", e);
		}
	}

	/**
	 * loading configuration for global tenant.
	 * 
	 * @param configCtx
	 * @param actualSubscriberId
	 * @return
	 * @throws MissingConfigurationException
	 */
	private SubscribeEvent reloadConfigurationForGlobalTenant(ConfigurationContext configCtx, String actualSubscriberId)
			throws MissingConfigurationException {
		// will use global tenant for now, just atleast we
		// can log by getting subsciber configuration.
		configCtx.setTenantId(LeapHeaderConstant.tenant);
		configCtx.setSiteId(LeapHeaderConstant.site);

		try {
			SubscribeEvent eventSubscription = eventFrameworkConfigService.getEventSubscriptionConfiguration(configCtx,
					actualSubscriberId);
			return eventSubscription;
		} catch (Exception e) {

			// logic handled in
			// SubscriberActionIndentificationProcessor
			throw new MissingConfigurationException(
					"FETCHING CONFIGURATION FAILED : Unable to fetch the configuration context "
							+ "in DataGrid for requested subscriber : " + actualSubscriberId + " due to "
							+ e.getMessage());
		}
	}

	/**
	 * setting the generic data in leapHeader
	 * 
	 * @param exchange
	 * @param leapHeader
	 * @param subscriberId
	 * @param topicName
	 * @param partition
	 * @param offset
	 * @param body
	 * @throws MissingConfigurationException
	 */
	private void setGenericLocalData(Exchange exchange, LeapHeader leapHeader, String subscriberId, String topicName,
			String partition, String offset, String body) throws MissingConfigurationException {
		EventSubscriptionTracker eventSubscriptionTracker = exchange.getIn()
				.getHeader(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS, EventSubscriptionTracker.class);

		EventSubscriptionTracker eventSubscriptionTracker2 = eventSubscriptionTracker;

		HashMap<String, Object> threadProcessMap = new HashMap<>();

		if (eventSubscriptionTracker2 == null) {
			eventSubscriptionTracker2 = new EventSubscriptionTracker();
			eventSubscriptionTracker2.setSubscriptionId(subscriberId);
			eventSubscriptionTracker2.setTopic(topicName);
			eventSubscriptionTracker2.setPartition(partition);
			eventSubscriptionTracker2.setOffset(offset);
			eventSubscriptionTracker2.setEventData(body);
			eventSubscriptionTracker2.setIsRetryable(false);
			eventSubscriptionTracker2.setRetryCount(0);
		}

		if (eventSubscriptionTracker2 != null)
			threadProcessMap.put(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS, eventSubscriptionTracker2);

		// removing from the exchange as stored in leapHeader generic map
		if (exchange.getIn().getHeader(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS) != null)
			exchange.getIn().removeHeader(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);

		// just to identify the retry
		Boolean isRetryTriggered = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY,
				Boolean.class);

		if (isRetryTriggered == null)
			threadProcessMap.put(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY, false);
		else
			threadProcessMap.put(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY, isRetryTriggered);

		// set leap Header before fetching tenant and site from event
		// body and setting topic-metadata
		leapHeader.setGenricdata(threadProcessMap);
	}

}
