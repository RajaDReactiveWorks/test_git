package com.attunedlabs.eventframework.abstractbean;

import java.sql.SQLException;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.camel.Exchange;
import org.springframework.jdbc.core.JdbcTemplate;

import com.hazelcast.transaction.TransactionContext;

/**
 * This class is setting jdbc template with mysql XA transaction
 * 
 * @author ubuntu
 *
 */
public abstract class AbstractLeapCamelJDBCBean extends AbstractLeapCamelBean {

	protected JdbcTemplate jdbc;
	public TransactionContext context = null;
	protected XADataSource ds;
	public void setDataSource(DataSource ds) {
		jdbc = new JdbcTemplate(ds);
	}

	@Override
	abstract protected void processBean(Exchange exch) throws Exception;

}
