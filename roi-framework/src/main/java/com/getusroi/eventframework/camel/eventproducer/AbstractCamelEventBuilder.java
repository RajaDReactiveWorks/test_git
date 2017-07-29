package com.getusroi.eventframework.camel.eventproducer;

import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.RequestContext;
import com.getusroi.eventframework.event.ROIEvent;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.mesh.MeshHeader;
import com.getusroi.mesh.MeshHeaderConstant;

/**
 * 
 * @author bizruntime
 *
 */
public abstract class AbstractCamelEventBuilder implements ICamelEventBuilder{
	
	protected static final Logger logger = LoggerFactory.getLogger(AbstractCamelEventBuilder.class);

	
	/**
	 * This method is to update Standard Camel header
	 * 
	 * @param fromCamelExchange
	 *           : Camel exchange
	 * @param toROIEvent
	 *           : ROIEvent
	 */
	protected void updateStandardCamelHeader(Exchange fromCamelExchange, ROIEvent toROIEvent) {
		String routeId = fromCamelExchange.getFromRouteId();
		String contextStr = fromCamelExchange.getContext().getName();
		/* #TODO Match the tenantId. keeping default for now */
		String tenantId = (String) fromCamelExchange.getIn().getHeader("tenantid");
		toROIEvent.addEventHeader(CamelEventProducerConstant.CAMEL_CONTEXT_ID, contextStr);
		toROIEvent.addEventHeader(CamelEventProducerConstant.TENANT_ID, tenantId);
		toROIEvent.addEventHeader(CamelEventProducerConstant.CAMEL_ROUTER_ID, routeId);
		toROIEvent.addEventHeader(CamelEventProducerConstant.CAMEL_TIMESTAMP, new Date());
		String requestId = (String) fromCamelExchange.getIn().getHeader(CamelEventProducerConstant.REQUEST_UID);
		toROIEvent.addEventHeader(CamelEventProducerConstant.REQUEST_UID,requestId);
	}
	
	protected ROIEvent updateStandardCamelHeader(String eventId,Exchange fromCamelExchange) {
		//ROIEvent toROIEvent
		RequestContext reqCtx=getRequestContextFromCamelExchange(fromCamelExchange);
		ROIEvent toROIEvent=new ROIEvent(eventId,reqCtx);
		updateStandardCamelHeader(fromCamelExchange,toROIEvent);
		return toROIEvent;
	}
	
	/**
	 * This method is to set tenantid
	 * 
	 * @param fromCamelExchange
	 *           : Camel Exchange
	 * @return String :tenantid
	 */
	protected String getTenantId(Exchange fromCamelExchange) {
		logger.debug(".getTenantId method of AbstractCamelEventBuilder");
		MeshHeader meshHeader = (MeshHeader) fromCamelExchange.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);

		String tenantId = meshHeader.getTenant();
		logger.debug("tenantid : " + tenantId);
		if (tenantId == null || tenantId.isEmpty())
			return "default";
		return tenantId;
	}

	/**
	 * This method is used to get service type
	 * 
	 * @param fromCamelExchange
	 *           :Camel Exchange
	 * @return String : service type
	 */
	protected String getServiceType(Exchange fromCamelExchange) {
		logger.debug(".getTenantId method of AbstractCamelEventBuilder");

		MeshHeader meshHeader = (MeshHeader) fromCamelExchange.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);
		String serviceType = meshHeader.getServicetype();
		logger.debug("servicetype : " + serviceType);
		return serviceType;
	}

	/**
	 * This method is to update failureCamelHeader
	 * 
	 * @param fromCamelExchange
	 *           : camel exchange
	 * @param toROIEvent
	 *           : ROIEvent
	 */
	protected void updateFailureCamelHeader(Exchange fromCamelExchange, ROIEvent toROIEvent) {
		Map<String, Object> excProp = fromCamelExchange.getProperties();
		String failureEndPoint = (String) excProp.get("CamelFailureEndpoint");
		Exception expCaught = (Exception) excProp.get("CamelExceptionCaught");
		Boolean wasRollbacked = (Boolean) excProp.get("CamelRollbackOnly");

		toROIEvent.addEventHeader(CamelEventProducerConstant.CAMEL_FAILED_ENDPONT, failureEndPoint);
		toROIEvent.addEventHeader(CamelEventProducerConstant.CAMEL_FAILURE_MSG, expCaught);
		toROIEvent.addEventHeader(CamelEventProducerConstant.CAMEL_ROUTE_ROLLBACK, wasRollbacked);
	}

	/**
	 * This method is to get event name
	 * 
	 * @param fromCamelExchange
	 * @return String : event name
	 */
	protected String getEventId(Exchange fromCamelExchange) {
		MeshHeader meshEventHeader = (MeshHeader) fromCamelExchange.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);

		Map<String, Object> meshHeaderMap = meshEventHeader.getGenricdata();

		String eventname = (String) meshHeaderMap.get("eventid");
		logger.debug("eventid name : " + eventname);
		if (eventname == null || eventname.isEmpty())
			return "UNKOWN";
		return eventname;

	}
	
	protected RequestContext getRequestContextFromCamelExchange(Exchange fromCamelExchange){
		MeshHeader meshEventHeader = (MeshHeader) fromCamelExchange.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);
		return meshEventHeader.getRequestContext();
	}

}
