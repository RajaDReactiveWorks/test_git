package com.attunedlabs.core;

/**
 * This util class is reponsible for loading and giving us the bean.<BR>
 * The Bean loading can vary based on the enviorment. 1.) Normal java will be
 * class.forname. 2.) normal OSGI through the OSGI Registry 3.) OSGI Blueprint
 * through BlueprintRegistry 4.) Spring through the Spring Registry
 * 
 * @author amit
 *
 */
public interface IBeanDependencyResolver {
	public Object getBeanInstance(String interfaceName, String nameofClass) throws BeanDependencyResolveException;

	public Object getBeanInstance(Class interfaceClass, String nameofClass) throws BeanDependencyResolveException;

	public Object getBeanInstance(Class interfaceClass, String nameofClass, Class<?> constructorArgsClass[],
			String parameters[]) throws BeanDependencyResolveException;

}
