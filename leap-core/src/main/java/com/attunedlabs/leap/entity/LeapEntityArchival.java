package com.attunedlabs.leap.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapHeaderConstant;

public class LeapEntityArchival {

	static Logger logger = LoggerFactory.getLogger(LeapEntityArchival.class);

	LeapEntityArchivalDAO archivalDAO = new LeapEntityArchivalDAO();
	LeapEntityArchivalUtility utility = new LeapEntityArchivalUtility();

	public void entryMethod(Exchange exchange) throws Exception {
		String incomingUrl = exchange.getIn().getHeader(Exchange.HTTP_URI).toString();
		String entity = incomingUrl.substring(incomingUrl.indexOf("data/"), incomingUrl.lastIndexOf("/"));
		String[] entityName = entity.split("/");
		exchange.getIn().setHeader("entityName", entityName[1]);

		// accessMethod(entryMethod, exchange);
	}

	public static boolean checkForXmlContent(String body) {
		if (body.matches(PipelineServiceConstant.XML_PATTERN)) {
			return true;
		}
		return false;
	}// end of the method.

	public static String convertToJson(String body) throws LeapEntityArchivalException {
		JSONObject xmlJSONObj;
		try {
			xmlJSONObj = XML.toJSONObject(body);
			String pipelineData = xmlJSONObj.toString(4);
			return pipelineData;
		} catch (JSONException e) {
			throw new LeapEntityArchivalException(e.getMessage());
		}
	}// end of the method.

	public void getArchivedDataFromAccessMethod(Exchange exchange) throws LeapEntityArchivalDAOException {
		try {
			archivalDAO.getArchivedOrder(exchange);
		} catch (Exception e) {
			throw new LeapEntityArchivalDAOException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * 
	 * @param exchange
	 * @throws LeapEntityArchivalException
	 */
	public void fetchData(Exchange exchange) throws LeapEntityArchivalException {
		String exchangeBody = exchange.getIn().getBody(String.class);
		logger.debug("exchangeBody : " + exchangeBody);
		JSONObject exchangeJSON = null;
		JSONObject jobj = null;
		JSONArray jsonArray = null;
		List<String> list = new ArrayList<>();
		String name = null, tagkey = null;
		try {
			exchangeJSON = new JSONObject(exchangeBody);
			if (exchangeJSON.has(LeapHeaderConstant.DATA_KEY)) {
				jsonArray = (JSONArray) exchangeJSON.get(LeapHeaderConstant.DATA_KEY);
				jobj = (JSONObject) jsonArray.get(0);
				logger.debug("jobj : " + jobj);
			} else {
				jobj = exchangeJSON;
			}
			Iterator<?> keys = jobj.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				logger.debug("key : " + key);
				list = utility.getAttribute(key);
				name = list.get(1);
				tagkey = name + ":" + jobj.getString(name);
				logger.debug("tagkey : " + tagkey);
			}

			if (list.get(0).equals("R")) {
				archivalDAO.getArchivedOrder(tagkey, exchange);
			} else {
//				archivalDAO.getOrderDetails(name, jobj.getString(name), exchange);
			}
		} catch (Exception e) {
			throw new LeapEntityArchivalException(e.getMessage(), e.getCause());
		}
	}

}
