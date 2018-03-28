package com.attunedlabs.config.util;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * singleton
 * 
 * @author Reactiveworks42
 *
 */
public class DataSourceInstance {
	final static Logger logger = LoggerFactory.getLogger(DataSourceInstance.class);

	private static String URL = null;
	private static String DRIVER_CLASS = null;
	private static String USER = null;
	private static String PASSWORD = null;
	private static DataSourceInstance ds;
	private ComboPooledDataSource cpds;

	private DataSourceInstance() {
		cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass(DRIVER_CLASS);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		cpds.setJdbcUrl(URL);
		cpds.setUser(USER);
		cpds.setPassword(PASSWORD);
		cpds.setMinPoolSize(5);
		cpds.setAcquireIncrement(5);
		cpds.setMaxPoolSize(10);
	}

	public static DataSourceInstance getInstance() throws IOException {
		if (URL == null) {
			loadConfigrationDbPropertyFile();
		}
		if (ds == null) {
			ds = new DataSourceInstance();
			return ds;
		} else
			return ds;
	}

	public static Connection getConnection() throws SQLException, IOException {
		Connection con =getInstance().cpds.getConnection();
		return con;
	}

	public ComboPooledDataSource getDataSource() throws SQLException {
		return this.cpds;
	}

	public static void closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (Exception e) {
			logger.error("failed to close connection instance due to ..." + e.getMessage());
		}
	}

	private synchronized static void loadConfigrationDbPropertyFile() throws IOException {
		Properties properties = new Properties();
		properties.load(DataBaseUtil.class.getClassLoader().getResourceAsStream("globalAppDeploymentConfig.properties"));
		URL = properties.getProperty("DB_URL");
		DRIVER_CLASS = properties.getProperty("DB_DRIVER_CLASS");
		USER = properties.getProperty("DB_USER");
		PASSWORD = properties.getProperty("DB_PASSWORD");
	}
}
