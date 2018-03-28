package com.attunedlabs.eventframework.dispatcher.transformer;


import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.event.LeapEvent;

public class GenericLeapEventJsonTransformer implements ILeapEventTransformer {

	protected static final Logger logger = LoggerFactory.getLogger(GenericLeapEventJsonTransformer.class);

	@Override
	public Serializable  transformEvent(LeapEvent leapevent)	throws LeapEventTransformationException {
		
		logger.debug("inside TransformDispatcherImpl Bean transformDispatcherData()");
		JSONObject json=new JSONObject();
		if(leapevent == null){
			throw new LeapEventTransformationException();
		}
		try {
			json.put(EventFrameworkConstants.EVENT_ID_KEY,leapevent.getEventId());
			json.put(EventFrameworkConstants.EVENT_HEADER_KEY,leapevent.getEventHeader());
			json.put(EventFrameworkConstants.EVENT_PARAM_KEY,leapevent.getEventParam());
			
			logger.debug("json data : "+json);
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		
		return json.toString();
	}

//	public static void main(String[] args) {
//		GenericLeapEventJsonTransformer imp=new GenericLeapEventJsonTransformer();
//		LeapEvent event=new LeapEvent("HELLO");
//		event.addEventHeader("TenantId", "123");
//		event.addEventHeader("contextid", "basecontext");
//		event.addEventParam("eventid","HELLO");
//		event.addEventParam("ok","everything is ok");
//		try {
//			imp.transformEvent(event);
//		} catch (LeapEventTransformationException e) {
//			logger.error("error : "+e.getMessage());
//		}
//		
//	}
}
