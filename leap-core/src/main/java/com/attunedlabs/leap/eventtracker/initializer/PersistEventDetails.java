package com.attunedlabs.leap.eventtracker.initializer;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerException;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;

/**
 * <code>PersistEventDetails</code> is called from exit route for persisting
 * event details for newly created event List which is still in transaction so
 * the persist operation will also be in transaction until the commit invoked by
 * JTATransactionManager.
 * 
 * @author Reactiveworks42
 *
 */
public class PersistEventDetails  {
	final Logger logger = LoggerFactory.getLogger(PersistEventDetails.class);
	public static final String REQUEST_ID = "REQUEST_ID";

	/**
	 * adding the event details with status NEW and Event_Created_DTM will be
	 * current system time.
	 * 
	 * @throws EventDispatcherTrackerException
	 * 
	 */
	public void processBean(Exchange camelExchange) throws EventDispatcherTrackerException {
		logger.debug("inside processBean... to PersistEventDetails ");
		LeapHeader leapHeader = (LeapHeader) camelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		IEventDispatcherTrackerService eventDispatcherTrackerService = new EventDispatcherTrackerImpl();
		boolean addedStatus = eventDispatcherTrackerService.addEventTracking(leapHeader.getTenant(),
				leapHeader.getSite(), leapHeader.getRequestUUID(), leapHeader.getRequestUUID(), camelExchange);
		logger.debug("added the event to track ... " + addedStatus);
		camelExchange.getIn().setHeader(REQUEST_ID,leapHeader.getRequestUUID());
		

	}

}
