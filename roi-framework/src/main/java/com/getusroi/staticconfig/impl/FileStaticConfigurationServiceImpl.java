package com.getusroi.staticconfig.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.staticconfig.AddStaticConfigException;
import com.getusroi.staticconfig.IStaticConfigurationService;
import com.getusroi.staticconfig.StaticConfigDuplicateNameofFileException;
import com.getusroi.staticconfig.StaticConfigFetchException;
import com.getusroi.staticconfig.StaticConfigInitializationException;
import com.getusroi.staticconfig.util.LocalfileUtil;

/**
 * 
 * This is a temporary class,
 * 
 * @author bizruntime
 *
 */
public class FileStaticConfigurationServiceImpl implements IStaticConfigurationService {

	

	/**
	 * 1.) Get the base StaticconfigLocation<br>
	 * 2.) Based on the Directory Exists or not we will decide whether to create
	 * a new one or not 3.) IF already created we will make add the static
	 * ConfigName into that directory
	 * 
	 * @throws StaticConfigDuplicateNameofFileException
	 * @throws StaticConfigInitializationException
	 * @throws AccessProtectionException
	 * 
	 * @throws FileStaticConfigurationServiceException
	 */
	@Override
	public void addStaticConfiguration(ConfigurationContext ctx, String staticConfigName, String configContent)
			throws AddStaticConfigException, StaticConfigDuplicateNameofFileException,
			StaticConfigInitializationException, AccessProtectionException {
		String staticConfigRootDirectory = "";
		staticConfigRootDirectory = LocalfileUtil.initialize(staticConfigRootDirectory);
		FileStaticConfigHelper fileStaticConfigHelper = new FileStaticConfigHelper();
		Path baseDirectory = Paths.get(staticConfigRootDirectory + fileStaticConfigHelper
				.changeNamespaceintoPath(fileStaticConfigHelper.createNamespaceFromConfigurationContext(ctx)));
		if (Files.notExists(Paths.get(staticConfigRootDirectory))) {
			try {
				Files.createDirectories(baseDirectory);
			} catch (IOException e) {
				throw new AddStaticConfigException("Unable to create a new Directory");
			}
		} else {
			String staticConfigDirectory = staticConfigRootDirectory + "/" + fileStaticConfigHelper
					.changeNamespaceintoPath(fileStaticConfigHelper.createNamespaceFromConfigurationContext(ctx));
			if (Files.notExists(Paths.get(staticConfigDirectory))) {
				baseDirectory = Paths.get(staticConfigDirectory);
				try {
					Files.createDirectories(baseDirectory);
				} catch (IOException e) {
					throw new AddStaticConfigException(
							"Base directory is available, but unable to create child directory");
				}
			}
		}
		try {
			fileStaticConfigHelper.write2File(staticConfigName, configContent, baseDirectory);
		} catch (IOException e) {
			throw new AddStaticConfigException("Unable to right the file, it already exists");
		}
	}

	/**
	 * StaticConfiguration method to fetch from RequestContextPath, FilePath
	 * 
	 * @throws StaticConfigFetchException
	 * @throws StaticConfigInitializationException
	 * @throws AccessProtectionException
	 */
	@Override
	public String getStaticConfiguration(RequestContext reqCtx, String staticConfigName)
			throws StaticConfigFetchException, StaticConfigInitializationException, AccessProtectionException {
		String staticConfigRootDirectory = "";
		FileStaticConfigHelper fileStaticConfigHelper = new FileStaticConfigHelper();
		staticConfigRootDirectory = LocalfileUtil.initialize(staticConfigRootDirectory);
		String reqCtxPath = fileStaticConfigHelper
				.changeNamespaceintoPath(fileStaticConfigHelper.createNamespaceFromRequestContext(reqCtx));
		String filePath = staticConfigRootDirectory + reqCtxPath + "/" + staticConfigName;
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		String line;
		try {
			InputStream inputStream = new FileInputStream(filePath);
			br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			throw new StaticConfigFetchException("Unable to get the static configuration file..", e);
		}
		return sb.toString();
	}
	
	@Override
	public String updateStaticConfiguration(ConfigurationContext configurationContext, String staticConfigName,
			String configContent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String deleteStaticConfiguration(ConfigurationContext configurationContext, String staticConfigName) {
		// TODO Auto-generated method stub
		return null;
	}

}