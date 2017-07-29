package com.getusroi.integrationfwk.activities.bean;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.integrationfwk.config.IntegrationPipelineConfigUnit;
import com.getusroi.integrationfwk.config.jaxb.IntegrationPipe;
import com.getusroi.integrationfwk.config.jaxb.PipeActivity;


public class PipelineExchangeInitializer {

	private Logger looger = (Logger) LoggerFactory.getLogger(PipelineExchangeInitializer.class.getName());

	/**
	 * bean method called ffrom the IntegrationPipeactivity, to boot the pipe
	 * configs to the exchange headers, with its size
	 * 
	 * @param exchange
	 */
	public void processPipelineInit(Exchange exchange) {
		looger.debug("The exchange headers: " + exchange.getIn().getHeaders() + " - ExchangeId: "
				+ exchange.getExchangeId());
		getPipeActivitySize(exchange);

	}// ..end of the method

	/**
	 * Getting the size of the pipeactivity , called in the processPipelineInit
	 * 
	 * @param exchange
	 */
	private void getPipeActivitySize(Exchange exchange) {
		looger.debug(".gettingPipeactivitiesSize()....");
		IntegrationPipelineConfigUnit pipelineConfigUnit = (IntegrationPipelineConfigUnit) exchange.getIn()
				.getHeader("pipeActivityKey");
		IntegrationPipe integrationPipe = pipelineConfigUnit.getIntegrationPipe();
		java.util.List<PipeActivity> pipeactivityList = integrationPipe.getPipeActivity();
		int sizeOfpipeActivities = pipeactivityList.size();
		looger.debug("Size of pipes in " + exchange.getExchangeId() + ": " + sizeOfpipeActivities);
		exchange.getIn().setHeader("counter", sizeOfpipeActivities);
	}// ..end of the method

}
