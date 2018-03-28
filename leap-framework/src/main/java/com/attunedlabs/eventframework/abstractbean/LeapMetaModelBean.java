package com.attunedlabs.eventframework.abstractbean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.delete.DeleteFrom;
import org.apache.metamodel.factory.DataContextFactoryRegistryImpl;
import org.apache.metamodel.factory.DataContextPropertiesImpl;
import org.apache.metamodel.insert.InsertInto;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.FunctionType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.builder.FunctionSelectBuilder;
import org.apache.metamodel.query.builder.SatisfiedQueryBuilder;
import org.apache.metamodel.query.builder.TableFromBuilder;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.Update;

import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.security.TenantSecurityConstant;

/**
 * Wrapper for the MetaModel CRUD
 * 
 * @author GetUsRoi
 *
 */
public class LeapMetaModelBean extends AbstractMetaModelBean {
	public static final String SELECT_OPERATION = "SELECT";
	public static final String INSERT_OPERATION = "INSERT";
	public static final String UPDATE_OPERATION = "UPDATE";
	public static final String DELETE_OPERATION = "DELETE";
	public static final String MYSQL_DB_TYPE = "MYSQL";
	public static final String POSTGRESQL_DB_TYPE = "POSTGRESQL";
	public static final String ORACLE_DB_TYPE = "ORACLE";
	public static final String SQLSERVER_DB_TYPE = "SQLSERVER";
	public static final String CASSANDRA_DB_TYPE = "CASSANDRA";

	/**
	 * protected MetaModel service to perform insert on given table and set of
	 * values
	 * 
	 * @param objectName
	 * @param jdbcDataContext
	 * @param columnNames
	 * @param values
	 * @throws Exception
	 */
	public void doLeapInsert(String tableName, List<String> insertableColumns, List<Object> insertableValues,
			Exchange exchange) throws Exception {
		if (tableName == null) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + tableName);
		}
		insertableColumns = LeapMetaModelHelper.validateListGeneric(insertableColumns);
		insertableValues = LeapMetaModelHelper.validateListGeneric(insertableValues);
		JdbcDataContext jdbcDataContext = (JdbcDataContext) getDataContext(exchange, INSERT_OPERATION);

		/** final Table instance by name - MetaModel **/
		final Table table = jdbcDataContext.getTableByQualifiedLabel(tableName);
		final int colCount = insertableColumns.size();
		insertableValues = LeapMetaModelHelper.getArraylistofList(insertableValues);
		final int valCount = insertableValues.size();
		//logger.debug("DateChecking: " + insertableValues.get(0));
		/** New InsertInto MetaModel derived instance **/
		InsertInto insertInto = new InsertInto(table);
		Set<String> columnSet;
		if (colCount != 0) {
			/** check for equal params **/
			if (colCount == valCount) {
				/**
				 * Set of ColumnNames, from the String[].. to avoid duplicate
				 * names
				 **/
				//logger.debug("InsertableCol name: " + insertableColumns.get(1));
				columnSet = LeapMetaModelHelper.getSetofColumns(insertableColumns);
				int index = 0;
				/** Iterating values, for the given number of Columns **/
				logger.debug("Column: " + columnSet + " Values: " + insertableValues);
				for (String column : columnSet) {
					insertInto.value(column.trim(), insertableValues.get(index));
					index++;
				} // ..end of for-loop
			} // ..end of if
			else {
				throw new InvalidSqlParamsCountException(
						"Colum count " + colCount + " & Values count " + valCount + " missmatched");
			} // ..end of else
		} // ..end of if
		else {
			/**
			 * Set of ColumnNames, from the getColumsbyTable[].. to avoid
			 * duplicate names
			 **/
			int index = 0;
			insertableColumns = LeapMetaModelHelper.getColumsbyTable(table);
			/** Iterate over values **/
			for (Object object : insertableValues) {
				insertInto.value(insertableColumns.get(index).toString().trim(), object);
				index = index + 1;
			} // ..end of for-each
		} // ..end of else
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		insertInto.value(TenantSecurityConstant.TENANT_ID, leapHeader.getTenant());
		insertInto.value(TenantSecurityConstant.SITE_ID, leapHeader.getSite());

		jdbcDataContext.executeUpdate(insertInto);
		logger.info("Value has been inserted succesfully..!");

	}// ..end of the method

	/**
	 * to process the update the table objects
	 * 
	 * @param tableName
	 * @param jdbcDataContext
	 * @param updatableColumns
	 * @param values
	 * @param predicates
	 * @throws Exception
	 */
	public void doLeapUpdate(String tableName, String predicateString, List<?> predicatefieldList,
			List<String> updatableColumns, List<?> values, Exchange exchange) throws Exception {
		logger.debug(".processMetaModelUpdate().. tableName: " + tableName + " values: " + values);
		if (tableName == null) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + tableName);
		}
		updatableColumns = LeapMetaModelHelper.validateListGeneric(updatableColumns);
		values = LeapMetaModelHelper.validateListGeneric(values);
		JdbcDataContext jdbcDataContext = (JdbcDataContext) getDataContext(exchange, UPDATE_OPERATION);

		/** final Table instance by name - MetaModel **/
		final Table table = jdbcDataContext.getTableByQualifiedLabel(tableName);
		predicateString = LeapMetaModelHelper.validatePredicateList(predicateString);
		predicatefieldList = LeapMetaModelHelper.validatePredicateFieldList(predicatefieldList);
		final int colCount = updatableColumns.size();
		final int valCount = values.size();
		Set<String> columnSet;
		String predicateMapped = LeapMetaModelHelper.getMappedStringFromList(predicateString, predicatefieldList);
		/** Update object initialization **/
		Update updateObject = new Update(table);
		columnSet = LeapMetaModelHelper.getSetofColumns(updatableColumns);
		logger.debug("ColumnSet returned: " + columnSet);
		int index = 0;
		if (LeapMetaModelHelper.isColumnToValueCountEqual(colCount, valCount)) {
			/** if predicates are empty **/
			if (LeapMetaModelHelper.isPredicateEmpty(predicateMapped)) {
				logger.info("Updating all the matching columns, since no predicates are mentioned.");
				/** Looping if no predicates **/
				for (String column : columnSet) {
					logger.debug("Checking the columns to be updated: " + column + " & the values" + values.get(index)
							+ " respectively.");
					updateObject.value(column, values.get(index));
					index = index + 1;
				} // ..end of for-each
			} // ..end of if
			else {
				for (String column : columnSet) {
					updateObject.value(column, values.get(index));
					index = index + 1;
				} // ..end of for-each
				updateObject.where(LeapMetaModelHelper.getFilterItem(predicateMapped));
			} // ..end of else
			LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
			updateObject.where(TenantSecurityConstant.TENANT_ID).eq(leapHeader.getTenant())
					.where(TenantSecurityConstant.SITE_ID).eq(leapHeader.getSite());
			jdbcDataContext.executeUpdate(updateObject);
			logger.info("Value has been Updated succesfully..!");
		} else {
			throw new InvalidSqlParamsCountException(
					"Colum count " + colCount + " & Values count " + valCount + " missmatched");
		}
	}// ..end of the method

	/**
	 * service to delete rows from the table wrapped over the metamodel
	 * 
	 * @param tableName
	 * @param jdbcDataContext
	 * @param predicates
	 * @throws Exception
	 */
	public void doLeapDelete(String tableName, String predicateString, List<?> predicatefieldList, Exchange exchange)
			throws Exception {
		if (tableName == null) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + tableName);
		}
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		JdbcDataContext jdbcDataContext = (JdbcDataContext) getDataContext(exchange, DELETE_OPERATION);
		/** final instance of the table by name **/
		final Table table = jdbcDataContext.getTableByQualifiedLabel(tableName);
		predicateString = LeapMetaModelHelper.validatePredicateList(predicateString);
		predicatefieldList = LeapMetaModelHelper.validatePredicateFieldList(predicatefieldList);
		/* If no predicates then called for delete all from the table */
		if (LeapMetaModelHelper.isPredicateEmpty(predicateString)) {
			jdbcDataContext.executeUpdate(new DeleteFrom(table).where(TenantSecurityConstant.TENANT_ID)
					.eq(leapHeader.getTenant()).where(TenantSecurityConstant.SITE_ID).eq(leapHeader.getSite()));
			logger.info("Successfully deleted all data from the given table..");
		} // ..end of if, to delete all from table
		else {
			String mapOfConstraint = LeapMetaModelHelper.getMappedStringFromList(predicateString, predicatefieldList);
			jdbcDataContext.executeUpdate(new DeleteFrom(table)
					.where(LeapMetaModelHelper.getFilterItem(mapOfConstraint)).where(TenantSecurityConstant.TENANT_ID)
					.eq(leapHeader.getTenant()).where(TenantSecurityConstant.SITE_ID).eq(leapHeader.getSite()));
			logger.info("Successfully deleted a row data from the given table,for the given predicate.");
		} // ..end of else, delete where column name given
	}// ..end of the method

	/**
	 * to process the select operation, for (select all, select columns, select
	 * columns where)
	 * 
	 * @param tableName
	 * @param jdbcDataContext
	 * @param predicates
	 * @return DataSet
	 * @throws Exception
	 */
	public DataSet doLeapSelect(String tableName, String predicateString, List<?> predicatefieldList,
			List<String> selectableColumns, Exchange exchange) throws Exception {
		logger.debug("Select Api is called..");
		if (tableName == null) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + tableName);
		}
		if (selectableColumns == null) {
			selectableColumns = new ArrayList<>();
		}
		DataContext dataContext = getDataContext(exchange, SELECT_OPERATION);
		/** final instance of the table by name **/
		final Table table = dataContext.getTableByQualifiedLabel(tableName);
		predicatefieldList = LeapMetaModelHelper.validatePredicateFieldList(predicatefieldList);
		predicateString = LeapMetaModelHelper.validatePredicateList(predicateString);
		DataSet ds;
		final boolean isEmptyPredicate = LeapMetaModelHelper.isPredicateEmpty(predicateString);
		final boolean isEmptyColumns = LeapMetaModelHelper.isEmptyList(selectableColumns);
		String substitutedPredicates = LeapMetaModelHelper.getMappedStringFromList(predicateString, predicatefieldList);
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		Query q;
		if (isEmptyPredicate && isEmptyColumns) {
			logger.debug("Select all is called.");
			q = dataContext.query().from(table).selectAll().where(TenantSecurityConstant.TENANT_ID)
					.eq(leapHeader.getTenant()).and(TenantSecurityConstant.SITE_ID).eq(leapHeader.getSite()).toQuery();
		} else if (isEmptyPredicate && !isEmptyColumns) {
			String[] columnNames = new String[selectableColumns.size()];
			columnNames = selectableColumns.toArray(columnNames);
			q = dataContext.query().from(table).select(columnNames).where(TenantSecurityConstant.TENANT_ID)
					.eq(leapHeader.getTenant()).and(TenantSecurityConstant.SITE_ID).eq(leapHeader.getSite()).toQuery();
		} else if (!isEmptyPredicate && isEmptyColumns) {
			q = dataContext.query().from(table).selectAll().where(TenantSecurityConstant.TENANT_ID)
					.eq(leapHeader.getTenant()).and(TenantSecurityConstant.SITE_ID).eq(leapHeader.getSite())
					.where(LeapMetaModelHelper.getFilterItem(substitutedPredicates)).toQuery();
		} else {
			String[] columnNames = new String[selectableColumns.size()];
			columnNames = selectableColumns.toArray(columnNames);
			q = dataContext.query().from(table).select(columnNames)
					.where(LeapMetaModelHelper.getFilterItem(substitutedPredicates))
					.where(TenantSecurityConstant.TENANT_ID).eq(leapHeader.getTenant())
					.and(TenantSecurityConstant.SITE_ID).eq(leapHeader.getSite()).toQuery();
		}

		ds = dataContext.executeQuery(q);
		return ds;
	}// ..end of the method

	/**
	 * to perform functionType select on Function
	 * 
	 * @param functionType
	 * @param columnOnFuncApplied
	 * @param tableName
	 * @param exchange
	 * @return MetaModel-DataSet Object
	 * @throws Exception
	 */
	public DataSet doLeapSelectByFunction(FunctionType functionType, String columnOnFuncApplied, String tableName,
			String predicateString, List predicateFieldList, Exchange exchange) throws Exception {
		if (tableName == null) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + tableName);
		}
		DataContext dataContext = getDataContext(exchange, SELECT_OPERATION);
		final Table table = dataContext.getTableByQualifiedLabel(tableName);
		predicateString = LeapMetaModelHelper.validatePredicateList(predicateString);
		predicateFieldList = LeapMetaModelHelper.validatePredicateFieldList(predicateFieldList);
		Query q = null;
		DataSet ds;
		TableFromBuilder tableFromBuilder = dataContext.query().from(table);
		final boolean isEmptypredicateString = LeapMetaModelHelper.isPredicateEmpty(predicateString);
		final boolean isEmptypredicateFieldList = LeapMetaModelHelper.isEmptyList(predicateFieldList);
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		if (isEmptypredicateString && isEmptypredicateFieldList) {
			FunctionSelectBuilder<?> fSelectBuilder = tableFromBuilder.select(functionType, columnOnFuncApplied);
			q = fSelectBuilder.where(TenantSecurityConstant.TENANT_ID).eq(leapHeader.getTenant())
					.and(TenantSecurityConstant.SITE_ID).eq(leapHeader.getSite()).toQuery();
		} else if (!isEmptypredicateString && !isEmptypredicateFieldList) {
			String substitutedPredicates = LeapMetaModelHelper.getMappedStringFromList(predicateString,
					predicateFieldList);
			SatisfiedQueryBuilder<?> funcSelectSatisfiedBuilder = tableFromBuilder
					.select(functionType, columnOnFuncApplied)
					.where(LeapMetaModelHelper.getFilterItem(substitutedPredicates));
			q = funcSelectSatisfiedBuilder.where(TenantSecurityConstant.TENANT_ID).eq(leapHeader.getTenant())
					.and(TenantSecurityConstant.SITE_ID).eq(leapHeader.getSite()).toQuery();
		}

		ds = dataContext.executeQuery(q);
		return ds;
	}// ..end of the method

	/**
	 * Wrapper to perform join query selects, need to use the LeapMMConstants to
	 * set the jointypes
	 * 
	 * @param queryFromtableName
	 * @param joinObject
	 * @param columnsToCompare
	 * @param columnsToquery
	 * @param joinType
	 * @param exchange
	 * @return
	 * @throws Exception
	 */
	public DataSet doLeapJoinSelect(String queryFromtableName, String joinObject, List<String> columnsToCompare,
			List<String> columnsToquery, String joinType, Exchange exchange) throws Exception {
		DataContext dataContext = getDataContext(exchange, SELECT_OPERATION);
		Query query = LeapMetaModelHelper.buildJoinQueries(dataContext, queryFromtableName, joinObject,
				columnsToCompare, columnsToquery, joinType);
		return dataContext.executeQuery(query);
	}// ..end of the method

	public com.attunedlabs.datacontext.jaxb.DataContext loadDataContext(Exchange exchange) throws Exception {
		return  (com.attunedlabs.datacontext.jaxb.DataContext) getFeatureDataContext(exchange);
	}
	
	/**
	 * Utility to return JdbcDataContext - metamodel
	 * 
	 * @param exchange
	 * @return
	 * @throws Exception
	 */
	private DataContext getDataContext(Exchange exchange, String operation) throws Exception {

		com.attunedlabs.datacontext.jaxb.DataContext dataContext = loadDataContext(exchange);
		String dbRefName = dataContext.getDbBeanRefName();
		String dbHost = dataContext.getDbHost();
		String dbPort = dataContext.getDbPort();
		String dbSchema = dataContext.getDbSchema();
		String dbUser = dataContext.getDbUser();
		String dbPassword = dataContext.getDbPassword();
		String dbType = dataContext.getDbType().toUpperCase();
		String dbUrl = dataContext.getDbUrl();
		String dbDriver = dataContext.getDbDriver();
		logger.debug("Logging DbReferenceName: " + dbRefName + " dbType : " + dbType);
		if ((dbType.equals(MYSQL_DB_TYPE) || dbType.equals(POSTGRESQL_DB_TYPE) || dbType.equals(SQLSERVER_DB_TYPE)
				|| dbType.equals(ORACLE_DB_TYPE)) && checkNonEmptyString(dbRefName)) {
			CamelContext camelContext = exchange.getContext();
			DataSource dataSourcegetting = (DataSource) camelContext.getRegistry().lookupByName(dbRefName);
			logger.debug("dataSourcegetting object: " + dataSourcegetting);
			/** setting DataSource **/
			setDataSource(dataSourcegetting);
			/** Getting the instance of JdbcDataContext **/
			JdbcDataContext jdbcDataContext = getLocalDataContext(exchange);
			return jdbcDataContext;
		} else if (checkDBProperties(dataContext, operation) != null) {

			DataContextPropertiesImpl properties = new DataContextPropertiesImpl();
			// main foctor for deciding implementation
			properties.setDataContextType(checkDBProperties(dataContext, operation));
			properties.put(DataContextPropertiesImpl.PROPERTY_HOSTNAME, dbHost);
			properties.put(DataContextPropertiesImpl.PROPERTY_PORT, dbPort);
			properties.put(DataContextPropertiesImpl.PROPERTY_URL, dbUrl);
			properties.put(DataContextPropertiesImpl.PROPERTY_DRIVER_CLASS, dbDriver);
			properties.put(DataContextPropertiesImpl.PROPERTY_USERNAME, dbUser);
			properties.put(DataContextPropertiesImpl.PROPERTY_PASSWORD, dbPassword);
			properties.put(DataContextPropertiesImpl.PROPERTY_DATABASE, dbSchema);
			return DataContextFactoryRegistryImpl.getDefaultInstance().createDataContext(properties);
		} else
			throw new UnableToFetchAppropriateContext("Failed to fetch appropriate datacontext for DbReferenceName: "
					+ dbRefName + " dbType : " + dbType);

	}

	private String checkDBProperties(com.attunedlabs.datacontext.jaxb.DataContext dataContext, String operation) {
		logger.debug("checkDBProperties : " + dataContext);
		String dbHost = dataContext.getDbHost();
		String dbPort = dataContext.getDbPort();
		String dbSchema = dataContext.getDbSchema();
		String dbType = dataContext.getDbType().toUpperCase();
		String dbUrl = dataContext.getDbUrl();
		String dbDriver = dataContext.getDbDriver();

		if (checkNonEmptyString(dbHost) && checkNonEmptyString(dbPort) && checkNonEmptyString(dbSchema)
				&& dbType.equals(CASSANDRA_DB_TYPE) && operation.equals(SELECT_OPERATION))
			return "cassandra";
		else if (checkNonEmptyString(dbUrl) && checkNonEmptyString(dbDriver) && !operation.equals(SELECT_OPERATION))
			return "jdbc";
		return null;
	}

	private boolean checkNonEmptyString(String checkString) {
		return checkString != null && !checkString.isEmpty();
	}

	private boolean checkEmptyString(String checkString) {
		if (checkString == null)
			return true;
		return checkString.trim().isEmpty();
	}

	/**
	 * Not implementing , from parent
	 */
	@Override
	protected void processBean(Exchange exch) throws Exception {
		// TODO Auto-generated method stub

	}
}