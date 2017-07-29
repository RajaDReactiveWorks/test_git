package com.getusroi.config.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.beans.ConfigurationUnit;
//import com.getusroi.config.test.ConfigServerTest;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;

public abstract class ROIConfigurationListener implements EntryAddedListener<String, ConfigurationUnit>,EntryRemovedListener<String,ConfigurationUnit>,
												 EntryUpdatedListener<String, ConfigurationUnit>,EntryEvictedListener<String, ConfigurationUnit>, 
												 MapEvictedListener,MapClearedListener{
	boolean ifStatusOnlyChanged=true;
	protected static final Logger logger = LoggerFactory.getLogger(ROIConfigurationListener.class);
	
	public final void entryAdded(EntryEvent<String, ConfigurationUnit> event) {
		//logger.debug("HC.entryAdded-"+event);
		ROIBaseConfigEvent roiConfigEvt=buildBaseEvent(event,ROIBaseConfigEvent.EVT_CONFIGURATION_ADDED);
		configAdded(roiConfigEvt);
	}

	public final void entryRemoved(EntryEvent<String, ConfigurationUnit> event) {
		//logger.debug("HC.entryRemoved-"+event);
		ROIBaseConfigEvent roiConfigEvt=buildBaseEvent(event,ROIBaseConfigEvent.EVT_CONFIGURATION_REMOVED);
		configRemoved(roiConfigEvt);
	}

	public final void entryUpdated(EntryEvent<String, ConfigurationUnit> event) {
		//logger.debug("HC.entryUpdated-"+event);
		ROIBaseConfigEvent roiConfigEvt=buildBaseEvent(event,ROIBaseConfigEvent.EVT_CONFIGURATION_UPDATED);
		
		if(ifStatusOnlyChanged){
			roiConfigEvt.setEventType(ROIBaseConfigEvent.EVT_CONFIGURATION_STATUSCHANGED);
			configStatusChanged(roiConfigEvt);
		}else{ 
			configUpdated(roiConfigEvt);
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
	
	private ROIBaseConfigEvent buildBaseEvent(EntryEvent<String, ConfigurationUnit> event,String eventType){
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
			
		ROIBaseConfigEvent roiConfigEvt=new ROIBaseConfigEvent(tenantId, value,eventType);
		roiConfigEvt.setConfigGroup(configName);
		roiConfigEvt.setConfigName(configKey);
		return roiConfigEvt;
	}
	
	public abstract void configAdded(ROIBaseConfigEvent configAddedEvent);
	public abstract void configUpdated(ROIBaseConfigEvent configUpdatedEvent);
	public abstract void configRemoved(ROIBaseConfigEvent configRemovedEvent);
	public abstract void configStatusChanged(ROIBaseConfigEvent configStatusChangedEvent);
	
}
