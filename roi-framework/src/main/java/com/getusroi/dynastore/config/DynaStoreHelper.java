package com.getusroi.dynastore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.RequestContext;
import com.getusroi.dynastore.config.jaxb.DynastoreConfiguration;
import com.getusroi.dynastore.config.jaxb.PublishEvent;
import com.getusroi.eventframework.config.EventFrameworkConfigurationException;
import com.getusroi.eventframework.config.IEventFrameworkConfigService;
import com.getusroi.eventframework.config.impl.EventFrameworkConfigService;
import com.getusroi.eventframework.event.DynastoreEvent;
import com.getusroi.eventframework.jaxb.Event;

public class DynaStoreHelper {
	protected static final Logger logger = LoggerFactory.getLogger(DynaStoreHelper.class);
	
	
	
	public DynastoreEvent entryAdditionPostHandler(DynastoreConfiguration config,RequestContext reqCtx)throws DynaStoreEventBuilderException {
		PublishEvent pubEvent=config.getPublishEvent();
		if(pubEvent==null)
			 return null;//no event Configured no post Processing Nothing Doing
		String onEntryAddedEventId=pubEvent.getOnEntryAdded();
		if(onEntryAddedEventId==null || onEntryAddedEventId.isEmpty())
			return null; //No <onEntryAdded> Event Configured so nothing doing
		String dynaStoreName=config.getDynastoreName().getValue();
		String dynaStoreVersion=config.getDynastoreName().getVersion();
		
		try {
			Event eventConfig=getEventForEventId(reqCtx,onEntryAddedEventId);
			if(eventConfig==null){
				throw new DynaStoreEventBuilderException("Failure to Build event on entryAddition for DynaStore{"+dynaStoreName+"} for eventId {"+onEntryAddedEventId+"} may be eventId not configured in eventing system");
			}
			DynastoreEvent entryAddedDynaEvent=new DynastoreEvent(onEntryAddedEventId,dynaStoreName,DynastoreEvent.EVENTTYPE_ONENTRY_ADDED,reqCtx);
			return entryAddedDynaEvent;
		} catch (EventFrameworkConfigurationException e) {
			logger.error("Failure to Build event on entryAddition for DynaStore{"+dynaStoreName+"} for eventId {"+onEntryAddedEventId+"}",e);
			throw new DynaStoreEventBuilderException("Failure to Build event on entryAddition for DynaStore{"+dynaStoreName+"} for eventId {"+onEntryAddedEventId+"}",e);
		}

	}
	public DynastoreEvent entryDeletionPostHandler(DynastoreConfiguration config,RequestContext reqCtx)throws DynaStoreEventBuilderException {
		PublishEvent pubEvent=config.getPublishEvent();
		if(pubEvent==null)
			 return null;//no event Configured no post Processing Nothing Doing
		String onEntryDeletedEventId=pubEvent.getOnEntryDeleted();
		if(onEntryDeletedEventId==null || onEntryDeletedEventId.isEmpty())
			return null; //No <onEntryAdded> Event Configured so nothing doing
		String dynaStoreName=config.getDynastoreName().getValue();
		String dynaStoreVersion=config.getDynastoreName().getVersion();
		
		try {
			Event eventConfig=getEventForEventId(reqCtx,onEntryDeletedEventId);
			if(eventConfig==null){
				throw new DynaStoreEventBuilderException("Failure to Build event on entryDeletion for DynaStore{"+dynaStoreName+"} for eventId {"+onEntryDeletedEventId+"} may be eventId not configured in eventing system");
			}
			DynastoreEvent entryDeletedDynaEvent=new DynastoreEvent(onEntryDeletedEventId,dynaStoreName,DynastoreEvent.EVENTTYPE_ONENTRY_DELETED,reqCtx);
			return entryDeletedDynaEvent;
		} catch (EventFrameworkConfigurationException e) {
			logger.error("Failure to Build event on entryDeletion for DynaStore{"+dynaStoreName+"} for eventId {"+onEntryDeletedEventId+"}",e);
			throw new DynaStoreEventBuilderException("Failure to Build event on entryAddition for DynaStore{"+dynaStoreName+"} for eventId {"+onEntryDeletedEventId+"}",e);
		}

	}
	
	public DynastoreEvent entryUpdationPostHandler(DynastoreConfiguration config,RequestContext reqCtx)throws DynaStoreEventBuilderException {
		PublishEvent pubEvent=config.getPublishEvent();
		if(pubEvent==null)
			 return null;//no event Configured no post Processing Nothing Doing
		String onEntryUpdatedEventId=pubEvent.getOnEntryUpdated();
		if(onEntryUpdatedEventId==null || onEntryUpdatedEventId.isEmpty())
			return null; //No <onEntryAdded> Event Configured so nothing doing
		String dynaStoreName=config.getDynastoreName().getValue();
		String dynaStoreVersion=config.getDynastoreName().getVersion();
		
		try {
			Event eventConfig=getEventForEventId(reqCtx,onEntryUpdatedEventId);
			if(eventConfig==null){
				throw new DynaStoreEventBuilderException("Failure to Build event on entryUpdation for DynaStore{"+dynaStoreName+"} for eventId {"+onEntryUpdatedEventId+"} may be eventId not configured in eventing system");
			}
			DynastoreEvent entryUpdatedDynaEvent=new DynastoreEvent(onEntryUpdatedEventId,dynaStoreName,DynastoreEvent.EVENTTYPE_ONENTRY_UPDATED,reqCtx);
			return entryUpdatedDynaEvent;
		} catch (EventFrameworkConfigurationException e) {
			logger.error("Failure to Build event on entryUpdation for DynaStore{"+dynaStoreName+"} for eventId {"+onEntryUpdatedEventId+"}",e);
			throw new DynaStoreEventBuilderException("Failure to Build event on entryUpdation for DynaStore{"+dynaStoreName+"} for eventId {"+onEntryUpdatedEventId+"}",e);
		}

	}
	
	public DynastoreEvent sessionTerminationPostHandler(DynastoreConfiguration config,RequestContext reqCtx)throws DynaStoreEventBuilderException {
		PublishEvent pubEvent=config.getPublishEvent();
		if(pubEvent==null)
			 return null;//no event Configured no post Processing Nothing Doing
		String sessionTerminationEventId=pubEvent.getOnTermination();
		if(sessionTerminationEventId==null || sessionTerminationEventId.isEmpty())
			return null; //No <onEntryAdded> Event Configured so nothing doing
		String dynaStoreName=config.getDynastoreName().getValue();
		String dynaStoreVersion=config.getDynastoreName().getVersion();
		
		try {
			Event eventConfig=getEventForEventId(reqCtx,sessionTerminationEventId);
			if(eventConfig==null){
				throw new DynaStoreEventBuilderException("Failure to Build event on session Termination for DynaStore{"+dynaStoreName+"} for eventId {"+sessionTerminationEventId+"} may be eventId not configured in eventing system");
			}
			DynastoreEvent sessionTerminatedDynaEvent=new DynastoreEvent(sessionTerminationEventId,dynaStoreName,DynastoreEvent.EVENTTYPE_ONTERMINATION,reqCtx);
			return sessionTerminatedDynaEvent;
		} catch (EventFrameworkConfigurationException e) {
			logger.error("Failure to Build event on sessionTermination for DynaStore{"+dynaStoreName+"} for eventId {"+sessionTerminationEventId+"}",e);
			throw new DynaStoreEventBuilderException("Failure to Build event on sessionTermination for DynaStore{"+dynaStoreName+"} for eventId {"+sessionTerminationEventId+"}",e);
		}

	}
	
	private Event getEventForEventId(RequestContext reqCtx,String eventId) throws EventFrameworkConfigurationException{
		IEventFrameworkConfigService eventConfigService = new EventFrameworkConfigService();
		Event eventConfig=eventConfigService.getEventConfiguration(reqCtx.getConfigurationContext(), eventId);
		return eventConfig;
		
	}
}
