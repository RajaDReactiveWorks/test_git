package com.getusroi.integrationfwk.activities.bean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.getusroi.eventframework.abstractbean.AbstractMetaModelBean;
import com.getusroi.integrationfwk.config.jaxb.FieldMapper;
import com.getusroi.integrationfwk.config.jaxb.JDBCIntActivity;
import com.getusroi.integrationfwk.config.jaxb.PipeActivity;
import com.getusroi.integrationfwk.jdbcIntactivity.config.JdbcIntActivityConfigurationLoaderException;
import com.getusroi.integrationfwk.jdbcIntactivity.config.JdbcIntActivityQueryProcessingException;
import com.getusroi.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigHelper;
import com.getusroi.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigurationException;
import com.getusroi.integrationfwk.jdbcIntactivity.config.persistence.JdbcIntActivityConnectionException;
import com.getusroi.integrationfwk.jdbcIntactivity.config.persistence.JdbcIntActivityPersistenceException;
import com.getusroi.integrationfwk.jdbcIntactivity.config.persistence.impl.JdbcIntActivityMetaModelUtil;

public class JdbcIntActivity extends AbstractMetaModelBean {

	private final Logger log = LoggerFactory.getLogger(JdbcIntActivity.class.getName());
	private static final String DATA_SOURCE_KEY = "dataSourceA";

	/**
	 * processor of the Sql query from the Configuration JAXBObject
	 * 
	 * @throws JdbcIntActivityQueryProcessingException
	 */
	@Override
	protected void processBean(Exchange exchange) throws JdbcIntActivityQueryProcessingException {
		log.debug(".processBean()..of QueryConfigProcessor..");
		JdbcIntActivityConfigHelper configHelper = new JdbcIntActivityConfigHelper();
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		log.debug(".inProcessBean jdbcIntActivity.." + pipeactivity);
		JDBCIntActivity configObject = pipeactivity.getJDBCIntActivity();
		Set<String> xpathExpression;
		String newQuery;
		String operation;
		Connection connection;
		Document xmlDocument;
		try {
			/*
			 * @note: this snippet is before execution of Query, where
			 * substitution of field happens
			 */
			xpathExpression = getXpathExpressionFromJdbcActivityUnit(configObject);
			ArrayList<String> fieldKey = getListofFieldMapperKeys(configObject);
			String sqlQueryConfig = getSqlStringFromJdbcActivityConfigUnit(configObject);
			operation = getSqlOperationFromJdbcActivityConfigUnit(configObject);
			// String inputXml = XML_RAW_VALUE;
			String inputXml = retreiveXmlInputFromExchangeBody(exchange);
			log.debug(" before un escaped Xml : "+ inputXml);

			inputXml=	StringEscapeUtils.unescapeXml(inputXml);
			log.debug(" escaped Xml : "+ inputXml);
			 xmlDocument = configHelper.generateDocumentFromString(inputXml);
			List<Object> setOfValuesProcessed = configHelper.xpathProcessingOnInputXml(xpathExpression, xmlDocument);

			newQuery = configHelper.processSqlFieldSubstitution(sqlQueryConfig, fieldKey, setOfValuesProcessed,operation);
			log.debug("The new query: " + newQuery);
		} catch (JdbcIntActivityConfigurationException e) {
			throw new JdbcIntActivityQueryProcessingException(
					"Unable to process , as pre-processing failed! in preparation stage", e);
		}
		try {
			/*
			 * this snippet is to execute, based on the operation specified in
			 * configuration
			 * 
			 */
			setDataSource(JdbcIntActivityMetaModelUtil.getDataSource(exchange.getContext(), DATA_SOURCE_KEY));
			JdbcDataContext datacontext = getLocalDataContext(exchange);
			configHelper = new JdbcIntActivityConfigHelper(datacontext, operation, newQuery);
			Object response = configHelper.chooseExecutor();
			if ("SELECT".equals(operation)) {
				ArrayList<String> colums = configHelper.getColumnNamesFromSelectQuery(newQuery);
				Row rowresp = (Row) response;
				List<Object> listOfValues = Arrays.asList(rowresp.getValues());
				if (listOfValues.size() == colums.size()) {
					String xmlEnriched = configHelper.processxmlEnrichment(xmlDocument, configObject, colums, listOfValues);
					exchange.getIn().setBody(xmlEnriched);
				}
			} else {
				exchange.getIn().setBody(response);
			}
		} catch (Exception e) {
			throw new JdbcIntActivityQueryProcessingException(
					"Not a valid jdbc operation to get it executed..The operation specified is : " + operation, e);
		}
	}// ..end of the method

	/**
	 * gets the exchange body and traverse the JsonObject to get the
	 * XmlToProcess
	 * 
	 * @param exchange,
	 *            is used to get the xml from exchange Body
	 * @return returns the xmlString
	 * @throws JdbcIntActivityQueryProcessingException
	 */
	private String retreiveXmlInputFromExchangeBody(Exchange exchange) throws JdbcIntActivityQueryProcessingException {
		log.debug(".retreiveXmlInputFromExchangeBody method of JdbcIntActivity");
		String xmlInput = exchange.getIn().getBody(String.class);
		log.debug("xml input in exchange body : " + xmlInput);
		if (xmlInput != null && !(xmlInput.isEmpty())) {
			return xmlInput;
		} else {
			throw new JdbcIntActivityQueryProcessingException("exchange body is null,There is no data to process");
		}

	}// ..end of the method

	/**
	 * it gets the connection object useful for the dml operations
	 * 
	 * @param exchange,
	 *            is send to get the Context
	 * @param lookupDSName,
	 *            based on this name, it will lookup in the Context
	 * @return datasource Connection
	 * @throws JdbcIntActivityConnectionException
	 * @throws SQLException
	 */
	private Connection getDSConnection(Exchange exchange, String lookupDSName)
			throws JdbcIntActivityConnectionException {
		log.debug(".getDSConnection()..getting connection...");
		Connection connection = null;
		try {
			connection = getConnection(JdbcIntActivityMetaModelUtil.getDataSource(exchange.getContext(), lookupDSName),
					exchange);
		} catch (SQLException | JdbcIntActivityPersistenceException e) {
			throw new JdbcIntActivityConnectionException("Unable to establish DataSource connection..", e);
		}
		log.debug(".getDSConnection()..Connection object, returned: " + connection);
		return connection;
	}// ..end of the method

	/**
	 * gets the xpath expressions from the JDBCIntActivity object
	 * 
	 * @param configObject,
	 *            is loaded with the XpathExpressions
	 * @return the setOfXpathExpressions from the ConfigurationObject
	 * @throws JdbcIntActivityConfigurationException
	 */
	private Set<String> getXpathExpressionFromJdbcActivityUnit(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		log.debug(".getXpathExpressionSet()..getting xpath xpressions configured as list...");
		Set<String> set = new LinkedHashSet();
		ArrayList<FieldMapper> arr = (ArrayList<FieldMapper>) configObject.getDbmsMapper().getFieldMapper();
		for (int i = 0; i < arr.size(); i++) {
			String fieldStr = arr.get(i).getXPath();
			set.add(fieldStr);
		}
		if (!set.isEmpty() || set.size() != arr.size()) {
			return set;
		} else {
			throw new JdbcIntActivityConfigurationException(
					"Activity configuration is not formed well, as it encountered empty xpath Expression");
		}
	}// .. end of the method

	/**
	 * gets the list of field mappers available in the configuration
	 * 
	 * @param configObject,
	 *            is loaded with field Mappers
	 * @return list of field mapper Keys
	 * @throws JdbcIntActivityConfigurationException
	 */
	private ArrayList<String> getListofFieldMapperKeys(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		log.debug(".getListofFieldMapperKeys()..getting fieldMapper keys from the configurations..");
		ArrayList<String> list = new ArrayList<>();
		ArrayList<FieldMapper> arr = (ArrayList<FieldMapper>) configObject.getDbmsMapper().getFieldMapper();
		if (!arr.isEmpty()) {
			for (int i = 0; i < arr.size(); i++) {
				String fieldStr = arr.get(i).getField();
				list.add(fieldStr);
			}
			return list;
		} else {
			throw new JdbcIntActivityConfigurationException("Unable to get non-empty list of fieldMapper..");
		}
	}// ..end of the method

	/**
	 * gets the sql query configured, as String
	 * 
	 * @param configObject,
	 *            the configuration object is loaded with the SQL
	 * @return sql queryString to be processed
	 * @throws JdbcIntActivityConfigurationException
	 */
	private String getSqlStringFromJdbcActivityConfigUnit(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		String sqlQuery = configObject.getSQL();
		if (!sqlQuery.isEmpty()) {
			return sqlQuery;
		} else {
			throw new JdbcIntActivityConfigurationException("Unable to get the Sql query configured..");
		}
	}// ..end of the method

	/**
	 * gets the SQL operation specified will be processed
	 * 
	 * @param configObject,
	 *            loaded with the type of Operation
	 * @return Operation specified in String
	 * @throws JdbcIntActivityConfigurationException
	 */
	private String getSqlOperationFromJdbcActivityConfigUnit(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		String sqlOperation = configObject.getDBConfig().getOperation();
		if (!sqlOperation.isEmpty()) {
			log.info("FrameWork is instructed to process the operation : " + sqlOperation
					+ " : Hence preparing the rest to progress..");
			return sqlOperation;
		} else {
			throw new JdbcIntActivityConfigurationException("Unable to get the Sql operation configured..");
		}
	}// ..end of the method
	
	/**
	 * takes the configName as parameter, which searches the exchange header to
	 * get the ConfigurationObject
	 * 
	 * @param exchange,
	 *            in order to process the exchange header
	 * @param configName,
	 *            acts as key to get the Object
	 * @return JDBCIntActivity object
	 * @throws JdbcIntActivityConfigurationLoaderException
	 */
	private JDBCIntActivity getJdbcIntActivityUnitByNameFromHeader(Exchange exchange, String configHeaderName)
			throws JdbcIntActivityConfigurationLoaderException {
		log.debug("Checking the headers and values available: " + exchange.getIn().getHeaders());
		JDBCIntActivity configObject = (JDBCIntActivity) exchange.getIn().getHeader(configHeaderName);
		log.debug("the config object loaded from header" + configObject);
		if (configObject != null) {
			return configObject;
		} else {
			throw new JdbcIntActivityConfigurationLoaderException(
					"Unable to load non-empty the configuration - " + configHeaderName + " - from contextHeader");
		}
	}// ..end of the method
}
