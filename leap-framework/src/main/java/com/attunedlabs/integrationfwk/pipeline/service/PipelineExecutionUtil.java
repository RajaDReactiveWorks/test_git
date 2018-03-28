package com.attunedlabs.integrationfwk.pipeline.service;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class PipelineExecutionUtil {

	/**
	 * @param document
	 * @return
	 * @throws PipeLineExecutionException
	 */
	public static String convertDocumentToString(Document document) throws PipeLineExecutionException {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, PipelineServiceConstant.YES);
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			return writer.toString();
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			throw new PipeLineExecutionException("Error while converting Document to String " + e.getMessage());
		}
	}// end of the method.

	/**
	 * @param xmlStr
	 * @return
	 * @throws PipeLineExecutionException
	 */
	public static Document convertStringToDocument(String xmlStr) throws PipeLineExecutionException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
			return doc;
		} catch (Exception e) {
			throw new PipeLineExecutionException("unable to parse the Xml String to Document due to " + e.getMessage());
		}
	}// end of the method.

	/**
	 * @param body
	 * @param exchange
	 * @return
	 */
	public static boolean checkForXmlContent(Exchange exchange) {
		String body = exchange.getIn().getBody(String.class);
		// return body.trim().startsWith("<");
		if (body.matches(PipelineServiceConstant.XML_PATTERN)) {
			return true;
		}
		return false;
	}// end of the method.

	/**
	 * @param body
	 * @param exchange
	 * @return
	 */
	public static boolean checkForJsonContent(Exchange exchange) {
		String body = exchange.getIn().getBody(String.class);
		try {
			new JSONObject(body);
			return true;
		} catch (JSONException e) {
			try {
				new JSONArray(body);
				return true;
			} catch (JSONException e1) {
				// do nothing
			}
		}
		return false;
	}// end of the method.

	/**
	 * @param exchange
	 * @param doc
	 * @throws PipeLineExecutionException
	 * @throws Exception
	 */
	public static Document createIntegerationPipesDocument(Exchange exchange, Document doc)
			throws PipeLineExecutionException {
		try {
			String generatedPipelineName = generatePipelineName(exchange);
			XPath xPath = XPathFactory.newInstance().newXPath();
			XPathExpression xPathExpression = xPath.compile(PipelineServiceConstant.INTEGRATION_PIPELINE_PATH);
			NodeList nodes = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
			Document newXmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = newXmlDocument.createElement(PipelineServiceConstant.INTEGRATION_PIPES);
			setRootAttribute(root);
			newXmlDocument.appendChild(root);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if (node.getNodeName().equals(PipelineServiceConstant.INTEGRATION_PIPE)) {
						((Element) node).setAttribute(PipelineServiceConstant.NAME, generatedPipelineName);
						((Element) node).setAttribute(PipelineServiceConstant.IS_ENABLED, PipelineServiceConstant.TRUE);
					}
					Node copyNode = newXmlDocument.importNode(node, true);
					root.appendChild(copyNode);
				}
			}
			exchange.getIn().setHeader(PipelineServiceConstant.PIPELINE_NAME, generatedPipelineName);
			return newXmlDocument;
		} catch (Exception e) {
			throw new PipeLineExecutionException(e.getMessage());
		}
	}// end of the method.

	/**
	 * @param root
	 */
	private static void setRootAttribute(Element root) {
		root.setAttribute(PipelineServiceConstant.XMLNS_XSI_NAME, PipelineServiceConstant.XMLNS_XSI_VALUE);
		root.setAttribute(PipelineServiceConstant.XMLNS_NO_NAME_SPACE_SCHEMA_LOCATION_NAME,
				PipelineServiceConstant.XMLNS_NO_NAME_SPACE_SCHEMA_LOCATION_VALUE);
		root.setAttribute(PipelineServiceConstant.XMLNS_FWK_NAME, PipelineServiceConstant.XMLNS_FWK_VALUE);
		root.setAttribute(PipelineServiceConstant.XMLNS_PIPE_NAME, PipelineServiceConstant.XMLNS_PIPE_VALUE);
	}

	/**
	 * @param exchange
	 * @param doc
	 * @throws Exception
	 */
	public static Document createRequestDataDocument(Exchange exchange, Document doc)
			throws PipeLineExecutionException {
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			XPathExpression xPathExpression = xPath.compile(PipelineServiceConstant.REQUEST_DATA_PATH);
			NodeList nodes = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);

			Document newXmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			for (int i = 0; i < nodes.getLength(); i++) {
				NodeList nodeList = nodes.item(i).getChildNodes();
				for (int j = 0; j < nodeList.getLength(); j++) {
					Node node = nodeList.item(j);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Node copyNode = newXmlDocument.importNode(node, true);
						newXmlDocument.appendChild(copyNode);
					}
				}
			}
			return newXmlDocument;
		} catch (Exception e) {
			throw new PipeLineExecutionException(e.getMessage());
		}
	}// end of the method.

	/**
	 * @param exchange
	 * @param doc
	 * @throws Exception
	 */
	public static String getPipelineNameFromDocument(Exchange exchange, Document doc)
			throws PipeLineExecutionException {
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			XPathExpression xPathExpression = xPath.compile(PipelineServiceConstant.PIPELINE_NAME_PATH);
			NodeList nodes = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
			String pipelineName = "";
			for (int i = 0; i < nodes.getLength(); i++) {
				NodeList nodeList = nodes.item(i).getChildNodes();
				for (int j = 0; j < nodeList.getLength(); j++) {
					Node node = nodeList.item(j);
					if (node.getNodeType() == Node.TEXT_NODE) {
						NodeList childNodes = node.getChildNodes();
						if (childNodes.getLength() == 0)
							pipelineName = node.getNodeValue();
					}
				}
			}
			return pipelineName;
		} catch (Exception e) {
			throw new PipeLineExecutionException(e.getMessage());
		}
	}// end of the method.

	/**
	 * @param exchange
	 * @throws PipeLineExecutionException
	 */
	public static void convertToJson(Exchange exchange) throws PipeLineExecutionException {
		JSONObject xmlJSONObj;
		try {
			String body = exchange.getIn().getBody(String.class);
			xmlJSONObj = XML.toJSONObject(body);
			String pipelineData = xmlJSONObj.toString(4);
			exchange.getIn().setBody(pipelineData);
		} catch (JSONException e) {
			throw new PipeLineExecutionException(e.getMessage());
		}
	}// end of the method.

	/**
	 * @param exchange
	 * @return
	 * @throws PipeLineExecutionException
	 */
	public static String convertToXml(Exchange exchange) throws PipeLineExecutionException {
		String body = exchange.getIn().getBody(String.class);
		JSONObject json;
		try {
			json = new JSONObject(body);
			return XML.toString(json);
		} catch (JSONException e) {
			throw new PipeLineExecutionException(e.getMessage());
		}
	}// end of the method.

	/**
	 * @param exchange
	 * @return
	 */
	protected static String generatePipelineName(Exchange exchange) {
		Object pipelineNameObj = exchange.getIn().getHeader(PipelineServiceConstant.PIPELINE_NAME);
		if (pipelineNameObj != null)
			return (String) pipelineNameObj;
		else
			return getPipelineName(exchange);
	}

	/**
	 * @return
	 */
	protected synchronized static String getPipelineName(Exchange exchange) {
		int pipeNameCounter = 0;
		Object pipeNameCounterObj = exchange.getProperty(PipelineServiceConstant.PIPE_NAME_COUNTER);
		if(pipeNameCounterObj != null)
			pipeNameCounter = (int) pipeNameCounterObj;
		int counter = ++pipeNameCounter;
		exchange.setProperty(PipelineServiceConstant.PIPE_NAME_COUNTER, counter);
		return PipelineServiceConstant.PIPELINE_IN_REQUEST + counter;
	}

	/**
	 * @param source
	 * @return
	 */
	public static String convertToSingleLine(String source) {
		return source.replaceAll(PipelineServiceConstant.STRING_NEW_LINE_PATTERN, "");
	}

}
