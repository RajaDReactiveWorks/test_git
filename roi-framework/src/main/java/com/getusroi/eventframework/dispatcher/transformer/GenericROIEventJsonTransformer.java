package com.getusroi.eventframework.dispatcher.transformer;


import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.eventframework.event.ROIEvent;

public class GenericROIEventJsonTransformer implements IROIEventTransformer {

	protected static final Logger logger = LoggerFactory.getLogger(GenericROIEventJsonTransformer.class);

	@Override
	public Serializable  transformEvent(ROIEvent roievent)	throws ROIEventTransformationException {
		
		logger.debug("inside TransformDispatcherImpl Bean transformDispatcherData()");
		JSONObject json=new JSONObject();
		if(roievent == null){
			throw new ROIEventTransformationException();
		}
		try {
			json.put("eventid",roievent.getEventId());
			json.put("eventHeader",roievent.getEventHeader());
			json.put("eventparam",roievent.getEventParam());
			
			logger.debug("json data : "+json);
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		
		return json.toString();
	}

//	public static void main(String[] args) {
//		GenericROIEventJsonTransformer imp=new GenericROIEventJsonTransformer();
//		ROIEvent event=new ROIEvent("HELLO");
//		event.addEventHeader("TenantId", "123");
//		event.addEventHeader("contextid", "basecontext");
//		event.addEventParam("eventid","HELLO");
//		event.addEventParam("ok","everything is ok");
//		try {
//			imp.transformEvent(event);
//		} catch (ROIEventTransformationException e) {
//			logger.error("error : "+e.getMessage());
//		}
//		
//	}
}
