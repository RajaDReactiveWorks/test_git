package com.attunedlabs.leap.header.initializer;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootDeployableConfiguration {
	protected static final Logger logger = LoggerFactory.getLogger(RootDeployableConfiguration.class);
	private static final String ROOT_DEPLOYABLE_PROPERTIES = "globalAppDeploymentConfig.properties";
	private static final String ROOT_DEPLOYABLE_FEATURES_KEY = "rootDeployableFeatures";
	private static final String FEATURE_LIST_SEPERATOR = ",";
	private static final String FEATURE_SEPERATOR = "-";

	public static boolean isRootDeployableFeature(String featureGroup, String featureName) {
		try {
			Properties properties = new Properties();
			properties.load(
					RootDeployableConfiguration.class.getClassLoader().getResourceAsStream(ROOT_DEPLOYABLE_PROPERTIES));
			String allFeatures = properties.getProperty(ROOT_DEPLOYABLE_FEATURES_KEY);
			if (allFeatures != null) {
				boolean isRoot = false;
				String[] featureArray = allFeatures.trim().split(FEATURE_LIST_SEPERATOR);
				for (int i = 0; i < featureArray.length; i++) {
					String featureCombo = featureArray[i];
					String[] featureComboArray = featureCombo.trim().split(FEATURE_SEPERATOR);
					if(featureComboArray.length == 2 && !isRoot)
						isRoot = featureComboArray[0].trim().equalsIgnoreCase(featureGroup) && featureComboArray[1].trim().equalsIgnoreCase(featureName);
				}
				return isRoot;
			}
		} catch (IOException e) {
			logger.warn("failed to identify root deployable feature..." + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

}
