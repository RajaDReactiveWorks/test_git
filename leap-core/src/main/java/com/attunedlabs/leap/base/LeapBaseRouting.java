package com.attunedlabs.leap.base;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.entity.leapdata.LeapDataConstants;
import com.attunedlabs.leap.feature.routing.DynamicallyImplRoutingFailedException;
import com.attunedlabs.leap.generic.UnableToLoadPropertiesException;
import com.attunedlabs.leap.util.LeapConfigurationUtil;

/**
 * This class is for routing to specific execution route based of service type
 * 
 * @author bizruntime
 *
 */
public class LeapBaseRouting {
	private static final Logger logger = LoggerFactory.getLogger(LeapBaseRouting.class);

	public static final String EXECUTIONROUTE_BASEDON_SERVICETYPE_FILE = "routesendpoints.properties";

	/**
	 * This method is used to route to execution route based on servicetype
	 * 
	 * @param exchange
	 *            : Exchange object to get leap header
	 * @return : execution route to send in string
	 * @throws DynamicallyTRRoutingFailedException
	 * @throws JSONException
	 * @throws FeatureDeploymentServiceException
	 * @throws DynamicallyImplRoutingFailedException
	 * @throws UnableToLoadPropertiesException
	 */
	public void route(Exchange exchange) throws DynamicallyTRRoutingFailedException, JSONException,
			FeatureDeploymentServiceException, DynamicallyImplRoutingFailedException {
		logger.debug(".route of LeapBaseRouting");
		logger.debug(exchange.getIn().getBody(String.class));
		// get the servicetype
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		String featureName = leapHeader.getFeatureName();
		String servicetype = leapHeader.getServicetype();

		if (featureName != null && servicetype != null && !servicetype.isEmpty()) {
			if (servicetype.trim().equals(LeapDataConstants.DATA)) {
				exchange.getIn().setHeader(PipelineServiceConstant.EXE_ROUTE,
						LeapDataConstants.DATA_ENTITY_SERVICE_ROUTE);
			} else if (servicetype.trim().equals(PipelineServiceConstant.EXECUTE_PIPELINE)) {
				exchange.getIn().setHeader(PipelineServiceConstant.EXE_ROUTE,
						PipelineServiceConstant.PIPELINE_SERVICE_ROUTE);
				IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();
				FeatureDeployment featureDeployment = featureDeploymentservice
						.getActiveAndPrimaryFeatureDeployedFromCache(leapHeader.getTenant(), leapHeader.getSite(),
								featureName, leapHeader);
				if (featureDeployment != null) {
					// providing implementation, vendor and version support
					leapHeader.setImplementationName(featureDeployment.getImplementationName());
					leapHeader.setVendor(featureDeployment.getVendorName());
					leapHeader.setVersion(featureDeployment.getFeatureVersion());
				} else {
					throw new DynamicallyImplRoutingFailedException(
							"unable to get the Implementation name : " + featureDeployment);
				}
			} else {
				String executionroute = featureName.trim() + "-" + servicetype.trim() + "-TR";

				logger.debug("execution route to send based on servicetype is : " + executionroute.trim());
				exchange.getIn().setHeader(PipelineServiceConstant.EXE_ROUTE, executionroute.trim());

				logger.debug("=============================================================================");
				logger.debug("LeapBaseRouting [Stop] : " + System.currentTimeMillis());
			}
		} else {
			LeapConfigurationUtil.setResponseCode(404, exchange,
					"No transformation route name is configured for the service in feature  :" + servicetype);
			throw new DynamicallyTRRoutingFailedException(
					"No transformation route name is configured for the service in feature  :" + servicetype);
		}

	}// end of route method
}
