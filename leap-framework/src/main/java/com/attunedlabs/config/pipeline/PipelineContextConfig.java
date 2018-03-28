package com.attunedlabs.config.pipeline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.attunedlabs.integrationfwk.config.jaxb.IntegrationPipe;

public class PipelineContextConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private int level;
	private String sourceData;
	private IntegrationPipe integrationPipe;
	private List<String> requestList = new ArrayList<String>();

	public PipelineContextConfig(String name, int level, String sourceData, IntegrationPipe integrationPipe) {
		this();
		this.name = name;
		this.level = level;
		this.sourceData = sourceData;
		this.integrationPipe = integrationPipe;
	}

	private PipelineContextConfig() {
		super();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the sourceData
	 */
	public String getSourceData() {
		return sourceData;
	}

	/**
	 * @param sourceData
	 *            the sourceData to set
	 */
	public void setSourceData(String sourceData) {
		this.sourceData = sourceData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PipelineContextConfig [name=" + name + ", level=" + level + ", integrationPipe="
				+ integrationPipe.getPipeActivity().size() + "]";
	}

	/**
	 * @return the integrationPipe
	 */
	public IntegrationPipe getIntegrationPipe() {
		return integrationPipe;
	}

	/**
	 * @param integrationPipe
	 *            the integrationPipe to set
	 */
	public void setIntegrationPipe(IntegrationPipe integrationPipe) {
		this.integrationPipe = integrationPipe;
	}

	/**
	 * @return the requestList
	 */
	public List<String> getRequestList() {
		return requestList;
	}

	/**
	 * @param requestList
	 *            the requestList to set
	 */
	public void setRequestList(List<String> requestList) {
		this.requestList = requestList;
	}
}
