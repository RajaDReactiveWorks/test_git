package com.attunedlabs.config.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.beans.ConfigurationUnit;
//import com.attunedlabs.config.test.ConfigServerTest;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;

public abstract class LeapConfigurationListener implements EntryAddedListener<String, ConfigurationUnit>,EntryRemovedListener<String,ConfigurationUnit>,
												 EntryUpdatedListener<String, ConfigurationUnit>,EntryEvictedListener<String, ConfigurationUnit>, 
												 MapEvictedListener,MapClearedListener{
	boolean ifStatusOnlyChanged=true;
	protected static final Logger logger = LoggerFactory.getLogger(LeapConfigurationListener.class);
	
	public final void entryAdded(EntryEvent<String, ConfigurationUnit> event) {
		//logger.debug("HC.entryAdded-"+event);
		LeapBaseConfigEvent leapConfigEvt=buildBaseEvent(event,LeapBaseConfigEvent.EVT_CONFIGURATION_ADDED);
		configAdded(leapConfigEvt);
	}

	public final void entryRemoved(EntryEvent<String, ConfigurationUnit> event) {
		//logger.debug("HC.entryRemoved-"+event);
		LeapBaseConfigEvent leapConfigEvt=buildBaseEvent(event,LeapBaseConfigEvent.EVT_CONFIGURATION_REMOVED);
		configRemoved(leapConfigEvt);
	}

	public final void entryUpdated(EntryEvent<String, ConfigurationUnit> event) {
		//logger.debug("HC.entryUpdated-"+event);
		LeapBaseConfigEvent leapConfigEvt=buildBaseEvent(event,LeapBaseConfigEvent.EVT_CONFIGURATION_UPDATED);
		
		if(ifStatusOnlyChanged){
			leapConfigEvt.setEventType(LeapBaseConfigEvent.EVT_CONFIGURATION_STATUSCHANGED);
			configStatusChanged(leapConfigEvt);
		}else{ 
			configUpdated(leapConfigEvt);
		}
	}

	
	public final void entryEvicted(EntryEvent<String, ConfigurationUnit> event) {
		logger.debug("Entry Evicted:" + event);
	}

	public final void mapEvicted(MapEvent event) {
		logger.debug("Map Evicted:" + event);
	}

	public final void mapCleared(MapEvent event) {
		logger.debug("Map Cleared:" + event);
	}
	
	private LeapBaseConfigEvent buildBaseEvent(EntryEvent<String, ConfigurationUnit> event,String eventType){
		logger.debug(".buildBaseEvent-"+event);
		String configKey=event.getKey();
		String configName=event.getName();
		ConfigurationUnit value=null;
		ConfigurationUnit value1=null;
		
		String sourcEventType=event.getEventType().name();
		System.out.println("source event type : "+sourcEventType);
		if(sourcEventType.equalsIgnoreCase("REMOVED")){
			value=(ConfigurationUnit)event.getOldValue();
			
		}else if(sourcEventType.equalsIgnoreCase("UPDATED")){
			
			
			value1=(ConfigurationUnit)event.getOldValue();
			value=(ConfigurationUnit)event.getValue();
			boolean bool1=value.getIsEnabled();
			boolean bool2=value1.getIsEnabled();
			if(bool1==bool2){
				ifStatusOnlyChanged=false;
				
			}
			else{
				
				System.out.println("");
			}
			
		}else{
			value=(ConfigurationUnit)event.getValue();
		}
		String tenantId=value.getTenantId();
			
		LeapBaseConfigEvent leapConfigEvt=new LeapBaseConfigEvent(tenantId, value,eventType);
		leapConfigEvt.setConfigGroup(configName);
		leapConfigEvt.setConfigName(configKey);
		return leapConfigEvt;
	}
	
	public abstract void configAdded(LeapBaseConfigEvent configAddedEvent);
	public abstract void configUpdated(LeapBaseConfigEvent configUpdatedEvent);
	public abstract void configRemoved(LeapBaseConfigEvent configRemovedEvent);
	public abstract void configStatusChanged(LeapBaseConfigEvent configStatusChangedEvent);
	
}
