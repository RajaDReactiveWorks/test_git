package com.attunedlabs.eventframework.camel.eventproducer;

import org.apache.camel.Exchange;

import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.Event;

public interface ICamelEventBuilder {

	public LeapEvent buildEvent(Exchange camelExchange,Event eventConfig);

}
