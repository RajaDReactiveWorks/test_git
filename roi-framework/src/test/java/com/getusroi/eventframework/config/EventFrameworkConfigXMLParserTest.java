package com.getusroi.eventframework.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.eventframework.jaxb.DispatchChanel;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.eventframework.jaxb.EventFramework;
import com.getusroi.eventframework.jaxb.EventSubscription;
import com.getusroi.eventframework.jaxb.SystemEvent;

public class EventFrameworkConfigXMLParserTest {
	final Logger logger = LoggerFactory.getLogger(EventFrameworkConfigXMLParserTest.class);
	
	private String goodConfigFile;
	private String invalidConfigFile;
	EventFrameworkXmlHandler parser;
	
	@Before
	public void getFileObject() throws EventFrameworkConfigParserException{
		parser=new EventFrameworkXmlHandler();
		InputStream inputstream= EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(EventFrameworkTestConstant.configfileToParse);
				
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			    
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath",e);
		}
		goodConfigFile=out1.toString();
		//Load bad File
		inputstream= EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(EventFrameworkTestConstant.invalidconfigfileToParse);
		reader = new BufferedReader(new InputStreamReader(inputstream));
		out1 = new StringBuilder();
		line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			    
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath",e);
		}
		invalidConfigFile=out1.toString();
	}
	
	/**
	 * Test wheter eventing.xml is loaded properly or not
	 * @throws EventFrameworkConfigParserException
	 */
	@Test
	public void testMarshallConfigxmlToObject() throws EventFrameworkConfigParserException{
		EventFramework eventFwkConfig=parser.marshallConfigXMLtoObject(goodConfigFile);
		Assert.assertNotNull("EventFramework Configuration xml should not be null and should be valid xml String",eventFwkConfig);
	}
	
	@Test
	public void testUnmarshallObjecttoXMLDispatchChanel() throws EventFrameworkConfigParserException{
		EventFramework eventFwkConfig=parser.marshallConfigXMLtoObject(goodConfigFile);
		List<DispatchChanel> chanelList=eventFwkConfig.getDispatchChanels().getDispatchChanel();
		DispatchChanel fileStoreChanel=null;
		for(DispatchChanel chanel:chanelList){
			if(chanel.getId().equalsIgnoreCase("FILE_STORE")){
				fileStoreChanel=chanel;
				break;
			}
		}
		String xmlString=parser.unmarshallObjecttoXML(fileStoreChanel);
		Assert.assertNotNull("Failed to convert DispatchChanel to respective XML",xmlString);
		
	}
	@Test
	public void testUnmarshallObjecttoXMLSystemEvent() throws EventFrameworkConfigParserException{
		EventFramework eventFwkConfig=parser.marshallConfigXMLtoObject(goodConfigFile);
		List<SystemEvent> sysEventList=eventFwkConfig.getSystemEvents().getSystemEvent();
		SystemEvent serviceCompEvent=null;
		for(SystemEvent sysEvent:sysEventList){
			if(sysEvent.getId().equalsIgnoreCase("SERVICE_COMPLETION_FAILURE")){
				serviceCompEvent=sysEvent;
				break;
			}
		}
		String xmlString=parser.unmarshallObjecttoXML(serviceCompEvent);
		Assert.assertNotNull("Failed to convert SystemEvent to respective XML",xmlString);
	}
	
	@Test
	public void testUnmarshallObjecttoXMLEvent() throws EventFrameworkConfigParserException{
		EventFramework eventFwkConfig=parser.marshallConfigXMLtoObject(goodConfigFile);
		List<Event> eventList=eventFwkConfig.getEvents().getEvent();
		Event serviceCompEvent=null;
		for(Event event:eventList){
			if(event.getId().equalsIgnoreCase("PRINT_SERVICE")){
				serviceCompEvent=event;
				break;
			}
		}
		String xmlString=parser.unmarshallObjecttoXML(serviceCompEvent);
		Assert.assertNotNull("Failed to convert Event to respective XML",xmlString);
	}
	
	@Test
	public void testUnmarshallThanMarshallOFEvent() throws EventFrameworkConfigParserException{
		EventFramework eventFwkConfig=parser.marshallConfigXMLtoObject(goodConfigFile);
		List<Event> eventList=eventFwkConfig.getEvents().getEvent();
		Event serviceCompEvent=null;
		for(Event event:eventList){
			if(event.getId().equalsIgnoreCase("PRINT_SERVICE")){
				serviceCompEvent=event;
				break;
			}
		}
		String xmlString=parser.unmarshallObjecttoXML(serviceCompEvent);
		Assert.assertNotNull("Failed to convert Event to respective XML",xmlString);
		
		EventFramework eventFwkConfig2=parser.marshallConfigXMLtoObject(xmlString);
		Assert.assertNotNull("EventFramework Failure converting Object to xml and than xml to back to Object",eventFwkConfig2);
	}
	@Test
	public void testUnmarshallThanMarshallOFEventSubscription() throws EventFrameworkConfigParserException{
		EventFramework eventFwkConfig=parser.marshallConfigXMLtoObject(goodConfigFile);
		List<EventSubscription> eventSubscriptionList=eventFwkConfig.getEventSubscriptions().getEventSubscription();
		EventSubscription testSubscription=null;
		for(EventSubscription evtSub:eventSubscriptionList){
			if(evtSub.getEventId().equalsIgnoreCase("PRINT_SERVICE")){
				testSubscription=evtSub;
				break;
			}
		}
		String xmlString=parser.unmarshallObjecttoXML(testSubscription);
		Assert.assertNotNull("Failed to convert EventSubscription to respective XML",xmlString);
		
		EventFramework eventFwkConfig2=parser.marshallConfigXMLtoObject(xmlString);
		Assert.assertNotNull("EventFramework Failure converting Object to xml and than xml to back to Object",eventFwkConfig2);
	}
}
