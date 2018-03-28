package com.attunedlabs.eventframework.eventtracker.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.dispatchchannel.exception.RetryableMessageDispatchingException;
import com.attunedlabs.eventframework.eventtracker.impl.EventTrackerTableConstants;

public class EventTrackerUtil {

	/**
	 * create the json for failure events.
	 * 
	 * @param failureJson
	 * @param exception
	 * @param failureMsg
	 */
	public static void setFailureJSONString(JSONObject failureJson, Exception exception, String failureMsg) {
		if (failureMsg == null)
			failureMsg = "empty exception message!";
		else
			failureMsg = exception.getMessage();

		try {
			failureJson.put(EventTrackerTableConstants.FAILURE_TYPE, exception.getClass().getSimpleName());
			failureJson.put(EventTrackerTableConstants.FAILURE_MESSAGE, failureMsg);
			failureJson.put(EventTrackerTableConstants.IS_RETRYABLE,
					exception instanceof RetryableMessageDispatchingException);
		} catch (JSONException e) {
		}

	}
}
