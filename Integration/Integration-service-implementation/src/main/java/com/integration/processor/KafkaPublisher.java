package com.integration.processor;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.abstractbean.AbstractLeapCamelBean;
import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;

public class KafkaPublisher extends AbstractLeapCamelBean{
	private final static Logger logger = LoggerFactory.getLogger(KafkaPublisher.class.getName());
	public KafkaPublisher() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processBean(Exchange arg0) throws Exception {
		logger.debug("inside KafkaPublisher processBean");
		
	}

}
