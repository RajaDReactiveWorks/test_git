package com.attunedlabs.integrationfwk.activities.bean;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.integrationfwk.config.jaxb.FTLEnricherActivity;
import com.attunedlabs.integrationfwk.config.jaxb.FtlDataMapper;
import com.attunedlabs.integrationfwk.config.jaxb.FtlPathMap;
import com.attunedlabs.integrationfwk.config.jaxb.FtlfieldMapper;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.staticconfig.IStaticConfigurationService;
import com.attunedlabs.staticconfig.StaticConfigFetchException;
import com.attunedlabs.staticconfig.StaticConfigInitializationException;
import com.attunedlabs.staticconfig.factory.StaticConfigurationFactory;
import com.attunedlabs.staticconfig.impl.AccessProtectionException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

public class FtlEnricher {
	public static final String PREFIX = "stream2file";
	public static final String SUFFIX = ".tmp";
	private Logger logger = LoggerFactory.getLogger(FtlEnricher.class.getName());
	private IStaticConfigurationService iStaticConfigurationService;

	/**
	 * processor to process the ftl enricher, which gets the input from the
	 * header, process it an stores back into the same header key
	 * 
	 * @param exchange
	 * @throws ActivityEnricherException
	 * @throws StaticConfigInitializationException
	 * @throws StaticConfigFetchException
	 * @throws AccessProtectionException
	 */
	public void processorBean(Exchange exchange) throws ActivityEnricherException, StaticConfigFetchException,
			StaticConfigInitializationException, AccessProtectionException {
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		FTLEnricherActivity ftlActivity = pipeactivity.getFTLEnricherActivity();
		List<FtlPathMap> ftlFileNameList = ftlActivity.getFtlpathMapper().getFtlPathMap();
		logger.debug("Checking the leapHeader in FtlEnricher.."
				+ exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY));
		String inPut = exchange.getIn().getBody(String.class);
		Map<String, Object> mapObj = generateMapFromFtlConfig(ftlActivity, inPut);
		logger.debug("The inputMapObject: " + mapObj);
		String res = templateEngineInit(mapObj, ftlFileNameList.get(0).getFilePath(), exchange);
		logger.debug("The templated string: " + res);
		Document xmlDocument = generateDocumentFromString(inPut);
		Set<String> xpathExpressionSet = new LinkedHashSet<String>();
		xpathExpressionSet.add(pipeactivity.getFTLEnricherActivity().getFtlpathMapper().getFtlMapto().getToXpath());
		String newXmlDocument = getNodeAndAppend(xpathExpressionSet, xmlDocument, res);
		logger.debug(".The Appended XML IS : " + newXmlDocument);
		exchange.getIn().setBody(newXmlDocument);
		// exchange.getIn().setBody(res);
	}// ..end of the method

	/**
	 * locally generateMapFromFtlConfig will process the Xml Input with Xpath
	 * and will retreive values for the Ftl templating
	 * 
	 * @param ftlActivity
	 * @param xmlInput
	 * @return mapObject of Key-Value pairs
	 * @throws ActivityEnricherException
	 */
	private Map<String, Object> generateMapFromFtlConfig(FTLEnricherActivity ftlActivity, String xmlInput)
			throws ActivityEnricherException {
		FtlDataMapper ftlDataMapperObj = ftlActivity.getFtlDataMapper();
		List<FtlfieldMapper> ftlFieldMapperList = ftlDataMapperObj.getFtlfieldMapper();
		String msgXpath;
		String xpathField;
		Set<String> ftlFields = new LinkedHashSet<>();
		Set<String> xpathExpression = new LinkedHashSet<>();
		for (int i = 0; i < ftlFieldMapperList.size(); i++) {
			msgXpath = ftlFieldMapperList.get(i).getMsgXpath();
			xpathExpression.add(msgXpath);
			xpathField = ftlFieldMapperList.get(i).getFtlField();
			ftlFields.add(xpathField);
		}
		Set<Object> setOfValues = xpathProcessingOnInputXml(xpathExpression, generateDocumentFromString(xmlInput));
		Iterator<String> i1 = ftlFields.iterator();
		Iterator<Object> i2 = setOfValues.iterator();
		Map<String, Object> map = new HashMap<>();
		while (i1.hasNext() && i2.hasNext()) {
			map.put(i1.next(), i2.next());
		}
		return map;
	}// ..end of the method

	/**
	 * to generate the document object once and all from the xml input which is
	 * of String
	 * 
	 * @param xmlInput
	 * @return documentObject
	 * @throws ActivityEnricherException
	 */
	private Document generateDocumentFromString(String xmlInput) throws ActivityEnricherException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document xmlDocument;
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ActivityEnricherException("Unable to initiate the document builder..", e);
		}
		try {
			xmlDocument = builder.parse(new ByteArrayInputStream(xmlInput.getBytes("UTF-16")));
		} catch (SAXException | IOException e) {
			throw new ActivityEnricherException("Unable to parse the xmlString into document..", e);
		}
		return xmlDocument;
	}// ..end of method

	/**
	 * to append string(res) into node fetched by using xpath expression
	 * 
	 * @param expression
	 * @param xmlDocument
	 * @return
	 * @return non duplicate values as set
	 * @throws ActivityEnricherException
	 * @throws TransformerException
	 */
	private String getNodeAndAppend(Set<String> expression, Document xmlDocument, String res)
			throws ActivityEnricherException {
		String appendedDocument = null;
		logger.debug(
				"The expressionSet inside xpathProcessor: " + expression + "and its size is: " + expression.size());
		XPath xPath = XPathFactory.newInstance().newXPath();
		for (int x = 0; x < expression.size(); x++) {
			NodeList nodeList = null;
			try {
				nodeList = (NodeList) xPath.compile((String) expression.toArray()[x]).evaluate(xmlDocument,
						XPathConstants.NODESET);

				logger.debug("The length of nodeList: " + nodeList.getLength());

				nodeList.item(0).setTextContent(res);
				appendedDocument = documentToString(xmlDocument);
			} catch (XPathExpressionException e) {
				throw new ActivityEnricherException("Unable to compile the xpath expression at index - " + x
						+ " when evaluating document - " + xmlDocument + "..", e);
			}
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
	private String documentToString(Document xmlDocument) throws ActivityEnricherException {
		DOMSource domSource = new DOMSource(xmlDocument);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-16");
			transformer.transform(domSource, result);
		} catch (TransformerException e) {
			throw new ActivityEnricherException("Unable to convert Document to String In Ftl Enricher");
		}
		return writer.toString();
	}// ..end of the method

	// #TODO eliminate the commented methods in the class once it is
	// successfully working

	/*
	 * * @param expression
	 * 
	 * @param xmlDocument
	 * 
	 * @return
	 * 
	 * @return non duplicate values as set
	 * 
	 * @throws ActivityEnricherException
	 * 
	 * @throws TransformerException
	 */
	/*
	 * private String getNodeAndAppend(Set<String> expression, Document
	 * xmlDocument,String res) throws ActivityEnricherException,
	 * TransformerException { String appendedDocument=null; logger.debug(
	 * "The expressionSet inside xpathProcessor: " + expression +
	 * "and its size is: " + expression.size()); XPath xPath =
	 * XPathFactory.newInstance().newXPath(); for (int x = 0; x <
	 * expression.size(); x++) { NodeList nodeList = null; try { nodeList =
	 * (NodeList) xPath.compile((String)
	 * expression.toArray()[x]).evaluate(xmlDocument, XPathConstants.NODESET);
	 * 
	 * logger.debug("The length of nodeList: " + nodeList.getLength());
	 * 
	 * nodeList.item(0).setTextContent(res); appendedDocument =
	 * documentToString(xmlDocument); } catch (XPathExpressionException e) {
	 * throw new ActivityEnricherException(
	 * "Unable to compile the xpath expression at index - " + x +
	 * " when evaluating document - " + xmlDocument + "..", e); } } return
	 * appendedDocument; }
	 */// ..end of the method

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
	/*
	 * private String documentToString(Document xmlDocument) throws
	 * ActivityEnricherException, TransformerException { DOMSource domSource =
	 * new DOMSource(xmlDocument); StringWriter writer = new StringWriter();
	 * StreamResult result = new StreamResult(writer); TransformerFactory tf =
	 * TransformerFactory.newInstance(); Transformer transformer =
	 * tf.newTransformer(); transformer.setOutputProperty(OutputKeys.ENCODING,
	 * "UTF-16"); transformer.transform(domSource, result); return
	 * writer.toString(); }
	 */
	// ..end of the method

	/**
	 * to process the xpath expression on document to get the respective
	 * FiledValues to be substituted by
	 * 
	 * @param expression
	 * @param xmlDocument
	 * @return non duplicate values as set
	 * @throws ActivityEnricherException
	 */
	private Set<Object> xpathProcessingOnInputXml(Set<String> expression, Document xmlDocument)
			throws ActivityEnricherException {
		logger.debug(
				"The expressionSet inside xpathProcessor: " + expression + "and its size is: " + expression.size());
		Object fieldVal;
		XPath xPath = XPathFactory.newInstance().newXPath();
		Set<Object> fieldValList = new LinkedHashSet();
		for (int x = 0; x < expression.size(); x++) {
			NodeList nodeList = null;
			try {
				nodeList = (NodeList) xPath.compile((String) expression.toArray()[x]).evaluate(xmlDocument,
						XPathConstants.NODESET);
				logger.debug("The length of nodeList: " + nodeList.getLength());
				for (int i = 0; i < nodeList.getLength(); i++) {
					fieldVal = nodeList.item(i).getTextContent();
					logger.debug("The field values adding: " + fieldVal);
					fieldValList.add(fieldVal);
				}
			} catch (XPathExpressionException e) {
				throw new ActivityEnricherException("Unable to compile the xpath expression at index - " + x
						+ " when evaluating document - " + xmlDocument + "..", e);
			}
		}
		if (!fieldValList.isEmpty()) {
			logger.debug("List of FieldSubstitutable non-empty Values: " + fieldValList);
			return fieldValList;
		} else {
			throw new ActivityEnricherException(
					"Unable to get the substitutable fields from the fieldMapper configured - listOfSubstitutable fields are -"
							+ fieldValList);
		}
	}// ..end of the method

	/**
	 * to initialize the freemarker template engine and to process the
	 * templating
	 * 
	 * @param mapObj
	 * @param ftlFileName
	 * @return the processed/ template outputString
	 * @throws TemplateNotFoundException
	 * @throws IOException
	 * @throws TemplateException
	 * @throws StaticConfigInitializationException
	 * @throws StaticConfigFetchException
	 * @throws AccessProtectionException
	 */
	private String templateEngineInit(Map<String, Object> mapObj, String ftlFileName, Exchange exchange)
			throws StaticConfigFetchException, StaticConfigInitializationException, AccessProtectionException {
		logger.debug(".templateEngineInit().. -MapObject constructed: " + mapObj + "file - " + ftlFileName);
		// Configure Freemarker
		Configuration cfg = new Configuration();

		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		RequestContext requestContext = leapHeader.getRequestContext();

		try {
			iStaticConfigurationService = StaticConfigurationFactory.getFilemanagerInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
			throw new StaticConfigInitializationException(
					"Unable to instantiate filemanager for the ftl static configuration.." + ftlFileName, e1);
		}
		String ftlFile;
		try {
			ftlFile = iStaticConfigurationService.getStaticConfiguration(requestContext, ftlFileName);
		} catch (StaticConfigFetchException | StaticConfigInitializationException | AccessProtectionException e) {
			RequestContext reCntx=new RequestContext(ActivityConstant.GLOBAL_TENANT_ID,ActivityConstant.GLOBAL_SITE_ID,requestContext.getFeatureGroup(), requestContext.getFeatureName(),requestContext.getImplementationName(), requestContext.getVendor(),requestContext.getVersion());
			ftlFile = iStaticConfigurationService.getStaticConfiguration(reCntx, ftlFileName);
			logger.debug("ftlfile Fetch from Global :  " + ftlFileName);
		}catch (Exception e) {
			RequestContext reCntx=new RequestContext(ActivityConstant.GLOBAL_TENANT_ID,ActivityConstant.GLOBAL_SITE_ID,requestContext.getFeatureGroup(), requestContext.getFeatureName(),requestContext.getImplementationName(), requestContext.getVendor(),requestContext.getVersion());
			ftlFile = iStaticConfigurationService.getStaticConfiguration(reCntx, ftlFileName);
			logger.debug("ftlfile Fetch from Global :  " + ftlFileName);
		}
		
		
		
		logger.debug(".FTLPath : " + ftlFileName);
		if(ftlFile==null){
			RequestContext reCntx=new RequestContext(ActivityConstant.GLOBAL_TENANT_ID,ActivityConstant.GLOBAL_SITE_ID,requestContext.getFeatureGroup(), requestContext.getFeatureName(),requestContext.getImplementationName(), requestContext.getVendor(),requestContext.getVersion());
			ftlFile = iStaticConfigurationService.getStaticConfiguration(reCntx, ftlFileName);
			logger.debug("ftlfile Fetch from Global :  " + ftlFileName);

		}
		// Load the template
		InputStream is;
		try {
			InputStream in = IOUtils.toInputStream(ftlFile, "UTF-8");
			logger.debug("InputStreamObject of the file.." + in);
			Path p = stream2file(in);
			Path folder = p.getParent();
			logger.debug("Path to be searched for: " + folder);
			cfg.setDirectoryForTemplateLoading(new File(folder.toString()));
			Template template = cfg.getTemplate(p.getFileName().toString());
			Map<String, Object> dataSender = mapObj;
			Writer writer = new StringWriter();
			template.process(dataSender, writer);
			logger.debug("The returningTemplated data: " + writer.toString());
			return writer.toString();
		} catch (IOException | TemplateException e) {
			throw new StaticConfigFetchException(
					"Failed to create a template from the file, error in the file or template");
		}
	}// ..end of the method

	/**
	 * this is to create a temporary file which will be deleted once processed,
	 * it is just to store the 'ftl' as a file, in order to process
	 * 
	 * @param inutStream
	 * @return the path of the temporary file
	 * @throws StaticConfigFetchException
	 */
	private Path stream2file(InputStream in) throws StaticConfigFetchException {
		File tempFile;
		try {
			tempFile = File.createTempFile(PREFIX, SUFFIX);
			tempFile.deleteOnExit();
			try (FileOutputStream out = new FileOutputStream(tempFile)) {
				IOUtils.copy(in, out);
			}
			return Paths.get(tempFile.getPath());
		} catch (IOException e) {
			throw new StaticConfigFetchException("failed to create temporary file for ftl");
		}
	}// ..end of the method
}
