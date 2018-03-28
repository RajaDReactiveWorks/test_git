package com.attunedlabs.scheduler.config;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.scheduler.ScheduledJobConfigurationException;
import com.attunedlabs.scheduler.jaxb.ScheduledJobConfiguration;

public interface IScheduledJobConfigurationService {
	
	public boolean checkScheduledJobConfigarationExistOrNot(ConfigurationContext configurationContext,String configName) throws ScheduledJobConfigRequestException;
	
	public void addScheduledJobConfiguration(ConfigurationContext configurationContext, ScheduledJobConfiguration schedulerConfig) throws ScheduledJobConfigurationException;

}
