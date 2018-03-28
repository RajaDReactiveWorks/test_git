package com.attunedlabs.leap.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.Exchange;
import org.apache.camel.spi.Registry;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.factory.DataContextFactoryRegistryImpl;
import org.apache.metamodel.factory.DataContextPropertiesImpl;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.eventframework.abstractbean.LeapMetaModelBean;
import com.attunedlabs.leap.base.ComputeTimeBean;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.google.common.reflect.TypeToken;

public class LeapEntityArchivalUtility {

	private final static Logger logger = LoggerFactory.getLogger(LeapEntityArchivalUtility.class);

	public List<String> parseEntityColumns() throws LeapEntityArchivalException {
		logger.debug("inside .parseEntityColumns() of LeapEntityArchivalUtility");
		DocumentBuilderFactory dbc = DocumentBuilderFactory.newInstance();
		DocumentBuilder dbuilder;
		String name = null;
		List<String> list = new ArrayList<>();
		try {
			dbuilder = dbc.newDocumentBuilder();
			Document doc = dbuilder.parse(new InputSource(new StringReader(getEntityConfiguration())));
			NodeList nl = doc.getElementsByTagName("EntityColumn");
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				name = e.getAttribute("name");
				// if (name.equals(key)) {
				// logger.debug("name : " + name);
				list.add(name);
				// }
			}
		} catch (Exception e) {
			throw new LeapEntityArchivalException(e.getMessage(), e.getCause());
		}
		return list;
	}

	/**
	 * 
	 * @param key
	 * @return
	 * @throws LeapEntityArchivalException
	 */
	public List<String> getAttribute(String key) throws LeapEntityArchivalException {
		logger.debug("inside .getAttribute() of LeapEntityArchivalUtility");
		DocumentBuilderFactory dbc = DocumentBuilderFactory.newInstance();
		DocumentBuilder dbuilder;
		String name = null, accessType = null;
		List<String> list = new ArrayList<>();
		try {
			dbuilder = dbc.newDocumentBuilder();
			Document doc = dbuilder.parse(new InputSource(new StringReader(getEntityConfiguration())));
			NodeList n2 = doc.getElementsByTagName("EntityAccess");
			for (int i = 0; i < n2.getLength(); i++) {
				Element e = (Element) n2.item(i);
				accessType = e.getAttribute("accessType");
				list.add(accessType);
			}
			NodeList nl = doc.getElementsByTagName("EntityColumn");
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				name = e.getAttribute("name");
				if (name.equals(key)) {
					logger.debug("name : " + name);
					list.add(name);
				}
			}
		} catch (Exception e) {
			throw new LeapEntityArchivalException(e.getMessage(), e.getCause());
		}
		return list;
	}

	/**
	 * 
	 * @return
	 * @throws LeapEntityArchivalException
	 */
	private String getEntityConfiguration() throws LeapEntityArchivalException {
		InputStream leapDataServices = ComputeTimeBean.class.getClassLoader()
				.getResourceAsStream(LeapEntityArchivalConstant.LEAP_DATA_SERVICES);
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(leapDataServices));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			throw new LeapEntityArchivalException(e.getMessage(), e.getCause());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
		logger.debug("sb.toString() : " + sb.toString());
		return sb.toString();
	}

	/**
	 * To get {@link DataContext} Object.
	 * 
	 * @param exchange
	 * @param columnName
	 *            - Name of the column(only for Cassandra database).
	 * @param operation
	 *            - Name of the operation like SELECT, INSERT, INSERT,
	 *            DELETE...(only for Cassandra database).
	 * @return {@link DataContext} Object.
	 */
	public static DataContext getDataContext(Exchange exchange, String operation, String columnName) {
		if (isSQL(exchange))
			return getCassandraDataContext(columnName, operation);
		else {
			return getJDBCDataContext(exchange);
		}
	}// end of getDataContext() method.

	/**
	 * To get {@link DataContext} Object.
	 * 
	 * @param exchange
	 * @return {@link DataContext} Object.
	 */
	private static DataContext getJDBCDataContext(Exchange exchange) {
		Registry registry = exchange.getContext().getRegistry();
		DataSource dataSource = (DataSource) registry.lookupByName(LeapEntityArchivalConstant.LEAP_XA_DATASOURCE);
		Connection con = null;
		try {
			con = DataSourceUtils.getConnection(dataSource);
		} catch (CannotGetJdbcConnectionException e) {
			logger.warn("Error in getting the Connection from " + dataSource);
		}
		DataContext metamodelJdbcContext = new JdbcDataContext(con);
		return metamodelJdbcContext;
	}// end of getJDBCDataContext() method.

	/**
	 * To get Cassandra {@link DataContext} Object.
	 * 
	 * @param columnName
	 *            - Name of the column(if column has to be added in
	 *            {@link TypeToken}).
	 * @param operation
	 *            - Name of the operation like SELECT, INSERT, INSERT, DELETE....
	 * @return {@link DataContext} Object.
	 */
	private static DataContext getCassandraDataContext(String columnName, String operation) {
		HashMap<String, TypeToken<?>> typeTokenMap = new HashMap<>();
		typeTokenMap.put(columnName, new TypeToken<List<String>>() {
			private static final long serialVersionUID = 1L;
		});
		Properties prop = null;
		try {
			prop = loadingCassandraPropertiesFile();
		} catch (PropertiesConfigException e) {
			logger.error("Cannot load Properties file");
		}
		DataContextPropertiesImpl properties = new DataContextPropertiesImpl();
		if (operation.toUpperCase().equals(LeapMetaModelBean.SELECT_OPERATION))
			properties.setDataContextType(LeapEntityArchivalConstant.CASSANDRA);
		else
			properties.setDataContextType(LeapEntityArchivalConstant.JDBC);
		properties.put(DataContextPropertiesImpl.PROPERTY_HOSTNAME, prop.getProperty(LeapEntityArchivalConstant.HOST));
		properties.put(DataContextPropertiesImpl.PROPERTY_PORT, prop.getProperty(LeapEntityArchivalConstant.PORT));
		properties.put(DataContextPropertiesImpl.PROPERTY_URL, prop.getProperty(LeapEntityArchivalConstant.URL));
		properties.put(DataContextPropertiesImpl.PROPERTY_DRIVER_CLASS,
				prop.getProperty(LeapEntityArchivalConstant.DRIVER_CLASS));
		properties.put(DataContextPropertiesImpl.PROPERTY_DATABASE,
				prop.getProperty(LeapEntityArchivalConstant.KEYSPACE));
		properties.put(LeapEntityArchivalConstant.TYPE_TOKEN, typeTokenMap);
		org.apache.metamodel.DataContext dataContext = DataContextFactoryRegistryImpl.getDefaultInstance()
				.createDataContext(properties);
		return dataContext;
	}// end of getCassandraDataContext() method.

	/**
	 * To load the properties file
	 * 
	 * @return Properties Object
	 * @throws PropertiesConfigException
	 */
	public static Properties loadingCassandraPropertiesFile() throws PropertiesConfigException {
		Properties properties = new Properties();
		InputStream inputStream = LeapConfigurationUtil.class.getClassLoader()
				.getResourceAsStream(LeapEntityArchivalConstant.CONFIG_PROPERTY_FILE);
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			throw new PropertiesConfigException(
					"unable to load property file = " + LeapEntityArchivalConstant.CONFIG_PROPERTY_FILE);
		}

		return properties;

	}// end of loadingCassandraPropertiesFile() method.

	/**
	 * Check whether the given request is for SQL or NO-SQL Database
	 * 
	 * @param exchange
	 * @return true or false
	 */
	public static boolean isSQL(Exchange exchange) {
		Object noSqlObj = exchange.getIn().getHeader(LeapEntityArchivalConstant.NO_SQL_HEADER_KEY);
		boolean noSql = false;
		if (noSqlObj != null)
			noSql = Boolean.valueOf(noSqlObj.toString().trim());
		return noSql;
	}// end of isSQL() method.
}
