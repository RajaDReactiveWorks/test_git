package com.attunedlabs.integrationfwk.groovyactivity.config;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.codehaus.groovy.ant.Groovyc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.integrationfwk.activities.bean.ActivityConstant;
import com.attunedlabs.integrationfwk.activities.bean.GroovyScriptActivityException;

/**
 * @author Reactiveworks
 *
 */
public class GroovycScriptClassGenerator {
	private static Groovyc groovyc;
	private static Project project;
	private static GroovycScriptClassGenerator classGenerator;
	private final static Logger logger = LoggerFactory.getLogger(GroovycScriptClassGenerator.class.getName());
	static {
		classGenerator = new GroovycScriptClassGenerator();
		groovyc = new Groovyc();
		project = new Project();
		groovyc.setProject(project);
	}

	/**
	 * 
	 */
	private GroovycScriptClassGenerator() {

	}

	/**
	 * getInstance() will always return only one instance of the
	 * GroovycScriptClassGenerator.
	 * 
	 * @return GroovyScriptCache
	 */
	public static GroovycScriptClassGenerator getInstance() {
		return classGenerator;
	}// ..end of the method

	/**
	 * @param script
	 * @throws GroovyScriptActivityException
	 */
	public void generateClassFiles(String script, String scriptName, String sourceFolder, String destinationFolder)
			throws GroovyScriptActivityException {
		logger.debug("generating ClassFile for " + scriptName + " in " + destinationFolder);
		logger.debug("source folder :" + sourceFolder);
		// File inputFile = loadGroovyCompiler(script, scriptName, sourceFolder,
		// destinationFolder);
		loadGroovyCompiler(script, scriptName, sourceFolder, destinationFolder);
		try {
			groovyc.execute();
		} catch (BuildException ex) {
			throw new GroovyScriptActivityException("Problem compiling", ex);
			/*
			 * } finally { try { //deleteFile(inputFile); } catch (Exception e) { // do
			 * nothing... }
			 */ }
	}// ..end of the method

	/**
	 * @param script
	 * @param scriptName
	 * @param destinationFolder
	 * @param sourceFolder
	 * @return
	 * @throws GroovyScriptActivityException
	 */
	private static File loadGroovyCompiler(String script, String scriptName, String sourceFolder,
			String destinationFolder) throws GroovyScriptActivityException {
		logger.debug("loadGroovyCompiler...");
		Path sourcePath = new Path(project);
		File destination = new File(destinationFolder);
		if (!destination.exists()) {
			logger.debug(destinationFolder + " not exist");
			destination.mkdir();
			logger.debug(destinationFolder + " is created");
		}
		groovyc.setDestdir(destination);
		deleteSrcAndDesGroovy(destination, new File(sourceFolder));
		File inputFile = getFileFromLocalSystem(sourceFolder, script, scriptName);
		File input = new File(sourceFolder);
		sourcePath.setLocation(input);
		groovyc.setSrcdir(sourcePath);
		return inputFile;
	}

	/**
	 * @param destination
	 * @param source
	 */
	private static void deleteSrcAndDesGroovy(File destination, File source) {
		destination.delete();
		source.delete();
	}// ..end of the method

	/**
	 * @param path
	 * @param script
	 * @return
	 * @throws GroovyScriptActivityException
	 */
	private static File getFileFromLocalSystem(String path, String script, String scriptName)
			throws GroovyScriptActivityException {
		File groovyFile = new File(path + ActivityConstant.BACKWORD_SLASH + generateScriptName(scriptName));
		try {
			FileUtils.writeStringToFile(groovyFile, script);
		} catch (Exception e) {
			throw new GroovyScriptActivityException("Error while executing the Script." + e.getMessage());
		}
		return groovyFile;
	}// ..end of the method

	/**
	 * @return
	 */
	protected synchronized static String generateScriptName(String scriptName) {
		return scriptName + ActivityConstant.GROOVY_FILE_EXTENTION;
	}// ..end of the method

}
