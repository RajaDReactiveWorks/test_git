package com.getusroi.integrationfwk.jdbcIntactivity.config.persistence.impl;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.integrationfwk.jdbcIntactivity.config.persistence.JdbcIntActivityPersistenceException;

public class JdbcIntActivityMetaModelUtil {
	static final Logger logger = LoggerFactory.getLogger(JdbcIntActivityMetaModelUtil.class.getName());

	/**
	 * to get the datasource object by lookup called in the processor
	 * 
	 * @param context
	 * @param lookupName
	 * @return dataSourceObject
	 * @throws JdbcIntActivityPersistenceException
	 */
	public static DataSource getDataSource(CamelContext context, String lookupName)
			throws JdbcIntActivityPersistenceException {
		DataSource datasource = (DataSource) context.getRegistry().lookupByName(lookupName);
		logger.debug("dataSource object by exchange lookup..: " + datasource);
		if (!(datasource==null)) {
			return datasource;
		} else {
			throw new JdbcIntActivityPersistenceException(
					"Unable to lookup " + lookupName + " the dataSource from the Context");
		}
	}// ..end of the method

}
