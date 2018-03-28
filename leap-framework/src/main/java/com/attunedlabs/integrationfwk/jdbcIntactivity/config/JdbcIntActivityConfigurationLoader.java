package com.attunedlabs.integrationfwk.jdbcIntactivity.config;

public class JdbcIntActivityConfigurationLoader {
/*	public static final String JDBCINT_ACTIVITIES_HEADER_KEY = "activitiesheader";
	public static final String JDBCINT_ACTIVITY_HEADER_KEY = "activityheader";
	private static final String ACTIVITY_CONFIG_FILE = "/activityConfig.xml";
	private static final String ACTIVITY_CONFIGS_FILE = "/activityConfigs.xml";
	private Logger logger = LoggerFactory.getLogger(JdbcIntActivityConfigurationLoader.class.getName());

	*//**
	 * called even before the executer, in order to set the jdbcIntActivity
	 * object to the exchange header
	 * 
	 * @param exchange,
	 *            is passed in order to set the multiple configurations as a
	 *            LIST in exchange Header
	 * @throws JdbcIntActivityConfigurationLoaderException
	 *//*
	public void setUnmarshalledJdbcIntActivity(Exchange exchange) throws JdbcIntActivityConfigurationLoaderException {
		logger.debug(".setUnmarshalledJdbcIntActivity()..");
		InputStream xmlConfigFile = getClass().getResourceAsStream(ACTIVITY_CONFIGS_FILE);
		String configXMLFile = null;
		try {
			configXMLFile = IOUtils.toString(xmlConfigFile, "UTF-8");
			JDBCIntActivities configObjects = unmarshalXml(configXMLFile);
			exchange.getIn().setHeader(JDBCINT_ACTIVITIES_HEADER_KEY, configObjects);
		} catch (IOException e) {
			throw new JdbcIntActivityConfigurationLoaderException(
					"Unable to load values of the configuration from - " + configXMLFile + " - to the object", e);
		}
	}// ..end of method

	*//**
	 * called in implementation route to load the configname to header by name
	 * from the list of configurations
	 * 
	 * @param exchange
	 * @param configName
	 * @throws JdbcIntActivityConfigurationLoaderException
	 *//*
	public void loadActivityConfigByName(String configName, Exchange exchange)
			throws JdbcIntActivityConfigurationLoaderException {
		logger.debug(".loadActivityConfigByName().." + configName);
		JDBCIntActivities jdbcIntActivities = (JDBCIntActivities) exchange.getIn()
				.getHeader(JdbcIntActivityConfigurationLoader.JDBCINT_ACTIVITIES_HEADER_KEY);
		List<JDBCIntActivity> listOfConfigsTosearchFor = jdbcIntActivities.getJDBCIntActivity();

		for (int i = 0; i < listOfConfigsTosearchFor.size(); i++) {
			String activityName = listOfConfigsTosearchFor.get(i).getName();
			logger.debug("Each-config-name: " + activityName + " - configNamePassed - " + configName);
			if (activityName.equals(configName)) {
				exchange.getIn().setHeader(JdbcIntActivityConfigurationLoader.JDBCINT_ACTIVITY_HEADER_KEY,
						listOfConfigsTosearchFor.get(i));
				logger.debug("Configuration foind at index: " + i);
				break;
			}
		}
	}// ..end of the method

	*//**
	 * jaxb unmarshal of the configuration xml file
	 * 
	 * @param configXMLFile
	 * @return JDBCIntActivity, object with values
	 * @throws JdbcIntActivityConfigurationLoaderException
	 *//*
	private JDBCIntActivities unmarshalXml(String configXMLFile) throws JdbcIntActivityConfigurationLoaderException {
		logger.debug(".unmarshalXml().." + configXMLFile);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(JDBCIntActivities.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));
			return (JDBCIntActivities) jaxbUnmarshaller.unmarshal(inputSourceConfigXml);
		} catch (JAXBException e) {
			throw new JdbcIntActivityConfigurationLoaderException(
					"Unable to load values of the configuration from - " + configXMLFile + " - to the object", e);
		}
	}// ..end of the method
*/}
