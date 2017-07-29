package com.getusroi.staticconfig.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.staticconfig.impl.FileStaticConfigHelper;
import com.getusroi.config.RequestContext;
import com.getusroi.staticconfig.StaticConfigInitializationException;
import com.getusroi.staticconfig.impl.AccessProtectionException;

public class LocalfileUtil {

	static final String BASE_DIRECTORY = "staticConfigDirectory";
	static final String STATIC_CONFIG_FILE = "StaticConfigProperties.properties";

	static final String WIN_PATH_PATTERN = ":/";
	static final String UNIX_PATH_PATTERN = "/";
	static final String WIN_PATH_PATTERN_PART = ":\\";

	static final String OPERATING_SYSTEM = System.getProperty("os.name")
			.toLowerCase();
	private static Logger logger = LoggerFactory.getLogger(LocalfileUtil.class
			.getName());

	public boolean checkFileExists(String filepath) {
		return Files.exists(Paths.get(filepath));
	}

	/**
	 * Load properties file "StaticConfigProperties." from the properties file
	 * get the base FilePath for managing the staticConfig
	 * 
	 * @throws AccessProtectionException
	 * 
	 * @throws FileStaticConfigurationServiceException
	 */
	public static String initialize(String staticConfigRootDirectory)
			throws StaticConfigInitializationException,
			AccessProtectionException {
		staticConfigRootDirectory = loadStaticConfigProperty();
		if ((staticConfigRootDirectory.contains(WIN_PATH_PATTERN)||staticConfigRootDirectory.contains(WIN_PATH_PATTERN_PART))&& isWindows()
				|| staticConfigRootDirectory.contains(UNIX_PATH_PATTERN)
				&& isUnix()) {
             logger.debug("staticConfigRootDirectory in initialize() "+staticConfigRootDirectory);
			// return isWriteAccessEnabled(staticConfigRootDirectory);
			return staticConfigRootDirectory;
		} else {
			throw new StaticConfigInitializationException("Directory path "
					+ staticConfigRootDirectory + " specified doesn't match ..");
		}
	}// ..end of the method

	/**
	 * Method to load the directory path from the StaticConfigPropertiy file.
	 * 
	 * @throws FileStaticConfigurationServiceException
	 */
	public static String loadStaticConfigProperty()
			throws StaticConfigInitializationException {
		logger.debug("loadStaticConfigProperty method of LocalfileUtil");
		Properties properties = new Properties();
		InputStream input = null;
		try {
			// load the properties File
			input = FileHelper.class.getClassLoader().getResourceAsStream(
					STATIC_CONFIG_FILE);
		} catch (Exception e) {
			throw new StaticConfigInitializationException(
					"Property File not Found ");
		}
		try {
			properties.load(input);
			logger.debug("base directry in loadStaticConfigProperty --"+properties.getProperty(BASE_DIRECTORY));
			return properties.getProperty(BASE_DIRECTORY);
		} catch (IOException e) {
			throw new StaticConfigInitializationException(
					"IOException Occured : " + e);
		}
	}

	/**
	 * Below are static methods, which will return true , if OS matches
	 * 
	 * @return
	 */
	private static boolean isWindows() {
		return OPERATING_SYSTEM.indexOf("win") >= 0;
	}

	private static boolean isMac() {
		return OPERATING_SYSTEM.indexOf("mac") >= 0;
	}

	private static boolean isUnix() {
		return OPERATING_SYSTEM.indexOf("nix") >= 0
				|| OPERATING_SYSTEM.indexOf("nux") >= 0
				|| OPERATING_SYSTEM.indexOf("aix") > 0;
	}

	private static boolean isSolaris() {
		return OPERATING_SYSTEM.indexOf("sunos") >= 0;
	}// ..end of methods

	/**
	 * Validator to check the write permission for the static-config path
	 * 
	 * @param path
	 * @return
	 * @throws AccessProtectionException
	 */
	// #TODO have to use with testing in other machines
	private String isWriteAccessEnabled(String path)
			throws AccessProtectionException {

		File f = new File(path);
		if (f.canWrite()) {
			return path;
		} else {
			throw new AccessProtectionException("Write access in the path - "
					+ path
					+ " - is not granted..Change permission and try again");
		}
	}// ..end of the method

	/**
	 * method to get the file Path of the staticConfigFiles
	 * 
	 * @throws AccessProtectionException
	 * @throws StaticConfigInitializationException
	 * 
	 */
	public String getStaticFilePath(RequestContext reqCtx, String fileName)
			throws StaticConfigInitializationException,
			AccessProtectionException {
		logger.debug("RequestContext in getStaticFilePath() : "+reqCtx);
		String staticConfigRootDirectory = "";
		FileStaticConfigHelper fileStaticConfigHelper = new FileStaticConfigHelper();
		staticConfigRootDirectory = LocalfileUtil
				.initialize(staticConfigRootDirectory);
		logger.debug("LocalfileUtil.initialize in getStaticFilePath() : "+staticConfigRootDirectory);
		String reqCtxPath = fileStaticConfigHelper
				.changeNamespaceintoPath(fileStaticConfigHelper
						.createNamespaceFromRequestContext(reqCtx));
		logger.debug("reqCtxPath in getStaticFilePath() : "+reqCtxPath+" filename: "+fileName);
		String filePath = staticConfigRootDirectory + reqCtxPath + "/"
				+ fileName;
		logger.debug("filepath : " + filePath);
		return filePath;

	}// ..end of the method
}
