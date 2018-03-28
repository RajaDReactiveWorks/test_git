package com.attunedlabs.leap.base;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComputeTimeBean implements Processor {
	final static Logger log = LoggerFactory.getLogger(ComputeTimeBean.class);

	long startTime;
	long endTime;

	@Override
	public void process(Exchange exchange) throws Exception {
		startTime = System.currentTimeMillis();
		log.debug("started pipeline time: " + startTime);
	}

	public void processEndTime(Exchange exchange) throws Exception {
		endTime = System.currentTimeMillis();
		log.debug("finished pipeline time: " + endTime);
		log.debug("total processing pipelinetime required : " + (endTime - startTime) + "ms");

	}

}
