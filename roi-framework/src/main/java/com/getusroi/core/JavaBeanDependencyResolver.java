package com.getusroi.core;

import java.util.Arrays;

/**
 * Simple java bean dependency Resolver using class.forName<BR>
 * The way classes are looked up will be different in java and OSGI world hence keeping it out.<br>
 * It will be looked into at the time of moving the code to OSGI
 * @author amit
 *
 */
public class JavaBeanDependencyResolver implements IBeanDependencyResolver{

	
	public Object getBeanInstance(String interfaceName,String nameofClass) throws BeanDependencyResolveException {
		Object obj=null;
		try {
			obj = Class.forName(nameofClass).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new BeanDependencyResolveException("Requested Bean with interface {"+interfaceName+"} and beanName {"+nameofClass+"}",e);
		}
		return obj;
	}
	
	public Object getBeanInstance(Class interfaceClass,String nameofClass) throws BeanDependencyResolveException{
		Object obj=null;
		try {
			obj = Class.forName(nameofClass).newInstance();
			Class[] interfaces=obj.getClass().getInterfaces();
				return obj;
			} catch (InstantiationException | IllegalAccessException|ClassNotFoundException e) {
			throw new BeanDependencyResolveException("Requested Bean with interface {"+interfaceClass+"} and beanName {"+nameofClass+"}",e);
		}
	}
}
