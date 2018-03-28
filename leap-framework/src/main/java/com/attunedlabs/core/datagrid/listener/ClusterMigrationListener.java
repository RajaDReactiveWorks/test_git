package com.attunedlabs.core.datagrid.listener;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.eventtracker.impl.EventTrackerTableConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;

public class ClusterMigrationListener implements MigrationListener {
	protected static final Logger logger = LoggerFactory.getLogger(ClusterMigrationListener.class);

	@Override
	public void migrationStarted(MigrationEvent migrationEvent) {
		logger.debug("Started: " + migrationEvent);
	}

	@Override
	public void migrationCompleted(MigrationEvent migrationEvent) {
		logger.debug("Completed:*********" + migrationEvent + " ***********");
		
		String eventStoreKey = "";
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			dataContext.setIsInTransaction(false);
			final Table table = dataContext.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			DataSet dataSet = dataContext.query().from(table)
					.select(EventTrackerTableConstants.EVENT_STORE_ID).execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				eventStoreKey = row.getValue(table.getColumnByName(EventTrackerTableConstants.EVENT_STORE_ID)).toString();
				HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
				IMap<String, ArrayList<LeapEvent>> eventList= hazelcastInstance.getMap(eventStoreKey);
				logger.debug("migrated eventStoreKey : " + eventStoreKey + "with eventMap size : " + eventList.size());
			} else {
				logger.debug("EventDispatcherTracker table is empty no events!");
			}
		} catch (Exception e) {
			logger.warn("cannot get migration status for hazelcast event map db operation failed!");
		}finally{DataSourceInstance.closeConnection(connection);}

		
	}

	@Override
	public void migrationFailed(MigrationEvent migrationEvent) {
		logger.debug("Failed: " + migrationEvent);
	}
}