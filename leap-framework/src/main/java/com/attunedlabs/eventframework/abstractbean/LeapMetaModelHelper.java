package com.attunedlabs.eventframework.abstractbean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeapMetaModelHelper {

	private static final Logger logger = LoggerFactory.getLogger(AbstractLeapCamelBean.class);

	/**
	 * To restrict a construct of instance
	 */
	private LeapMetaModelHelper() {
	}

	/**
	 * Query builder to build join queries
	 * 
	 * @param jdbcDataContext
	 * @param queryFromObject
	 * @param joinObject
	 * @param columnsToCompare
	 * @param columnsToquery
	 * @param joinType
	 * @return
	 * @throws InvalidOperationException
	 */
	protected static Query buildJoinQueries(DataContext jdbcDataContext, String queryFromObject, String joinObject,
			List<String> columnsToCompare, List<String> columnsToquery, String joinType)
			throws InvalidOperationException {
		if (columnsToCompare.size() > 2) {
			throw new InvalidOperationException("Size of columnsToCompare is unexpectedly greater than 2");
		}
		final Table fromTable = jdbcDataContext.getTableByQualifiedLabel(queryFromObject);
		final Table joinTable = jdbcDataContext.getTableByQualifiedLabel(joinObject);
		String[] columnNames = new String[columnsToquery.size()];
		columnNames = columnsToquery.toArray(columnNames);
		switch (joinType) {
		case LeapMMConstants.INNER_JOIN:
			return jdbcDataContext.query().from(fromTable).innerJoin(joinTable)
					.on(columnsToCompare.get(0), columnsToCompare.get(1)).select(columnNames).toQuery();
		case LeapMMConstants.LEFT_JOIN:
			return jdbcDataContext.query().from(fromTable).leftJoin(joinTable)
					.on(columnsToCompare.get(0), columnsToCompare.get(1)).select(columnNames).toQuery();
		case LeapMMConstants.RIGHT_JOIN:
			return jdbcDataContext.query().from(fromTable).rightJoin(joinTable)
					.on(columnsToCompare.get(0), columnsToCompare.get(1)).select(columnNames).toQuery();
		default:
			throw new InvalidOperationException("Unable to build the query to perform join..!");
		}
	}// ..end of the method

	/**
	 * utility converting two lists in to map key value
	 * 
	 * @param keys
	 * @param values
	 * @return listToMap
	 */
	protected static String getMappedStringFromList(String substitutableString, List<?> substitutableValues) {
		logger.debug("Keys: " + substitutableString + " Values: " + substitutableValues);
		String temp = "";
		if (!LeapMetaModelHelper.isEmptyList(substitutableValues)) {
			for (Object object : substitutableValues) {
				if (object instanceof String) {
					object = "'" + object.toString().trim() + "'";
				} else if (object instanceof Date)
					object = "'" + ((Date) object).toString().trim() + "'";

				temp = substitutableString.replaceFirst(Pattern.quote("?"), object.toString().trim());
				substitutableString = temp;
			}
		}
		logger.debug("Predicates Mapped and returning: " + substitutableString);
		return temp;
	}// ..end of the method

	/**
	 * Utility to validate predicateListString
	 * 
	 * @param predicateList
	 * @return
	 */
	protected static String validatePredicateList(String predicateList) {
		if (predicateList == null) {
			return predicateList = new String();
		} else {
			return predicateList;
		}
	}// ..end of the method

	/**
	 * Utility to validate the list for exception-less flow
	 * 
	 * @param predicateFieldList
	 * @return
	 */
	protected static List validateListGeneric(List list) {
		if (list == null) {
			return new ArrayList<>();
		} else {
			return list;
		}
	}// ..end of the method

	/**
	 * Utility to validate the list for exception-less flow
	 * 
	 * @param predicateFieldList
	 * @return
	 */
	protected static List validatePredicateFieldList(List predicateFieldList) {
		if (predicateFieldList == null) {
			return new ArrayList<>();
		} else {
			return predicateFieldList;
		}
	}// ..end of the method

	/**
	 * Utility for preparing FilterItem which is expression based constraint
	 * filtering
	 * 
	 * @param predicates
	 * @return
	 * @throws InvalidPredicateException
	 */
	protected static FilterItem getFilterItem(String predicates) throws InvalidPredicateException {
		logger.debug(".getFilterItem().. Predicates: " + predicates);
		if (predicates == null) {
			throw new InvalidPredicateException("Null is not a valid predicate");
		}
		String expression = predicates;
		return new FilterItem(expression);
	}// ..end of the method

	/**
	 * Utility for checking the empty predicate
	 * 
	 * @param predicates
	 * @return
	 */
	protected static boolean isPredicateEmpty(String predicates) {
		return (predicates.isEmpty() || predicates == null) ? true : false;
	}// ..end of the method

	/**
	 * Utility to validate empty list
	 * 
	 * @param inParams
	 * @return
	 */
	protected static boolean isEmptyList(List<?> inParams) {
		logger.debug("InParams: " + inParams);
		return (inParams.size() == 0 || inParams.isEmpty() || inParams == null || inParams.equals(null)) ? true : false;
	}// ..end of the method

	/**
	 * Validate mapping between the Column and Value count
	 * 
	 * @param colCount
	 * @param valCount
	 * @return bool
	 */
	protected static boolean isColumnToValueCountEqual(int colCount, int valCount) {
		return (colCount == valCount) ? true : false;
	}// ..end of the method

	/**
	 * To eradicate the ambiguity of String array
	 * 
	 * @param columns
	 * @return
	 */
	protected static Set<String> getSetofColumns(List<String> columns) {
		return new LinkedHashSet<>(columns);
	}// ..end of the method

	/**
	 * Utility to check whether EmptyColumns specified
	 * 
	 * @param columnNames
	 * @return boolean
	 */
	protected static boolean isEmptyColums(String[] columnNames) {
		return (columnNames.length == 0) ? true : false;
	}// ..end of the method

	/**
	 * Utility to convert List to ArrayList
	 * 
	 * @param list
	 * @return
	 */
	protected static ArrayList<Object> getArraylistofList(List<Object> list) {
		return new ArrayList<>(list);
	}// ..end of the method

	/**
	 * Gets the columns as string array[]
	 * 
	 * @param table
	 * @return
	 */
	protected static List<String> getColumsbyTable(Table table) {
		return table.getColumnNames();
	}// ..end of the method

}
