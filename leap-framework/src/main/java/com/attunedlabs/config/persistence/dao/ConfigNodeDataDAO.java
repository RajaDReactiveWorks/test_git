package com.attunedlabs.config.persistence.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.RowUpdationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.exception.ConfigNodeDataConfigurationException;
import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.scheduler.ScheduledJobData;

/**
 * 
 * @author Reactiveworks42
 *
 */
public class ConfigNodeDataDAO {
	final Logger logger = LoggerFactory.getLogger(ConfigNodeDataDAO.class);

	/**
	 * insert confignodedata table with the given confignodedata.
	 * 
	 * @param nodeData
	 * @return generatedNodeId
	 * @throws ConfigNodeDataConfigurationException
	 */
	public int insertConfigNodeData(final ConfigNodeData nodeData) throws ConfigNodeDataConfigurationException {
		logger.debug("inside insertConfigNodeData..." + nodeData);
		Integer generatedNodeId = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(table);
					insert.value(LeapConstants.CONFIG_NODE_ID, nodeData.getParentConfigNodeId())
							.value(LeapConstants.CONFIG_NAME, nodeData.getConfigName())
							.value(LeapConstants.CONFIG_DATA, nodeData.getConfigData())
							.value(LeapConstants.CONFIG_TYPE, nodeData.getConfigType())
							.value(LeapConstants.CONFIG_STATUS, nodeData.getConfigLoadStatus())
							.value(LeapConstants.CREATED_DTM,
									new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));

					if (nodeData.getFailureMsg() == null)
						insert.value(LeapConstants.FAILURE_MSG, java.sql.Types.NULL);
					else
						insert.value(LeapConstants.FAILURE_MSG, nodeData.getFailureMsg());
					if (nodeData.isEnabled())
						insert.value(LeapConstants.IS_ENABLED, 1);
					else
						insert.value(LeapConstants.IS_ENABLED, 0);

					insert.execute();

				}
			});

			if (insertSummary.getGeneratedKeys().isPresent()) {
				generatedNodeId = Integer
						.parseInt(insertSummary.getGeneratedKeys().get().iterator().next().toString().trim());
				logger.debug("insertConfigNodeData-generatedKey: " + generatedNodeId);
			} else
				logger.debug("insertConfigNodeData-generatedKey  not found");
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException("failed to insert config node data --> " + e.getMessage(),
					e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

		return generatedNodeId;

	}

	/**
	 * change the enable status to true or false.
	 * 
	 * @param setEnable
	 * @param nodeDataId
	 * @throws ConfigNodeDataConfigurationException
	 */
	public void enableConfigNodeData(final boolean setEnable, final Integer nodeDataId)
			throws ConfigNodeDataConfigurationException {
		logger.debug("inside enableConfigNodeData... nodeDataId: " + nodeDataId);
		int totalRowsUpdated = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			DefaultUpdateSummary updateSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowUpdationBuilder update = callback.update(table);
					update.where(LeapConstants.CONFIG_NODE_DATA_ID).eq(nodeDataId);
					if (setEnable)
						update.value(LeapConstants.IS_ENABLED, 1);
					else
						update.value(LeapConstants.IS_ENABLED, 0);
					update.execute();

				}
			});
			if (updateSummary.getUpdatedRows().isPresent()) {
				totalRowsUpdated = (Integer) updateSummary.getUpdatedRows().get();
				logger.debug("total updated configdata nodes: " + totalRowsUpdated);
			} else
				logger.debug("total configdata nodes: " + totalRowsUpdated);
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException(
					"failed to update config node at nodeDataId:" + nodeDataId + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

	}

	/**
	 * updating the config data where it matches the
	 * nodeId,configName,configType.
	 * 
	 * @param xmlString
	 * @param nodeId
	 * @param configName
	 * @param configType
	 * @throws ConfigNodeDataConfigurationException
	 */
	public void updateConfigDataByNameAndNodeId(final String xmlString, final Integer nodeId, final String configName,
			final String configType) throws ConfigNodeDataConfigurationException {
		logger.debug("inside updateConfigDataByNameAndNodeId   nodeData: " + xmlString + " where nodeId: " + nodeId
				+ " &configName: " + configName + " &configType: " + configType);
		int totalRowsUpdated = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			DefaultUpdateSummary updateSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowUpdationBuilder update = callback.update(table);
					update.where(LeapConstants.CONFIG_NODE_ID).eq(nodeId).where(LeapConstants.CONFIG_NAME)
							.eq(configName).where(LeapConstants.CONFIG_TYPE).eq(configType)
							.value(LeapConstants.CONFIG_DATA, xmlString);
					update.execute();

				}
			});
			if (updateSummary.getUpdatedRows().isPresent()) {
				totalRowsUpdated = (Integer) updateSummary.getUpdatedRows().get();
				logger.debug("total updated configdata nodes: " + totalRowsUpdated);
			} else
				logger.debug("total configdata nodes: " + totalRowsUpdated);
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException(
					"failed to update config node date at nodeId:" + nodeId + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

	}

	/**
	 * get the confignodedata where it matches the column
	 * configType,nodeId,configName.
	 * 
	 * @param nodeId
	 * @param configName
	 * @param configType
	 * @return ConfigNodeData
	 * @throws ConfigNodeDataConfigurationException
	 */
	public ConfigNodeData getConfigNodeDatabyNameAndNodeId(Integer nodeId, String configName, String configType)
			throws ConfigNodeDataConfigurationException {
		logger.debug("inside getConfigNodeDatabyNameAndNodeId... nodeId: " + nodeId + " configName: " + configName
				+ " configType: " + configType);
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			dataContext.setIsInTransaction(true);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.CONFIG_NODE_ID).eq(nodeId)
					.and(LeapConstants.CONFIG_NAME).eq(configName.trim()).and(LeapConstants.CONFIG_TYPE)
					.eq(configType.trim()).execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				ConfigNodeData configNodeData = new ConfigNodeData();
				logger.info("fetched config node row: " + row);
				parseROW(configNodeData, row, table);
				logger.info("parsed config node from row retrieved: " + configNodeData);
				return configNodeData;
			} else {
				logger.warn("getConfigNodeDatabyNameAndNodeId is confignodenull");
				return null;
			}
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException("failed to get config node data --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

	}

	/**
	 * get the configNodeData from the table where it matches the given
	 * configNodeDataId.
	 * 
	 * @param configNodeDataId
	 * @return ConfigNodeData
	 * @throws ConfigNodeDataConfigurationException
	 */
	public ConfigNodeData getConfigNodeDatabyId(Integer configNodeDataId) throws ConfigNodeDataConfigurationException {
		logger.debug("inside getConfigNodeDatabyId... configNodeDataId: " + configNodeDataId);
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.CONFIG_NODE_DATA_ID)
					.eq(configNodeDataId).execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				ConfigNodeData configNodeData = new ConfigNodeData();
				logger.info("fetched config node row: " + row);
				parseROW(configNodeData, row, table);
				logger.info("parsed config node from row retrieved: " + configNodeData);
				return configNodeData;
			} else {
				logger.warn("getConfigNodeDatabyId is confignodenull");
				return null;
			}
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException(
					"failed to get config node data at configNodeDataId: " + configNodeDataId + "--> " + e.getMessage(),
					e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

	}

	/**
	 * get the list of configNodeData from the table where it matches the given
	 * nodeId.
	 * 
	 * @param nodeId
	 * @return List<ConfigNodeData>
	 * @throws ConfigNodeDataConfigurationException
	 */
	public List<ConfigNodeData> getConfigNodeDataByNodeId(Integer nodeId) throws ConfigNodeDataConfigurationException {
		logger.debug("inside getConfigNodeDataByNodeId()... NodeId=" + nodeId);
		List<ConfigNodeData> list = new ArrayList<ConfigNodeData>();
		ConfigNodeData configNodeData = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.CONFIG_NODE_ID).eq(nodeId)
					.execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				configNodeData = new ConfigNodeData();
				logger.info("fetched config node row: " + row);
				parseROW(configNodeData, row, table);
				logger.info("parsed config node from row retrieved: " + configNodeData);
				list.add(configNodeData);
			}
			logger.info("all config-node data from rows retrieved: " + list);
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException(
					"failed to get all config-node data at nodeId: " + nodeId + "--> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

		return list;
	}

	/**
	 * deletes the row from the configNodeData table where it matches the
	 * configNodeDataId.
	 * 
	 * @param configNodeDataId
	 * @return int
	 * @throws ConfigNodeDataConfigurationException
	 */
	public int deleteConfigNodeData(final Integer configNodeDataId) throws ConfigNodeDataConfigurationException {
		logger.debug("inside .deleteConfigNodeData() configNodeData: " + configNodeDataId);
		int totalRowsDeleted = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			DefaultUpdateSummary deleteSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowDeletionBuilder delete = callback.deleteFrom(table);
					delete.where(LeapConstants.CONFIG_NODE_DATA_ID).eq(configNodeDataId).execute();

				}
			});
			if (deleteSummary.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) deleteSummary.getDeletedRows().get();
				logger.info("confignodedata is deleted  for configNodeDataId= " + configNodeDataId
						+ " total impacted  node-->" + totalRowsDeleted);
				return totalRowsDeleted;
			} else {
				logger.info("total impacted node: " + totalRowsDeleted);
				return 0;
			}
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException(
					"failed to deletenode with configNodeDataId:" + configNodeDataId + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

	}

	/**
	 * get the list of configNodeData from the table where it matches the given
	 * nodeId and configType.
	 * 
	 * @param nodeId
	 * @param configType
	 * @return List<ConfigNodeData>
	 * @throws SQLException
	 * @throws IOException
	 * @throws ConfigNodeDataConfigurationException
	 */
	public List<ConfigNodeData> getConfigNodeDataByNodeIdByConfigType(Integer nodeId, String configType)
			throws ConfigNodeDataConfigurationException {
		logger.debug(
				"inside getConfigNodeDataByNodeIdByConfigType()... NodeId=" + nodeId + " configType: " + configType);
		List<ConfigNodeData> list = new ArrayList<ConfigNodeData>();
		ConfigNodeData configNodeData = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.CONFIG_NODE_ID).eq(nodeId)
					.and(LeapConstants.CONFIG_TYPE).eq(configType).execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				configNodeData = new ConfigNodeData();
				logger.info("fetched config node row: " + row);
				parseROW(configNodeData, row, table);
				logger.info("parsed config node from row retrieved: " + configNodeData);
				list.add(configNodeData);
			}
			logger.info("all config-node data from rows retrieved: " + list);
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException("failed to get all config-node data at nodeId: " + nodeId
					+ "configType: " + configType + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

		return list;
	}

	/**
	 * updates the configNodeData table with the ConfigNodeData where
	 * nodeDataId=?
	 * 
	 * @param nodeData
	 * @return totalRowsUpdated
	 * @throws SQLException
	 * @throws IOException
	 * @throws ConfigNodeDataConfigurationException
	 */
	public int updateConfigNodeData(final ConfigNodeData nodeData) throws ConfigNodeDataConfigurationException {
		logger.debug("inside updateConfigNodeData..." + nodeData);
		Integer totalRowsUpdated = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			DefaultUpdateSummary updateSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowUpdationBuilder update = callback.update(table);
					update.where(LeapConstants.CONFIG_NODE_DATA_ID).eq(nodeData.getNodeDataId());

					update.value(LeapConstants.CONFIG_NODE_ID, nodeData.getParentConfigNodeId())
							.value(LeapConstants.CONFIG_NAME, nodeData.getConfigName())
							.value(LeapConstants.CONFIG_DATA, nodeData.getConfigData())
							.value(LeapConstants.CONFIG_TYPE, nodeData.getConfigType())
							.value(LeapConstants.CONFIG_STATUS, nodeData.getConfigLoadStatus())
							.value(LeapConstants.CREATED_DTM,
									new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));

					if (nodeData.getFailureMsg() == null)
						update.value(LeapConstants.FAILURE_MSG, java.sql.Types.NULL);
					else
						update.value(LeapConstants.FAILURE_MSG, nodeData.getFailureMsg());
					if (nodeData.isEnabled())
						update.value(LeapConstants.IS_ENABLED, 1);
					else
						update.value(LeapConstants.IS_ENABLED, 0);

					update.execute();

				}
			});

			if (updateSummary.getUpdatedRows().isPresent()) {
				totalRowsUpdated = (Integer) updateSummary.getUpdatedRows().get();
				logger.debug("total updated configdata nodes: " + totalRowsUpdated);
			} else
				logger.debug("total configdata nodes: " + totalRowsUpdated);
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException("failed to update config node data with nodeDataId: "
					+ nodeData.getNodeDataId() + "--> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

		return totalRowsUpdated;
	}

	/**
	 * delete the row where it matches nodeId provided.
	 * 
	 * @param nodeId
	 * @return totalRowsDeleted
	 * @throws ConfigNodeDataConfigurationException
	 */
	public int deleteConfigNodeDataByNodeId(final Integer nodeId) throws ConfigNodeDataConfigurationException {
		logger.debug("inside .deleteConfigNodeDataByNodeId()... nodeId: " + nodeId);
		int totalRowsDeleted = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			DefaultUpdateSummary deleteSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowDeletionBuilder delete = callback.deleteFrom(table);
					delete.where(LeapConstants.CONFIG_NODE_ID).eq(nodeId).execute();

				}
			});
			if (deleteSummary.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) deleteSummary.getDeletedRows().get();
				logger.info("confignodedata is deleted  for nodeId= " + nodeId + " total impacted  node-->"
						+ totalRowsDeleted);
				return totalRowsDeleted;
			} else {
				logger.info("total impacted node: " + totalRowsDeleted);
				return 0;
			}
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException(
					"failed to deletenode with nodeId:" + nodeId + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

	}

	/**
	 * delete the row where it matches nodeId and configName provided.
	 * 
	 * @param configName
	 * @param nodeId
	 * @return totalRowsDeleted
	 * @throws ConfigNodeDataConfigurationException
	 * @throws SQLException
	 * @throws IOException
	 */
	public int deleteConfigNodeDataByNodeIdAndByConfigName(final String configName, final int nodeId)
			throws ConfigNodeDataConfigurationException {
		logger.debug("inside .deleteConfigNodeDataByNodeId()... nodeId: " + nodeId + " configName: " + configName);
		int totalRowsDeleted = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			DefaultUpdateSummary deleteSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowDeletionBuilder delete = callback.deleteFrom(table);
					delete.where(LeapConstants.CONFIG_NODE_ID).eq(nodeId).where(LeapConstants.CONFIG_NAME)
							.eq(configName).execute();

				}
			});
			if (deleteSummary.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) deleteSummary.getDeletedRows().get();
				logger.info("confignodedata is deleted  for nodeId= " + nodeId + " &configName: " + configName
						+ " total impacted  node-->" + totalRowsDeleted);
				return totalRowsDeleted;
			} else {
				logger.info("total impacted node: " + totalRowsDeleted);
				return 0;
			}
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException("failed to deletenode with nodeId:" + nodeId
					+ " &configName: " + configName + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

	}

	private void parseROW(ConfigNodeData configData, Row row, Table table) {
		configData.setNodeDataId((Integer) row.getValue(table.getColumnByName(LeapConstants.CONFIG_NODE_DATA_ID)));
		configData.setParentConfigNodeId((Integer) row.getValue(table.getColumnByName(LeapConstants.CONFIG_NODE_ID)));
		configData.setConfigName(row.getValue(table.getColumnByName(LeapConstants.CONFIG_NAME)).toString().trim());
		configData.setConfigData(row.getValue(table.getColumnByName(LeapConstants.CONFIG_DATA)).toString().trim());
		configData.setConfigType(row.getValue(table.getColumnByName(LeapConstants.CONFIG_TYPE)).toString().trim());
		configData.setConfigLoadStatus(
				row.getValue(table.getColumnByName(LeapConstants.CONFIG_STATUS)).toString().trim());
		int isEnabled = ((Integer) row.getValue(table.getColumnByName(LeapConstants.IS_ENABLED)));
		configData.setEnabled(isEnabled > 0);
		configData.setCreatedDTM((Date) row.getValue(table.getColumnByName(LeapConstants.CREATED_DTM)));
		configData.setFailureMsg(row.getValue(table.getColumnByName(LeapConstants.FAILURE_MSG)).toString());
	}

	public int checkSchedulerDatabyName(String configName) throws ConfigNodeDataConfigurationException {
		logger.debug("inside getConfigNodeDatabyName... configName: " + configName);
		Connection connection = null;
		int jobId = 0;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.ALL_TENANT_SCHEDULER_JOB_REQUEST);
			DataSet dataSet = dataContext.query().from(table).select(LeapConstants.JOBID).where(LeapConstants.JOBNAME)
					.eq(configName.trim()).execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				/*
				 * ConfigNodeData configNodeData = new ConfigNodeData();
				 * logger.info("fetched config node row: " + row);
				 * parseROW(configNodeData, row, table);
				 * logger.info("parsed config node from row retrieved: " +
				 * configNodeData);
				 */
				Column JOBID = table.getColumnByName(LeapConstants.JOBID);
				jobId = Integer.parseInt(row.getValue(JOBID).toString());
				return jobId;
			} else {
				logger.warn("getConfigNodeDatabyNameAndNodeId is confignodenull");
				return 0;
			}
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException(
					"failed to get jobId against scheduler data --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
	}

	public int insertScheduledJobData(final ScheduledJobData jobData) throws ConfigNodeDataConfigurationException {
		logger.debug("inside insertScheduledJobData..." + jobData);
		Integer generatedJobId = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.ALL_TENANT_SCHEDULER_JOB_REQUEST);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(table);
					insert.value(LeapConstants.ALLAPPLICABLETENANT, true)
							.value(LeapConstants.JOBNAME, jobData.getJobName())
							.value(LeapConstants.JOBSERTVICE, jobData.getJobService())
							.value(LeapConstants.JOBTYPE, jobData.getJobType())
							.value(LeapConstants.FEATURE_GROUP, jobData.getFeatureGroup())
							.value(LeapConstants.FEATURE, jobData.getFeature())
							.value(LeapConstants.JOBCONTEXTDETAIL, jobData.getJobContextDetail())
							.value(LeapConstants.SCHEDUELINGEXPRESSION, jobData.getSchedulingExpresssion())
							.value(LeapConstants.CREATEDDTM,
									new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));

					if (jobData.isEnabled())
						insert.value(LeapConstants.IS_ENABLED, 1);
					else
						insert.value(LeapConstants.IS_ENABLED, 0);
					insert.value(LeapConstants.STATUS, "new");
					insert.execute();

				}
			});

			if (insertSummary.getGeneratedKeys().isPresent()) {
				generatedJobId = Integer
						.parseInt(insertSummary.getGeneratedKeys().get().iterator().next().toString().trim());
				logger.debug("insertJobData-generatedKey: " + generatedJobId);
			} else
				logger.debug("insertJobData-generatedKey  not found");
		} catch (Exception e) {
			throw new ConfigNodeDataConfigurationException("failed to insert job data --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

		return generatedJobId;
	}

}