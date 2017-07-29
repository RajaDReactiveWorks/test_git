package com.getusroi.eventframework.config;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.getusroi.eventframework.jaxb.DispatchChanel;
import com.getusroi.eventframework.jaxb.DispatchChanels;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.eventframework.jaxb.EventFramework;
import com.getusroi.eventframework.jaxb.EventSubscription;
import com.getusroi.eventframework.jaxb.EventSubscriptions;
import com.getusroi.eventframework.jaxb.Events;
import com.getusroi.eventframework.jaxb.SystemEvent;
import com.getusroi.eventframework.jaxb.SystemEvents;
import com.getusroi.feature.config.FeatureConfigParserException;
import com.getusroi.feature.jaxb.FeaturesServiceInfo;

/**
 * This class is to read xsd and validate xml against xsd.Also generate java classes for XSD
 * @author ubuntu
 *
 */

public class EventFrameworkXmlHandler {
	final Logger logger = LoggerFactory.getLogger(EventFrameworkXmlHandler.class);
		
	/**
	 * this method is to validate xml against xsd defined
	 * @param url : URL Object to get xml path
	 * @throws EventingXmlXSDValidationException
	 * @throws EventFrameworkXSDLoadingException
	 * @throws EventFrameworkConfigParserException
	 */
	private  void validateXml(String configXMLFile) throws EventFrameworkConfigParserException{
		try {
			logger.debug("Custom Error Handler while Validating XML against XSD");
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
             Schema schema = factory.newSchema(EventFrameworkXmlHandler.class.getClassLoader().getResource(EventFrameworkConstants.SCHEMA_NAME));
            Validator validator = schema.newValidator();
            
            StringReader stringReader=new StringReader(configXMLFile);
 	         validator.validate(new StreamSource(stringReader));
           
            logger.debug("Validation is successful");
        } catch (IOException | SAXException e) {
      	  logger.error("Exception while validating xml against schema", e);
           throw new EventFrameworkConfigParserException("EventFramework Config XML schema validation failed",e);
        }
 	}
	
	
	public EventFramework marshallConfigXMLtoObject(String configXMLFile)throws EventFrameworkConfigParserException{
			validateXml(configXMLFile);
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(EventFramework.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				 InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));
				 EventFramework evtFwkConfig=(EventFramework)jaxbUnmarshaller.unmarshal(inputSourceConfigXml);
				 return evtFwkConfig;
			} catch (JAXBException e) {
				throw new EventFrameworkConfigParserException("EventFramework Config XML parsing failed for file",e);
			}
	}
	
	
	public EventFramework marshallXMLtoObject(String eventConfigxml) throws EventFrameworkConfigParserException{
		try {
			
			logger.debug(".marshallXMLtoObject() xmlString is ["+eventConfigxml+"]");
			JAXBContext jaxbContext = JAXBContext.newInstance(com.getusroi.eventframework.jaxb.ObjectFactory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			EventFramework eventFramework=(EventFramework)jaxbUnmarshaller.unmarshal(new StringReader(eventConfigxml));
			return eventFramework;
			
		  } catch (Exception exp) {
			  logger.error("EventConfig XMLParsing Failed ",exp);
		      throw new EventFrameworkConfigParserException("EventConfig XMLParsing Failed ",exp);
		  }
	}
	
	public String unmarshallObjecttoXML(DispatchChanel dispatchChanelConfig) throws EventFrameworkConfigParserException{
			EventFramework evtFramework=new EventFramework();
			DispatchChanels dispatchChanels=new DispatchChanels();
			List<DispatchChanel> dispatchChanelList=dispatchChanels.getDispatchChanel();
			dispatchChanelList.add(dispatchChanelConfig);
			//add Dispatch Chanel to event Framework
			evtFramework.setDispatchChanels(dispatchChanels);
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(EventFramework.class);
				Marshaller marshaller = jaxbContext.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				StringWriter writer = new StringWriter();
				marshaller.marshal(evtFramework,writer);
				String theXML = writer.toString();
				logger.debug("DispatchChanel ObjecttoXML()=["+theXML+"]");
				return theXML;
			} catch (JAXBException e) {
				throw new EventFrameworkConfigParserException("Failed to convert DispatchChanel to xml for dispatchChanel{ID:"+dispatchChanelConfig.getId()+"}",e);
			}
	}
	
	public String unmarshallObjecttoXML(SystemEvent systemEventConfig) throws EventFrameworkConfigParserException{
		EventFramework evtFramework=new EventFramework();
		SystemEvents sysEvents=new SystemEvents();
		List<SystemEvent> sysEventList=sysEvents.getSystemEvent();
		sysEventList.add(systemEventConfig);
		//add SystemEvents to event Framework
		evtFramework.setSystemEvents(sysEvents);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(EventFramework.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(evtFramework,writer);
			String theXML = writer.toString();
			logger.debug("SystemEvent ObjecttoXML()=["+theXML+"]");
			return theXML;
		} catch (JAXBException e) {
			throw new EventFrameworkConfigParserException("Failed to convert SystemEvent to xml for systemEvent{ID:"+systemEventConfig.getId()+"}",e);
		}
	}
	
	public String unmarshallObjecttoXML(Event eventConfig) throws EventFrameworkConfigParserException{
		
		EventFramework evtFramework=new EventFramework();
		Events events=new Events();
		List<Event> eventList=events.getEvent();
		eventList.add(eventConfig);
		evtFramework.setEvents(events);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(EventFramework.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(evtFramework,writer);
			String theXML = writer.toString();
			logger.debug("Event ObjecttoXML()=["+theXML+"]");
			return theXML;
		} catch (JAXBException e) {
			throw new EventFrameworkConfigParserException("Failed to convert Event to xml for Event{ID:"+eventConfig.getId()+"}",e);
		}
	}
	
public String unmarshallObjecttoXML(EventSubscription eventSubscription) throws EventFrameworkConfigParserException{
		
		EventFramework evtFramework=new EventFramework();
		EventSubscriptions eventSubscriptions=new EventSubscriptions();
		List<EventSubscription> eventSubscriptionList=eventSubscriptions.getEventSubscription();
		eventSubscriptionList.add(eventSubscription);
		evtFramework.setEventSubscriptions(eventSubscriptions);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(EventFramework.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(evtFramework,writer);
			String theXML = writer.toString();
			logger.debug("Event ObjecttoXML()=["+theXML+"]");
			return theXML;
		} catch (JAXBException e) {
			throw new EventFrameworkConfigParserException("Failed to convert EventSubscription to xml for Event{ID:"+eventSubscription.getEventId()+"}",e);
		}
	}
	
	private void validateMarshallRequest(Object obj,String msg) throws EventFrameworkConfigParserException{
		if(obj==null)
			throw new EventFrameworkConfigParserException(msg);
	}
}
