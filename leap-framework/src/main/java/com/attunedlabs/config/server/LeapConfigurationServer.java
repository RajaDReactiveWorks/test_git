package com.attunedlabs.config.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.beans.ConfigurationUnit;
import com.attunedlabs.config.event.LeapConfigurationListener;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.featuremetainfo.jaxb.Feature;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;

public class LeapConfigurationServer {
	protected static final Logger logger = LoggerFactory.getLogger(LeapConfigurationServer.class);

	private static LeapConfigurationServer configService;

	private HazelcastInstance hazelcastInstance;
	/** #TODO Refactor and Remove this Map not Needed */
	IMap<String, String> configGroupKeys = null;

	/** Singleton method **/
	public static LeapConfigurationServer getConfigurationService() throws ConfigServerInitializationException {
		if (configService == null) {
			synchronized (LeapConfigurationServer.class) {
				try {
					logger.debug("initialized LeapConfigurationServer");
					HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
					List<String> imapkeys = new ArrayList<>();
					IMap<String, String> configGroupKeys = hazelcastInstance
							.getMap(ConfigurationConstant.CONFIG_GROUP_KEYS);
					configService = new LeapConfigurationServer(hazelcastInstance, configGroupKeys);
				} catch (Exception exp) {
					logger.error("LeapConfigurationServer initializatiob error", exp);
					throw new ConfigServerInitializationException("Failed to initialize the ConfigServer", exp);
				}
			}
		}
		return configService;
	}// end of singleton method

	// public HazelcastInstance getHazelcastInstance(){
	// return hazelcastInstance;
	// }

	/**
	 * loading all configuration context in datagrid.
	 * 
	 * 
	 */
	public void loadFeatureInDataGrid(Feature feature, String featureGroupName) {
		ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant,
				LeapHeaderConstant.site, featureGroupName, feature.getName(), feature.getImplementationName(),
				feature.getVendorName(), feature.getVendorVersion());
		logger.debug("inside loadFeatureInDataGrid to load context : " + configContext);
		IList<String> listcontexts = hazelcastInstance.getList(ConfigurationConstant.ALL_SUBSCRIPTION_CONFIG_KEY);
		if (!listcontexts.contains(configContext.toString()))
			listcontexts.add(configContext.toString());

	}

	/**
	 * gives all the config-context configured.
	 * 
	 * @return
	 */
	public List<ConfigurationContext> getAllConfigContext() {
		logger.debug("inside getAllConfigContext to get all context");
		IList<String> listStringContexts = hazelcastInstance.getList(ConfigurationConstant.ALL_SUBSCRIPTION_CONFIG_KEY);

		List<ConfigurationContext> configurationContexts = new ArrayList<>();
		ConfigurationContext configurationContext = null;
		if (listStringContexts != null)
			for (String jsonContext : listStringContexts) {
				configurationContext = getConfigContextFromJson(jsonContext);
				if (configurationContext != null) {
					configurationContexts.add(configurationContext);
					configurationContext = null;
				}
			}
		logger.debug("list of all context" + configurationContexts);
		return configurationContexts;
	}

	/**
	 * build the configuration context based on the request context passed from
	 * the json body.
	 * 
	 * @param eventBody
	 *            consumed data.
	 * @return {@link ConfigurationContext}
	 */
	protected ConfigurationContext getConfigContextFromJson(String jsonContext) {
		ConfigurationContext configurationContext = null;
		try {
			configurationContext = new ObjectMapper().readValue(jsonContext, ConfigurationContext.class);
			logger.debug("config context getConfigContextFromEventBody.. " + configurationContext);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("unable to get requestCtx  from event body...", e);
		}
		return configurationContext;
	}

	/**
	 * loading subscribers for particular topic to dataGrid.
	 * 
	 * @param subscriptionId
	 * @param subscribeTopics
	 */
	public void loadSubscribersByTopicName(String subscriptionId, String subscribeTopics) {
		logger.debug("inside loadSubscribersByTopicName: subscriptionId" + subscriptionId + " subscription Topics : "
				+ subscribeTopics);
		IMap<String, Set<String>> topicMap = hazelcastInstance.getMap(ConfigurationConstant.TOPIC_SUBSCRIBERS_KEYS);
		if (!subscribeTopics.isEmpty()) {
			String[] topics = subscribeTopics.split(",");
			List<String> topicsParticularSubscriber = Arrays.asList(topics);
			if (topicsParticularSubscriber != null)
				for (String topicName : topicsParticularSubscriber) {
					Set<String> subscriberAlreadyRegistered = topicMap.get(topicName);
					if (subscriberAlreadyRegistered == null) {
						subscriberAlreadyRegistered = new HashSet<String>();
						subscriberAlreadyRegistered.add(subscriptionId);
					} else
						subscriberAlreadyRegistered.add(subscriptionId);
					topicMap.put(topicName, subscriberAlreadyRegistered);
				}
		}

	}
	/**
	 * getAllTopicSubscribers gets subscribers for particular topic.
	 * 
	 * 
	 * @return topicName
	 */
	public Set<String> getAllTopicSubscribers(String topicName) {
		logger.debug("inside getAllTopicSubscribers for topic" + topicName);
		Set<String> topicSubscribers = null;
		IMap<String, Set<String>> topicMap = hazelcastInstance.getMap(ConfigurationConstant.TOPIC_SUBSCRIBERS_KEYS);
		Set<String> topicNames = topicMap.keySet();
		if (topicNames != null && !topicName.isEmpty())
			for (String topic : topicNames) {
				if (topic.equals(topicName))
					topicSubscribers = topicMap.get(topicName);
			}
		if (topicSubscribers == null)
			topicSubscribers = new HashSet<>();
		return topicSubscribers;

	}
	/***
	 * loading the topicNames to dataGrid.
	 * 
	 * @param subscribeIdKey
	 * @param subscribeTopics
	 */
	public void loadSubscriberTopicBySubscribeId(String subscribeIdKey, String subscribeTopics) {
		logger.debug("inside loadSubscriberTopicBySubscribeId");
		IMap<String, String> topicMap = hazelcastInstance.getMap(ConfigurationConstant.SUBSCRIPTION_TOPIC_KEYS);
		topicMap.put(subscribeIdKey, subscribeTopics);
	}

	/**
	 * getSubscriptionTopicsbySubscriptionId gets all the subscribed topics and
	 * returns you the topic names to be subscribed.
	 * 
	 * @return topicNames
	 */
	public String getSubscriptionTopicsbySubscriptionId(String subscriptionId) {
		logger.debug("inside getSubscriptionTopicsbySubscriptionId");
		List<String> topicNames = new ArrayList<>();

		IMap<String, String> topicMap = hazelcastInstance.getMap(ConfigurationConstant.SUBSCRIPTION_TOPIC_KEYS);
		Set<String> subscriptionKeys = topicMap.keySet();
		if (subscriptionKeys != null)
			for (String subscriptionKey : subscriptionKeys) {
				if (subscriptionId.equals(subscriptionKey)) {
					String topicNamesStr = topicMap.get(subscriptionKey);
					String[] topics = topicNamesStr.split(",");
					List<String> topicsParticularSubscriber = Arrays.asList(topics);
					topicNames.addAll(topicsParticularSubscriber);
				}
			}

		Set<String> topicNamesSet = new HashSet<String>(topicNames);
		logger.debug("topicNamesSet : " + topicNamesSet);
		return topicNamesSet.toString().replace("[", "").trim().replace("]", "").trim().replace(" ", "".trim());
	}
	
	/**
	 * getAllSubscribersAvailable gets all the subscribed topics and
	 * returns you the topic names to be subscribed.
	 * 
	 * @return topicNames
	 */
	public Set<String> getAllSubscribersAvailable() {
		logger.debug("inside getAllSubscribersAvailable");
		IMap<String, String> topicMap = hazelcastInstance.getMap(ConfigurationConstant.SUBSCRIPTION_TOPIC_KEYS);
		Set<String> subscriptionKeys = topicMap.keySet();
		logger.debug("subscriptionKeys : " + subscriptionKeys);
		return subscriptionKeys;
	}

	/**
	 * getAllSubscriberTopic gets all the subscribed topics and returns you the
	 * topic names to be subscribed.
	 * 
	 * @return topicNames
	 */
	public String getAllSubscriberTopic() {
		logger.debug("inside getAllSubscriberTopic");
		List<String> topicNames = new ArrayList<>();

		IMap<String, String> topicMap = hazelcastInstance.getMap(ConfigurationConstant.SUBSCRIPTION_TOPIC_KEYS);
		Set<String> subscriptionKeys = topicMap.keySet();
		if (subscriptionKeys != null)
			for (String subscriptionKey : subscriptionKeys) {
				String topicNamesStr = topicMap.get(subscriptionKey);
				String[] topics = topicNamesStr.split(",");
				List<String> topicsParticularSubscriber = Arrays.asList(topics);
				topicNames.addAll(topicsParticularSubscriber);
			}

		Set<String> topicNamesSet = new HashSet<String>(topicNames);
		logger.debug("topicNamesSet : " + topicNamesSet);
		return topicNamesSet.toString().replace("[", "").trim().replace("]", "").trim().replace(" ", "".trim());
	}

	

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
	private LeapConfigurationServer(HazelcastInstance hazelcastInstance, IMap<String, String> configGroupKeys) {
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
		logger.debug(".getConfiguration() of LeapCOnfiguration Service -> tenantid :" + tenantId + ", config group :"
				+ configGroup + ", config key :" + configKey);
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(tenantId, configGroup));
		logger.debug("config Group map : " + configGroupMap);
		if (configGroupMap == null && configGroupMap.isEmpty()) {
			logger.debug("config map is empty/null");
			return null;
		}
		// logger.debug("config unit : " + configGroupMap.get(configKey));
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
		logger.debug(
				".getConfiguration().. of LeapConfiguration Service: " + configGroup + " -configKey- " + configKey);
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(configGroup));
		logger.debug("IMapKey-:-" + configGroupMap);
		if (configGroupMap.isEmpty()) {
			logger.debug("Config Imap is null");
		}
		return configGroupMap.get(configKey);
	}// ..end of the method

	public void deleteConfiguration(String tenantId, String configGroup, String configKey) {
		logger.debug(".deleteConfiguration inLeapConfigurationServer");
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(tenantId, configGroup));
		// logger.debug("configGroupMap : "+configGroupMap);
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

	public void addConfigListener(String tenantId, String configGroup, LeapConfigurationListener listener) {
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
