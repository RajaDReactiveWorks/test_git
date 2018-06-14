package com.integration.eventbuilber;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.dispatcher.transformer.ILeapEventTransformer;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.google.gson.Gson;

public class CustomEventTransformer implements ILeapEventTransformer {
	private final static Logger logger = LoggerFactory.getLogger(CustomEventTransformer.class.getName());

	public CustomEventTransformer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Serializable transformEvent(LeapEvent leapevent) throws LeapEventTransformationException {
		logger.debug("inside the custom transformEvent method");
		JSONObject finalJson = new JSONObject();
		Map<String, Serializable> eventParam = leapevent.getEventParam();
		String source = (String) eventParam.get("source");
		JSONObject sourceJson = null;
		try {
			sourceJson = new JSONObject(source);
			JSONObject finalsourceJson = new JSONObject();
			finalsourceJson.put("Source", sourceJson);
			Map<String, Serializable> eventHeader = leapevent.getEventHeader();
			/*String eventHeaderString=(String)eventHeader.get("EventHeader");
			JSONObject eventHeaderJson=new JSONObject(eventHeaderString);*/
			finalJson.put("EventParam", finalsourceJson);
			finalJson.put("EventHeader", eventHeader);
			String eventId = (String) eventHeader.get("eventId");
			finalJson.put("EventId", eventId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return finalJson.toString();

	}
}
