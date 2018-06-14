package com.integration.eventbuilber;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.dispatcher.transformer.ILeapEventTransformer;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.event.LeapEvent;

public class CustomTranformer1 implements ILeapEventTransformer {
	private final static Logger logger = LoggerFactory.getLogger(CustomEventTransformer.class.getName());

	public CustomTranformer1() {
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
		
			Map<String, Serializable> eventHeader = leapevent.getEventHeader();
			/*String eventHeaderString=(String)eventHeader.get("EventHeader");
			JSONObject eventHeaderJson=new JSONObject(eventHeaderString);*/
			finalJson.put("EventParam", sourceJson);
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
