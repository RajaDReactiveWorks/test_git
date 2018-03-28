package com.attunedlabs.leap.eventsubscription.routebuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.abstractretrystrategy.InstantiateSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.exception.MissingConfigurationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;
import com.attunedlabs.leap.LeapHeaderConstant;

/**
 * <code>GenericRetryRouteBuilder</code> route builder implementation for
 * retrying the failed subscriptions based on the list provided by the
 * getRetryableRecords() invoked on the implementation class of
 * <code>AbstractSubscriptionRetryStrategy</code>.
 * 
 * @see {@link AbstractSubscriptionRetryStrategy}.
 * @author Reactiveworks42
 *
 */
public class GenericRetryRouteBuilder extends RouteBuilder {
	final static Logger logger = LoggerFactory.getLogger(GenericRetryRouteBuilder.class);
	static SubscriptionUtil subscriptionUtil = new SubscriptionUtil();
	final public static List<EventSubscriptionTracker> totalRetryableRecords = new ArrayList<>();

	private static Properties props = new Properties();
	static {
		InputStream inputStream;
		try {
			inputStream = GenericRetryRouteBuilder.class.getClassLoader()
					.getResourceAsStream(SubscriptionConstant.SUBSCRIPTION_QUARTZ_CONFIGS);
			props.load(inputStream);
		} catch (IOException e) {
			logger.error("failed to load quartz config..." + SubscriptionConstant.SUBSCRIPTION_QUARTZ_CONFIGS);
		}
	}// ..end of static block to load the ConsumerProperties

	@Override
	public void configure() throws Exception {
		logger.debug("inside configure() of GenericRetryRouteBuilder...");
		final HashMap<String, Map<String, AbstractSubscriptionRetryStrategy>> cachingInstance = InstantiateSubscriptionRetryStrategy
				.getCachingInstance();

		if (cachingInstance.size() != 0) {

			// quartz for failed subscription next quartz will trigger once all
			// the failed subscription are processed
			from(SubscriptionUtil.constructQuartzURI(props))

					// skip the failed subscription of failed to fing required
					// data
					.onException(Exception.class).continued(true).process(new Processor() {
						@Override
						public void process(Exchange exchange) throws Exception {
							Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
							log.error("ignore exception's thrown from retry route proceed for next failed subscription"
									+ exception.getMessage());

						}
					}).end().

					// calling getRetryableRecords on all the subscriptions.
					process(new Processor() {

						@Override
						public void process(Exchange exchange) throws Exception {
							while (!totalRetryableRecords.isEmpty()) {
								TimeUnit.MILLISECONDS.sleep(200);
							}
							System.out.println("THREAD WOKE");
							logger.debug("INITAL \n" + "\n GenericRetryRouteBuilder.totalRetryableRecords \n"
									+ GenericRetryRouteBuilder.totalRetryableRecords.size());
							exchange.getIn().setHeader(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY, true);
							Set<String> tenantSiteKeySet = cachingInstance.keySet();
							for (String tenantSiteKey : tenantSiteKeySet) {
								Map<String, AbstractSubscriptionRetryStrategy> classInstanceMap = cachingInstance
										.get(tenantSiteKey);
								String tenantId = LeapHeaderConstant.tenant;
								String siteId = LeapHeaderConstant.site;
								String[] tenantSiteArray = tenantSiteKey
										.split(SubscriptionConstant.TENANT_SITE_SEPERATOR);
								if (tenantSiteArray.length == 2) {
									tenantId = tenantSiteArray[0];
									siteId = tenantSiteArray[1];
								}
								Set<String> classNameKeys = classInstanceMap.keySet();
								for (String subIdAndclassName : classNameKeys) {
									String[] subIdClassArray = subIdAndclassName
											.split(SubscriptionConstant.SUB_ID_CLASS_SEPERATOR);
									String subscriptionId = subIdClassArray[0];
									AbstractSubscriptionRetryStrategy abstractSubscriptionRetryStrategy = classInstanceMap
											.get(subIdAndclassName);
									List<EventSubscriptionTracker> retryableRecords = abstractSubscriptionRetryStrategy
											.getRetryableRecords(exchange, tenantId, siteId, subscriptionId);
									if (retryableRecords != null)
										totalRetryableRecords.addAll(retryableRecords);
								}
							}
							exchange.getIn().setHeader(SubscriptionConstant.RETRYABLE_SUBSCRIPTIONS_COUNT_KEY,
									totalRetryableRecords.size());

						}
					}).
					// looping on each subscription
					loop(header(SubscriptionConstant.RETRYABLE_SUBSCRIPTIONS_COUNT_KEY)).
					// clone the exchange for every subscription
					copy()

					// exception handling to skip the iteration if
					// the subscription failed to retry.
					.doTry().

					process(new Processor() {

						@Override
						public void process(Exchange exchange) throws Exception {
							EventSubscriptionTracker eventSubscriptionTracker = totalRetryableRecords
									.get(exchange.getProperty(SubscriptionConstant.LOOP_INDEX_KEY, Integer.class));
							try {

								if (subscriptionUtil.attributeEmptyCheck(eventSubscriptionTracker.getSubscriptionId())
										|| subscriptionUtil.attributeEmptyCheck(eventSubscriptionTracker.getTopic())
										|| subscriptionUtil.attributeEmptyCheck(eventSubscriptionTracker.getPartition())
										|| subscriptionUtil.attributeEmptyCheck(eventSubscriptionTracker.getOffset())
										|| subscriptionUtil
												.attributeEmptyCheck(eventSubscriptionTracker.getEventData()))
									throw new MissingConfigurationException(
											"RETRY ATTEMPT FAILED : Failed subscriptionId "
													+ eventSubscriptionTracker.getSubscriptionId() + ""
													+ " cannot be retried due to either details in EventSubscriptionTracker are"
													+ " empty(subscriptionId,topic,partition,offset,eventData)");

								String eventBody = eventSubscriptionTracker.getEventData();
								JSONObject jsonBody = subscriptionUtil.identifyContentType(eventBody);
								if (!jsonBody.has(EventFrameworkConstants.EVENT_HEADER_KEY))
									throw new MissingConfigurationException(
											"INVALID EVENT DATA : Cannot retry subscription with ID "
													+ eventSubscriptionTracker.getSubscriptionId()
													+ "because, either of key is not found in event data "
													+ "(eventHeader,eventParam,eventId) ");

								exchange.getIn().setHeader(SubscriptionConstant.RETRYABLE_SUBSCRIPTIONS_COUNT_KEY,
										totalRetryableRecords.size());
								exchange.getIn().setHeader(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS,
										eventSubscriptionTracker);
								exchange.getIn().setHeader(KafkaConstants.TOPIC, eventSubscriptionTracker.getTopic());
								exchange.getIn().setHeader(KafkaConstants.PARTITION,
										eventSubscriptionTracker.getPartition());
								exchange.getIn().setHeader(KafkaConstants.OFFSET, eventSubscriptionTracker.getOffset());
								exchange.getIn().setHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY,
										eventSubscriptionTracker.getSubscriptionId());
								exchange.getIn().setBody(eventBody, String.class);

							} catch (Exception e) {
								log.error("FAILED RETRYING SUBSCRIPTION : " + e.getMessage());
								throw e;
							}
						}
					}).
					// call the same logic to invoke any action for failed
					// subscriptions
					to("seda:subscriptionRouteForRetry")

					// end of the
					// try blocks
					.endDoTry()

					// catch the exception raised.
					.doCatch(Exception.class)

					// warp the exception and rethrow it the route
					// level onException clause specified.
					.process(new Processor() {
						public void process(Exchange exchange) throws Exception {
							Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
							log.debug("wrap and rethrow retry subscription exception " + exception.getMessage());
							throw exception;
						}
					})

					// end of try-catch clause
					.end()
					// end of loop .
					.end().process(new Processor() {

						@Override
						public void process(Exchange exchange) throws Exception {
							// clearing the record list for retry to trigger
							// next time.
							totalRetryableRecords.clear();
							log.debug("FINISHED RETRYING...");
						}
					});

		} else
			logger.debug("no retry startegy cached found...GenericRetryRouteBuilder cannot be build");

	}

}
