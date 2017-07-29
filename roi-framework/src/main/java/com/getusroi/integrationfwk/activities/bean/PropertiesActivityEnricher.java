package com.getusroi.integrationfwk.activities.bean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.getusroi.integrationfwk.config.jaxb.PipeActivity;
import com.getusroi.integrationfwk.config.jaxb.PropertiesMapping;
import com.getusroi.mesh.MeshHeader;
import com.getusroi.mesh.MeshHeaderConstant;

public class PropertiesActivityEnricher {

	private Logger logger = LoggerFactory.getLogger(PropertiesActivityEnricher.class.getName());

	/**
	 * This method is to fetch the propertyvalue from the pipeline configuration
	 * and putting it into Exchange's body whose xpath it is fetching from the
	 * pipeline again.
	 * 
	 * @param exchange
	 * @throws PropertyActivityException
	 * @throws TransformerException
	 * @throws ActivityEnricherException
	 */
	public void processorBean(Exchange exchange)
			throws PropertyActivityException, ActivityEnricherException, TransformerException {
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		List<PropertiesMapping> propertyMappingList = pipeactivity.getPropertiesActivity().getPropertiesMapping();
		String exchangeIncomingBody=exchange.getIn().getBody(String.class);
		logger.debug("exchangeIncomingBody:: "+exchangeIncomingBody);
		if(exchangeIncomingBody!=null && !exchangeIncomingBody.isEmpty()){
			exchangeIncomingBody=StringEscapeUtils.escapeXml(exchangeIncomingBody);
			logger.debug("exchangeIncomingBody:: "+exchangeIncomingBody);
		}
		Document xmlDocument = generateDocumentFromString(exchangeIncomingBody);
		logger.debug("xml document  : " + xmlDocument);
		for (int i = 0; i < propertyMappingList.size(); i++) {
			logger.debug("Entered Loop");
			String propertyValue = pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i)
					.getPropertyValue();
			logger.debug("propertyValue : " + propertyValue);
			String nodeToAdd = pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).getElementToAdd();
			logger.debug("nodeToAdd : " + nodeToAdd);
			String toXpath = pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).getSetToXpath()
					.toString();
			logger.debug("got the xpath : " + toXpath.toString());
			String propertyValueSource = pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i)
					.getPropertyValueSource();
			logger.debug("got the propertyValueSource : " + propertyValueSource);
			String newXmlDoc = checkPropertyValueSource(propertyValueSource, toXpath, xmlDocument, propertyValue,
					nodeToAdd, exchange, i);
			logger.debug("newXMLDoc : " + newXmlDoc);
			if(newXmlDoc!=null && !newXmlDoc.isEmpty()){
				newXmlDoc=StringEscapeUtils.escapeXml(newXmlDoc);
				logger.debug("newXmlDoc after escaped:: "+newXmlDoc);
			}
			xmlDocument = generateDocumentFromString(newXmlDoc);
		}
		exchange.getIn().setBody(documentToString(xmlDocument));
	}// ..end of method

	/**
	 * to generate the document object once and all from the xml input which is
	 * of String
	 * 
	 * @param xmlInput
	 * @return documentObject
	 * @throws EmailNotifierException
	 * @throws ParserConfigurationException
	 */
	public Document generateDocumentFromString(String xmlInput) throws PropertyActivityException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		xmlInput = StringEscapeUtils.unescapeXml(xmlInput);
		DocumentBuilder builder = null;
		Document xmlDocument;
		xmlInput = xmlInput.trim();
		// xmlInput = StringEscapeUtils.unescapeXml();
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new PropertyActivityException("Unable to initiate the document builder..", e);
		}
		try {
			xmlDocument = builder.parse(new ByteArrayInputStream(xmlInput.getBytes("UTF-16")));
		} catch (SAXException | IOException e) {
			throw new PropertyActivityException("Unable to parse the xmlString into document..", e);
		}
		return xmlDocument;
	}// ..end of method

	private String checkPropertyValueSource(String propertyValueSource, String toXpath, Document xmlDocument,
			String propertyValue, String nodeToAdd, Exchange exchange, int i)
			throws ActivityEnricherException, TransformerException, PropertyActivityException {
		logger.debug(".checkPropertyValueSource method PropertiesActivityEnricher");
		String newXmlDoc = null;
		logger.debug("xml document  : " + xmlDocument);
		if (propertyValueSource != null && propertyValueSource.length() > 0 && !(propertyValueSource.isEmpty())) {
			switch (propertyValueSource) {
			case "MeshHeader":
				logger.debug("propertyValueSource is MeshHeader");
				newXmlDoc = getNodeValueFromMeshHeaderAndAppend(toXpath, xmlDocument, propertyValue, nodeToAdd,
						exchange, i);
				break;

			case "Exchange":
				logger.debug("propertyValueSource is Exchange");
				newXmlDoc = getNodeValueFromExchangeAndAppend(toXpath, xmlDocument, propertyValue, nodeToAdd, exchange,
						i);
				break;
			case "Xpath":
				logger.debug("propertyValueSource is Xpath");
				newXmlDoc = getNodeAndAppendFromXpath(exchange, toXpath, xmlDocument, propertyValue, nodeToAdd, i);
				break;
			default:
				logger.debug("propertyValueSource is direct");
				newXmlDoc = getNodeAndAppend(exchange, toXpath, xmlDocument, propertyValue, nodeToAdd, i);
				break;

			}
		} else {
			// #TODO , need to throw proper exception
			logger.debug("propertyValueSource shouldnot be null");
		}

		return newXmlDoc;
	}

	private String getNodeAndAppendFromXpath(Exchange exchange, String expression, Document xmlDocument, String res,
			String nodeToAdd, int i) throws ActivityEnricherException, TransformerException, PropertyActivityException {
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		String appendedDocument = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = null;
		try {
			logger.debug("expression : " + expression);
			logger.debug("doc : " + documentToString(xmlDocument));
			String xmlStr = documentToString(xmlDocument);
			xmlDocument = generateDocumentFromString(xmlStr);
			logger.debug("xpath : " + res);
			logger.debug("xmlDocument : " + getvalueFromDocument(xmlDocument, res));
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			// Node has been added fetched from the pipline configuration
			if (pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).getOverrideExistingNode() != null
					&& pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).getOverrideExistingNode()) {
				logger.debug("override true : "+nodeToAdd);
				// if(nodeToAdd.trim().startsWith("["));
				
				if (nodeToAdd.startsWith("@") && nodeToAdd.contains("=")){
					logger.debug("starts with @ and contain =");
					int num = nodeList.getLength();
					logger.info("Number of nodes" + num);
					for (int nodeCount = 0; nodeCount < num; nodeCount++) {
						Element element = (Element) nodeList.item(nodeCount);
						logger.info("Element name : " + element.getNodeName());
						NamedNodeMap attributes = element.getAttributes();
						int numAttrs = attributes.getLength();

						for (int attributeCount = 0; attributeCount < numAttrs; attributeCount++) {
							Attr attr = (Attr) attributes.item(attributeCount);
							String attrName = attr.getNodeName();
							String attrValue = attr.getNodeValue();
							logger.info("Found attribute Key: " + attrName + " with name: " + attrValue
									+ "..attribute value" + element.getTextContent());
							logger.info("comparing attribute to add " + nodeToAdd + " with " + attrValue);
							if (nodeToAdd.contains(attrValue)) {
								logger.info("before changing the element " + element.getTextContent());
								element.setTextContent(getvalueFromDocument(xmlDocument, res));
								logger.info("after changing the element " + element.getTextContent());
							}
						}
					}
				} else if (nodeToAdd.startsWith("@") && !nodeToAdd.contains("=")) {
					logger.debug("starts with @");
					logger.debug("nodeToAdd is an attribute : " + nodeToAdd);
					Element node = (Element) nodeList.item(nodeList.getLength() - 1);
					logger.debug("Adding attribute to : " + node.getNodeName());
					node.setAttribute(nodeToAdd, getvalueFromDocument(xmlDocument, res));
				} else {
					logger.debug("normal ELement");
					logger.debug("nodeListElement : " + nodeList.item(0).getNodeName() + "value : "
							+ nodeList.item(0).getNodeName());
					Element nodeListElement = (Element) nodeList.item(0);
					Element existingNode = (Element) nodeListElement.getElementsByTagName(nodeToAdd).item(0);
					logger.debug("doc : " + documentToString(xmlDocument));
					logger.debug("xpath : " + res);
					logger.debug("xmlDocument : " + getvalueFromDocument(xmlDocument, res));
					existingNode.setTextContent(getvalueFromDocument(xmlDocument, res));
				}
			} else {
				logger.debug("override false");
				Element node = xmlDocument.createElement(nodeToAdd);
				node.setTextContent(getvalueFromDocument(xmlDocument, res));
				// appended the element as a child to the node fetched from the
				// xpath
				nodeList.item(0).appendChild(node);
			}
			appendedDocument = documentToString(xmlDocument);
		} catch (XPathExpressionException e) {
			throw new ActivityEnricherException("Unable to compile the xpath expression at index - "
					+ " when evaluating document - " + xmlDocument + "..", e);
		}

		return appendedDocument;
	}

	/**
	 * to append string(res) into node fetched by using xpath expression
	 * 
	 * @param expression
	 * @param xmlDocument
	 * @param nodeToAdd
	 * @return
	 * @return non duplicate values as set
	 * @throws ActivityEnricherException
	 * @throws TransformerException
	 */
	private String getNodeAndAppend(Exchange exchange, String expression, Document xmlDocument, String res,
			String nodeToAdd, int i) throws ActivityEnricherException, TransformerException {
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		String appendedDocument = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = null;
		try {
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			// Node has been added fetched from the pipline configuration
			if (pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).getOverrideExistingNode() != null
					&& pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).getOverrideExistingNode()) {
				Element nodeListElement = (Element) nodeList.item(0);
				Element existingNode = (Element) nodeListElement.getElementsByTagName(nodeToAdd).item(0);
				existingNode.setTextContent(res);
			} else {
				Element node = xmlDocument.createElement(nodeToAdd);
				node.setTextContent(res);
				// appended the element as a child to the node fetched from the
				// xpath
				nodeList.item(0).appendChild(node);
			}
			appendedDocument = documentToString(xmlDocument);
		} catch (XPathExpressionException e) {
			throw new ActivityEnricherException("Unable to compile the xpath expression at index - "
					+ " when evaluating document - " + xmlDocument + "..", e);
		}

		return appendedDocument;
	}// ..end of the method

	/**
	 * to append string(res) into node fetched by using xpath expression
	 * 
	 * @param expression
	 * @param xmlDocument
	 * @param nodeToAdd
	 * @return
	 * @return non duplicate values as set
	 * @throws ActivityEnricherException
	 * @throws TransformerException
	 */
	private String getNodeValueFromMeshHeaderAndAppend(String expression, Document xmlDocument, String res,
			String nodeToAdd, Exchange exchange, int i) throws ActivityEnricherException, TransformerException {
		logger.debug(".getNodeValueFromMeshHeaderAndAppend method of PropertiesActivityEnricher");
		MeshHeader meshHeader = (MeshHeader) exchange.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		Map<String, Object> serviceRequestDataValue = meshHeader.getServiceRequestData();
		String appendedDocument = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = null;

		try {
			logger.debug("fetching the nodeList");
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			logger.debug("got the nodeList : " + nodeList.item(nodeList.getLength() - 1).getNodeName());
			if (pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).getOverrideExistingNode() != null) {
				logger.debug("property Node To Add is overrided");
				Element nodeListElement = (Element) nodeList.item(0);
				if (nodeListElement.getElementsByTagName(nodeToAdd).item(0) != null) {
					Element existingNode = (Element) nodeListElement.getElementsByTagName(nodeToAdd).item(0);
					existingNode.setTextContent((String) serviceRequestDataValue.get(res));
				} else {
					if (nodeToAdd.startsWith("@")) {
						logger.debug("nodeToAdd is an attribute : " + nodeToAdd);
						Element node = (Element) nodeList.item(nodeList.getLength() - 1);
						logger.debug("Adding attribute to : " + node.getNodeName());
						node.setAttribute(nodeToAdd, (String) serviceRequestDataValue.get(res));
					} else {
						Element node = xmlDocument.createElement(nodeToAdd);
						node.setTextContent((String) serviceRequestDataValue.get(res));
						nodeList.item(0).appendChild(node);
					}
				}
			} else {
				if (nodeToAdd.startsWith("@")) {
					logger.debug("nodeToAdd is an attribute : " + nodeToAdd);
					Element node = (Element) nodeList.item(nodeList.getLength() - 1);
					logger.debug("Adding attribute to : " + node.getNodeName());
					node.setAttribute(nodeToAdd.substring(1), (String) serviceRequestDataValue.get(res));
				} else {

					// Node has been added fetched from the pipline
					// configuration
					Element node = xmlDocument.createElement(nodeToAdd);
					node.setTextContent((String) serviceRequestDataValue.get(res));
					// appended the element as a child to the node fetched from
					// the
					// xpath
					nodeList.item(0).appendChild(node);
				}
			}
			appendedDocument = documentToString(xmlDocument);
		} catch (XPathExpressionException e) {
			throw new ActivityEnricherException("Unable to compile the xpath expression at index - "
					+ " when evaluating document - " + xmlDocument + "..", e);
		}
		return appendedDocument;
	}// ..end of the method

	/**
	 * to append string(res) into node fetched by using xpath expression
	 * 
	 * @param expression
	 * @param xmlDocument
	 * @param nodeToAdd
	 * @return
	 * @return non duplicate values as set
	 * @throws ActivityEnricherException
	 * @throws TransformerException
	 */
	private String getNodeValueFromExchangeAndAppend(String expression, Document xmlDocument, String res,
			String nodeToAdd, Exchange exchange, int i) throws ActivityEnricherException, TransformerException {
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		String appendedDocument = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = null;
		logger.debug("expression : " + expression);
		logger.debug("xmlDocument : " + documentToString(xmlDocument));
		logger.debug("res : " + res);
		logger.debug("nodeToAdd : " + nodeToAdd);
		logger.debug("exchange headers : " + exchange.getIn().getHeaders().toString());
		logger.debug("woXML : " + exchange.getIn().getBody(String.class));
		try {
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			if (pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).getOverrideExistingNode() != null
					&& pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).getOverrideExistingNode()) {
				Element nodeListElement = (Element) nodeList.item(0);
				Element existingNode = (Element) nodeListElement.getElementsByTagName(nodeToAdd).item(0);
				existingNode.setTextContent((String) exchange.getIn().getHeader(res));
			} else {
				// Node has been added fetched from the pipline configuration
				Element node = xmlDocument.createElement(nodeToAdd);
				node.setTextContent((String) exchange.getIn().getHeader(res));
				// appended the element as a child to the node fetched from the
				// xpath
				nodeList.item(0).appendChild(node);
			}
			appendedDocument = documentToString(xmlDocument);
		} catch (XPathExpressionException e) {
			throw new ActivityEnricherException("Unable to compile the xpath expression at index - "
					+ " when evaluating document - " + xmlDocument + "..", e);
		}

		return appendedDocument;
	}// ..end of the method

	/**
	 * locally used to instantiate the transformation factory and to process the
	 * transformation
	 * 
	 * @param inputXml
	 * @param xsltName
	 * @return transformed xml
	 * @throws ActivityEnricherException
	 * @throws TransformerException
	 */
	private String documentToString(Document xmlDocument) throws ActivityEnricherException, TransformerException {
		DOMSource domSource = new DOMSource(xmlDocument);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-16");
		transformer.transform(domSource, result);
		return writer.toString();
	}// ..end of the method

	/**
	 * To get Value From XmlDocument based on given xpath
	 * 
	 * @param document
	 * @param exhExchange
	 * @return
	 * @throws SCNotifyRequestProcessingException
	 */
	public String getvalueFromDocument(Document document, String xpath) {
		logger.debug("(.)  getvalueFromDocument method of ServiceChannelNotificationProcessBean class ");
		String val = null;
		if (document != null) {

			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeTypeList;
			try {
				nodeTypeList = (NodeList) xPath.compile((String) xpath).evaluate(document, XPathConstants.NODESET);
				// checking if
				if (nodeTypeList != null && nodeTypeList.getLength() > 0) {
					Node node = nodeTypeList.item(0);
					val = node.getTextContent();
				}
			} catch (XPathExpressionException e) {
				logger.error("Error in Evavluating  Xpath Expression to get  value  ", e);
			}
		}
		return val;
	}// .. End of Method getValueFromXmlData
}