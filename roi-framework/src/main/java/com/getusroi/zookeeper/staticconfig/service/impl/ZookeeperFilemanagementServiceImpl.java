package com.getusroi.zookeeper.staticconfig.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.eventframework.abstractbean.util.ConnectionConfigurationException;
import com.getusroi.staticconfig.IStaticConfigurationService;
import com.getusroi.staticconfig.StaticConfigDeleteException;
import com.getusroi.staticconfig.StaticConfigDuplicateNameofFileException;
import com.getusroi.staticconfig.StaticConfigFetchException;
import com.getusroi.staticconfig.StaticConfigInitializationException;
import com.getusroi.staticconfig.StaticConfigUpdateException;
import com.getusroi.staticconfig.impl.AccessProtectionException;
import com.getusroi.staticconfig.util.LocalfileUtil;
import com.getusroi.zookeeper.staticconfig.InvalidFileTypeException;
import com.getusroi.zookeeper.staticconfig.session.ZookeeperSession;


public class ZookeeperFilemanagementServiceImpl implements IStaticConfigurationService {

	private static Logger logger = (Logger) LoggerFactory.getLogger(ZookeeperFilemanagementServiceImpl.class.getName());
	static final String FILE_EXTENSION_PROP = "fileextension.properties";
	static final String STATIC_CONFIG_PROPS = "StaticConfigProperties.properties";
	static final String STATIC_CONFIG_KEY = "staticConfigDirectory";
	static Enumeration<String> propetiesFileExtensionEnum;
	static Enumeration<String> propetiesStaticConfigEnum;
	static Properties propsFileextension = new Properties();
	static Properties propsStaticConfig = new Properties();
	static ArrayList<String> keys = new ArrayList<>();
	static ArrayList<String> values = new ArrayList<>();
	static String value = null;
	/**
	 * To load the properties once
	 */
	static {
		InputStream fileExtensioninputStream = ZookeeperFilemanagementServiceImpl.class.getClassLoader()
				.getResourceAsStream(FILE_EXTENSION_PROP);
		InputStream staticConfiginputStream = ZookeeperFilemanagementServiceImpl.class.getClassLoader()
				.getResourceAsStream(STATIC_CONFIG_PROPS);
		if (fileExtensioninputStream != null && staticConfiginputStream != null) {
			try {
				propsFileextension.load(fileExtensioninputStream);
				propsStaticConfig.load(staticConfiginputStream);
				propetiesFileExtensionEnum = (Enumeration<String>) propsFileextension.propertyNames();
				propetiesStaticConfigEnum = (Enumeration<String>) propsStaticConfig.propertyNames();
				while (propetiesFileExtensionEnum.hasMoreElements()) {
					String key = (String) propetiesFileExtensionEnum.nextElement();
					keys.add(key);
					value = propsFileextension.getProperty(key);
					values.add(value);
				}

			} catch (IOException e) {
				logger.error("Unable to load the file properties..", e);
			}
		}
	}// ..end of static propertiesLoader

	@Override
	public void addStaticConfiguration(ConfigurationContext configCtx, String staticConfigName, String configContent)
			throws StaticConfigDuplicateNameofFileException, InvalidFilePathException {
		logger.debug(".addStaticConfiguration..." + configCtx + " " + staticConfigName);
		String tenantId = configCtx.getTenantId();
		String siteId = configCtx.getSiteId();
		String fGroup = configCtx.getFeatureGroup();
		String fName = configCtx.getFeatureName();
		String implementationName=configCtx.getImplementationName();
		String vendor = configCtx.getVendorName();
		String version = configCtx.getVersion();
		String extension = staticConfigName.split("\\.")[1];

		if (keys.contains(extension)) {
			value = values.get(keys.indexOf(extension));
		}
		String pathToWatch = "/" + tenantId + "/" + siteId + "/" + fGroup + "/" + fName +"/" + implementationName + "/" + vendor + "/" + version
				+ "/" + value;
		ZookeeperSession.setPathToWatch(pathToWatch);
		try {
			addConfigFileinFileStore(configCtx, staticConfigName, configContent,
					ZookeeperSession.getZookeeperSession().getZk());
		} catch (ZookeeperPathExistsException | IOException | ZookeeperFileExistsException
				| InvalidFileTypeException | ConnectionConfigurationException e) {
			throw new StaticConfigDuplicateNameofFileException(
					"Failed to add file content to zookeeper config-manager..", e);
		}
	}// ..end of the method

	@Override
	public String getStaticConfiguration(RequestContext reqCtx, String staticConfigName)
			throws StaticConfigFetchException, StaticConfigInitializationException, AccessProtectionException {
		String znodeFileData;
		LocalfileUtil localfileUtil = new LocalfileUtil();
		String extension = staticConfigName.split("\\.")[1];
		logger.debug("StaticConfigFile extension: " + extension);
		try {
			String staticConfigBasedirectory = propsStaticConfig.getProperty(STATIC_CONFIG_KEY);
			String pathToWatch = "/" + reqCtx.getTenantId() + "/" + reqCtx.getSiteId() + "/" + reqCtx.getFeatureGroup()
					+ "/" + reqCtx.getFeatureName() + "/" + reqCtx.getImplementationName() + "/" + reqCtx.getVendor() + "/" + reqCtx.getVersion();

			if (keys.contains(extension)) {
				value = values.get(keys.indexOf(extension));
			}
			logger.debug("path constructed for znode search- before setting watch path..: " + staticConfigBasedirectory
					+ pathToWatch + "/" + staticConfigName);

			boolean existsInlocal = localfileUtil
					.checkFileExists(staticConfigBasedirectory + pathToWatch + "/" + staticConfigName);
			if (!existsInlocal) {
				logger.debug(
						"path constructed for znode search: " + pathToWatch + "/" + value + "/" + staticConfigName);
				ZookeeperSession.setPathToWatch(pathToWatch + "/" + value + "/" + staticConfigName);
				znodeFileData = getConfigFilefromFileStore(reqCtx, staticConfigName,
						ZookeeperSession.getZookeeperSession().getZk());
				String directoryTowrite = staticConfigBasedirectory + pathToWatch + "/" + staticConfigName;
				logger.debug(" file path To write content : "+ directoryTowrite);
				createDirctory(reqCtx, staticConfigBasedirectory);
				writeTolocalSystem(directoryTowrite, znodeFileData.getBytes());
			} else {
				byte[] byteData = Files.readAllBytes(
						Paths.get(staticConfigBasedirectory + "/" + pathToWatch + "/" + staticConfigName));
				znodeFileData = new String(byteData);
			}
		} catch (ZookeeperFileReadException | InvalidFileTypeException | ZookeeperFileNotFoudException
				| IOException  e) {
			throw new StaticConfigFetchException("Unable to get the file from zookeeper node Service..", e);
		}catch (Exception e) {
			throw new StaticConfigFetchException("Unable to get the file from zookeeper node Service..", e);
		}
		return znodeFileData;
	}// ..end of the method

	@Override
	public String updateStaticConfiguration(ConfigurationContext configCtx, String staticConfigName,
			String configContent) throws StaticConfigUpdateException {
		String resp = null;
		String pathToWatch = "/" + configCtx.getTenantId() + "/" + configCtx.getSiteId() + "/"
				+ configCtx.getFeatureGroup() + "/" + configCtx.getFeatureName()+ "/" + configCtx.getImplementationName() + "/" + configCtx.getVendorName() + "/"
				+ configCtx.getVersion();
		ZookeeperSession.setPathToWatch(pathToWatch);
		try {
			resp = updateConfigFileinFileStore(configCtx, staticConfigName, configContent, true,
					ZookeeperSession.getZookeeperSession().getZk());
		} catch (ZookeeperFileUpdateException | InvalidFileTypeException | IOException | ConnectionConfigurationException e) {
			throw new StaticConfigUpdateException("Unable to update the content of configuration - " + staticConfigName,
					e);
		}
		return resp;
	}// ..end of the method

	@Override
	public String deleteStaticConfiguration(ConfigurationContext configCtx, String staticConfigName)
			throws StaticConfigDeleteException {
		String pathToWatch = "/" + configCtx.getTenantId() + "/" + configCtx.getSiteId() + "/"
				+ configCtx.getFeatureGroup() + "/" + configCtx.getFeatureName() + "/" + configCtx.getImplementationName() + "/" + configCtx.getVendorName() + "/"
				+ configCtx.getVersion();
		ZookeeperSession.setPathToWatch(pathToWatch);
		String resp = null;
		try {
			resp = deleteConfigFilefromFileStore(staticConfigName, pathToWatch + "/" + value,
					ZookeeperSession.getZookeeperSession().getZk());
		} catch (ZookeeperFileDeleteException | ZookeeperFileNotFoudException | IOException | KeeperException
				| InterruptedException | ConnectionConfigurationException e) {
			throw new StaticConfigDeleteException(
					"Unable to delete the configuration file in zookeeper file-manager..Path mismatch", e);
		}
		return resp;
	}// ..end of the method

	/**
	 * Zookeeper specific logic to add data to Znode
	 * 
	 * @param reqCtx
	 * @param fileName
	 * @param localfilePath
	 * @param zookeeper
	 * @return
	 * @throws ZookeeperFilestoreException
	 * @throws ZookeeperPathExistsException
	 * @throws IOException
	 * @throws ZookeeperFileExistsException
	 * @throws InvalidFileTypeException
	 * @throws InvalidFilePathException
	 */
	public String addConfigFileinFileStore(ConfigurationContext configurationContext, String fileName,
			String fileContent, ZooKeeper zookeeper) throws ZookeeperPathExistsException, IOException,
			ZookeeperFileExistsException, InvalidFileTypeException, InvalidFilePathException {

		logger.debug(".addFileinFileStore() method of ZookeeperServiceImpl : "+fileName);
		logger.debug("ConfigurationContext : "+configurationContext.toString());
		Stat stat;
		int count = 0;
		List<String> listOfNodes = getConfigFilestorePathConfigContext(configurationContext, fileName);
		try {
			String temp = "/" + listOfNodes.get(0);
			String temp2;
			if (zookeeper.exists(temp, true) != null)
				count++;
			else {
				zookeeper.create(temp, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			for (int i = 1; i < listOfNodes.size(); i++) {
				temp2 = listOfNodes.get(i);
				temp = temp + "/" + temp2;
				if (zookeeper.exists(temp, true) != null) {
					count++;
				} else {
					zookeeper.create(temp, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
			}
			byte[] fileData = fileContent.getBytes();
			fileName = temp + "/" + fileName;

			if (zookeeper.exists(fileName, true) != null)
				throw new ZookeeperFileExistsException(fileName + " exists in zookeeper.");
			else {
				zookeeper.create(fileName, fileData, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (InterruptedException | KeeperException e1) {
			throw new InvalidFilePathException("Unable to add file to the specified path.." + listOfNodes, e1);
		}
		return fileName + " successfully added";

	}// ...end of method

	/**
	 * Zookeeper specific logic to get the data in string from znodes
	 * 
	 * @param reqCtx
	 * @param fileName
	 * @param zookeeper
	 * @return
	 * @throws ZookeeperFileReadException
	 * @throws InvalidFileTypeException
	 * @throws ZookeeperFileNotFoudException
	 */
	private String getConfigFilefromFileStore(RequestContext reqCtx, String fileName, ZooKeeper zookeeper)
			throws ZookeeperFileReadException, InvalidFileTypeException, ZookeeperFileNotFoudException {
		logger.debug(".getFilefromFileStore() method of ZookeeperServiceImpl");

		List<String> listOfNodes = getConfigFilestorePath(reqCtx, fileName);
		byte[] data = null;
		Stat stat = null;
		String zpath = "";
		String fileExtension;
		String znodeFileName;

		try {
			for (String nodeName : listOfNodes) {
				zpath += "/" + nodeName;
			}
			znodeFileName = fileName;
			zpath = zpath + "/" + znodeFileName;
			logger.debug("Getting data from -: " + zpath);
			stat = zookeeper.exists(zpath, true);
			if (stat != null){
				data = zookeeper.getData(zpath, null, stat);
				
				logger.debug("Getting data -: " + data);
				if(data==null)
				return null;
					return new String(data);
			}else{
				throw new ZookeeperFileNotFoudException(fileName + " does not exists in the path.");
		}} catch (InterruptedException | KeeperException | NullPointerException  e1) {
			throw new ZookeeperFileReadException("Unable to read file from the specified path.." + zpath, e1);
		}catch (Exception e) {
			throw new ZookeeperFileReadException("Unable to read file from the specified path.." + zpath, e);
		}
		
	}// ...end of method

	/**
	 * zookeeper specific logic to update the data with in the znode
	 * 
	 * @param configCtx
	 * @param fileName
	 * @param configContent
	 * @param isUpdatedVersion
	 * @param zookeeper
	 * @return
	 * @throws ZookeeperFilestoreException
	 * @throws ZookeeperFileUpdateException
	 * @throws InvalidFileTypeException
	 */
	private String updateConfigFileinFileStore(ConfigurationContext configCtx, String fileName, String configContent,
			boolean isUpdatedVersion, ZooKeeper zookeeper)
			throws ZookeeperFileUpdateException, InvalidFileTypeException {

		logger.debug(".updateConfigFileinFileStore() method of ZookeeperServiceImpl");
		String fpath = "";
		byte[] data = null;
		Stat stat = null;
		List<String> listOfNodes = getConfigFilestorePathConfigContext(configCtx, fileName);

		try {
			// fileName = fileName.substring(0, fileName.indexOf('.'));
			for (String nodeName : listOfNodes) {
				fpath += "/" + nodeName;
			}

			fpath = fpath + "/" + fileName;
			data = configContent.getBytes();
			stat = zookeeper.exists(fpath, true);
			if (stat != null) {
				if (!isUpdatedVersion) {
					zookeeper.delete(fpath, stat.getVersion());
					zookeeper.create(fpath, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					stat = zookeeper.exists(fpath, true);
				} else {
					zookeeper.setData(fpath, data, stat.getVersion());
					stat = zookeeper.exists(fpath, true);
				}
			} else {
				zookeeper.create(fpath, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				stat = zookeeper.exists(fpath, true);
			}

		} catch (InterruptedException | KeeperException e1) {
			throw new ZookeeperFileUpdateException("Unable to update the file." + fileName, e1);
		}
		return fileName + " updated successfully, version :" + stat.getVersion();
	}// ...end of method

	/**
	 * zookeeper specific logic to delete data from the znode
	 * 
	 * @param fileName
	 * @param path
	 * @param zookeeper
	 * @return
	 * @throws ZookeeperFileDeleteException
	 * @throws ZookeeperFileNotFoudException
	 * @throws InterruptedException
	 * @throws KeeperException
	 */
	private String deleteConfigFilefromFileStore(String fileName, String path, ZooKeeper zookeeper)
			throws ZookeeperFileDeleteException, ZookeeperFileNotFoudException, KeeperException, InterruptedException {

		logger.debug(".deleteConfigFilefromFileStore() method of ZookeeperServiceImpl");
		Stat stat = null;
		path = path + "/" + fileName;
		stat = zookeeper.exists(path, null);
		if (stat != null)
			zookeeper.delete(path, stat.getVersion());
		else
			throw new ZookeeperFileNotFoudException(fileName + " does not exists in the path.");

		return fileName + " deleted succesfully";
	}// ...end of method

	/**
	 * Construction of path parameters from the RequestContext
	 * 
	 * @param reqCtx
	 * @param fileName
	 * @return
	 * @throws InvalidFileTypeException
	 */
	private List<String> getConfigFilestorePath(RequestContext reqCtx, String fileName)
			throws InvalidFileTypeException {

		logger.debug(".getConfigFilestorePath() method of ZookeeperServiceImpl");
		String fileStoreKey;
		String partialPath;
		String tenantId = reqCtx.getTenantId();
		String siteId = reqCtx.getSiteId();
		String fGroup = reqCtx.getFeatureGroup();
		String fName = reqCtx.getFeatureName();
		String implementationName=reqCtx.getImplementationName();
		String vendorName = reqCtx.getVendor();
		String version = reqCtx.getVersion();

		String fileExtension = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
		ArrayList<String> listOfPath = new ArrayList<>();

		if (!tenantId.equals(null) && !siteId.equals(null) && !fGroup.equals(null) && !fName.equals(null)) {
			partialPath = "/" + tenantId + "/" + siteId + "/" + fGroup + "/" + fName + "/" + implementationName+ "/" + vendorName + "/" + version;
			if (!fileExtension.equals(null)) {
				fileStoreKey = checkFilevalidity(fileExtension);
				listOfPath.add(tenantId);
				listOfPath.add(siteId);
				listOfPath.add(fGroup);
				listOfPath.add(fName);
				listOfPath.add(implementationName);
				listOfPath.add(vendorName);
				listOfPath.add(version);
				listOfPath.add(fileStoreKey);
				logger.debug("Path to be created: " + partialPath + "/" + fileStoreKey);
			}
			return listOfPath;
		} else {
			throw new InvalidFileTypeException("Unable to create path for the file extention specified..");
		}
	}// ...end of method

	/**
	 * logic to construct path parameters from the ConfigurationContext object
	 * 
	 * @param configurationContext
	 * @param fileName
	 * @return
	 * @throws InvalidFileTypeException
	 */
	private List<String> getConfigFilestorePathConfigContext(ConfigurationContext configurationContext, String fileName)
			throws InvalidFileTypeException {

		logger.debug(".getConfigFilestorePath() method of ZookeeperServiceImpl");
		String fileStoreKey;
		String partialPath;
		String tenantId = configurationContext.getTenantId();
		String siteId = configurationContext.getSiteId();
		String fGroup = configurationContext.getFeatureGroup();
		String fName = configurationContext.getFeatureName();
		String implementationName=configurationContext.getImplementationName();
		String vendorName = configurationContext.getVendorName();
		String version = configurationContext.getVersion();

		String fileExtension = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
		ArrayList<String> listOfPath = new ArrayList<>();

		if (!tenantId.equals(null) && !siteId.equals(null) && !fGroup.equals(null) && !fName.equals(null)) {
			partialPath = "/" + tenantId + "/" + siteId + "/" + fGroup + "/" + fName + "/" + implementationName  + "/" + vendorName + "/" + version;
			if (!fileExtension.equals(null)) {
				fileStoreKey = checkFilevalidity(fileExtension);
				listOfPath.add(tenantId);
				listOfPath.add(siteId);
				listOfPath.add(fGroup);
				listOfPath.add(fName);
				listOfPath.add(implementationName);
				listOfPath.add(vendorName);
				listOfPath.add(version);
				listOfPath.add(fileStoreKey);
				logger.debug("Path to be created: " + partialPath + "/" + fileStoreKey);
			}
			return listOfPath;
		} else {
			throw new InvalidFileTypeException("Unable to create path for the file extention specified..");
		}
	}// ...end of method

	/**
	 * check the valid extension , for creating path of FileStore
	 * 
	 * @param fileExtension
	 * @return
	 * @throws InvalidFileTypeException
	 */
	public String checkFilevalidity(String fileExtension) throws InvalidFileTypeException {

		logger.debug(".checkFilevalidity() method of ZookeeperHelper");

		if (keys.contains(fileExtension)) {
			value = values.get(keys.indexOf(fileExtension));
			return value;
		} else {
			throw new InvalidFileTypeException("Specified an Invalid file-type to store..");
		}
	}// ..end of the method

	/**
	 * Write to local system called when needed
	 * 
	 * @param directoryTowrite
	 * @param byteData
	 * @throws IOException
	 */
	private void writeTolocalSystem(String directoryTowrite, byte[] byteData) throws IOException {

		logger.debug(" .writeTolocalSystem  method of zookeeperImpl  ");
		ByteBuffer byteBuffer = ByteBuffer.wrap(byteData);
		Set<StandardOpenOption> options = new HashSet<StandardOpenOption>();
		options.add(StandardOpenOption.CREATE);
		options.add(StandardOpenOption.APPEND);
		

		Path path = Paths.get(directoryTowrite);
		Files.createFile(path);
		FileChannel fileChannel = FileChannel.open(path, options);
		fileChannel.write(byteBuffer);
		fileChannel.close();
		logger.debug(" .writeTolocalSystem  is done ");

	}// ..end of the method

	/**
	 * To make folder tenant,site,featureGroup,feature,vendor,version
	 * @param requestContext
	 * @param path
	 */
	private void createDirctory(RequestContext requestContext,String path){
		String tenantFolder=path+"/"+requestContext.getTenantId();
		File file=new File(tenantFolder);
		creatFolder(file);
		String siteFolder=tenantFolder+"/"+requestContext.getSiteId();
		file=new File(siteFolder);
		creatFolder(file);
		String featureGroupFolder=siteFolder+"/"+requestContext.getFeatureGroup();
		file=new File(featureGroupFolder);
		creatFolder(file);
		String featureFolder=featureGroupFolder+"/"+requestContext.getFeatureName();
		file=new File(featureFolder);
		creatFolder(file);
		String implementationFolder=featureFolder+"/"+requestContext.getImplementationName();
		file=new File(implementationFolder);
		creatFolder(file);
		String vendorFolder=implementationFolder+"/"+requestContext.getVendor();
		file=new File(vendorFolder);
		creatFolder(file);
		String versionFolder=vendorFolder+"/"+requestContext.getVersion();
		file=new File(versionFolder);
		creatFolder(file);
		
		
	}
	/**
	 * Ti create Folder
	 * @param file
	 */
 private void creatFolder(File file) {
	 if (!file.isDirectory()) {
			file.mkdir();
		}
}
	
		

	
}