/**
 * 
 */
package com.attunedlabs.integrationfwk.activities.bean;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
import javax.xml.xpath.XPathFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.attunedlabs.integrationfwk.config.jaxb.LoopActivity;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.config.jaxb.Pipeline;
import com.attunedlabs.integrationfwk.pipeline.service.PipeLineExecutionHelper;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;

/**
 * @author Reactiveworks
 *
 */
public class LoopActivityProcessor {
	private static Logger logger = LoggerFactory.getLogger(LoopActivityProcessor.class.getName());

	/**
	 * @param exchange
	 * @throws LoopActivityException
	 */
	public void processor(Exchange exchange) throws LoopActivityException {
		logger.debug("inside processor() of LoopActivityProcessor...");
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		executeLooping(pipeactivity.getLoopActivity(), exchange);
	}

	/**
	 * @param loopActivity
	 * @param exchange
	 * @throws LoopActivityException
	 */
	private static void executeLooping(LoopActivity loopActivity, Exchange exchange) throws LoopActivityException {
		try {
			logger.debug("inside executeLooping() of LoopActivityProcessor...");
			String loopingCondition = loopActivity.getLoopingCondition();
			String requestBody = exchange.getIn().getBody(String.class);
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = getDocument(requestBody);
			NodeList nodeList = (NodeList) xPath.compile(loopingCondition).evaluate(doc, XPathConstants.NODESET);
			logger.debug("Total number of elements matching xquery : " + nodeList.getLength() + "(" + loopingCondition + ")");
			List<String> requestList = new ArrayList<String>();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node != null) {
					String nodeString = convertXMLNodeToString(node);
					String newRequestBody = PipeLineExecutionHelper.modifyRequestBody(loopActivity.getPipeline(),
							nodeString);
					requestList.add(newRequestBody);
				}
			}
			boolean parallelProcessing = loopActivity.isParallelProcessing();
			exchange.getIn().setHeader(PipelineServiceConstant.LOOP_REQUEST_DATA, requestList);
			exchange.getIn().setHeader(PipelineServiceConstant.PARALLEL_PROCESSING, parallelProcessing);
		} catch (Exception e) {
			throw new LoopActivityException("Error while fetching Xpath values. " + e.getMessage());
		}
	}

	/**
	 * @param pipeline
	 * @param exchange
	 * @throws LoopActivityException
	 */
	@SuppressWarnings("unused")
	private static void executePipeline(Pipeline pipeline, Exchange exchange) throws LoopActivityException {
		if (pipeline != null) {
			try {
				String newRequestBody = PipeLineExecutionHelper.modifyRequestBody(pipeline, exchange);
				CamelContext camelContext = exchange.getContext();
				Route route = camelContext.getRoute(PipelineServiceConstant.PIPELINE_SERVICE_ROUTE);
				Endpoint endpoint = route.getConsumer().getEndpoint();
				ProducerTemplate template = camelContext.createProducerTemplate();
				ExchangePattern pattern = exchange.getPattern();
				Object sendBodyAndHeaders = template.sendBodyAndHeaders(endpoint, pattern, newRequestBody,
						exchange.getIn().getHeaders());
				exchange.getIn().setBody(sendBodyAndHeaders);
				logger.debug("pipeline execution completed.");
			} catch (Exception ex) {
				throw new LoopActivityException(ex.getMessage(), ex.getCause());
			}
		}
	}

	/**
	 * @param node
	 * @return
	 * @throws LoopActivityException
	 */
	private static String convertXMLNodeToString(Node node) throws LoopActivityException {
		StringWriter writer = new StringWriter();
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, PipelineServiceConstant.YES);
			transformer.setOutputProperty(OutputKeys.INDENT, PipelineServiceConstant.YES);
			transformer.transform(new DOMSource(node), new StreamResult(writer));
		} catch (TransformerException ex) {
			throw new LoopActivityException(ex.getMessage(), ex.getCause());
		}
		return writer.toString();
	}

	/**
	 * @param content
	 * @return
	 * @throws LoopActivityException
	 * @throws Exception
	 */
	private static Document getDocument(String content) throws LoopActivityException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new InputSource(new StringReader(content)));
		} catch (SAXException | IOException | ParserConfigurationException ex) {
			throw new LoopActivityException(ex.getMessage(), ex.getCause());
		}
		doc.getDocumentElement().normalize();
		return doc;
	}

}
