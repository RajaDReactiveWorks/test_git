package com.attunedlabs.leap.integrationpipeline;

import java.util.List;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.pipeline.PipelineContext;
import com.attunedlabs.config.pipeline.PipelineContextConfig;
import com.attunedlabs.integrationfwk.activities.bean.ActivityConstant;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigUnit;
import com.attunedlabs.integrationfwk.config.jaxb.IntegrationPipe;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.pipeline.service.PipeLineExecutionHelper;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;

public class IntegrationPipelineRouteDecider {
	final static Logger logger = LoggerFactory.getLogger(IntegrationPipelineRouteDecider.class);

	/**
	 * To load all the pipeActivity from the given integration pipeline.
	 * 
	 * @param configName
	 * @param exchange
	 * @throws InitializingPipelineException
	 */
	public void processAllPipeActivity(Exchange exchange) throws InitializingPipelineException {
		logger.debug("processAllPipeActivity() from IntegrationPipelineRouteDecider...");
		IntegrationPipelineConfigUnit pipelineConfigUnit = (IntegrationPipelineConfigUnit) exchange.getIn()
				.getHeader(PipelineServiceConstant.PIPE_ACTIVITY_KEY_HEADER_KEY);
		IntegrationPipe integrationPipe = pipelineConfigUnit.getIntegrationPipe();
		try {
			PipeLineExecutionHelper.updatePipelineContext(exchange, integrationPipe, integrationPipe.getName());
		} catch (Exception e) {
			throw new InitializingPipelineException(e.getMessage(), e);
		}

	}// end of method processAllPipeActivity

	/**
	 * Loads the PipeActity one by one that are present in the pipeActivities.
	 * 
	 * @param exchange
	 * @throws Exception
	 */
	public void loadPipeActivity(Exchange exchange) throws Exception {
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		PipelineContext pipelineContext = leapHeader.getPipelineContext();
		int size = pipelineContext.getPipelineContextConfigs().size();
		if (size == 0) {
			logger.debug("pipelineContext data is empty");
			setNullActivity(exchange, pipelineContext, leapHeader);
			return;
		}
		int index = size - 1;
		String sendvalue = (String) exchange.getIn().getHeader(ActivityConstant.FILTER_RESULT_SEND_KEY);
		String dropvalue = (String) exchange.getIn().getHeader(ActivityConstant.FILTER_RESULT_DROP_KEY);
		logger.debug("send value : " + sendvalue + " , drop value : " + dropvalue);
		if (sendvalue == null && dropvalue == null) {
			setActualPipeActivity(exchange, index);
		} else if (sendvalue != null && !(sendvalue.isEmpty()) && sendvalue.equalsIgnoreCase("false")) {
			setNullActivity(exchange, pipelineContext, index, leapHeader);
		} else if (dropvalue != null && !(dropvalue.isEmpty()) && dropvalue.equalsIgnoreCase("true")) {
			setNullActivity(exchange, pipelineContext, index, leapHeader);
		} else {
			setActualPipeActivity(exchange, index);
		}
	}

	/**
	 * Sets the next pipeActivity in the header.
	 * 
	 * @param exchange
	 * @param pipeActivities
	 * @param pipelineContext
	 * @param index
	 * @param leapHeader
	 * @param pipelineContextConfig
	 * @param pipelineConfigList
	 * @throws Exception
	 */
	private void setActualPipeActivity(Exchange exchange, int index) throws Exception {
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		PipelineContext pipelineContext = leapHeader.getPipelineContext();
		List<PipelineContextConfig> pipelineContextConfigs = pipelineContext.getPipelineContextConfigs();
		PipelineContextConfig pipelineContextConfig = pipelineContextConfigs.get(index);
		IntegrationPipe integrationPipe = pipelineContextConfig.getIntegrationPipe();
		List<PipeActivity> pipeActivities = integrationPipe.getPipeActivity();
		PipeActivity pipeactivity = pipeActivities.get(0);
		String key = PipeLineExecutionHelper.getRouteKey(pipeactivity);
		logger.debug("Loading & Executing " + key + " from " + pipelineContextConfig.getName());
		exchange.getIn().setHeader(PipelineServiceConstant.ROUTE_DECIDER_KEY, key);
		exchange.getIn().setHeader(PipelineServiceConstant.PIPE_ACTIVITY_HEADER_KEY, pipeactivity);
		exchange.getIn().setHeader(PipelineServiceConstant.PIPELINE_NAME, pipelineContextConfig.getName());
		pipeActivities.remove(0);
		if (pipeActivities.isEmpty())
			pipelineContextConfigs.remove(index);
		pipelineContext.setPipelineContextConfigs(pipelineContextConfigs);
		pipelineContext.setPipeActivity(pipeactivity);
		pipelineContext.setPipelineName(pipelineContextConfig.getName());
		leapHeader.setPipelineContext(pipelineContext);
		exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
	}

	/**
	 * Sets the null value in the header to stop the execution.
	 * 
	 * @param exchange
	 */
	private void setNullActivity(Exchange exchange) {
		exchange.getIn().setHeader(PipelineServiceConstant.ROUTE_DECIDER_KEY, null);
		exchange.getIn().setHeader(PipelineServiceConstant.PIPE_ACTIVITY_HEADER_KEY, null);
	}

	/**
	 * Sets the null value in the header to stop the execution.
	 * 
	 * @param exchange
	 * @param leapHeader
	 * @param index
	 * @param pipelineContext
	 * @param pipeActivities
	 */
	private void setNullActivity(Exchange exchange, PipelineContext pipelineContext, int index, LeapHeader leapHeader) {
		setNullActivity(exchange);
		pipelineContext.getPipelineContextConfigs().clear();
		leapHeader.setPipelineContext(pipelineContext);
		exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
	}

	/**
	 * Sets the null value in the header to stop the execution.
	 * 
	 * @param exchange
	 * @param leapHeader
	 * @param index
	 * @param pipelineContext
	 * @param pipeActivities
	 */
	private void setNullActivity(Exchange exchange, PipelineContext pipelineContext, LeapHeader leapHeader) {
		setNullActivity(exchange);
		pipelineContext.getPipelineContextConfigs().clear();
		leapHeader.setPipelineContext(pipelineContext);
		exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
	}
}
