package com.integration.eventbuilber;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.Event;

public class CustomEventBuilder1 extends AbstractCamelEventBuilder{

	public CustomEventBuilder1() {
		// TODO Auto-generated constructor stub
		
	}

	@Override
	 public LeapEvent buildEvent(Exchange exchange, Event event) {
		logger.debug("inside the custom builder 1  to build the event");
		JSONObject kafkaJsonObject=exchange.getIn().getBody(JSONObject.class);
		LeapEvent leapEvent=null;
		try {
			String eventId=kafkaJsonObject.getString("EventId");
			JSONObject eventHeader=kafkaJsonObject.getJSONObject("EventHeader");
			String eventName = (String)exchange.getIn().getHeader("event-Id");
			leapEvent = super.updateStandardCamelHeader(eventName, exchange);
			leapEvent.addEventHeader("sequenceId",
					eventHeader.getString("sequenceId"));
			leapEvent.addEventHeader("correlationId",
					eventHeader.getString("correlationId"));
			leapEvent.addEventHeader("eventId",
					eventId);
			leapEvent.addEventHeader("configKey",
					eventHeader.getString("configKey"));
			Map<String, Serializable> source=new HashMap<String, Serializable>();
			source.put("source", kafkaJsonObject.getJSONObject(EventFrameworkConstants.EVENT_PARAM_KEY).toString());
				   leapEvent.setEventParam(source);
				   logger.debug("leap event is : "+leapEvent.toString());
		} catch (JSONException e) {
			logger.debug("invalid format of event is requested to process:" + e.getMessage());
		}
		
		return leapEvent;
		
	}
}
