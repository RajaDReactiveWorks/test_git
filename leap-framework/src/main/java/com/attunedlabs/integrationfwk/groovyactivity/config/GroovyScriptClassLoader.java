package com.attunedlabs.integrationfwk.groovyactivity.config;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.codehaus.groovy.runtime.InvokerHelper;

import com.attunedlabs.integrationfwk.activities.bean.GroovyScriptActivityException;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;

import groovy.lang.Binding;
import groovy.lang.Script;

public class GroovyScriptClassLoader {
	private static GroovyScriptClassLoader classLoader;
	static {
		classLoader = new GroovyScriptClassLoader();
	}

	/**
	* 
	*/
	private GroovyScriptClassLoader() {

	}

	/**
	 * getInstance() will always return only one instance of the
	 * GroovycScriptClassGenerator.
	 * 
	 * @return GroovyScriptCache
	 */
	public static GroovyScriptClassLoader getInstance() {
		return classLoader;
	}// ..end of the method

	/**
	 * @param path
	 * @param exchange
	 * @param pipeactivity
	 * @return
	 * @throws Exception
	 */
	public Script executeGroovyScript(String path, String className, PipeActivity pipeactivity)
			throws GroovyScriptActivityException {
		Class<?> scriptClass = loadClassFile(path, className);
		if (scriptClass == null)
			return null;
		Script createScript = InvokerHelper.createScript(scriptClass, new Binding());
		return createScript;
	}// ..end of the method

	/**
	 * @param path
	 * @param className
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	private static Class<?> loadClassFile(String path, String className) throws GroovyScriptActivityException {
		try {
			File file = new File(path);
			if (!file.exists())
				return null;
			URL url = file.toURI().toURL();
			URL[] urls = new URL[] { url };
			ClassLoader cl = new URLClassLoader(urls);
			return cl.loadClass(className);
		} catch (Exception ex) {
			throw new GroovyScriptActivityException(ex.getMessage());
		}
	}// ..end of the method

}
