package com.attunedlabs.core.datagrid.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.partition.PartitionLostEvent;
import com.hazelcast.partition.PartitionLostListener;

public class ConsoleLoggingPartitionLostListener implements PartitionLostListener {
	protected static final Logger logger = LoggerFactory.getLogger(ConsoleLoggingPartitionLostListener.class);

	@Override
	public void partitionLost(PartitionLostEvent event) {
		logger.warn("LOST : " + event);
	}
}