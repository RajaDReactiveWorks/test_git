package com.attunedlabs.eventframework.abstractbean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.core.BeanDependencyResolverFactory;
import com.attunedlabs.core.IBeanDependencyResolver;
import com.attunedlabs.eventframework.camel.EventBuilderInstantiationException;
import com.attunedlabs.eventframework.camel.eventbuilder.OgnlEventBuilder;
import com.attunedlabs.eventframework.camel.eventproducer.CamelEventProducerConstant;
import com.attunedlabs.eventframework.camel.eventproducer.ICamelEventBuilder;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.CamelEventBuilder;
import com.attunedlabs.eventframework.jaxb.CamelEventProducer;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;

/**
 * This Class is responsible for generating events for beans
 * 
 * @author ubuntu
 *
 */
public abstract class AbstractLeapCamelBean {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractLeapCamelBean.class);

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

	protected void preProcessing(Exchange exch) {
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

	protected synchronized Connection getConnection(DataSource dataSource, Exchange camelExchange) throws SQLException {
		logger.debug(".getConnection method  in AbstractLeapCamelBean");
		LeapHeader leapHeader = (LeapHeader) camelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		logger.debug(".getConnection method  in AbstractLeapCamelBean leapHeader : "+leapHeader);
		Map<Object, Object> resourceHolderMap = leapHeader.getResourceHolder();
		Connection con = (Connection) resourceHolderMap.get(dataSource);
		if (con == null || con.isClosed()) {
			logger.debug("LeapDataAccessContext.Fetching New Connection from dataSource" + dataSource);
			try {
				con = DataSourceUtils.getConnection(dataSource);
			} catch (CannotGetJdbcConnectionException e) {
				logger.warn("Error in getting the Connection from " + dataSource);
			}
			if (con.isClosed()) {
				con = dataSource.getConnection();
			}
			resourceHolderMap.put(dataSource, con);
			logger.debug("LeapDataAccessContext.Added connection and DS to HeaderMap" + dataSource);
			logger.debug("Added connection status : " + con.isClosed());
		}
		leapHeader.setResourceHolder(resourceHolderMap);
		camelExchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
		return con;
	}

	/**
	 * This method is used to generate events for bean
	 * 
	 * @param exch
	 *            : camel exchange
	 * @throws EventBuilderInstantiationException
	 */
	private void postProcessEventGeneration(Exchange exch) throws EventBuilderInstantiationException {
		logger.debug("inside postProcessEventGeneration()");
		LeapHeader leapHeader = (LeapHeader) exch.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		RequestContext reqContext = leapHeader.getRequestContext();
		String beanName = this.getClass().getName();
		String serviceName = leapHeader.getServicetype();
		String featureName = leapHeader.getFeatureName();
		String requestId = leapHeader.getRequestUUID();
		logger.debug("bean name : " + beanName + ", featureName : " + featureName + ", request uuid : " + requestId
				+ ", service name : " + serviceName);
		ConfigurationContext configCtx1 = new ConfigurationContext(reqContext);
//		ConfigurationContext configCtx1 = new ConfigurationContext(reqContext.getTenantId(), reqContext.getSiteId(),
//				null, null, null);
		EventFrameworkConfigService evtConfigService = new EventFrameworkConfigService();
		Event evtConfig;
		CamelEventProducer camelEvtProducer = null;
		try {
			evtConfig = evtConfigService.getEventConfigProducerForBean(configCtx1, serviceName, beanName);
			if (evtConfig != null) {
				camelEvtProducer = evtConfig.getCamelEventProducer();
			} // end of if(evtConfig!=null)
		} catch (EventFrameworkConfigurationException e) {
			throw new EventBuilderInstantiationException(
					"Exception while finding the Event to produce from configuration", e);
		}
		if (camelEvtProducer != null) {
			logger.debug("---------before calling hazelcast transaction ------");
			TransactionContext hcTransactionContext = leapHeader.getHazelcastTransactionalContext();
			logger.debug("postProcessEventGeneration requestId=" + requestId + "--hcTransactionContext="
					+ hcTransactionContext);
			TransactionalMap<String, ArrayList<LeapEvent>> txEventMap = hcTransactionContext.getMap(requestId);
			
			logger.debug("postProcessEventGeneration transactionalList=" + txEventMap);
			ICamelEventBuilder evtBuilder = null;
			CamelEventBuilder camelEvtBuilderConfig = camelEvtProducer.getCamelEventBuilder();

			if (camelEvtBuilderConfig.getType().equalsIgnoreCase(CamelEventProducerConstant.OGNL_EVENT_BUILDER)) {
				evtBuilder = getOGNLEventBuilderInstance();
			} else {
				// Event builder is of type Custom
				evtBuilder = getEventBuilderInstance(camelEvtBuilderConfig);

			}
			
			ArrayList<LeapEvent> leapEventList = null;
			if(((ArrayList<LeapEvent>)txEventMap.get(requestId)) == null)
				leapEventList = new ArrayList<>();
			else
				leapEventList = ((ArrayList<LeapEvent>)txEventMap.get(requestId));
			// building the leap event
			LeapEvent leapevent = evtBuilder.buildEvent(exch, evtConfig);
			
			leapevent.addEventParam(LeapHeaderConstant.TENANT_KEY, leapHeader.getTenant());
			leapevent.addEventParam(LeapHeaderConstant.SITE_KEY, leapHeader.getSite());
			
			logger.debug("map size before adding event : " + txEventMap.size());
			if (txEventMap != null && leapEventList != null) {
				leapEventList.add(leapevent);
				txEventMap.put(requestId, leapEventList);
				logger.info("leap event called " + leapevent);
			} // end of if(eventList!=null)
			logger.debug("eventMap size after adding event : " + txEventMap.size());

			logger.debug(
					".postProcessEventGeneration() Event for this Bean is EventId=" + leapevent.toString() + " created");
		} else {
			logger.debug(".postProcessEventGeneration() No Event found for this Bean");
		}
	}

	/**
	 * This method is used to generate custom event builder for the bean events
	 * 
	 * @param cEvtbuilder
	 *            : CamelEventBuilder object
	 * @return AbstractCamelEventBuilder
	 * @throws EventBuilderInstantiationException
	 */
	// #TODO THIS WILL NOT WORK IN osgi.TO CHANGED EITHER loaded spring or osgi
	// registry
	private ICamelEventBuilder getEventBuilderInstance(CamelEventBuilder cEvtbuilder)
			throws EventBuilderInstantiationException {
		logger.debug("inside getEventBuilderInstance() of AbstractLeapCamelBean");
		String fqcn = cEvtbuilder.getEventBuilder().getFqcn();
		logger.debug("fcqn : " + fqcn);
		IBeanDependencyResolver beanResolver = BeanDependencyResolverFactory.getBeanDependencyResolver();
		try {
			ICamelEventBuilder evtBuilderInstance = (ICamelEventBuilder) beanResolver
					.getBeanInstance(ICamelEventBuilder.class, fqcn);
			return evtBuilderInstance;
		} catch (BeanDependencyResolveException e) {
			logger.error("error in building custom eveint builder inside AbstractLeapCamelBean for : " + fqcn, e);
			throw new EventBuilderInstantiationException("Failed to Load CustomEventBuilder", e);
		}
	}

	/**
	 * This method is to get OGNL Event Builder Instance
	 * 
	 * @return AbstractCamelEventBuilder
	 */
	private ICamelEventBuilder getOGNLEventBuilderInstance() {
		logger.debug("inside getOGNLEventBuilderInstance() of AbstractLeapCamelBean");
		ICamelEventBuilder evtBuilderInstance = (ICamelEventBuilder) new OgnlEventBuilder();

		return evtBuilderInstance;
	}

}
