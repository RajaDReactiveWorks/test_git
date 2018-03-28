package com.attunedlabs.leap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.pipeline.PipelineContext;

public class LeapHeaderCloneProcessor implements Processor {

	private static Logger logger = LoggerFactory.getLogger(LeapHeaderCloneProcessor.class.getName());

	/* (non-Javadoc)
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		logger.debug("Deep Cloning of the LeapHeader...");
		LeapHeader header = exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY, LeapHeader.class);
		LeapHeader deepClone = header.deepClone();
		deepClone.setPipelineContext(new PipelineContext());
		exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, deepClone);
	}// end of the method.
}
