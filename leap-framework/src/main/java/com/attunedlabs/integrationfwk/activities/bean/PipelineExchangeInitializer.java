package com.attunedlabs.integrationfwk.activities.bean;

import java.util.TimeZone;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;

public class PipelineExchangeInitializer {

	private Logger looger = (Logger) LoggerFactory.getLogger(PipelineExchangeInitializer.class.getName());

	/**
	 * bean method called from the IntegrationPipeactivity, to boot the pipe
	 * configs to the exchange headers, with its size
	 * 
	 * @param exchange
	 */
	public void processPipelineInit(Exchange exchange) {
		looger.debug("The exchange headers: " + exchange.getIn().getHeaders() + " - ExchangeId: "
				+ exchange.getExchangeId());
		if (exchange.getIn().getHeader(LeapHeaderConstant.TIMEZONE) == null)
			setTimeZone(exchange);

	}// ..end of the method

	private void setTimeZone(Exchange exchange) {
		// getting utc based on tenant and site
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		String tenantId = leapHeader.getTenant();
		String siteId = leapHeader.getSite();
		if (tenantId == null || siteId == null)
			exchange.getIn().setHeader(LeapHeaderConstant.TIMEZONE, TimeZone.getDefault().getID());
		else {
			IAccountRegistryService accountRegistryService = new AccountRegistryServiceImpl();
			String accountId = null;
			String timeZoneId = TimeZone.getDefault().getID();
			try {
				accountId = accountRegistryService.getAccountIdByTenant(tenantId);
				timeZoneId = accountRegistryService.getTimeZoneBySite(accountId, siteId);
				if (timeZoneId == null || timeZoneId.isEmpty()) {
					looger.debug("timezone not found for tenant and site" + tenantId + " : " + siteId);
					timeZoneId = TimeZone.getDefault().getID();
				}
			} catch (Exception e) {
				looger.error("timezone not found for tenant and site" + tenantId + " : " + siteId + " due to  : "
						+ e.getMessage());
				e.printStackTrace();
			}
			exchange.getIn().setHeader(LeapHeaderConstant.TIMEZONE, timeZoneId);

		}

	}

}
