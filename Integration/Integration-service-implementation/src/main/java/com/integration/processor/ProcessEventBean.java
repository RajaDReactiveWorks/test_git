package com.integration.processor;

import static com.integration.processor.IntegrationConstants.AGGREGATION_OR_ENRICHMENT;
import static com.integration.processor.IntegrationConstants.AGGREGATION_REQUIRED;
import static com.integration.processor.IntegrationConstants.AGG_ENRICH_TRANSFORM_EVENT;
import static com.integration.processor.IntegrationConstants.APPLY;
import static com.integration.processor.IntegrationConstants.CORRELATION_ID;
import static com.integration.processor.IntegrationConstants.CSV_HEADER;
import static com.integration.processor.IntegrationConstants.CSV_HEADER_CONTAINS;
import static com.integration.processor.IntegrationConstants.DONE;
import static com.integration.processor.IntegrationConstants.END_POINT;
import static com.integration.processor.IntegrationConstants.END_POINT_URL;
import static com.integration.processor.IntegrationConstants.ENTITY;
import static com.integration.processor.IntegrationConstants.ENTITY_TYPE_STRATEGY;
import static com.integration.processor.IntegrationConstants.EVENT_HEADER;
import static com.integration.processor.IntegrationConstants.EVENT_ID;
import static com.integration.processor.IntegrationConstants.EVENT_PARAM;
import static com.integration.processor.IntegrationConstants.FAILED;
import static com.integration.processor.IntegrationConstants.FILENAMEPATTERN;
import static com.integration.processor.IntegrationConstants.FILE_CONTENT_SPLITING;
import static com.integration.processor.IntegrationConstants.FILE_NAME;
import static com.integration.processor.IntegrationConstants.FILE_NAME_REGEX;
import static com.integration.processor.IntegrationConstants.FILE_TYPE;
import static com.integration.processor.IntegrationConstants.IMPL_ROUTE;
import static com.integration.processor.IntegrationConstants.IN_PROGRESS;
import static com.integration.processor.IntegrationConstants.JSON_ARRAY_KEY;
import static com.integration.processor.IntegrationConstants.JSON_KEY;
import static com.integration.processor.IntegrationConstants.JSON_PATH;
import static com.integration.processor.IntegrationConstants.JSON_ROOT_KEY;
import static com.integration.processor.IntegrationConstants.LOCAL_PATH;
import static com.integration.processor.IntegrationConstants.PROCESSING_QUEUE_EVENT;
import static com.integration.processor.IntegrationConstants.SEQUENCE_ID;
import static com.integration.processor.IntegrationConstants.SITE_ID;
import static com.integration.processor.IntegrationConstants.SOURCE;
import static com.integration.processor.IntegrationConstants.SOURCE_FOLDER;
import static com.integration.processor.IntegrationConstants.SPLITING_REQUIRED;
import static com.integration.processor.IntegrationConstants.SUCCESS;
import static com.integration.processor.IntegrationConstants.TENANT_ID;
import static com.integration.processor.IntegrationConstants.TRANSFORMATION;
import static com.integration.processor.IntegrationConstants.TRANSFORMATION_XSLT;
import static com.integration.processor.IntegrationConstants.VALIDATION;
import static com.integration.processor.IntegrationConstants.VALIDATION_REQUIRED;
import static com.integration.processor.IntegrationConstants.VALIDATION_XSD;
import static com.integration.processor.IntegrationConstants.XML_HEADER;
import static com.integration.processor.IntegrationConstants.XML_ROOT;
import static com.integration.processor.IntegrationConstants.XML_ROOT_TAG;
import static com.integration.processor.IntegrationConstants.X_PATH;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.attunedlabs.eventframework.abstractbean.AbstractLeapCamelBean;
import com.datastax.driver.core.utils.UUIDs;
import com.integration.exception.CSVToXMLConversionException;
import com.integration.exception.EventTransformationException;
import com.integration.exception.FailedToMoveFileException;
import com.integration.exception.JSONFileFormatException;
import com.integration.exception.NoStrategyDefinedException;
import com.integration.exception.PermaStoreDoesNotExistException;
import com.integration.exception.UnableToDoTransformationExeption;
import com.integration.exception.UnableToGetEntityTypeException;
import com.integration.exception.UnableToGetPermaStoreException;
import com.integration.exception.UnableToParseException;
import com.integration.exception.UnableToProccessEventException;
import com.integration.exception.UnableToReadFileFromLocalException;
import com.integration.exception.UnableToReadPropertiesFileException;
import com.integration.exception.UnableToSplitFileExeption;
import com.integration.exception.ValidationFailedExeption;
import com.integration.exception.XMLTOSTringConversionException;

public class ProcessEventBean {

	
	private final static Logger logger = LoggerFactory.getLogger(ProcessEventBean.class.getName());
	private final IntegrationLogger dblogger;

	ProcessEventBean() throws UnableToReadPropertiesFileException {
		dblogger = new IntegrationLogger();
	}

	/**
	 * The stage -2 event processing task starts with this method
	 * 
	 * @param exchange
	 * @throws FailedToMoveFileException
	 * @throws UnableToProccessEventException
	 * @throws Exception
	 */
	public void processUnprocessedReq(Exchange exchange)
			throws FailedToMoveFileException, UnableToProccessEventException {
		logger.debug("inside processUnprocessedReq () method");
		String startTime = LocalDateTime.now().toString();
		exchange.getIn().setHeader("startTime", startTime);
		Message message = exchange.getIn();
		String eventBody = message.getBody(String.class);
		String fileContent = null;
		String fileType = null;
		String fileName = null;
		String sourceFolder = null;
		String eventId = null;
		String endpoint = null;
		String tenantId = null;
		String siteId = null;
		try {
			JSONObject jsonEvent = new JSONObject(eventBody);
			eventId = jsonEvent.getString(EVENT_ID);
			JSONObject eventHeader = jsonEvent.getJSONObject(EVENT_HEADER);
			tenantId = eventHeader.getString(TENANT_ID);
			siteId = eventHeader.getString(SITE_ID);
			JSONObject eventParamJson = jsonEvent.getJSONObject(EVENT_PARAM);
			JSONObject sourceJson = eventParamJson.getJSONObject(SOURCE);
			endpoint = sourceJson.getString(END_POINT);
			fileType = sourceJson.getString(FILE_TYPE);
			fileName = sourceJson.getString(FILE_NAME);
			sourceFolder = sourceJson.getString(SOURCE_FOLDER);
			dblogger.updateFiletrackingStatus(eventId, IN_PROGRESS, null);
			JSONObject entityStratagyPerma = IntegrationPermaStoreUtil.getPermaStore(exchange, ENTITY_TYPE_STRATEGY);
			fileContent = IntegrationUtil.readFileFromLocal(fileName, fileType, tenantId, siteId);
			Document doc = null;
			JSONObject strategyJson = null;
			if (fileType.equalsIgnoreCase("json")) {
				if (fileContent.trim().startsWith("{")) {
					exchange.getIn().setHeader("jsonType", "jsonObject");
					JSONObject eventJson = new JSONObject(fileContent);
					strategyJson = getStrategyToApplyForJSON(exchange, entityStratagyPerma, fileName, eventJson);
					String entityType = (String) exchange.getIn().getHeader("entityType");
					doc = IntegrationUtil.getXmlDocumentFromJSONObject(eventJson, entityType);
					doXmlSplitValidationAndPublish(exchange, eventId, strategyJson, fileType, doc, sourceJson,
							JSON_PATH, tenantId, siteId);
				}
				if (fileContent.trim().startsWith("[")) {
					exchange.getIn().setHeader("jsonType", "jsonArray");
					JSONArray eventArray = new JSONArray(fileContent);
					strategyJson = getStrategyToApplyForJSONArray(exchange, entityStratagyPerma, fileName, eventArray);
					String entityType = (String) exchange.getIn().getHeader("entityType");
					logger.debug("entity type is :"+entityType);
					doc = IntegrationUtil.getXmlDocumentFromJSONArray(eventArray, entityType);
					doXmlSplitValidationAndPublish(exchange, eventId, strategyJson, fileType, doc, sourceJson,
							JSON_PATH, tenantId, siteId);
				}
				
			} else if (fileType.equalsIgnoreCase("xml")) {
				String lineSeperator=System.getProperty("line.separator");
				logger.debug("line seprerator is :"+lineSeperator);
				logger.debug("before removing line seperator :"+fileContent);
				fileContent=fileContent.replace("\r\n", "");
				logger.debug("after removing line seperator :"+fileContent);
				doc = IntegrationUtil.getXmlDocumentFromString(fileContent);
				strategyJson = getStrategyToApplyForXml(exchange, entityStratagyPerma, fileName, doc);
				doXmlSplitValidationAndPublish(exchange, eventId, strategyJson, fileType, doc, sourceJson, X_PATH,
						tenantId, siteId);
			} else if (fileType.equalsIgnoreCase("csv")) {
				strategyJson = getStategyToApplyForCSV(exchange, entityStratagyPerma, fileName, fileContent);
				doCSVSplitValidationAndPublish(exchange, eventId, strategyJson, fileType, fileContent, sourceJson,
						tenantId, siteId);
			}
			if (strategyJson == null) {
				throw new NoStrategyDefinedException("there is no strategy defined");
			}

		} catch (JSONException | UnableToGetEntityTypeException | NoStrategyDefinedException | UnableToParseException
				| UnableToGetPermaStoreException | PermaStoreDoesNotExistException | UnableToReadFileFromLocalException
				| UnableToSplitFileExeption | CSVToXMLConversionException | XMLTOSTringConversionException
				| JSONFileFormatException | SAXException e) {
			moveFileToError(exchange, eventId, sourceFolder, fileName, fileType, endpoint, e.getMessage(), tenantId,
					siteId);
			throw new UnableToProccessEventException("unable to process the event : " + e.getMessage(), e);
		}
	}

	/**
	 * if we are unable to find the startegy , then we should move the file to
	 * error folder
	 * 
	 * @param exchange
	 * @param eventId
	 * @param sourceFolder
	 * @param fileName
	 * @param fileType
	 * @param endpoint
	 * @param message
	 * @param tenantId
	 * @param siteId
	 * @throws FailedToMoveFileException
	 */
	private void moveFileToError(Exchange exchange, String eventId, String sourceFolder, String fileName,
			String fileType, String endpoint, String message, String tenantId, String siteId)
			throws FailedToMoveFileException {
		logger.debug("inside moveFileToError () method");
		String sourcePath = LOCAL_PATH + "\\" + sourceFolder + "\\" + fileName;
		String destPath = LOCAL_PATH + "\\" + tenantId + "\\" + siteId + "\\error\\" + fileName;
		try {
			Path temp = Files.move(Paths.get(sourcePath), Paths.get(destPath));
			if (temp == null) {
				throw new FailedToMoveFileException("failed to move the file : " + fileName);
			}
			dblogger.updateFiletrackingStatus(eventId, FAILED, message);
		} catch (IOException e) {
			throw new FailedToMoveFileException("failed to move the file : " + fileName + " : " + e.getMessage(), e);
		}

	}

	/**
	 * The stage-3 agg or entich and transform starts with this method.
	 * 
	 * @param exchange
	 * @throws EventTransformationException
	 * @throws UnableToProccessEventException
	 */
	public void aggOrEnrichmentOrTransformReq(Exchange exchange)
			throws EventTransformationException, UnableToProccessEventException {
		logger.debug("inside aggOrEnrichmentOrTransformReq () method");
		String startTime = LocalDateTime.now().toString();
		exchange.getIn().setHeader("startTime", startTime);
		Message message = exchange.getIn();
		String kafkaEvent = message.getBody(String.class);
		logger.debug("event from kafka is : " + kafkaEvent);
		try {
			JSONObject json = new JSONObject(kafkaEvent);
			JSONObject eventHeaderJson = json.getJSONObject(EVENT_HEADER);
			String configKey = eventHeaderJson.getString("configKey");
			JSONObject permaJson = IntegrationPermaStoreUtil.getPermaStore(exchange, ENTITY_TYPE_STRATEGY);
			JSONObject strategyJson = permaJson.getJSONObject(configKey);
			logger.debug("configkey is :" + configKey);
			logger.debug("strategyJson is :" + strategyJson);
			JSONObject aggorEnrJson = strategyJson.getJSONObject(APPLY).getJSONObject(AGGREGATION_OR_ENRICHMENT);
			boolean isRequired = aggorEnrJson.getBoolean(AGGREGATION_REQUIRED);
			exchange.getIn().setHeader("isAggOrEnrichReq", isRequired);
			if (isRequired) {
				String implRoute = aggorEnrJson.getString(IMPL_ROUTE);
				exchange.getIn().setHeader("implRoute", implRoute);
			}
		} catch (JSONException | UnableToGetPermaStoreException | PermaStoreDoesNotExistException e) {
			throw new UnableToProccessEventException("unable to do agg | enrich | transform : " + e.getMessage(), e);
		}
	}

	/**
	 * transforms an xml using xslt. After transform publish it to ProcessQueue
	 * 
	 * @param exchange
	 * @return JSONObject
	 * @throws Exception
	 */
	public JSONObject doTransformation(Exchange exchange) throws Exception {
		logger.debug("inside the doTransformation(..) util method");
		String eventId = null;
		String correlationId = null;
		String entityType = null;
		String fileContent = null;
		String endPoint = null;
		String fileType = null;
		String fileName = null;
		String sourceFolder = null;
		String http_url = null;
		String tenantId = null;
		String siteId = null;
		String sequenceId = null;
		try {
			Message message = exchange.getIn();
			String eventBody = message.getBody(String.class);
			JSONObject jsonFromKafka = new JSONObject(eventBody);
			eventId = jsonFromKafka.getString(EVENT_ID);
			JSONObject eventParamJson = jsonFromKafka.getJSONObject(EVENT_PARAM);
			JSONObject sourceJSon = eventParamJson.getJSONObject(SOURCE);
			endPoint = sourceJSon.getString(END_POINT);
			fileType = sourceJSon.getString(FILE_TYPE);
			entityType = sourceJSon.getString("entityType");
			fileName = sourceJSon.getString(FILE_NAME);
			sourceFolder = sourceJSon.getString(SOURCE_FOLDER);
			http_url = sourceJSon.getString(END_POINT_URL);
			String content = sourceJSon.getString("content");
			JSONObject eventHeaderJson = jsonFromKafka.getJSONObject(EVENT_HEADER);
			sequenceId = eventHeaderJson.getString(SEQUENCE_ID);
			correlationId = eventHeaderJson.getString(CORRELATION_ID);
			tenantId = eventHeaderJson.getString(TENANT_ID);
			siteId = eventHeaderJson.getString(SITE_ID);
			String configKey = eventHeaderJson.getString("configKey");
			JSONObject permaJson = IntegrationPermaStoreUtil.getPermaStore(exchange, ENTITY_TYPE_STRATEGY);

			JSONObject strategyJson = permaJson.getJSONObject(configKey);
			JSONObject transformJson = strategyJson.getJSONObject(APPLY).getJSONObject(TRANSFORMATION);
			String xsltPath = transformJson.getString(TRANSFORMATION_XSLT);
			xsltPath = IntegrationConstants.LOCAL_PATH + "//" + xsltPath;
			logger.debug("xml content before transform is : " + content + " loaltion is :" + xsltPath);

			String transformXml = transformEventBody(content, xsltPath);
			sourceJSon.put("content", transformXml);

			dblogger.logInIntegrationFlowTracking(eventId, correlationId, Integer.valueOf(sequenceId) + 1,
					"kafka-ProcessingQueue", LocalDateTime.now().toString(), entityType, "kafka-subscriber", null,
					transformXml, fileType, DONE, 0, siteId, "kafka-aggOrEnrichmentOrTransformQueue",
					(String)exchange.getIn().getHeader("startTime"), SUCCESS, tenantId);
			exchange.getIn().setHeader("event-Id", PROCESSING_QUEUE_EVENT);
			logger.debug("kafka request is : " + jsonFromKafka.toString());
			AbstractLeapCamelBean publisher = new KafkaPublisher();
			exchange.getIn().setBody(jsonFromKafka);
			publisher.invokeBean(exchange);
			return jsonFromKafka;
		} catch (JSONException | UnableToGetPermaStoreException | PermaStoreDoesNotExistException
				| EventTransformationException e) {
			dblogger.updateFiletrackingStatus(correlationId, FAILED, e.getMessage());
			dblogger.logInIntegrationFlowTracking(eventId, correlationId, Integer.valueOf(sequenceId) + 1,
					"kafka-proccess", LocalDateTime.now().toString(), entityType, "kafka-subscriber", e.getMessage(),
					fileContent, fileType, null, 0, siteId, "kafka-aggEnrich", (String)exchange.getIn().getHeader("startTime"), FAILED,
					tenantId);
			throw new UnableToDoTransformationExeption("unable to do the event transformation : " + e.getMessage(), e);
		}
	}

	/**
	 * tranforms an xml using xslt
	 * 
	 * @param content
	 * @param xsltPath
	 * @return String
	 * @throws EventTransformationException
	 */
	private String transformEventBody(String content, String xsltPath) throws EventTransformationException {
		logger.debug("inside the transformEventBody(..)  method");

		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document document = docBuilder.parse(new InputSource(new StringReader(content)));
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transForm = factory.newTransformer(new StreamSource(xsltPath));
			DOMSource domSource = new DOMSource(document);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(output);
			transForm.transform(domSource, result);
			return result.getOutputStream().toString();
		} catch (TransformerException | ParserConfigurationException | SAXException | IOException e) {
			throw new EventTransformationException("unbale to transform event : " + e.getMessage(), e);
		}
	}// ..end of the transformEventBody(..) method

	/**
	 * create a json object new to be published to kafka
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param correlationId
	 * @param integrationeventId
	 * @param sourceJson
	 * @param entityType
	 * @param content
	 * @param configKey
	 * @return JSONObject
	 * @throws JSONException
	 */
	private JSONObject createJsonToPublish(String tenantId, String siteId, String correlationId,
			String integrationeventId, JSONObject sourceJson, String entityType, String content, String configKey)
			throws JSONException {
		logger.debug("inside createJsonToPublish () method");

		JSONObject eventHeader = new JSONObject();
		JSONObject kafkaJson = new JSONObject();
		kafkaJson.put("eventId", integrationeventId);
		sourceJson.put("entityType", entityType);
		sourceJson.put("content", content);
		kafkaJson.put(EVENT_PARAM, sourceJson);
		eventHeader.put(TENANT_ID, tenantId);
		eventHeader.put(SITE_ID, siteId);
		eventHeader.put(SEQUENCE_ID, 2);
		eventHeader.put(CORRELATION_ID, correlationId);
		eventHeader.put("configKey", configKey);
		kafkaJson.put(EVENT_HEADER, eventHeader);
		return kafkaJson;

	}

	/**
	 * splits the xml and do's validation and publish to kafka
	 * 
	 * @param exchange
	 * @param eventId
	 * @param json
	 * @param fileType
	 * @param doc
	 * @param sourceJson
	 * @param jsonKeyForSplitting
	 * @param tenantId
	 * @param siteId
	 * @throws JSONException
	 * @throws UnableToSplitFileExeption
	 * @throws XMLTOSTringConversionException
	 */
	private void doXmlSplitValidationAndPublish(Exchange exchange, String eventId, JSONObject strategyJson,
			String fileType, Document doc, JSONObject sourceJson, String jsonKeyForSplitting, String tenantId,
			String siteId) throws JSONException, UnableToSplitFileExeption, XMLTOSTringConversionException {
		if (strategyJson == null) {
			return;
		}
		String entityType = (String) exchange.getIn().getHeader("entityType");
		JSONObject filesplitJson = strategyJson.getJSONObject(FILE_CONTENT_SPLITING);
		boolean isSplitRequired = filesplitJson.getBoolean(SPLITING_REQUIRED);
		String tagToSplit = null;
		
		if (jsonKeyForSplitting.equalsIgnoreCase(JSON_PATH) &&((String)exchange.getIn().getHeader("jsonType")).equalsIgnoreCase("jsonArray")) {
			logger.debug("inside if block");
			tagToSplit = entityType + "s/" + entityType;
		
		}else {
			tagToSplit = filesplitJson.getString(jsonKeyForSplitting);
		}
		logger.debug("tag to split is :"+tagToSplit);
		JSONObject validationJson = strategyJson.getJSONObject(VALIDATION);
		boolean isValidationReq = validationJson.getBoolean(VALIDATION_REQUIRED);
		if (isSplitRequired) {
			doXmlSplit(exchange, tagToSplit, doc, eventId, fileType, isValidationReq, validationJson, sourceJson,
					tenantId, siteId);
		} else {
			exchange.getIn().setHeader("event-Id", AGG_ENRICH_TRANSFORM_EVENT);
			String configKey = (String) exchange.getIn().getHeader("configKey");
			String integrationeventId = UUIDs.timeBased().toString();
			doc = addHeaderTagInDoc(doc, integrationeventId, entityType, fileType);
			String fileContent = IntegrationUtil.convertDocumentToString(doc);
			boolean validationFlag = true;
			try {
				if (isValidationReq) {
					doXmlValidation(validationJson, doc);
				}
			} catch (ValidationFailedExeption e) {
				validationFlag = false;
				dblogger.updateFiletrackingStatus(eventId, FAILED, e.getMessage());
				dblogger.logInIntegrationFlowTracking(integrationeventId, eventId, 2,
						"Kafka-aggOrEnrichmentOrTransformQueue", LocalDateTime.now().toString(), entityType,
						"Kafka-Subscriber", e.getMessage(), fileContent, fileType, DONE, 0, siteId,
						"Kafka-UnprocessedReq", (String)exchange.getIn().getHeader("startTime"), FAILED, tenantId);
			}
			if (validationFlag) {
				dblogger.logInIntegrationFlowTracking(integrationeventId, eventId, 2,
						"Kafka-aggOrEnrichmentOrTransformQueue", LocalDateTime.now().toString(), entityType,
						"Kafka-Subscriber", null, fileContent, fileType, DONE, 0, siteId, "Kafka-UnprocessedReq",
						(String)exchange.getIn().getHeader("startTime"), SUCCESS, tenantId);
				JSONObject jonTokafka = createJsonToPublish(tenantId, siteId, eventId, integrationeventId, sourceJson,
						entityType, fileContent, configKey);
				AbstractLeapCamelBean publisher = new KafkaPublisher();
				exchange.getIn().setBody(jonTokafka);
				try {
					publisher.invokeBean(exchange);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	/*
	 * private void doJSONSplitValidationAndPublish(Exchange exchange, String
	 * eventId, JSONObject strategyJson, String fileType, Object eventJson,
	 * JSONObject sourceJson, String tenantId, String siteId) throws
	 * UnableToParseException, XMLTOSTringConversionException { logger.debug(
	 * "inside doJSONSplitValidationAndPublish method"); try { JSONObject
	 * filesplitJson = strategyJson.getJSONObject(FILE_CONTENT_SPLITING);
	 * boolean isSplitRequired = filesplitJson.getBoolean(SPLITING_REQUIRED);
	 * String tagToSplit = filesplitJson.getString(JSON_PATH); JSONObject
	 * validationJson = strategyJson.getJSONObject(VALIDATION); boolean
	 * isValidationReq = validationJson.getBoolean(VALIDATION_REQUIRED); String
	 * entityType = (String) exchange.getIn().getHeader("entityType"); String
	 * configKey = (String) exchange.getIn().getHeader("configKey"); if
	 * (eventJson instanceof JSONArray) { JSONArray eventArray = (JSONArray)
	 * eventJson; if (isSplitRequired) { for (int i = 0; i <
	 * eventArray.length(); i++) { JSONObject event =
	 * eventArray.getJSONObject(i); logger.debug("splitted json is :"
	 * +event.toString()); doJSONValidationAndPublish(eventId, event,
	 * isValidationReq, validationJson, sourceJson, configKey, entityType,
	 * fileType, tenantId, siteId, exchange); } } else {
	 * 
	 * doJSONValidationAndPublish(eventId, eventJson, isValidationReq,
	 * validationJson, sourceJson, configKey, entityType, fileType, tenantId,
	 * siteId, exchange); } return; } if (eventJson instanceof JSONObject) {
	 * 
	 * doJSONValidationAndPublish(eventId, eventJson, isValidationReq,
	 * validationJson, sourceJson, configKey, entityType, fileType, tenantId,
	 * siteId, exchange); }
	 * 
	 * } catch (JSONException e) {
	 * 
	 * }
	 * 
	 * }
	 */

	/*
	 * private void doJSONValidationAndPublish(String eventId, Object event,
	 * boolean isValidationReq, JSONObject validationJSON, JSONObject
	 * sourceJson, String configKey, String entityType, String fileType, String
	 * tenantId, String siteId, Exchange exchange) throws JSONException,
	 * UnableToParseException, XMLTOSTringConversionException { logger.debug(
	 * "event needs to do publish is :"+event.toString()); Document doc =
	 * IntegrationUtil.getXmlDocumentFromJSONObject(event, entityType); String
	 * integrationeventId = UUIDs.timeBased().toString(); doc =
	 * addHeaderTagInDoc(doc, integrationeventId, entityType, fileType); String
	 * fileContent = IntegrationUtil.convertDocumentToString(doc); boolean
	 * validationFlag = true; try { if (isValidationReq) {
	 * doXmlValidation(validationJSON, doc); } } catch (ValidationFailedExeption
	 * e) { validationFlag = false;
	 * dblogger.logInIntegrationFlowTracking(integrationeventId, eventId, 2,
	 * "Kafka-aggOrEnrichmentOrTransformQueue", LocalDateTime.now().toString(),
	 * entityType, "Kafka-Subscriber", e.getMessage(), fileContent, fileType,
	 * DONE, 0, siteId, "Kafka-UnprocessedReq", LocalDateTime.now().toString(),
	 * FAILED, tenantId); } if (validationFlag) {
	 * dblogger.logInIntegrationFlowTracking(integrationeventId, eventId, 2,
	 * "Kafka-aggOrEnrichmentOrTransformQueue", LocalDateTime.now().toString(),
	 * entityType, "Kafka-Subscriber", null, fileContent, fileType, DONE, 0,
	 * siteId, "Kafka-UnprocessedReq", LocalDateTime.now().toString(), SUCCESS,
	 * tenantId); JSONObject jonTokafka = createJsonToPublish(tenantId, siteId,
	 * eventId, integrationeventId, sourceJson, entityType, fileContent,
	 * configKey); AbstractLeapCamelBean publisher = new KafkaPublisher();
	 * exchange.getIn().setBody(jonTokafka); try {
	 * publisher.invokeBean(exchange); } catch (Exception e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } }
	 * 
	 * }
	 */

	/**
	 * adds header tag containing eventId
	 * 
	 * @param doc
	 * @param eventId
	 * @param entityType
	 * @param fileType
	 * @return Document
	 */
	private Document addHeaderTagInDoc(Document doc, String eventId, String entityType, String fileType) {
		Node root = null;
		if (fileType.equalsIgnoreCase("json")) {
			Node root1 = doc.getFirstChild();
			root = root1.getFirstChild();
		}
		if (fileType.equalsIgnoreCase("xml")) {
			root = doc.getDocumentElement();
		}
		logger.debug("root tag is : " + root.getNodeName());

		Element headerTag = doc.createElement(XML_HEADER);
		Element eventIdTag = doc.createElement("eventId");
		eventIdTag.appendChild(doc.createTextNode(eventId));
		headerTag.appendChild(eventIdTag);
		Node cloneHeaderNode = headerTag.cloneNode(true);
		doc.adoptNode(cloneHeaderNode);
		root.appendChild(cloneHeaderNode);
		return doc;
	}

	/**
	 * 
	 * @param exchange
	 * @param tagToSplit
	 * @param doc
	 * @param EventId
	 * @param fileType
	 * @param isValidationReq
	 * @param validationJson
	 * @param sourceJson
	 * @param tenantId
	 * @param siteId
	 * @throws JSONException
	 * @throws UnableToSplitFileExeption
	 * @throws XMLTOSTringConversionException
	 */
	private void doXmlSplit(Exchange exchange, String tagToSplit, Document doc, String EventId, String fileType,
			boolean isValidationReq, JSONObject validationJson, JSONObject sourceJson, String tenantId, String siteId)
			throws JSONException, UnableToSplitFileExeption, XMLTOSTringConversionException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			XPathFactory xfactory = XPathFactory.newInstance();
			XPath xpath = xfactory.newXPath();
			XPathExpression exp = xpath.compile(tagToSplit);
			NodeList eventNodesFiltered = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);
			Element root1=doc.getDocumentElement();
			String patentTag = root1.getNodeName();
			logger.debug("paraant tag is : "+patentTag);
			for (int i = 0; i < eventNodesFiltered.getLength(); ++i) {
				logger.debug("inside for loop");
				String integrationeventId = UUIDs.timeBased().toString();
				Document newDoc = builder.newDocument();
				Element root = newDoc.createElement(patentTag);
				newDoc.appendChild(root);
				Element headerTag = newDoc.createElement(XML_HEADER);
				Element eventIdTag = newDoc.createElement("eventId");
				eventIdTag.appendChild(newDoc.createTextNode(integrationeventId));
				headerTag.appendChild(eventIdTag);
				Node cloneHeaderNode = headerTag.cloneNode(true);
				newDoc.adoptNode(cloneHeaderNode);
				root.appendChild(cloneHeaderNode);
				Node eventNode = eventNodesFiltered.item(i);
				Node clonedNode = eventNode.cloneNode(true);
				newDoc.adoptNode(clonedNode); // We adopt the orphan :)
				root.appendChild(clonedNode);
				exchange.getIn().setHeader("event-Id", AGG_ENRICH_TRANSFORM_EVENT);
				String entityType = (String) exchange.getIn().getHeader("entityType");
				String configKey = (String) exchange.getIn().getHeader("configKey");
				String fileContent = IntegrationUtil.convertDocumentToString(newDoc);
				boolean validationFlag = true;
				try {
					if (isValidationReq)
						doXmlValidation(validationJson, newDoc);
				} catch (ValidationFailedExeption e) {
					validationFlag = false;
					dblogger.updateFiletrackingStatus(EventId, FAILED, e.getMessage());
					dblogger.logInIntegrationFlowTracking(integrationeventId, EventId, 2,
							"Kafka-aggOrEnrichmentOrTransformQueue", LocalDateTime.now().toString(), entityType,
							"Kafka-Subscriber", e.getMessage(), fileContent, fileType, DONE, 0, siteId,
							"Kafka-UnprocessedReq",(String)exchange.getIn().getHeader("startTime"), FAILED, tenantId);
				}
				if (validationFlag) {
					dblogger.logInIntegrationFlowTracking(integrationeventId, EventId, 2,
							"Kafka-aggOrEnrichmentOrTransformQueue", LocalDateTime.now().toString(), entityType,
							"Kafka-Subscriber", null, fileContent, fileType, DONE, 0, siteId, "Kafka-UnprocessedReq",
							(String)exchange.getIn().getHeader("startTime"), SUCCESS, tenantId);
					JSONObject jonTokafka = createJsonToPublish(tenantId, siteId, EventId, integrationeventId,
							sourceJson, entityType, fileContent, configKey);
					AbstractLeapCamelBean publisher = new KafkaPublisher();
					exchange.getIn().setBody(jonTokafka);
					try {
						publisher.invokeBean(exchange);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					logger.debug("splitted xml is : " + IntegrationUtil.convertDocumentToString(newDoc));
				}
			}
			
		} catch (ParserConfigurationException | XPathExpressionException e) {
			throw new UnableToSplitFileExeption("unable to split the xml file : " + e.getMessage(), e);
		}
	}

	/**
	 * do xml validation based on the xslt in the configuration
	 * 
	 * @param validateJson
	 * @param object
	 * @throws ValidationFailedExeption
	 */
	private void doXmlValidation(JSONObject validateJson, Object object) throws ValidationFailedExeption {
		Document doc = (Document) object;
		try {
			String xsdPath = validateJson.getString(VALIDATION_XSD);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new File(xsdPath));
			Validator validator = schema.newValidator();
			validator.validate(new DOMSource(doc));
		} catch (SAXException | IOException | JSONException e) {
			logger.error("validation failed : " + e.getMessage());
			throw new ValidationFailedExeption("The xml " + doc + " is not valid ", e);
		}

	}

	/**
	 * do csv splitting validation and publish to kafka
	 * 
	 * @param exchange
	 * @param eventId
	 * @param strategyJson
	 * @param fileType
	 * @param fileContent
	 * @param sourceJson
	 * @param tenantId
	 * @param siteId
	 * @throws JSONException
	 * @throws CSVToXMLConversionException
	 * @throws XMLTOSTringConversionException
	 */
	private void doCSVSplitValidationAndPublish(Exchange exchange, String eventId, JSONObject strategyJson,
			String fileType, String fileContent, JSONObject sourceJson, String tenantId, String siteId)
			throws JSONException, CSVToXMLConversionException, XMLTOSTringConversionException {
		logger.debug("inside doCSVSplitValidationAndPublish method");
		if (strategyJson == null) {
			return;
		}
		JSONObject filesplitJson = strategyJson.getJSONObject(FILE_CONTENT_SPLITING);
		boolean isSplitRequired = filesplitJson.getBoolean(SPLITING_REQUIRED);
		JSONObject validationJson = strategyJson.getJSONObject(VALIDATION);
		boolean isValidationReq = validationJson.getBoolean(VALIDATION_REQUIRED);
		String[] lines = fileContent.split("\n");
		String header = lines[0];
		String[] csvFileds = header.split(",");
		if (isSplitRequired) {
			doCSVSplitConversionAndPublish(exchange, fileContent, eventId, fileType, isValidationReq, validationJson,
					sourceJson, tenantId, siteId);
		} else {
			exchange.getIn().setHeader("event-Id", AGG_ENRICH_TRANSFORM_EVENT);
			String entityType = (String) exchange.getIn().getHeader("entityType");
			String configKey = (String) exchange.getIn().getHeader("configKey");
			String integrationeventId = UUIDs.timeBased().toString();
			Document doc = doCSVConversion(fileContent, csvFileds, eventId, entityType);
			String xmlFileContent = IntegrationUtil.convertDocumentToString(doc);
			boolean validationFlag = true;
			try {
				if (isValidationReq) {
					doXmlValidation(validationJson, doc);
				}
			} catch (ValidationFailedExeption e) {
				validationFlag = false;
				dblogger.updateFiletrackingStatus(eventId, FAILED, e.getMessage());
				dblogger.logInIntegrationFlowTracking(integrationeventId, eventId, 2,
						"Kafka-aggOrEnrichmentOrTransformQueue", LocalDateTime.now().toString(), entityType,
						"Kafka-Subscriber", e.getMessage(), xmlFileContent, fileType, DONE, 0, siteId,
						"Kafka-UnprocessedReq", (String)exchange.getIn().getHeader("startTime"), FAILED, tenantId);
			}
			if (validationFlag) {
				dblogger.logInIntegrationFlowTracking(integrationeventId, eventId, 2,
						"Kafka-aggOrEnrichmentOrTransformQueue", LocalDateTime.now().toString(), entityType,
						"Kafka-Subscriber", null, xmlFileContent, fileType, DONE, 0, siteId, "Kafka-UnprocessedReq",
						(String)exchange.getIn().getHeader("startTime"), SUCCESS, tenantId);
				JSONObject jonTokafka = createJsonToPublish(tenantId, siteId, eventId, integrationeventId, sourceJson,
						entityType, xmlFileContent, configKey);
				AbstractLeapCamelBean publisher = new KafkaPublisher();
				exchange.getIn().setBody(jonTokafka);
				try {
					publisher.invokeBean(exchange);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param exchange
	 * @param fileContent
	 * @param eventId
	 * @param fileType
	 * @param isValidationReq
	 * @param validationJson
	 * @param sourceJson
	 * @param tenantId
	 * @param siteId
	 * @throws JSONException
	 * @throws CSVToXMLConversionException
	 * @throws XMLTOSTringConversionException
	 */
	private void doCSVSplitConversionAndPublish(Exchange exchange, String fileContent, String eventId, String fileType,
			boolean isValidationReq, JSONObject validationJson, JSONObject sourceJson, String tenantId, String siteId)
			throws JSONException, CSVToXMLConversionException, XMLTOSTringConversionException {
		String[] lines = fileContent.split("\n");
		String header = lines[0];
		String[] csvFields = header.split(",");

		Document doc = null;
		for (int i = 1; i < lines.length; i++) {
			String integrationeventId = UUIDs.timeBased().toString();
			String singleFile = header.concat("\n").concat(lines[i]);
			logger.debug("splitted csv file is : " + singleFile);
			exchange.getIn().setHeader("event-Id", AGG_ENRICH_TRANSFORM_EVENT);
			String entityType = (String) exchange.getIn().getHeader("entityType");
			String configKey = (String) exchange.getIn().getHeader("configKey");
			doc = doCSVConversion(singleFile, csvFields, integrationeventId, entityType);
			String xmlFileContent = IntegrationUtil.convertDocumentToString(doc);
			boolean validationFlag = true;
			try {
				if (isValidationReq)
					doXmlValidation(validationJson, doc);
			} catch (ValidationFailedExeption e) {
				validationFlag = false;
				dblogger.logInIntegrationFlowTracking(integrationeventId, eventId, 2,
						"Kafka-aggOrEnrichmentOrTransformQueue", LocalDateTime.now().toString(), entityType,
						"Kafka-Subscriber", e.getMessage(), xmlFileContent, fileType, DONE, 0, siteId,
						"Kafka-UnprocessedReq", (String)exchange.getIn().getHeader("startTime"), FAILED, tenantId);
			}
			if (validationFlag) {
				dblogger.logInIntegrationFlowTracking(integrationeventId, eventId, 2,
						"Kafka-aggOrEnrichmentOrTransformQueue", LocalDateTime.now().toString(), entityType,
						"Kafka-Subscriber", null, xmlFileContent, fileType, DONE, 0, siteId, "Kafka-UnprocessedReq",
						(String)exchange.getIn().getHeader("startTime"), SUCCESS, tenantId);
				JSONObject jonTokafka = createJsonToPublish(tenantId, siteId, eventId, integrationeventId, sourceJson,
						entityType, xmlFileContent, configKey);
				AbstractLeapCamelBean publisher = new KafkaPublisher();
				exchange.getIn().setBody(jonTokafka);
				try {
					publisher.invokeBean(exchange);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.debug("splitted xml is : " + IntegrationUtil.convertDocumentToString(doc));
			}

		}
	}

	/**
	 * it will convert the csv string to the XML
	 * 
	 * @param fileContent
	 * @param csvFields
	 * @param eventId
	 * @param entityType
	 * @return Document
	 * @throws CSVToXMLConversionException
	 */
	private Document doCSVConversion(String fileContent, String[] csvFields, String eventId, String entityType)
			throws CSVToXMLConversionException {
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			Document newDoc = domBuilder.newDocument();
			// Root element
			Element rootElement = newDoc.createElement(entityType);
			newDoc.appendChild(rootElement);
			// Read csv file
			BufferedReader csvReader = new BufferedReader(new StringReader(fileContent));
			csvReader.readLine();
			String curLine = null;
			while ((curLine = csvReader.readLine()) != null) {
				Element headerElement = newDoc.createElement(XML_HEADER);
				Element eventIdElement = newDoc.createElement("eventId");
				eventIdElement.appendChild(newDoc.createTextNode(eventId));
				headerElement.appendChild(eventIdElement);
				Node cloneHeaderNode = headerElement.cloneNode(true);
				newDoc.adoptNode(cloneHeaderNode);
				rootElement.appendChild(cloneHeaderNode);
				logger.debug("line is : " + curLine);
				String[] values=curLine.split(",",-1);
				
					Element rowElement = newDoc.createElement(entityType.substring(0, entityType.length() - 1));
					for(int i=0; i<csvFields.length;i++) {
						logger.debug("i value is :" +i);
						logger.debug("filed is : "+csvFields[i]);
							logger.debug("values is : "+values[i]);
							Element curElement = newDoc.createElement(csvFields[i].trim());
							curElement.appendChild(newDoc.createTextNode(values[i].trim()));
							rowElement.appendChild(curElement);
						
					}
					rootElement.appendChild(rowElement);
			}
			return newDoc;
		} catch (IOException | ParserConfigurationException e) {
			logger.error("exception is : " + e.getMessage());
			throw new CSVToXMLConversionException("unable to convert csv - xml :" + e.getMessage(), e);
		}
	}

	/**
	 * if file type is xml , reads the configuation and gets the strategy to be
	 * applied
	 * 
	 * @param exchange
	 * @param entityStratagyPerma
	 * @param fileName
	 * @param fileContent
	 * @return
	 * @throws JSONException
	 * @throws UnableToGetEntityTypeException
	 */
	private JSONObject getStrategyToApplyForXml(Exchange exchange, JSONObject entityStratagyPerma, String fileName,
			Document fileContent) throws JSONException, UnableToGetEntityTypeException {
		logger.debug("inside getStrategyToApply () method");
		Iterator keys = entityStratagyPerma.keys();
		JSONObject strategyJsonObject = null;
		String key = null;
		while (keys.hasNext()) {
			key = (String) keys.next();
			JSONObject strategyJson = entityStratagyPerma.getJSONObject(key);
			String enityTypeStategy = strategyJson.getString(ENTITY_TYPE_STRATEGY);

			switch (enityTypeStategy.toLowerCase()) {
			case FILENAMEPATTERN:
				getEntityTypeByFileNamePattern(key, exchange, strategyJson, fileName);

			case XML_ROOT:
				logger.debug("inside xml-root");
				JSONObject json = getEntityTypeByXMLRoot(key, exchange, strategyJson, fileContent);
				if (json != null) {
					strategyJsonObject = json;
				}
			}
			if (strategyJsonObject != null) {
				logger.debug("stategy json is : " + strategyJsonObject);
				return strategyJsonObject;
			}
		}
		return strategyJsonObject;
	}

	/**
	 * if file type is JSON , reads the configuation and gets the strategy to be
	 * applied
	 * 
	 * @param exchange
	 * @param entityStratagyPerma
	 * @param fileName
	 * @param fileContent
	 * @return JSONObject
	 * @throws JSONException
	 * @throws UnableToGetEntityTypeException
	 * @throws JSONFileFormatException
	 */
	private JSONObject getStrategyToApplyForJSON(Exchange exchange, JSONObject entityStratagyPerma, String fileName,
			JSONObject eventJson) throws JSONException, UnableToGetEntityTypeException, JSONFileFormatException {
		logger.debug("inside getStrategyToApplyForJSON () method");
		Iterator keys = entityStratagyPerma.keys();

		JSONObject json = null;
		String key = null;
		while (keys.hasNext()) {
			key = (String) keys.next();
			JSONObject strategyJson = entityStratagyPerma.getJSONObject(key);
			String enityTypeStategy = strategyJson.getString(ENTITY_TYPE_STRATEGY);
			switch (enityTypeStategy.toLowerCase()) {
			case FILENAMEPATTERN:
				json = getEntityTypeByFileNamePattern(key, exchange, strategyJson, fileName);

			case JSON_KEY:
				logger.debug("inside Json-Key");
				json = getEntityTypeByJSONKey(key, exchange, strategyJson, eventJson);

			}
			if (json != null) {
				return json;
			}
		}
		logger.debug("stategy json is : " + json);
		return json;
	}

	/**
	 * if file type is JSON , reads the configuation and gets the strategy to be
	 * applied
	 * 
	 * @param exchange
	 * @param entityStratagyPerma
	 * @param fileName
	 * @param fileContent
	 * @return JSONObject
	 * @throws JSONException
	 * @throws UnableToGetEntityTypeException
	 * @throws JSONFileFormatException
	 */
	private JSONObject getStrategyToApplyForJSONArray(Exchange exchange, JSONObject entityStratagyPerma,
			String fileName, JSONArray eventJson)
			throws JSONException, UnableToGetEntityTypeException, JSONFileFormatException {
		logger.debug("inside getStrategyToApplyForJSON () method");
		Iterator keys = entityStratagyPerma.keys();

		JSONObject json = null;
		String key = null;
		while (keys.hasNext()) {
			key = (String) keys.next();
			JSONObject strategyJson = entityStratagyPerma.getJSONObject(key);
			String enityTypeStategy = strategyJson.getString(ENTITY_TYPE_STRATEGY);

			switch (enityTypeStategy.toLowerCase()) {
			case FILENAMEPATTERN:
				json = getEntityTypeByFileNamePattern(key, exchange, strategyJson, fileName);

			case JSON_ARRAY_KEY:
				logger.debug("inside Json-array-Key");
				json = getEntityTypeByJSONArrayKey(key, exchange, strategyJson, eventJson);

			}
			if (json != null) {
				return json;
			}
		}
		logger.debug("stategy json is : " + json);
		return json;
	}

	/**
	 * if file type is csv , reads the configuation and gets the strategy to be
	 * applied
	 * 
	 * @param exchange
	 * @param entityStratagyPerma
	 * @param fileName
	 * @param fileContent
	 * @return
	 * @throws JSONException
	 */
	private JSONObject getStategyToApplyForCSV(Exchange exchange, JSONObject entityStratagyPerma, String fileName,
			String fileContent) throws JSONException {

		logger.debug("inside getStrategyToApplyForJSON () method");
		Iterator keys = entityStratagyPerma.keys();
		JSONObject strategyJsonObject = null;
		String key = null;
		while (keys.hasNext()) {
			key = (String) keys.next();
			JSONObject strategyJson = entityStratagyPerma.getJSONObject(key);
			String enityTypeStategy = strategyJson.getString(ENTITY_TYPE_STRATEGY);

			switch (enityTypeStategy.toLowerCase()) {
			case FILENAMEPATTERN:
				getEntityTypeByFileNamePattern(key, exchange, strategyJson, fileName);

			case CSV_HEADER:
				JSONObject json = getEntityTypeByCSVHeader(key, exchange, strategyJson, fileContent);
				if (json != null) {
					strategyJsonObject = json;
				}
			}
			if (strategyJsonObject != null) {
				logger.debug("stategy json is : " + strategyJsonObject);
				return strategyJsonObject;
			}
		}
		return strategyJsonObject;
	}

	/**
	 * gets the entity type for csv by csv header
	 * 
	 * @param key
	 * @param exchange
	 * @param strategyJson
	 * @param fileContent
	 * @return JSONObject
	 * @throws JSONException
	 */
	private JSONObject getEntityTypeByCSVHeader(String key, Exchange exchange, JSONObject strategyJson,
			String fileContent) throws JSONException {
		logger.debug("getEntityTypeByCSVHeader ()");
		JSONObject json = null;
		String csvContent = strategyJson.getString(CSV_HEADER_CONTAINS);
		String[] lines = fileContent.split("\n");
		String header = lines[0];
		String[] headers = header.split(",");
		for (String col : headers) {
			if (col.equalsIgnoreCase(csvContent)) {
				json = strategyJson.getJSONObject(APPLY);
				String entityType = json.getString(ENTITY);
				exchange.getIn().setHeader("entityType", entityType);
				exchange.getIn().setHeader("configKey", key);
				logger.debug("starttegy json object is :" + json);
				return json;

			}
		}
		return null;

	}

	/**
	 * gets the entity type by the file name
	 * 
	 * @param key
	 * @param exchange
	 * @param strategyJSon
	 * @param fileName
	 * @return
	 * @throws JSONException
	 */
	private JSONObject getEntityTypeByFileNamePattern(String key, Exchange exchange, JSONObject strategyJSon,
			String fileName) throws JSONException {
		String regex = strategyJSon.getString(FILE_NAME_REGEX);
		JSONObject json = strategyJSon.getJSONObject(APPLY);
		String entityTpe = json.getString(ENTITY);
		if (regex.equalsIgnoreCase(fileName)) {
			exchange.getIn().setHeader("entityType", entityTpe);
			exchange.getIn().setHeader("configKey", key);
			return json;
		}
		return null;
	}

	/**
	 * gets the entity type by the json key
	 * 
	 * @param key
	 * @param exchange
	 * @param strategyJSon
	 * @param doc
	 * @return JSONObject
	 * @throws JSONException
	 * @throws UnableToGetEntityTypeException
	 * @throws JSONFileFormatException
	 */
	private JSONObject getEntityTypeByJSONKey(String key, Exchange exchange, JSONObject strategyJSon,
			JSONObject jsonEvent) throws JSONException, UnableToGetEntityTypeException, JSONFileFormatException {
		String jsonRootKey = strategyJSon.getString(JSON_ROOT_KEY);
		Iterator keys = jsonEvent.keys();
		if (keys.hasNext()) {
			if (jsonRootKey.equalsIgnoreCase((String) keys.next())) {
				JSONObject applyJson = strategyJSon.getJSONObject(APPLY);
				String entityType = applyJson.getString(ENTITY);
				exchange.getIn().setHeader("entityType", entityType);
				exchange.getIn().setHeader("configKey", key);
				logger.debug("starttegy json object is :" + applyJson);
				return applyJson;

			}
		}
		return null;
	}

	/**
	 * gets the entity type by the json key
	 * 
	 * @param key
	 * @param exchange
	 * @param strategyJSon
	 * @param doc
	 * @return JSONObject
	 * @throws JSONException
	 * @throws UnableToGetEntityTypeException
	 * @throws JSONFileFormatException
	 */
	private JSONObject getEntityTypeByJSONArrayKey(String key, Exchange exchange, JSONObject strategyJSon,
			JSONArray jsonArray) throws JSONException, UnableToGetEntityTypeException, JSONFileFormatException {
		String jsonRootKey = strategyJSon.getString(JSON_ROOT_KEY);
		JSONObject jsonEvent = jsonArray.getJSONObject(0);
		Iterator keys = jsonEvent.keys();
		while (keys.hasNext()) {
			if (jsonRootKey.equalsIgnoreCase((String) keys.next())) {
				JSONObject applyJson = strategyJSon.getJSONObject(APPLY);
				String entityType = applyJson.getString(ENTITY);
				exchange.getIn().setHeader("entityType", entityType);
				exchange.getIn().setHeader("configKey", key);
				logger.debug("starttegy json object is :" + applyJson);
				return applyJson;

			}
		}
		return null;
	}

	/**
	 * gets the entity type by the xml root tag
	 * 
	 * @param key
	 * @param exchange
	 * @param strategyJSon
	 * @param doc
	 * @return
	 * @throws JSONException
	 * @throws UnableToGetEntityTypeException
	 */
	private JSONObject getEntityTypeByXMLRoot(String key, Exchange exchange, JSONObject strategyJSon, Document doc)
			throws JSONException, UnableToGetEntityTypeException {
		JSONObject json = null;
		Element root = doc.getDocumentElement();
		String rootTag = root.getNodeName();

		String jsonRootKey = strategyJSon.getString(XML_ROOT_TAG);
		logger.debug("root tag is :" + rootTag);
		if (rootTag.equalsIgnoreCase(jsonRootKey)) {
			json = strategyJSon.getJSONObject(APPLY);
			String entityType = json.getString(ENTITY);
			exchange.getIn().setHeader("entityType", entityType);
			exchange.getIn().setHeader("configKey", key);
			logger.debug("starttegy json object is :" + json);
		}
		return json;

	}

	/**
	 * do enrichrichment for the event
	 * 
	 * @param exchange
	 * @throws JSONException
	 */
	public void enrichEvent(Exchange exchange) throws JSONException {
		logger.debug("indie enrichEvent");
		Message message = exchange.getIn();
		String kafkaEvent = message.getBody(String.class);
		String eventId = null;
		String correlationId = null;
		String entityType = null;
		String fileContent = null;
		String endPoint = null;
		String fileType = null;
		String fileName = null;
		String sourceFolder = null;
		String http_url = null;
		String tenantId = null;
		String siteId = null;
		String sequenceId = null;
		try {
			JSONObject kafkaEventJson = new JSONObject(kafkaEvent);
			eventId = kafkaEventJson.getString(EVENT_ID);
			JSONObject eventParamJson = kafkaEventJson.getJSONObject(EVENT_PARAM);
			JSONObject sourceJson = eventParamJson.getJSONObject(SOURCE);
			endPoint = sourceJson.getString(END_POINT);
			fileType = sourceJson.getString(FILE_TYPE);
			entityType = sourceJson.getString("entityType");
			fileName = sourceJson.getString(FILE_NAME);
			sourceFolder = sourceJson.getString(SOURCE_FOLDER);
			http_url = sourceJson.getString(END_POINT_URL);
			fileContent = sourceJson.getString("content");
			JSONObject eventHeaderJson = kafkaEventJson.getJSONObject(EVENT_HEADER);
			correlationId = eventHeaderJson.getString(CORRELATION_ID);
			sequenceId = eventHeaderJson.getString(SEQUENCE_ID);
			// logic in enrich ment

		} catch (JSONException e) {
			dblogger.updateFiletrackingStatus(correlationId, FAILED, e.getMessage());
			dblogger.logInIntegrationFlowTracking(eventId, correlationId, Integer.valueOf(sequenceId), "kafka-proccess",
					LocalDateTime.now().toString(), entityType, "kafka-subscriber", e.getMessage(), fileContent,
					fileType, null, 0, siteId, "kafka-aggEnrich", LocalDateTime.now().toString(), FAILED, tenantId);
			throw new JSONException(e);

		}
	}

	/**
	 * stage4--consumes event from the processedQueue
	 * 
	 * @param exchange
	 * @throws JSONException
	 */
	public void processProcessedEvent(Exchange exchange) throws JSONException {
		logger.debug("indie processProcessedEvent method");
		Message message = exchange.getIn();
		String kafkaEvent = message.getBody(String.class);
		String eventId = null;
		String correlationId = null;
		String entityType = null;
		String fileContent = null;
		String endPoint = null;
		String fileType = null;
		String fileName = null;
		String sourceFolder = null;
		String http_url = null;
		String tenantId = null;
		String siteId = null;
		String sequenceId = null;
		try {

			JSONObject kafkaEventJson = new JSONObject(kafkaEvent);
			eventId = kafkaEventJson.getString(EVENT_ID);
			JSONObject eventParamJson = kafkaEventJson.getJSONObject(EVENT_PARAM);
			JSONObject sourceJson = eventParamJson.getJSONObject(SOURCE);
			endPoint = sourceJson.getString(END_POINT);
			fileType = sourceJson.getString(FILE_TYPE);
			entityType = sourceJson.getString("entityType");
			fileName = sourceJson.getString(FILE_NAME);
			sourceFolder = sourceJson.getString(SOURCE_FOLDER);
			http_url = sourceJson.getString(END_POINT_URL);
			fileContent = sourceJson.getString("content");
			JSONObject eventHeaderJson = kafkaEventJson.getJSONObject(EVENT_HEADER);
			tenantId = eventHeaderJson.getString(TENANT_ID);
			siteId = eventHeaderJson.getString(SITE_ID);
			correlationId = eventHeaderJson.getString(CORRELATION_ID);
			sequenceId = eventHeaderJson.getString(SEQUENCE_ID);
			// logic to proccess the event
			dblogger.updateFiletrackingStatus(correlationId, SUCCESS, null);
			dblogger.logInIntegrationFlowTracking(eventId, correlationId, Integer.valueOf(sequenceId) + 2,
					"web service", LocalDateTime.now().toString(), entityType, "kafka-subscriber", null, fileContent,
					fileType, DONE, 0, siteId, "kafka-ProcessingQueue", LocalDateTime.now().toString(), SUCCESS,
					tenantId);

		} catch (JSONException e) {
			dblogger.updateFiletrackingStatus(correlationId, FAILED, e.getMessage());
			dblogger.logInIntegrationFlowTracking(eventId, correlationId, Integer.valueOf(sequenceId), "kafka-ProcessingQueue",
					LocalDateTime.now().toString(), entityType, "kafka-subscriber", e.getMessage(), fileContent,
					fileType, null, 0, siteId, "web service", LocalDateTime.now().toString(), FAILED, tenantId);
			throw new JSONException(e);
		}

	}

	public static void main(String[] args)
			throws UnableToReadPropertiesFileException, EventTransformationException, UnableToParseException,
			JSONException, XMLTOSTringConversionException, JSONFileFormatException, NoStrategyDefinedException {
		ProcessEventBean b = new ProcessEventBean();
		String tenantId = "all";
		String siteId = "all";
		String filePath = IntegrationConstants.LOCAL_PATH + "//" + tenantId + "//" + siteId + "//Unprocessed//"
				+ "orders" + "." + "json";

		logger.debug("file path is :" + filePath);

		String line="123,,7000,,";
		String[] split = line.split(",");
		logger.debug("split length is "+split.length);
		for(String str : split){
			logger.debug("array is : "+str);
		}
		StringTokenizer s=new StringTokenizer(line, ",");
		logger.debug("lenth is :"+s.countTokens());
		System.setProperty("test","true");
		boolean boolean1 = Boolean.getBoolean("test");
		logger.debug("boolean is : "+boolean1);
		
//	String content = new String(Files.readAllBytes(Paths.get(filePath)));
		//logger.debug("file content is : " + content);
		// Document doc =
		// IntegrationUtil.getXmlDocumentFromJsonString1(content);
		// logger.debug("xml string is : " +
		// IntegrationUtil.convertDocumentToString(doc));
		/*
		 * String[] lines = content.split("\n"); String header = lines[0];
		 * String[] csvFileds = header.split(",");
		 */
		// Document doc = b.getXmlDocumentFromString(content);
		/*
		 * Document doc = b.doCSVConversion(content, csvFileds); String xml
		 * = convertDocumentToString(doc); logger.debug("xml string is :" +
		 * xml);
		 */
		/*
		 * String xsltfilePath = IntegrationConstants.LOCAL_PATH + "//" +
		 * tenantId + "//" + siteId + "//Unprocessed//" + "productxslt" +
		 * "." + "xslt";
		 */
		// b.transformEventBody(doc, xsltfilePath);
		// b.doXmlSplit("products/product",
		// b.getXmlDocumentFromString(content));

	}

}
