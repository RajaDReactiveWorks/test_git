package com.integration.processor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.integration.exception.JSONFileFormatException;
import com.integration.exception.NoStrategyDefinedException;
import com.integration.exception.UnableToParseException;
import com.integration.exception.UnableToReadFileFromLocalException;
import com.integration.exception.XMLTOSTringConversionException;

public class IntegrationUtil {
	private final static Logger logger = LoggerFactory.getLogger(IntegrationUtil.class.getName());
	public IntegrationUtil() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * returns the xml document from the JSon String
	 * 
	 * @param fileContent
	 * @return Document
	 * @throws JSONException
	 * @throws UnableToParseException
	 * @throws JSONFileFormatException 
	 * @throws NoStrategyDefinedException 
	 *//*
	public  static Document getXmlDocumentFromJsonString(String fileContent, String entityType) throws JSONException, UnableToParseException, JSONFileFormatException, NoStrategyDefinedException {
		if(entityType==null){
			throw new NoStrategyDefinedException("there is no strategy defined");
		}
		
		JSONObject rootJson = new JSONObject();
		rootJson.put(entityType, json);
		
		String eventXml = XML.toString(json, entityType+"s");
		return getXmlDocumentFromString(eventXml);
		
		
		
	}*/
	
	
	
	public static Document getXmlDocumentFromJSONObject(JSONObject json, String entityType) throws JSONException, UnableToParseException{
		Document doc=null;
		String eventXml = XML.toString(json);
		try {
			 doc=getXmlDocumentFromString(eventXml);
		} catch (SAXException e) {
			logger.debug("inside catch block");
			if(e.getMessage().contains("The markup in the document following the root element must be well-formed")){
				logger.debug("inside if block");
			eventXml = XML.toString(json,entityType+"s" );
			try {
				doc=getXmlDocumentFromString(eventXml);
			} catch (SAXException e1) {
				throw new UnableToParseException("unable to convert the json  to xml : "+e.getMessage(), e);
			}
			}
			else{
				throw new UnableToParseException("unable to convert the json  to xml : "+e.getMessage(), e);
			}
		}
		return doc;
	}
	
	public static Document getXmlDocumentFromJSONArray(JSONArray jsonArray, String entityType) throws JSONException, UnableToParseException{
		Document doc=null;
		JSONObject root=new JSONObject();
		root.put(entityType, jsonArray);
		String eventXml = XML.toString(root, entityType+"s");
		try {
			 doc=getXmlDocumentFromString(eventXml);
		} catch (SAXException e) {
			throw new UnableToParseException("unable to convert the json  to xml : "+e.getMessage(), e);
			}
		return doc;
		}
		
	
	
	/**
	 * converts the doc object to String content
	 * 
	 * @param doc
	 * @return String
	 * @throws XMLTOSTringConversionException 
	 */
	public static String convertDocumentToString(Document doc) throws XMLTOSTringConversionException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
			// below code to remove XML declaration
			// transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
			// "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			String output = writer.getBuffer().toString();
			
			return output;
		} catch (TransformerException e) {
			throw new XMLTOSTringConversionException("unable to convert Document object to string : "+e.getMessage(), e);
		}
	}
	
	/**
	 * reads the file from the local and returns a string
	 * 
	 * @param fileName
	 * @param fileType
	 * @param tenantId
	 * @param siteId
	 * @return String
	 * @throws UnableToReadFileFromLocalException
	 */
	public static String readFileFromLocal(String fileName, String fileType, String tenantId, String siteId)
			throws UnableToReadFileFromLocalException {
		logger.debug("inside readFileFromLocal () method");
		String filePath = IntegrationConstants.LOCAL_PATH + "//" + tenantId + "//" + siteId + "//Unprocessed//"
				+ fileName;
		logger.debug("file path is :" + filePath);
		try {
			String content = new String(Files.readAllBytes(Paths.get(filePath)));
			logger.debug("file content is : " + content);
			return content;
		} catch (IOException e) {
			throw new UnableToReadFileFromLocalException(
					"unable to read the file " + fileName + " from " + filePath + " : " + e.getMessage(), e);
		}
	}
	
	/**
	 * gets the xml document from the xml string
	 * 
	 * @param fileContent
	 * @return Document
	 * @throws UnableToParseException
	 * @throws SAXException 
	 */
	public static Document getXmlDocumentFromString(String fileContent) throws UnableToParseException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setExpandEntityReferences(false);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			logger.debug("xml string is :" + fileContent);
			Document doc = builder.parse(new InputSource(new StringReader(fileContent.trim())));
			return doc;
		} catch ( IOException | ParserConfigurationException e) {
			throw new UnableToParseException("Unable to parse the file content to Xml : " + e.getMessage(), e);
		}
	}
	
	

}
