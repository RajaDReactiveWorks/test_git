package com.attunedlabs.eventframework.camel.eventbuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.camel.eventproducer.CamelEventProducerConstant;
import com.attunedlabs.eventframework.jaxb.CamelEventProducer;


/**
 * This class is to helper class for building and event
 * @author Deepali
 *
 */
//#TODO Review comments Change the implementation of this class.
public class EventBuilderHelper {
	
	protected static final Logger logger = LoggerFactory.getLogger(EventBuilderHelper.class);

	
	
	
	/**
	 * This method is to check CamelBuilder type is OGNL or not
	 * @param evtProdConfig : CamelEventProducer Object
	 * @return boolean
	 */
	public static boolean isOgnlBuilderType(CamelEventProducer evtProdConfig){
		logger.debug("inside eventSourceBasedOnType method of EventBuilderHelper ");
		
		String buildertype=evtProdConfig.getCamelEventBuilder().getType();
		if(buildertype.equalsIgnoreCase(CamelEventProducerConstant.OGNL_EVENT_BUILDER)){
			return true;
		}else{
			return false;
		}
		
		
	}//end of eventSourceBasedOnType method

	
	/**
	 * This method is to get internal configuration key
	 * @param tenantid : tenant in String
	 * @return internal configuration key in String
	 */
	private static String getInternalEventKey(String tenantid) {

		logger.debug("inside getInternalEventKey method in EventConfiguration");
		if (tenantid == null && tenantid.isEmpty()) {
			tenantid = "default";
		}

		String internalgroupkey=tenantid+"-"+CamelEventProducerConstant.INTERNAL_GROUP_KEY;
		
		return internalgroupkey;

	}
}
