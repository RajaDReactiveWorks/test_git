package com.getusroi.eventframework.camel.eventbuilder;

import org.apache.camel.Exchange;

import com.getusroi.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.getusroi.eventframework.event.ROIEvent;
import com.getusroi.eventframework.jaxb.Event;


public class InventoryUpdateEventBuilder extends AbstractCamelEventBuilder {

	@Override
	public ROIEvent buildEvent(Exchange camelExchange, Event eventConfig) {

		return null;
	}

}
