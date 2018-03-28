package com.attunedlabs.config.configbuilder;

import java.util.ResourceBundle;


public class ConfigBuilderFactory {
	private static ResourceBundle rb ;
	
	private ConfigBuilderFactory(){}
	
	public static IConfigBuilder getConfigBuilder(String configType)throws ConfigTypeNotSupportedException{
		if(rb==null){
			rb = ResourceBundle.getBundle("configuration-builder");
		}
		if(configType==null || !rb.containsKey(configType))
			throw new ConfigTypeNotSupportedException();
		
		String configBuilderName=rb.getString(configType);
		
			try {
				Class builderClass=Class.forName(configBuilderName);
				IConfigBuilder configBuilder=(IConfigBuilder)builderClass.newInstance();
				return configBuilder;
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				throw new ConfigTypeNotSupportedException();
			}
	}//end of method
	
}
