package com.getusroi.eventframework.abstractbean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.delete.DeleteFrom;
import org.apache.metamodel.insert.InsertInto;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.FunctionType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.builder.FunctionSelectBuilder;
import org.apache.metamodel.query.builder.SatisfiedQueryBuilder;
import org.apache.metamodel.query.builder.TableFromBuilder;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.Update;

import com.getusroi.datacontext.jaxb.DataContext;

/**
 * Wrapper for the MetaModel CRUD
 * 
 * @author GetUsRoi
 *
 */
public class MeshMetaModelBean extends AbstractMetaModelBean {

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
	public void doMeshInsert(String objectName, List<String> insertableColumns, List<Object> insertableValues,
			Exchange exchange) throws Exception {
		if (objectName == null) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + objectName);
		}
		insertableColumns = MeshMetaModelHelper.validateListGeneric(insertableColumns);
		insertableValues = MeshMetaModelHelper.validateListGeneric(insertableValues);
		JdbcDataContext jdbcDataContext = getJdbcDataContext(exchange);

		/** final Table instance by name - MetaModel **/
		final Table table = jdbcDataContext.getTableByQualifiedLabel(objectName);
		final int colCount = insertableColumns.size();
		insertableValues = MeshMetaModelHelper.getArraylistofList(insertableValues);
		final int valCount = insertableValues.size();
		logger.debug("DateChecking: " + insertableValues.get(1));
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
				logger.debug("InsertableCol name: " + insertableColumns.get(1));
				columnSet = MeshMetaModelHelper.getSetofColumns(insertableColumns);
				int index = 0;
				/** Iterating values, for the given number of Columns **/
				for (String column : columnSet) {
					logger.debug("Column: " + column.trim() + " Values: " + insertableValues.get(1));
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
			insertableColumns = MeshMetaModelHelper.getColumsbyTable(table);
			/** Iterate over values **/
			for (Object object : insertableValues) {
				insertInto.value(insertableColumns.get(index).toString().trim(), object);
				index = index + 1;
			} // ..end of for-each
		} // ..end of else
		jdbcDataContext.executeUpdate(insertInto);
		logger.info("Valuse has been inserted succesfully..!");
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
	public void doMeshUpdate(String objectName, String predicateList, List<?> predicatefieldList,
			List<String> updatableColumns, List<?> values, Exchange exchange) throws Exception {
		logger.debug(".processMetaModelUpdate().. tableName: " + objectName + " values: " + values);
		if (objectName == null) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + objectName);
		}
		updatableColumns = MeshMetaModelHelper.validateListGeneric(updatableColumns);
		values = MeshMetaModelHelper.validateListGeneric(values);
		JdbcDataContext jdbcDataContext = getJdbcDataContext(exchange);

		/** final Table instance by name - MetaModel **/
		final Table table = jdbcDataContext.getTableByQualifiedLabel(objectName);
		predicateList = MeshMetaModelHelper.validatePredicateList(predicateList);
		predicatefieldList = MeshMetaModelHelper.validatePredicateFieldList(predicatefieldList);
		final int colCount = updatableColumns.size();
		final int valCount = values.size();
		Set<String> columnSet;
		String predicateMapped = MeshMetaModelHelper.getMappedStringFromList(predicateList, predicatefieldList);
		/** Update object initialization **/
		Update updateObject = new Update(table);
		columnSet = MeshMetaModelHelper.getSetofColumns(updatableColumns);
		logger.debug("ColumnSet returned: " + columnSet);
		int index = 0;
		if (MeshMetaModelHelper.isColumnToValueCountEqual(colCount, valCount)) {
			/** if predicates are empty **/
			if (MeshMetaModelHelper.isPredicateEmpty(predicateMapped)) {
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
				updateObject.where(MeshMetaModelHelper.getFilterItem(predicateMapped));
			} // ..end of else

			jdbcDataContext.executeUpdate(updateObject);
			logger.info("Valuse has been Updated succesfully..!");
		} else {
			throw new InvalidSqlParamsCountException(
					"Colum count " + colCount + " & Values count " + valCount + " missmatched");
		}
	}// ..end of the method

	/**
	 * service to delete rows from the table wrapped over the metamodel
	 * 
	 * @param objectName
	 * @param jdbcDataContext
	 * @param predicates
	 * @throws Exception
	 */
	public void doMeshDelete(String objectName, String predicateList, List<?> predicatefieldList, Exchange exchange)
			throws Exception {
		if (objectName == null) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + objectName);
		}
		JdbcDataContext jdbcDataContext = getJdbcDataContext(exchange);
		/** final instance of the table by name **/
		final Table table = jdbcDataContext.getTableByQualifiedLabel(objectName);
		predicateList = MeshMetaModelHelper.validatePredicateList(predicateList);
		predicatefieldList = MeshMetaModelHelper.validatePredicateFieldList(predicatefieldList);
		/* If no predicates then called for delete all from the table */
		if (MeshMetaModelHelper.isPredicateEmpty(predicateList)) {
			jdbcDataContext.executeUpdate(new DeleteFrom(table));
			logger.info("Successfully deleted all data from the given table..");
		} // ..end of if, to delete all from table
		else {

			String mapOfConstraint = MeshMetaModelHelper.getMappedStringFromList(predicateList, predicatefieldList);
			jdbcDataContext
					.executeUpdate(new DeleteFrom(table).where(MeshMetaModelHelper.getFilterItem(mapOfConstraint)));
			logger.info("Successfully deleted a row data from the given table,for the given predicate.");
		} // ..end of else, delete where column name given
	}// ..end of the method

	/**
	 * to process the select operation, for (select all, select columns, select
	 * columns where)
	 * 
	 * @param objectName
	 * @param jdbcDataContext
	 * @param predicates
	 * @return DataSet
	 * @throws Exception
	 */
	public DataSet doMeshSelect(String objectName, String predicateList, List<?> predicatefieldList,
			List<String> selectableColumns, Exchange exchange) throws Exception {
		logger.debug("Select Api is called..");
		if (objectName == null) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + objectName);
		}
		if (selectableColumns == null) {
			selectableColumns = new ArrayList<>();
		}
		JdbcDataContext jdbcDataContext = getJdbcDataContext(exchange);
		/** final instance of the table by name **/
		final Table table = jdbcDataContext.getTableByQualifiedLabel(objectName);
		predicatefieldList = MeshMetaModelHelper.validatePredicateFieldList(predicatefieldList);
		predicateList = MeshMetaModelHelper.validatePredicateList(predicateList);
		DataSet ds;
		final boolean isEmptyPredicate = MeshMetaModelHelper.isPredicateEmpty(predicateList);
		final boolean isEmptyColumns = MeshMetaModelHelper.isEmptyList(selectableColumns);
		String substitutedPredicates = MeshMetaModelHelper.getMappedStringFromList(predicateList, predicatefieldList);
		Query q;
		if (isEmptyPredicate && isEmptyColumns) {
			logger.debug("Select all is called.");
			q = jdbcDataContext.query().from(table).selectAll().toQuery();
		} else if (isEmptyPredicate && !isEmptyColumns) {
			String[] columnNames = new String[selectableColumns.size()];
			columnNames = selectableColumns.toArray(columnNames);
			q = jdbcDataContext.query().from(table).select(columnNames).toQuery();
		} else if (!isEmptyPredicate && isEmptyColumns) {
			q = jdbcDataContext.query().from(table).selectAll()
					.where(MeshMetaModelHelper.getFilterItem(substitutedPredicates)).toQuery();
		} else {
			String[] columnNames = new String[selectableColumns.size()];
			columnNames = selectableColumns.toArray(columnNames);
			q = jdbcDataContext.query().from(table).select(columnNames)
					.where(MeshMetaModelHelper.getFilterItem(substitutedPredicates)).toQuery();
		}
		ds = jdbcDataContext.executeQuery(q);
		return ds;
	}// ..end of the method

	/**
	 * to perform functionType select on Function
	 * 
	 * @param functionType
	 * @param columnOnFuncApplied
	 * @param objectName
	 * @param exchange
	 * @return MetaModel-DataSet Object
	 * @throws Exception
	 */
	public DataSet doMeshSelectByFunction(FunctionType functionType, String columnOnFuncApplied, String objectName,
			String predicateList, List predicateFieldList, Exchange exchange) throws Exception {
		if (objectName == null) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + objectName);
		}
		JdbcDataContext jdbcDataContext = getJdbcDataContext(exchange);
		final Table table = jdbcDataContext.getTableByQualifiedLabel(objectName);
		predicateList = MeshMetaModelHelper.validatePredicateList(predicateList);
		predicateFieldList = MeshMetaModelHelper.validatePredicateFieldList(predicateFieldList);
		Query q = null;
		DataSet ds;
		TableFromBuilder tableFromBuilder = jdbcDataContext.query().from(table);
		final boolean isEmptypredicateList = MeshMetaModelHelper.isPredicateEmpty(predicateList);
		final boolean isEmptypredicateFieldList = MeshMetaModelHelper.isEmptyList(predicateFieldList);
		if (isEmptypredicateList && isEmptypredicateFieldList) {
			FunctionSelectBuilder<?> fSelectBuilder = tableFromBuilder.select(functionType, columnOnFuncApplied);
			q = fSelectBuilder.toQuery();
		} else if (!isEmptypredicateList && !isEmptypredicateFieldList) {
			String substitutedPredicates = MeshMetaModelHelper.getMappedStringFromList(predicateList,
					predicateFieldList);
			SatisfiedQueryBuilder<?> funcSelectSatisfiedBuilder = tableFromBuilder
					.select(functionType, columnOnFuncApplied)
					.where(MeshMetaModelHelper.getFilterItem(substitutedPredicates));
			q = funcSelectSatisfiedBuilder.toQuery();
		}
		ds = jdbcDataContext.executeQuery(q);
		return ds;
	}// ..end of the method

	/**
	 * Wrapper to perform join query selects, need to use the MeshMMConstants to
	 * set the jointypes
	 * 
	 * @param queryFromObjectName
	 * @param joinObject
	 * @param columnsToCompare
	 * @param columnsToquery
	 * @param joinType
	 * @param exchange
	 * @return
	 * @throws Exception
	 */
	public DataSet doMeshJoinSelect(String queryFromObjectName, String joinObject, List<String> columnsToCompare,
			List<String> columnsToquery, String joinType, Exchange exchange) throws Exception {
		JdbcDataContext jdbcDataContext = getJdbcDataContext(exchange);
		Query query = MeshMetaModelHelper.buildJoinQueries(jdbcDataContext, queryFromObjectName, joinObject,
				columnsToCompare, columnsToquery, joinType);
		return jdbcDataContext.executeQuery(query);
	}// ..end of the method

	/**
	 * Utility to return JdbcDataContext - metamodel
	 * 
	 * @param exchange
	 * @return
	 * @throws Exception
	 */
	private JdbcDataContext getJdbcDataContext(Exchange exchange) throws Exception {

		DataContext dataContext = getFeatureDataContext(exchange);
		String dbRefName = dataContext.getDbBeanRefName();
		logger.debug("Logging DbReferenceName: " + dbRefName);

		CamelContext camelContext = exchange.getContext();
		DataSource dataSourcegetting = (DataSource) camelContext.getRegistry().lookupByName(dbRefName);
		logger.debug("dataSourcegetting object: " + dataSourcegetting);
		/** setting DataSource **/
		setDataSource(dataSourcegetting);
		/** Getting the instance of JdbcDataContext **/
		JdbcDataContext jdbcDataContext = getLocalDataContext(exchange);
		return jdbcDataContext;
	}// ..end of the method

	/**
	 * Not implementing , from parent
	 */
	@Override
	protected void processBean(Exchange exch) throws Exception {
		// TODO Auto-generated method stub

	}
}
