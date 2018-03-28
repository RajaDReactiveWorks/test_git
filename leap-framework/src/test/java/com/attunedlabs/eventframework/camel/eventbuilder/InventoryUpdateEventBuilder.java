package com.attunedlabs.eventframework.camel.eventbuilder;

import org.apache.camel.Exchange;

import com.attunedlabs.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.Event;


public class InventoryUpdateEventBuilder extends AbstractCamelEventBuilder {

	@Override
	public LeapEvent buildEvent(Exchange camelExchange, Event eventConfig) {

		return null;
	}

}
