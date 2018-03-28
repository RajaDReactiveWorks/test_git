package com.attunedlabs.leap.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.builder.SatisfiedQueryBuilder;
import org.apache.metamodel.query.builder.SatisfiedWhereBuilder;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.base.ComputeTimeBean;
import com.attunedlabs.leap.entity.leapdata.LeapDataConfiguration;
import com.attunedlabs.leap.entity.leapdata.WhereClauseFilter;
import com.attunedlabs.leap.util.LeapConfigurationUtil;

public class LeapEntityArchivalDAO extends AbstractMetaModelBean {

	private static final Logger logger = LoggerFactory.getLogger(LeapEntityArchivalDAO.class);

	LeapConfigurationUtil leapConfigUtil = new LeapConfigurationUtil();
	LeapDataConfiguration leapdataConfig = new LeapDataConfiguration();
	ComputeTimeBean time = new ComputeTimeBean();

	/**
	 * 
	 * @param jobj
	 * @param exchange
	 * @throws LeapEntityArchivalDAOException
	 */
	protected void getOrderDetails(String key, String value, Exchange exchange) throws LeapEntityArchivalDAOException {
		logger.debug("inside .getOrderDetails() of LeapEntityArchival : " + key);
		CamelContext context = exchange.getContext();
		DataSource datasource = (DataSource) context.getRegistry().lookupByName("dataSourceA");
		setDataSource(datasource);
		try {
			JdbcDataContext localDataContext = getLocalDataContext(exchange);
			final Table table = localDataContext.getTableByQualifiedLabel("ord");
			logger.debug("Table defined successfully in datacontext:: " + table.getName());
			DataSet dataSet = localDataContext.query().from(table).selectAll().where(key).eq(value).execute();
			logger.debug("dataSet : " + dataSet);
			JSONObject mapObject = new JSONObject();
			JSONArray mapArray = new JSONArray();
			while (dataSet.next()) {
				mapObject.put("client_id", dataSet.getRow().getValue(table.getColumnByName("client_id")));
				mapObject.put("ordnum", dataSet.getRow().getValue(table.getColumnByName("ordnum")));
				mapObject.put("btcust", dataSet.getRow().getValue(table.getColumnByName("btcust")));
				mapObject.put("stcust", dataSet.getRow().getValue(table.getColumnByName("stcust")));
				mapObject.put("rtcust", dataSet.getRow().getValue(table.getColumnByName("rtcust")));
				mapObject.put("ordtyp", dataSet.getRow().getValue(table.getColumnByName("ordtyp")));
				mapObject.put("entdte", dataSet.getRow().getValue(table.getColumnByName("entdte")));
				mapObject.put("cpotyp", dataSet.getRow().getValue(table.getColumnByName("cpotyp")));
				mapObject.put("cponum", dataSet.getRow().getValue(table.getColumnByName("cponum")));
				mapObject.put("wave_flg", dataSet.getRow().getValue(table.getColumnByName("wave_flg")));
				mapObject.put("deptno", dataSet.getRow().getValue(table.getColumnByName("deptno")));
				mapArray.put(mapObject);
			}
			exchange.getIn().setBody(mapArray);
		} catch (Exception e) {
		}
	}

	public void getArchivedOrder(String tagkey, Exchange exchange) throws LeapEntityArchivalDAOException {

		final DataContext cdc = leapConfigUtil.getDataContext(exchange, "SELECT");
		logger.debug("dbType : " + cdc.getDefaultSchema());
		Table table = cdc.getTableByQualifiedLabel("observationtest_list");
		Column column = table.getColumnByName("eventbody");

		tagkey = "eventkeys contains '" + tagkey + "'";
		logger.debug("tagkey : " + tagkey);
		FilterItem keysFilterItem = new FilterItem(tagkey);
		logger.debug("keysFilterItem : " + keysFilterItem);

		Query query = cdc.query().from(table).selectAll().where(keysFilterItem).toQuery();
		logger.debug("query : " + query);

		DataSet ds = cdc.executeQuery(query);
		List<Object> list = new ArrayList<Object>();
		while (ds.next()) {
			org.apache.metamodel.data.Row dataSetRow = ds.getRow();
			System.out.println(dataSetRow);
			System.out.println(dataSetRow.getSelectItems());
			System.out.println(dataSetRow.getValue(column));
			try {
				list.add(XML.toJSONObject(dataSetRow.getValue(column).toString()));
			} catch (JSONException e) {
			}
		}
		logger.debug("list : " + list);
		exchange.getIn().setBody(list);
	}

	public static String changeType(String eachPredicate) throws Exception {

		String[] eachPredicateArr;
		if (eachPredicate.contains(">=")) {
			// eachPredicate = eachPredicate.replace("%3E=", ">=");
			eachPredicateArr = eachPredicate.split(">=");

			try {
				Integer.parseInt(eachPredicateArr[1]);
				return eachPredicateArr[0] + ">=" + eachPredicateArr[1];
			} catch (Exception e) {
				return eachPredicateArr[0] + ">=" + "'" + eachPredicateArr[1] + "'";
			}

		} else if (eachPredicate.contains(">")) {
			// eachPredicate = eachPredicate.replace("%3E", ">");
			eachPredicateArr = eachPredicate.split(">");

			try {
				Integer.parseInt(eachPredicateArr[1]);
				return eachPredicateArr[0] + ">" + eachPredicateArr[1];
			} catch (Exception e) {
				return eachPredicateArr[0] + ">" + "'" + eachPredicateArr[1] + "'";
			}

		} else if (eachPredicate.contains("<=")) {
			// eachPredicate = eachPredicate.replace("%3C=", "<=");
			eachPredicateArr = eachPredicate.split("<=");

			try {
				Integer.parseInt(eachPredicateArr[1]);
				return eachPredicateArr[0] + "<=" + eachPredicateArr[1];
			} catch (Exception e) {
				return eachPredicateArr[0] + "<=" + "'" + eachPredicateArr[1] + "'";
			}
		} else if (eachPredicate.contains("<")) {
			// eachPredicate = eachPredicate.replace("%3C", "<");
			eachPredicateArr = eachPredicate.split("<");

			try {
				Integer.parseInt(eachPredicateArr[1]);
				return eachPredicateArr[0] + "<" + eachPredicateArr[1];
			} catch (Exception e) {
				return eachPredicateArr[0] + "<" + "'" + eachPredicateArr[1] + "'";
			}
		} else if (eachPredicate.contains("NOT LIKE")) {
			// eachPredicate = eachPredicate.replace("%3C", "<");
			eachPredicateArr = eachPredicate.split("NOT LIKE");

			try {
				Integer.parseInt(eachPredicateArr[1]);
				return eachPredicateArr[0] + " NOT LIKE " + eachPredicateArr[1];
			} catch (Exception e) {
				return eachPredicateArr[0] + " NOT LIKE " + "'" + eachPredicateArr[1] + "'";
			}
		} else if (eachPredicate.contains("LIKE")) {
			// eachPredicate = eachPredicate.replace("%3C", "<");
			eachPredicateArr = eachPredicate.split("LIKE");

			try {
				Integer.parseInt(eachPredicateArr[1]);
				return eachPredicateArr[0] + " LIKE " + eachPredicateArr[1];
			} catch (Exception e) {
				return eachPredicateArr[0] + " LIKE " + "'" + eachPredicateArr[1] + "'";
			}
		} else if (eachPredicate.contains("BETWEEN")) {
			// eachPredicate = eachPredicate.replace("%3C", "<");
			eachPredicateArr = eachPredicate.split("BETWEEN");

			String betweenQueryFilter = eachPredicateArr[1];
			String[] betweenOptions = betweenQueryFilter.split("AND");

			return eachPredicateArr[0] + " Between " + "'" + betweenOptions[0] + "'" + "" + " AND " + "'"
					+ betweenOptions[1] + "'";

			/*
			 * try { Integer.parseInt(eachPredicateArr[1]); return eachPredicateArr[0] +
			 * " Between " + eachPredicateArr[1]; } catch( Exception e ) { return
			 * eachPredicateArr[0] + " Between " + "'" + eachPredicateArr[1] + "'"; }
			 */
		} else if (eachPredicate.contains(":")) {
			eachPredicateArr = eachPredicate.split(":");
			try {
				Integer.parseInt(eachPredicateArr[1]);
				return eachPredicateArr[0] + "=" + eachPredicateArr[1];
			} catch (Exception e) {
				return eachPredicateArr[0] + "=" + "'" + eachPredicateArr[1] + "'";
			}
		} else {
			eachPredicateArr = eachPredicate.split("=");
			try {
				Integer.parseInt(eachPredicateArr[1]);
				return eachPredicateArr[0] + "=" + eachPredicateArr[1];
			} catch (Exception e) {
				return eachPredicateArr[0] + "=" + "'" + eachPredicateArr[1] + "'";
			}
		}
	}

	public void getArchivedOrder(Exchange exchange) throws Exception {
		Map<String, Object> leapQueryData = getLeapQueryData(exchange);
		String columnName = "eventkeys";
		String operation = "SELECT";
		boolean noSQL = LeapEntityArchivalUtility.isSQL(exchange);
		String tableName;
		if (noSQL)
			tableName = "observationtest_list";
		else
			tableName = "ord";
		final DataContext dataContext = LeapEntityArchivalUtility.getDataContext(exchange, operation, columnName);
		Table table = dataContext.getTableByQualifiedLabel(tableName);
		Query query = generateQueryFromQueryData(leapQueryData, table, dataContext, exchange);
		logger.debug("query : " + query);
		DataSet dataSet = dataContext.executeQuery(query);
		JSONObject leapdata = leapdataConfig.leapDataConfiguration(dataSet, exchange);
		exchange.getIn().setBody(leapdata);
	}

	private Query generateQueryFromQueryData(Map<String, Object> leapQueryData, Table table, DataContext dataContext,
			Exchange exchange) {
		SatisfiedQueryBuilder<?> queryBuilder = generateQuery(leapQueryData, table, dataContext);
		queryBuilder = addFilterToQuery(leapQueryData, queryBuilder, dataContext, table, exchange);
		queryBuilder = getOrderByDetails(leapQueryData, queryBuilder, table);
		queryBuilder = getTopSearch(leapQueryData, queryBuilder, table);
		return queryBuilder.toQuery();
	}

	private SatisfiedQueryBuilder<?> getTopSearch(Map<String, Object> leapQueryData,
			SatisfiedQueryBuilder<?> queryBuilder, Table table) {
		Object topSearchObj = leapQueryData.get(LeapEntityArchivalConstant.TOP);
		if (topSearchObj != null) {
			if (topSearchObj instanceof Integer) {
				return queryBuilder.maxRows((int) topSearchObj);
			}
		}
		return queryBuilder;
	}

	private SatisfiedQueryBuilder<?> getOrderByDetails(Map<String, Object> leapQueryData,
			SatisfiedQueryBuilder<?> queryBuilder, Table table) {
		Object orderByObj = leapQueryData.get(LeapEntityArchivalConstant.ORDER_BY);
		if (orderByObj != null) {
			if (orderByObj instanceof String) {
				String orderBy = ((String) orderByObj).trim();
				String[] split = orderBy.split(LeapEntityArchivalConstant.SPACE);
				String order = split[split.length - 1];
				if (order.toUpperCase().equals(LeapEntityArchivalConstant.ORDER_BY_DESC))
					return queryBuilder
							.orderBy(table.getColumnByName(orderBy.substring(0, orderBy.length() - 5).trim())).desc();
				else if (order.toUpperCase().equals(LeapEntityArchivalConstant.ORDER_BY_ASC))
					return queryBuilder
							.orderBy(table.getColumnByName(orderBy.substring(0, orderBy.length() - 4).trim())).asc();
			}
		}
		return queryBuilder;
	}

	private SatisfiedQueryBuilder<?> addFilterToQuery(Map<String, Object> leapQueryData,
			SatisfiedQueryBuilder<?> queryBuilder, DataContext dataContext, Table table, Exchange exchange) {
		Object filterObj = leapQueryData.get(LeapEntityArchivalConstant.FILTER);
		boolean isSQL = !LeapEntityArchivalUtility.isSQL(exchange);
		if (filterObj != null) {
			if (filterObj instanceof String) {
				String filterStr = (String) filterObj;
				if (!filterStr.isEmpty())
					return addWhereClauseConditions(filterStr, queryBuilder, table, isSQL);
			}
		}
		return queryBuilder;
	}

	private SatisfiedQueryBuilder<?> addWhereClauseConditions(String filterStr, SatisfiedQueryBuilder<?> queryBuilder,
			Table table, boolean isSQL) {
		List<String> conditionsFromFilter = getConditionsFromFilter(filterStr.toLowerCase());
		List<WhereClauseFilter> clauseFilters = getWhereConditionElements(filterStr);
		if (!clauseFilters.isEmpty()) {
			SatisfiedWhereBuilder<?> satisfiedWhereBuilder = null;
			while (satisfiedWhereBuilder == null) {
				if (!clauseFilters.isEmpty()) {
					WhereClauseFilter whereClauseFilter = clauseFilters.get(0);
					satisfiedWhereBuilder = getSatisfiedWhereBuilder(queryBuilder, whereClauseFilter, table, isSQL);
					clauseFilters.remove(whereClauseFilter);
				} else
					return queryBuilder;
			}
			for (int index = 0; index < clauseFilters.size(); index++) {
				WhereClauseFilter whereClauseFilter = clauseFilters.get(index);
				String condition = conditionsFromFilter.get(index);
				addWhereClause(satisfiedWhereBuilder, whereClauseFilter, condition, table, isSQL);
			}
		}
		return queryBuilder;
	}

	private SatisfiedWhereBuilder<?> getSatisfiedWhereBuilder(SatisfiedQueryBuilder<?> queryBuilder,
			WhereClauseFilter whereClauseFilter, Table table, boolean isSQL) {
		SatisfiedWhereBuilder<?> satisfiedWhereBuilder = null;
		String operatorType = whereClauseFilter.getOperatorType();
		if (operatorType.equals(LeapEntityArchivalConstant.EQ.trim())) {
			satisfiedWhereBuilder = queryBuilder.where(table.getColumnByName(whereClauseFilter.getColumnName()))
					.eq(whereClauseFilter.getValue());
		} else if (operatorType.equals(LeapEntityArchivalConstant.NE.trim()))
			satisfiedWhereBuilder = queryBuilder.where(table.getColumnByName(whereClauseFilter.getColumnName()))
					.ne(whereClauseFilter.getValue());
		else if (operatorType.equals(LeapEntityArchivalConstant.GT.trim()))
			satisfiedWhereBuilder = queryBuilder.where(table.getColumnByName(whereClauseFilter.getColumnName()))
					.gt(whereClauseFilter.getValue());
		else if (operatorType.equals(LeapEntityArchivalConstant.GE.trim()))
			satisfiedWhereBuilder = queryBuilder.where(table.getColumnByName(whereClauseFilter.getColumnName()))
					.gte(whereClauseFilter.getValue());
		else if (operatorType.equals(LeapEntityArchivalConstant.LT.trim()))
			satisfiedWhereBuilder = queryBuilder.where(table.getColumnByName(whereClauseFilter.getColumnName()))
					.lt(whereClauseFilter.getValue());
		else if (operatorType.equals(LeapEntityArchivalConstant.LE.trim()))
			satisfiedWhereBuilder = queryBuilder.where(table.getColumnByName(whereClauseFilter.getColumnName()))
					.lte(whereClauseFilter.getValue());
		else if (operatorType.equals(LeapEntityArchivalConstant.CONTAINS)) {
			if (isSQL)
				satisfiedWhereBuilder = queryBuilder.where(table.getColumnByName(whereClauseFilter.getColumnName()))
						.like(LeapEntityArchivalConstant.PERCENTAGE + getWhereFilterValue(whereClauseFilter.getValue())
								+ LeapEntityArchivalConstant.PERCENTAGE);
			else
				queryBuilder.where(new FilterItem("eventkeys contains '" + whereClauseFilter.getColumnName() + ":"
						+ getWhereFilterValue(whereClauseFilter.getValue()) + "'"));
		} else if (operatorType.equals(LeapEntityArchivalConstant.STARTS_WITH))
			satisfiedWhereBuilder = queryBuilder.where(whereClauseFilter.getColumnName())
					.like(setValue(whereClauseFilter.getValue().toString()) + LeapEntityArchivalConstant.PERCENTAGE);
		else if (operatorType.equals(LeapEntityArchivalConstant.ENDS_WITH))
			satisfiedWhereBuilder = queryBuilder.where(whereClauseFilter.getColumnName())
					.like(LeapEntityArchivalConstant.PERCENTAGE + setValue(whereClauseFilter.getValue().toString()));
		return satisfiedWhereBuilder;
	}

	private String getWhereFilterValue(Object value) {
		if (value instanceof String)
			return value.toString().replaceAll("'", "");
		else
			return null;
	}

	private SatisfiedWhereBuilder<?> addWhereClause(SatisfiedWhereBuilder<?> satisfiedWhereBuilder,
			WhereClauseFilter whereClauseFilter, String condition, Table table, boolean isSQL) {
		if (condition.equals(LeapEntityArchivalConstant.AND.trim())) {
			if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.EQ.trim())) {
				satisfiedWhereBuilder = satisfiedWhereBuilder
						.and(table.getColumnByName(whereClauseFilter.getColumnName())).eq(whereClauseFilter.getValue());
			} else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.NE.trim()))
				satisfiedWhereBuilder = satisfiedWhereBuilder
						.and(table.getColumnByName(whereClauseFilter.getColumnName())).ne(whereClauseFilter.getValue());
			else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.GT.trim()))
				satisfiedWhereBuilder = satisfiedWhereBuilder
						.and(table.getColumnByName(whereClauseFilter.getColumnName())).gt(whereClauseFilter.getValue());
			else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.GE.trim()))
				satisfiedWhereBuilder = satisfiedWhereBuilder
						.and(table.getColumnByName(whereClauseFilter.getColumnName()))
						.gte(whereClauseFilter.getValue());
			else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.LT.trim()))
				satisfiedWhereBuilder = satisfiedWhereBuilder
						.and(table.getColumnByName(whereClauseFilter.getColumnName())).lt(whereClauseFilter.getValue());
			else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.LE.trim()))
				satisfiedWhereBuilder = satisfiedWhereBuilder
						.and(table.getColumnByName(whereClauseFilter.getColumnName()))
						.lte(whereClauseFilter.getValue());
			else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.CONTAINS)) {
				if (isSQL)
					satisfiedWhereBuilder = satisfiedWhereBuilder
							.and(table.getColumnByName(whereClauseFilter.getColumnName()))
							.like(LeapEntityArchivalConstant.PERCENTAGE
									+ getWhereFilterValue(whereClauseFilter.getValue())
									+ LeapEntityArchivalConstant.PERCENTAGE);
				else
					satisfiedWhereBuilder
							.where(new FilterItem("eventkeys contains '" + whereClauseFilter.getColumnName() + ":"
									+ getWhereFilterValue(whereClauseFilter.getValue()) + "'"));
			} else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.STARTS_WITH))
				satisfiedWhereBuilder = satisfiedWhereBuilder.and(whereClauseFilter.getColumnName()).like(
						setValue(whereClauseFilter.getValue().toString()) + LeapEntityArchivalConstant.PERCENTAGE);
			else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.ENDS_WITH))
				satisfiedWhereBuilder = satisfiedWhereBuilder.and(whereClauseFilter.getColumnName()).like(
						LeapEntityArchivalConstant.PERCENTAGE + setValue(whereClauseFilter.getValue().toString()));
		} else if (condition.equals(LeapEntityArchivalConstant.OR.trim())) {
			if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.EQ.trim())) {
				satisfiedWhereBuilder = satisfiedWhereBuilder
						.or(table.getColumnByName(whereClauseFilter.getColumnName())).eq(whereClauseFilter.getValue());
			} else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.NE.trim()))
				satisfiedWhereBuilder = satisfiedWhereBuilder
						.or(table.getColumnByName(whereClauseFilter.getColumnName())).ne(whereClauseFilter.getValue());
			else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.GT.trim()))
				satisfiedWhereBuilder = satisfiedWhereBuilder
						.or(table.getColumnByName(whereClauseFilter.getColumnName())).gt(whereClauseFilter.getValue());
			else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.GE.trim()))
				satisfiedWhereBuilder = satisfiedWhereBuilder
						.or(table.getColumnByName(whereClauseFilter.getColumnName())).gte(whereClauseFilter.getValue());
			else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.LT.trim()))
				satisfiedWhereBuilder = satisfiedWhereBuilder
						.or(table.getColumnByName(whereClauseFilter.getColumnName())).lt(whereClauseFilter.getValue());
			else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.LE.trim()))
				satisfiedWhereBuilder = satisfiedWhereBuilder
						.or(table.getColumnByName(whereClauseFilter.getColumnName())).lte(whereClauseFilter.getValue());
			else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.CONTAINS)) {
				if (isSQL)
					satisfiedWhereBuilder = satisfiedWhereBuilder
							.or(table.getColumnByName(whereClauseFilter.getColumnName()))
							.like(LeapEntityArchivalConstant.PERCENTAGE
									+ getWhereFilterValue(whereClauseFilter.getValue())
									+ LeapEntityArchivalConstant.PERCENTAGE);
				else
					satisfiedWhereBuilder
							.where(new FilterItem("eventkeys contains '" + whereClauseFilter.getColumnName() + ":"
									+ getWhereFilterValue(whereClauseFilter.getValue()) + "'"));
			}else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.STARTS_WITH))
				satisfiedWhereBuilder = satisfiedWhereBuilder.or(whereClauseFilter.getColumnName()).like(
						setValue(whereClauseFilter.getValue().toString()) + LeapEntityArchivalConstant.PERCENTAGE);
			else if (whereClauseFilter.getOperatorType().equals(LeapEntityArchivalConstant.ENDS_WITH))
				satisfiedWhereBuilder = satisfiedWhereBuilder.or(whereClauseFilter.getColumnName()).like(
						LeapEntityArchivalConstant.PERCENTAGE + setValue(whereClauseFilter.getValue().toString()));
		}
		return satisfiedWhereBuilder;
	}
	
	private List<WhereClauseFilter> getWhereConditionElements(String filterStr) {
		List<WhereClauseFilter> clauseFilters = new ArrayList<>();
		String[] whereElements = filterStr.split(LeapEntityArchivalConstant.AND_OR_SPLITTER);
		for (String whereElement : whereElements) {
			String[] whereClause = null;
			String operatorType = null;
			if (whereElement.toLowerCase().contains(LeapEntityArchivalConstant.EQ)) {
				whereClause = whereElement.split(LeapEntityArchivalConstant.EQ_SPLITTER);
				operatorType = LeapEntityArchivalConstant.EQ.trim();
			} else if (whereElement.toLowerCase().contains(LeapEntityArchivalConstant.NE)) {
				whereClause = whereElement.split(LeapEntityArchivalConstant.NE_SPLITTER);
				operatorType = LeapEntityArchivalConstant.NE.trim();
			} else if (whereElement.toLowerCase().contains(LeapEntityArchivalConstant.GT)) {
				whereClause = whereElement.split(LeapEntityArchivalConstant.GT_SPLITTER);
				operatorType = LeapEntityArchivalConstant.GT.trim();
			} else if (whereElement.toLowerCase().contains(LeapEntityArchivalConstant.GE)) {
				whereClause = whereElement.split(LeapEntityArchivalConstant.GE_SPLITTER);
				operatorType = LeapEntityArchivalConstant.GE.trim();
			} else if (whereElement.toLowerCase().contains(LeapEntityArchivalConstant.LT)) {
				whereClause = whereElement.split(LeapEntityArchivalConstant.LT_SPLITTER);
				operatorType = LeapEntityArchivalConstant.LT.trim();
			} else if (whereElement.toLowerCase().contains(LeapEntityArchivalConstant.LE)) {
				whereClause = whereElement.split(LeapEntityArchivalConstant.LE_SPLITTER);
				operatorType = LeapEntityArchivalConstant.LE.trim();
			} else if (whereElement.toLowerCase()
					.contains(LeapEntityArchivalConstant.CONTAINS + LeapEntityArchivalConstant.OPEN_BRACKET)) {
				int indexOf = whereElement.indexOf(LeapEntityArchivalConstant.OPEN_BRACKET) + 1;
				int lastIndexOf = whereElement.lastIndexOf(LeapEntityArchivalConstant.CLOSE_BRACKET);
				whereClause = whereElement.substring(indexOf, lastIndexOf).split(LeapEntityArchivalConstant.COMMA);
				operatorType = LeapEntityArchivalConstant.CONTAINS;
			} else if (whereElement.toLowerCase()
					.contains(LeapEntityArchivalConstant.STARTS_WITH + LeapEntityArchivalConstant.OPEN_BRACKET)) {
				int indexOf = whereElement.indexOf(LeapEntityArchivalConstant.OPEN_BRACKET) + 1;
				int lastIndexOf = whereElement.lastIndexOf(LeapEntityArchivalConstant.CLOSE_BRACKET);
				whereClause = whereElement.substring(indexOf, lastIndexOf).split(LeapEntityArchivalConstant.COMMA);
				operatorType = LeapEntityArchivalConstant.STARTS_WITH;
			} else if (whereElement.toLowerCase()
					.contains(LeapEntityArchivalConstant.ENDS_WITH + LeapEntityArchivalConstant.OPEN_BRACKET)) {
				int indexOf = whereElement.indexOf(LeapEntityArchivalConstant.OPEN_BRACKET) + 1;
				int lastIndexOf = whereElement.lastIndexOf(LeapEntityArchivalConstant.CLOSE_BRACKET);
				whereClause = whereElement.substring(indexOf, lastIndexOf).split(LeapEntityArchivalConstant.COMMA);
				operatorType = LeapEntityArchivalConstant.ENDS_WITH;
			} else {
				whereClause = null;
				operatorType = null;
			}
			if (whereClause != null) {
				clauseFilters.add(checkOperationalAndFuncionalValue(whereClause, operatorType));
			}
		}
		return clauseFilters;
	}

	private static WhereClauseFilter checkOperationalAndFuncionalValue(String[] whereClause, String operatorType) {
		// WhereClauseFilter whereClauseFilter = new
		// WhereClauseFilter(getColumnName(whereClause[0]), operatorType,
		// getValue(whereClause[1]));
		// #TODO columnName bracket removal
		WhereClauseFilter whereClauseFilter = new WhereClauseFilter(whereClause[0], operatorType, whereClause[1]);
		if (NumberUtils.isNumber(whereClauseFilter.getValue().toString())) {
			whereClauseFilter = checkArithematicOperationFilter(whereClause, whereClauseFilter);
			return whereClauseFilter;
		} else {
			whereClauseFilter = checkStringFunctionFilter(whereClauseFilter);
			return whereClauseFilter;
		}
	}

	private static WhereClauseFilter checkStringFunctionFilter(WhereClauseFilter whereClauseFilter) {
		String[] stringFunctionArray = null;
		String columnString = whereClauseFilter.getColumnName().trim();
		if (columnString.toLowerCase()
				.contains(LeapEntityArchivalConstant.TO_LOWER + LeapEntityArchivalConstant.OPEN_BRACKET)) {
			int indexOf = columnString.indexOf(LeapEntityArchivalConstant.OPEN_BRACKET) + 1;
			int lastIndexOf = columnString.lastIndexOf(LeapEntityArchivalConstant.CLOSE_BRACKET);
			columnString = columnString.substring(indexOf, lastIndexOf).trim();
			whereClauseFilter.setColumnName(columnString);
			whereClauseFilter.setValue(setValue(whereClauseFilter.getValue().toString()).toLowerCase());
		} else if (columnString.toLowerCase()
				.contains(LeapEntityArchivalConstant.TO_UPPER + LeapEntityArchivalConstant.OPEN_BRACKET)) {
			int indexOf = columnString.indexOf(LeapEntityArchivalConstant.OPEN_BRACKET) + 1;
			int lastIndexOf = columnString.lastIndexOf(LeapEntityArchivalConstant.CLOSE_BRACKET);
			columnString = columnString.substring(indexOf, lastIndexOf).trim();
			whereClauseFilter.setColumnName(columnString);
			whereClauseFilter.setValue(setValue(whereClauseFilter.getValue().toString()).toUpperCase());
		} else if (columnString.toLowerCase()
				.contains(LeapEntityArchivalConstant.TRIM + LeapEntityArchivalConstant.OPEN_BRACKET)) {
			int indexOf = columnString.indexOf(LeapEntityArchivalConstant.OPEN_BRACKET) + 1;
			int lastIndexOf = columnString.lastIndexOf(LeapEntityArchivalConstant.CLOSE_BRACKET);
			stringFunctionArray = columnString.substring(indexOf, lastIndexOf).split(LeapEntityArchivalConstant.COMMA);
			whereClauseFilter.setColumnName(stringFunctionArray[0]);
			whereClauseFilter.setValue(setValue(whereClauseFilter.getValue().toString()).trim());
		}
		else if (columnString.toLowerCase()
				.contains(LeapEntityArchivalConstant.LENGTH + LeapEntityArchivalConstant.OPEN_BRACKET)) {
			int indexOf = columnString.indexOf(LeapEntityArchivalConstant.OPEN_BRACKET) + 1;
			int lastIndexOf = columnString.lastIndexOf(LeapEntityArchivalConstant.CLOSE_BRACKET);
			columnString = columnString.substring(indexOf, lastIndexOf).trim();
			whereClauseFilter.setColumnName(columnString);
			whereClauseFilter.setValue(getColumnLengthString(NumberUtils.toInt(whereClauseFilter.getValue().toString())));
			whereClauseFilter.setOperatorType(LeapEntityArchivalConstant.LENGTH);
		}

		return whereClauseFilter;
	}

	private static WhereClauseFilter checkArithematicOperationFilter(String[] whereClause,
			WhereClauseFilter whereClauseFilter) {
		String[] arithmeticOperatorArray = null;
		String operation = null;
		Number value = null;
		Number operationValue = null;
		String columnString = whereClause[0].trim();
		if (columnString.toLowerCase().contains(LeapEntityArchivalConstant.ADD)) {
			arithmeticOperatorArray = columnString.split(LeapEntityArchivalConstant.ADD_SPLITTER);
			if (NumberUtils.isNumber(arithmeticOperatorArray[1]))
				operation = LeapEntityArchivalConstant.ADD;
		} else if (columnString.toLowerCase().contains(LeapEntityArchivalConstant.SUB)) {
			arithmeticOperatorArray = columnString.split(LeapEntityArchivalConstant.SUB_SPLITTER);
			if (NumberUtils.isNumber(arithmeticOperatorArray[1]))
				operation = LeapEntityArchivalConstant.SUB;
		} else if (columnString.toLowerCase().contains(LeapEntityArchivalConstant.MUL)) {
			arithmeticOperatorArray = columnString.split(LeapEntityArchivalConstant.MUL_SPLITTER);
			if (NumberUtils.isNumber(arithmeticOperatorArray[1]))
				operation = LeapEntityArchivalConstant.MUL;
		} else if (columnString.toLowerCase().contains(LeapEntityArchivalConstant.DIV)) {
			arithmeticOperatorArray = columnString.split(LeapEntityArchivalConstant.DIV_SPLITTER);
			if (NumberUtils.isNumber(arithmeticOperatorArray[1]))
				operation = LeapEntityArchivalConstant.DIV;
		} else if (columnString.toLowerCase().contains(LeapEntityArchivalConstant.MOD)) {
			arithmeticOperatorArray = columnString.split(LeapEntityArchivalConstant.MOD_SPLITTER);
			if (NumberUtils.isNumber(arithmeticOperatorArray[1]))
				operation = LeapEntityArchivalConstant.MOD;
		}
		if (operation != null) {
			value = NumberUtils.createNumber(whereClause[1]);
			// #TODO columnName bracket removal
			// operationValue =
			// NumberUtils.createNumber(getValue(arithmeticOperatorArray[1]));
			// whereClauseFilter.setColumnName(getColumnName(arithmeticOperatorArray[0]));
			operationValue = NumberUtils.createNumber(arithmeticOperatorArray[1]);
			whereClauseFilter.setColumnName(arithmeticOperatorArray[0]);
			whereClauseFilter.setValue(setValue(value, operationValue, operation));
		}
		return whereClauseFilter;
	}

	// #TODO columnName bracket removal
	@SuppressWarnings("unused")
	private static String getValue(String value) {
		if (value.trim().endsWith(LeapEntityArchivalConstant.CLOSE_BRACKET))
			value = value.trim().substring(0, value.trim().length() - 1);
		return value.trim();
	}

	// #TODO columnName bracket removal

	@SuppressWarnings("unused")
	private static String getColumnName(String columnName) {
		if (columnName.trim().startsWith(LeapEntityArchivalConstant.OPEN_BRACKET))
			columnName = columnName.trim().substring(1);
		return columnName.trim();
	}

	private static Number setValue(Number value, Number operationValue, String operation) {
		Number finalValue = null;
		if (LeapEntityArchivalConstant.ADD.equals(operation))
			finalValue = new BigDecimal(value.toString()).subtract(new BigDecimal(operationValue.toString()));
		if (LeapEntityArchivalConstant.SUB.equals(operation))
			finalValue = new BigDecimal(value.toString()).add(new BigDecimal(operationValue.toString()));
		if (LeapEntityArchivalConstant.MUL.equals(operation))
			finalValue = new BigDecimal(value.toString()).divide(new BigDecimal(operationValue.toString()));
		if (LeapEntityArchivalConstant.DIV.equals(operation))
			finalValue = new BigDecimal(value.toString()).multiply(new BigDecimal(operationValue.toString()));
		if (LeapEntityArchivalConstant.MOD.equals(operation))
			finalValue = new BigDecimal(value.toString()).remainder(new BigDecimal(operationValue.toString()));
		return finalValue;
	}

	private static List<String> getConditionsFromFilter(String filterStr) {
		Map<Integer, String> conditionsMap = new HashMap<>();

		int index = filterStr.indexOf(LeapEntityArchivalConstant.AND);
		while (index >= 0) {
			conditionsMap.put(index, LeapEntityArchivalConstant.AND.trim());
			index = filterStr.indexOf(LeapEntityArchivalConstant.AND, index + 1);
		}
		index = filterStr.indexOf(LeapEntityArchivalConstant.OR);
		while (index >= 0) {
			conditionsMap.put(index, LeapEntityArchivalConstant.OR.trim());
			index = filterStr.indexOf(LeapEntityArchivalConstant.OR, index + 1);
		}
		SortedSet<Integer> keys = new TreeSet<Integer>(conditionsMap.keySet());
		List<String> conditions = new ArrayList<>();
		for (Integer key : keys)
			conditions.add(conditionsMap.get(key));
		return conditions;
	}

	private SatisfiedQueryBuilder<?> generateQuery(Map<String, Object> leapQueryData, Table table,
			DataContext dataContext) {
		List<Column> columns = new ArrayList<>();
		Object columnsObj = leapQueryData.get(LeapEntityArchivalConstant.SELECT);
		if (columnsObj != null) {
			String columnsStr = (String) columnsObj;
			if (!columnsStr.isEmpty()) {
				String[] columnArr = columnsStr.trim().split(LeapEntityArchivalConstant.COMMA);
				for (String column : columnArr) {
					if (!column.isEmpty()) {
						Column columnByName = table.getColumnByName(column.trim());
						columns.add(columnByName);
					}
				}
			}
		}
		return getSelectQuery(dataContext, table, columns);
	}

	private SatisfiedQueryBuilder<?> getSelectQuery(DataContext dataContext, Table table, List<Column> columns) {
		SatisfiedQueryBuilder<?> queryBuilder;
		if (!columns.isEmpty())
			queryBuilder = dataContext.query().from(table).select(columns);
		else
			queryBuilder = dataContext.query().from(table).selectAll();
		return queryBuilder;
	}


	private Map<String, Object> getLeapQueryData(Exchange exchange) throws JSONException {
		String body = exchange.getIn().getBody(String.class);
		JSONObject object = new JSONObject(body);
		String jsonArray = object.getJSONArray(LeapEntityArchivalConstant.LEAP_QUERY).toString();
		JSONArray array = new JSONArray(jsonArray);
		Map<String, Object> queryDataMap = new HashMap<>();
		for (int index = 0; index < array.length(); index++) {
			Object typeObj = array.get(index);
			if (typeObj instanceof JSONObject) {
				JSONObject typeJson = new JSONObject(typeObj.toString());
				queryDataMap.put(typeJson.getString(LeapEntityArchivalConstant.TYPE),
						typeJson.get(LeapEntityArchivalConstant.DATA));
			} else {
				logger.debug("cannot get data from leapQuery for " + typeObj);
			}
		}
		return queryDataMap;
	}

	private static String setValue(String value) {
		value = StringUtils.removeStart(value.trim(), LeapEntityArchivalConstant.APOSTROPHE);
		value = StringUtils.removeEnd(value.trim(), LeapEntityArchivalConstant.APOSTROPHE);
		return value;
	}

	private static String getColumnLengthString(int length) {
		String columnValue = "";
		for (int index = 0; index < length; index++) {
			columnValue = columnValue + "_";
		}
		return columnValue;
	}

	@Override
	protected void processBean(Exchange arg0) throws Exception {
	}

}
