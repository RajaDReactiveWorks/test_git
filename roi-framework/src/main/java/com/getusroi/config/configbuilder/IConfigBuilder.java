package com.getusroi.config.configbuilder;

import java.io.File;
import java.util.List;

import com.getusroi.config.beans.ConfigurationUnit;

public interface IConfigBuilder {
	public List<ConfigurationUnit> buildConfigurationUnit(File configFileToLoad,String directory);
	public boolean supportedEventingXML(File configFileToLoad,String directory);
}
