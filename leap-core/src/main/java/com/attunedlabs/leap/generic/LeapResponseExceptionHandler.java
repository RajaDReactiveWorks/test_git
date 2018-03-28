package com.attunedlabs.leap.generic;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.attunedlabs.core.feature.exception.LeapAuthorizationFailedException;
import com.attunedlabs.core.feature.exception.LeapBadRequestException;
import com.attunedlabs.core.feature.exception.LeapValidationFailureException;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.i18n.exception.LocaleResolverException;
import com.attunedlabs.leap.i18n.service.ILeapResourceBundleResolver;
import com.attunedlabs.leap.i18n.service.LeapResourceBundleResolverImpl;

public class LeapResponseExceptionHandler implements Processor {
	
	static Logger logger = LoggerFactory.getLogger(LeapResponseExceptionHandler.class);

	/**
	 * Handler processed across the below process method. Which will identify
	 * the type to set the respective response formats and codes
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		/*
		 * if (exception == null) { return; }
		 */
		String developerMessage;
		Integer status;
		Long errorCode;
		String userMessage;
		String feature ;
		Long vendorErrorCode = 400L;
		String vendorErrorMessage = null;
		if (exception instanceof LeapBadRequestException) {
			LeapBadRequestException badRequestException = (LeapBadRequestException) exception;
			developerMessage = badRequestException.getDeveloperMessage();
			status = LeapBadRequestException.RESPONSE_CODE;
			errorCode = badRequestException.getAppErrorCode();
			feature = badRequestException.getFeature();
			userMessage = getLocalizedMessage(badRequestException.getUserMessage(), exchange).toString();
			vendorErrorCode = badRequestException.getVendorErrorCode();
			vendorErrorMessage = badRequestException.getVendorErrorMessage();
			setResponseData(developerMessage, userMessage, status, errorCode, feature,vendorErrorCode,vendorErrorMessage, exchange);
		} else if (exception instanceof LeapAuthorizationFailedException) {
			LeapAuthorizationFailedException authorizationFailedException = (LeapAuthorizationFailedException) exception;
			developerMessage = authorizationFailedException.getDeveloperMessage();
			status = LeapAuthorizationFailedException.RESPONSE_CODE;
			errorCode = authorizationFailedException.getAppErrorCode();
			userMessage = getLocalizedMessage(authorizationFailedException.getUserMessage(), exchange).toString();
			feature = authorizationFailedException.getFeature();
			setResponseData(developerMessage, userMessage, status, errorCode, feature, vendorErrorCode, vendorErrorMessage, exchange);
		} else if (exception instanceof LeapValidationFailureException) {
			LeapValidationFailureException validationFailureException = (LeapValidationFailureException) exception;
			developerMessage = validationFailureException.getDeveloperMessage();
			status = LeapValidationFailureException.RESPONSE_CODE;
			errorCode = validationFailureException.getAppErrorCode();
			userMessage = getLocalizedMessage(validationFailureException.getUserMessage(), exchange).toString();
			feature = validationFailureException.getFeature();
			setResponseData(developerMessage, userMessage, status, errorCode, feature, vendorErrorCode, vendorErrorMessage, exchange);
		} else {
			developerMessage = exception.getMessage();
			status = 500;
			errorCode = 500L;
			feature = exchange.getIn().getHeader(LeapResponseHandlerConstant.FEATURE, String.class);
			setResponseData(developerMessage, LeapResponseHandlerConstant.INTERNAL_ERROR, status, errorCode, feature,vendorErrorCode, vendorErrorMessage, exchange);
		}
	}// ..end of the method

	/**
	 * Setting error response data from the subClass
	 * 
	 * @param developerMessage
	 * @param userMessage
	 * @param status
	 * @param errorCode
	 * @param exchange
	 * @throws JSONException
	 * @throws SOAPException
	 * @throws IOException
	 */
	private void setResponseData(String developerMessage, String userMessage, Integer status, long errorCode,
			String feature, long vendorErrorCode, String vendorErrorMessage, Exchange exchange) throws JSONException, SOAPException, IOException {
		System.out.println("Inside of setResponseData ");
		Message message = exchange.getIn();
		String featureGroup = message.getHeader(LeapResponseHandlerConstant.FEATURE_GROUP, String.class);
		if(feature==null){
			feature = message.getHeader(LeapResponseHandlerConstant.FEATURE, String.class);
		}
		JSONObject responseData = new JSONObject();
		responseData.put(LeapResponseHandlerConstant.DEVELOPER_MESSAGE, developerMessage);
		responseData.put(LeapResponseHandlerConstant.USER_MESSAGE, userMessage);
		responseData.put(LeapResponseHandlerConstant.ERROR_CODE, errorCode);
		responseData.put(LeapResponseHandlerConstant.FEATURE, feature);
		responseData.put(LeapResponseHandlerConstant.INFO,
				LeapResponseHandlerConstant.BASE_URL_INFO + featureGroup + "/" + feature + "/" + errorCode);
		if(vendorErrorMessage != null){
			System.out.println("Inside if of setResponseData vendorErrorMessage = "+vendorErrorMessage);
			JSONObject vendorDetails = new JSONObject();
			vendorDetails.put(LeapResponseHandlerConstant.VENDOR_ERROR_CODE, vendorErrorCode);
			vendorDetails.put(LeapResponseHandlerConstant.VENDOR_ERROR_MESSAGE, vendorErrorMessage);
			responseData.put(LeapResponseHandlerConstant.VENDOR_DETAILS, vendorDetails);
		}
		message.setHeader(Exchange.HTTP_RESPONSE_CODE, status);
		JSONObject finalResponse = new JSONObject();
		finalResponse.put(LeapResponseHandlerConstant.RESPONSE, responseData);
		System.out.println("finalResponse of setResponseData : "+finalResponse);
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		try {
			if (leapHeader.getEndpointType().equals(LeapResponseHandlerConstant.ENDPOINT_XML)) {
				message.setBody(XML.toString(finalResponse));
			} else if (leapHeader.getEndpointType().equals(LeapResponseHandlerConstant.ENDPOINT_SOAP)) {
				SOAPMessage soapMessage = createSoapFault(developerMessage, userMessage, errorCode, message, feature);
				message.setBody(soapMessage);
			} else {
				message.setBody(finalResponse);
			}
		} catch (Exception e) {
			message.setBody(finalResponse);
		}
	}// ..end of the private method

	/**
	 * To construct the soapFault message when endpoint is CXF
	 * 
	 * @param developerMessage
	 * @param userMessage
	 * @param errorCode
	 * @param message
	 * @param status
	 * @return
	 * @throws SOAPException
	 * @throws IOException
	 */
	private static SOAPMessage createSoapFault(String developerMessage, String userMessage, Long errorCode,
			Message message, String feature) throws SOAPException, IOException {
		String featureGroup = message.getHeader(LeapResponseHandlerConstant.FEATURE_GROUP, String.class);
		SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
		SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
		SOAPFault fault = body.addFault();
		fault.setFaultCode(errorCode.toString());
		fault.setFaultString(userMessage);
		SOAPElement detailElement = fault.addChildElement(LeapResponseHandlerConstant.DETAIL);
		detailElement.addChildElement(LeapResponseHandlerConstant.DEVELOPER_MESSAGE).addTextNode(developerMessage);
		detailElement.addChildElement(LeapResponseHandlerConstant.FEATURE).addTextNode(feature);
		detailElement.addChildElement(LeapResponseHandlerConstant.INFO).addTextNode(
				LeapResponseHandlerConstant.BASE_URL_INFO + featureGroup + "/" + feature + "/" + errorCode);
		return soapMessage;
	}// ..end of the private method
	
	/**
	 * Method to get localized messages
	 * 
	 * @param content
	 * @param exchange
	 * @return
	 */
	private static Object getLocalizedMessage(Object content, Exchange exchange) {
		ILeapResourceBundleResolver bundleResolver = new LeapResourceBundleResolverImpl();
		ResourceBundle resourceBundle;
		Object message = "";
		String localeId;
		if (exchange.getIn().getHeaders().containsKey("locale")) {
			localeId = (String) exchange.getIn().getHeader("locale");
			logger.debug("localeId in if : " + localeId);
		} else {
			localeId = "en_US";
			return content;
		}
		
		String tenantId, siteId, featureName;
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		
		tenantId = leapHeader.getTenant();
		siteId = leapHeader.getSite();
		featureName = leapHeader.getFeatureName();
		
		try {
			resourceBundle = bundleResolver.getLeapLocaleBundle(tenantId, siteId, featureName, localeId, exchange);
			if(content instanceof java.lang.String) {
				if (resourceBundle.containsKey(content.toString()) ) {
					message = resourceBundle.getString(content.toString());
					return message;
				} 
			}else if(content instanceof java.lang.Integer) {
				if (resourceBundle.containsKey(content.toString()) ) {
					message = Integer.parseInt(resourceBundle.getString(content.toString()));
					return message;
				} 
			}
		} catch (LocaleResolverException e) {
			e.printStackTrace();
		}
		
		return content;
	}
}