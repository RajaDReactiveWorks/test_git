package com.attunedlabs.eventframework.camel.eventproducer;

import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;

import oracle.net.aso.l;

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
	 * @param toLeapEvent
	 *           : LeapEvent
	 */
	protected void updateStandardCamelHeader(Exchange fromCamelExchange, LeapEvent toLeapEvent) {
		String routeId = fromCamelExchange.getFromRouteId();
		String contextStr = fromCamelExchange.getContext().getName();
		/* #TODO Match the tenantId. keeping default for now */
		LeapHeader leapHeader = (LeapHeader) fromCamelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		if(leapHeader != null)
			toLeapEvent.addEventHeader(LeapHeaderConstant.REQUEST_ID_KEY, leapHeader.getRequestUUID());
			
		toLeapEvent.addEventHeader(LeapHeaderConstant.TENANT_KEY, getTenantId(fromCamelExchange));
		toLeapEvent.addEventHeader(LeapHeaderConstant.SITE_KEY, leapHeader.getSite());
		toLeapEvent.addEventHeader(CamelEventProducerConstant.CAMEL_CONTEXT_ID, contextStr);
		toLeapEvent.addEventHeader(CamelEventProducerConstant.CAMEL_ROUTER_ID, routeId);
		toLeapEvent.addEventHeader(CamelEventProducerConstant.CAMEL_TIMESTAMP, new Date());
	}
	
	protected LeapEvent updateStandardCamelHeader(String eventId,Exchange fromCamelExchange) {
		//LeapEvent toLeapEvent
		RequestContext reqCtx=getRequestContextFromCamelExchange(fromCamelExchange);
		LeapEvent toLeapEvent=new LeapEvent(eventId,reqCtx);
		updateStandardCamelHeader(fromCamelExchange,toLeapEvent);
		return toLeapEvent;
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
		LeapHeader leapHeader = (LeapHeader) fromCamelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);

		String tenantId = leapHeader.getTenant();
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

		LeapHeader leapHeader = (LeapHeader) fromCamelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		String serviceType = leapHeader.getServicetype();
		logger.debug("servicetype : " + serviceType);
		return serviceType;
	}

	/**
	 * This method is to update failureCamelHeader
	 * 
	 * @param fromCamelExchange
	 *           : camel exchange
	 * @param toLeapEvent
	 *           : LeapEvent
	 */
	protected void updateFailureCamelHeader(Exchange fromCamelExchange, LeapEvent toLeapEvent) {
		Map<String, Object> excProp = fromCamelExchange.getProperties();
		String failureEndPoint = (String) excProp.get("CamelFailureEndpoint");
		Exception expCaught = (Exception) excProp.get("CamelExceptionCaught");
		Boolean wasRollbacked = (Boolean) excProp.get("CamelRollbackOnly");

		toLeapEvent.addEventHeader(CamelEventProducerConstant.CAMEL_FAILED_ENDPONT, failureEndPoint);
		toLeapEvent.addEventHeader(CamelEventProducerConstant.CAMEL_FAILURE_MSG, expCaught);
		toLeapEvent.addEventHeader(CamelEventProducerConstant.CAMEL_ROUTE_ROLLBACK, wasRollbacked);
	}

	/**
	 * This method is to get event name
	 * 
	 * @param fromCamelExchange
	 * @return String : event name
	 */
	protected String getEventId(Exchange fromCamelExchange) {
		LeapHeader leapEventHeader = (LeapHeader) fromCamelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);

		Map<String, Object> leapHeaderMap = leapEventHeader.getGenricdata();

		String eventname = (String) leapHeaderMap.get("eventid");
		logger.debug("eventid name : " + eventname);
		if (eventname == null || eventname.isEmpty())
			return "UNKOWN";
		return eventname;

	}
	
	protected RequestContext getRequestContextFromCamelExchange(Exchange fromCamelExchange){
		LeapHeader leapEventHeader = (LeapHeader) fromCamelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		return leapEventHeader.getRequestContext();
	}

}
