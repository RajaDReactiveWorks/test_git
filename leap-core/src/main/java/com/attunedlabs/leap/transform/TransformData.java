package com.attunedlabs.leap.transform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.generic.LeapGenericConstant;
import com.attunedlabs.leap.util.LeapConfigurationUtil;

public class TransformData {
	final Logger logger = LoggerFactory.getLogger(TransformData.class);

	/**
	 * Custom bean to marshal the XML string to org.Json Object
	 * 
	 * @param exchange
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	public void marshalXmltoJson(Exchange exchange) throws JSONException, RequestDataTransformationException {
		try {
			String exchngeContentType = (String) exchange.getIn().getHeader(Exchange.CONTENT_TYPE);
			if (exchngeContentType != null
					&& exchngeContentType.equalsIgnoreCase(LeapGenericConstant.HTTP_CONTENT_TYPE_ENCODED_KEY)) {
				// request is coming for service request of SAC as encoded
				// value
				processRequestForEncodedData(exchange);

			} else {
				// request is for other service
				String bodyIn = exchange.getIn().getBody(String.class);
				// if body is null then check in custom header
				if (bodyIn != null) {
					createJSONRequestFromXMLString(bodyIn, exchange);
				} else {
					throw new RequestDataTransformationException(
							"Unable to transform exchange body as no data availble in exchange body for service : "
									+ exchange.getIn().getHeader(LeapHeaderConstant.SERVICETYPE_KEY) + ", feature : "
									+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY)
									+ " and feature group : "
									+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY));
				}
			}
		} catch (RequestDataTransformationException e) {
			LeapConfigurationUtil.setResponseCode(503, exchange, e.getMessage());
			throw new RequestDataTransformationException(
					"Unable to transform exchange body as no data availble in exchange body for service : "
							+ exchange.getIn().getHeader(LeapHeaderConstant.SERVICETYPE_KEY) + ", feature : "
							+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY) + " and feature group : "
							+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY));
		}
	}// end of method marshalXmltoJson

	/**
	 * Custom bean to unmarshal the JSON string to XML
	 * 
	 * @param exchange
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	public void unmarshalJsonToXML(Exchange exchange) throws JSONException, RequestDataTransformationException {
		String bodyIn = exchange.getIn().getBody(String.class);
		if (bodyIn != null) {
			org.json.JSONObject jsonObject = new org.json.JSONObject(bodyIn);
			if (jsonObject.has(LeapHeaderConstant.DATA_KEY)) {
				JSONArray jsonArray = (JSONArray) jsonObject.get(LeapHeaderConstant.DATA_KEY);
				org.json.JSONObject jobj = (org.json.JSONObject) jsonArray.get(0);
				String xmlString = XML.toString(jobj);
				logger.debug("unmarshalled - jsonObject to xml: " + xmlString);
				exchange.getIn().setBody(xmlString);
			} else {
				String xmlString = XML.toString(jsonObject);
				logger.debug("unmarshalled - jsonObject to xml: " + xmlString);
				exchange.getIn().setBody(xmlString);
			}
		} else {
			LeapConfigurationUtil.setResponseCode(503, exchange,
					"Unable to transform exchange body as no data availble in exchange body for service : "
							+ exchange.getIn().getHeader(LeapHeaderConstant.SERVICETYPE_KEY) + ", feature : "
							+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY) + " and feature group : "
							+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY));
			throw new RequestDataTransformationException(
					"Unable to transform exchange body as no data availble in exchange body for service : "
							+ exchange.getIn().getHeader(LeapHeaderConstant.SERVICETYPE_KEY) + ", feature : "
							+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY) + " and feature group : "
							+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY));
		}
	}

	public String transformRequestData(Exchange exchange) throws JSONException, RequestDataTransformationException {
		logger.debug(".transformRequestData of Transform data");
		String jsonstring = null;
		try {
			String body = exchange.getIn().getBody(String.class);
			if (body != null && !(body.isEmpty())) {
				jsonstring = convertBodyToJSONString(body, exchange);
			} else {
				throw new RequestDataTransformationException(
						"Unable to transform request data body as no data availble in exchange body for service : "
								+ exchange.getIn().getHeader(LeapHeaderConstant.SERVICETYPE_KEY) + ", feature : "
								+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY) + " and feature group : "
								+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY));
			}
		} catch (ParseException | RequestDataTransformationException e) {
			LeapConfigurationUtil.setResponseCode(503, exchange, e.getMessage());
			throw new RequestDataTransformationException(
					"Unable to transform request data body as no data availble in exchange body for service : "
							+ exchange.getIn().getHeader(LeapHeaderConstant.SERVICETYPE_KEY) + ", feature : "
							+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY) + " and feature group : "
							+ exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY));
		}

		return jsonstring;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private String convertBodyToJSONString(String body, Exchange exh) throws ParseException {
		JSONObject data = null;
		JSONObject jsonobj2 = null;
		logger.debug("body : " + body);
		JSONParser parser = new JSONParser();
		JSONObject jobj = (JSONObject) parser.parse(body);
		logger.debug("json onj : " + jobj);
		logger.debug("jobj keys : " + jobj.keySet());
		// logger.info("staring : "+jobj.toJSONString());
		Iterator itr = jobj.keySet().iterator();
		String key = null;
		while (itr.hasNext()) {
			logger.debug("itr has elemt");
			key = (String) itr.next();
			logger.debug("key : " + key);
			data = (JSONObject) jobj.get(key);
			logger.debug("key : " + key + ", data : " + data);

		}
		if (data != null) {
			logger.debug("data is not null : " + data);

			JSONArray jsonArr = new JSONArray();
			jsonArr.put(data);
			logger.debug("json array : " + jsonArr.toString());
			jsonobj2 = new JSONObject();
			jsonobj2.put(LeapHeaderConstant.DATA_KEY, jsonArr);
			logger.debug("json obj : " + jsonobj2.toString());
		} else {
			return null;
		}
		exh.getIn().setHeader(LeapHeaderConstant.SERVICETYPE_KEY, key.toLowerCase().trim());
		return jsonobj2.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public String transformRestRequestData(Exchange exchange) throws RequestDataTransformationException, JSONException {
		String body = null;
		String httpMethod = null;
		try {
			httpMethod = (String) exchange.getIn().getHeader(LeapHeaderConstant.CAMEL_HTTP_METHOD);
			logger.debug("httpMethod : " + httpMethod);
			if (httpMethod.equalsIgnoreCase("GET")) {
				String httpQuery = (String) exchange.getIn().getHeader("CamelHttpQuery");
				if (httpQuery != null) {
					Map<String, Object> mapdata = new HashMap<>();
					logger.debug("http query data : " + httpQuery);
					String[] splitQueryString = httpQuery.split("&");
					int noOfqueryEle = splitQueryString.length;
					for (int i = 0; i <= noOfqueryEle - 1; i++) {
						String[] splitQueryString1 = splitQueryString[i].split("=");
						mapdata.put(splitQueryString1[0].trim(), splitQueryString1[1].trim());
					}
					logger.debug("map data : " + mapdata);

					org.json.JSONObject jobj = new org.json.JSONObject(mapdata.toString());
					logger.debug("jobj : " + jobj.toString());
					JSONArray jsonArr = new JSONArray();
					jsonArr.put(jobj);
					JSONObject jobj1 = new JSONObject();
					jobj1.put(LeapHeaderConstant.DATA_KEY, jsonArr);
					logger.debug("jobj1 : " + jobj1.toJSONString());
					logger.debug("feature group : " + exchange.getIn().getHeader("featuregroup") + ", feature : "
							+ exchange.getIn().getHeader("feature") + ", service : "
							+ exchange.getIn().getHeader("servicetype"));
					return jobj1.toJSONString();
				}
			} else if (httpMethod.equalsIgnoreCase(LeapHeaderConstant.POST_KEY)) {
				body = exchange.getIn().getBody(String.class);
				logger.debug("body for post request : " + body);
				logger.debug("feature group : " + exchange.getIn().getHeader("featuregroup") + ", feature : "
						+ exchange.getIn().getHeader("feature") + ", service : "
						+ exchange.getIn().getHeader("servicetype"));
			} else {
				logger.debug("UNSUPPORTED http method");
				throw new RequestDataTransformationException("UNSUPPORTED http method " + httpMethod);
			}
		} catch (JSONException | RequestDataTransformationException e) {
			LeapConfigurationUtil.setResponseCode(503, exchange, e.getMessage());
			throw new RequestDataTransformationException("UNSUPPORTED http method " + httpMethod);
		}
		return body;
	}

	/**
	 * This method is used to see if encoded request data available in body or
	 * custom header and convert into required json data.
	 * 
	 * @param exchange
	 *            : Camel Exchange Object
	 * @throws RequestDataTransformationException
	 */
	private void processRequestForEncodedData(Exchange exchange) throws RequestDataTransformationException {
		String bodyIn = exchange.getIn().getBody(String.class);
		// if body is null then check in custom header
		if (bodyIn != null) {
			createJSONRequestFromXMLString(bodyIn, exchange);
		} else {
			String headerDataValue = (String) exchange.getIn().getHeader(LeapGenericConstant.ENCODED_REQUEST_DATA_KEY);
			logger.debug(" message value in header : " + headerDataValue);
			createJSONRequestFromXMLString(headerDataValue, exchange);

		}
	}// end of method processRequestForEncodedData

	/**
	 * This method is used to convert xml request data into JSON format
	 * 
	 * @param xmlRequest
	 *            : request XML string
	 * @param exchange
	 *            : Camel Exchnage Object
	 * @throws RequestDataTransformationException
	 */
	private void createJSONRequestFromXMLString(String xmlRequest, Exchange exchange)
			throws RequestDataTransformationException {
		logger.debug(".createJSONRequestFromXMLString method of TransformData");
		String serviceType = (String) exchange.getIn().getHeader(LeapHeaderConstant.SERVICETYPE_KEY);
		logger.debug("serviceType : " + serviceType);
		org.json.JSONObject jsonObject;
		//skipping for exposing pipeline as service.
		if (serviceType.equalsIgnoreCase(PipelineServiceConstant.EXECUTE_PIPELINE)) {
			logger.debug("skipping the XML to Json for executing the Pipeline");
		} else {
			try {
				jsonObject = XML.toJSONObject(xmlRequest);
				logger.debug("marshalled - jsonObject from body: " + jsonObject.toString());
				if (jsonObject.has(LeapHeaderConstant.DATA_KEY)) {
					JSONArray jsonArray = new JSONArray();
					jsonArray.put(jsonObject);
					org.json.JSONObject reqJsonObject = new org.json.JSONObject();
					reqJsonObject.put(LeapHeaderConstant.DATA_KEY, jsonArray);
					exchange.getIn().setBody(reqJsonObject.toString());
				} else
					exchange.getIn().setBody(jsonObject);

			} catch (JSONException e) {
				throw new RequestDataTransformationException(
						"Unable to transform xml data " + xmlRequest + " into json ", e);
			}
		}

	}// end of method createJSONRequestFromXMLString

}
