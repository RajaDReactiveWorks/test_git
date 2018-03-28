package com.attunedlabs.integrationfwk.activities.bean;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.Exchange;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.attunedlabs.integrationfwk.config.jaxb.ConditionalFlow;
import com.attunedlabs.integrationfwk.config.jaxb.ElseBlock;
import com.attunedlabs.integrationfwk.config.jaxb.ElseIfBlock;
import com.attunedlabs.integrationfwk.config.jaxb.IfBlock;
import com.attunedlabs.integrationfwk.config.jaxb.InnerElseBlock;
import com.attunedlabs.integrationfwk.config.jaxb.InnerElseIfBlock;
import com.attunedlabs.integrationfwk.config.jaxb.InnerIfBlock;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.config.jaxb.Pipeline;
import com.attunedlabs.integrationfwk.pipeline.service.PipeLineExecutionHelper;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

public class ConditionalFlowActivityProcessor {

	private static Logger logger = LoggerFactory.getLogger(GroovyScriptProcessor.class.getName());
	private static GroovyShell groovyShell = new GroovyShell();;

	/**
	 * @param exchange
	 * @throws ConditionalFlowActivityException
	 */
	public void processor(Exchange exchange) throws ConditionalFlowActivityException {
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		List<ConditionalFlow> conditionalFlows = pipeactivity.getConditionalFlowActivity().getConditionalFlow();
		if (conditionalFlows == null || conditionalFlows.isEmpty())
			throw new ConditionalFlowActivityException("Configure atleast one ConditionalFlow.");
		for (ConditionalFlow conditionalFlow : conditionalFlows) {
			executeConditionalFlow(exchange, conditionalFlow);
		}
		logger.debug("conditionalFlow : " + conditionalFlows);
	}// ..end of the method

	/**
	 * @param exchange
	 * @param conditionalFlow
	 * @throws ConditionalFlowActivityException
	 */
	private void executeConditionalFlow(Exchange exchange, ConditionalFlow conditionalFlow)
			throws ConditionalFlowActivityException {
		IfBlock ifBlock = conditionalFlow.getIfBlock();
		boolean validIfBlockExpression = validateIfBlockExpression(exchange, ifBlock);
		if (validIfBlockExpression) {
			logger.debug("inside ifBlock");
			if (ifBlock.getPipeline() != null) {
				Pipeline pipeline = ifBlock.getPipeline();
				try {
					PipeLineExecutionHelper.modifyRequestBody(pipeline, exchange);
				} catch (PipelineExecutionException e) {
					throw new ConditionalFlowActivityException(e.getMessage());
				}
			} else {
				executeInnerConditions(exchange, ifBlock);
			}
			return;
		}
		List<ElseIfBlock> elseIfBlocks = conditionalFlow.getElseIfBlock();
		for (ElseIfBlock elseIfBlock : elseIfBlocks) {
			logger.debug("inside elseIfBlock");
			boolean validElseIfBlockExpression = validateElseIfBlockExpression(exchange, elseIfBlock);
			if (validElseIfBlockExpression) {
				if (elseIfBlock.getPipeline() != null) {
					Pipeline pipeline = elseIfBlock.getPipeline();
					try {
						PipeLineExecutionHelper.modifyRequestBody(pipeline, exchange);
					} catch (PipelineExecutionException e) {
						throw new ConditionalFlowActivityException(e.getMessage());
					}
				} else {
					executeInnerConditions(exchange, elseIfBlock);
				}
				return;
			}
		}
		ElseBlock elseBlock = conditionalFlow.getElseBlock();
		if (elseBlock != null) {
			logger.debug("inside elseblock");
			if (elseBlock.getPipeline() != null) {
				Pipeline pipeline = elseBlock.getPipeline();
				try {
					PipeLineExecutionHelper.modifyRequestBody(pipeline, exchange);
				} catch (PipelineExecutionException e) {
					throw new ConditionalFlowActivityException(e.getMessage());
				}
			} else {
				executeInnerConditions(exchange, elseBlock);
			}
		}
	}// ..end of the method

	/**
	 * @param exchange
	 * @param ifBlock
	 * @throws ConditionalFlowActivityException
	 */
	private void executeInnerConditions(Exchange exchange, IfBlock ifBlock) throws ConditionalFlowActivityException {
		InnerIfBlock innerIfBlock = ifBlock.getInnerIfBlock();
		List<InnerElseIfBlock> innerElseIfBlocks = ifBlock.getInnerElseIfBlock();
		InnerElseBlock innerElseBlock = ifBlock.getInnerElseBlock();
		executeInnerPipeline(exchange, innerIfBlock, innerElseIfBlocks, innerElseBlock);
	}// ..end of the method

	/**
	 * @param exchange
	 * @param elseIfBlock
	 * @throws ConditionalFlowActivityException
	 */
	private void executeInnerConditions(Exchange exchange, ElseIfBlock elseIfBlock)
			throws ConditionalFlowActivityException {
		InnerIfBlock innerIfBlock = elseIfBlock.getInnerIfBlock();
		List<InnerElseIfBlock> innerElseIfBlocks = elseIfBlock.getInnerElseIfBlock();
		InnerElseBlock innerElseBlock = elseIfBlock.getInnerElseBlock();
		executeInnerPipeline(exchange, innerIfBlock, innerElseIfBlocks, innerElseBlock);
	}// ..end of the method

	/**
	 * @param exchange
	 * @param elseBlock
	 * @throws ConditionalFlowActivityException
	 */
	private void executeInnerConditions(Exchange exchange, ElseBlock elseBlock)
			throws ConditionalFlowActivityException {
		InnerIfBlock innerIfBlock = elseBlock.getInnerIfBlock();
		List<InnerElseIfBlock> innerElseIfBlocks = elseBlock.getInnerElseIfBlock();
		InnerElseBlock innerElseBlock = elseBlock.getInnerElseBlock();
		executeInnerPipeline(exchange, innerIfBlock, innerElseIfBlocks, innerElseBlock);
	}// ..end of the method

	/**
	 * @param exchange
	 * @param ifBlock
	 * @return
	 * @throws ConditionalFlowActivityException
	 */
	private boolean validateIfBlockExpression(Exchange exchange, IfBlock ifBlock)
			throws ConditionalFlowActivityException {
		boolean validExpression = false;
		if (ifBlock == null)
			throw new ConditionalFlowActivityException("ifBlock is required.");
		String conditionalExpression = ifBlock.getConditionalExpression();
		if (conditionalExpression == null)
			throw new ConditionalFlowActivityException("conditionalExpression is required.");
		String type = ifBlock.getType();
		if (type == null)
			throw new ConditionalFlowActivityException("If block expression type is required.");
		validExpression = validateExpression(exchange, type, conditionalExpression, ifBlock.getConditionalValue());
		return validExpression;
	}// ..end of the method

	/**
	 * @param exchange
	 * @param elseIfBlock
	 * @return
	 * @throws ConditionalFlowActivityException
	 */
	private boolean validateElseIfBlockExpression(Exchange exchange, ElseIfBlock elseIfBlock)
			throws ConditionalFlowActivityException {
		boolean validExpression = false;
		if (elseIfBlock == null)
			throw new ConditionalFlowActivityException("elseIfBlock is required.");
		String conditionalExpression = elseIfBlock.getConditionalExpression();
		if (conditionalExpression == null)
			throw new ConditionalFlowActivityException("conditionalExpression is required.");
		String type = elseIfBlock.getType();
		if (type == null)
			throw new ConditionalFlowActivityException("Else If block expression type is required.");
		validExpression = validateExpression(exchange, type, conditionalExpression, elseIfBlock.getConditionalValue());
		return validExpression;
	}// ..end of the method

	/**
	 * @param exchange
	 * @param innerIfBlock
	 * @param innerElseIfBlocks
	 * @param innerElseBlock
	 * @throws ConditionalFlowActivityException
	 */
	private void executeInnerPipeline(Exchange exchange, InnerIfBlock innerIfBlock,
			List<InnerElseIfBlock> innerElseIfBlocks, InnerElseBlock innerElseBlock)
			throws ConditionalFlowActivityException {
		if (innerIfBlock != null) {
			boolean validInnerIfBlockExpression = validateInnerIfBlockExpression(exchange, innerIfBlock);
			if (validInnerIfBlockExpression) {
				logger.debug("inside inner ifBlock");
				Pipeline pipeline = innerIfBlock.getPipeline();
				try {
					PipeLineExecutionHelper.modifyRequestBody(pipeline, exchange);
				} catch (PipelineExecutionException e) {
					throw new ConditionalFlowActivityException(e.getMessage());
				}
				return;
			}
		}
		for (InnerElseIfBlock innerElseIfBlock : innerElseIfBlocks) {
			logger.debug("inside innerElseIfBlock");
			boolean validInnerElseIfBlockExpression = validateInnerElseIfBlockExpression(exchange, innerElseIfBlock);
			if (validInnerElseIfBlockExpression) {
				logger.debug("inside inner innerElseIfBlock");
				Pipeline pipeline = innerElseIfBlock.getPipeline();
				try {
					PipeLineExecutionHelper.modifyRequestBody(pipeline, exchange);
				} catch (PipelineExecutionException e) {
					throw new ConditionalFlowActivityException(e.getMessage());
				}
				return;
			}
		}
		if (innerElseBlock != null) {
			logger.debug("inside innerElseBlock");
			Pipeline pipeline = innerElseBlock.getPipeline();
			try {
				PipeLineExecutionHelper.modifyRequestBody(pipeline, exchange);
			} catch (PipelineExecutionException e) {
				throw new ConditionalFlowActivityException(e.getMessage());
			}
			return;
		}
	}// ..end of the method

	/**
	 * @param exchange
	 * @param innerIfBlock
	 * @return
	 * @throws ConditionalFlowActivityException
	 */
	private boolean validateInnerIfBlockExpression(Exchange exchange, InnerIfBlock innerIfBlock)
			throws ConditionalFlowActivityException {
		boolean validExpression = false;
		if (innerIfBlock == null)
			throw new ConditionalFlowActivityException("innerIfBlock is required.");
		String conditionalExpression = innerIfBlock.getConditionalExpression();
		if (conditionalExpression == null)
			throw new ConditionalFlowActivityException("conditionalExpression is required.");
		String type = innerIfBlock.getType();
		if (type == null)
			throw new ConditionalFlowActivityException("Inner if block expression type is required.");
		validExpression = validateExpression(exchange, type, conditionalExpression, innerIfBlock.getConditionalValue());
		return validExpression;
	}// ..end of the method

	/**
	 * @param exchange
	 * @param innerElseIfBlock
	 * @return
	 * @throws ConditionalFlowActivityException
	 */
	private boolean validateInnerElseIfBlockExpression(Exchange exchange, InnerElseIfBlock innerElseIfBlock)
			throws ConditionalFlowActivityException {
		boolean validExpression = false;
		if (innerElseIfBlock == null)
			throw new ConditionalFlowActivityException("innerElseIfBlock is required.");
		String conditionalExpression = innerElseIfBlock.getConditionalExpression();
		if (conditionalExpression == null)
			throw new ConditionalFlowActivityException("conditionalExpression is required.");
		String type = innerElseIfBlock.getType();
		if (type == null)
			throw new ConditionalFlowActivityException("Inner else if block expression type is required.");
		validExpression = validateExpression(exchange, type, conditionalExpression,
				innerElseIfBlock.getConditionalValue());
		return validExpression;
	}// ..end of the method

	/**
	 * @param exchange
	 * @param type
	 * @param conditionalExpression
	 * @return
	 * @throws ConditionalFlowActivityException
	 */
	private boolean validateExpression(Exchange exchange, String type, String conditionalExpression,
			String conditionalValue) throws ConditionalFlowActivityException {
		boolean validExpression = false;
		String requestBody = exchange.getIn().getBody(String.class);
		if (type.equals("xpath"))
			validExpression = getXpathValidationResult(requestBody, exchange, conditionalExpression, conditionalValue);
		if (type.equals("mvel"))
			validExpression = getMvelValidationResult(requestBody, exchange, conditionalExpression);
		if (type.equals("groovy"))
			validExpression = getGroovyValidationResult(requestBody, exchange, conditionalExpression);
		return validExpression;
		// return true;
	}// ..end of the method

	/**
	 * @param requestBody
	 * @param exchange
	 * @param conditionalExpression
	 * @param conditionalValue
	 * @return
	 * @throws ConditionalFlowActivityException
	 */
	private boolean getXpathValidationResult(String requestBody, Exchange exchange, String conditionalExpression,
			String conditionalValue) throws ConditionalFlowActivityException {
		try {
			Document doc = convertStringToDocument(requestBody);
			return getXpathValues(doc, conditionalExpression, conditionalValue);
		} catch (Exception e) {
			throw new ConditionalFlowActivityException("Error while fetching Xpath values. " + e.getMessage());
		}
	}// ..end of the method

	/**
	 * @param requestBody
	 * @param exchange
	 * @param conditionalExpression
	 * @return
	 */
	private boolean getMvelValidationResult(String requestBody, Exchange exchange, String conditionalExpression) {
		Object mvelResult = MVEL.eval(conditionalExpression);
		return getBooleanValue(mvelResult);
	}// ..end of the method

	/**
	 * @param requestBody
	 * @param exchange
	 * @param conditionalExpression
	 * @return
	 */
	private boolean getGroovyValidationResult(String requestBody, Exchange exchange, String conditionalExpression) {
		Script scriptObj = groovyShell.parse(conditionalExpression);
		Object evaluate = scriptObj.run();
		logger.debug("evaluate : " + evaluate);
		return getBooleanValue(evaluate);
	}// ..end of the method

	/**
	 * @param object
	 * @return
	 */
	private boolean getBooleanValue(Object object) {
		if (object instanceof Boolean)
			return ((Boolean) object).booleanValue();
		;
		return false;
	}// ..end of the method

	/**
	 * @param doc
	 * @param xpathExp
	 * @param conditionalValue
	 * @return
	 * @throws ConditionalFlowActivityException
	 */
	private static boolean getXpathValues(Document doc, String xpathExp, String conditionalValue)
			throws ConditionalFlowActivityException {
		try {
			doc.getDocumentElement().normalize();
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.compile(xpathExp).evaluate(doc, XPathConstants.NODESET);
			if (conditionalValue != null && !conditionalValue.isEmpty()) {
				logger.debug("Validating " + xpathExp + " with " + conditionalValue);
				boolean flag = false;
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					if (node.getTextContent().equals(conditionalValue)) {
						flag = true;
						return flag;
					}
				}
				return flag;
			}
			int length = nodeList.getLength();
			if (length > 0)
				return true;
			else
				return false;
		} catch (XPathExpressionException e) {
			throw new ConditionalFlowActivityException("Error while compiling the Xpath. " + e.getMessage());
		}
	}// ..end of the method

	/**
	 * @param xmlStr
	 * @return
	 * @throws ConditionalFlowActivityException
	 */
	private static Document convertStringToDocument(String xmlStr) throws ConditionalFlowActivityException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
			return doc;
		} catch (Exception e) {
			throw new ConditionalFlowActivityException(
					"unable to parse the Xml String to Document due to " + e.getMessage());
		}
	}// end of the method.

}
