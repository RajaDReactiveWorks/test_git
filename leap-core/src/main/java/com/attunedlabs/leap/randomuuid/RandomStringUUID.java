package com.attunedlabs.leap.randomuuid;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;



public class RandomStringUUID {
	final Logger logger = LoggerFactory.getLogger(RandomStringUUID.class);

	/**
	 * This method is to generate random uuid for request
	 * @param exchange :Exchange Object
	 */
	public  void uuidgenrate(Exchange exchange){
     logger.debug(".uuidgenerate() of RandomStringUUID");
     
     //generating the random uuid
		  UUID uuid = UUID.randomUUID();
	        String randomUUIDString = uuid.toString();
	        logger.debug("random uuid"+randomUUIDString);
	        
	        LeapHeader leapHeader=(LeapHeader)exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
	        
	        leapHeader.setRequestUUID(randomUUIDString);
	     
		
		
	}//end of method

}
