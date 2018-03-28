package com.attunedlabs.leap.identityservice;

import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.ACCESS_TOKEN;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.AUTHERIZATION;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.FALSE_KEY;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.IDENTIFIER;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.IS_AUTH;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.OAUTH_VALIDATOR_SERVIE_URL;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.SKIP_SERVICES;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.TOKEN_TYPE;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.TOKEN_TYPE_VALUE;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.VALIDATION_REQUEST_DTO;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.XSD_NAMESPACE1;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.XSD_NAMESPACE2;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.header.initializer.JsonParserException;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.DigestMakeException;
import com.attunedlabs.security.exception.TenantTokenValidationException;
import com.attunedlabs.security.service.IAccountSecurityService;
import com.attunedlabs.security.service.impl.AccountSecurityServiceImpl;
import com.attunedlabs.security.utils.TenantSecurityUtil;

public class IdentityServiceBean {
	private static Logger logger = LoggerFactory.getLogger(IdentityServiceBean.class.getName());
	private Properties properties;
	private DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder builder;
	private IAccountSecurityService accountSecurityService = new AccountSecurityServiceImpl();
	private SOAPConnectionFactory soapConnFactory;
	private MessageFactory msgFct;
	private SOAPConnection soapCon;
	private LeapByteArrayOutputStream out1 = new LeapByteArrayOutputStream();

	static {
		try {
			initializeClientCall();
		} catch (TrustValidationException e) {
			logger.error("" + e.getMessage() + " " + e.getCause());
		}
	}

	public void isAuthenticated(Exchange exchange) throws IdentityServiceException, JsonParserException, JSONException {
		logger.debug(". isAuthenticated method of IdentityService");
		Message message = exchange.getIn();
		try {
			if (this.properties == null)
				this.properties = LeapConfigurationUtil.loadingPropertiesFile();
			String isAuthenticated = properties.getProperty(IS_AUTH);
			if (isAuthenticated.equalsIgnoreCase("true")) {
				JSONArray servicesToSkip = new JSONArray(properties.getProperty(SKIP_SERVICES));
				for (int i = 0; i < servicesToSkip.length(); i++) {
					String service = servicesToSkip.getString(i);
					String[] serviceDetail = service.trim().split("-");
					if (message.getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY).toString()
							.equalsIgnoreCase(serviceDetail[0])) {
						if (message.getHeader(LeapHeaderConstant.FEATURE_KEY).toString()
								.equalsIgnoreCase(serviceDetail[1])) {
							if (message.getHeader(LeapHeaderConstant.SERVICETYPE_KEY).toString()
									.equalsIgnoreCase(serviceDetail[2])) {
								isAuthenticated = "false";
								logger.debug("Authentication Turned false for " + serviceDetail);
							}
						}
					}
				}
			}
			message.setHeader(IS_AUTH, isAuthenticated);
		} catch (PropertiesConfigException e) {
			LeapConfigurationUtil.setResponseCode(500, exchange, e.getMessage());
			throw new IdentityServiceException("unable to load the properties file", e);
		} catch (JSONException e) {
			LeapConfigurationUtil.setResponseCode(422, exchange, e.getMessage());
			throw new IdentityServiceException("Unable to load the Property of SkipServices into JSONArray", e);
		}
	}

	/**
	 * This method is get the response XML file from the Oauth Validator Service
	 * 
	 * @param exchange
	 *            : Exchange Object
	 * @throws IdentityServiceException
	 * @throws JsonParserException
	 * @throws JSONException
	 * @throws TenantTokenValidationException
	 * @throws OauthValidatorException
	 */
	public void oauthValidatorTokenProcessBean(Exchange exchange)
			throws IdentityServiceException, TenantTokenValidationException {

		Map<String, Object> messageMap = exchange.getIn().getHeaders();
		String accountId = (String) messageMap.get(TenantSecurityConstant.ACCOUNT_ID);
		String siteId = (String) messageMap.get(TenantSecurityConstant.SITE_ID);
		long expirationTime = Long.parseLong(messageMap.get("tenant_token_expiration").toString());
		String tenantToken = (String) messageMap.get(TenantSecurityConstant.TENANT_TOKEN);
		try {
			if (accountSecurityService.validateTenantToken(accountId, siteId, expirationTime, tenantToken)) {
				validateTokenProcessBean(validateAccessToken(messageMap.get(OAuth.OAUTH_ACCESS_TOKEN).toString()));
			} else {
				throw new TenantTokenValidationException("Invalid tenantToken" + tenantToken);
			}
		} catch (UnsupportedOperationException | TrustValidationException | PropertiesConfigException
				| ValidationFailedException | ParserException | TenantTokenValidationException e) {
			throw new IdentityServiceException("unable to validate the access token from the header", e);
		}

	}// end of method OauthValidatorToken

	/**
	 * This method is used to get the validate value from the response XML file
	 * 
	 * @param responseXml
	 * 
	 * @param exchange
	 *            : Exchange Object
	 * @throws IdentityServiceException
	 * @throws JSONException
	 * @throws ValidationFailedException
	 * @throws ParserException
	 * @throws JsonParserException
	 */
	public void validateTokenProcessBean(String responseXml) throws ParserException, ValidationFailedException {
		logger.debug(" . validate method of IdentityService");
		String valid = fetchRequiredValuesFromDocument(generateDocumentFromString(responseXml));
		if (valid.equalsIgnoreCase(FALSE_KEY)) {
			throw new ValidationFailedException("Unauthenticated access to the service : verify following logs: "
					+ fetchRequiredValuesFromDocument(generateDocumentFromString(responseXml)));
		}

	}// end of method validate

	/**
	 * This method is used to add account details in the header.
	 * 
	 * @throws IdentityServiceException
	 */
	public void setAccountDetailsInHeader(Exchange exchange)
			throws IdentityServiceException, AccountFetchException, DigestMakeException {
		//for pipeline execution
		setDefaultValue(exchange);
		Message message = exchange.getIn();
		String isauthenticated = message.getHeader("isauthenticated", String.class);
		String servicetype = (String) message.getHeader(LeapHeaderConstant.SERVICETYPE_KEY);
		logger.debug(
				"loginServiceName from exchange : " + servicetype + "exchange body : " + exchange.getIn().getBody());
		if (isauthenticated.equalsIgnoreCase("true") || servicetype.equalsIgnoreCase("login")) {
			List<String> skipServices = new ArrayList<>();
			getSkipServicesList(skipServices, exchange);
			if (!skipServices.isEmpty() && (skipServices.contains(servicetype))) {
				removeTenantTokenDetails(exchange);
				return;
			}
			addTenantTokenDetails(exchange);
		} else {
			removeTenantTokenDetails(exchange);
		}
	}

	/**
	 * This method is used to set the default value to the counter in pipeline Execution.
	 */
	private void setDefaultValue(Exchange exchange) {
		exchange.setProperty(PipelineServiceConstant.PIPE_NAME_COUNTER, 0);
	}

	/**
	 * This method is used to add tenant_token and tenant_token_expiration in the
	 * header.
	 * 
	 * @throws IdentityServiceException
	 */
	private void addTenantTokenDetails(Exchange exchange) throws IdentityServiceException {
		Message message = exchange.getIn();
		String accountId = message.getHeader(LeapHeaderConstant.ACCOUNT_ID, String.class);
		String siteId = message.getHeader(LeapHeaderConstant.SITE_KEY, String.class);
		if (TenantSecurityUtil.isEmpty(accountId) || TenantSecurityUtil.isEmpty(siteId)) {
			throw new IdentityServiceException("Valid " + LeapHeaderConstant.ACCOUNT_ID + " - "
					+ LeapHeaderConstant.SITE_KEY + " is missing from request headers");
		}
		try {
			Map<String, Object> tenantTokenMap = accountSecurityService.getTenantTokenAttributes(accountId, siteId);
			message.setHeader("tenant_token", tenantTokenMap.get("tenantToken").toString());
			message.setHeader("tenant_token_expiration", tenantTokenMap.get("expiration"));
		} catch (AccountFetchException | DigestMakeException e) {
			throw new IdentityServiceException(
					"Unable to fetch the tenant token and expiration for the given account. " + e.getMessage());
		}
	}

	/**
	 * This method is used to remove tenant_token and tenant_token_expiration in the
	 * header if available.
	 * 
	 * @throws IdentityServiceException
	 */
	private void removeTenantTokenDetails(Exchange exchange) {
		Message message = exchange.getIn();
		message.removeHeader("tenant_token");
		message.removeHeader("tenant_token_expiration");
	}

	private void getSkipServicesList(List<String> skipServices, Exchange exchange) throws IdentityServiceException {
		try {
			if (this.properties == null)
				this.properties = LeapConfigurationUtil.loadingPropertiesFile();

			JSONArray servicesToSkip = new JSONArray(properties.getProperty(SKIP_SERVICES));
			for (int i = 0; i < servicesToSkip.length(); i++) {
				String service = servicesToSkip.getString(i);
				String[] serviceDetail = service.trim().split("-");
				skipServices.add(serviceDetail[2]);
			}
		} catch (Exception e) {
			throw new IdentityServiceException("Unable to fetch the skip services name. " + e.getMessage());
		}

	}

	/**
	 * This method is used to create the document object from the xml of the format
	 * String
	 * 
	 * @param xmlInput
	 *            : String
	 * @param exchnage
	 * @return Document Object
	 * @throws ParserException
	 */
	private Document generateDocumentFromString(String xmlInput) throws ParserException {
		logger.debug(". generateDoocumentFromString method of IdentityService" + xmlInput);
		Document xmlDocument = null;
		if (xmlInput != null) {
			this.builderFactory.setNamespaceAware(true);
			try {
				if (this.builder == null) {
					this.builder = this.builderFactory.newDocumentBuilder();
				}
				InputSource insrc = new InputSource(new StringReader(xmlInput));
				xmlDocument = this.builder.parse(insrc);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				throw new ParserException("Unable to Build the document object from the xml string ", e);
			}
		}
		return xmlDocument;
	}// end of method generateDocumentFromString

	/**
	 * This method is used to fetch the value from the Document Object
	 * 
	 * @param xmlDoc
	 *            : Document Object
	 * @return Valid value true or false
	 * @throws ParserException
	 * @throws ValidationFailedException
	 */
	private String fetchRequiredValuesFromDocument(Document xmlDoc) throws ParserException, ValidationFailedException {
		logger.debug(". generateXpathExpression method of IdentityService");
		try {
			String valid = xmlDoc.getChildNodes().item(0).getFirstChild().getFirstChild().getFirstChild()
					.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling()
					.getTextContent();
			return valid;
		} catch (Exception e) {
			throw new ValidationFailedException("Access Denied. Authentication failed - System error occurred", e);
		}

	}

	/**
	 * This method is used to validate the accessToken
	 * 
	 * @param accessToken
	 *            : accessToken String
	 * @param authorization
	 *            : authorization String
	 * @return validate XML file
	 * @throws IOException
	 * @throws SOAPException
	 * @throws UnsupportedOperationException
	 * @throws IdentityServiceException
	 * @throws TrustValidationException
	 * @throws PropertiesConfigException
	 */
	private String validateAccessToken(String accessToken)
			throws IdentityServiceException, TrustValidationException, PropertiesConfigException {
		logger.debug(". validateAccessToken method of IdentityService");
		try {
			if (this.properties == null) {
				this.properties = LeapConfigurationUtil.loadingPropertiesFile();
			}
			String outhValidatorURL = this.properties.getProperty(OAUTH_VALIDATOR_SERVIE_URL);
			if (this.soapConnFactory == null) {
				this.soapConnFactory = SOAPConnectionFactory.newInstance();
			}

			if (msgFct == null) {
				this.msgFct = MessageFactory.newInstance();
			}
			SOAPMessage message = this.msgFct.createMessage();
			SOAPPart mySPart = message.getSOAPPart();
			SOAPEnvelope myEnvp = mySPart.getEnvelope();
			Name xsdName = myEnvp.createName(XSD_NAMESPACE1);
			Name xsd1Name = myEnvp.createName(XSD_NAMESPACE2);
			myEnvp.addAttribute(xsdName, "http://org.apache.axis2/xsd");
			myEnvp.addAttribute(xsd1Name, "http://dto.oauth2.identity.carbon.wso2.org/xsd");
			SOAPBody body = myEnvp.getBody();
			Name bodyName = myEnvp.createName("xsd:validate");
			SOAPBodyElement gltp = body.addBodyElement(bodyName);
			SOAPElement myxsdvalidationReqDTO = gltp.addChildElement(VALIDATION_REQUEST_DTO, "xsd");
			SOAPElement myxsd1accessToken = myxsdvalidationReqDTO.addChildElement(ACCESS_TOKEN, "xsd");
			SOAPElement myxsd1tokenType = myxsd1accessToken.addChildElement(TOKEN_TYPE, "xsd1");
			myxsd1tokenType.addTextNode(TOKEN_TYPE_VALUE);
			SOAPElement myxsd1identifier = myxsd1accessToken.addChildElement(IDENTIFIER, "xsd1");
			myxsd1identifier.addTextNode(accessToken);
			MimeHeaders headers = message.getMimeHeaders();
			headers.addHeader(AUTHERIZATION,
					"Basic " + org.apache.commons.codec.binary.Base64.encodeBase64String("admin:admin".getBytes()));
			message.saveChanges();
			if (this.soapCon == null) {
				this.soapCon = this.soapConnFactory.createConnection();
			}
			message.writeTo(out1);
			out1.reset();
			SOAPMessage resp = this.soapCon.call(message, outhValidatorURL);
			resp.writeTo(out1);
			return new String(out1.toByteArray());
		} catch (IOException | UnsupportedOperationException | SOAPException e) {
			throw new IdentityServiceException(
					"unable to generate the response xml of the validate token " + e.getMessage(), e);
		}
	}// end of method validateAccessToken

	/**
	 * trust store path. this must contains server's certificate or Server's CA
	 * chain
	 * 
	 * @throws TrustValidationException
	 */
	private static void initializeClientCall() throws TrustValidationException {
		disablesslTrustValidation();
		System.setProperty("java.net.useSystemProxies", "true");
	}// ..end of the method initializeClientCall

	/**
	 * This method is used to disable the java trust store certificate
	 * 
	 * @throws TrustValidationException
	 * @throws TrustStoreCertificateException
	 */
	private static void disablesslTrustValidation() throws TrustValidationException {
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {

				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (NoSuchAlgorithmException |

				KeyManagementException e) {
			throw new TrustValidationException("Java Certificate Exception : " + e.getMessage());
		}
	}

}
