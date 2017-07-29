package com.getusroi.eventframework.abstractbean.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraUtil {
	static Logger logger=LoggerFactory.getLogger(CassandraUtil.class);
	private static Properties prop=null;
	private static Properties paasProp=null;
	private static Properties appsDeploymentEnvProp=null;
	public static final String CONFIG_PROPERTY_FILE="cassandraDBConfig.properties";
	public static final String PAAS_CONFIG_PROPERTY_FILE="paasDeploymentCassandraDBConfig.properties";
	public static final String APPS_DEPLOYEMENT_ENVIRONMENT_CONFIG_PROPERTY_FILE="appsDeploymentEnvConfig.properties";
	public static final String DRIVER_CLASS_KEY="driver_class";
	public static final String URL_KEY="url";
	public static final String HOST_KEY="host";
	public static final String PORT_KEY="port";
	public static final String KEYSPACE_KEY="keyspace";
	public static final String DEPLOYMENT_ENVIRONMENT_KEY="deploymentEnvConfig";
	public static final String CASSANDRA_IP="CASSANDRA_IP";
	public static final String JDBC_CASSANDRA_PROTOCOL="jdbc:cassandra://";
	public static final String JDBC_CASSANDRA_PROTOCOL_COLON_SEPERATED=":";
	public static final String JDBC_CASSANDRA_PROTOCOL_SEPERATED="/";
	public static final String LOCAL_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY="Local";
	public static final String PAAS_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY="PAAS";
	
	
	/**
	 * This static block is used to load the cassandra config properties
	 */
	static{
		 prop = new Properties();
		 paasProp=new Properties();
		 appsDeploymentEnvProp=new Properties();
		InputStream input = CassandraUtil.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTY_FILE);
		InputStream paasinput = CassandraUtil.class.getClassLoader().getResourceAsStream(PAAS_CONFIG_PROPERTY_FILE);
		InputStream appsDeploymentEnvinput = CassandraUtil.class.getClassLoader().getResourceAsStream(APPS_DEPLOYEMENT_ENVIRONMENT_CONFIG_PROPERTY_FILE);
		try {
			prop.load(input);
			paasProp.load(paasinput);
			appsDeploymentEnvProp.load(appsDeploymentEnvinput);
		} catch (IOException e) {
			logger.warn(
					"unable to load property file = " + CONFIG_PROPERTY_FILE+" or "+PAAS_CONFIG_PROPERTY_FILE+" or "+APPS_DEPLOYEMENT_ENVIRONMENT_CONFIG_PROPERTY_FILE);
		}
	}

	/**
	 * This method is used to load the local cassandra config
	 * @return Properties Object
	 * @throws ConnectionConfigurationException
	 */
	public static Properties getCassandraConfigProperties() throws ConnectionConfigurationException {
		if(prop !=null){
			return prop;
		}else{
			throw new ConnectionConfigurationException(
					"unable to load property file = " + CONFIG_PROPERTY_FILE);
		}
		
	}//end of method
	
	/**
	 * This method is used to load the paas cassandra config
	 * @return Properties Object
	 * @throws ConnectionConfigurationException
	 */
	public static Properties getPAASCassandraConfigProperties() throws ConnectionConfigurationException{
		if(paasProp !=null){
			return paasProp;
		}else{
			throw new ConnectionConfigurationException(
					"unable to load property file = " + PAAS_CONFIG_PROPERTY_FILE);
		}
	}
	
	/**
	 * This method is used to load the paas cassandra config
	 * @return Properties Object
	 * @throws ConnectionConfigurationException
	 */
	public static Properties getAppsDeploymentEnvConfigProperties() throws ConnectionConfigurationException{
		if(appsDeploymentEnvProp !=null){
			return appsDeploymentEnvProp;
		}else{
			throw new ConnectionConfigurationException(
					"unable to load property file = " + APPS_DEPLOYEMENT_ENVIRONMENT_CONFIG_PROPERTY_FILE);
		}
	}
	
	/**
	 * This method is used to get the cassandra connection
	 * @return
	 * @throws CassandraConnectionException
	 *//*
	protected Connection getCassandraConnection() throws CassandraConnectionException{
		logger.debug(".getCassandraConnection method of AbstractCassandraBean ");
		Connection con = null;
		Properties prop=null;
		try {
			prop = CassandraUtil.getCassandraConfigProperties();
			try {
				Class.forName(prop.getProperty(DRIVER_CLASS_KEY));
				try {
					con = DriverManager
							.getConnection(prop.getProperty(URL_KEY));
					logger.debug("Connection Object : "+con);
					return con;
				} catch (SQLException e) {
					throw new CassandraConnectionException("unable to get the connection object for cassandra : "+prop.getProperty(URL_KEY),e);
				}
			} catch (ClassNotFoundException e) {
				throw new CassandraConnectionException("unable to load the driver name for cassandra : "+prop.getProperty(DRIVER_CLASS_KEY),e);
			}
		} catch (ConnectionConfigurationException e1) {
			throw new CassandraConnectionException("unable to get the connection object for cassandra : "+prop.getProperty(CONFIG_PROPERTY_FILE),e1);
		}
		
		
	}//end of method
	
	protected Cluster getCassandraCluster() throws CassandraClusterException{
		logger.debug(".getCassandraCluster method of AbstractCassandraBean ");
		Cluster cluster = null;
		Properties prop=null;
		try {
			prop = CassandraUtil.getCassandraConfigProperties();
			String host = prop.getProperty(CassandraUtil.HOST_KEY);
			int port = Integer.parseInt(prop.getProperty(CassandraUtil.PORT_KEY));			
			try{
				cluster = Cluster.builder().addContactPoint(host)
						.withPort(port).build();
				logger.debug("cluster Object : "+cluster);
				return cluster;	
			}
			catch(Exception e){
				throw new CassandraClusterException("unable to connect to the host for cassandra : "+prop.getProperty("host"),e);
			}
		} catch (ConnectionConfigurationException e1) {
			throw new CassandraClusterException("unable to get the connection object for cassandra : "+prop.getProperty(CONFIG_PROPERTY_FILE),e1);
		}		
			
	}//end of method
	
	protected UpdateableDataContext getUpdateableDataContextForCassandra(Connection connection){
		logger.debug(".getUpdateableDataContextForCassandra method of AbstractCassandraBean");
		UpdateableDataContext dataContext = DataContextFactory
				.createJdbcDataContext(connection);
		return dataContext;
	}
	
	protected DataContext getDataContextForCassandraByCluster(Cluster cluster,String keyspace){
		logger.debug(".getDataContextForCassandraByCluster method of AbstractCassandraBean");
		DataContext dataContext = DataContextFactory
				.createCassandraDataContext(cluster, keyspace);
		return dataContext;
	}
	
	protected Table getTableForDataContext(DataContext datacontext,String tableName){
		logger.debug(".getTableForDataContext method of AbstractCassandraBean");
		Table table=datacontext.getTableByQualifiedLabel(tableName);
		return table;

	}
	
	public static void main(String[] args) throws ConnectionConfigurationException, CassandraConnectionException {
		CassandraUtil util=new CassandraUtil();
		Properties prop=util.getCassandraConfigProperties();
		System.out.println("prop : "+prop);
		Connection conn=util.getCassandraConnection();
		System.out.println("con : "+conn);
	}*/
	
/*	public static void main(String[] args) {
		final String java=System.getenv("CASSANDRA_IP");
		System.out.println(java);
				
	}*/
}

