package com.attunedlabs.integrationfwk.pipeline.service;

import java.util.List;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.pipeline.PipelineContext;
import com.attunedlabs.config.pipeline.PipelineContextConfig;
import com.attunedlabs.integrationfwk.activities.bean.PipelineExecutionException;
import com.attunedlabs.integrationfwk.config.impl.IntegrationPipelineConfigXmlParser;
import com.attunedlabs.integrationfwk.config.jaxb.IntegrationPipe;
import com.attunedlabs.integrationfwk.config.jaxb.IntegrationPipes;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.config.jaxb.Pipeline;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;

public class PipeLineExecutionHelper {
	private static final Logger logger = LoggerFactory.getLogger(PipelineExecutionInitializer.class);

	/**
	 * @param exchange
	 * @param configXMLFile
	 * @throws PipeLineExecutionException
	 */
	public static void unmarshallConfigXMLtoObject(Exchange exchange, String configXMLFile, String pipelineName)
			throws PipeLineExecutionException {
		try {
			logger.debug("unmarshallConfigXMLtoObject()");
			IntegrationPipelineConfigXmlParser parser = new IntegrationPipelineConfigXmlParser();
			IntegrationPipes integrationPipes = parser.unmarshallConfigXMLtoObject(configXMLFile);
			IntegrationPipe integrationPipe = integrationPipes.getIntegrationPipe().get(0);
			updatePipelineContext(exchange, integrationPipe, pipelineName);
		} catch (Exception e) {
			throw new PipeLineExecutionException(e.getMessage());
		}
	}// ..end of the method

	/**
	 * @param exchange
	 * @param integrationPipe
	 */
	public static void updatePipelineContext(Exchange exchange, IntegrationPipe integrationPipe, String pipelineName) {
		logger.debug("updatePipelineContext() from PipeLineExecutionHelper....");
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		String body = exchange.getIn().getBody(String.class);
		PipelineContext pipelineContext = leapHeader.getPipelineContext();
		if (pipelineContext == null)
			pipelineContext = setPipelineContext(pipelineContext);
		List<PipelineContextConfig> pipelineContextConfigs = pipelineContext.getPipelineContextConfigs();
		PipelineContextConfig config = new PipelineContextConfig(pipelineName, (pipelineContextConfigs.size() + 1),
				PipelineExecutionUtil.convertToSingleLine(body), integrationPipe);
		pipelineContextConfigs.add(config);
		pipelineContext.setPipelineContextConfigs(pipelineContextConfigs);
		pipelineContext.setPipelineName(pipelineName);
		leapHeader.setPipelineContext(pipelineContext);
		exchange.getIn().setHeader(PipelineServiceConstant.PIPELINE_NAME, pipelineName);
		exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
	}

	/**
	 * @param pipeline
	 * @param exchange
	 * @return
	 * @throws PipelineExecutionException 
	 */
	public static String modifyRequestBody(Pipeline pipeline, Exchange exchange) throws PipelineExecutionException {
		try {
			String body = modifyRequestBody(pipeline, exchange.getIn().getBody(String.class));
			exchange.getIn().setBody(body);
			return body;
		} catch (Exception ex) {
			throw new PipelineExecutionException(ex.getMessage());
		}
	}// ..end of the method

	/**
	 * @param pipeline
	 * @param exchange
	 * @return
	 */
	public static String modifyRequestBody(Pipeline pipeline, String requestData) {
		String integrationPipe = pipeline.getIntegrationPipe();
		String pipelineName = pipeline.getPipelineName();
		StringBuilder builder = new StringBuilder();
		builder.append(PipelineServiceConstant.PIPELINE_START_TAG);
		if (pipelineName != null && !pipelineName.isEmpty())
			builder.append(PipelineServiceConstant.PIPELINENAME_START_TAG + pipelineName
					+ PipelineServiceConstant.PIPELINENAME_END_TAG);
		else
			builder.append(PipelineServiceConstant.INTEGRATION_PIPE_START_TAG
					+ PipelineExecutionUtil.convertToSingleLine(integrationPipe)
					+ PipelineServiceConstant.INTEGRATION_PIPE_END_TAG);
		builder.append(
				PipelineServiceConstant.REQUEST_DATA_START_TAG + PipelineExecutionUtil.convertToSingleLine(requestData)
						+ PipelineServiceConstant.REQUEST_DATA_END_TAG);
		builder.append(PipelineServiceConstant.PIPELINE_END_TAG);
		return builder.toString();
	}// ..end of the method

	/**
	 * @param pipeactivity
	 * @return
	 * @throws Exception
	 */
	public static String getRouteKey(PipeActivity pipeactivity) throws Exception {
		if (pipeactivity.getFTLEnricherActivity() != null) {
			return PipelineServiceConstant.FTL_ENRICHER_ROUTE_KEY;
		} else if (pipeactivity.getXSLTEnricherActivity() != null) {
			return PipelineServiceConstant.XSLT_ENRICHER_ROUTE_KEY;
		} else if (pipeactivity.getJDBCIntActivity() != null) {
			if (pipeactivity.getJDBCIntActivity().getDBConfig().getDbType()
					.equalsIgnoreCase(PipelineServiceConstant.CASSANDRA_DBTYPE)) {
				return PipelineServiceConstant.JDBC_INTACTIVITY_CASSANDRA_ROUTE_KEY;
			} else if (pipeactivity.getJDBCIntActivity().getDBConfig().getDbType()
					.equalsIgnoreCase(PipelineServiceConstant.MYSQL_DBTYPE)) {
				return PipelineServiceConstant.JDBC_INTACTIVITY_MYSQL_ROUTE_KEY;
			} else
				return null;
		} else if (pipeactivity.getFilterPipelineActivity() != null) {
			return PipelineServiceConstant.FILTER_PIPELINE_ROUTE_KEY;
		} else if (pipeactivity.getEmailNotifyActivity() != null) {
			return PipelineServiceConstant.EMAIL_NOTIFY_ROUTE_KEY;
		} else if (pipeactivity.getCamelRouteEndPoint() != null) {
			return PipelineServiceConstant.CAMEL_ENDPOINT_ROUTE_KEY;
		} else if (pipeactivity.getPropertiesActivity() != null) {
			return PipelineServiceConstant.PROPERTIES_ACTIVITY_ROUTE_KEY;
		} else if (pipeactivity.getEventPublishActivity() != null) {
			return PipelineServiceConstant.EVENT_PUBLISH_PIPELINE_ROUTE_KEY;
		} else if (pipeactivity.getGroovyScriptActivity() != null) {
			return PipelineServiceConstant.GROOVY_SCRIPT_ACTIVITY_ROUTE_KEY;
		} else if (pipeactivity.getConditionalFlowActivity() != null) {
			return PipelineServiceConstant.CONDITIONAL_FLOW_ACTIVITY_ROUTE_KEY;
		} else if (pipeactivity.getLoopActivity() != null) {
			return PipelineServiceConstant.LOOP_ACTIVITY_ROUTE_KEY;
		} else {
			throw new Exception("Invalid configuration exist in IntegrationPipeLine Activities..");
		}
	}

	/**
	 * @param completdata
	 * @param pipelineContext
	 * @return
	 */
	private synchronized static PipelineContext setPipelineContext(PipelineContext pipelineContext) {
		if (pipelineContext == null)
			pipelineContext = new PipelineContext();
		return pipelineContext;
	}

}
