package com.attunedlabs.eventframework.abstractbean;

import static com.attunedlabs.eventframework.abstractbean.util.CassandraUtil.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.abstractbean.util.CassandraClusterException;
import com.attunedlabs.eventframework.abstractbean.util.CassandraConnectionException;
import com.attunedlabs.eventframework.abstractbean.util.CassandraUtil;
import com.attunedlabs.eventframework.abstractbean.util.ConnectionConfigurationException;
import com.datastax.driver.core.Cluster;

public abstract class AbstractCassandraBean extends AbstractLeapCamelBean {
	Logger logger = LoggerFactory.getLogger(AbstractCassandraBean.class);
	Connection con = null;

	/**
	 * This method is used to get the cassandra connection
	 * 
	 * @return
	 * @throws CassandraConnectionException
	 */
	protected Connection getCassandraConnection() throws CassandraConnectionException {
		logger.debug(".getCassandraConnection method of AbstractCassandraBean ");
		Properties prop = null;
		try {
			prop = CassandraUtil.getAppsDeploymentEnvConfigProperties();
			if (prop != null) {
				String deployemntEnv = prop.getProperty(DEPLOYMENT_ENVIRONMENT_KEY);
				if (deployemntEnv != null && !(deployemntEnv.isEmpty()) && deployemntEnv.length() > 0
						&& deployemntEnv.equalsIgnoreCase(PAAS_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY)) {
					return getPAASCassandraConnection();
				} else if (deployemntEnv != null && !(deployemntEnv.isEmpty()) && deployemntEnv.length() > 0
						&& deployemntEnv.equalsIgnoreCase(LOCAL_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY)) {
					return getLocalCassandraConnection();
				} else {
					throw new CassandraConnectionException("Unsupportable deployment environment for cassandra "
							+ deployemntEnv + ". Please provide either PAAS or Local");
				}
			} else {
				throw new CassandraConnectionException("Error while fetching the cassadra deployemnt detail ");
			}
		} catch (ConnectionConfigurationException e2) {
			throw new CassandraConnectionException("unable to get the connection object for cassandra : "
					+ prop.getProperty(APPS_DEPLOYEMENT_ENVIRONMENT_CONFIG_PROPERTY_FILE), e2);
		}

	}// end of method

	/**
	 * This method is used to get the cassandra cluster
	 * 
	 * @return
	 * @throws CassandraClusterException
	 */
	protected Cluster getCassandraCluster() throws CassandraClusterException {
		logger.debug(".getCassandraCluster method of AbstractCassandraBean ");
		Properties prop = null;
		try {
			prop = CassandraUtil.getAppsDeploymentEnvConfigProperties();
			if (prop != null) {
				String deployemntEnv = prop.getProperty(DEPLOYMENT_ENVIRONMENT_KEY);
				if (deployemntEnv != null && !(deployemntEnv.isEmpty()) && deployemntEnv.length() > 0
						&& deployemntEnv.equalsIgnoreCase(PAAS_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY)) {
					return getPAASCassandraCluster();
				} else if (deployemntEnv != null && !(deployemntEnv.isEmpty()) && deployemntEnv.length() > 0
						&& deployemntEnv.equalsIgnoreCase(LOCAL_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY)) {
					return getLocalCassandraCluster();
				} else {
					throw new CassandraClusterException(
							"Unsupportable deployment environment for cassandra " + deployemntEnv);
				}
			} else {
				throw new CassandraClusterException("Error while fetching the cassadra deployemnt detail ");
			}
		} catch (ConnectionConfigurationException e2) {
			throw new CassandraClusterException("unable to get the connection object for cassandra : "
					+ prop.getProperty(APPS_DEPLOYEMENT_ENVIRONMENT_CONFIG_PROPERTY_FILE), e2);
		}

	}// end of method getCassandraCluster

	protected UpdateableDataContext getUpdateableDataContextForCassandra(Connection connection) {
		logger.debug(".getUpdateableDataContextForCassandra method of AbstractCassandraBean");
		UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
		return dataContext;
	}

	protected DataContext getDataContextForCassandraByCluster(Cluster cluster, String keyspace) {
		logger.debug(".getDataContextForCassandraByCluster method of AbstractCassandraBean");
		DataContext dataContext = DataContextFactory.createCassandraDataContext(cluster, keyspace);
		return dataContext;
	}

	protected Table getTableForDataContext(DataContext datacontext, String tableName) {
		logger.debug(".getTableForDataContext method of AbstractCassandraBean");
		Table table = datacontext.getTableByQualifiedLabel(tableName);
		return table;

	}

	@Override
	abstract protected void processBean(Exchange exch) throws Exception;

	/**
	 * This method is used to get data require for Cassandra local
	 * 
	 * @return Connection Object
	 * @throws CassandraConnectionException
	 */
	private Connection getLocalCassandraConnection() throws CassandraConnectionException {
		logger.debug(".getLocalCassandraConnection method of AbstractCassandraBean ");
		Properties prop = null;
		try {
			if (con == null || con.isClosed()) {
				prop = CassandraUtil.getCassandraConfigProperties();
				try {
					Class.forName(prop.getProperty(DRIVER_CLASS_KEY));
					try {
						con = DriverManager.getConnection(prop.getProperty(URL_KEY));
						logger.debug("Connection Object : " + con);
						return con;
					} catch (SQLException e) {
						throw new CassandraConnectionException(
								"URL to get the connection object for cassandra is invalid : "
										+ prop.getProperty(URL_KEY),
								e);
					}
				} catch (ClassNotFoundException e) {
					throw new CassandraConnectionException(
							"unable to load the driver name for cassandra : " + prop.getProperty(DRIVER_CLASS_KEY), e);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CassandraConnectionException(
					"unable to get the connection object for cassandra : " + prop.getProperty(CONFIG_PROPERTY_FILE), e);
		}
		return con;
	}// end
		// of
		// method
		// getLocalCassandraConnection

	/**
	 * This method is used to get data require for Cassandra PAAS
	 * 
	 * @return Connection Object
	 * @throws CassandraConnectionException
	 */
	private Connection getPAASCassandraConnection() throws CassandraConnectionException {
		logger.debug(".getPAASCassandraConnection method of AbstractCassandraBean ");
		Connection con = null;
		Properties prop = null;
		try {
			prop = CassandraUtil.getPAASCassandraConfigProperties();
			try {
				Class.forName(prop.getProperty(DRIVER_CLASS_KEY));
				String cassandraIP = System.getenv(CASSANDRA_IP);
				if (cassandraIP != null && !(cassandraIP.isEmpty()) && cassandraIP.length() > 0) {
					String cassandraPort = prop.getProperty(PORT_KEY);
					String cassandraKeysapce = prop.getProperty(KEYSPACE_KEY);
					if ((cassandraPort != null && !(cassandraPort.isEmpty()) && cassandraPort.length() > 0)
							&& (cassandraKeysapce != null && !(cassandraKeysapce.isEmpty())
									&& cassandraKeysapce.length() > 0)) {
						// create url
						String url = JDBC_CASSANDRA_PROTOCOL + cassandraIP + JDBC_CASSANDRA_PROTOCOL_COLON_SEPERATED
								+ cassandraPort + JDBC_CASSANDRA_PROTOCOL_SEPERATED + cassandraKeysapce;
						logger.debug("cassandra db url : " + url);
						try {
							con = DriverManager.getConnection(url);
							logger.debug("Connection Object : " + con);
							return con;
						} catch (SQLException e) {
							throw new CassandraConnectionException(
									"unable to create cassandra connection object with url : " + url);
						}
					} else {
						throw new CassandraConnectionException(
								"Unable to create connection for cassandra with port and and keyspace : "
										+ cassandraPort + ", " + cassandraKeysapce);
					}
				} else {
					throw new CassandraConnectionException(
							"Unable to create connection for cassandra with an IP given in system evironment : "
									+ cassandraIP);
				}
			} catch (ClassNotFoundException e) {
				throw new CassandraConnectionException(
						"unable to load the driver name for cassandra : " + prop.getProperty(DRIVER_CLASS_KEY), e);
			}
		} catch (ConnectionConfigurationException e1) {
			throw new CassandraConnectionException("unable to get the connection object for cassandra : "
					+ prop.getProperty(PAAS_CONFIG_PROPERTY_FILE), e1);
		}
	}// end of method getPAASCassandraConnection

	/**
	 * This method is used to get cluster object of cassandra from local
	 * environment
	 * 
	 * @return
	 * @throws CassandraClusterException
	 */
	private Cluster getLocalCassandraCluster() throws CassandraClusterException {
		logger.debug(".getLocalCassandraCluster method of AbstractCassandraBean ");
		Cluster cluster = null;
		Properties prop = null;
		try {
			prop = CassandraUtil.getCassandraConfigProperties();
			String host = prop.getProperty(CassandraUtil.HOST_KEY);
			int port = Integer.parseInt(prop.getProperty(CassandraUtil.PORT_KEY));
			try {
				cluster = Cluster.builder().addContactPoint(host).withPort(port).build();
				logger.debug("cluster Object : " + cluster);
				return cluster;
			} catch (Exception e) {
				throw new CassandraClusterException(
						"unable to connect to the host for cassandra : " + prop.getProperty(CassandraUtil.HOST_KEY), e);
			}
		} catch (ConnectionConfigurationException e1) {
			throw new CassandraClusterException(
					"unable to get the connection object for cassandra : " + prop.getProperty(CONFIG_PROPERTY_FILE),
					e1);
		}
	}// end of method getLocalCassandraCluster

	/**
	 * This method is used to get cluster object of cassandra from PAAS
	 * environment
	 * 
	 * @return
	 * @throws CassandraClusterException
	 */
	private Cluster getPAASCassandraCluster() throws CassandraClusterException {
		logger.debug(".getPAASCassandraCluster method of AbstractCassandraBean ");
		Cluster cluster = null;
		Properties prop = null;
		try {
			prop = CassandraUtil.getPAASCassandraConfigProperties();
			String host = System.getenv(CASSANDRA_IP);
			if (host != null && !(host.isEmpty()) && host.length() > 0) {
				int port = Integer.parseInt(prop.getProperty(CassandraUtil.PORT_KEY));
				try {
					cluster = Cluster.builder().addContactPoint(host).withPort(port).build();
					logger.debug("cluster Object : " + cluster);
					return cluster;

				} catch (Exception e) {
					throw new CassandraClusterException(
							"unable to connect to the host for cassandra host and port : " + host + ", " + port, e);
				}
			} else {
				throw new CassandraClusterException(
						"Unable to create connection for cassandra with an IP given in system evironment : " + host);

			}
		} catch (ConnectionConfigurationException e1) {
			throw new CassandraClusterException("unable to get the connection object for cassandra : "
					+ prop.getProperty(PAAS_CONFIG_PROPERTY_FILE), e1);
		}
	}// end of method getPAASCassandraCluster
}
