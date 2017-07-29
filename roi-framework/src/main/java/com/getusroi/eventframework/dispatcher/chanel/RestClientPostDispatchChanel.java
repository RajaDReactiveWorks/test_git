package com.getusroi.eventframework.dispatcher.chanel;

import java.io.IOException;
import java.io.Serializable;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.RequestContext;

/**
 * jsonConfig is {"postURI": "http://localhost:8080/"}
 * 
 * @author Bizruntime
 *
 */
public class RestClientPostDispatchChanel extends AbstractDispatchChanel {
	final Logger logger = LoggerFactory.getLogger(RestClientPostDispatchChanel.class);
	private String serverURL;// ="http://localhost:8080/";
	private CloseableHttpClient httpClient;

	public RestClientPostDispatchChanel(String chaneljsonconfig) throws DispatchChanelInitializationException {
		this.chaneljsonconfig = chaneljsonconfig;
		initializeFromConfig();
	}

	public RestClientPostDispatchChanel() {

	}

	/**
	 * This method is to dispatch message to rest url as post data
	 * 
	 * @param msg
	 *           :Object
	 */
	@Override
	public void dispatchMsg(Serializable msg,RequestContext requestContext,String eventId) throws MessageDispatchingException{
		logger.debug("inside dispatchmesg() of RestClient with TARGET URL {"+serverURL+"}");
		try {
			HttpPost postRequest = new HttpPost(serverURL);
			StringEntity input = new StringEntity(msg.toString());
			logger.debug("dispatch msg : " + input);
			input.setContentType("application/json");
			postRequest.setEntity(input);
			HttpResponse response = httpClient.execute(postRequest);
			if (response.getStatusLine().getStatusCode() != 201) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}
			try {
				closeResource();
			} catch (Throwable e) {
				throw new MessageDispatchingException("RestClientPostDispatchChanel failed close the resources");
			}
		} catch (IOException exp) {
			throw new MessageDispatchingException("RestClientPostDispatchChanel failed to Dispatch EventMsg to Server {"+serverURL+"}",exp);
		} finally {
			logger.debug("inside finally of rest client");
		}

	}

	/**
	 * This method is to configure rest url and create rest client
	 * 
	 * @param chaneljsonconfig
	 * @throws DispatchChanelInitializationException 
	 */
	// #TODO Write clean and better code for Chanel.
	public void initializeFromConfig() throws DispatchChanelInitializationException {
		logger.debug(".initializeFromConfig from restclinet");
		parseConfiguration(chaneljsonconfig);
		this.httpClient = HttpClients.createDefault();
		
		logger.debug("exiting initializeFromConfig from restclinet");
	}// end of method

	/**
	 * This method is used to parse json data
	 * 
	 * @param chaneljsonconfig
	 * @throws DispatchChanelInitializationException 
	 */
	private void parseConfiguration(String chaneljsonconfig) throws DispatchChanelInitializationException {
		logger.debug(".parseConfiguration from restclinet :"+chaneljsonconfig);

		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(chaneljsonconfig);
			JSONObject jsonObject = (JSONObject) obj;
			logger.debug("jsonObject : "+jsonObject);
			this.serverURL = (String) jsonObject.get("restpath");
			// logger.info("parseConfiguration:serverURL="+serverURL);
		} catch (ParseException e) {
			throw new DispatchChanelInitializationException("RestClientPostDispatchChanel failed to initialize");
		}
		logger.debug("exiting parseConfiguration from restclinet");

	}

	private void closeResource() throws Throwable{
		try {
			if(httpClient != null)
			httpClient.close();
		} catch (Exception exp) {
			logger.error("Error while closing the http restlet client");
			exp.printStackTrace();
		}
	}

	protected void finalize() throws Throwable {
		closeResource();
	}

}
