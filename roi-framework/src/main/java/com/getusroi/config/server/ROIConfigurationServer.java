package com.getusroi.config.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationConstant;
import com.getusroi.config.beans.ConfigurationUnit;
import com.getusroi.config.event.ROIConfigurationListener;
import com.getusroi.core.datagrid.DataGridService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ROIConfigurationServer {
	protected static final Logger logger = LoggerFactory.getLogger(ROIConfigurationServer.class);

	private static ROIConfigurationServer configService;

	private HazelcastInstance hazelcastInstance;
	/** #TODO Refactor and Remove this Map not Needed */
	IMap<String, String> configGroupKeys = null;

	/** Singleton method **/
	public static ROIConfigurationServer getConfigurationService() throws ConfigServerInitializationException {
		if (configService == null) {
			synchronized (ROIConfigurationServer.class) {
				try {
					logger.debug("initialized ROIConfigurationServer");
					HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
					List<String> imapkeys = new ArrayList<>();
					IMap<String, String> configGroupKeys = hazelcastInstance
							.getMap(ConfigurationConstant.CONFIG_GROUP_KEYS);
					configService = new ROIConfigurationServer(hazelcastInstance, configGroupKeys);
				} catch (Exception exp) {
					logger.error("ROIConfigurationServer initializatiob error", exp);
					throw new ConfigServerInitializationException("Failed to initialize the ConfigServer", exp);
				}
			}
		}
		return configService;
	}// end of singleton method

	// public HazelcastInstance getHazelcastInstance(){
	// return hazelcastInstance;
	// }

	public List<ConfigurationUnit> getConfigUnitList() {
		logger.debug("inside getConfigUnitList");
		List<ConfigurationUnit> configunitList = new ArrayList<>();
		IMap<String, String> configGroupMap = hazelcastInstance.getMap(ConfigurationConstant.CONFIG_GROUP_KEYS);

		if (configGroupMap != null && !configGroupMap.isEmpty()) {

			for (Map.Entry<String, String> configgroupkeys : configGroupMap.entrySet()) {
				String configgroupkey = configgroupkeys.getValue();
				logger.debug("config group key : " + configgroupkey);
				IMap<String, ConfigurationUnit> configGroupMapList = hazelcastInstance.getMap(configgroupkey);

				if (configGroupMapList != null && !configGroupMapList.isEmpty()) {
					for (Map.Entry<String, ConfigurationUnit> configunit : configGroupMapList.entrySet()) {
						configunitList.add(configunit.getValue());

					} // end of inner for
				} else {
					logger.debug("no configuration unit with config key : " + configgroupkey);
				} // end of else of inner if
			} // end of outter for
		} else {
			logger.debug("no config group key list with : " + ConfigurationConstant.CONFIG_GROUP_KEYS);
		} // end of else of outter if
		return configunitList;
	}

	public List<ConfigurationUnit> getSpecificConfigUnitList(String groupkey) {
		logger.debug("inside getSpecificConfigUnitList");
		List<ConfigurationUnit> configunitList = new ArrayList<>();

		IMap<String, ConfigurationUnit> configGroupMapList = hazelcastInstance.getMap(groupkey);

		if (configGroupMapList != null && !configGroupMapList.isEmpty()) {
			for (Map.Entry<String, ConfigurationUnit> configunit : configGroupMapList.entrySet()) {
				logger.debug("inside for loop ");
				configunitList.add(configunit.getValue());

			} // end of inner for
		} else {
			logger.debug("no configuration unit with config key : " + groupkey);
		} // end of else of inner if

		return configunitList;
	}

	/** Private constructor due to singleton */
	private ROIConfigurationServer(HazelcastInstance hazelcastInstance, IMap<String, String> configGroupKeys) {
		this.hazelcastInstance = hazelcastInstance;

		this.configGroupKeys = configGroupKeys;
	}

	public void addConfiguration(ConfigurationUnit configUnit) {
		logger.debug("addConfiguration called ");
		String groupkey = getGroupKey(configUnit);
		IMap<String, ConfigurationUnit> groupConfigMap = hazelcastInstance.getMap(groupkey);
		groupConfigMap.put(configUnit.getKey(), configUnit);
		logger.debug("addConfiguration added Config with GroupKey=" + groupkey + "--Key=" + configUnit.getKey()
				+ "--ConfigUnit=" + configUnit);		
		
	}

	/**
	 * 
	 * @param configUnit
	 */
	public void addConfigurationWithoutTenant(ConfigurationUnit configUnit) {
		logger.debug("addConfiguration called ");
		String groupkey = getGroupKeyWithoutTenant(configUnit);
		IMap<String, ConfigurationUnit> groupConfigMap = hazelcastInstance.getMap(groupkey);
		groupConfigMap.put(configUnit.getKey(), configUnit);
		logger.debug("addConfiguration added Config with GroupKey=" + groupkey + "--Key=" + configUnit.getKey()
				+ "--ConfigUnit=" + configUnit);
	}// ..end of the method

	public void addConfiguration(List<ConfigurationUnit> configUnits) {
		for (ConfigurationUnit configUnit : configUnits) {
			addConfiguration(configUnit);
		}
	}

	public ConfigurationUnit getConfiguration(String tenantId, String configGroup, String configKey) {
		logger.debug(".getConfiguration() of ROICOnfiguration Service -> tenantid :" + tenantId + ", config group :"
				+ configGroup + ", config key :" + configKey);
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(tenantId, configGroup));
		logger.debug("config Group map : " + configGroupMap);
		if (configGroupMap == null && configGroupMap.isEmpty()) {
			logger.debug("config map is empty/null");
			return null;
		}
		logger.debug("config unit : " + configGroupMap.get(configKey));
		return configGroupMap.get(configKey);
	}

	/**
	 * to get the configuration unit by passing the key value, where
	 * tenant-vendorId has been changed to get only 'VendorNodeId'
	 * 
	 * @param configGroup
	 * @param configKey
	 * @return configurationUnit Object
	 */
	public ConfigurationUnit getConfiguration(String configGroup, String configKey) {
		logger.debug(".getConfiguration().. of ROIConfiguration Service: " + configGroup + " -configKey- " + configKey);
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(configGroup));
		logger.debug("IMapKey-:-" + configGroupMap);
		if (configGroupMap.isEmpty()) {
			logger.debug("Config Imap is null");
		}
		return configGroupMap.get(configKey);
	}// ..end of the method

	public void deleteConfiguration(String tenantId, String configGroup, String configKey) {
		logger.debug(".deleteConfiguration inROIConfigurationServer");
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(tenantId, configGroup));
//		logger.debug("configGroupMap : "+configGroupMap);
		if (configGroupMap != null && !configGroupMap.isEmpty())
			configGroupMap.remove(configKey);
	}

	public void changeConfigState(String tenantId, String configGroup, String configKey, boolean enableConfig) {
		logger.debug("inside changeConfigState()");
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(tenantId, configGroup));
		if (configGroupMap != null && !configGroupMap.isEmpty()) {
			ConfigurationUnit configUntit = configGroupMap.get(configKey);
			configUntit.setIsEnabled(new Boolean(enableConfig));
			configGroupMap.put(configKey, configUntit);
			logger.debug("changed config unit : " + configGroupMap.get(configKey).getIsEnabled());
		}
	}

	public void addConfigListener(String tenantId, String configGroup, ROIConfigurationListener listener) {
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(tenantId, configGroup));
		if (configGroupMap != null) {
			configGroupMap.addEntryListener(listener, true);
		}
	}

	private String getGroupKey(ConfigurationUnit configUnit) {
		String tenantId = configUnit.getTenantId();
		String groupId = configUnit.getConfigGroup();
		storeGroupKeys(tenantId + "-" + groupId);
		return tenantId + "-" + groupId;
	}

	private String getGroupKeyWithoutTenant(ConfigurationUnit configurationUnit) {
		return configurationUnit.getConfigGroup();
	}

	private String getGroupKey(String tenantId, String configGroup) {
		return tenantId + "-" + configGroup;
	}

	private String getGroupKey(String configGroup) {
		return configGroup;
	}

	private void storeGroupKeys(String configGroupKey) {

		configGroupKeys.put(configGroupKey, configGroupKey);
	}
}
