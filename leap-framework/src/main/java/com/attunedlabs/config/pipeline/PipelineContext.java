package com.attunedlabs.config.pipeline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;

public class PipelineContext implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String pipelineName;
	private PipeActivity pipeActivity;
	private List<PipelineContextConfig> pipelineContextConfigs;

	public PipelineContext() {
		super();
		pipelineContextConfigs = new ArrayList<>();
	}

	/**
	 * @return the pipelineName
	 */
	public String getPipelineName() {
		return pipelineName;
	}

	/**
	 * @return the pipeActivity
	 */
	public PipeActivity getPipeActivity() {
		return pipeActivity;
	}

	/**
	 * @param pipelineName
	 *            the pipelineName to set
	 */
	public void setPipelineName(String pipelineName) {
		this.pipelineName = pipelineName;
	}

	/**
	 * @param pipeActivity
	 *            the pipeActivity to set
	 */
	public void setPipeActivity(PipeActivity pipeActivity) {
		this.pipeActivity = pipeActivity;
	}

	/**
	 * @return the contextConfigs
	 */
	public List<PipelineContextConfig> getPipelineContextConfigs() {
		return pipelineContextConfigs;
	}

	/**
	 * @param contextConfigs
	 *            the contextConfigs to set
	 */
	public void setPipelineContextConfigs(List<PipelineContextConfig> contextConfigs) {
		this.pipelineContextConfigs = contextConfigs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[pipelineName=" + pipelineName + ", pipelineContextConfigs : " + pipelineContextConfigs + "]";
	}

}
