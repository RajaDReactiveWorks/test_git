package com.attunedlabs.config.configbuilder;

import java.io.File;
import java.util.List;

import com.attunedlabs.config.beans.ConfigurationUnit;

public interface IConfigBuilder {
	public List<ConfigurationUnit> buildConfigurationUnit(File configFileToLoad,String directory);
	public boolean supportedEventingXML(File configFileToLoad,String directory);
}
