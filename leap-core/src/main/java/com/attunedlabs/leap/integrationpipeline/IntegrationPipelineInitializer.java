package com.attunedlabs.leap.integrationpipeline;

import org.apache.camel.Exchange;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.persistence.ITenantConfigTreeService;
import com.attunedlabs.config.persistence.UndefinedPrimaryVendorForFeature;
import com.attunedlabs.config.persistence.impl.TenantConfigTreeServiceImpl;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigException;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.util.LeapConfigurationUtil;

public class IntegrationPipelineInitializer {
	final org.slf4j.Logger logger = LoggerFactory.getLogger(IntegrationPipelineInitializer.class);

	/**
	 * to initialize a few things, like LeapHeader params, before propagation
	 * 
	 * @param exchange
	 * @throws InitializingPipelineException
	 */
	public void loadPipeConfiguration(String piplineConfigName, Exchange exchange)
			throws InitializingPipelineException {
		ITenantConfigTreeService tenantTreeService = TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
		ConfigurationTreeNode vendorTreeNode;
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		LeapConfigurationUtil leapConfigurationUtil = new LeapConfigurationUtil();
		RequestContext reqcontext;
		try {
			vendorTreeNode = tenantTreeService.getPrimaryVendorForFeature(leapHeader.getTenant(), leapHeader.getSite(),
					leapHeader.getFeatureGroup(), leapHeader.getFeatureName());
			String vendor = vendorTreeNode.getNodeName();
			String version = vendorTreeNode.getVersion();
			leapHeader.setVendor(vendor);
			leapHeader.setVersion(version);
			reqcontext = new RequestContext(leapHeader.getTenant(), leapHeader.getSite(), leapHeader.getFeatureGroup(),
					leapHeader.getFeatureName(), leapHeader.getImplementationName(), vendor, version);
			leapHeader.setRequestContext(reqcontext);
		} catch (UndefinedPrimaryVendorForFeature e1) {
			throw new InitializingPipelineException(
					"Unable to load the VendorTree for initializing PipeConfiguration..", e1);
		}
		logger.debug(
				"To load we are calling: " + piplineConfigName + " " + leapHeader + " " + exchange.getExchangeId());
		try {
			if (piplineConfigName != null && !(piplineConfigName.isEmpty())) {
				leapConfigurationUtil.getIntegrationPipelineConfiguration(piplineConfigName, reqcontext, leapHeader,
						exchange);
			} else {
				throw new InitializingPipelineException("pipeline configuration name is null");
			}
		} catch (IntegrationPipelineConfigException e) {
			throw new InitializingPipelineException("Unable to load the IntegrationPipeline from cache to leapHeader..",
					e);
		}
	}// ..end of the method
}