package com.attunedlabs.integrationfwk.activities.event;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.dispatcher.EventFrameworkDispatcherException;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.integrationfwk.activities.bean.ActivityConstant;
import com.attunedlabs.integrationfwk.config.jaxb.EventData;
import com.attunedlabs.integrationfwk.config.jaxb.EventPublishActivity;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigHelper;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigurationException;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;

public class PipelineEventBuilder extends AbstractCamelEventBuilder {
	Logger logger = LoggerFactory.getLogger(PipelineEventBuilder.class);

	/**
	 * This method is used to build event for pipeline Activity
	 * 
	 * @param camelExchange
	 *            : Camel Exchange Object
	 * @param eventConfig:
	 *            Event Object
	 */
	@Override
	public LeapEvent buildEvent(Exchange camelExchange, Event eventConfig) {
		logger.debug(".buildEvent method of PipelineEventBuilder");

		LeapHeader leapHeader = (LeapHeader) camelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		PipeActivity pipeactivity = (PipeActivity) camelExchange.getIn()
				.getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		EventPublishActivity eventPublishActivity = pipeactivity.getEventPublishActivity();
		JdbcIntActivityConfigHelper activityConfigHelper = new JdbcIntActivityConfigHelper();
		String inBodyData = camelExchange.getIn().getBody(String.class);
		PipelineEvent pipelineEvent = null;
		try {
			Document document = activityConfigHelper.generateDocumentFromString(inBodyData);
			logger.debug("=:=:=:In Body Data : " + inBodyData);
			pipelineEvent = createPipeLineEvent(eventPublishActivity, document, leapHeader);
		} catch (JdbcIntActivityConfigurationException e) {
			// #TODO, build event doesnot throw exception so catching it
			logger.error("error in geting the document object for incoming exchange body: " + inBodyData, e);
		}
		return pipelineEvent;
	}

	/**
	 * This method is used to create pipeline event
	 * 
	 * @param eventPublishActivity
	 *            : EventPublishActivity Object
	 * @param document
	 *            : DOcument Object
	 * @param leapHeader
	 *            : LeapHeader Object
	 * @return PipelineEvent Object
	 * @throws EventFrameworkConfigurationException
	 * @throws EventFrameworkDispatcherException
	 */
	private PipelineEvent createPipeLineEvent(EventPublishActivity eventPublishActivity, Document document,
			LeapHeader leapHeader) {
		RequestContext requestContext = leapHeader.getRequestContext();
		// creating pipeline event object
		logger.debug("EventName : : : : : " + eventPublishActivity.getEventName().trim());
		PipelineEvent pipelineEvent = new PipelineEvent(eventPublishActivity.getEventName().trim(), requestContext);
		Map<String, Serializable> eventParam = pipelineEvent.getEventParam();
		logger.debug("eventParam : : : " + eventParam.keySet().toString());
		List<EventData> eventDataList = eventPublishActivity.getEventActivityParams().getEventData();
		XPath xPath = XPathFactory.newInstance().newXPath();
		JSONObject eventJObj = new JSONObject();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		for (EventData eventData : eventDataList) {
			try {
				logger.debug("eventData :- " + eventData.getValue() + ":: eventExpressionValue : "
						+ eventData.getExpressionValue());
				XPathExpression expr = xPath.compile(eventData.getXpathExpression());
				String eventName = eventData.getExpressionValue().toString();
				NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
				if (nodeList != null && nodeList.getLength() > 0) {
					try {
						if (nodeList.item(0).getNodeName().contains("text") && eventName != null) {
							eventJObj.put(eventName, nodeList.item(0).getTextContent());
							logger.debug("added " + nodeList.item(0).getTextContent() + " into the eventName");
						} else if (eventName != null) {
							eventJObj.put(eventName, nodeList.item(0).getTextContent());
							logger.debug("added " + nodeList.item(0).getTextContent() + " into the eventName");
						}
					} catch (DOMException | JSONException e) {
						logger.error("Unable to add " + nodeList.item(0).getNodeName() + " into "
								+ EventFrameworkConstants.EVENT_PARAM_KEY);
					}
				}
			} catch (XPathExpressionException e) {
				logger.error("error in processing the xpath : " + eventData.getXpathExpression(), e);
			}
			Calendar cal = Calendar.getInstance();
			String eventRaisedDateTime = dateFormat.format(cal.getTime());
			try {
				logger.debug("EventCreatedOn : = > " + eventRaisedDateTime);
				eventJObj.put("EventCreatedOn", eventRaisedDateTime);
			} catch (JSONException e) {
				logger.error("Unable to add EventCreatedOn into " + EventFrameworkConstants.EVENT_PARAM_KEY);
			}
		}
		pipelineEvent.addEventParam(EventFrameworkConstants.EVENT_PARAM_KEY, eventJObj.toString());
		logger.debug("Final Event BuildUp : " + pipelineEvent.getEventParam().toString());
		return pipelineEvent;
	}// end of method createPipeLineEvent

}
