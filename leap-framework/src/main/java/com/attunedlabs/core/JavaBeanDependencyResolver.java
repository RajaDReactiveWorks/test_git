package com.attunedlabs.core;

import java.lang.reflect.InvocationTargetException;

/**
 * Simple java bean dependency Resolver using class.forName<BR>
 * The way classes are looked up will be different in java and OSGI world hence
 * keeping it out.<br>
 * It will be looked into at the time of moving the code to OSGI
 * 
 * @author amit
 *
 */
public class JavaBeanDependencyResolver implements IBeanDependencyResolver {

	public Object getBeanInstance(String interfaceName, String nameofClass) throws BeanDependencyResolveException {
		Object obj = null;
		try {
			obj = Class.forName(nameofClass).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new BeanDependencyResolveException(
					"Requested Bean with interface {" + interfaceName + "} and beanName {" + nameofClass + "}", e);
		}
		return obj;
	}

	public Object getBeanInstance(Class interfaceClass, String nameofClass) throws BeanDependencyResolveException {
		Object obj = null;
		try {
			obj = Class.forName(nameofClass).newInstance();
			Class[] interfaces = obj.getClass().getInterfaces();
			return obj;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new BeanDependencyResolveException(
					"Requested Bean with interface {" + interfaceClass + "} and beanName {" + nameofClass + "}", e);
		}
	}

	/**
	 * pass one args contructor object
	 * 
	 * @param interfaceClass
	 * @param nameofClass
	 * @param constructorArgsClass
	 * @param parameter
	 * @return
	 * @throws BeanDependencyResolveException
	 */
	@Override
	public Object getBeanInstance(Class interfaceClass, String nameofClass, Class<?>[] constructorArgsClass,
			String[] parameters) throws BeanDependencyResolveException {
		Object obj = null;
		try {
			obj = Class.forName(nameofClass).getConstructor(constructorArgsClass).newInstance(parameters);
			Class[] interfaces = obj.getClass().getInterfaces();
			return obj;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new BeanDependencyResolveException(
					"Requested Bean with interface {" + interfaceClass + "} and beanName {" + nameofClass + "}", e);
		}
	}


}
