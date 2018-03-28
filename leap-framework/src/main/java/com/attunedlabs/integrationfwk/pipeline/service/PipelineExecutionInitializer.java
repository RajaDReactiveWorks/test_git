package com.attunedlabs.integrationfwk.pipeline.service;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class PipelineExecutionInitializer {
	private static final Logger logger = LoggerFactory.getLogger(PipelineExecutionInitializer.class);

	/**
	 * @param exchange
	 * @throws Exception
	 */
	public void decidePipelineRoute(Exchange exchange) throws Exception {
		logger.debug("entered into processRequest() method...");
		boolean isavalidXml = PipelineExecutionUtil.checkForXmlContent(exchange);
		String body = exchange.getIn().getBody(String.class);
		Document doc = null;
		PipelineExecution execution = new PipelineExecution();
		if (isavalidXml) {
			doc = PipelineExecutionUtil.convertStringToDocument(body);
		} else
			throw new PipeLineExecutionException("Request data is not a valid xml.");
		if (doc != null) {
			execution.setPipelineName(PipelineExecutionUtil.getPipelineNameFromDocument(exchange, doc));
			execution.setRequestDataDocument(PipelineExecutionUtil.createRequestDataDocument(exchange, doc));
			if (!execution.getPipelineName().isEmpty())
				exchange.getIn().setHeader(PipelineServiceConstant.EXECUTE_PIPELINE,
						PipelineServiceConstant.EXECUTE_PIPELINE_ROUTE);
			else {
				execution.setIntegerationPipesDocument(
						PipelineExecutionUtil.createIntegerationPipesDocument(exchange, doc));
				exchange.getIn().setHeader(PipelineServiceConstant.EXECUTE_PIPELINE,
						PipelineServiceConstant.EXECUTE_REQUEST_PIPELINE_ROUTE);
			}
			exchange.getIn().setHeader(PipelineServiceConstant.PIPELINE_EXECUTION, execution);
		} else
			throw new PipeLineExecutionException("Request data is not a valid xml..");
	}// end of the method.

	/**
	 * @param exchange
	 * @throws PipeLineExecutionException
	 */
	public void processPipelineRequest(Exchange exchange) throws PipeLineExecutionException {
		logger.debug("entered into processRequest() method...");
		boolean isavalidXml = PipelineExecutionUtil.checkForXmlContent(exchange);
		String body = exchange.getIn().getBody(String.class);
		PipelineExecution execution = exchange.getIn().getHeader(PipelineServiceConstant.PIPELINE_EXECUTION, PipelineExecution.class);
		Document doc = null;
		if (execution != null)
			try {
				if (isavalidXml) {
					doc = PipelineExecutionUtil.convertStringToDocument(body);
				} else
					throw new PipeLineExecutionException("Give a valid xml data.");
				if (doc != null) {
					String requestDataStr = PipelineExecutionUtil
							.convertDocumentToString(execution.getRequestDataDocument());
					exchange.getIn().setBody(requestDataStr);
					exchange.getIn().setHeader(PipelineServiceConstant.PIPELINE_NAME, execution.getPipelineName());
					exchange.getIn().removeHeader(PipelineServiceConstant.PIPELINE_EXECUTION);
				}
			} catch (PipeLineExecutionException e) {
				throw new PipeLineExecutionException("Error while processing the Pipeline : "
						+ execution.getPipelineName() + " due to " + e.getMessage());
			}
	}// end of the method.

	/**
	 * @param exchange
	 * @throws Exception
	 */
	public void processPipelineConfigRequest(Exchange exchange) throws Exception {
		logger.debug("entered into processRequest() method...");
		boolean isavalidXml = PipelineExecutionUtil.checkForXmlContent(exchange);
		String body = exchange.getIn().getBody(String.class);
		PipelineExecution execution = exchange.getIn().getHeader(PipelineServiceConstant.PIPELINE_EXECUTION, PipelineExecution.class);
		Document doc = null;
		if (execution != null)if (isavalidXml) {
			doc = PipelineExecutionUtil.convertStringToDocument(body);
		}
		if (doc != null) {
			String integerationPipesStr = PipelineExecutionUtil.convertDocumentToString(execution.getIntegerationPipesDocument());
			String requestDataStr = PipelineExecutionUtil.convertDocumentToString(execution.getRequestDataDocument());
			PipeLineExecutionHelper.unmarshallConfigXMLtoObject(exchange, integerationPipesStr,
					PipelineExecutionUtil.getPipelineName(exchange));
			exchange.getIn().setBody(requestDataStr);
			exchange.getIn().removeHeader(PipelineServiceConstant.PIPELINE_EXECUTION);
		}
	}// end of the method.

}
