package com.attunedlabs.config.persistence.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.exception.FeatureMasterConfigurationException;
import com.attunedlabs.config.util.ConfigUtil;
import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.featuremaster.FeatureMaster;

public class ConfigFeatureMasterDAO {
	final Logger logger = LoggerFactory.getLogger(ConfigFeatureMasterDAO.class);

	/**
	 * getFeatureMasterId by feature and feature group
	 * 
	 * @param featureName
	 * @param featureGroup
	 * @param version
	 * @param siteId
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws FeatureMasterConfigurationException
	 */
	public int getFeatureMasterIdByFeatureAndFeaturegroup(String featureName, String featureGroup, String version,
			int siteId) throws FeatureMasterConfigurationException {
		logger.debug(".getFeatureMasterIdByFeatureAndFeaturegroup....featureGroup: " + featureGroup + " featureName: "
				+ featureName + " version: " + version + " siteId: " + siteId);

		int masterNodeId = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			DataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			Table table = dataContext.getTableByQualifiedLabel(LeapConstants.TABLE_FEATUREMASTER);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.FEATURE_NAME)
					.eq(featureName).and(LeapConstants.FEATURE_GROUP).eq(featureGroup).and(LeapConstants.VERSION)
					.eq(version).and(LeapConstants.SITE_ID).eq(siteId).execute();
			if (dataSet.next()) {
				masterNodeId = ConfigUtil.conversionOfLongToIntSetup(
						dataSet.getRow().getValue(table.getColumnByName(LeapConstants.FEATURE_MASTER_ID)));
			}

			logger.debug("masterNodeId : " + masterNodeId);
		} catch (Exception e) {
			throw new FeatureMasterConfigurationException(
					"Failed to get featuremasterId from given parameters " + e.getMessage(), e);
		}finally{DataSourceInstance.closeConnection(connection);}
		return masterNodeId;
	}

	/**
	 * insert feature master details with the provided data.
	 * 
	 * @param featureMaster
	 * @return
	 * @throws FeatureMasterConfigurationException
	 */
	public boolean insertFeatureMasterDetails(final FeatureMaster featureMaster)
			throws FeatureMasterConfigurationException {
		logger.debug(".insertFeatureMasterDetails... featuremaster: " + featureMaster);
		Boolean isInserted = false;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.TABLE_FEATUREMASTER);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(table);
					insert.value(LeapConstants.FEATURE_MASTER_ID, featureMaster.getId())
							.value(LeapConstants.FEATURE_NAME, featureMaster.getFeature())
							.value(LeapConstants.FEATURE_GROUP, featureMaster.getFeatureGroup())
							.value(LeapConstants.SITE_ID, featureMaster.getSiteId())
							.value(LeapConstants.VERSION, featureMaster.getVersion())
							.value(LeapConstants.DESCRIPTION, featureMaster.getDescription())
							.value(LeapConstants.MULTI_VENDOR_SUPPORT, featureMaster.isMultipleVendorSupport())
							.value(LeapConstants.PRODUCT, featureMaster.getProduct()).execute();
				}
			});
			if (insertSummary.getInsertedRows().isPresent()) {
				logger.info(
						"inserted rows : " + insertSummary.getInsertedRows().get() + " with data : " + featureMaster);
				if (insertSummary.getInsertedRows().get() > 0)
					return true;
			}

		} catch (Exception e) {
			throw new FeatureMasterConfigurationException(
					"Failed to insert into featureMaster featuredetails: " + featureMaster, e);
		}finally{DataSourceInstance.closeConnection(connection);}

		return isInserted;
	}

	/**
	 * delete feature from feature master with feature and specified site Id.
	 * 
	 * @param feature
	 * @param siteId
	 * @return
	 * @throws FeatureMasterConfigurationException
	 */
	public boolean deleteFeatureMasterDetails(final String feature, final int siteId)
			throws FeatureMasterConfigurationException {
		logger.debug("deleteFeatureMasterDetails... feature: " + feature + " & siteId: " + siteId);
		int totalRowsDeleted = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.TABLE_FEATUREMASTER);
			DefaultUpdateSummary deleteSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowDeletionBuilder delete = callback.deleteFrom(table);
					delete.where(LeapConstants.FEATURE_NAME).eq(feature).where(LeapConstants.SITE_ID).eq(siteId)
							.execute();
					;

				}
			});
			if (deleteSummary.getDeletedRows().isPresent()) {
				totalRowsDeleted = deleteSummary.getDeletedRows().get();
				logger.info("total deleted records: " + totalRowsDeleted);
				return totalRowsDeleted > 0;
			} else
				logger.info("total deleted records: " + totalRowsDeleted);
		} catch (Exception e) {
			throw new FeatureMasterConfigurationException("failed to deleterecord  with records containing feature: "
					+ feature + " siteId: " + siteId + "  from featureMaster --> " + e.getMessage(), e);
		}finally{DataSourceInstance.closeConnection(connection);}

		return false;

	}

}
