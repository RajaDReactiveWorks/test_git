package com.getusroi.eventframework.abstractbean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.core.BeanDependencyResolveException;
import com.getusroi.core.BeanDependencyResolverFactory;
import com.getusroi.core.IBeanDependencyResolver;
import com.getusroi.eventframework.camel.EventBuilderInstantiationException;
import com.getusroi.eventframework.camel.eventbuilder.OgnlEventBuilder;
import com.getusroi.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.getusroi.eventframework.camel.eventproducer.CamelEventProducerConstant;
import com.getusroi.eventframework.camel.eventproducer.ICamelEventBuilder;
import com.getusroi.eventframework.config.EventFrameworkConfigurationException;
import com.getusroi.eventframework.config.impl.EventFrameworkConfigService;
import com.getusroi.eventframework.event.ROIEvent;
import com.getusroi.eventframework.jaxb.CamelEventBuilder;
import com.getusroi.eventframework.jaxb.CamelEventProducer;
import com.getusroi.eventframework.jaxb.Event;
import com.getusroi.mesh.MeshHeader;
import com.getusroi.mesh.MeshHeaderConstant;
import com.hazelcast.core.TransactionalList;
import com.hazelcast.core.TransactionalSet;
import com.hazelcast.transaction.TransactionContext;

/**
 * This Class is responsible for generating events for beans
 * 
 * @author ubuntu
 *
 */
public abstract class AbstractROICamelBean {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractROICamelBean.class);

	/**
	 * This method is used to invoke preprocessing, process bean and
	 * postProcessing method
	 * 
	 * @param exch
	 * @throws Exception 
	 */
	@Handler
	public void invokeBean(Exchange exch) throws Exception {
		preProcessing(exch);
		try {
			processBean(exch);
		} catch (Exception exp) {
			postProcessingForException(exch, exp);
			throw exp;
		}
		try {
			postProcessing(exch);
		} catch (EventBuilderInstantiationException evtBuilderExp) {
			evtBuilderExp.printStackTrace();
		}
	}

	protected void preProcessing(Exchange exch)  {
		logger.debug("PreProcessing Completed");
	}

	abstract protected void processBean(Exchange exch) throws Exception;

	protected void postProcessing(Exchange exch) throws EventBuilderInstantiationException {
		logger.debug("PostProcessing Completed");
		postProcessEventGeneration(exch);

	}

	protected void postProcessingForException(Exchange exch, Exception thrownExption) {
		logger.debug("postProcessingForException Completed");
	}

	protected Connection getConnection(DataSource dataSource,Exchange camelExchange) throws SQLException {
		logger.debug(".getConnection method  in AbstractROICamelBean");
		MeshHeader meshHeader = (MeshHeader) camelExchange.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);
		Map<Object,Object> resourceHolderMap=meshHeader.getResourceHolder();		
		Connection con=(Connection)resourceHolderMap.get(dataSource);
		if(con==null){
			logger.debug("MeshDataAccessContext.Fetching New Connection from dataSource"+dataSource);
			con=dataSource.getConnection();
			resourceHolderMap.put(dataSource, con);
			logger.debug("MeshDataAccessContext.Added connection and DS to HeaderMap"+dataSource);
		}		
		return con;
	}
	
	
	
	/**
	 * This method is used to generate events for bean
	 * 
	 * @param exch
	 *           : camel exchange
	 * @throws EventBuilderInstantiationException
	 */
	private void postProcessEventGeneration(Exchange exch) throws EventBuilderInstantiationException {
		logger.debug("inside postProcessEventGeneration()");
		MeshHeader meshHeader = (MeshHeader) exch.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);
		RequestContext reqContext = meshHeader.getRequestContext();
		String beanName = this.getClass().getName();
		String serviceName = meshHeader.getServicetype();
		String featureName=meshHeader.getFeatureName();
		String requestId = meshHeader.getRequestUUID();
		logger.debug("bean name : "+beanName+", featureName : "+featureName+", request uuid : "+requestId+", service name : "+serviceName);
		ConfigurationContext configCtx = new ConfigurationContext(reqContext);
		ConfigurationContext configCtx1=new ConfigurationContext(reqContext.getTenantId(),reqContext.getSiteId(),null,null,null);
		EventFrameworkConfigService evtConfigService = new EventFrameworkConfigService();
		Event evtConfig;
		CamelEventProducer camelEvtProducer=null;
		try {
			evtConfig = evtConfigService.getEventConfigProducerForBean(configCtx1, serviceName, beanName);
			if(evtConfig!=null){
			camelEvtProducer=evtConfig.getCamelEventProducer();
			}//end of if(evtConfig!=null)
		} catch (EventFrameworkConfigurationException e) {
			throw new EventBuilderInstantiationException("Exception while finding the Event to produce from configuration",e);
		}
		if (camelEvtProducer != null) {
				logger.debug("---------before calling hazelcast transaction -----");
				TransactionContext hcTransactionContext = meshHeader.getHazelcastTransactionalContext();
				logger.debug("postProcessEventGeneration requestId=" + requestId + "--hcTransactionContext=" + hcTransactionContext);
				TransactionalList<ROIEvent> eventList = hcTransactionContext.getList(requestId);
				logger.debug("postProcessEventGeneration transactionalList=" + eventList);
				ICamelEventBuilder evtBuilder = null;
				CamelEventBuilder camelEvtBuilderConfig = camelEvtProducer.getCamelEventBuilder();
	
				if (camelEvtBuilderConfig.getType().equalsIgnoreCase(CamelEventProducerConstant.OGNL_EVENT_BUILDER)) {
					evtBuilder = getOGNLEventBuilderInstance();
				} else {
					// Event builder is of type Custom
					evtBuilder = getEventBuilderInstance(camelEvtBuilderConfig);
	
				}
				// building the roi event
				ROIEvent roievent = evtBuilder.buildEvent(exch,evtConfig);
				logger.debug("eventlist before adding event : "+eventList);
				if (eventList != null) {
					eventList.add(roievent);
				}// end of if(eventList!=null)
				logger.debug("eventlist after adding event : "+eventList);

			logger.debug(".postProcessEventGeneration() Event for this Bean is EventId="+roievent.toString()+" created");
		}else{
			logger.debug(".postProcessEventGeneration() No Event found for this Bean");
		}
	}

	/**
	 * This method is used to generate custom event builder for the bean events
	 * 
	 * @param cEvtbuilder
	 *           : CamelEventBuilder object
	 * @return AbstractCamelEventBuilder
	 * @throws EventBuilderInstantiationException
	 */
	// #TODO THIS WILL NOT WORK IN osgi.TO CHANGED EITHER loaded spring or osgi
	// registry
	private ICamelEventBuilder getEventBuilderInstance(CamelEventBuilder cEvtbuilder) throws EventBuilderInstantiationException {
		logger.debug("inside getEventBuilderInstance() of AbstractROICamelBean");
		String fqcn = cEvtbuilder.getEventBuilder().getFqcn();
		logger.debug("fcqn : "+fqcn);
		IBeanDependencyResolver beanResolver = BeanDependencyResolverFactory.getBeanDependencyResolver();
		try {
			ICamelEventBuilder evtBuilderInstance = (ICamelEventBuilder) beanResolver.getBeanInstance(ICamelEventBuilder.class, fqcn);
			return evtBuilderInstance;
		} catch (BeanDependencyResolveException e) {
			logger.error("error in building custom eveint builder inside AbstractROICamelBean for : " + fqcn, e);
			throw new EventBuilderInstantiationException("Failed to Load CustomEventBuilder", e);
		}
	}

	/**
	 * This method is to get OGNL Event Builder Instance
	 * 
	 * @return AbstractCamelEventBuilder
	 */
	private ICamelEventBuilder getOGNLEventBuilderInstance() {
		logger.debug("inside getOGNLEventBuilderInstance() of AbstractROICamelBean");
		ICamelEventBuilder evtBuilderInstance = (ICamelEventBuilder) new OgnlEventBuilder();

		return evtBuilderInstance;
	}

}
