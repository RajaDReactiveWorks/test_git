package org.apache.metamodel.cassandra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.QueryPostprocessDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.ScalarFunction;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;

/**
 * Abstract DataContext for data sources that do not support SQL queries
 * natively.
 * 
 * Instead this superclass only requires that a subclass can materialize a
 * single table at a time. Then the query will be executed by post processing
 * the datasets client-side.
 */
public abstract class CassandraCustomQueryProcessor extends QueryPostprocessDataContext {
	public static final String INFORMATION_SCHEMA_NAME = "information_schema";

	public CassandraCustomQueryProcessor() {
		super();
	}

	/**
	 * Execute a simple one-table query against a table in the main schema of
	 * the subclasses of this class. This default implementation will delegate
	 * to {@link #materializeMainSchemaTable(Table, List, int, int)} and apply
	 * WHERE item filtering afterwards.
	 * 
	 * @param table
	 * @param selectItems
	 * @param whereItems
	 * @param firstRow
	 * @param maxRows
	 * @return
	 */
	protected DataSet materializeMainSchemaTable(Table table, List<SelectItem> selectItems, List<FilterItem> whereItems,
			int firstRow, int maxRows) {
		final List<SelectItem> workingSelectItems = buildWorkingSelectItems(selectItems, whereItems);
		DataSet dataSet;
		if (whereItems.isEmpty()) {
			// paging is pushed down to materializeMainSchemaTable
			dataSet = super.materializeMainSchemaTableSelect(table, workingSelectItems, firstRow, maxRows);
			// dataSet = super.materializeMainSchemaTable(table, selectItems,
			// // whereItems, firstRow, maxRows);
			dataSet = MetaModelHelper.getSelection(selectItems, dataSet);
		} else {
			// do not push down paging, first we have to apply filtering
			dataSet = materializeMainSchemaTableTemp(table, workingSelectItems, whereItems, 1, maxRows);
			// dataSet = MetaModelHelper.getFiltered(dataSet, whereItems);
			dataSet = MetaModelHelper.getPaged(dataSet, firstRow, maxRows);
			// dataSet = MetaModelHelper.getSelection(selectItems, dataSet);
		}
		return dataSet;

		// return super.materializeMainSchemaTable(table, selectItems,
		// whereItems, firstRow, maxRows);
	}

	protected DataSet materializeMainSchemaTableTemp(Table table, List<SelectItem> selectItems,
			List<FilterItem> whereItems, int firstRow, int maxRows) {
		Column[] columns = new Column[selectItems.size()];
		String select = "";
		String mainSchema = table.getSchema().getName();

		for (int i = 0; i < columns.length; i++) {
			columns[i] = selectItems.get(i).getColumn();
			select += selectItems.get(i).toString() + " ";
			if (i < (columns.length - 1)) {
				select += ", ";
			}
		}
		String sql = null;
		String where = "";


		
		ArrayList<FilterItem> filterItems = new  ArrayList<>();
		filterItems.addAll(whereItems);
		
		int size = filterItems.size() - 1;

		where = getWhereClause(where, filterItems, size);


		sql = "SELECT " + select + " FROM " + mainSchema + "." + table.getName();

		if (!where.isEmpty()) {
			sql += " " + where;
		}

		if (maxRows > 0) {
			sql += " LIMIT " + maxRows;
		}

		if (!where.isEmpty()) {
			sql += " allow filtering";
		}
		// adding offset to the time manually considering that utc time is
		// passes
		//sql = addTimezoneOffset(sql);

		sql = sql.replaceAll("TIMESTAMP", "");

		sql = sql.replaceAll(table.getName() + "\\.", "");
		System.out.println(sql);
		DataSet dataSet = materializeMainSchemaTableUsingSql(table, columns, sql, maxRows);

		// dataSet = MetaModelHelper.getSelection(selectItems, dataSet);

		return dataSet;
	}

	private String getWhereClause(String where, List<FilterItem> whereItems, int size) {
		int count = 0;
		for (FilterItem item : whereItems) {
			try {
				if (count == 0) {
					where += " where ";
				}
				where += item + " ";
				if (count < size) {
					where += " AND ";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			count++;
		}
		return where;
	}

	private String addTimezoneOffset(String sql) {
		String[] a = sql.split("TIMESTAMP");
		for (int i = 0; i < a.length; i++) {
			if (i == 0)
				System.out.println("");
			else {
				String dateString = a[i].substring(a[i].indexOf("'") + 1, a[i].indexOf("'", a[i].indexOf("'") + 1));
				String timeZoneOffset = "+0000";
				sql = sql.replaceAll(dateString, dateString + timeZoneOffset);
			}

		}
		return sql;
	}

	private List<SelectItem> buildWorkingSelectItems(List<SelectItem> selectItems, List<FilterItem> whereItems) {
		final List<SelectItem> primarySelectItems = new ArrayList<>(selectItems.size());
		for (SelectItem selectItem : selectItems) {
			final ScalarFunction scalarFunction = selectItem.getScalarFunction();
			if (scalarFunction == null || isScalarFunctionMaterialized(scalarFunction)) {
				primarySelectItems.add(selectItem);
			} else {
				final SelectItem copySelectItem = selectItem.replaceFunction(null);
				primarySelectItems.add(copySelectItem);
			}
		}
		final List<SelectItem> evaluatedSelectItems = MetaModelHelper.getEvaluatedSelectItems(whereItems);
		return CollectionUtils.concat(true, primarySelectItems, evaluatedSelectItems);
	}

	protected abstract DataSet materializeMainSchemaTableUsingSql(Table table, Column[] columns, String sql,
			int maxRows);

}