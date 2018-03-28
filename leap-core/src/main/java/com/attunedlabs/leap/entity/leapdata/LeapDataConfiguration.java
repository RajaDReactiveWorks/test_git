package com.attunedlabs.leap.entity.leapdata;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.SelectItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.entity.LeapEntityArchivalUtility;

/**
 * @author Reactiveworks
 *
 */
public class LeapDataConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(LeapDataConfiguration.class);

	private static JSONObject sqlRowData = new JSONObject();
	JSONArray dataArray = new JSONArray();
	static Map<String, JSONObject> metaDataMap = new HashMap<>();

	/**
	 * @param dataSet
	 * @param table
	 * @param cdc
	 * @param exchange
	 * @return
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	public JSONObject leapDataConfiguration(DataSet dataSet, Exchange exchange) throws LeapDataConfigurationException {
		try {
			logger.debug("inside leapDataConfiguration : " + dataSet);
			String entityName = (String) exchange.getIn().getHeader("entityName");

			LeapConfigData leapData = new LeapConfigData();
			JSONObject eventParamJsonObj = new JSONObject();
			// JSONArray dataArray = new JSONArray();
			// outer data Object
			JSONObject dataObj = new JSONObject();

			if (LeapEntityArchivalUtility.isSQL(exchange)) {
				eventParamJsonObj = getDataForNoSql(dataSet, entityName);
			} else {
				eventParamJsonObj = getDataForSql(dataSet, entityName);
			}
			dataObj.put(LeapDataConstants.DATA, dataArray);
			leapData.setData(dataArray);

			// reading the JSON and setting the attributes
			metaDataMap = new HashMap<>();
			generateEntity(eventParamJsonObj, 0, entityName, exchange);
			JSONArray metaData = new JSONArray();
			logger.debug("metaDataMap.get(string) : " + metaDataMap);
			Set<String> keySet = metaDataMap.keySet();
			for (String string : keySet) {
				metaData.put(metaDataMap.get(string));
			}
			
			leapData.setMetaData(metaData);
			// setting the leapResult details.
			JSONObject leapResultJson = new JSONObject();
			leapResultJson.put(LeapDataConstants.DATA, leapData.getData());
			leapResultJson.put(LeapDataConstants.METADATA, leapData.getMetaData());
			logger.debug("leapResultJson : " + leapResultJson.toString());

			// setting data in the final output JSON
			JSONObject outputJson = new JSONObject();
			outputJson.put(LeapDataConstants.LEAPRESULT, leapResultJson);
			return outputJson;
		} catch (Exception e) {
			throw new LeapDataConfigurationException(e.getMessage(), e.getCause());
		}
	}

	private static void configureNewEntityToMetaData(String entity, JSONObject newEntity) throws LeapDataConfigurationException {
		try {
			JSONObject newEntityObj = new JSONObject();
			newEntityObj.put("entityName", entity);
			newEntityObj.put("attributes", newEntity);
			metaDataMap.put(entity, newEntityObj);
			logger.debug(" metaDataMap metaDataMap : " + metaDataMap);
		} catch (JSONException e) {
			throw new LeapDataConfigurationException(e.getMessage(), e.getCause());
		}
	}

	private JSONObject getDataForSql(DataSet dataSet, String entityName) throws LeapDataConfigurationException {
		logger.debug("inside .getDataForSql() ");
		JSONArray valueArray = new JSONArray();
		JSONObject eventParamJsonObj = new JSONObject();
		dataArray = new JSONArray();
		try {
			while (dataSet.next()) {
				Row row = dataSet.getRow();
				for (SelectItem selectItem : dataSet.getSelectItems()) {
					sqlRowData.put(selectItem.getColumn().getName(), row.getValue(selectItem));
					valueArray.put(sqlRowData);
					eventParamJsonObj = sqlRowData;
				}
				JSONObject entityJson = getModifiedData(eventParamJsonObj, entityName);
				dataArray.put(entityJson);
			}
		} catch (Exception e) {
			throw new LeapDataConfigurationException(e.getMessage(), e.getCause());
		}
		return eventParamJsonObj;
	}

	private JSONObject getDataForNoSql(DataSet dataSet, String entityName) throws LeapDataConfigurationException {
		logger.debug("inside .getDataForNoSql() : " + dataSet);
		JSONArray valueArray = new JSONArray();
		JSONObject rowJSONObject = new JSONObject();
		JSONObject eventParamJsonObj = new JSONObject();
		dataArray = new JSONArray();
		try {
			while (dataSet.next()) {
				Row row = dataSet.getRow();
				logger.debug("row : " + row);
				for (SelectItem selectItem : dataSet.getSelectItems()) {
					rowJSONObject.put(selectItem.getColumn().getName(), row.getValue(selectItem));
					logger.debug("rowJSONObject : " + rowJSONObject);
					valueArray.put(rowJSONObject);
					eventParamJsonObj = new JSONObject(rowJSONObject.get("eventbody").toString());
					logger.debug("eventParamJsonObj nosql : " + eventParamJsonObj);
					JSONObject entityJson = getModifiedData(eventParamJsonObj, entityName);
					dataArray.put(entityJson);
				}
			}
		} catch (Exception e) {
			throw new LeapDataConfigurationException(e.getMessage(), e.getCause());
		}
		logger.debug("eventParamJsonObj : " + eventParamJsonObj);
		return eventParamJsonObj;
	}

	/**
	 * 
	 * @param object
	 * @return
	 */
	private static String getDataTye(Object object) {
		if (object instanceof java.lang.String) {
			return LeapDataConstants.STRING_DATATYPE;
		} else if (object instanceof java.lang.Integer) {
			return LeapDataConstants.INTEGER_DATATYPE;
		} else if (object instanceof java.lang.Boolean) {
			return LeapDataConstants.BOOLEAN_DATATYPE;
		} else if (object instanceof java.lang.Double) {
			return LeapDataConstants.DOUBLE_DATATYPE;
		} else if (object instanceof org.json.JSONObject) {
			return LeapDataConstants.OBJECT_DATATYPE;
		} else
			return LeapDataConstants.OBJECT_DATATYPE;
	}

	/**
	 * @param object
	 * @return
	 * @throws LeapDataConfigurationException
	 */
	private static int getByteSize(Object object) throws LeapDataConfigurationException {
		byte[] utf8Bytes;
		try {
			utf8Bytes = object.toString().getBytes("UTF-8");
			return utf8Bytes.length;
		} catch (UnsupportedEncodingException e) {
			throw new LeapDataConfigurationException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * @param entity
	 * @param keyObj
	 * @param key
	 * @param level
	 * @param exchange
	 * @return
	 * @throws LeapDataConfigurationException
	 */
	private static JSONObject getJSONObject(String entity, Object keyObj, String key, int level, Exchange exchange)
			throws LeapDataConfigurationException {
		try {
			String featureGroup = (String) exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY);
			String featureName = (String) exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY);
			JSONObject object = new JSONObject();
			object.put(LeapDataConstants.BYTE_SIZE, getByteSize(keyObj));
			object.put(LeapDataConstants.I18N_LANG_TEXT, featureGroup + "-" + featureName + "-" + key);
			object.put(LeapDataConstants.I18N_ID, key.toUpperCase());
			object.put(LeapDataConstants.LEVEL, level);
			object.put(LeapDataConstants.NAME, key);
			object.put(LeapDataConstants.TYPE, getDataTye(keyObj));
			object.put(LeapDataConstants.IS_ENTITY, getIsEntityDataTye(keyObj));

			return object;
		} catch (JSONException e) {
			throw new LeapDataConfigurationException(e.getMessage(), e.getCause());
		}
	}

	private static int getIsEntityDataTye(Object object) {
		if (object instanceof java.lang.String || object instanceof java.lang.Character) {
			return 0;
		} else if (object instanceof java.lang.Boolean) {
			return 0;
		} else if (object instanceof java.lang.Number) {
			return 0;
		} else
			return 1;
	}

	private static int getIntegerDataTye(Object object) {
		if (object instanceof java.lang.String || object instanceof java.lang.Character) {
			return 1;
		} else if (object instanceof java.lang.Boolean) {
			return 1;
		} else if (object instanceof java.lang.Number) {
			return 1;
		} else
			return 0;
	}

	/**
	 * @param jsonArray
	 * @param level
	 * @param entity
	 * @return
	 * @throws LeapDataConfigurationException
	 */
	private static void generateEntity(JSONObject jsonObject, int level, String entity, Exchange exchange)
			throws LeapDataConfigurationException {

		try {
			JSONObject newEntity = new JSONObject();
			Iterator<?> keys = jsonObject.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				Object keyObj = jsonObject.get(key);
				JSONObject fieldJSON = getJSONObject(entity, keyObj, key, level, exchange);
				newEntity.put(key, fieldJSON);
				if (keyObj instanceof JSONObject) {
					generateEntity(new JSONObject(jsonObject.getJSONObject(key).toString()), ++level, key, exchange);
					--level;
				}
				if (keyObj instanceof JSONArray)
					generateEntity(new JSONArray(jsonObject.getJSONArray(key).toString()), level, key, exchange);
			}
			logger.debug("generateEntity end of Obj");
			configureNewEntityToMetaData(entity, newEntity);

		} catch (JSONException e) {
			throw new LeapDataConfigurationException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * @param jsonObject
	 * @param level
	 * @param entity
	 * @return
	 * @throws LeapDataConfigurationException
	 */

	private static void generateEntity(JSONArray jsonArray, int level, String entity, Exchange exchange)
			throws LeapDataConfigurationException {
		try {
			for (int idx = 0; idx < jsonArray.length(); idx++) {
				Object keyObj = jsonArray.get(idx);
				if (keyObj instanceof JSONObject) {
					generateEntity(new JSONObject(((JSONObject) keyObj).toString()), ++level, entity, exchange);
					--level;
				}
				if (keyObj instanceof JSONArray)
					generateEntity(new JSONArray(((JSONArray) keyObj).toString()), level, entity, exchange);
			}
		} catch (JSONException e) {
			throw new LeapDataConfigurationException(e.getMessage(), e.getCause());
		}
	}

	public static JSONObject getModifiedData(JSONObject eventParamJsonObj, String entity) throws JSONException {

		JSONArray elementArray = new JSONArray();
		JSONArray elementsArray = getDataJson(eventParamJsonObj, entity, elementArray);

		// Individual entity in data array
		JSONObject entityJson = new JSONObject();
		entityJson.put("entityName", entity);
		entityJson.put("elements", elementsArray);
		return entityJson;
	}

	private static JSONArray getDataJson(JSONObject jsonInputObject, String entity, JSONArray elementsArray)
			throws JSONException {
		Iterator<?> keys = jsonInputObject.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object keyObj = jsonInputObject.get(key);
			int dataTye = getIntegerDataTye(keyObj);
			if (dataTye == 1) {
				JSONObject newElement = new JSONObject();
				newElement.put("primitive", dataTye);
				newElement.put("name", key);
				newElement.put("value", keyObj);
				elementsArray.put(newElement);
			} else {
				JSONObject newElement = new JSONObject();
				newElement.put("primitive", dataTye);
				newElement.put("name", key);
				JSONArray elementArray = new JSONArray();
				JSONArray array;
				if (keyObj instanceof JSONObject)
					array = getDataJson(new JSONObject(jsonInputObject.getJSONObject(key).toString()), key,
							elementArray);
				else if (keyObj instanceof JSONArray)
					array = getDataJson(new JSONArray(((JSONArray) keyObj).toString()), key, elementArray);
				else
					array = null;
				JSONObject dataObj = new JSONObject();
				// data Array Obj
				JSONArray dataArray = new JSONArray();
				// Individual entity in data array
				JSONObject entityJson = new JSONObject();
				entityJson.put("entityName", key);
				entityJson.put("elements", array);
				dataArray.put(entityJson);
				dataObj.put("data", dataArray);
				newElement.put("value", dataObj);
				elementsArray.put(newElement);
			}
		}
		return elementsArray;
	}

	private static JSONArray getDataJson(JSONArray jsonArray, String entity, JSONArray elementsArray)
			throws JSONException {
		for (int idx = 0; idx < jsonArray.length(); idx++) {
			Object keyObj = jsonArray.get(idx);
			if (keyObj instanceof JSONObject) {
				getDataJson(new JSONObject(((JSONObject) keyObj).toString()), entity, elementsArray);
			} else if (keyObj instanceof JSONArray) {
				getDataJson(new JSONArray(((JSONArray) keyObj).toString()), entity, elementsArray);
			}
		}
		return elementsArray;
	}

}
