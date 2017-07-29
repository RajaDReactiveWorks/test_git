package com.getusroi.core.datagrid;

import static com.getusroi.eventframework.abstractbean.util.CassandraUtil.LOCAL_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY;
import static com.getusroi.eventframework.abstractbean.util.CassandraUtil.PAAS_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY;

import java.io.Serializable;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigurationTreeNode;
import com.getusroi.eventframework.abstractbean.util.CassandraUtil;
import com.getusroi.eventframework.abstractbean.util.ConnectionConfigurationException;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;

/**
 * This class is just a wrapper around Hazelcast instance
 * @author Bizruntime
 *
 */
public class DataGridService {
	protected static final Logger logger = LoggerFactory.getLogger(DataGridService.class);
	public static final String PROCESSOR_EXECUTOR_KEY="PROC";
	public static final String DISPATCHER_EXECUTOR_KEY="DISP";
	public static final String BUILDER_EXECUTOR_KEY="BUILDER";
	public static final String SYSEVENT_DISPATCHER_KEY="SYSEVTDIS";
	public static final String HAZELCAST_LOCAL_CONFIG_XML_KEY="hazelcast.xml";
	public static final String HAZELCAST_PAAS_CONFIG_XML_KEY="hazelcast-paas.xml";
	
	private HazelcastInstance hazelcastInstance;
	private static DataGridService hcService;

	private DataGridService(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	/**
	 * This method is used to created single object of hazelcast
	 * @return
	 * 
	 */
	public static DataGridService getDataGridInstance()  {

		if (hcService == null) {

			synchronized (DataGridService.class) {
				Properties prop;
				try {
					prop = CassandraUtil.getAppsDeploymentEnvConfigProperties();
					String deployemntEnv=prop.getProperty(CassandraUtil.DEPLOYMENT_ENVIRONMENT_KEY);
					if(deployemntEnv!=null && !(deployemntEnv.isEmpty()) && deployemntEnv.length()>0 && deployemntEnv.equalsIgnoreCase(PAAS_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY)){
						Config config=new ClasspathXmlConfig(HAZELCAST_PAAS_CONFIG_XML_KEY);
						config.setProperty("hazelcast.logging.type", "slf4j");
						config.setClassLoader(ConfigurationTreeNode.class.getClassLoader());
						HazelcastInstance hcInstance = Hazelcast.newHazelcastInstance(config);
						hcService = new DataGridService(hcInstance);
					}else{
						Config config=new ClasspathXmlConfig(HAZELCAST_LOCAL_CONFIG_XML_KEY);
						config.setProperty("hazelcast.logging.type", "slf4j");
						config.setClassLoader(ConfigurationTreeNode.class.getClassLoader());
						HazelcastInstance hcInstance = Hazelcast.newHazelcastInstance(config);
						hcService = new DataGridService(hcInstance);
					}
				} catch (ConnectionConfigurationException e) {
					logger.error("Problem in getting the deloyment config file ",e);
				}
				
				
			}//end of sync block
		}
		return hcService;
	}

	/**
	 * This method is to return hazelcast Instance object
	 * @return HazelcastInstance
	 */
	public HazelcastInstance getHazelcastInstance() {

		return hazelcastInstance;

	}
	public IExecutorService getDispatcherExecutor(String tenantId){
		String verifiedTenant=verifyTenant(tenantId);
		IExecutorService despExecutor=hazelcastInstance.getExecutorService(verifiedTenant+"-"+DISPATCHER_EXECUTOR_KEY);
		return despExecutor;
	}
	
	
	public IExecutorService getEventBuilderExecutor(String tenantId){
		String verifiedTenant=verifyTenant(tenantId);
		IExecutorService builderExecutor=hazelcastInstance.getExecutorService(verifiedTenant+"-"+BUILDER_EXECUTOR_KEY);
		return builderExecutor;
	}
	
	public IExecutorService getSystemEventDispatcherExecutor(String tenantId){
		String verifiedTenant=verifyTenant(tenantId);
		IExecutorService builderExecutor=hazelcastInstance.getExecutorService(verifiedTenant+"-"+SYSEVENT_DISPATCHER_KEY);
		return builderExecutor;
	}
	
	/**
	 * This method is to add configuration Listener
	 * @param tenantId : String 
	 * @param configGroup : String 
	 * @param listener : ROIConfigurationListener
	 */
	public void addConfigListener(String groupKey,ConfigurationTreeNodeListener listener){
		IMap<String, Serializable> configGroupMap=hazelcastInstance.getMap(groupKey);
		if(configGroupMap!=null){
			configGroupMap.addEntryListener(listener,true);
		}
	}
	
	public Long getClusterUniqueId(String idTypeName){
		IdGenerator idGen = hazelcastInstance.getIdGenerator(idTypeName);
		Long id = idGen.newId();
		return id;
	}
	
	private String verifyTenant(String tenantId){
		if(tenantId==null || tenantId.isEmpty())
			return "default";
		return tenantId.trim();
	}	
	
}
