package com.attunedlabs.integrationfwk.pipeline.service;

import org.w3c.dom.Document;

public class PipelineExecution {
	private Document integerationPipesDocument = null;
	private Document requestDataDocument = null;
	private String pipelineName;
	/**
	 * @return the integerationPipesDocument
	 */
	public Document getIntegerationPipesDocument() {
		return integerationPipesDocument;
	}
	/**
	 * @param integerationPipesDocument the integerationPipesDocument to set
	 */
	public void setIntegerationPipesDocument(Document integerationPipesDocument) {
		this.integerationPipesDocument = integerationPipesDocument;
	}
	/**
	 * @return the requestDataDocument
	 */
	public Document getRequestDataDocument() {
		return requestDataDocument;
	}
	/**
	 * @param requestDataDocument the requestDataDocument to set
	 */
	public void setRequestDataDocument(Document requestDataDocument) {
		this.requestDataDocument = requestDataDocument;
	}
	/**
	 * @return the pipelineName
	 */
	public String getPipelineName() {
		return pipelineName;
	}
	/**
	 * @param pipelineName the pipelineName to set
	 */
	public void setPipelineName(String pipelineName) {
		this.pipelineName = pipelineName;
	}
	
}
