package com.getusroi.eventframework.dispatcher;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.core.datagrid.DataGridService;
import com.getusroi.eventframework.config.EventFrameworkConfigurationException;
import com.getusroi.eventframework.config.IEventFrameworkConfigService;
import com.getusroi.eventframework.config.impl.EventFrameworkConfigService;
import com.getusroi.eventframework.event.ROIEvent;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.eventframework.jaxb.EventDispatcher;
import com.getusroi.eventframework.jaxb.SystemEvent;
import com.hazelcast.core.IExecutorService;

/**
 * 
 * @author bizruntime
 *
 */
public class EventFrameworkDispatcherService implements IEventFrameworkIDispatcherService {
	protected static final Logger logger = LoggerFactory.getLogger(EventFrameworkDispatcherService.class);
	private IEventFrameworkConfigService eventFrameworkConfigService;
	
	public EventFrameworkDispatcherService() {
		eventFrameworkConfigService=new EventFrameworkConfigService();
	}

	/**
	 * This method is to dispatch event to executor service
	 * 
	 * @param : ROIEvent
	 */
	public void dispatchforEvent(ROIEvent roiEvent) throws EventFrameworkDispatcherException{
		logger.debug("inside dispatchforEvent() in ROIDispachterService : ");
		String eventId=roiEvent.getEventId();
		//get DataGrid ExecutorService
		IExecutorService dispExecutor = getDispatcherExecutorService(roiEvent.getRequestContext());
		ConfigurationContext configContext=getConfigurationContext(roiEvent.getRequestContext());
		logger.debug("config context : "+configContext);
		
		Event eventConfig=null;
		try {
			eventConfig = eventFrameworkConfigService.getEventConfiguration(configContext, eventId);
			if(eventConfig==null){
				throw new EventFrameworkDispatcherException("DispatcherService failed to get EventConfiguration for EventId{"+eventId+"} and context="+configContext);
			}
		} catch (EventFrameworkConfigurationException e) {
			throw new EventFrameworkDispatcherException("DispatcherService failed to get EventConfiguration for EventId{"+eventId+"} and context="+configContext,e);
		}
	
		//Event can have more than One Dispatchers
		List<EventDispatcher> evtDispatcherConfigList=eventConfig.getEventDispatchers().getEventDispatcher();
		for(EventDispatcher evtDispatchConfig:evtDispatcherConfigList){
			String chanelId = evtDispatchConfig.getDispatchChanelId();
			String transformationType = evtDispatchConfig.getEventTransformation().getType();
			String tranformationBeanFQCN = null;
			String xslname = null;
			String xsltAsString=null;

			switch (transformationType) {
				case "CUSTOM": 
									logger.debug("transformation type is CUSTOM");
									tranformationBeanFQCN = evtDispatchConfig.getEventTransformation().getCustomTransformer().getFqcn();
									break;
	
				case "XML-XSLT": 
									logger.debug("transformation type is XML-XSLT");
									xslname = evtDispatchConfig.getEventTransformation().getXSLTName();
									xsltAsString=evtDispatchConfig.getEventTransformation().getXsltAsString();

									break;
	
				case "JSON":
									logger.debug("transformation type is JSON");
									//setting as null because JSON is default transformer
									tranformationBeanFQCN=null;
									break;
				default:
									logger.debug("no matching transformation type found in the system setting JSON as Default");
									//JSON is deFAULT TRANSFORMER
									tranformationBeanFQCN=null;
									break;
			}//end of switch
			
			logger.debug(" ROIEventDispatchTask sending for execution for EventId=" + roiEvent.getEventId() + "--ChanelId=" + chanelId
					+ ", transformation bean : " + tranformationBeanFQCN + ", xslname : " + xslname);

			ROIEventDispatchTask disatcherTask = new ROIEventDispatchTask(roiEvent, chanelId, tranformationBeanFQCN, xslname,xsltAsString);
			dispExecutor.execute(disatcherTask);
			
			logger.debug(" ROIEventDispatchTask sending for execution for EventId=" + roiEvent.getEventId() + "--ChanelId=" + chanelId
					+ ", transformation bean : " + tranformationBeanFQCN + ", xslname : " + xslname);
		}//end of For
	}// end of method

	

	
	/**
	 * This method is to dispatch event to executor service
	 * 
	 * @param : ROIEvent
	 */
	public void dispatchforSystemEvent(ROIEvent roiEvent) throws EventFrameworkDispatcherException{
		logger.debug("inside dispatchforEvent() in ROIDispachterService : ");
		String eventId=roiEvent.getEventId();
		logger.debug("event in EventFramework dispatcher service : "+roiEvent.toString());
		//get DataGrid ExecutorService
		IExecutorService dispExecutor = getDispatcherExecutorService(roiEvent.getRequestContext());
		logger.debug("request context in EventFrameworkDispatcherService : "+roiEvent.getRequestContext());
		ConfigurationContext configContext=getConfigurationContext(roiEvent.getRequestContext());
		logger.debug("config context : "+configContext);
		
		SystemEvent systemEventConfig=null;
		try {
			systemEventConfig = eventFrameworkConfigService.getSystemEventConfiguration(configContext, eventId);
			//code should not give any error in the because it must generate system event, But its giving so commenting
			if(systemEventConfig==null){
				logger.warn("DispatcherService failed to get SystemEventConfiguration for EventId{"+eventId+"} and context="+configContext);
				//throw new EventFrameworkDispatcherException("DispatcherService failed to get SystemEventConfiguration for EventId{"+eventId+"} and context="+configContext);
			}
		} catch (EventFrameworkConfigurationException e) {
			throw new EventFrameworkDispatcherException("DispatcherService failed to get SystemEventConfiguration for EventId{"+eventId+"} and context="+configContext,e);
		}
	
		//Event can have more than One Dispatchers
		if(systemEventConfig!=null){
		List<EventDispatcher> evtDispatcherConfigList=systemEventConfig.getEventDispatchers().getEventDispatcher();
		for(EventDispatcher evtDispatchConfig:evtDispatcherConfigList){
			String chanelId = evtDispatchConfig.getDispatchChanelId();
			String transformationType = evtDispatchConfig.getEventTransformation().getType();
			String tranformationBeanFQCN = null;
			String xslname = null;
			String xsltAsString=null;

			switch (transformationType) {
				case "CUSTOM": 
									logger.debug("transformation type is CUSTOM");
									tranformationBeanFQCN = evtDispatchConfig.getEventTransformation().getCustomTransformer().getFqcn();
									break;
	
				case "XML-XSLT": 
									logger.debug("transformation type is XML-XSLT");
									xslname = evtDispatchConfig.getEventTransformation().getXSLTName();
									xsltAsString=evtDispatchConfig.getEventTransformation().getXsltAsString();

									break;
	
				case "JSON":
									logger.debug("transformation type is JSON");
									//setting as null because JSON is default transformer
									tranformationBeanFQCN=null;
									break;
				default:
									logger.debug("no matching transformation type found in the system setting JSON as Default");
									//JSON is deFAULT TRANSFORMER
									tranformationBeanFQCN=null;
									break;
			}//end of switch
			
			logger.debug(" ROIEventDispatchTask sending for execution for EventId=" + roiEvent.getEventId() + "--ChanelId=" + chanelId
					+ ", transformation bean : " + tranformationBeanFQCN + ", xslname : " + xslname);

			ROIEventDispatchTask disatcherTask = new ROIEventDispatchTask(roiEvent, chanelId, tranformationBeanFQCN, xslname,xsltAsString);
			dispExecutor.execute(disatcherTask);
			
			logger.debug(" ROIEventDispatchTask sending for execution for EventId=" + roiEvent.getEventId() + "--ChanelId=" + chanelId
					+ ", transformation bean : " + tranformationBeanFQCN + ", xslname : " + xslname);
		}//end of For
	}//end of  if
 	}// end of method	
	private IExecutorService getDispatcherExecutorService(RequestContext requestContext){
		IExecutorService dispExecutor = DataGridService.getDataGridInstance().getDispatcherExecutor(requestContext.getTenantId()+"-"+requestContext.getSiteId());
		return dispExecutor;
	}
	
	private ConfigurationContext getConfigurationContext(RequestContext rqCtx){
		logger.debug("request context in EventFramework dispacher serice : "+rqCtx.getTenantId()+", site : "+rqCtx.getSiteId());
		ConfigurationContext configContext=new ConfigurationContext(rqCtx);
		logger.debug("config context before return : "+configContext);
		
		return configContext;
	}

}
