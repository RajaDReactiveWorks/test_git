package com.getusroi.core.datagrid;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;

public abstract class ConfigurationTreeNodeListener implements EntryAddedListener<String,Serializable>,EntryRemovedListener<String,Serializable>,EntryUpdatedListener<String, Serializable>,EntryEvictedListener<String, Serializable>, 
MapEvictedListener,MapClearedListener{
	 static final Logger logger = LoggerFactory.getLogger(ConfigurationTreeNodeListener.class);
	@Override
	public void entryAdded(EntryEvent<String, Serializable> addEvent) {
		configNodeTreeAdded(addEvent);
		
	}

	@Override
	public void entryRemoved(EntryEvent<String, Serializable> removedEvent) {
		configNodeTreeRemoved(removedEvent);
		
	}

	@Override
	public void entryEvicted(EntryEvent<String, Serializable> evictedEvent) {
		logger.debug(".entryEvicted method of ConfigurationTreeNodeListener");
		
	}

	@Override
	public void entryUpdated(EntryEvent<String, Serializable> updateEvent) {
		configNodeTreeUpdated(updateEvent);
	}

	@Override
	public void mapEvicted(MapEvent mapEvictedEvent) {
		logger.debug(".mapEvicted method of ConfigurationTreeNodeListener");
		
	}

	@Override
	public void mapCleared(MapEvent mapClearedEvent) {
		logger.debug(".mapCleared method of ConfigurationTreeNodeListener");
		
	}
	
	
	public abstract void configNodeTreeAdded(EntryEvent<String, Serializable> addEvent);
	public abstract void configNodeTreeUpdated(EntryEvent<String, Serializable> updateEvent);
	public abstract void configNodeTreeRemoved(EntryEvent<String, Serializable> removedEvent);

}
