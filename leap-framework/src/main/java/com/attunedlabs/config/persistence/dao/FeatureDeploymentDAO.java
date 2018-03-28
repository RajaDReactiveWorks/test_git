package com.attunedlabs.config.persistence.dao;

import java.sql.Connection;

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
import org.apache.metamodel.update.RowUpdationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.exception.FeatureDeploymentConfigurationException;
import com.attunedlabs.config.persistence.exception.FeatureMasterConfigurationException;
import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.featuredeployment.FeatureDeployment;

public class FeatureDeploymentDAO {
	final Logger logger = LoggerFactory.getLogger(FeatureDeploymentDAO.class);

	/**
	 * insert feature deployment at the auto generated row and setting that
	 * auto-generated key as the DeploymentId.
	 * 
	 * @param featureDeployment
	 * @throws FeatureMasterConfigurationException
	 * @throws FeatureDeploymentConfigurationException
	 */
	public FeatureDeployment insertFeatureDeploymentDetails(final FeatureDeployment featureDeployment)
			throws FeatureDeploymentConfigurationException {
		logger.debug(" inside insertFeatureDeploymentDetails..." + featureDeployment);
		int insertedKey;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.TABLE_FEATUREDEPLOYMENT);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(table);
					insert.value(LeapConstants.FEATURE_MASTER_ID, featureDeployment.getFeatureMasterId())
							.value(LeapConstants.FEATURE_NAME, featureDeployment.getFeatureName())
							.value(LeapConstants.IMPLEMENTATION_NAME, featureDeployment.getImplementationName())
							.value(LeapConstants.VENDOR_NAME, featureDeployment.getVendorName())
							.value(LeapConstants.FEATURE_VERSION, featureDeployment.getFeatureVersion())
							.value(LeapConstants.IS_ACTIVE, featureDeployment.isActive())
							.value(LeapConstants.IS_PRIMARY, featureDeployment.isPrimary())
							.value(LeapConstants.IS_CUSTOMIZED, featureDeployment.isCustomized())
							.value(LeapConstants.PROVIDER, featureDeployment.getProvider())
							.execute();

				}
			});

			if (insertSummary.getGeneratedKeys().isPresent()) {
				insertedKey = Integer
						.parseInt(insertSummary.getGeneratedKeys().get().iterator().next().toString().trim());
				logger.debug("feature deployed-generatedKey: " + insertedKey);
				featureDeployment.setId(insertedKey);
			} else
				logger.warn("feature deployed-generatedKey not found");

		} catch (Exception e) {
			throw new FeatureDeploymentConfigurationException("Failed to insert into featureDeploymentdetails: "
					+ featureDeployment + " due to -- > " + e.getMessage(), e);
		}finally{DataSourceInstance.closeConnection(connection);}

		return featureDeployment;
	}

	/**
	 * get feature deployment data with feature and implementation name.
	 * 
	 * @param featureMasterId
	 * @param featureName
	 * @param implName
	 * @param vendorName
	 * @param version
	 * @return featureDeployment
	 * @throws FeatureMasterConfigurationException
	 * @throws FeatureDeploymentConfigurationException
	 */
	public FeatureDeployment getFeatureDeploymentByFeatureAndImplName(int featureMasterId, String featureName,
			String implName, String vendorName, String version) throws FeatureDeploymentConfigurationException {
		logger.debug(" inside getFeatureDeploymentByFeatureAndImplName... featureMasterId: " + featureMasterId
				+ " featureName: " + featureName + " implName: " + implName + " vendorName: " + vendorName
				+ " version: " + version);
		FeatureDeployment featureDeployment = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			DataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.TABLE_FEATUREDEPLOYMENT);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.FEATURE_NAME)
					.eq(featureName).and(LeapConstants.IMPLEMENTATION_NAME).eq(implName).and(LeapConstants.VENDOR_NAME)
					.eq(vendorName).and(LeapConstants.FEATURE_VERSION).eq(version).and(LeapConstants.FEATURE_MASTER_ID)
					.eq(featureMasterId).execute();
			if (dataSet.next()) {
				int featureDeploymentId = (Integer) dataSet.getRow()
						.getValue(table.getColumnByName(LeapConstants.FEATUREDEPLOYMENT_ID));
				Object activeObj = dataSet.getRow().getValue(table.getColumnByName(LeapConstants.IS_ACTIVE));
				Object primaryObj = dataSet.getRow().getValue(table.getColumnByName(LeapConstants.IS_PRIMARY));
				Object custumizedObj = dataSet.getRow().getValue(table.getColumnByName(LeapConstants.IS_CUSTOMIZED));

				boolean isActive = false;
				boolean isPrimary = false;
				boolean isCustomized = false;

				if (activeObj instanceof Boolean)
					isActive = (Boolean) activeObj;
				if (activeObj instanceof Integer)
					isActive = (Integer) activeObj != 0;

				if (primaryObj instanceof Boolean)
					isPrimary = (Boolean) primaryObj;
				if (primaryObj instanceof Integer)
					isPrimary = (Integer) primaryObj != 0;

				if (custumizedObj instanceof Boolean)
					isCustomized = (Boolean) custumizedObj;
				if (custumizedObj instanceof Integer)
					isCustomized = (Integer) custumizedObj != 0;

				featureDeployment = new FeatureDeployment(featureDeploymentId, featureMasterId, featureName, implName,
						vendorName, version, isActive, isPrimary, isCustomized);
				logger.info("feature deployment details fetched " + featureDeployment.toString());
			}
		} catch (Exception e) {
			throw new FeatureDeploymentConfigurationException(
					"Failed to get FeatureDeployment by feature and implName --> " + e.getMessage(), e);
		}finally{DataSourceInstance.closeConnection(connection);}


		return featureDeployment;
	}

	/**
	 * update existing feature deployment with following details.
	 * 
	 * @param featureMasterId
	 * @param featureName
	 * @param implName
	 * @param vendorName
	 * @param version
	 * @param isPrimary
	 * @param isActive
	 * @return boolean
	 * @throws FeatureDeploymentConfigurationException
	 */
	public boolean updateFeatureDeployment(final int featureMasterId, final String featureName, final String implName,
			final String vendorName, final String version, final boolean isPrimary, final boolean isActive)
			throws FeatureDeploymentConfigurationException {
		logger.debug("inside updateFeatureDeployment.. method of FeatureDeploymentDAO");
		int totalRowsUpdated = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.TABLE_FEATUREDEPLOYMENT);
			DefaultUpdateSummary updateSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowUpdationBuilder update = callback.update(table);
					update.value(LeapConstants.IS_PRIMARY, isPrimary).value(LeapConstants.IS_ACTIVE, isActive)
							.where(LeapConstants.FEATURE_NAME).eq(featureName).where(LeapConstants.IMPLEMENTATION_NAME)
							.eq(implName).where(LeapConstants.VENDOR_NAME).eq(vendorName)
							.where(LeapConstants.FEATURE_VERSION).eq(version).where(LeapConstants.FEATURE_MASTER_ID)
							.eq(featureMasterId).execute();

				}
			});
			if (updateSummary.getUpdatedRows().isPresent()) {
				totalRowsUpdated = (Integer) updateSummary.getUpdatedRows().get();
				logger.debug("total updated records: " + totalRowsUpdated);
				if (totalRowsUpdated > 0)
					return true;
			} else
				logger.debug("total updated records: " + totalRowsUpdated);
		} catch (Exception e) {
			throw new FeatureDeploymentConfigurationException(
					"failed to update featuredeployment details: " + e.getMessage(), e);
		}finally{DataSourceInstance.closeConnection(connection);}

		return false;

	}

	/**
	 * delete feature deployment with provided details.
	 * 
	 * @param featureMasterId
	 * @param featureName
	 * @param implName
	 * @param vendorName
	 * @param version
	 * @return
	 * @throws FeatureDeploymentConfigurationException
	 */
	public boolean deleteFeatureDeployment(final int featureMasterId, final String featureName, final String implName,
			final String vendorName, final String version) throws FeatureDeploymentConfigurationException {
		logger.debug("inside .deleteFeatureDeployment() featureMasterId: " + featureMasterId + " featureName: "
				+ featureName + " implName: " + implName + " vendorName: " + vendorName + " version: " + version);
		int totalRowsDeleted = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.TABLE_FEATUREDEPLOYMENT);
			DefaultUpdateSummary deleteSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowDeletionBuilder delete = callback.deleteFrom(table);
					delete.where(LeapConstants.FEATURE_MASTER_ID).eq(featureMasterId).where(LeapConstants.FEATURE_NAME)
							.eq(featureName).where(LeapConstants.IMPLEMENTATION_NAME).eq(implName)
							.where(LeapConstants.VENDOR_NAME).eq(vendorName).where(LeapConstants.VERSION).eq(version);
					delete.execute();

				}
			});
			if (deleteSummary.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) deleteSummary.getDeletedRows().get();
				logger.info("toatal impacted rows -->" + totalRowsDeleted);
			} else {
				logger.info("total impacted node: " + totalRowsDeleted);
			}
		} catch (Exception e) {
			throw new FeatureDeploymentConfigurationException(
					"failed to feature in featuredeployment!  --> " + e.getMessage(), e);
		}finally{DataSourceInstance.closeConnection(connection);}

		return totalRowsDeleted > 0;
	}
}
