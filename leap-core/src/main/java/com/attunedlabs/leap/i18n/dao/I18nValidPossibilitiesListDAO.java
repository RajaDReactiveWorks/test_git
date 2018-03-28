package com.attunedlabs.leap.i18n.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.leap.i18n.LeapI18nConstant;
import com.attunedlabs.leap.i18n.entity.LeapValidPossibilitiesList;
import com.attunedlabs.leap.i18n.exception.LocaleRegistryException;
import com.attunedlabs.leap.i18n.exception.LocaleResolverException;

public class I18nValidPossibilitiesListDAO {

	static Logger logger = LoggerFactory.getLogger(I18nValidPossibilitiesListDAO.class);

	/**
	 * Insert new Valid Possibilities List for new tenant & new site
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param locale
	 * @return
	 * @throws LocaleRegistryException
	 */
	public int insertNewVpList(final String tenantId, final String siteId, final String feature,
			final String vpListI18nId, final String vpType) throws LocaleRegistryException {
		logger.debug("inside insertNewMessage...tenantId: " + tenantId + " siteId: " + siteId + " feature: " + feature
				+ " vpListI18nId: " + vpListI18nId + " vpType: " + vpType);
		Integer generatedId = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			System.out.println(connection);
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESLIST_TABLE);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(table);
					insert.value(LeapI18nConstant.TENANT_ID, tenantId).value(LeapI18nConstant.SITE_ID, siteId)
							.value(LeapI18nConstant.FEATURENAME, feature).value(LeapI18nConstant.VPLISTI18NID, vpListI18nId)
							.value(LeapI18nConstant.VPTYPE, vpType);
					insert.execute();

				}
			});

			if (insertSummary.getGeneratedKeys().isPresent()) {
				generatedId = Integer
						.parseInt(insertSummary.getGeneratedKeys().get().iterator().next().toString().trim());
				logger.debug("insertNewMessage key: " + generatedId);
			} else {
				logger.debug(
						"insertNewMessage key not found " + insertSummary.getGeneratedKeys().get().iterator().next());
			}
		} catch (Exception e) {
			throw new LocaleRegistryException("Unable to register the new Message! --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

		return generatedId;
	}

	/**
	 * get the tenant-specific list of all the locale Valid Possibilities List.
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param localeId
	 * @param feature
	 * @return
	 * @throws LocaleResolverException
	 */
	public List<LeapValidPossibilitiesList> selectAllTenantVpList(String tenantId, String siteId, String feature)
			throws LocaleResolverException {
		logger.debug("inside selectAllTenantMessage... tenantId: " + tenantId + " siteId: " + siteId + " feature: "
				+ feature);
		List<LeapValidPossibilitiesList> listMessageContext = new ArrayList<>();
		LeapValidPossibilitiesList messageContext = null;
		DataSet dataSet = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESLIST_TABLE);
			dataSet = dataContext.query().from(table).selectAll().where(LeapI18nConstant.TENANT_ID).eq(tenantId)
					.and(LeapI18nConstant.SITE_ID).eq(siteId).and(LeapI18nConstant.FEATURENAME).eq(feature).execute();
			if (dataSet == null)
				throw new LocaleResolverException("Empty dataSet returned on get all available messages! ");
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				logger.info("fetched config node row: " + row);
				messageContext = parseROW(messageContext, row, table);
				logger.info("parsed LeapI18nMessage from row retrieved: " + messageContext);
				listMessageContext.add(messageContext);
			}
			logger.info("list of all parsed LeapI18nMessage from rows retrieved: " + listMessageContext);
		} catch (Exception e) {
			throw new LocaleResolverException("Unable to get all messages details --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

		return listMessageContext;
	}// ..end of the method

	/**
	 * get specific locale Valid Possibilities List.
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param localeId
	 * @param applicableNodeId
	 * @return
	 * @throws LocaleResolverException
	 */
	public LeapValidPossibilitiesList selectVpList(String tenantId, String siteId, String feature, String vpType)
			throws LocaleResolverException {
		logger.debug("inside selectMessage... tenantId: " + tenantId + " siteId: " + siteId + " feature: " + feature
				+ " vpType: " + vpType);
		LeapValidPossibilitiesList messageContext = null;
		DataSet dataSet = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESLIST_TABLE);
			dataSet = dataContext.query().from(table).selectAll().where(LeapI18nConstant.TENANT_ID).eq(tenantId)
					.and(LeapI18nConstant.SITE_ID).eq(siteId).and(LeapI18nConstant.FEATURENAME).eq(feature)
					.and(LeapI18nConstant.VPTYPE).eq(vpType).execute();
			if (dataSet == null)
				throw new LocaleResolverException("Empty dataSet returned on get all available messages! ");
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				logger.info("fetched config node row: " + row);
				messageContext = parseROW(messageContext, row, table);
				logger.info("parsed LeapI18nMessage from row retrieved: " + messageContext);
			}
		} catch (Exception e) {
			throw new LocaleResolverException("Unable to get messages details --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

		return messageContext;
	}// ..end of the method

	/**
	 * get the list of all locale Valid Possibilities List.
	 * 
	 * @return
	 * @throws LocaleResolverException
	 */
	public List<LeapValidPossibilitiesList> selectAllVpList() throws LocaleResolverException {
		logger.debug("inside selectAllMessage() ...");
		List<LeapValidPossibilitiesList> listMessageContext = new ArrayList<>();
		LeapValidPossibilitiesList messageContext = null;
		DataSet dataSet = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESLIST_TABLE);
			dataSet = dataContext.query().from(table).selectAll().execute();
			if (dataSet == null)
				throw new LocaleResolverException("Empty dataSet returned on get all available messages! ");
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				logger.info("fetched config node row: " + row);
				messageContext = parseROW(messageContext, row, table);
				logger.info("parsed LeapI18nMessage from row retrieved: " + messageContext);
				listMessageContext.add(messageContext);
			}
			logger.info("list of all parsed LeapI18nMessage from rows retrieved: " + listMessageContext);
		} catch (Exception e) {
			throw new LocaleResolverException("Unable to get all messages details --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		return listMessageContext;
	}// ..end of the method

	/**
	 * 
	 * @param messageContext
	 * @param row
	 * @param table
	 * @return
	 */
	private LeapValidPossibilitiesList parseROW(LeapValidPossibilitiesList messageContext, Row row, Table table) {
		return messageContext = new LeapValidPossibilitiesList(
				// ConfigUtil.conversionOfLongToIntSetup(row.getValue(table.getColumnByName(LeapI18nConstant.MSG_ID))),
				Integer.parseInt(row.getValue(table.getColumnByName(LeapI18nConstant.VPLISTID)).toString()),
				row.getValue(table.getColumnByName(LeapI18nConstant.TENANT_ID)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.SITE_ID)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.FEATURENAME)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.VPLISTI18NID)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.VPTYPE)).toString());
	}
	
	/*public static void main(String[] args) {
		I18nValidPossibilitiesListDAO text = new I18nValidPossibilitiesListDAO();
		try {
//			text.insertNewVpList("Fidelity", "wh1", "Parcel", "Parcel.ParcelMaintFrm.isInterNational", "Radio");
//			List<LeapValidPossibilitiesList> list = text.selectAllVpList();
//			List<LeapValidPossibilitiesList> list = text.selectAllTenantVpList("Fidelity", "wh1", "Parcel");
//			logger.debug("list : "+list);
			LeapValidPossibilitiesList textValue = text.selectVpList("Fidelity", "wh1", "Parcel", "Radio");
			logger.debug("textValue : "+textValue);
		} catch (LocaleResolverException e) {
			e.printStackTrace();
		}
	}*/
}
