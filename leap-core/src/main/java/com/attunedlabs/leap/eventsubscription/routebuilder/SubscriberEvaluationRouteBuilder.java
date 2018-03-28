package com.attunedlabs.leap.eventsubscription.routebuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
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
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.SubscriptionFailureHandlerBean;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.SubscriptionPerProcessHandlerBean;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.SubscriptionSuccessHandlerBean;
import com.attunedlabs.leap.eventsubscription.processor.HttpPostRequestProcessor;
import com.attunedlabs.leap.eventsubscription.processor.InvokeCamelRouteProcessor;
import com.attunedlabs.leap.eventsubscription.processor.PipelineProcessor;
import com.attunedlabs.leap.eventsubscription.processor.SubscriberActionIndentificationProcessor;
import com.attunedlabs.leap.eventsubscription.processor.SubscriberRoutingRuleCalculationProcessor;
import com.attunedlabs.leap.eventsubscription.processor.SubscriptionCriteriaEvaluationProcessor;

/**
 * <code>SubscriberRouteBuilder</code> route builder implementation for
 * evaluating the subscriber configuration on the consumed event message with
 * the help of different processors like
 * {@link SubscriptionCriteriaEvaluationProcessor},{@link SubscriberRoutingRuleCalculationProcessor}.
 * This class will create two endpoint for each subscriber one is direct and
 * seda for based on subscriber configuration descision will be done how the
 * message will be processed.
 * 
 * @author Reactiveworks42
 *
 */
public class SubscriberEvaluationRouteBuilder extends RouteBuilder {

	final static Logger log = LoggerFactory.getLogger(SubscriberEvaluationRouteBuilder.class);
	protected final IEventFrameworkConfigService eventFrameworkConfigService = new EventFrameworkConfigService();
	protected final IEventSubscriptionTrackerService eventSubscriptionLogService = new EventSubscriptionTrackerImpl();
	protected final static SubscriptionUtil subscriptionUtil = new SubscriptionUtil();
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

	@Override
	public void configure() throws Exception {

		final LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();

		// get the topics names from the subscription configured by
		// feature developer and subscribe to all the topics.
		Set<String> subscribers = leapConfigurationServer.getAllSubscribersAvailable();
		if (!subscribers.isEmpty()) {
			for (final String subscriptionId : subscribers) {

				RouteDefinition startSubscriberRouteEndpoint = null;
				/**********
				 * PER SUBSCRIBER DIRECT and SEDA EXECUTION ROUTE
				 ***********/
				startSubscriberRouteEndpoint = from(SubscriptionUtil.constructSedaURIToProcessMessage(props,
						subscriptionUtil.getActualSubscriberId(subscriptionId)))
								.from(SubscriptionConstant.SIMPLE_PROCESSING_ROUTE_ENDPOINT
										+ subscriptionUtil.getActualSubscriberId(subscriptionId));

				// write the exception handling logic to retry here
				// for the failed subscribers.
				// continue to the next iterations on exception type by writing
				// own predicates.
				startSubscriberRouteEndpoint.onException(Exception.class).continued(true)
						.process(new SubscriptionFailureHandlerBean()).end()

						.log("start the subscription process for topic ...")
						.setProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY).constant(true)

						//do this stuff if invoked via retry Thread
						.choice().when(header(SubscriptionConstant.KAFKA_CALL).isNull())
						// Subscription criteria evaluation
						.process(new SubscriptionCriteriaEvaluationProcessor(eventFrameworkConfigService,
								subscriptionUtil))
						// invoking pre-process activity from retry
						// lifecycle
						.process(new SubscriptionPerProcessHandlerBean())
						.endChoice().end()

						// calculating the number of event routing rules
						// configured for subscriber.
						.process(new SubscriberRoutingRuleCalculationProcessor(eventFrameworkConfigService,
								subscriptionUtil))

						// get the count of rules
						.loop(header(SubscriptionConstant.ROUTING_RULES_PER_SUBSCIBER_LOOP_COUNT_KEY))

						// and clone the exchange for every routing
						// rule.
						.copy()

						// exception handling to skip the iteration if
						// the
						// routing rules specified by subscriber fails
						// to
						// evaluate.
						.doTry()

						/** ACTION IDENTIFICATION MECHANISM **/
						.process(new SubscriberActionIndentificationProcessor(subscriptionUtil)).choice()

						/** INVOKE CAMEL ROUTE ACTION **/
						.when(header(SubscriptionConstant.ACTION_KEY)
								.isEqualTo(SubscriptionConstant.INVOKE_CAMEL_ROUTE_KEY))
						// invoke the camel route..
						.process(new InvokeCamelRouteProcessor(subscriptionUtil))

						// new exchange is created and forwarded to
						// respective route.
						.choice().when(header(SubscriptionConstant.ROUTE_ENDPOINT_KEY).isNotNull())
						.toD("${header." + SubscriptionConstant.ROUTE_ENDPOINT_KEY + "}").process(new Processor() {

							@Override
							public void process(Exchange exchange) throws Exception {
								log.debug("processor invocation after INVOKE_CAMEL_ROUTE");
								Message outMessage = exchange.getIn();
								log.info("CamelExchange after invocation: Headers => " + outMessage.getHeaders());
								log.info("CamelExchange after invocation: BODY => " + outMessage.getBody());
							}
						})

						/** HTTP POST REQUEST ACTION **/
						.when(header(SubscriptionConstant.ACTION_KEY)
								.isEqualTo(SubscriptionConstant.HTTP_POST_REQUEST_KEY))
						// invoke service
						.process(new HttpPostRequestProcessor(subscriptionUtil)).process(new Processor() {

							@Override
							public void process(Exchange exchange) throws Exception {
								log.debug("processor invocation after  HTTP_POST_REQUEST");
								Message outMessage = exchange.getIn();
								log.info("CamelExchange after call: HTTP_POST_REQUEST Headers => "
										+ outMessage.getHeaders());
								log.info("CamelExchange after call: HTTP_POST_REQUEST BODY=> " + outMessage.getBody());
							}
						})

						/** PIPELINE ACTION **/
						.when(header(SubscriptionConstant.ACTION_KEY).isEqualTo(SubscriptionConstant.PIPELINE_KEY))
						.process(new PipelineProcessor(subscriptionUtil))
						.toD("${header." + SubscriptionConstant.ROUTE_ENDPOINT_KEY + "}").process(new Processor() {

							@Override
							public void process(Exchange exchange) throws Exception {
								log.debug("processor invocation for evaluating PIPELINE");
								Message outMessage = exchange.getIn();
								log.info("CamelExchange after PIPELINE: Headers => " + outMessage.getHeaders());
								log.info("CamelExchange after PIPELINE: BODY => " + outMessage.getBody());

							}
						}).

						/** DEFAULT ACTION **/
						otherwise().process(new Processor() {

							@Override
							public void process(Exchange exchange) throws Exception {
								log.debug("processor invocation for evaluating UNSUPPORTED_ACTIONS");
								Message outMessage = exchange.getIn();
								log.info("CamelExchange after UNSUPPORTED_ACTIONS: Headers => "
										+ outMessage.getHeaders());
								log.info("CamelExchange after UNSUPPORTED_ACTIONS: BODY => " + outMessage.getBody());
							}
						})

						.endChoice()

						// do some task on the exchange once subscriber
						// gets the message.
						.process(new SubscriptionSuccessHandlerBean())

						// end of both the try blocks
						.endDoTry()

						// catch the exception raised.
						.doCatch(Exception.class)

						// warp the exception and rethrow it the route
						// level onException clause specified.
						.process(new Processor() {
							public void process(Exchange exchange) throws Exception {
								Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
								log.debug("wrap and rethrow routing rule loop exception " + exception.getMessage());
								throw exception;
							}
						})

						// end of inner-most try-catch clause for
						// routing
						// rule loop.
						.end()
						// end of inner loop for event routing rule.
						.end();

			}
		} else
			log.info("There is no subsciber feature found to load...");
	}

}
