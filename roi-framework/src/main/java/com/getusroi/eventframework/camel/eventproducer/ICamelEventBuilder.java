package com.getusroi.eventframework.camel.eventproducer;

import org.apache.camel.Exchange;

import com.getusroi.eventframework.event.ROIEvent;
import com.getusroi.eventframework.jaxb.Event;

public interface ICamelEventBuilder {

	public ROIEvent buildEvent(Exchange camelExchange,Event eventConfig);

}
