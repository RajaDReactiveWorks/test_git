package com.attunedlabs.featureInstaller.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.datacontext.config.DataContextConfigurationException;
import com.attunedlabs.datacontext.config.DataContextParserException;
import com.attunedlabs.datacontext.config.IDataContextConfigurationService;
import com.attunedlabs.datacontext.config.impl.DataContextConfigXMLParser;
import com.attunedlabs.datacontext.config.impl.DataContextConfigurationService;
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
import com.attunedlabs.eventframework.dispatcher.transformer.ILeapEventTransformer;
import com.attunedlabs.eventframework.jaxb.DispatchChanel;
import com.attunedlabs.eventframework.jaxb.DispatchChanels;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventDispatcher;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.EventSubscription;
import com.attunedlabs.eventframework.jaxb.EventSubscriptions;
import com.attunedlabs.eventframework.jaxb.Events;
import com.attunedlabs.eventframework.jaxb.SystemEvent;
import com.attunedlabs.eventframework.jaxb.SystemEvents;
import com.attunedlabs.feature.config.FeatureConfigParserException;
import com.attunedlabs.feature.config.FeatureConfigRequestContext;
import com.attunedlabs.feature.config.FeatureConfigRequestException;
import com.attunedlabs.feature.config.FeatureConfigurationException;
import com.attunedlabs.feature.config.IFeatureConfigurationService;
import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;
import com.attunedlabs.feature.config.impl.FeatureConfigurationService;
import com.attunedlabs.feature.config.impl.FeatureDiscoveryService;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.featuremetainfo.jaxb.DataContexts;
import com.attunedlabs.featuremetainfo.jaxb.DynaStoreConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.DynaStoreConfigurations;
import com.attunedlabs.featuremetainfo.jaxb.EventResource;
import com.attunedlabs.featuremetainfo.jaxb.EventResources;
import com.attunedlabs.featuremetainfo.jaxb.Feature;
import com.attunedlabs.featuremetainfo.jaxb.FeatureDataContexts;
import com.attunedlabs.featuremetainfo.jaxb.FeatureImplementation;
import com.attunedlabs.featuremetainfo.jaxb.FeatureImplementations;
import com.attunedlabs.featuremetainfo.jaxb.PermaStoreConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.PolicyConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.PolicyConfigurations;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.permastore.config.IPermaStoreConfigurationService;
import com.attunedlabs.permastore.config.IPermaStoreCustomCacheObjectBuilder;
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

/**
 * This class is to take all the resource defined in featureMetaInfo.xml file
 * and parse it
 * 
 * @author bizruntime
 *
 */
public class FeatureMetaInfoResourceUtil {
	final static Logger logger = LoggerFactory.getLogger(FeatureMetaInfoResourceUtil.class);

	/**
	 * This method is used to check the resource available in featureMetaInfo.xml
	 * and call method to load them
	 * 
	 * @param feature
	 *           : feature name
	 * @param featureGroupName
	 *           : feature group name
	 * @throws FeatureMetaInfoResourceException
	 */
	public void checkResourceAvailableAndload(Feature feature,Bundle bundle,String featureGroupName) throws FeatureMetaInfoResourceException {
		logger.debug(".checkResourceAvailableAndCall of FeatureMetaInfoResourceUtil");
		//check if event resource configured and then load
		EventResources eventResources=feature.getEventResources();
		checkEventResourceAndLoad(eventResources,bundle, feature, featureGroupName);
		
		//check if permastore resource configured and then load
		com.attunedlabs.featuremetainfo.jaxb.PermaStoreConfigurations permastoreConfiguration=feature.getPermaStoreConfigurations();
		checkPermastoreResourceAndLoad(permastoreConfiguration,bundle, feature, featureGroupName);
		
		//check if dynastore resource configured and then load
		DynaStoreConfigurations dynastoreConfiguration=feature.getDynaStoreConfigurations();
		checkDynastoreResourceAndLoad(dynastoreConfiguration,bundle, feature, featureGroupName);
		
		//check if policy resource configured and then load
		PolicyConfigurations policyConfiguration=feature.getPolicyConfigurations();
		checkPolicyResourceAndLoad(policyConfiguration,bundle, feature, featureGroupName);
		
		//check if featureImpl resource configured and then load
		FeatureImplementations featureImplementation=feature.getFeatureImplementations();
		checkFeatureImplResourceAndLoad(featureImplementation,bundle, feature, featureGroupName);
		
		
		// check if featureDataContext is defined or not
		FeatureDataContexts featureDataContexts = feature.getFeatureDataContexts();
		checkFeatureDataContextsResourceAndLoad(featureDataContexts,bundle, feature, featureGroupName);

	}
	
	/**
	 * This method is used to if dataContext define for featureMetaInfo, if Specified then call method add it into db and datagrid
	 * @param featureDataContexts : FeatureDataContexts Object
	 * @param bundle : Bundle Object
	 * @param feature : feature name in String
	 * @param featureGroupName : feature group name in String
	 * @throws FeatureMetaInfoResourceException 
	 */
	private void checkFeatureDataContextsResourceAndLoad(FeatureDataContexts featureDataContexts,Bundle bundle, Feature feature,
			String featureGroupName) throws FeatureMetaInfoResourceException {
		if(featureDataContexts !=null){
				List<DataContexts> dataContextsList=featureDataContexts.getDataContexts();
				if(dataContextsList !=null){
				loadFeatureDataContextResourceInFeatureMetaInfo(dataContextsList,bundle, featureGroupName, feature.getName(),feature.getVendorName(),feature.getVendorVersion());
				}else{
					logger.debug("No DataContexts is defined in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature+" but empty");

				}
			}else{
				logger.debug("No DataContexts configured in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature);
			}
		
	}

	/**
	 * This method is used to add featureDataContext into db and datagrid
	 * @param dataContextsList : List of DataContext define in featuremetainfo
	 * @param bundle : Bundle Object
	 * @param featureGroupName : feature group in String
	 * @param name : feature name in String
	 * @param vendorName : vendor name in String
	 * @param vendorVersion : Vendor Version in String
	 * @throws FeatureMetaInfoResourceException : Unable to parse/add FeatureDataContext
	 */
	private void loadFeatureDataContextResourceInFeatureMetaInfo(List<DataContexts> dataContextsList, Bundle bundle,
			String featureGroupName, String name, String vendorName, String vendorVersion) throws FeatureMetaInfoResourceException {
		logger.debug(".loadFeatureDataContextResourceInFeatureMetaInfo method of FeatureMetaInfoResourceUtil");
		for(DataContexts dataContexts:dataContextsList){
			String dataContextResourceName="/"+dataContexts.getResourceName();
			logger.debug("dataContexts resource name : "+dataContextResourceName);
			URL dataContextResourceUrl =bundle.getResource(dataContextResourceName);
			if(dataContextResourceUrl !=null){
				String dataContextAsSourceAsString=null;
				try {
					dataContextAsSourceAsString=convertXmlToString(dataContextResourceUrl, bundle, dataContextResourceName);
					DataContextConfigXMLParser dataContextXmlParser=new DataContextConfigXMLParser();
					IDataContextConfigurationService dataContextConfigService=new DataContextConfigurationService();
					ConfigurationContext configContext=null;
					try {
						FeatureDataContext featureDataContext=dataContextXmlParser.marshallXMLtoObject(dataContextAsSourceAsString);
						configContext = new ConfigurationContext(LeapHeaderConstant.tenant, LeapHeaderConstant.site,
								featureGroupName, name,vendorName,vendorVersion);
						try {
							dataContextConfigService.addDataContext(configContext, featureDataContext);
						} catch (DataContextConfigurationException e) {
							throw new FeatureMetaInfoResourceException("Unable to add configuration file "+dataContextResourceName+" for feature group :  "+featureGroupName+", feature name : "+name+", with context data : "+configContext);

						}
					} catch (DataContextParserException e) {
						throw new FeatureMetaInfoResourceException("Unable to parse featuredatacontext configuration file"+dataContextResourceName+" for feature group :  "+featureGroupName+", feature name : "+name);

					}
				} catch (FeatureMetaInfoResourceException e) {
					throw new FeatureMetaInfoResourceException("Unable to get string formate of featuredatacontext config for file  "+dataContextResourceName+" for feature group :  "+featureGroupName+", feature name : "+name);
				}
			}else{
				logger.debug("No datacontexts config xml exist with name : "+dataContextResourceName);
			}	
		}
	}//end of loadFeatureDataContextResourceInFeatureMetaInfo method


	/**
	 * This method is used to check and load event resource defined in featureMetaInfo.xml
	 * @param eventResources : EventResources Object of FeatureMetaInfo
	 * @param bundle : Bundle Object
	 * @param feature : feature Name
	 * @param featureGroupName : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void checkEventResourceAndLoad(EventResources eventResources,Bundle bundle,Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException{
		if(eventResources !=null){
			List<EventResource> eventResourceList = eventResources.getEventResource();
			if (eventResourceList != null) {
				loadEventResourcesInFeatureMetaInfo(eventResourceList,bundle, featureGroupName, feature.getName());
			}else{
				logger.debug("No EventResource is defined in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature+" but empty");
			}
			}else{
				logger.debug("No EventResource configured in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature);
			}
	}
	
	/**
	 * This method is used to check and load permastore resource defined in featureMetaInfo.xml
	 * @param permastoreConfiguration : PermaStoreConfigurations Object of FeatureMetaInfo
	 * @param bundle : Bundle Object
	 * @param feature : feature Name
	 * @param featureGroupName : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void checkPermastoreResourceAndLoad(com.attunedlabs.featuremetainfo.jaxb.PermaStoreConfigurations permastoreConfiguration,Bundle bundle,Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException{
		if(permastoreConfiguration !=null){
			List<PermaStoreConfiguration> permastoreConfigList = permastoreConfiguration.getPermaStoreConfiguration();
			if (permastoreConfigList != null) {
				loadPermastoreResourceInFeatureMetaInfo(permastoreConfigList,bundle, featureGroupName, feature.getName(),feature.getVendorName(),feature.getVendorVersion());
			}else{
				logger.debug("No PermastoreResource is defined in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature+" but empty");
			}
			}else{
				logger.debug("No PermastoreResource configured in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature);
			}
	}
	
	
	/**
	 * This method is used to check and load PolicyResource resource defined in featureMetaInfo.xml
	 * @param policyConfiguration : PolicyConfigurations Object of FeatureMetaInfo
	 * @param bundle : Bundle Object
	 * @param feature : feature Name
	 * @param featureGroupName : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void checkPolicyResourceAndLoad(PolicyConfigurations policyConfiguration,Bundle bundle,Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException{
		if(policyConfiguration !=null){
			List<PolicyConfiguration> policyConfigList = policyConfiguration.getPolicyConfiguration();
			if (policyConfigList != null) {
				loadPolicyResourceInFeatureMetaInfo(policyConfigList,bundle, featureGroupName, feature.getName(),feature.getVendorName(),feature.getVendorVersion());
			}else{
				logger.debug("No PolicyResource is defined in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature+" but empty");
			}
			}else{
				logger.debug("No PolicyResource configured in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature);
			}
	}
	
	/**
	 * This method is used to check and load FeatureImplResource resource defined in featureMetaInfo.xml
	 * @param FeatureImplementations : FeatureImplementations Object of FeatureMetaInfo
	 * @param bundle : Bundle Object
	 * @param feature : feature Name
	 * @param featureGroupName : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void checkFeatureImplResourceAndLoad(FeatureImplementations featureImplementations,Bundle bundle,Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException{
		if(featureImplementations !=null){
			List<FeatureImplementation> featureImplList = featureImplementations.getFeatureImplementation();
			if (featureImplList != null) {
				loadFeatureResourceInFeatureMetaInfo(featureImplList,bundle, featureGroupName, feature.getName(),feature.getVendorName(),feature.getVendorVersion());
			}else{
				logger.debug("No FeatureImplResource is defined in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature+" but empty");
			}
			}else{
				logger.debug("No FeatureImplResource configured in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature);
			}
	}
	
	
	/**
	 * This method is used to check and load DynastoreResource resource defined in featureMetaInfo.xml
	 * @param dynastoreConfiguration : DynaStoreConfigurations Object of FeatureMetaInfo
	 * @param bundle : Bundle Object
	 * @param feature : feature Name
	 * @param featureGroupName : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void checkDynastoreResourceAndLoad(DynaStoreConfigurations dynastoreConfiguration,Bundle bundle,Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException{
		if(dynastoreConfiguration !=null){
			List<DynaStoreConfiguration> dynaStoreConfigList=feature.getDynaStoreConfigurations().getDynaStoreConfiguration();
			if (dynaStoreConfigList != null) {
				loadDynastoreResourceInFeatureMetaInfo(dynaStoreConfigList,bundle, featureGroupName, feature.getName(),feature.getVendorName(),feature.getVendorVersion());
			}else{
				logger.debug("No DynastoreResource is defined in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature+" but empty");
			}
			}else{
				logger.debug("No DynastoreResource configured in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature);
			}
	}

	/**
	 * This method is used to load dynastore configuration
	 * @param dynaStoreConfigList : List<DynaStoreConfiguration>
	 * @param bundle : Bundle object
	 * @param featureGroupName : feature group name
	 * @param name : feature name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void loadDynastoreResourceInFeatureMetaInfo(List<DynaStoreConfiguration> dynaStoreConfigList, Bundle bundle,String featureGroupName, String name,String vendor,String version) throws FeatureMetaInfoResourceException {
		logger.debug(". getDynastoreResourceInFeatureMetaInfo() of FeatureMetaInfoResourceUtil");
		for(DynaStoreConfiguration dynastoreconfig:dynaStoreConfigList){
			String dynaResourceName="/"+dynastoreconfig.getResourceName();
			logger.debug("dynaResourceName : " + dynaResourceName);
			URL dynaResourceUrl =bundle.getResource(dynaResourceName);
			if(dynaResourceUrl !=null){
				String dynastoreAsSourceAsString=null;
				try {
					dynastoreAsSourceAsString = convertXmlToString(dynaResourceUrl,bundle,dynaResourceName);
				} catch (FeatureMetaInfoResourceException e1) {
					throw new FeatureMetaInfoResourceException("Unable to get string formate of dynastore config for file  "+dynaResourceName+" for feature group :  "+featureGroupName+", feature name : "+name);
				}
				if(dynastoreAsSourceAsString !=null){
				DynaStoreConfigXmlParser parser = new DynaStoreConfigXmlParser();
				IDynaStoreConfigurationService iDynaStoreConfigurationService=null;
				ConfigurationContext configContext=null;
				try {
					DynastoreConfigurations dynastoreConfigurations =parser.marshallConfigXMLtoObject(dynastoreAsSourceAsString);
					 configContext = new ConfigurationContext(LeapHeaderConstant.tenant, LeapHeaderConstant.site,
							featureGroupName, name,vendor,version);
					iDynaStoreConfigurationService= new DynaStoreConfigurationService();
					List<DynastoreConfiguration> dynastoreConfigList=dynastoreConfigurations.getDynastoreConfiguration();
					for(DynastoreConfiguration dynaConfig:dynastoreConfigList){
					try {
						iDynaStoreConfigurationService.addDynaStoreConfiguration(configContext, dynaConfig);
					} catch (DynaStoreConfigurationException e) {
						throw new FeatureMetaInfoResourceException("Unable to add configuration file "+dynaResourceName+" for feature group :  "+featureGroupName+", feature name : "+name+", with context data : "+configContext);
					}
					}//end of for
				} catch (DynaStoreConfigParserException e) {
					throw new FeatureMetaInfoResourceException("Unable to parse dynastore configuration file"+dynaResourceName+" for feature group :  "+featureGroupName+", feature name : "+name);
				}				
				}//end of if(dynastoreAsSourceAsString !=null)
				}else{
				logger.debug("No dynastore config xml exist with name : "+dynaResourceName);
			}			
		}//end of for		
		logger.debug("exiting getDynastoreResourceInFeatureMetaInfo() of FeatureMetaInfoResourceUtil");

	}

	/**
	 * This method is used to load event configuration
	 * @param eventResourceList : List<EventResource>
	 * @param bundle : Bundle Object
	 * @param featureGroupName : feature group name
	 * @param featureName : feature name
	 * @throws FeatureMetaInfoResourceException
	 */
	public void loadEventResourcesInFeatureMetaInfo(List<EventResource> eventResourceList, Bundle bundle, String featureGroupName, String featureName)
			throws FeatureMetaInfoResourceException {
		logger.debug(".getEventResourcesInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		for (EventResource eventresource : eventResourceList) {
			String eventResourceName = "/" + eventresource.getResourceName();
			URL eventResourceUrl = bundle.getResource(eventResourceName);
			String eventSourceAsString = convertXmlToString(eventResourceUrl, bundle, eventresource.getResourceName());
			if (eventresource != null) {
				EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
				EventFramework eventFrameworkConfig = null;
				IEventFrameworkConfigService eventConfigService = new EventFrameworkConfigService();
				try {
					eventFrameworkConfig = parser.marshallConfigXMLtoObject(eventSourceAsString);
					// prepare the configcontext for eventing
					ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant, LeapHeaderConstant.site,
							featureGroupName, featureName);
					//check dispatcher defined or not and then load configuration
					DispatchChanels dispatcherChanel=eventFrameworkConfig.getDispatchChanels();
					loadEventChanelConfiguration(dispatcherChanel,eventConfigService,configContext);
					
					//check SystemEvents defined or not and then load configuration
					SystemEvents systemEvent=eventFrameworkConfig.getSystemEvents();
					loadSystemEventConfiguration(systemEvent,eventConfigService,configContext,bundle);
					
					//check Events defined or not and then load configuration
					Events events=eventFrameworkConfig.getEvents();
					loadEventConfiguration(events,eventConfigService,configContext,bundle);	
					
					//check event subscription defined or not and then load configuration
					EventSubscriptions eventSusbscriptions=eventFrameworkConfig.getEventSubscriptions();
					loadEventSubscriptionConfiguration(eventSusbscriptions,eventConfigService,configContext);
				} catch (EventFrameworkConfigParserException e) {
					throw new FeatureMetaInfoResourceException("Unable to parse event file : " + eventresource.getResourceName() + " for bundle id : "
							+ bundle.getBundleId() + " and symbolic name : " + bundle.getSymbolicName());				}	

			}// end of if(eventresource !=null)
			logger.debug("exiting getEventResourcesInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		}// end of for
	}// end of method
	
	/**
	 * This method is used to add channel configuration of event
	 * @param dispatcherChanel : DispatchChanels Object
	 * @param eventConfigService : EventFrameworkConfigService Object
	 * @param configContext : ConfigurationContext Object
	 * @throws FeatureMetaInfoResourceException
	 */
	private void loadEventChanelConfiguration(DispatchChanels dispatcherChanel,IEventFrameworkConfigService eventConfigService,ConfigurationContext configContext) throws FeatureMetaInfoResourceException{
		logger.debug(".loadEventChanelConfiguration of FeatureMetaInfoResourceUtil");
		if(dispatcherChanel !=null){
		List<DispatchChanel> disChanelList = dispatcherChanel.getDispatchChanel();
		// addchanel init cache
		for (DispatchChanel disChanelConfig : disChanelList) {
			try {
				eventConfigService.addEventFrameworkConfiguration(configContext, disChanelConfig);
			} catch (EventFrameworkConfigurationException e) {
				throw new FeatureMetaInfoResourceException("Error in adding channel configuration ", e);

			}
		}//end of for loop
		}else{
			logger.debug("dispatcher channel is not defined for the event configuration");
		}
	}//end of method
	
	
	/**
	 * This method is used to add system event configuration of event
	 * @param systemEvent : SystemEvents Object
	 * @param eventConfigService : EventFrameworkConfigService Object
	 * @param configContext : ConfigurationContext Object
	 * @param bundle : Bundle Object
	 * @throws FeatureMetaInfoResourceException
	 */
	private void loadSystemEventConfiguration(SystemEvents systemEvents,IEventFrameworkConfigService eventConfigService,ConfigurationContext configContext,Bundle bundle) throws FeatureMetaInfoResourceException{
		logger.debug(".loadSystemEventConfiguration of FeatureMetaInfoResourceUtil");
		if(systemEvents !=null){
			List<SystemEvent> systemEventList = systemEvents.getSystemEvent();
			// add system events
			for (SystemEvent systemEvent : systemEventList) {
				List<EventDispatcher> eventDispacherList = systemEvent.getEventDispatchers().getEventDispatcher();
				for (EventDispatcher eventDispacher : eventDispacherList) {
					String transformationtype = eventDispacher.getEventTransformation().getType();
					if (transformationtype.equalsIgnoreCase("CUSTOM")) {
						String fcqnTransformation = eventDispacher.getEventTransformation().getCustomTransformer().getFqcn();
						try {
							Class<?> classObj = bundle.loadClass(fcqnTransformation);
							ILeapEventTransformer leapEventTransformer = (ILeapEventTransformer) classObj.newInstance();
							Dictionary<String, String> props = new Hashtable<String, String>();
							props.put("tenant", LeapHeaderConstant.tenant);
							props.put("site", LeapHeaderConstant.site);
							props.put("featuregroup", configContext.getFeatureGroup());
							props.put("feature", configContext.getFeatureName());
							props.put("fqcnBuilder", fcqnTransformation);
							props.put("bundleid", "" + bundle.getBundleId());
							// Register a new service with the above props
							logger.debug("bundle context object : " + bundle.getBundleContext());
							bundle.getBundleContext().registerService(ILeapEventTransformer.class.getName(), leapEventTransformer, props);
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
							throw new FeatureMetaInfoResourceException("Unable to load/instantiate object for class with fully qualified name  "
									+ fcqnTransformation + " for bundle id : " + bundle.getBundleId() + " and symbolic name : "
									+ bundle.getSymbolicName());
						}
					}else if(transformationtype.equalsIgnoreCase("XML-XSLT")){
						logger.debug("event for whom xslt defined : "+systemEvent);
						String xslName=eventDispacher.getEventTransformation().getXSLTName();
						URL xslUrl = bundle.getResource(xslName);
						logger.debug("xsl url : "+xslUrl+" for xslt name : "+xslName);
						String xslAsString = convertXmlToString(xslUrl, bundle, xslName);
						logger.debug("xslt As String : "+xslAsString);
						eventDispacher.getEventTransformation().setXsltAsString(xslAsString);
					}
				}
				try {
					eventConfigService.addEventFrameworkConfiguration(configContext, systemEvent);
				} catch (EventFrameworkConfigurationException e) {
					throw new FeatureMetaInfoResourceException("Error in system Event configuration ", e);
				}

			}// end of for(SystemEvent systemEvent : systemEventList)			
		}else{
			logger.debug("System event is not defined for the event configuration");
		}
	}//end of method
	
	
	/**
	 * This method is used to add  event configuration of event
	 * @param events : Events Object
	 * @param eventConfigService : EventFrameworkConfigService Object
	 * @param configContext : ConfigurationContext Object
	 * @param bundle : Bundle Object
	 * @throws FeatureMetaInfoResourceException
	 */
	private void loadEventConfiguration(Events events,IEventFrameworkConfigService eventConfigService,ConfigurationContext configContext,Bundle bundle) throws FeatureMetaInfoResourceException{
		logger.debug(".loadSystemEventConfiguration of FeatureMetaInfoResourceUtil");
		if(events !=null){
			List<Event> eventList =events.getEvent();
			// add events
			for (Event event : eventList) {
				List<EventDispatcher> eventDispacherList = event.getEventDispatchers().getEventDispatcher();
				for (EventDispatcher eventDispacher : eventDispacherList) {
					String transformationtype = eventDispacher.getEventTransformation().getType();
					if (transformationtype.equalsIgnoreCase("CUSTOM")) {
						String fcqnTransformation = eventDispacher.getEventTransformation().getCustomTransformer().getFqcn();
						try {
							Class<?> classObj = bundle.loadClass(fcqnTransformation);
							ILeapEventTransformer leapEventTransformer = (ILeapEventTransformer) classObj.newInstance();
							Dictionary<String, String> props = new Hashtable<String, String>();
							props.put("tenant", LeapHeaderConstant.tenant);
							props.put("site", LeapHeaderConstant.site);
							props.put("featuregroup", configContext.getFeatureGroup());
							props.put("feature", configContext.getFeatureName());
							props.put("fqcnBuilder", fcqnTransformation);
							props.put("bundleid", "" + bundle.getBundleId());

							// Register a new service with the above props
							logger.debug("bundle context object : " + bundle.getBundleContext());
							bundle.getBundleContext().registerService(ILeapEventTransformer.class.getName(), leapEventTransformer, props);
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
							throw new FeatureMetaInfoResourceException("Unable to load/instantiate object for class with fully qualified name  "
									+ fcqnTransformation + " for bundle id : " + bundle.getBundleId() + " and symbolic name : "
									+ bundle.getSymbolicName());
						}
					}else if(transformationtype.equalsIgnoreCase("XML-XSLT")){
						logger.debug("event for which xslt defined : "+event.getId());
						String xslName=eventDispacher.getEventTransformation().getXSLTName();
						URL xslUrl = bundle.getResource(xslName);
						logger.debug("xsl url : "+xslUrl+" for xslt name : "+xslName);
						String xslAsString = convertXmlToString(xslUrl, bundle, xslName);
						logger.debug("xslt As String : "+xslAsString);
						eventDispacher.getEventTransformation().setXsltAsString(xslAsString);
					}
				}
				try {
					eventConfigService.addEventFrameworkConfiguration(configContext, event);
				} catch (EventFrameworkConfigurationException e) {
					throw new FeatureMetaInfoResourceException("Error in  Event configuration ", e);
				}
			}
		}else{
			logger.debug("events is not defined for the event configuration");
		}
	}//end of method

	/**
	 * This method is used to add event susbscription for the event in cache and in db
	 * @param eventSusbscriptions : EventSubscriptions Object
	 * @param eventConfigService : EventFrameworkConfigService Object
	 * @param configContext : ConfigurationContext Object
	 * @throws FeatureMetaInfoResourceException
	 */
	private void loadEventSubscriptionConfiguration(EventSubscriptions eventSusbscriptions,IEventFrameworkConfigService eventConfigService,ConfigurationContext configContext) throws FeatureMetaInfoResourceException{
		logger.debug(".loadEventSubscriptionConfiguration of FeatureMetaInfoResourceUtil");
		if(eventSusbscriptions!=null){
			List<EventSubscription> eventSubscriptionList=eventSusbscriptions.getEventSubscription();
			for(EventSubscription eventSubscription:eventSubscriptionList){
				try {
					eventConfigService.addEventFrameworkConfiguration(configContext, eventSubscription);
				} catch (EventFrameworkConfigurationException e) {
					throw new FeatureMetaInfoResourceException("Error in adding eventSubscription configuration ", e);

				}
			}//end of for loop
		}else{
			logger.debug("Event subscription is undefined");

		}

	}//end of method
	
	
	/**
	 * This method is used to load permastore configuration 
	 * @param permastoreConfigList : List<PermaStoreConfiguration>
	 * @param bundle : Bundle Object
	 * @param featureGroupName : feature group name
	 * @param featureName : feature name
	 * @throws FeatureMetaInfoResourceException
	 */
	public void loadPermastoreResourceInFeatureMetaInfo(List<PermaStoreConfiguration> permastoreConfigList, Bundle bundle, String featureGroupName,
			String featureName,String vendor,String version) throws FeatureMetaInfoResourceException {
		logger.debug(".getPermastoreResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		for (PermaStoreConfiguration permastore : permastoreConfigList) {
			String permastoreResourceName = "/" + permastore.getResourceName();
			URL permastoreResourceUrl = bundle.getResource(permastoreResourceName);
			String permastoreSourceAsString = convertXmlToString(permastoreResourceUrl, bundle, permastore.getResourceName());
			if (permastoreSourceAsString != null) {
				PermaStoreConfigXMLParser parmastoreConfigParser = new PermaStoreConfigXMLParser();
				PermaStoreConfigurations permastorConfig = null;
				try {
					RequestContext requestContext = new RequestContext(LeapHeaderConstant.tenant,
							LeapHeaderConstant.site, featureGroupName, featureName,vendor,version);
					permastorConfig = parmastoreConfigParser.marshallConfigXMLtoObject(permastoreSourceAsString);
					List<com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration> permastoreConfigList1 = permastorConfig.getPermaStoreConfiguration();
					for (com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration permaStoreConfiguration : permastoreConfigList1) {
						String configname = permaStoreConfiguration.getName();
						String builderType = permaStoreConfiguration.getConfigurationBuilder().getType().value();
						if (builderType.equalsIgnoreCase("CUSTOM")) {
							loadingPermastoreCustomBuildType(permaStoreConfiguration,requestContext,configname,bundle);
					}
					}// end of for
				} catch (PermaStoreConfigParserException e) {
					throw new FeatureMetaInfoResourceException("Unable to parse permastore file : " + permastore.getResourceName() + " for bundle id : "
							+ bundle.getBundleId() + " and symbolic name : " + bundle.getSymbolicName());
				}
			}
			logger.debug("exiting getPermastoreResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");

		}// end of for
	}// end of method
	
	/**
	 * This method is used to load permastore config when build type is custom
	 * @param permaStoreConfiguration  : Permastore Configuration Object
	 * @param requestContext : RequestContext Object
	 * @param configname : config name
	 * @param bundle : Bundle
	 * @throws FeatureMetaInfoResourceException
	 */
	private void loadingPermastoreCustomBuildType(com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration permaStoreConfiguration,RequestContext requestContext,String configname,Bundle bundle) throws FeatureMetaInfoResourceException{
		String fqnBuilder = permaStoreConfiguration.getConfigurationBuilder().getCustomBuilder().getBuilder();
		logger.debug("fully qualified name of custom builder type : " + fqnBuilder);
		try {
			Class<?> classObj = bundle.loadClass(fqnBuilder);
			IPermaStoreCustomCacheObjectBuilder customCacheObj = (IPermaStoreCustomCacheObjectBuilder) classObj.newInstance();
			logger.debug("customCacheObj : " + customCacheObj);
			Dictionary<String, String> props = new Hashtable<String, String>();
			props.put("tenant", LeapHeaderConstant.tenant);
			props.put("site", LeapHeaderConstant.site);
			props.put("featuregroup", requestContext.getFeatureGroup());
			props.put("feature", requestContext.getFeatureName());
			props.put("fqcnBuilder", fqnBuilder);
			props.put("bundleid", "" + bundle.getBundleId());

			// Register a new service with the above props
			logger.debug("bundle context object : " + bundle.getBundleContext());
			bundle.getBundleContext().registerService(IPermaStoreCustomCacheObjectBuilder.class.getName(), customCacheObj, props);
			IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();			
			ConfigurationContext configContext=new ConfigurationContext(requestContext);
			try {
				boolean isExist = psConfigService.checkPermaStoreConfigarationExistOrNot(configContext, permaStoreConfiguration.getName());
				if (!isExist) {
					//added configcontext ,before it was tenent and  site
					psConfigService.addPermaStoreConfiguration(configContext,
							permaStoreConfiguration);
					addServiceToServiceDiscovery(configContext,permaStoreConfiguration.getName());
				} else {
					logger.debug("Permastore configuration for : " + configname + "already exist for featuregroup : " + requestContext.getFeatureGroup()
							+ " and feature : " +requestContext.getFeatureName() + " in db");
				}
			} catch (PermaStoreConfigurationException | PermaStoreConfigRequestException e) {
				throw new FeatureMetaInfoResourceException("error in loading the PermastoreConfiguration ", e);
			}								
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new FeatureMetaInfoResourceException("Unable to load/instantiate object for class with fully qualified name  "
					+ fqnBuilder + " for bundle id : " + bundle.getBundleId() + " and symbolic name : " + bundle.getSymbolicName());
		}
	}//end of method

	/**
	 * This method is used to load policy configuration 
	 * @param policyConfigList : List<PolicyConfiguration>
	 * @param bundle : Bundle Object
	 * @param featureGroupName : feature group name
	 * @param featureName : feature name
	 * @throws FeatureMetaInfoResourceException
	 */
	public void loadPolicyResourceInFeatureMetaInfo(List<PolicyConfiguration> policyConfigList, Bundle bundle, String featureGroupName,
			String featureName,String vendor,String version) throws FeatureMetaInfoResourceException {
		logger.debug(".getPolicyResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		logger.debug("policy confiList : " + policyConfigList.size());
		for (PolicyConfiguration policyconfig : policyConfigList) {
			String policyconfigResourceName = "/" + policyconfig.getResourceName();
			URL policyResourceUrl = bundle.getResource(policyconfigResourceName);
			String policyconfigSourceAsString = convertXmlToString(policyResourceUrl, bundle, policyconfig.getResourceName());
			if (policyconfigSourceAsString != null) {
				PolicyConfigXMLParser policyParser = new PolicyConfigXMLParser();
				Policies policies = null;
				try {
					policies = policyParser.marshallConfigXMLtoObject(policyconfigSourceAsString);
					List<Policy> policyList = policies.getPolicy();
					if(!(policyList.isEmpty()) || policyList !=null){
					for (Policy policy : policyList) {
						logger.debug("policy related info : " + policy.getPolicyName());
						IPolicyConfigurationService policyConfigService = new PolicyConfigurationService();
						PolicyRequestContext policyRequestContext = new PolicyRequestContext(LeapHeaderConstant.tenant,
								LeapHeaderConstant.site, featureGroupName, featureName,vendor,version);
						ConfigurationContext configContext=new ConfigurationContext(policyRequestContext);
						try {
							boolean isExist = policyConfigService.checkPolicyExistInDbAndCache(configContext, policy.getPolicyName());
							if (!isExist) {
								//added conficontext before it was tenant and site
								policyConfigService.addPolicyConfiguration(configContext, policy);
								addServiceToServiceDiscovery(configContext,policy.getPolicyName());
							} else {
								logger.debug("Policy configuration for : " + policy.getPolicyName() + "already exist for featuregroup : "
										+ featureGroupName + " and feature : " + featureName + " in db");
							}
						} catch (PolicyConfigurationException | PolicyRequestException e) {
							throw new FeatureMetaInfoResourceException("error in loading the policyConfiguration for policy = " + policy.getPolicyName(), e);
						}				
					}// end of for loop
					}//end of if(!(policyList.isEmpty() || policyList !=null)
				} catch (PolicyConfigXMLParserException e) {
					throw new FeatureMetaInfoResourceException("Unable to parse policy file : " + policyconfig.getResourceName() + " for bundle id : "
							+ bundle.getBundleId() + " and symbolic name : " + bundle.getSymbolicName());
				}				
			}// end of if(policyconfigSourceAsString !=null)
		}
		logger.debug("exiting getPolicyResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
	}

	/**
	 * This method is used to add feature configuration 
	 * @param featureImplList : List<FeatureImplementation>
	 * @param bundle : Bundle
	 * @param featureGroupName : feature group name
	 * @param featureName : feature name
	 * @throws FeatureMetaInfoResourceException
	 */
	public void loadFeatureResourceInFeatureMetaInfo(List<FeatureImplementation> featureImplList, Bundle bundle, String featureGroupName,
			String featureName,String vendor,String version) throws FeatureMetaInfoResourceException {
		logger.debug(".getFeatureResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		for (FeatureImplementation featureImpl : featureImplList) {
			String featureImplResourceName = "/" + featureImpl.getResourceName();
			URL featureImplResourceUrl = bundle.getResource(featureImplResourceName);
			String featureImplSourceAsString = convertXmlToString(featureImplResourceUrl, bundle, featureImpl.getResourceName());
			logger.debug("feature as String : " + featureImplSourceAsString);
			if (featureImplSourceAsString != null) {
				logger.debug(".feature as string is not null");
				FeatureConfigXMLParser featureparser = new FeatureConfigXMLParser();
				FeaturesServiceInfo feaureServiceInfo = null;
				try {
					feaureServiceInfo = featureparser.marshallConfigXMLtoObject(featureImplSourceAsString);
					com.attunedlabs.feature.jaxb.Feature feature1 = feaureServiceInfo.getFeatures().getFeature();				
						logger.debug("feature related info : " + feature1.getFeatureName());
						IFeatureConfigurationService featureConfigService = new FeatureConfigurationService();
						FeatureConfigRequestContext requestContext = new FeatureConfigRequestContext(LeapHeaderConstant.tenant,
								LeapHeaderConstant.site, featureGroupName, featureName,vendor,version);
						ConfigurationContext configContext=new ConfigurationContext(requestContext);

						try {
							boolean isExist = featureConfigService.checkFeatureExistInDBAndCache(configContext, feature1.getFeatureName());
							if (!isExist) {
								//added configContext before it was  tenant and site
								featureConfigService.addFeatureConfiguration(configContext,feature1);
								addServiceToServiceDiscovery(configContext,feature1.getFeatureName());
							} else {
								logger.debug("feature configuration for : " + feature1.getFeatureName() + "already exist for featuregroup : "
										+ featureGroupName + " and feature : " + featureName + " in db");
							}
						} catch (FeatureConfigurationException | FeatureConfigRequestException e) {
							throw new FeatureMetaInfoResourceException("error in loading the feature Configuration for feature = " + feature1.getFeatureName(),
									e);
						}						
					
				} catch (FeatureConfigParserException e) {
					throw new FeatureMetaInfoResourceException("Unable to parse feature file : " + featureImpl.getResourceName() + " for bundle id : "
							+ bundle.getBundleId() + " and symbolic name : " + bundle.getSymbolicName());
				}
			}// end of if(featureImplSourceAsString!=null)
		}
		logger.debug("exiting getFeatureResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
	}

	/**
	 * This method is used to convert xml to string format
	 * @param url : URL object
	 * @param bundle : Bundle Object
	 * @param filename : file name
	 * @return String : xml in string format
	 * @throws FeatureMetaInfoResourceException
	 */
	private String convertXmlToString(URL url, Bundle bundle, String filename) throws FeatureMetaInfoResourceException {
		logger.debug(".convertXmlToString of FeatureMetaInfoResourceUtil");
		InputStream xmlInput = null;
		String xmlAsString = null;
		StringBuilder out1 = new StringBuilder();
		if (url != null) {
			try {
				xmlInput = url.openConnection().getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(xmlInput));
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						out1.append(line);
					}
				} catch (IOException e) {
					throw new FeatureMetaInfoResourceException("Unable to open the read for the BufferedReader for the file : " + filename
							+ " of bundle : " + bundle.getBundleId(), e);
				}
				logger.debug(out1.toString()); // Prints the string content read from input stream
				try {
					reader.close();
				} catch (IOException e) {
					throw new FeatureMetaInfoResourceException("Unable to close the read for the BufferedReader for the file : " + filename
							+ " of bundle : " + bundle.getBundleId(), e);
				}
				xmlAsString = out1.toString();
			} catch (IOException e) {
				throw new FeatureMetaInfoResourceException("Unable to open the input stream for the file : " + filename + "of bundle : "
						+ bundle.getBundleId(), e);			}
		} else {
			logger.debug(filename + " file doesn't exist into then bundle with id : " + bundle.getBundleId() + ", symbolic name : "
					+ bundle.getSymbolicName());
			}
		return xmlAsString;
	}// end of method

	/**
	 * This method is used to add servic eto service discovery
	 * @param context : ConfigurationContext Object
	 * @param servicename : Service Name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void addServiceToServiceDiscovery(ConfigurationContext context,String servicename) throws FeatureMetaInfoResourceException{
		logger.debug(".addServiceToServiceDiscovery of FeatureMetaInfoResourceUtil");
		String hostname=getHostName();
		if(hostname !=null){
		FeatureDiscoveryService featureDiscoveryService=new FeatureDiscoveryService();
		featureDiscoveryService.addFeatureService(context, servicename, hostname);
		}else{
			logger.debug("cannot add service : "+servicename+" with context data : "+context+" beacause hostname is null");			
		}
		logger.debug("exiting addServiceToServiceDiscovery of FeatureMetaInfoResourceUtil");

	}//end of method
	
	/**
	 * This method is used to delete the service registry when service is removed
	 * @param context : COnfiguration Context
	 * @param servicename : Service name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void deleteServiceFromServiceDiscovery(ConfigurationContext context,String servicename) throws FeatureMetaInfoResourceException{
		logger.debug(".deleteServiceToServiceDiscovery of FeatureMetaInfoResourceUtil");
		String hostname=getHostName();
		if(hostname !=null){
		FeatureDiscoveryService featureDiscoveryService=new FeatureDiscoveryService();
		featureDiscoveryService.deleteFeatureService(context, servicename, hostname);
		}else{
			logger.debug("cannot add service : "+servicename+" with context data : "+context+" beacause hostname is null");
			
		}
		logger.debug("exiting deleteServiceToServiceDiscovery of FeatureMetaInfoResourceUtil");

	}//end of method
	
	/**
	 * This method is used to get The host name
	 * @return String ; host name
	 * @throws FeatureMetaInfoResourceException
	 */
	private String getHostName() throws FeatureMetaInfoResourceException{
		logger.debug(".getHostName of FeatureMetaInfoResourceUtil");
		String hostname=null;
		try {
			hostname=InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new FeatureMetaInfoResourceException("No host found with the name : "+hostname,e);
		}
		
		System.out.println("hostname : "+hostname);
		logger.debug("exiting getHostName of FeatureMetaInfoResourceUtil");
		return hostname;
	}
	
	public void checkResourceAvailableAndDelete(Feature feature, Bundle bundle, String featureGroupName) throws FeatureMetaInfoResourceException {
		logger.debug(".checkResourceAvailableAndDelete of FeatureMetaInfoResourceUtil");
		logger.debug(".checkResourceAvailableAndCall of FeatureMetaInfoResourceUtil");
		//check if event resource configured and then load
		EventResources eventResources=feature.getEventResources();
		checkEventResourceAndDelete(eventResources,bundle, feature, featureGroupName);
		
		//check if permastore resource configured and then load
		com.attunedlabs.featuremetainfo.jaxb.PermaStoreConfigurations permastoreConfiguration=feature.getPermaStoreConfigurations();
		checkPermastoreResourceAndDelete(permastoreConfiguration,bundle, feature, featureGroupName);
		
		//check if dynastore resource configured and then load
		DynaStoreConfigurations dynastoreConfiguration=feature.getDynaStoreConfigurations();
		checkDynastoreResourceAndDelete(dynastoreConfiguration,bundle, feature, featureGroupName);
		
		//check if policy resource configured and then load
		PolicyConfigurations policyConfiguration=feature.getPolicyConfigurations();
		checkPolicyResourceAndDelete(policyConfiguration,bundle, feature, featureGroupName);
		
		//check if featureImpl resource configured and then load
		FeatureImplementations featureImplementation=feature.getFeatureImplementations();
		checkFeatureImplResourceAndDelete(featureImplementation,bundle, feature, featureGroupName);
		
		
		// check if featureImpl resource configured and then load
		FeatureDataContexts featureDataContexts = feature.getFeatureDataContexts();
		checkFeatureDataContextImplResourceAndDelete(featureDataContexts, bundle, feature, featureGroupName);

	}
	
	
	private void checkFeatureDataContextImplResourceAndDelete(FeatureDataContexts featureDataContexts, Bundle bundle,
			Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException {
		logger.debug(".checkFeatureDataContextImplResourceAndDelete method of FeatureMetaInfoResourceUtil");
		if(featureDataContexts !=null){
			List<DataContexts> dataContextsList=featureDataContexts.getDataContexts();
			if(dataContextsList !=null){
				deleteFeatureDataContextfromFeatureMetaInfo(dataContextsList,bundle, featureGroupName, feature.getName(),feature.getVendorName(),feature.getVendorVersion());
			}
		}else{
			logger.debug("No featureDataContexts configured in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature);
		}
		
	}

	private void deleteFeatureDataContextfromFeatureMetaInfo(List<DataContexts> dataContextsList, Bundle bundle,
			String featureGroupName, String name,String vendorName,String vendorVersion) throws FeatureMetaInfoResourceException {
		logger.debug(".deleteFeatureDataContextfromFeatureMetaInfo method of FeatureMetaInfoResourceUtil");
		for(DataContexts dataContexts:dataContextsList){
			String dataContextResourceName="/"+dataContexts.getResourceName();
			logger.debug("dataContexts resource name : "+dataContextResourceName);
			URL dataContextResourceUrl =bundle.getResource(dataContextResourceName);
			if(dataContextResourceUrl !=null){
				String dataContextAsSourceAsString=null;
				try {
					dataContextAsSourceAsString=convertXmlToString(dataContextResourceUrl, bundle, dataContextResourceName);
					DataContextConfigXMLParser dataContextXmlParser=new DataContextConfigXMLParser();
					IDataContextConfigurationService dataContextConfigService=new DataContextConfigurationService();
					ConfigurationContext configContext=null;
					try {
						FeatureDataContext featureDataContext=dataContextXmlParser.marshallXMLtoObject(dataContextAsSourceAsString);
						configContext = new ConfigurationContext(LeapHeaderConstant.tenant, LeapHeaderConstant.site,
								featureGroupName, name,vendorName,vendorVersion);
						try {
							dataContextConfigService.deleteDataContextConfiguration(configContext);
						} catch (DataContextConfigurationException e) {
							throw new FeatureMetaInfoResourceException("Unable to deleting configuration file "+dataContextResourceName+" for feature group :  "+featureGroupName+", feature name : "+name+", with context data : "+configContext);

						}
					} catch (DataContextParserException e) {
						throw new FeatureMetaInfoResourceException("Unable to parse featuredatacontext configuration file"+dataContextResourceName+" for feature group :  "+featureGroupName+", feature name : "+name);

					}
				} catch (FeatureMetaInfoResourceException e) {
					throw new FeatureMetaInfoResourceException("Unable to get string formate of featuredatacontext config for file  "+dataContextResourceName+" for feature group :  "+featureGroupName+", feature name : "+name);
				}
			}else{
				logger.debug("No datacontexts config xml exist with name : "+dataContextResourceName);
			}	
		}
	}//end of deleteFeatureDataContextfromFeatureMetaInfo method

	/**
	 * This method is used to check and Delete event resource defined in featureMetaInfo.xml
	 * @param eventResources : EventResources Object of FeatureMetaInfo
	 * @param bundle : Bundle Object
	 * @param feature : feature Name
	 * @param featureGroupName : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void checkEventResourceAndDelete(EventResources eventResources,Bundle bundle,Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException{
		if(eventResources !=null){
			List<EventResource> eventResourceList = eventResources.getEventResource();
			if (eventResourceList != null) {
				deleteEventConfigfromFeatureMetaInfo(eventResourceList,bundle, featureGroupName, feature.getName());
			}else{
				logger.debug("No EventResource is defined in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature+" but empty");
			}
			}else{
				logger.debug("No EventResource configured in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature);
			}
	}
	
	/**
	 * This method is used to check and Delete permastore resource defined in featureMetaInfo.xml
	 * @param permastoreConfiguration : PermaStoreConfigurations Object of FeatureMetaInfo
	 * @param bundle : Bundle Object
	 * @param feature : feature Name
	 * @param featureGroupName : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void checkPermastoreResourceAndDelete(com.attunedlabs.featuremetainfo.jaxb.PermaStoreConfigurations permastoreConfiguration,Bundle bundle,Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException{
		if(permastoreConfiguration !=null){
			List<PermaStoreConfiguration> permastoreConfigList = permastoreConfiguration.getPermaStoreConfiguration();
			if (permastoreConfigList != null) {
				deletePermastoreConfigFromFeatureMetaInfo(permastoreConfigList,bundle, featureGroupName, feature.getName(),feature.getVendorName(),feature.getVendorVersion());
			}else{
				logger.debug("No PermastoreResource is defined in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature+" but empty");
			}
			}else{
				logger.debug("No PermastoreResource configured in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature);
			}
	}
	
	
	/**
	 * This method is used to check and Delete PolicyResource resource defined in featureMetaInfo.xml
	 * @param policyConfiguration : PolicyConfigurations Object of FeatureMetaInfo
	 * @param bundle : Bundle Object
	 * @param feature : feature Name
	 * @param featureGroupName : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void checkPolicyResourceAndDelete(PolicyConfigurations policyConfiguration,Bundle bundle,Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException{
		if(policyConfiguration !=null){
			List<PolicyConfiguration> policyConfigList = policyConfiguration.getPolicyConfiguration();
			if (policyConfigList != null) {
				deletePolicyConfigFromFeatureMetaInfo(policyConfigList,bundle, featureGroupName, feature.getName(),feature.getVendorName(),feature.getVendorVersion());
			}else{
				logger.debug("No PolicyResource is defined in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature+" but empty");
			}
			}else{
				logger.debug("No PolicyResource configured in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature);
			}
	}
	
	/**
	 * This method is used to check and Delete FeatureImplResource resource defined in featureMetaInfo.xml
	 * @param FeatureImplementations : FeatureImplementations Object of FeatureMetaInfo
	 * @param bundle : Bundle Object
	 * @param feature : feature Name
	 * @param featureGroupName : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void checkFeatureImplResourceAndDelete(FeatureImplementations featureImplementations,Bundle bundle,Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException{
		if(featureImplementations !=null){
			List<FeatureImplementation> featureImplList = featureImplementations.getFeatureImplementation();
			if (featureImplList != null) {
				deleteFeatureConfigInFeatureMetaInfo(featureImplList,bundle, featureGroupName, feature.getName(),feature.getVendorName(),feature.getVendorVersion());
			}else{
				logger.debug("No FeatureImplResource is defined in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature+" but empty");
			}
			}else{
				logger.debug("No FeatureImplResource configured in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature);
			}
	}
	
	
	/**
	 * This method is used to check and Delete DynastoreResource resource defined in featureMetaInfo.xml
	 * @param dynastoreConfiguration : DynaStoreConfigurations Object of FeatureMetaInfo
	 * @param bundle : Bundle Object
	 * @param feature : feature Name
	 * @param featureGroupName : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void checkDynastoreResourceAndDelete(DynaStoreConfigurations dynastoreConfiguration,Bundle bundle,Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException{
		if(dynastoreConfiguration !=null){
			List<DynaStoreConfiguration> dynaStoreConfigList=feature.getDynaStoreConfigurations().getDynaStoreConfiguration();
			if (dynaStoreConfigList != null) {
				deleteDynastoreResourceFromFeatureMetaInfo(dynaStoreConfigList,bundle, featureGroupName, feature.getName(),feature.getVendorName(),feature.getVendorVersion());
			}else{
				logger.debug("No DynastoreResource is defined in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature+" but empty");
			}
			}else{
				logger.debug("No DynastoreResource configured in FeatureMetaInfo for feature Group : "+featureGroupName+", feature name : "+feature);
			}
	}
	
	/**
	 * This method is used to delete dynastore configuration
	 * @param dynaStoreConfigList : List<DynaStoreConfiguration>
	 * @param bundle : Bundle Object
	 * @param featureGroupName : feature group 
	 * @param name : feature name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void deleteDynastoreResourceFromFeatureMetaInfo(List<DynaStoreConfiguration> dynaStoreConfigList, Bundle bundle,String featureGroupName, String name,String vendor,String version) throws FeatureMetaInfoResourceException {
		logger.debug(".deleteDynastoreResourceFromFeatureMetaInfo() of FeatureMetaInfoResourceUtil");
		for(DynaStoreConfiguration dynastoreconfig:dynaStoreConfigList){
			String dynaResourceName="/"+dynastoreconfig.getResourceName();
			logger.debug("dynaResourceName : " + dynaResourceName);
			URL dynaResourceUrl =bundle.getResource(dynaResourceName);
			if(dynaResourceUrl !=null){
				String dynastoreAsSourceAsString=null;
				try {
					dynastoreAsSourceAsString = convertXmlToString(dynaResourceUrl,bundle,dynaResourceName);
				} catch (FeatureMetaInfoResourceException e1) {
					throw new FeatureMetaInfoResourceException("Unable to get string formate of dynastore config for file  "+dynaResourceName+" for feature group :  "+featureGroupName+", feature name : "+name);

				}
				if(dynastoreAsSourceAsString !=null){
				DynaStoreConfigXmlParser parser = new DynaStoreConfigXmlParser();
				IDynaStoreConfigurationService iDynaStoreConfigurationService=null;
				ConfigurationContext configContext=null;
				try {
					DynastoreConfigurations dynastoreConfigurations =parser.marshallConfigXMLtoObject(dynastoreAsSourceAsString);
					 
					 RequestContext dynaStoreConfigRequestContext = new RequestContext(LeapHeaderConstant.tenant, LeapHeaderConstant.site,
								featureGroupName, name,vendor,version);
					iDynaStoreConfigurationService= new DynaStoreConfigurationService();
					List<DynastoreConfiguration> dynastoreConfigList=dynastoreConfigurations.getDynastoreConfiguration();
					for(DynastoreConfiguration dynaConfig:dynastoreConfigList){
					try {
						iDynaStoreConfigurationService.deleteDynaStoreConfiguration(dynaStoreConfigRequestContext, dynaConfig.getDynastoreName().getValue(),dynaConfig.getDynastoreName().getVersion());
					} catch (DynaStoreConfigurationException e) {
						throw new FeatureMetaInfoResourceException("Unable to delete configuration file "+dynaResourceName+" for feature group :  "+featureGroupName+", feature name : "+name+", with context data : "+configContext);

					}
					}//end of for
				} catch (DynaStoreConfigParserException e) {
					throw new FeatureMetaInfoResourceException("Unable to parse dynastore configuration file"+dynaResourceName+" for feature group :  "+featureGroupName+", feature name : "+name);
				}				
				}//end of if(dynastoreAsSourceAsString !=null)
				}else{
				logger.debug("No dynastore config xml exist with name : "+dynaResourceName);
				}			
		}//end of for
		logger.debug("exiting deleteDynastoreResourceFromFeatureMetaInfo() of FeatureMetaInfoResourceUtil");
	}//end of method

	/**
	 * This method is used to delete event configuration
	 * @param eventResourceList
	 * @param bundle
	 * @param featureGroupName
	 * @param name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void deleteEventConfigfromFeatureMetaInfo(List<EventResource> eventResourceList, Bundle bundle, String featureGroupName, String name)
			throws FeatureMetaInfoResourceException {
		logger.debug(".getEventResourcesInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		for (EventResource eventresource : eventResourceList) {
			String eventResourceName = "/" + eventresource.getResourceName();
			URL eventResourceUrl = bundle.getResource(eventResourceName);
			String eventSourceAsString = convertXmlToString(eventResourceUrl, bundle, eventresource.getResourceName());
			if (eventresource != null) {
				EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
				EventFramework eventFrameworkConfig = null;
				IEventFrameworkConfigService eventConfigService = new EventFrameworkConfigService();
				try {
					eventFrameworkConfig = parser.marshallConfigXMLtoObject(eventSourceAsString);
				} catch (EventFrameworkConfigParserException e) {
					throw new FeatureMetaInfoResourceException("Unable to parse event file : " + eventresource.getResourceName() + " for bundle id : "
							+ bundle.getBundleId() + " and symbolic name : " + bundle.getSymbolicName());

				}
				// prepare the configcontext for eventing
				ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant, LeapHeaderConstant.site,
						featureGroupName, name);
				DispatchChanels dispatchChanels=eventFrameworkConfig.getDispatchChanels();
				SystemEvents systemEvents=eventFrameworkConfig.getSystemEvents();
				Events events=eventFrameworkConfig.getEvents();
				EventSubscriptions eventSubscriptions=eventFrameworkConfig.getEventSubscriptions();
				try {
					if(dispatchChanels !=null){
					List<DispatchChanel> disChanelList = dispatchChanels.getDispatchChanel();
					// delete chanel inti cache
					for (DispatchChanel disChanelConfig : disChanelList) {
						eventConfigService.deleteDipatcherChanelConfiguration(configContext, disChanelConfig.getId());
					}
					}
					if(systemEvents !=null){
						List<SystemEvent> systemEventList = systemEvents.getSystemEvent();
					// delete system events
					for (SystemEvent systemEvent : systemEventList) {
						eventConfigService.deleteSystemEventConfiguration(configContext, systemEvent.getId());
					}// end of for(SystemEvent systemEvent : systemEventList)
					}
					if(events !=null){
						List<Event> eventList = events.getEvent();
					// delete events
					for (Event event : eventList) {
						eventConfigService.deleteEventConfiguration(configContext, event.getId());
					}
					}	
					
					if(eventSubscriptions !=null){
						List<EventSubscription> eventSubscriptionList=eventSubscriptions.getEventSubscription();
						for(EventSubscription eventSubscription:eventSubscriptionList){
							eventConfigService.deleteEventSubscriptionConfiguration(configContext, eventSubscription.getEventId());
						}
					}
				} catch (EventFrameworkConfigurationException e) {
					throw new FeatureMetaInfoResourceException("error in loading the EventFramework configuration ", e);
				}
			}// end of if(eventresource !=null)
			logger.debug("exiting deleteEventConfigfromFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		}// end of for

	}//end of method

	/**
	 * This method is used to delete permastore configuration
	 * @param permastoreConfigList :List<PermaStoreConfiguration>
	 * @param bundle : Bundle Object
	 * @param featureGroupName : feature group name
	 * @param name : feature name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void deletePermastoreConfigFromFeatureMetaInfo(List<PermaStoreConfiguration> permastoreConfigList, Bundle bundle, String featureGroupName,
			String name,String vendor,String version) throws FeatureMetaInfoResourceException {
		logger.debug(".deletePermastoreConfigFromFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		for (PermaStoreConfiguration permastore : permastoreConfigList) {
			String permastoreResourceName = "/" + permastore.getResourceName();
			URL permastoreResourceUrl = bundle.getResource(permastoreResourceName);
			String permastoreSourceAsString = convertXmlToString(permastoreResourceUrl, bundle, permastore.getResourceName());
			if (permastoreSourceAsString != null) {
				PermaStoreConfigXMLParser parmastoreConfigParser = new PermaStoreConfigXMLParser();
				PermaStoreConfigurations permastorConfig = null;
				try {
					permastorConfig = parmastoreConfigParser.marshallConfigXMLtoObject(permastoreSourceAsString);
					List<com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration> permastoreConfigList1 = permastorConfig.getPermaStoreConfiguration();
					for (com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration permaStoreConfiguration : permastoreConfigList1) {
						String configname = permaStoreConfiguration.getName();
						IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
						RequestContext requestContext = new RequestContext(LeapHeaderConstant.tenant, LeapHeaderConstant.site,
								featureGroupName, name,vendor,version);
						ConfigurationContext configContext=new ConfigurationContext(requestContext);
						try {
							boolean isExist = psConfigService.checkPermaStoreConfigarationExistOrNot(configContext, permaStoreConfiguration.getName());
							if (isExist) {
								//added configcontext before it takes tenant and site
								psConfigService.deletePermaStoreConfiguration(configContext, permaStoreConfiguration.getName());
								deleteServiceFromServiceDiscovery(configContext, permaStoreConfiguration.getName());
							} else {
								logger.debug("Permastore configuration for : " + configname + " doesnot exist for featuregroup : " + featureGroupName
										+ " and feature : " + name + " in db");							}

						} catch (PermaStoreConfigurationException | PermaStoreConfigRequestException e) {
							throw new FeatureMetaInfoResourceException("error in loading the PermastoreConfiguration ", e);

						}						
					}// end of for
				} catch (PermaStoreConfigParserException e) {
					throw new FeatureMetaInfoResourceException("Unable to parse permastore file : " + permastore.getResourceName() + " for bundle id : "
							+ bundle.getBundleId() + " and symbolic name : " + bundle.getSymbolicName());
				}				
			}
			logger.debug("exiting deletePermastoreConfigFromFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		}// end of for
	}//end of method

	/**
	 * This method is used to delete policy configuration
	 * @param policyConfigList : List<PolicyConfiguration>
	 * @param bundle : Bundle Object
	 * @param featureGroupName : feature group name
	 * @param featureName : feature name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void deletePolicyConfigFromFeatureMetaInfo(List<PolicyConfiguration> policyConfigList, Bundle bundle, String featureGroupName, String featureName,String vendor,String version) throws FeatureMetaInfoResourceException {
		logger.debug(".deletePolicyConfigFromFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		logger.debug("policy confiList : " + policyConfigList.size());
		for (PolicyConfiguration policyconfig : policyConfigList) {
			String policyconfigResourceName = "/" + policyconfig.getResourceName();
			URL policyResourceUrl = bundle.getResource(policyconfigResourceName);
			String policyconfigSourceAsString = convertXmlToString(policyResourceUrl, bundle, policyconfig.getResourceName());
			logger.debug("policy as string : " + policyconfigSourceAsString);
			if (policyconfigSourceAsString != null) {
				PolicyConfigXMLParser policyParser = new PolicyConfigXMLParser();
				Policies policies = null;
				try {
					policies = policyParser.marshallConfigXMLtoObject(policyconfigSourceAsString);
					List<Policy> policyList = policies.getPolicy();
					for (Policy policy : policyList) {
						logger.debug("policy related info : " + policy.getPolicyName());
						IPolicyConfigurationService policyConfigService = new PolicyConfigurationService();
						PolicyRequestContext policyRequestContext = new PolicyRequestContext(LeapHeaderConstant.tenant,
								LeapHeaderConstant.site, featureGroupName, featureName,vendor,version);
						ConfigurationContext configContext=new ConfigurationContext(policyRequestContext);
						try {
							boolean isExist = policyConfigService.checkPolicyExistInDbAndCache(configContext, policy.getPolicyName());
							if (isExist) {
								//added configContext, before it was taking tenant ad site
								policyConfigService.deletePolicy(configContext, policy.getPolicyName());
								deleteServiceFromServiceDiscovery(configContext, policy.getPolicyName());
							} else {
								logger.debug("Policy configuration for : " + policy.getPolicyName() + "doesnot exist for featuregroup : "
										+ featureGroupName + " and feature : " + featureName + " in db");
							}
						} catch (PolicyConfigurationException | PolicyRequestException e) {
							throw new FeatureMetaInfoResourceException("error in loading the policyConfiguration for policy = " + policy.getPolicyName(), e);
						}						
					}// end of for loop
				} catch (PolicyConfigXMLParserException e) {
					throw new FeatureMetaInfoResourceException("Unable to parse policy file : " + policyconfig.getResourceName() + " for bundle id : "
							+ bundle.getBundleId() + " and symbolic name : " + bundle.getSymbolicName());
				}				
			}// end of if(policyconfigSourceAsString !=null)
		}
		logger.debug("exiting deletePolicyConfigFromFeatureMetaInfo of FeatureMetaInfoResourceUtil");
	}//end of method 

	/**
	 * This method is used to delete feature configuration
	 * @param featureImplList : List<FeatureImplementation>
	 * @param bundle : Bundle Object
	 * @param featureGroupName : feature group name
	 * @param featureName : feature name
	 * @throws FeatureMetaInfoResourceException
	 */
	private void deleteFeatureConfigInFeatureMetaInfo(List<FeatureImplementation> featureImplList, Bundle bundle, String featureGroupName, String featureName,String vendor,String version) throws FeatureMetaInfoResourceException {
		logger.debug(".deleteFeatureConfigInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
		for (FeatureImplementation featureImpl : featureImplList) {
			String featureImplResourceName = "/" + featureImpl.getResourceName();
			URL featureImplResourceUrl = bundle.getResource(featureImplResourceName);
			String featureImplSourceAsString = convertXmlToString(featureImplResourceUrl, bundle, featureImpl.getResourceName());
			logger.debug("feature as String : " + featureImplSourceAsString);
			if (featureImplSourceAsString != null) {
				logger.debug(".feature as string is not null");
				FeatureConfigXMLParser featureparser = new FeatureConfigXMLParser();
				FeaturesServiceInfo feaureServiceInfo = null;
				try {
					feaureServiceInfo = featureparser.marshallConfigXMLtoObject(featureImplSourceAsString);
					com.attunedlabs.feature.jaxb.Feature feature1 = feaureServiceInfo.getFeatures().getFeature();				
						logger.debug("feature related info : " + feature1.getFeatureName());
						IFeatureConfigurationService featureConfigService = new FeatureConfigurationService();
						FeatureConfigRequestContext requestContext = new FeatureConfigRequestContext(LeapHeaderConstant.tenant,
								LeapHeaderConstant.site, featureGroupName, feature1.getFeatureName(),vendor,version);
						ConfigurationContext configContext=new ConfigurationContext(requestContext);
						try {
							boolean isExist = featureConfigService.checkFeatureExistInDBAndCache(configContext, feature1.getFeatureName());
							if (isExist) {
								//added configcontext,before it takes tenant and site
								featureConfigService.deleteFeatureConfiguration(configContext, featureName);
								deleteServiceFromServiceDiscovery(configContext, feature1.getFeatureName());		
							} else {
								logger.debug("feature configuration for : " + feature1.getFeatureName() + "doesnot exist for featuregroup : "
										+ featureGroupName + " and feature : " + featureName + " in db");
							}
						} catch (FeatureConfigurationException | FeatureConfigRequestException e) {
							throw new FeatureMetaInfoResourceException("error in loading the feature Configuration for feature = " + feature1.getFeatureName(),
									e);
						}						
					
				} catch (FeatureConfigParserException e) {
					throw new FeatureMetaInfoResourceException("Unable to parse feature file : " + featureImpl.getResourceName() + " for bundle id : "
							+ bundle.getBundleId() + " and symbolic name : " + bundle.getSymbolicName());
				}				
			}// end of if(featureImplSourceAsString!=null)
		}
		logger.debug("exiting deleteFeatureConfigInFeatureMetaInfo of FeatureMetaInfoResourceUtil");
	}//end of method

}
