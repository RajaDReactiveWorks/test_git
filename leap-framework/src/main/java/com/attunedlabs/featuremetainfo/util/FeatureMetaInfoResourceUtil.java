package com.attunedlabs.featuremetainfo.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.schema.Table;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.dao.LeapConstants;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.datacontext.config.DataContextConfigurationException;
import com.attunedlabs.datacontext.config.DataContextConfigurationUnit;
import com.attunedlabs.datacontext.config.DataContextParserException;
import com.attunedlabs.datacontext.config.IDataContextConfigurationService;
import com.attunedlabs.datacontext.config.impl.DataContextConfigXMLParser;
import com.attunedlabs.datacontext.config.impl.DataContextConfigurationService;
import com.attunedlabs.datacontext.jaxb.DataContext;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.dynastore.config.DynaStoreConfigParserException;
import com.attunedlabs.dynastore.config.DynaStoreConfigurationException;
import com.attunedlabs.dynastore.config.IDynaStoreConfigurationService;
import com.attunedlabs.dynastore.config.impl.DynaStoreConfigXmlParser;
import com.attunedlabs.dynastore.config.impl.DynaStoreConfigurationService;
import com.attunedlabs.dynastore.config.jaxb.DynastoreConfiguration;
import com.attunedlabs.dynastore.config.jaxb.DynastoreConfigurations;
import com.attunedlabs.eventframework.config.EventFrameworkConfigParserException;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.DispatchChanel;
import com.attunedlabs.eventframework.jaxb.DispatchChanels;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventDispatcher;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.EventSubscription;
import com.attunedlabs.eventframework.jaxb.Events;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SystemEvent;
import com.attunedlabs.eventframework.jaxb.SystemEvents;
import com.attunedlabs.feature.config.FeatureConfigParserException;
import com.attunedlabs.feature.config.FeatureConfigRequestContext;
import com.attunedlabs.feature.config.FeatureConfigRequestException;
import com.attunedlabs.feature.config.FeatureConfigurationException;
import com.attunedlabs.feature.config.IFeatureConfigurationService;
import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;
import com.attunedlabs.feature.config.impl.FeatureConfigurationService;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.featuremaster.FeatureMasterServiceException;
import com.attunedlabs.featuremaster.IFeatureMasterService;
import com.attunedlabs.featuremaster.impl.FeatureMasterService;
import com.attunedlabs.featuremetainfo.FeatureMetaInfoConfigParserException;
import com.attunedlabs.featuremetainfo.impl.FeatureMetaInfoConfigXmlParser;
import com.attunedlabs.featuremetainfo.jaxb.ConfigFile;
import com.attunedlabs.featuremetainfo.jaxb.DBConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.DataContexts;
import com.attunedlabs.featuremetainfo.jaxb.Database;
import com.attunedlabs.featuremetainfo.jaxb.DynaStoreConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.DynaStoreConfigurations;
import com.attunedlabs.featuremetainfo.jaxb.EventResource;
import com.attunedlabs.featuremetainfo.jaxb.EventResources;
import com.attunedlabs.featuremetainfo.jaxb.Feature;
import com.attunedlabs.featuremetainfo.jaxb.FeatureDataContexts;
import com.attunedlabs.featuremetainfo.jaxb.FeatureGroup;
import com.attunedlabs.featuremetainfo.jaxb.FeatureImplementation;
import com.attunedlabs.featuremetainfo.jaxb.FeatureImplementations;
import com.attunedlabs.featuremetainfo.jaxb.FeatureMetainfo;
import com.attunedlabs.featuremetainfo.jaxb.IntegrationPipeLineConfigurations;
import com.attunedlabs.featuremetainfo.jaxb.PermaStoreConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.PipeConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.PolicyConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.PolicyConfigurations;
import com.attunedlabs.featuremetainfo.jaxb.ScheduledJobConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.ScheduledJobConfigurations;
import com.attunedlabs.featuremetainfo.jaxb.StaticFileConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.UIConfig;
import com.attunedlabs.integrationfwk.config.IIntegrationPipeLineConfigurationService;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigException;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigParserException;
import com.attunedlabs.integrationfwk.config.impl.IntegrationPipelineConfigXmlParser;
import com.attunedlabs.integrationfwk.config.impl.IntegrationPipelineConfigurationService;
import com.attunedlabs.integrationfwk.config.jaxb.IntegrationPipe;
import com.attunedlabs.integrationfwk.config.jaxb.IntegrationPipes;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.permastore.config.IPermaStoreConfigurationService;
import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationException;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigXMLParser;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigurationService;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfigurations;
import com.attunedlabs.policy.config.IPolicyConfigurationService;
import com.attunedlabs.policy.config.PolicyConfigXMLParser;
import com.attunedlabs.policy.config.PolicyConfigXMLParserException;
import com.attunedlabs.policy.config.PolicyConfigurationException;
import com.attunedlabs.policy.config.PolicyRequestContext;
import com.attunedlabs.policy.config.PolicyRequestException;
import com.attunedlabs.policy.config.impl.PolicyConfigurationService;
import com.attunedlabs.policy.jaxb.Policies;
import com.attunedlabs.policy.jaxb.Policy;
import com.attunedlabs.scheduler.ScheduledJobConfigParserException;
import com.attunedlabs.scheduler.ScheduledJobConfigurationException;
import com.attunedlabs.scheduler.config.IScheduledJobConfigurationService;
import com.attunedlabs.scheduler.config.ScheduledJobConfigRequestException;
import com.attunedlabs.scheduler.config.impl.ScheduledJobConfigXMLParser;
import com.attunedlabs.scheduler.config.impl.ScheduledJobConfigurationService;
import com.attunedlabs.staticconfig.AddStaticConfigException;
import com.attunedlabs.staticconfig.IStaticConfigurationService;
import com.attunedlabs.staticconfig.StaticConfigDuplicateNameofFileException;
import com.attunedlabs.staticconfig.StaticConfigInitializationException;
import com.attunedlabs.staticconfig.factory.StaticConfigurationFactory;
import com.attunedlabs.staticconfig.impl.AccessProtectionException;
import com.attunedlabs.staticconfig.util.LocalfileUtil;
import com.attunedlabs.zookeeper.staticconfig.service.impl.InvalidFilePathException;
import com.attunedlabs.zookeeper.staticconfig.service.impl.ZookeeperFilemanagementServiceImpl;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import oracle.jdbc.pool.OracleDataSource;

/**
 * This class is responsible for loading all feature related configuration in
 * non-osgi environment
 * 
 * @author bizruntime
 * 
 */
@SuppressWarnings({ "rawtypes", "resource", "unused" })
public class FeatureMetaInfoResourceUtil {
	final static Logger logger = LoggerFactory.getLogger(FeatureMetaInfoResourceUtil.class);
	private static final String JAVA_CLASSPATH_KEY = "java.class.path";
	private static final String PATH_SEPERATOR_KEY = "path.separator";
	static final String MYSQL_TYPE = "mysql";
	static final String ORACLE_TYPE = "oracle";
	static final String MSSQL_TYPE = "mssql";
	static final String POSTGRESSQL_TYPE = "postgressql";
	static final String POSTGRES_TYPE = "postgres";
	static final String DDLUITLS_XMLDOCTYPE = "<!DOCTYPE database SYSTEM \"http://db.apache.org/torque/dtd/database.dtd\"> ";

	static String path = "";
	private static final String STATICCONFIG_PROPS = "globalAppDeploymentConfig.properties";
	private static final String STATICCONFIG_DIC_KEY = "staticConfigDirectory";
	private static final String RESOURCE_FILE_PATH = "src/main/resources";

	private static Properties propsStaticConfig = new Properties();

	private String tenant;
	private String siteId;
	private String featureGroup;
	private String featureName;
	private String implementationName;
	private String vendor;
	private String version;

	public FeatureMetaInfoResourceUtil() {
		super();
	}

	public FeatureMetaInfoResourceUtil(String tenant, String siteId, String featureGroup, String featureName,
			String implementationName, String vendor, String version) {
		this.tenant = tenant;
		this.siteId = siteId;
		this.featureGroup = featureGroup;
		this.featureName = featureName;
		this.implementationName = implementationName;
		this.vendor = vendor;
		this.version = version;
	}

	static {
		InputStream staticConfiginputStream = FeatureMetaInfoResourceUtil.class.getClassLoader()
				.getResourceAsStream(STATICCONFIG_PROPS);
		try {
			propsStaticConfig.load(staticConfiginputStream);
			path = propsStaticConfig.getProperty(STATICCONFIG_DIC_KEY);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to get all elements of java.class.path get a
	 * Collection of resources Pattern pattern =
	 * Pattern.compile("featureMetaInfo.xml"); gets all resources
	 * 
	 * @param pattern
	 *            the pattern to match
	 * @return the resources if they are found
	 * @throws FeatureDeploymentServiceException
	 * @throws IOException
	 */
	public Collection<String> getClassPathResources(Pattern pattern) throws FeatureDeploymentServiceException {
		final ArrayList<String> resource = new ArrayList<String>();
		final String javaclassPath = System.getProperty(JAVA_CLASSPATH_KEY, ".");
		final String[] javaclassPathElements = javaclassPath.split(System.getProperty(PATH_SEPERATOR_KEY));
		// erasePreviousLoadedConfiguration();
		for (final String element : javaclassPathElements) {
			try {
				resource.addAll(getResources(element, pattern));
			} catch (IOException e) {
				throw new FeatureDeploymentServiceException("Unable to get ClassPathResources: ", e);
			}
		}
		return resource;
	}

	/**
	 * This method is to search pattern in directory or jar available in
	 * classpath
	 * 
	 * @param element
	 *            : Jar/Directory to be searched
	 * @param pattern
	 *            : pattern to be matched
	 * @return Collection object
	 * @throws FeatureDeploymentServiceException
	 * @throws IOException
	 */
	private Collection<String> getResources(String element, Pattern pattern)
			throws FeatureDeploymentServiceException, IOException {
		final ArrayList<String> resourcevalue = new ArrayList<String>();
		final File file = new File(element);
		if (file.isDirectory()) {
			resourcevalue.addAll(getResourcesFromDirectory(file, pattern));
		} else {
			resourcevalue.addAll(getResourcesFromJar(file, pattern, element));
		}
		return resourcevalue;
	}

	/**
	 * This method is to searched the matching pattern in jar
	 * 
	 * @param file
	 *            : name of the file to be searched
	 * @param pattern
	 *            : pattern to be searched
	 * @param element
	 *            : jar to be searched
	 * @return Colection Object
	 * @throws FeatureDeploymentServiceException
	 * @throws IOException
	 */
	private Collection<String> getResourcesFromJar(File file, Pattern pattern, String element)
			throws FeatureDeploymentServiceException, IOException {
		final ArrayList<String> resourcevalue = new ArrayList<String>();
		ZipFile zipfile;
		try {
			zipfile = new ZipFile(file);
		} catch (ZipException e) {
			throw new FeatureDeploymentServiceException(
					"Unable to opens a ZIP file for reading given the specified File object.", e);
		} catch (IOException e) {
			throw new FeatureDeploymentServiceException(
					"Unable to read from given the File object. = " + file.getName(), e);
		}
		final Enumeration enumerator = zipfile.entries();
		while (enumerator.hasMoreElements()) {
			final ZipEntry zipentry = (ZipEntry) enumerator.nextElement();
			final String fileName = zipentry.getName();
			final boolean acceptedFile = pattern.matcher(fileName).matches();
			if (acceptedFile) {
				List<FeatureGroup> featureGroupList = parseAndGetFeatureMetaInfo(fileName, element);
				loadAllResourceFromFeatureLevel(featureGroupList);
				resourcevalue.add(fileName);
			}
		}
		try {
			zipfile.close();
		} catch (IOException e1) {
			throw new FeatureDeploymentServiceException("Unable to close a ZIP file object for specified File object.",
					e1);
		}
		return resourcevalue;
	}

	/**
	 * This method is to searched the matching pattern in directory
	 * 
	 * @param file
	 *            : name of the file to be searched
	 * @param pattern
	 *            : pattern to be searched
	 * @return Colection Object
	 * @throws FeatureDeploymentServiceException
	 */
	private Collection<String> getResourcesFromDirectory(File directory, Pattern pattern)
			throws FeatureDeploymentServiceException {
		final ArrayList<String> resourcevalue = new ArrayList<String>();
		final File[] fileList = directory.listFiles();
		for (File file : fileList) {
			if (file.isDirectory()) {
				resourcevalue.addAll(getResourcesFromDirectory(file, pattern));
			} else {
				try {
					final String fileName = file.getCanonicalPath();
					final boolean acceptedFile = pattern.matcher(fileName).matches();
					if (acceptedFile) {
						// parseAndGetFeatureMetaInfo(fileName);
						resourcevalue.add(fileName);
					}
				} catch (final IOException e) {
					throw new FeatureDeploymentServiceException(
							"Unable to get the canonical path of file : " + file.getName(), e);
				}
			}
		}
		return resourcevalue;
	}

	/**
	 * This method is used to parse the featureMetaInfo
	 * 
	 * @param featureMetaInfo
	 *            : filename to be parsed
	 * @param element
	 *            : jar from where it has to be loaded
	 * @return List : list of FeatureGroup Object
	 * @throws FeatureDeploymentServiceException
	 */
	public List<FeatureGroup> parseAndGetFeatureMetaInfo(String featureMetaInfo, String element)
			throws FeatureDeploymentServiceException {
		logger.debug(
				".parseAndgetFeatureMetaInfo of FeatureMetaInfoExtender " + featureMetaInfo + ",element : " + element);
		URL url;
		try {
			url = new URL("file:" + element);
		} catch (MalformedURLException e1) {
			throw new FeatureDeploymentServiceException("Unable to create URL from element path : " + element, e1);
		}
		CustomLoaderFromJar customJarLoader = new CustomLoaderFromJar(new URL[] { url });
		URL featureMetaInfoXmlUrl = customJarLoader.getResource(featureMetaInfo);
		logger.debug("featureMetaInfoXmlUrl : " + featureMetaInfoXmlUrl);
		String featurexmlAsString = convertXmlToString(featureMetaInfoXmlUrl, featureMetaInfo);
		List<FeatureGroup> featureGroupList = null;
		if (featurexmlAsString != null) {
			FeatureMetaInfoConfigXmlParser featureMetaInfoParser = new FeatureMetaInfoConfigXmlParser();
			try {
				FeatureMetainfo featureMetaInfo1 = featureMetaInfoParser.marshallConfigXMLtoObject(featurexmlAsString);
				featureGroupList = featureMetaInfo1.getFeatureGroup();
			} catch (FeatureMetaInfoConfigParserException e) {
				throw new FeatureDeploymentServiceException("Unable to parse featureMetaInfo xml string into object ");
			}
		} // end of if(featurexmlAsString!=null)
		logger.debug("exiting parseAndgetFeatureMetaInfo of FeatureMetaInfoExtender ");
		return featureGroupList;
	}

	/**
	 * This method is used to xml format to string format
	 * 
	 * @param featureMetaInfoXmlUrl
	 *            : URL Object of resource file
	 * @param featureMetaInfo
	 *            : name of xml file to be converted into String
	 * @return String
	 * @throws FeatureDeploymentServiceException
	 */
	private String convertXmlToString(URL featureMetaInfoXmlUrl, String featureMetaInfo)
			throws FeatureDeploymentServiceException {
		logger.debug(".convertFeatureMetaInfoXmlToString of FeatureMetaInfoExtender");
		InputStream featureMetaInfoXmlInput = null;
		String featurexmlAsString = null;
		StringBuilder out1 = new StringBuilder();
		if (featureMetaInfoXmlUrl != null) {
			try {
				featureMetaInfoXmlInput = featureMetaInfoXmlUrl.openConnection().getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(featureMetaInfoXmlInput));
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						out1.append(line);
					}
				} catch (IOException e) {
					throw new FeatureDeploymentServiceException(
							"Unable to open the read for the BufferedReader for the file : " + featureMetaInfo, e);
				}
				logger.debug(out1.toString()); // Prints the string content read
												// from input stream
				try {
					reader.close();
				} catch (IOException e) {
					throw new FeatureDeploymentServiceException(
							"Unable to close the read for the BufferedReader for the file : " + featureMetaInfo, e);
				}
				featurexmlAsString = out1.toString();
			} catch (IOException e) {
				throw new FeatureDeploymentServiceException(
						"Unable to open the input stream for the file : " + featureMetaInfo, e);
			}
		} else {
			logger.debug("FeatureMetaInfo.xml file doesn't exist ");
		}
		return featurexmlAsString;
	}// end of method

	/**
	 * This method will load resource from feature level
	 * 
	 * @param featureGroupList
	 *            : List of FeatureGroup available in feature
	 * @throws FeatureDeploymentServiceException
	 * @throws IOException
	 */
	private void loadAllResourceFromFeatureLevel(List<FeatureGroup> featureGroupList)
			throws FeatureDeploymentServiceException, IOException {
		if (featureGroupList != null && !featureGroupList.isEmpty()) {
			for (FeatureGroup featureGroup : featureGroupList) {
				String featureGroupName = featureGroup.getName();
				List<Feature> featureList = featureGroup.getFeatures().getFeature();
				logger.debug("featureList : " + featureList.size());
				for (Feature feature : featureList) {
					logger.debug("Feature group Name : " + featureGroupName + ", Feature Name : " + feature.getName()
							+ ", implementationName : " + feature.getImplementationName() + ", vendor name : "
							+ feature.getVendorName() + ", version : " + feature.getVendorVersion() + ", provider : "
							+ feature.getProvider());
					boolean isAvailable = checkFeatureExitInFeatureMaster(featureGroupName, feature.getName(),
							feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
					logger.debug("is avalable : " + isAvailable);
					if (isAvailable) {
						checkResourceAvailableAndload(feature, featureGroupName);
					} else {
						logger.debug("feature doesn't avaliable in master table with feature group name : "
								+ featureGroupName + " and feature name : " + feature.getName());
					}
				}
			}
		} // end of if(featureGroupList != null && !featureGroupList.isEmpty())
	}// end of method

	/**
	 * Method is used to erase previously loaded configurations for freshly
	 * updating the database with new records.
	 */

	private void erasePreviousLoadedConfiguration() {
		logger.debug(".erasePreviousLoadedConfiguration of FeatureMetaInfoResourceUtil");
		int totalRowsDeleted = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table confignode = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			final Table featureDeploymentTable = dataContext
					.getTableByQualifiedLabel(LeapConstants.TABLE_FEATUREDEPLOYMENT);
			DefaultUpdateSummary configNodeDelete = (DefaultUpdateSummary) dataContext
					.executeUpdate(new UpdateScript() {
						@Override
						public void run(UpdateCallback callback) {
							final RowDeletionBuilder delete = callback.deleteFrom(confignode);
							delete.execute();

						}
					});
			DefaultUpdateSummary featureDeploymentTableDelete = (DefaultUpdateSummary) dataContext
					.executeUpdate(new UpdateScript() {
						@Override
						public void run(UpdateCallback callback) {
							final RowDeletionBuilder delete = callback.deleteFrom(featureDeploymentTable);
							delete.execute();

						}
					});
			if (configNodeDelete.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) configNodeDelete.getDeletedRows().get();
				logger.info("confignodedata total deleted record's-->" + totalRowsDeleted);
			} else {
				logger.info("confignodedata already empty!");
			}
			totalRowsDeleted = 0;
			if (featureDeploymentTableDelete.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) featureDeploymentTableDelete.getDeletedRows().get();
				logger.info("featureDeploymentTable total deleted record's-->" + totalRowsDeleted);
			} else {
				logger.info("featureDeploymentTable already empty!");
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to table confignodedata and featureDeploymentTable --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

		// TODO Auto-generated method stub

	}

	/**
	 * This method is used to check if feature exist in FeatureMaster Table or
	 * not
	 * 
	 * @param featureGroup
	 *            : feature group name
	 * @param featureName
	 *            : feature name
	 * @return boolean : True/False
	 * @throws FeatureDeploymentServiceException
	 */
	private boolean checkFeatureExitInFeatureMaster(String featureGroup, String featureName, String implName,
			String vendorName, String version) throws FeatureDeploymentServiceException {
		logger.debug(".checkFeatureExitInFeatureMaster of FeatureMetaInfoExtender");
		IFeatureMasterService featureMasterServie = new FeatureMasterService();
		boolean isAvailable;
		try {
			ConfigurationContext configContext = new ConfigurationContext(tenant, siteId, featureGroup, featureName,
					implName, vendorName, version);
			isAvailable = featureMasterServie.checkFeatureExistInFeatureMasterOrNot(configContext);
		} catch (FeatureMasterServiceException e) {
			throw new FeatureDeploymentServiceException(
					"Unable  find out Feature with feature group name : " + featureGroup + " and feature name : "
							+ featureName + "impl name : " + implName + " in Feature master ",
					e);

		}
		return isAvailable;

	}

	/**
	 * This method is used to check the resource available in
	 * featureMetaInfo.xml and call method to load them
	 * 
	 * @param feature
	 *            : feature name
	 * @param featureGroupName
	 *            : feature group name
	 * @throws FeatureDeploymentServiceException
	 * @throws IOException
	 */
	public void checkResourceAvailableAndload(Feature feature, String featureGroupName)
			throws FeatureDeploymentServiceException, IOException {
		logger.debug(".checkResourceAvailableAndCall of FeatureMetaInfoResourceUtil");
		// check if event resource configured and then load
		logger.debug("feature MetaInfo:- " + feature);
		EventResources eventResources = feature.getEventResources();
		checkEventResourceAndLoad(eventResources, feature, featureGroupName);

		// check if permastore resource configured and then load
		com.attunedlabs.featuremetainfo.jaxb.PermaStoreConfigurations permastoreConfiguration = feature
				.getPermaStoreConfigurations();
		checkPermastoreResourceAndLoad(permastoreConfiguration, feature, featureGroupName);

		// check if dynastore resource configured and then load
		DynaStoreConfigurations dynastoreConfiguration = feature.getDynaStoreConfigurations();
		checkDynastoreResourceAndLoad(dynastoreConfiguration, feature, featureGroupName);

		// check if policy resource configured and then load
		PolicyConfigurations policyConfiguration = feature.getPolicyConfigurations();
		checkPolicyResourceAndLoad(policyConfiguration, feature, featureGroupName);

		// check if featureImpl resource configured and then load
		FeatureImplementations featureImplementation = feature.getFeatureImplementations();
		checkFeatureImplResourceAndLoad(featureImplementation, feature, featureGroupName);

		// check if featureDataContext is defined or not
		FeatureDataContexts featureDataContexts = feature.getFeatureDataContexts();
		checkFeatureDataContextsResourceAndLoad(featureDataContexts, feature, featureGroupName);

		// check if integrationpipelineConfig is defined or not
		IntegrationPipeLineConfigurations integrationPipes = feature.getIntegrationPipeLineConfigurations();
		checkIntegrationPipeCongigurationsResourceAndLoad(integrationPipes, feature, featureGroupName);

		// load the Static file configurations..
		StaticFileConfiguration staticFileConfiguration = feature.getStaticFileConfiguration();
		storeStaticFileConfigs(staticFileConfiguration, feature, featureGroupName);

		// loads the DBConfiguration from FeatureMetaInfo
		DBConfiguration dbConfiguration = feature.getDBConfiguration();
		configureDatabase(dbConfiguration, feature, featureGroupName);

		// loads the UIConfig from FeatureMetaInfo
		UIConfig uiConfig = feature.getUIConfig();
		configureUI(uiConfig, feature, featureGroupName);

		ScheduledJobConfigurations schedulerConfigurations = feature.getScheduledJobConfigurations();
		checkSchedulerResourceAndLoad(schedulerConfigurations, feature, featureGroupName);
	}

	private void checkSchedulerResourceAndLoad(ScheduledJobConfigurations schedulerConfigurations, Feature feature,
			String featureGroupName) throws FeatureDeploymentServiceException {
		logger.debug(".checkSchedulerResourceAndLoad of FeatureMetaInfoResourceUtil");
		if (schedulerConfigurations != null) {
			List<ScheduledJobConfiguration> schedulerConfigList = schedulerConfigurations
					.getScheduledJobConfiguration();
			if (schedulerConfigList != null) {
				loadSchedulerResourceInFeatureMetaInfo(schedulerConfigList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.debug("No SchedulerResource is defined in FeatureMetaInfo for feature Group : "
						+ featureGroupName + ", feature name : " + feature + " but empty");
			}
		} else {
			logger.debug("No SchedulerResource configured in FeatureMetaInfo for feature Group : " + featureGroupName
					+ ", feature name : " + feature);
		}

	}

	private void loadSchedulerResourceInFeatureMetaInfo(List<ScheduledJobConfiguration> schedulerConfigList,
			String featureGroupName, String featureName, String implementationName, String vendorName,
			String vendorVersion) throws FeatureDeploymentServiceException {
		logger.debug(".getSchedulerResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		for (ScheduledJobConfiguration schedulerConfig : schedulerConfigList) {
			logger.debug("schedulerConfig : " + schedulerConfig.toString());
			String schedulerResourceName = schedulerConfig.getResourceName();
			logger.debug("schedulerResourceName : " + schedulerResourceName);
			URL schedulerResourceUrl = FeatureMetaInfoResourceUtil.class.getClassLoader()
					.getResource(schedulerResourceName);
			logger.debug("schedulerResourceUrl : " + schedulerResourceUrl);
			if (schedulerResourceUrl != null) {
				String schedulerSourceAsString = convertXmlToString(schedulerResourceUrl,
						schedulerConfig.getResourceName());
				logger.debug("schedulerSourceAsString : " + schedulerSourceAsString);
				if (schedulerSourceAsString != null) {
					ScheduledJobConfigXMLParser schedulerConfigParser = new ScheduledJobConfigXMLParser();
					com.attunedlabs.scheduler.jaxb.ScheduledJobConfigurations scheduler = null;
					try {
						scheduler = schedulerConfigParser.marshallConfigXMLtoObject(schedulerSourceAsString);
						List<com.attunedlabs.scheduler.jaxb.ScheduledJobConfiguration> schedulerConfigList1 = scheduler
								.getScheduledJobConfiguration();
						for (com.attunedlabs.scheduler.jaxb.ScheduledJobConfiguration schedulerConfiguration : schedulerConfigList1) {
							logger.debug("schedulerConfiguration : " + schedulerConfiguration.toString());
							String configname = schedulerConfiguration.getName();
							logger.debug("configname : " + configname);
							IScheduledJobConfigurationService schedulerConfigService = new ScheduledJobConfigurationService();
							ConfigurationContext configurationContext = null;
							RequestContext requestContext = null;
							boolean isEnabled = schedulerConfiguration.getIsEnabled();
							logger.debug("isEnabled : " + isEnabled);
							// String response =
							// schedulerConfigParser.unmarshallConfigXMLtoObject(schedulerConfiguration);
							// logger.debug("response :"+response);
							if (isEnabled) {
								logger.debug("scheduler config is marked as global");
								requestContext = new RequestContext(FeatureMetaInfoConstant.DEFAULT_TENANT_KEY,
										FeatureMetaInfoConstant.DEFAULT_SITE_KEY, featureGroupName, featureName,
										implementationName, vendorName, vendorVersion);
								configurationContext = new ConfigurationContext(
										FeatureMetaInfoConstant.DEFAULT_TENANT_KEY,
										FeatureMetaInfoConstant.DEFAULT_SITE_KEY, featureGroupName, featureName,
										implementationName, vendorName, vendorVersion);
							} else {
								logger.debug("scheduler config is not global");
								requestContext = new RequestContext(LeapHeaderConstant.tenant, LeapHeaderConstant.site,
										featureGroupName, featureName, implementationName, vendorName, vendorVersion);
								configurationContext = new ConfigurationContext(LeapHeaderConstant.tenant,
										LeapHeaderConstant.site, featureGroupName, featureName, implementationName,
										vendorName, vendorVersion);
							}
							logger.debug("before isExist in loadSchedulerResourceInFeatureMetaInfo");
							boolean isExist = schedulerConfigService.checkScheduledJobConfigarationExistOrNot(
									configurationContext, schedulerConfiguration.getName());
							logger.debug(
									"after isExist in loadSchedulerResourceInFeatureMetaInfo isExist : " + isExist);
							if (!isExist) {
								schedulerConfigService.addScheduledJobConfiguration(configurationContext,
										schedulerConfiguration);

							} else {
								logger.debug("Scheduler configuration for : " + configname + " already exist");
							}
						} // end of if(builderType.equalsIgnoreCase("CUSTOM"))
					} catch (ScheduledJobConfigParserException | ScheduledJobConfigRequestException
							| ScheduledJobConfigurationException e) {
						throw new FeatureDeploymentServiceException(
								"Unable to parse scheduler file : " + schedulerConfig.getResourceName());
					}
				} // end of for
			} else {
				logger.debug("No scheduler config xml defined for : " + schedulerResourceName);
			}
		}
		logger.debug("exiting getSchedulerResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");

	}

	/**
	 * Configures the database form DBConfiguration, from DBConfiguration -
	 * consumes the ddlUtil-database structure
	 * 
	 * @param dbConfiguration
	 * @param feature
	 * @param featureGroupName
	 * @throws FeatureDeploymentServiceException
	 */
	private void configureDatabase(DBConfiguration dbConfiguration, Feature feature, String featureGroupName)
			throws FeatureDeploymentServiceException {
		if (dbConfiguration != null) {
			String databaseXml = "";
			try {
				databaseXml = getDbConfigasString(dbConfiguration);
				logger.debug(".configureDatabase(), the DatabaseConfigurations: " + databaseXml);
				RequestContext requestContext = getRequestContextFromConfigurationContext(feature, featureGroupName);
				DataSource dataSource = getDataSourceFromDataContext(requestContext);
				/*
				 * api-ddlUtils requires xml-hence converted from String to
				 * InputStream, which is read from FeatureMetaInfo
				 */
				InputStream input = IOUtils.toInputStream(databaseXml.trim(), "UTF-8");
				Reader reader = new InputStreamReader(input, "UTF-8");
				org.apache.ddlutils.model.Database desiredDB = new DatabaseIO().read(reader);

				configureTables(dataSource, desiredDB);// ..call to api-ddlutils

			} catch (JAXBException | IOException | DataContextConfigurationException | DataContextParserException
					| ParserConfigurationException | SAXException e) {
				throw new FeatureDeploymentServiceException("Unable to process model for the DatabaseConfigured ", e);
			}
		}
	}// ..end of the method

	/**
	 * 
	 * @param dataSource
	 * @param desiredDB
	 */
	private void configureTables(DataSource dataSource, org.apache.ddlutils.model.Database desiredDB) {
		String dbName = desiredDB.getName();
		logger.debug("Configured - dbName : " + dbName);
		Platform platform = PlatformFactory.createNewPlatformInstance(dataSource);
		org.apache.ddlutils.model.Database currentDB = platform.readModelFromDatabase(dbName);
		platform.alterModelExtended(false, currentDB, desiredDB, true);
	}// ..end of the method

	/**
	 * 
	 * @param feature
	 * @param featureGroupName
	 * @return
	 * @throws FeatureDeploymentServiceException
	 */
	private RequestContext getRequestContextFromConfigurationContext(Feature feature, String featureGroupName)
			throws FeatureDeploymentServiceException {
		ConfigurationContext configurationContext = new ConfigurationContext(tenant, siteId, featureGroupName,
				feature.getName(), feature.getImplementationName(), feature.getVendorName(),
				feature.getVendorVersion());
		RequestContext requestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(),
				configurationContext.getFeatureName(), configurationContext.getImplementationName(),
				configurationContext.getVendorName(), configurationContext.getVersion());
		return requestContext;
	}

	/**
	 * method to create dataSource from the dataContext
	 * 
	 * @return dataSource
	 * @throws DataContextConfigurationException
	 * @throws DataContextParserException
	 * @throws ParserConfigurationException
	 * @throws FeatureDeploymentServiceException
	 * @throws IOException
	 * @throws SAXException
	 */
	private DataSource getDataSourceFromDataContext(RequestContext requestContext)
			throws DataContextConfigurationException, DataContextParserException, FeatureDeploymentServiceException,
			ParserConfigurationException, SAXException, IOException {

		DataContextConfigurationService configService = new DataContextConfigurationService();
		DataContextConfigurationUnit dataContextConfig = configService.getDataContextConfiguration(requestContext);
		FeatureDataContext featureDataContext = (FeatureDataContext) dataContextConfig.getConfigData();

		DataSource dataSource = null;
		try {
			dataSource = getDataSource(featureDataContext);
		} catch (FeatureConfigurationException e) {
			throw new FeatureDeploymentServiceException("Unable to get the DataSource for the configured dataBase: ",
					e);
		}
		return dataSource;
	}// ..end of the method

	/**
	 * configures the dataSource object based on the dbType
	 * 
	 * @param featureDataContext
	 * @return javax.sql DataSource
	 * @throws FeatureConfigurationException
	 * @throws FeatureConfigRequestException
	 */
	private DataSource getDataSource(FeatureDataContext featureDataContext) throws FeatureConfigurationException {
		DataContext dataConetxt = featureDataContext.getDataContexts().getDataContext().get(0);
		String dbType = dataConetxt.getDbType();
		String dbHost = dataConetxt.getDbHost();
		String dbPort = dataConetxt.getDbPort();
		String dbSchema = dataConetxt.getDbSchema();
		String user = dataConetxt.getDbUser();
		String password = dataConetxt.getDbPassword();
		DataSource dataSource = null;
		switch (dbType.toLowerCase()) {
		case MYSQL_TYPE:
			dataSource = getMySqlDataSource(dbType, dbHost, dbPort, dbSchema, user, password);
			return dataSource;
		case ORACLE_TYPE:
			dataSource = getOracleDataSource(dbType, dbHost, dbPort, dbSchema, user, password);
			break;
		case MSSQL_TYPE:
			dataSource = getMSSqlDataSource(dbType, dbHost, dbPort, dbSchema, user, password);
			return dataSource;
		case POSTGRESSQL_TYPE:
			dataSource = getPostgresSqlDataSource(dbType, dbHost, dbPort, dbSchema, user, password);
			return dataSource;
		default:
			throw new FeatureConfigurationException(
					"Invalid dbType configured, configure any one of the following ['postgressql','mssql','oracle','mysql'].");
		}
		return dataSource;
	}// ..end of the method

	/**
	 * prepares the datasource in java.sql.Datasource, from the
	 * PGPoolingDatasource
	 * 
	 * @param dbType
	 * @param dbHost
	 * @param dbPort
	 * @param dbSchema
	 * @param user
	 * @param password
	 * @return postgresql DataSource in java.sql casted
	 */
	private DataSource getPostgresSqlDataSource(String dbType, String dbHost, String dbPort, String dbSchema,
			String user, String password) {
		PGPoolingDataSource source = new PGPoolingDataSource();
		source.setDataSourceName("postgresDatasource");
		source.setServerName(dbHost);
		source.setDatabaseName(dbSchema);
		source.setUser(user);
		source.setPassword(password);
		source.setMaxConnections(10);
		return source;
	}// ..end of the method

	/**
	 * constructs the SQLServer datasource, if called to perform the ddlutils
	 * operation
	 * 
	 * @param dbType
	 * @param dbHost
	 * @param dbPort
	 * @param dbSchema
	 * @param user
	 * @param password
	 * @return SQLServerDataSource
	 */
	private SQLServerDataSource getMSSqlDataSource(String dbType, String dbHost, String dbPort, String dbSchema,
			String user, String password) {
		SQLServerDataSource ds = new SQLServerDataSource();
		// ds.setUser(user);
		// ds.setPassword(password);
		// ds.setServerName(dbHost);

		// ds.setPortNumber(Integer.valueOf(dbPort));
		// jdbc:sqlserver://HOSP_SQL1.company.com;user=name;password=abcdefg;database=Test
		String url = "jdbc:sqlserver://" + dbHost + ":" + dbPort + ";database=" + dbSchema + ";" + "user=" + user
				+ ";password=" + password + ";";
		ds.setURL(url);
		// ds.setDatabaseName(dbSchema);
		return ds;
	}// ..end of the method

	/**
	 * Builds a new OracleDataSource to perform the ddlUtils DBConfiguration
	 * 
	 * @param dbType
	 * @param dbHost
	 * @param dbPort
	 * @param dbSchema
	 * @param user
	 * @param password
	 * @return
	 * @throws FeatureConfigurationException
	 */
	private OracleDataSource getOracleDataSource(String dbType, String dbHost, String dbPort, String dbSchema,
			String user, String password) throws FeatureConfigurationException {
		OracleDataSource oracleDS = null;
		try {
			oracleDS = new OracleDataSource();
			oracleDS.setDatabaseName(dbSchema);
			oracleDS.setPortNumber(Integer.valueOf(dbPort));
			oracleDS.setUser(user);
			oracleDS.setPassword(password);
			// jdbc:oracle:thin:@192.168.1.78:1521:XE
			String url = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + "/" + dbSchema;
			oracleDS.setURL(url);
		} catch (SQLException e) {
			throw new FeatureConfigurationException("Unable to connect to OracleDataSource: ", e);
		}
		return oracleDS;
	}// ..end of the method

	/**
	 * Private accessory, returns the mysql DS for the type = MySql
	 * 
	 * @param dbType
	 * @param dbHost
	 * @param dbPort
	 * @param dbSchema
	 * @return mysqlDS
	 */
	private MysqlDataSource getMySqlDataSource(String dbType, String dbHost, String dbPort, String dbSchema,
			String user, String password) {
		MysqlDataSource mysqlDS = new MysqlDataSource();
		mysqlDS.setURL("jdbc:" + dbType + ":" + "//" + dbHost + ":" + dbPort + "/" + dbSchema);
		mysqlDS.setUser(user);
		mysqlDS.setPassword(password);

		return mysqlDS;
	}// ..end of the method

	/**
	 * Gets the object defined in FeatureMetaInfoa and returns the string of the
	 * databases-needed by ddlUtils
	 * 
	 * @param dbConfiguration
	 * @return xmlString which hold the Database
	 * @throws JAXBException
	 */
	private String getDbConfigasString(DBConfiguration dbConfiguration) throws JAXBException {
		StringWriter xmlOut = new StringWriter();
		JAXBContext contextObj = JAXBContext.newInstance(Database.class);
		Marshaller marshallerObj = contextObj.createMarshaller();
		marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshallerObj.marshal(dbConfiguration.getDatabase(), xmlOut);

		StringBuffer sb = new StringBuffer(xmlOut.toString());
		sb.insert(56, DDLUITLS_XMLDOCTYPE);

		logger.debug(".getDbConfigasString(), the DatabaseConfigurations: " + sb);

		return sb.toString();
	}// ..end of the method

	/**
	 * to store the StaticFileConfigs into the zookeeperNode
	 * 
	 * @param staticFileConfiguration
	 * @param feature
	 * @param featureGroupName
	 * @throws IOException
	 * @throws FeatureDeploymentServiceException
	 */
	private void storeStaticFileConfigs(StaticFileConfiguration staticFileConfiguration, Feature feature,
			String featureGroupName) throws IOException, FeatureDeploymentServiceException {
		logger.debug(".storeStaticFileConfigs method of FeatureMetaInfoResourceUtil");
		if (staticFileConfiguration != null) {
			logger.debug("StaticConfigFileNames..." + staticFileConfiguration.getConfigFile());
			List<ConfigFile> statConfigs = staticFileConfiguration.getConfigFile();
			ConfigurationContext configurationContext = new ConfigurationContext(tenant, siteId, featureGroupName,
					feature.getName(), feature.getImplementationName(), feature.getVendorName(),
					feature.getVendorVersion());
			try {
				IStaticConfigurationService staticConfigurationService = StaticConfigurationFactory
						.getFilemanagerInstance();
				if (staticConfigurationService instanceof ZookeeperFilemanagementServiceImpl) {
					for (ConfigFile configFile : statConfigs) {
						String localPath = configFile.getFilePath();
						int index = localPath.length() - 1;
						if (!(localPath.charAt(index) == '/')) {
							localPath = localPath + "/";
						} // ..checking and appending '/' at the end of the
							// directory
						logger.debug(
								"ConfigNames..." + configFile.getFileName() + "---: Path specified is..." + localPath);
						try {
							String contents = null;
							contents = new String(Files.readAllBytes(Paths.get(localPath + configFile.getFileName())));
							staticConfigurationService.addStaticConfiguration(configurationContext,
									configFile.getFileName(), contents);
						} catch (StaticConfigDuplicateNameofFileException | AddStaticConfigException
								| AccessProtectionException | InvalidFilePathException e) {
							// This step is to skip the redundancy of file
							logger.debug("File Not saved in zookeeper nodes.. " + configFile.getFileName());
						}
					}
				}
			} catch (InstantiationException | IllegalAccessException | StaticConfigInitializationException e) {
				throw new FeatureDeploymentServiceException("Unable to instantiate the StaticFileConfiguration", e);
			}
		}
	}// ..end of the method

	/**
	 * this is the method which is called to load, validate and add to the
	 * database
	 * 
	 * @param integrationPipes
	 * @param feature
	 * @param featureGroupName
	 * @throws FeatureDeploymentServiceException
	 */
	private void checkIntegrationPipeCongigurationsResourceAndLoad(IntegrationPipeLineConfigurations integrationPipes,
			Feature feature, String featureGroupName) throws FeatureDeploymentServiceException {
		logger.debug(".checkIntegrationPipeCongigurationsResourceAndLoad()..." + integrationPipes);
		if (integrationPipes != null) {
			List<PipeConfiguration> listOfPipeConfigs = integrationPipes.getPipeConfiguration();
			if (listOfPipeConfigs != null) {
				loadPipeConfigResourcesInFeatureMetaInfo(listOfPipeConfigs, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.debug("No IntegrationPipeline is defined in FeatureMetaInfo for feature Group : "
						+ featureGroupName + ", feature name : " + feature + ", impl name : "
						+ feature.getImplementationName() + " but empty");
			}
		} else {
			logger.debug("No IntegrationPipeline configured in FeatureMetaInfo for feature Group : " + featureGroupName
					+ ", feature name : " + feature);
		}
	}// ..end of the method

	/**
	 * This method is used to load validate and add integration configuration in
	 * database and cache
	 * 
	 * @param featureDataContexts
	 * @param feature
	 * @param featureGroupName
	 * @throws FeatureDeploymentServiceException
	 */
	private void checkFeatureDataContextsResourceAndLoad(FeatureDataContexts featureDataContexts, Feature feature,
			String featureGroupName) throws FeatureDeploymentServiceException {
		logger.debug(".checkFeatureDataContextsResourceAndLoad method of FeatureLeapInfoResourceUtil");
		logger.debug("featureDataContexts : " + featureDataContexts);
		if (featureDataContexts != null) {
			List<DataContexts> dataContextsList = featureDataContexts.getDataContexts();
			if (dataContextsList != null) {
				logger.debug("calling method to  load datacontext");
				loadDataContextResourcesInFeatureMetaInfo(dataContextsList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.debug("No DataContext is defined in FeatureMetaInfo for feature Group : " + featureGroupName
						+ ", feature name : " + feature + ", impl name : " + feature.getImplementationName()
						+ " but empty");
			}
		} else {
			logger.debug("No DataContext configured in FeatureMetaInfo for feature Group : " + featureGroupName
					+ ", feature name : " + feature);
		}

	}// end of method checkFeatureDataContextsResourceAndLoad

	private void loadDataContextResourcesInFeatureMetaInfo(List<DataContexts> dataContextsList, String featureGroupName,
			String name, String implName, String vendorName, String vendorVersion)
			throws FeatureDeploymentServiceException {
		logger.debug(".loadDataContextResourcesInFeatureMetaInfo method of FeatureLeapInfoResourceUtil");
		for (DataContexts dataContexts : dataContextsList) {
			String dataContextResourceName = dataContexts.getResourceName();
			logger.debug("dataContextResourceName : " + dataContextResourceName);
			URL dataContextResourceUrl = FeatureMetaInfoResourceUtil.class.getClassLoader()
					.getResource(dataContextResourceName);
			if (dataContextResourceUrl != null) {
				String dataContextAsSourceAsString = convertXmlToString(dataContextResourceUrl,
						dataContextResourceName);
				if (dataContextAsSourceAsString != null) {
					DataContextConfigXMLParser dataContextXmlParser = new DataContextConfigXMLParser();
					IDataContextConfigurationService dataContextConfigService = new DataContextConfigurationService();
					ConfigurationContext configContext = null;
					try {
						FeatureDataContext featureDataContext = dataContextXmlParser
								.marshallConfigXMLtoObject(dataContextAsSourceAsString);

						configContext = new ConfigurationContext(tenant, siteId, featureGroupName, name, implName,
								vendorName, vendorVersion);
						try {
							boolean isExist = dataContextConfigService.checkDataContextConfigExistOrNot(configContext,
									featureDataContext.getDataContexts().getContextName());
							if (!isExist) {
								dataContextConfigService.addDataContext(configContext, featureDataContext);
							} else {
								logger.debug("pipeline configuration for : " + dataContextResourceName
										+ "already exist for featuregroup : " + featureGroupName + " and feature : "
										+ name + ", impl name : " + implName + " in db");
							}
						} catch (DataContextConfigurationException e) {
							throw new FeatureDeploymentServiceException(
									"Unable to add configuration file for feature group :  " + featureGroupName
											+ ", feature name : " + name + ", with context data : " + configContext);

						}

					} catch (DataContextParserException e) {
						throw new FeatureDeploymentServiceException(
								"Unable to parse datacontext configuration file for feature group :  "
										+ featureGroupName + ", feature name : " + name);

					}

				}
			} else {
				logger.debug("No datacontext config xml exist with name : " + dataContextResourceName);
			}
		}

	}// end of method loadDataContextResourcesInFeatureMetaInfo

	/**
	 * This method is used to check and load event resource defined in
	 * featureMetaInfo.xml
	 * 
	 * @param eventResources
	 *            : EventResources Object of FeatureMetaInfo
	 * @param feature
	 *            : feature Name
	 * @param featureGroupName
	 *            : Feature group name
	 * @throws FeatureDeploymentServiceException
	 */
	private void checkEventResourceAndLoad(EventResources eventResources, Feature feature, String featureGroupName)
			throws FeatureDeploymentServiceException {
		if (eventResources != null) {
			List<EventResource> eventResourceList = eventResources.getEventResource();
			if (eventResourceList != null) {
				loadEventResourcesInFeatureMetaInfo(eventResourceList, featureGroupName, feature);

			} else {
				logger.debug("No EventResource is defined in FeatureMetaInfo for feature Group : " + featureGroupName
						+ ", feature name : " + feature + " but empty");
			}
		} else {
			logger.debug("No EventResource configured in FeatureMetaInfo for feature Group : " + featureGroupName
					+ ", feature name : " + feature);
		}
	}

	/**
	 * This method is used to check and load permastore resource defined in
	 * featureMetaInfo.xml
	 * 
	 * @param permastoreConfiguration
	 *            : PermaStoreConfigurations Object of FeatureMetaInfo
	 * @param feature
	 *            : feature Name
	 * @param featureGroupName
	 *            : Feature group name
	 * @throws FeatureDeploymentServiceException
	 */
	private void checkPermastoreResourceAndLoad(
			com.attunedlabs.featuremetainfo.jaxb.PermaStoreConfigurations permastoreConfiguration, Feature feature,
			String featureGroupName) throws FeatureDeploymentServiceException {
		if (permastoreConfiguration != null) {
			List<PermaStoreConfiguration> permastoreConfigList = permastoreConfiguration.getPermaStoreConfiguration();
			if (permastoreConfigList != null) {
				loadPermastoreResourceInFeatureMetaInfo(permastoreConfigList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.debug("No PermastoreResource is defined in FeatureMetaInfo for feature Group : "
						+ featureGroupName + ", feature name : " + feature + " but empty");
			}
		} else {
			logger.debug("No PermastoreResource configured in FeatureMetaInfo for feature Group : " + featureGroupName
					+ ", feature name : " + feature);
		}
	}

	/**
	 * This method is used to check and load PolicyResource resource defined in
	 * featureMetaInfo.xml
	 * 
	 * @param policyConfiguration
	 *            : PolicyConfigurations Object of FeatureMetaInfo
	 * @param feature
	 *            : feature Name
	 * @param featureGroupName
	 *            : Feature group name
	 * @throws FeatureDeploymentServiceException
	 */
	private void checkPolicyResourceAndLoad(PolicyConfigurations policyConfiguration, Feature feature,
			String featureGroupName) throws FeatureDeploymentServiceException {
		if (policyConfiguration != null) {
			List<PolicyConfiguration> policyConfigList = policyConfiguration.getPolicyConfiguration();
			if (policyConfigList != null) {
				loadPolicyResourceInFeatureMetaInfo(policyConfigList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.debug("No PolicyResource is defined in FeatureMetaInfo for feature Group : " + featureGroupName
						+ ", feature name : " + feature + "impl name : " + feature.getImplementationName()
						+ " but empty");
			}
		} else {
			logger.debug("No PolicyResource configured in FeatureMetaInfo for feature Group : " + featureGroupName
					+ ", feature name : " + feature);
		}
	}

	/**
	 * This method is used to check and load FeatureImplResource resource
	 * defined in featureMetaInfo.xml
	 * 
	 * @param FeatureImplementations
	 *            : FeatureImplementations Object of FeatureMetaInfo
	 * @param feature
	 *            : feature Name
	 * @param featureGroupName
	 *            : Feature group name
	 * @throws FeatureDeploymentServiceException
	 */
	private void checkFeatureImplResourceAndLoad(FeatureImplementations featureImplementations, Feature feature,
			String featureGroupName) throws FeatureDeploymentServiceException {
		if (featureImplementations != null) {
			List<FeatureImplementation> featureImplList = featureImplementations.getFeatureImplementation();
			if (featureImplList != null) {
				loadFeatureResourceInFeatureMetaInfo(featureImplList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion(),
						feature.getProvider());
			} else {
				logger.debug("No FeatureImplResource is defined in FeatureMetaInfo for feature Group : "
						+ featureGroupName + ", feature name : " + feature + ", impl name : "
						+ feature.getImplementationName() + " but empty");
			}
		} else {
			logger.debug("No FeatureImplResource configured in FeatureMetaInfo for feature Group : " + featureGroupName
					+ ", feature name : " + feature);
		}
	}

	/**
	 * This method is used to check and load DynastoreResource resource defined
	 * in featureMetaInfo.xml
	 * 
	 * @param dynastoreConfiguration
	 *            : DynaStoreConfigurations Object of FeatureMetaInfo
	 * @param feature
	 *            : feature Name
	 * @param featureGroupName
	 *            : Feature group name
	 * @throws FeatureDeploymentServiceException
	 */
	private void checkDynastoreResourceAndLoad(DynaStoreConfigurations dynastoreConfiguration, Feature feature,
			String featureGroupName) throws FeatureDeploymentServiceException {
		if (dynastoreConfiguration != null) {
			List<DynaStoreConfiguration> dynaStoreConfigList = feature.getDynaStoreConfigurations()
					.getDynaStoreConfiguration();
			if (dynaStoreConfigList != null) {
				loadDynastoreResourceInFeatureMetaInfo(dynaStoreConfigList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.debug("No DynastoreResource is defined in FeatureMetaInfo for feature Group : "
						+ featureGroupName + ", feature name : " + feature + ", impl Name : "
						+ feature.getImplementationName() + " but empty");
			}
		} else {
			logger.debug("No DynastoreResource configured in FeatureMetaInfo for feature Group : " + featureGroupName
					+ ", feature name : " + feature);
		}
	}

	private void loadDynastoreResourceInFeatureMetaInfo(List<DynaStoreConfiguration> dynaStoreConfigList,
			String featureGroupName, String name, String implName, String vendorName, String version)
			throws FeatureDeploymentServiceException {
		logger.debug(".getDynastoreResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		for (DynaStoreConfiguration dynastoreconfig : dynaStoreConfigList) {
			String dynaResourceName = dynastoreconfig.getResourceName();
			logger.debug("dynaResourceName : " + dynaResourceName);
			URL dynaResourceUrl = FeatureMetaInfoResourceUtil.class.getClassLoader().getResource(dynaResourceName);
			if (dynaResourceUrl != null) {
				String dynastoreAsSourceAsString = convertXmlToString(dynaResourceUrl, dynaResourceName);
				if (dynastoreAsSourceAsString != null) {
					DynaStoreConfigXmlParser parser = new DynaStoreConfigXmlParser();
					IDynaStoreConfigurationService iDynaStoreConfigurationService = null;
					ConfigurationContext configContext = null;
					try {
						DynastoreConfigurations dynastoreConfigurations = parser
								.marshallConfigXMLtoObject(dynastoreAsSourceAsString);
						configContext = new ConfigurationContext(tenant, siteId, featureGroupName, name, implName,
								vendorName, version);
						iDynaStoreConfigurationService = new DynaStoreConfigurationService();
						List<DynastoreConfiguration> dynastoreConfigList = dynastoreConfigurations
								.getDynastoreConfiguration();
						for (DynastoreConfiguration dynaConfig : dynastoreConfigList) {
							try {
								iDynaStoreConfigurationService.addDynaStoreConfiguration(configContext, dynaConfig);
							} catch (DynaStoreConfigurationException e) {
								throw new FeatureDeploymentServiceException(
										"Unable to add configuration file for feature group :  " + featureGroupName
												+ ", feature name : " + name + ", with context data : "
												+ configContext);
							}
						} // end of for
					} catch (DynaStoreConfigParserException e) {
						throw new FeatureDeploymentServiceException(
								"Unable to parse dynastore configuration file for feature group :  " + featureGroupName
										+ ", feature name : " + name);
					}

				} // end of if(dynastoreAsSourceAsString !=null)
			} else {
				logger.debug("No dynastore config xml exist with name : " + dynaResourceName);
			}
		} // end of for
	}// end of method

	private void loadEventResourcesInFeatureMetaInfo(List<EventResource> eventResourceList, String featureGroupName,
			Feature feature) throws FeatureDeploymentServiceException {
		logger.debug(".getEventResourcesInFeatureMetaInfo of FeatureMetaInfoResourceUtil");

		String featureName = feature.getName();
		String implName = feature.getImplementationName();
		String vendorName = feature.getVendorName();
		String version = feature.getVendorVersion();

		for (EventResource eventresource : eventResourceList) {
			String eventResourceName = eventresource.getResourceName();
			URL eventResourceUrl = FeatureMetaInfoResourceUtil.class.getClassLoader().getResource(eventResourceName);
			if (eventResourceUrl != null) {
				logger.debug("eventResourceUrl : " + eventResourceUrl);
				String eventSourceAsString = convertXmlToString(eventResourceUrl, eventresource.getResourceName());
				if (eventresource != null) {
					EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
					EventFramework eventFrameworkConfig = null;
					IEventFrameworkConfigService eventConfigService = new EventFrameworkConfigService();
					try {
						eventFrameworkConfig = parser.marshallConfigXMLtoObject(eventSourceAsString);
						// prepare the configcontext for eventing
						ConfigurationContext configContext = new ConfigurationContext(tenant, siteId, featureGroupName,
								featureName, implName, vendorName, version);
						// check dispatcher defined or not and then load
						// configuration
						DispatchChanels dispatcherChanel = eventFrameworkConfig.getDispatchChanels();
						loadEventChanelConfiguration(dispatcherChanel, eventConfigService, configContext);

						// check SystemEvents defined or not and then load
						// configuration
						SystemEvents systemEvent = eventFrameworkConfig.getSystemEvents();
						loadSystemEventConfiguration(systemEvent, eventConfigService, configContext);

						// check Events defined or not and then load
						// configuration
						Events events = eventFrameworkConfig.getEvents();
						loadEventConfiguration(events, eventConfigService, configContext);

						// check event subscription defined or not and then load
						// configuration
						EventSubscription eventSusbscriptions = eventFrameworkConfig.getEventSubscription();
						loadEventSubscriptionConfiguration(eventSusbscriptions, eventConfigService, configContext,
								featureGroupName, feature);
					} catch (EventFrameworkConfigParserException e) {
						throw new FeatureDeploymentServiceException(
								"Unable to parse event file : " + eventresource.getResourceName());
					}
				} // end of if(eventresource !=null)
				logger.debug("exiting getEventResourcesInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
			} else {
				logger.debug("no such event source defined : " + eventResourceName);
			}
		} // end of for
	}// end of method

	public final static String SUBSCRIPTION_ID_REPLACEMENT_CHARACTER = "-";
	public final static String SUBSCRIPTION_ID_NEW_CHARACTER = "_";

	/**
	 * This method is used to add event susbscription for the event in cache and
	 * in db
	 * 
	 * @param eventSusbscriptions
	 *            : EventSubscriptions Object
	 * @param eventConfigService
	 *            : EventFrameworkConfigService Object
	 * @param configContext
	 *            : ConfigurationContext Object
	 * @param feature
	 * @param featureGroupName
	 * @throws FeatureDeploymentServiceException
	 */
	private void loadEventSubscriptionConfiguration(EventSubscription eventSusbscriptions,
			IEventFrameworkConfigService eventConfigService, ConfigurationContext configContext,
			String featureGroupName, Feature feature) throws FeatureDeploymentServiceException {
		logger.debug(".loadEventSubscriptionConfiguration of FeatureMetaInfoResourceUtil");
		if (eventSusbscriptions != null) {
			List<SubscribeEvent> eventSubscriptionList = eventSusbscriptions.getSubscribeEvent();
			for (SubscribeEvent eventSubscription : eventSubscriptionList) {
				try {
					// repace character with '-' to '_' for subscriptionId
					eventSubscription.setSubscriptionId(eventSubscription.getSubscriptionId()
							.replaceAll(SUBSCRIPTION_ID_REPLACEMENT_CHARACTER, SUBSCRIPTION_ID_NEW_CHARACTER));
					eventConfigService.addEventFrameworkConfiguration(configContext, eventSubscription);
				} catch (EventFrameworkConfigurationException e) {
					throw new FeatureDeploymentServiceException("Error in adding eventSubscription configuration ", e);

				}
			} // end of for loop

			// add subscription features in dataGrid
			try {
				LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
				leapConfigurationServer.loadFeatureInDataGrid(feature, featureGroupName);// ALL-CONFIG-CONTEXT-KEY
																							// as
																							// the
																							// listName
																							// in
																							// datagrid
			} catch (ConfigServerInitializationException e) {
				logger.warn("Unable to add request context for subscription in datagrid..." + e.getMessage());
			}
		} else {
			logger.debug("Event subscription is undefined");

		}

	}// end of method

	/**
	 * This method is used to add channel configuration of event
	 * 
	 * @param dispatcherChanel
	 *            : DispatchChanels Object
	 * @param eventConfigService
	 *            : EventFrameworkConfigService Object
	 * @param configContext
	 *            : ConfigurationContext Object
	 * @throws FeatureDeploymentServiceException
	 */
	private void loadEventChanelConfiguration(DispatchChanels dispatcherChanel,
			IEventFrameworkConfigService eventConfigService, ConfigurationContext configContext)
			throws FeatureDeploymentServiceException {
		logger.debug(".loadEventChanelConfiguration of FeatureMetaInfoResourceUtil");
		if (dispatcherChanel != null) {
			List<DispatchChanel> disChanelList = dispatcherChanel.getDispatchChanel();
			// addchanel init cache
			for (DispatchChanel disChanelConfig : disChanelList) {
				try {
					eventConfigService.addEventFrameworkConfiguration(configContext, disChanelConfig);
				} catch (EventFrameworkConfigurationException e) {
					throw new FeatureDeploymentServiceException("Error in adding channel configuration ", e);

				}
			} // end of for loop
		} else {
			logger.debug("dispatcher channel is not defined for the event configuration");
		}
	}// end of method

	/**
	 * This method is used to add system event configuration of event
	 * 
	 * @param systemEvent
	 *            : SystemEvents Object
	 * @param eventConfigService
	 *            : EventFrameworkConfigService Object
	 * @param configContext
	 *            : ConfigurationContext Object
	 * @throws FeatureDeploymentServiceException
	 */
	private void loadSystemEventConfiguration(SystemEvents systemEvents,
			IEventFrameworkConfigService eventConfigService, ConfigurationContext configContext)
			throws FeatureDeploymentServiceException {
		logger.debug(".loadSystemEventConfiguration of FeatureMetaInfoResourceUtil");
		if (systemEvents != null) {
			List<SystemEvent> systemEventList = systemEvents.getSystemEvent();
			// add system events
			for (SystemEvent systemEvent : systemEventList) {
				List<EventDispatcher> eventDispacherList = systemEvent.getEventDispatchers().getEventDispatcher();
				for (EventDispatcher eventDispacher : eventDispacherList) {
					String transformationtype = eventDispacher.getEventTransformation().getType();
					if (transformationtype.equalsIgnoreCase("XML-XSLT")) {
						logger.debug("event for whom xslt defined : " + systemEvent);
						String xslName = eventDispacher.getEventTransformation().getXSLTName();
						URL xslUrl = FeatureMetaInfoResourceUtil.class.getClassLoader().getResource(xslName);
						logger.debug("xsl url : " + xslUrl + " for xslt name : " + xslName);
						String xslAsString = convertXmlToString(xslUrl, xslName);
						logger.debug("xslt As String : " + xslAsString);
						eventDispacher.getEventTransformation().setXsltAsString(xslAsString);
					}
				}
				try {
					eventConfigService.addEventFrameworkConfiguration(configContext, systemEvent);
				} catch (EventFrameworkConfigurationException e) {
					throw new FeatureDeploymentServiceException("Error in adding System event configuration ", e);
				}
			} // end of for(SystemEvent systemEvent : systemEventList)
		} else {
			logger.debug("System event is not defined for the event configuration");
		}
	}// end of method

	/**
	 * This method is used to add event configuration of event
	 * 
	 * @param events
	 *            : Events Object
	 * @param eventConfigService
	 *            : EventFrameworkConfigService Object
	 * @param configContext
	 *            : ConfigurationContext Object
	 * @throws FeatureDeploymentServiceException
	 */
	private void loadEventConfiguration(Events events, IEventFrameworkConfigService eventConfigService,
			ConfigurationContext configContext) throws FeatureDeploymentServiceException {
		logger.debug(".loadSystemEventConfiguration of FeatureMetaInfoResourceUtil");
		if (events != null) {
			List<Event> eventList = events.getEvent();
			// add events
			for (Event event : eventList) {
				List<EventDispatcher> eventDispacherList = event.getEventDispatchers().getEventDispatcher();
				for (EventDispatcher eventDispacher : eventDispacherList) {
					String transformationtype = eventDispacher.getEventTransformation().getType();
					if (transformationtype.equalsIgnoreCase("XML-XSLT")) {
						logger.debug("event for which xslt defined : " + event.getId());
						String xslName = eventDispacher.getEventTransformation().getXSLTName();
						URL xslUrl = FeatureMetaInfoResourceUtil.class.getClassLoader().getResource(xslName);
						logger.debug("xsl url : " + xslUrl + " for xslt name : " + xslName);
						String xslAsString = convertXmlToString(xslUrl, xslName);
						logger.debug("xslt As String : " + xslAsString);
						eventDispacher.getEventTransformation().setXsltAsString(xslAsString);
					}
				}
				try {
					eventConfigService.addEventFrameworkConfiguration(configContext, event);
				} catch (EventFrameworkConfigurationException e) {
					throw new FeatureDeploymentServiceException("Error in adding  event configuration ", e);

				}

			}
		} else {
			logger.debug("events is not defined for the event configuration");
		}
	}// end of method

	private void loadPermastoreResourceInFeatureMetaInfo(List<PermaStoreConfiguration> permastoreConfigList,
			String featureGroupName, String featureName, String implName, String vendorName, String version)
			throws FeatureDeploymentServiceException {
		logger.debug(".getPermastoreResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		for (PermaStoreConfiguration permastore : permastoreConfigList) {
			String permastoreResourceName = permastore.getResourceName();
			URL permastoreResourceUrl = FeatureMetaInfoResourceUtil.class.getClassLoader()
					.getResource(permastoreResourceName);
			if (permastoreResourceUrl != null) {
				String permastoreSourceAsString = convertXmlToString(permastoreResourceUrl,
						permastore.getResourceName());
				if (permastoreSourceAsString != null) {
					PermaStoreConfigXMLParser parmastoreConfigParser = new PermaStoreConfigXMLParser();
					PermaStoreConfigurations permastorConfig = null;
					try {
						permastorConfig = parmastoreConfigParser.marshallConfigXMLtoObject(permastoreSourceAsString);
						List<com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration> permastoreConfigList1 = permastorConfig
								.getPermaStoreConfiguration();
						for (com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration permaStoreConfiguration : permastoreConfigList1) {
							String configname = permaStoreConfiguration.getName();
							IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
							ConfigurationContext configurationContext = null;
							RequestContext requestContext = null;
							boolean isGlobal = permaStoreConfiguration.isIsGlobal();
							if (isGlobal) {
								logger.debug("permastore config is marked as global");
								requestContext = new RequestContext(FeatureMetaInfoConstant.DEFAULT_TENANT_KEY,
										FeatureMetaInfoConstant.DEFAULT_SITE_KEY, featureGroupName, featureName,
										implName, vendorName, version);
								configurationContext = new ConfigurationContext(
										FeatureMetaInfoConstant.DEFAULT_TENANT_KEY,
										FeatureMetaInfoConstant.DEFAULT_SITE_KEY, featureGroupName, featureName,
										implName, vendorName, version);
							} else {
								logger.debug("permastore config is not global");
								requestContext = new RequestContext(tenant, siteId, featureGroupName, featureName,
										implName, vendorName, version);
								configurationContext = new ConfigurationContext(tenant, siteId, featureGroupName,
										featureName, implName, vendorName, version);
							}
							try {
								boolean isExist = psConfigService.checkPermaStoreConfigarationExistOrNot(
										configurationContext, permaStoreConfiguration.getName());
								if (!isExist) {
									psConfigService.addPermaStoreConfiguration(configurationContext,
											permaStoreConfiguration);

								} else {
									logger.debug("Permastore configuration for : " + configname
											+ "already exist for featuregroup : " + featureGroupName + " and feature : "
											+ featureName + " in db");
								}

							} catch (PermaStoreConfigurationException | PermaStoreConfigRequestException e) {
								throw new FeatureDeploymentServiceException(
										"error in loading the PermastoreConfiguration ", e);

							}
						} // end of if(builderType.equalsIgnoreCase("CUSTOM"))
					} catch (PermaStoreConfigParserException e) {
						throw new FeatureDeploymentServiceException(
								"Unable to parse permastore file : " + permastore.getResourceName());
					}
				} // end of for
			} else {
				logger.debug("No permastore config xml defined for : " + permastoreResourceName);
			}
		}
		logger.debug("exiting getPermastoreResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");

	}// end of method

	/**
	 * This method is used to load the the policy configuration in db and cache
	 * 
	 * @param policyConfigList
	 *            : List<PolicyConfiguration> Object
	 * @param featureGroupName
	 *            : feature group name
	 * @param featureName
	 *            : feature name
	 * @throws FeatureDeploymentServiceException
	 */
	private void loadPolicyResourceInFeatureMetaInfo(List<PolicyConfiguration> policyConfigList,
			String featureGroupName, String featureName, String implName, String vendorName, String version)
			throws FeatureDeploymentServiceException {
		logger.debug(".getPolicyResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		for (PolicyConfiguration policyconfig : policyConfigList) {
			String policyconfigResourceName = policyconfig.getResourceName();
			URL policyResourceUrl = FeatureMetaInfoResourceUtil.class.getClassLoader()
					.getResource(policyconfigResourceName);
			if (policyResourceUrl != null) {
				String policyconfigSourceAsString = convertXmlToString(policyResourceUrl,
						policyconfig.getResourceName());
				if (policyconfigSourceAsString != null) {
					PolicyConfigXMLParser policyParser = new PolicyConfigXMLParser();
					try {
						Policies policies = policyParser.marshallConfigXMLtoObject(policyconfigSourceAsString);
						List<Policy> policyList = policies.getPolicy();
						if (!(policyList.isEmpty()) || policyList != null) {
							for (Policy policy : policyList) {
								logger.debug("policy related info : " + policy.getPolicyName());
								IPolicyConfigurationService policyConfigService = new PolicyConfigurationService();
								PolicyRequestContext policyRequestContext = new PolicyRequestContext(tenant, siteId,
										featureGroupName, featureName, implName, vendorName, version);
								ConfigurationContext configurationContext = new ConfigurationContext(tenant, siteId,
										featureGroupName, featureName, implName, vendorName, version);
								try {
									boolean isExist = policyConfigService
											.checkPolicyExistInDbAndCache(configurationContext, policy.getPolicyName());
									if (!isExist) {
										policyConfigService.addPolicyConfiguration(configurationContext, policy);
									} else {
										logger.debug("Policy configuration for : " + policy.getPolicyName()
												+ "already exist for featuregroup : " + featureGroupName
												+ " and feature : " + featureName + " in db");
									}
								} catch (PolicyConfigurationException | PolicyRequestException e) {
									throw new FeatureDeploymentServiceException(
											"error in loading the policyConfiguration for policy = "
													+ policy.getPolicyName(),
											e);
								}
							} // end of for loop
						}
					} catch (PolicyConfigXMLParserException e) {
						throw new FeatureDeploymentServiceException(
								"Unable to parse policy file : " + policyconfig.getResourceName());
					}

				} // end of if(policyconfigSourceAsString !=null)
			} else {
				logger.debug("No policy config defined for policy : " + policyconfigResourceName);
			}
		}
		logger.debug("exiting getPolicyResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");

	}// end of method

	/**
	 * This method is used to load the the feature configuration in db and cache
	 * 
	 * @param featureImplList
	 *            : List<FeatureImplementation> Object
	 * @param featureGroupName
	 *            : feature group name
	 * @param featureName
	 *            : feature Name
	 * @throws FeatureDeploymentServiceException
	 */
	private void loadFeatureResourceInFeatureMetaInfo(List<FeatureImplementation> featureImplList,
			String featureGroupName, String featureName, String implName, String vendor, String version,
			String provider) throws FeatureDeploymentServiceException {
		logger.debug(".getFeatureResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		/*
		 * boolean isPrimary=false; if(featureImplList.size()==1){
		 * isPrimary=true; }
		 */
		for (FeatureImplementation featureImpl : featureImplList) {
			String featureImplResourceName = featureImpl.getResourceName();
			URL featureImplResourceUrl = FeatureMetaInfoResourceUtil.class.getClassLoader()
					.getResource(featureImplResourceName);
			if (featureImplResourceUrl != null) {
				String featureImplSourceAsString = convertXmlToString(featureImplResourceUrl,
						featureImpl.getResourceName());
				logger.debug("feature as String : " + featureImplSourceAsString);
				if (featureImplSourceAsString != null) {
					logger.debug(".feature as string is not null");
					FeatureConfigXMLParser featureparser = new FeatureConfigXMLParser();
					FeaturesServiceInfo feaureServiceInfo = null;
					try {
						feaureServiceInfo = featureparser.marshallConfigXMLtoObject(featureImplSourceAsString);
					} catch (FeatureConfigParserException e) {
						throw new FeatureDeploymentServiceException(
								"Unable to parse feature file : " + featureImpl.getResourceName());
					}
					// #TODO we have to convert it to a List implementation
					com.attunedlabs.feature.jaxb.Feature feature1 = feaureServiceInfo.getFeatures().getFeature();
					feature1.setImplementationName(implName);
					if (provider == null) {
						provider = "NA";
					} else {
						feature1.setProvider(provider);
					}
					logger.debug("feature related info : " + feature1.getFeatureName());
					IFeatureConfigurationService featureConfigService = new FeatureConfigurationService();
					FeatureConfigRequestContext requestContext;
					if (feature1.getProvider() != null) {
						logger.debug("inside if loadFeatureResourceInFeatureMetaInfo");
						requestContext = new FeatureConfigRequestContext(tenant, siteId, featureGroupName, featureName,
								implName, vendor, version, feature1.getProvider());
						logger.debug("requestContext inside loadFeatureResourceInFeatureMetaInfo : " + requestContext);
					} else {
						logger.debug("inside else loadFeatureResourceInFeatureMetaInfo");
						requestContext = new FeatureConfigRequestContext(tenant, siteId, featureGroupName, featureName,
								implName, vendor, version);
						logger.debug("requestContext inside loadFeatureResourceInFeatureMetaInfo : " + requestContext);
					}
					ConfigurationContext configurationContext = requestContext.getConfigurationContext();
					logger.debug("configurationContext inside loadFeatureResourceInFeatureMetaInfo : "
							+ configurationContext);
					try {
						boolean isExist = featureConfigService.checkFeatureExistInDBAndCache(configurationContext,
								feature1.getFeatureName());
						if (!isExist) {
							addFeatureInFeatureDeployment(configurationContext);
							featureConfigService.addFeatureConfiguration(configurationContext, feature1);
						} else {
							logger.debug("feature configuration for : " + feature1.getFeatureName()
									+ "already exist for featuregroup : " + featureGroupName + " and feature : "
									+ featureName + ", impl name : " + implName + " in db");
						}
					} catch (FeatureConfigurationException | FeatureConfigRequestException
							| FeatureDeploymentServiceException e) {
						throw new FeatureDeploymentServiceException(
								"error in loading the feature Configuration for feature = " + feature1.getFeatureName(),
								e);
					}
				} // end of for loop
			} // end of if(featureImplSourceAsString!=null)
		}
		logger.debug("exiting getFeatureResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
	}// end of method

	private void addFeatureInFeatureDeployment(ConfigurationContext configurationContext)
			throws FeatureDeploymentServiceException {
		logger.debug(".addFeatureInFeatureDeployment method of FeatureMetaInfoResourceUtil ");
		IFeatureDeployment featureDeployment = new FeatureDeploymentService();
		boolean isAlreadyDeployed = featureDeployment.checkIfFeatureIsAlreadyDeployed(configurationContext);
		if (isAlreadyDeployed) {
			featureDeployment.addFeatureDeployement(configurationContext, true, false, true);
		} else {
			featureDeployment.addFeatureDeployement(configurationContext, true, true, true);
		}
	}

	/**
	 * this is to load the pipeConfigurationfrom the resources
	 * 
	 * @param listOfPipeConfigs
	 * @param featureGroupName
	 * @param name
	 * @param vendorName
	 * @param vendorVersion
	 * @throws FeatureDeploymentServiceException
	 */
	private void loadPipeConfigResourcesInFeatureMetaInfo(List<PipeConfiguration> listOfPipeConfigs,
			String featureGroupName, String featureName, String implName, String vendorName, String vendorVersion)
			throws FeatureDeploymentServiceException {
		for (PipeConfiguration pipeConfiguration : listOfPipeConfigs) {
			String pipeConfigResourceName = pipeConfiguration.getResourceName();
			logger.debug("Loading IntegrationpipeConfiguration with name: " + pipeConfigResourceName);
			URL pipeConfigResourceUrl = FeatureMetaInfoResourceUtil.class.getClassLoader()
					.getResource(pipeConfigResourceName);
			if (pipeConfigResourceUrl != null) {
				String pipeConfigasSring = convertXmlToString(pipeConfigResourceUrl, pipeConfigResourceName);
				if (pipeConfigasSring != null) {
					IntegrationPipelineConfigXmlParser pipelineConfigXmlParser = new IntegrationPipelineConfigXmlParser();
					IIntegrationPipeLineConfigurationService pipeLineConfigurationService = new IntegrationPipelineConfigurationService();
					ConfigurationContext configContext = null;
					try {
						IntegrationPipes pipes = pipelineConfigXmlParser.unmarshallConfigXMLtoObject(pipeConfigasSring);
						configContext = new ConfigurationContext(tenant, siteId, featureGroupName, featureName,
								implName, vendorName, vendorVersion);
						for (IntegrationPipe pipe : pipes.getIntegrationPipe()) {
							try {
								boolean isExist = pipeLineConfigurationService
										.checkIntegrationPipelineConfigExistOrNot(configContext, pipe.getName());
								if (!isExist) {
									pipeLineConfigurationService.addIntegrationPipelineConfiguration(configContext,
											pipe);
								} else {
									logger.debug("pipeline configuration for : " + pipeConfiguration.getResourceName()
											+ "already exist for featuregroup : " + featureGroupName + " and feature : "
											+ featureName + ", impl name : " + implName + " in db");
								}
							} catch (IntegrationPipelineConfigException e) {
								throw new FeatureDeploymentServiceException(
										"Unable to load IntegrationPipeConfiguration to the database..", e);
							}
						}
					} catch (IntegrationPipelineConfigParserException e) {
						throw new FeatureDeploymentServiceException(
								"Unable to add Integrationconfiguration file for feature group :  " + featureGroupName
										+ ", featureName : " + featureName + ", with pipe data : " + configContext,
								e);
					}
				}
			}
		}
	}

	/**
	 * Configures the from and to url paths form UIConfig object
	 * 
	 * @param uiConfig
	 * @param feature
	 * @param featureGroupName
	 * @throws FeatureDeploymentServiceException
	 */
	private void configureUI(UIConfig uiConfig, Feature feature, String featureGroupName)
			throws FeatureDeploymentServiceException {

		if (uiConfig != null) {
			String uiConfigXml = "";
			if (uiConfig != null) {
				try {
					uiConfigXml = getUIConfigAsString(uiConfig);
					logger.debug(".configureUI(), the UIConfig: " + uiConfigXml);

					String tenantID = LeapHeaderConstant.tenant;
					String siteID = LeapHeaderConstant.site;

					ConfigurationContext configurationContext = new ConfigurationContext(tenantID, siteID,
							featureGroupName, feature.getName(), feature.getImplementationName(),
							feature.getVendorName(), feature.getVendorVersion());

					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					InputSource is = new InputSource(new StringReader(uiConfigXml));
					Document doc = builder.parse(is);

					String filePath = "";
					NodeList nList = doc.getElementsByTagName("FormFlow");
					for (int temp = 0; temp < nList.getLength(); temp++) {
						Node nNode = nList.item(temp);
						logger.debug("Current Element :" + nNode.getNodeName());
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement = (Element) nNode;
							logger.debug("path : " + eElement.getAttribute("path"));
							filePath = eElement.getAttribute("path");
							addAllFilesFromLocalToZNode(configurationContext, path, filePath);
						}
					}
				} catch (JAXBException | IOException | ParserConfigurationException | SAXException e) {
					throw new FeatureDeploymentServiceException("Unable to configure uiConfig as string ", e);
				}
			}
		}
	}

	/**
	 * method to get the defined UIConfig in featureMetaInfo.xml as string
	 * 
	 * @param uiConfig
	 * @return
	 * @throws JAXBException
	 */
	private String getUIConfigAsString(UIConfig uiConfig) throws JAXBException {

		StringWriter xmlOut = new StringWriter();
		JAXBContext contextObj = JAXBContext.newInstance(UIConfig.class);
		Marshaller formMarshallerObj = contextObj.createMarshaller();
		formMarshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		formMarshallerObj.marshal(uiConfig, xmlOut);

		logger.debug(".getUIConfigAsString(), the UIConfig : " + xmlOut);

		return xmlOut.toString();
	}

	/**
	 * 
	 * @param configCtx
	 * @param baseDir
	 * @throws FeatureDeploymentServiceException
	 * @throws UtilityException
	 */
	public void addAllFilesFromLocalToZNode(ConfigurationContext configCtx, String baseDir, String filePath)
			throws FeatureDeploymentServiceException {
		logger.debug(".addAllFilesFromLocalToZNode()...");
		nullValueChecking(configCtx);
		String dirPath = baseDir + "/" + obtainPath(configCtx);
		logger.debug("dirPath : " + dirPath);
		try {
			addingAllFilesFromLocal(configCtx, dirPath, filePath);
		} catch (StaticConfigDuplicateNameofFileException | InvalidFilePathException e) {
			e.printStackTrace();
		}
	}

	/**
	 * adding the files present in local present in directory formed based upon
	 * context object and baseDir.
	 * 
	 * @param configCtx
	 * @param dirPath
	 * @throws FeatureDeploymentServiceException
	 * @throws StaticConfigDuplicateNameofFileException
	 * @throws InvalidFilePathException
	 */
	private void addingAllFilesFromLocal(ConfigurationContext configCtx, String dirPath, String filepath)
			throws FeatureDeploymentServiceException, StaticConfigDuplicateNameofFileException,
			InvalidFilePathException {
		logger.debug(".recursiveAddingFilesFromLocal() ...");
		IStaticConfigurationService instance = null;
		try {
			instance = StaticConfigurationFactory.getFilemanagerInstance();
		} catch (InstantiationException | IllegalAccessException | StaticConfigInitializationException e1) {
			e1.printStackTrace();
		}
		if (instance instanceof ZookeeperFilemanagementServiceImpl) {

			LocalfileUtil localfileUtil = new LocalfileUtil();
			File file = new File(dirPath + "/" + filepath);
			logger.debug("file : " + file.toString().replaceAll("\\\\", "/"));
			String filePath = null;
			String fileData = null;
			try {
				FileReader freader = new FileReader(file.toString().replaceAll("\\\\", "/"));
				BufferedReader br = new BufferedReader(freader);
				String content;
				StringBuffer buffer = new StringBuffer();
				while ((content = br.readLine()) != null) {
					buffer.append(content);
					fileData = buffer.toString();
					// logger.debug("fileData : " + fileData);
				}
				filePath = removeExtraPath(file.getPath());
				logger.debug("removed filePath : " + filePath);
				boolean existsInlocal = localfileUtil.checkFileExists(dirPath + "/" + filepath);
				instance.addStaticConfiguration(configCtx, filePath, fileData);
				br.close();
			} catch (NullPointerException | IOException | StaticConfigInitializationException | AddStaticConfigException
					| AccessProtectionException e) {
				throw new FeatureDeploymentServiceException("cannot find the file in local no such directory present "
						+ file + " or templates folder might be empty", e);
			}
		}
	}

	/**
	 * removes the extra and unneccesary path from the provided path and takes
	 * the path only above the template also replaces the backslash in the file
	 * by forward slash
	 * 
	 * @param fileName
	 * @return filename
	 */
	private static String removeExtraPath(String fileName) {
		if (fileName.contains("templates")) {
			int tempIndex = (fileName.indexOf("templates"));
			fileName = fileName.substring(tempIndex).trim();
		}
		if (fileName.contains("/")) {
			logger.debug("fileName contains before :  " + fileName);
			fileName = fileName.replace("/", "\\");
			logger.debug("fileName contains after  : " + fileName);
		}
		return fileName;
	}

	/**
	 * obtains the path based upon the cTx provided. returns the same path.
	 * 
	 * @param ctx
	 * @return pathObtained
	 */
	public static String obtainPath(Object ctx) {
		String pathObtained = null;
		if (ctx instanceof ConfigurationContext) {
			ConfigurationContext configCtx = (ConfigurationContext) ctx;
			pathObtained = "/" + configCtx.getTenantId() + "/" + configCtx.getSiteId() + "/"
					+ configCtx.getFeatureGroup() + "/" + configCtx.getFeatureName() + "/"
					+ configCtx.getImplementationName() + "/" + configCtx.getVendorName() + "/"
					+ configCtx.getVersion();
		} else if (ctx instanceof RequestContext) {
			RequestContext requestCtx = (RequestContext) ctx;
			pathObtained = "/" + requestCtx.getTenantId() + "/" + requestCtx.getSiteId() + "/"
					+ requestCtx.getFeatureGroup() + "/" + requestCtx.getFeatureName() + "/"
					+ requestCtx.getImplementationName() + "/" + requestCtx.getVendor() + "/" + requestCtx.getVersion();
		}
		return pathObtained;
	}

	/**
	 * this method checks the null value is present in any field of object.
	 * throws Exception if present
	 * 
	 * @param ctx
	 * @throws UtilityException
	 * @throws FeatureDeploymentServiceException
	 * @throws NullValueException
	 */
	private static void nullValueChecking(Object ctx) throws FeatureDeploymentServiceException {
		logger.debug("nullValueChecking() ... for checking the fields");
		if (ctx instanceof ConfigurationContext) {
			ConfigurationContext configCtx = (ConfigurationContext) ctx;
			if (configCtx.getTenantId().equals(null) && configCtx.getSiteId().equals(null)
					&& configCtx.getFeatureGroup().equals(null) && configCtx.getFeatureName().equals(null)
					&& configCtx.getImplementationName().equals(null) && configCtx.getVendorName().equals(null)
					&& configCtx.getVersion().equals(null))
				throw new FeatureDeploymentServiceException(
						"Unable to create the path  because null value found  while creating Nodes ... ");
			else
				return;
		} else if (ctx instanceof RequestContext) {
			RequestContext requestCtx = (RequestContext) ctx;
			if (requestCtx.getTenantId().equals(null) && requestCtx.getSiteId().equals(null)
					&& requestCtx.getFeatureGroup().equals(null) && requestCtx.getFeatureName().equals(null)
					&& requestCtx.getImplementationName().equals(null) && requestCtx.getVendor().equals(null)
					&& requestCtx.getVersion().equals(null))
				throw new FeatureDeploymentServiceException(
						"Unable to create the path  because null value found  while creating Nodes ... ");
			else
				return;
		}
	}

}
/*
 * for (FeatureImplementation featureImpl : featureImplList) { String
 * featureImplResourceName = featureImpl.getResourceName(); URL
 * featureImplResourceUrl = FeatureMetaInfoResourceUtil.class.getClassLoader()
 * .getResource(featureImplResourceName); if (featureImplResourceUrl != null) {
 * String featureImplSourceAsString = convertXmlToString(featureImplResourceUrl,
 * featureImpl.getResourceName()); logger.debug("feature as String : " +
 * featureImplSourceAsString); if (featureImplSourceAsString != null) {
 * logger.debug(".feature as string is not null"); FeatureConfigXMLParser
 * featureparser = new FeatureConfigXMLParser(); FeaturesServiceInfo
 * feaureServiceInfo = null; try { feaureServiceInfo =
 * featureparser.marshallConfigXMLtoObject(featureImplSourceAsString); } catch
 * (FeatureConfigParserException e) { throw new
 * FeatureDeploymentServiceException( "Unable to parse feature file : " +
 * featureImpl.getResourceName()); } // #TODO we have to convert it to a List
 * implementation com.attunedlabs.feature.jaxb.Feature feature1 =
 * feaureServiceInfo.getFeatures().getFeature();
 * feature1.setImplementationName(implName); logger.debug(
 * "feature related info : " + feature1.getFeatureName());
 * IFeatureConfigurationService featureConfigService = new
 * FeatureConfigurationService(); FeatureConfigRequestContext requestContext =
 * new FeatureConfigRequestContext( LeapHeaderConstant.tenant,
 * LeapHeaderConstant.site, featureGroupName, featureName, implName, vendor,
 * version); ConfigurationContext configurationContext =
 * requestContext.getConfigurationContext(); try { boolean isExist =
 * featureConfigService.checkFeatureExistInDBAndCache(configurationContext,
 * feature1.getFeatureName()); if (!isExist) {
 * addFeatureInFeatureDeployment(configurationContext);
 * featureConfigService.addFeatureConfiguration(configurationContext, feature1);
 * } else { logger.debug("feature configuration for : " +
 * feature1.getFeatureName() + "already exist for featuregroup : " +
 * featureGroupName + " and feature : " + featureName + ", impl name : " +
 * implName + " in db"); } } catch (FeatureConfigurationException |
 * FeatureConfigRequestException | FeatureDeploymentServiceException e) { throw
 * new FeatureDeploymentServiceException(
 * "error in loading the feature Configuration for feature = " +
 * feature1.getFeatureName(), e); } } // end of for loop } // end of
 * if(featureImplSourceAsString!=null) }// ..end of the method
 */

/*
 * try { boolean isExist =
 * featureConfigService.checkFeatureExistInDBAndCache(configurationContext,
 * feature1.getFeatureName()); if (!isExist) {
 * addFeatureInFeatureDeployment(configurationContext);
 * featureConfigService.addFeatureConfiguration(configurationContext, feature1);
 * } else { logger.debug("feature configuration for : " +
 * feature1.getFeatureName() + "already exist for featuregroup : " +
 * featureGroupName + " and feature : " + featureName + ", impl name : " +
 * implName + " in db"); } } catch (FeatureConfigurationException |
 * FeatureConfigRequestException | FeatureDeploymentServiceException e) { throw
 * new FeatureDeploymentServiceException(
 * "error in loading the feature Configuration for feature = " +
 * feature1.getFeatureName(), e); }
 */
