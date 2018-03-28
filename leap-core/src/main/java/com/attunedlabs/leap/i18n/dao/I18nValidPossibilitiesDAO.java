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
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.dao.LeapConstants;
import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.leap.i18n.LeapI18nConstant;
import com.attunedlabs.leap.i18n.entity.LeapI18nText;
import com.attunedlabs.leap.i18n.entity.LeapValidPossibilities;
import com.attunedlabs.leap.i18n.entity.LeapValidPossibilitiesList;
import com.attunedlabs.leap.i18n.exception.LocaleRegistryException;
import com.attunedlabs.leap.i18n.exception.LocaleResolverException;
import com.attunedlabs.leap.i18n.service.ILeapI18nSetup;
import com.attunedlabs.leap.i18n.service.LeapI18nSetupImpl;

public class I18nValidPossibilitiesDAO {

	private static Logger logger = LoggerFactory.getLogger(I18nValidPossibilitiesDAO.class);
	
	/**
	 * get the list of all locale message details.
	 * 
	 * @return
	 * @throws LocaleResolverException
	 */
	public List<String> selectTenantValidPossibilities(String tenantId, String siteId, String feature, String vpType, String vpListI18nId, String localeId) throws LocaleResolverException {
		logger.debug("inside selectAllTenantValidPossibilities() ...");
//		List<LeapValidPossibilities> listMessageContext = new ArrayList<>();
		List<String> listMessageContext = new ArrayList<>();
		LeapValidPossibilities messageContext = null;
		DataSet dataSet = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table1 = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESLIST_TABLE);
			final Table table2 = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESDETAIL_TABLE);
			Column vpListId_1 = table1.getColumnByName("vpListId");
			Column vpListId_2 = table2.getColumnByName("vpListId");
			Query q = dataContext.query().from(table1).innerJoin(table2).on(vpListId_1, vpListId_2).selectAll().where(LeapI18nConstant.TENANT_ID)
					.eq(tenantId).and(LeapI18nConstant.SITE_ID).eq(siteId).and(LeapI18nConstant.FEATURENAME).eq(feature)
					.and(LeapI18nConstant.VPTYPE).eq(vpType).and(LeapI18nConstant.VPLISTI18NID).eq(vpListI18nId)
					.and(LeapI18nConstant.LOCALE_ID).eq(localeId).toQuery();
			dataSet = dataContext.executeQuery(q);
			if (dataSet == null)
				throw new LocaleResolverException("Empty dataSet returned on get all available messages! ");
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				logger.info("fetched config node row: " + row);
//				messageContext = parseROW(messageContext, row, table1, table2);
				logger.info("parsed LeapI18nMessage from row retrieved: " + messageContext);
//				listMessageContext.add(messageContext);
				listMessageContext.add(row.getValue(table2.getColumnByName(LeapI18nConstant.TEXT_VALUE)).toString());
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
	 * get the list of all locale message details.
	 * 
	 * @return
	 * @throws LocaleResolverException
	 */
	public List<LeapValidPossibilities> selectAllTenantLocaleSpecificValidPossibilities(String tenantId, String siteId, String loacaleId) throws LocaleResolverException {
		logger.debug("inside selectAllTenantValidPossibilities() ...");
		List<LeapValidPossibilities> listMessageContext = new ArrayList<>();
		LeapValidPossibilities messageContext = null;
		DataSet dataSet = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table1 = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESLIST_TABLE);
			final Table table2 = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESDETAIL_TABLE);
			Column vpListId_1 = table1.getColumnByName("vpListId");
			Column vpListId_2 = table2.getColumnByName("vpListId");
			Query q = dataContext.query().from(table1).innerJoin(table2).on(vpListId_1, vpListId_2).selectAll().where(LeapI18nConstant.TENANT_ID)
					.eq(tenantId).and(LeapI18nConstant.SITE_ID).eq(siteId).and(LeapI18nConstant.LOCALE_ID).eq(loacaleId).toQuery();
			dataSet = dataContext.executeQuery(q);
			if (dataSet == null)
				throw new LocaleResolverException("Empty dataSet returned on get all available messages! ");
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				logger.info("fetched config node row: " + row);
				messageContext = parseROW(messageContext, row, table1, table2);
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
	 * get the list of all locale message details.
	 * 
	 * @return
	 * @throws LocaleResolverException
	 */
	public List<LeapValidPossibilities> selectAllValidPossibilities() throws LocaleResolverException {
		logger.debug("inside selectAllMessage() ...");
		List<LeapValidPossibilities> listMessageContext = new ArrayList<>();
		LeapValidPossibilities messageContext = null;
		DataSet dataSet = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table1 = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESLIST_TABLE);
			final Table table2 = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESDETAIL_TABLE);
			Column vpListId_1 = table1.getColumnByName("vpListId");
			Column vpListId_2 = table2.getColumnByName("vpListId");
			Query q = dataContext.query().from(table1).innerJoin(table2).on(vpListId_1, vpListId_2).selectAll()
					.toQuery();
			dataSet = dataContext.executeQuery(q);
			if (dataSet == null)
				throw new LocaleResolverException("Empty dataSet returned on get all available messages! ");
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				logger.info("fetched config node row: " + row);
				messageContext = parseROW(messageContext, row, table1, table2);
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
	private LeapValidPossibilities parseROW(LeapValidPossibilities messageContext, Row row, Table table1, Table table2) {
		return messageContext = new LeapValidPossibilities(
				// ConfigUtil.conversionOfLongToIntSetup(row.getValue(table.getColumnByName(LeapI18nConstant.MSG_ID))),
				Integer.parseInt(row.getValue(table1.getColumnByName(LeapI18nConstant.VPLISTID)).toString()),
				row.getValue(table1.getColumnByName(LeapI18nConstant.TENANT_ID)).toString(),
				row.getValue(table1.getColumnByName(LeapI18nConstant.SITE_ID)).toString(),
				row.getValue(table1.getColumnByName(LeapI18nConstant.FEATURENAME)).toString(),
				row.getValue(table1.getColumnByName(LeapI18nConstant.VPLISTI18NID)).toString(),
				row.getValue(table1.getColumnByName(LeapI18nConstant.VPTYPE)).toString(),
				row.getValue(table2.getColumnByName(LeapI18nConstant.LOCALE_ID)).toString(),
				row.getValue(table2.getColumnByName(LeapI18nConstant.SEQNUMBER)).toString(),
				row.getValue(table2.getColumnByName(LeapI18nConstant.VPCODE)).toString(),
				row.getValue(table2.getColumnByName(LeapI18nConstant.TEXT_VALUE)).toString());
	}
	
	public static void main(String[] args) {
		I18nValidPossibilitiesDAO vp = new I18nValidPossibilitiesDAO();
		ILeapI18nSetup localeRegistryService = new LeapI18nSetupImpl();
		try {
//			List<LeapValidPossibilities> list = vp.selectAllValidPossibilities();
			List<String> list = vp.selectTenantValidPossibilities("Fidelity", "wh1", "Parcel", "Combo", "Parcel.ParcelMaintFrm.Shipper", "en_US");
//			List<LeapValidPossibilities> list = vp.selectAllTenantLocaleSpecificValidPossibilities("GetUsROI", "wh1", "en_US");
//			localeRegistryService.buildComboLocaleBundle(list);
			logger.debug("list : "+list);
//			LeapI18nText textValue = text.selectMessage("Fidelity", "wh1", "en_UK", "Parcel");
//			logger.debug("textValue : "+textValue);
		} catch (LocaleResolverException e) {
			e.printStackTrace();
		}
	}
}
