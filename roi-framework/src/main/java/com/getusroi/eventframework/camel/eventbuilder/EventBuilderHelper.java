package com.getusroi.eventframework.camel.eventbuilder;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.beans.ConfigurationUnit;
import com.getusroi.config.server.ConfigServerInitializationException;
import com.getusroi.config.server.ROIConfigurationServer;
import com.getusroi.eventframework.camel.eventproducer.CamelEventProducerConstant;
import com.getusroi.eventframework.jaxb.CamelEventProducer;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.eventframework.jaxb.Source;
import com.getusroi.mesh.MeshHeader;
import com.getusroi.mesh.MeshHeaderConstant;


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
