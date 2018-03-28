package com.attunedlabs.core;

public class BeanDependencyResolverFactory {
	
	/*
	 * Till we dont adapt it to OSGI Making it default 
	 */
	public static IBeanDependencyResolver getBeanDependencyResolver(){
		return new JavaBeanDependencyResolver();
	}
}
