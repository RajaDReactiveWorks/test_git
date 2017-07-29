package com.getusroi.integrationfwk.activities.bean;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.camel.Exchange;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.RequestContext;
import com.getusroi.integrationfwk.config.jaxb.PipeActivity;
import com.getusroi.integrationfwk.config.jaxb.XsltPathMap;
import com.getusroi.mesh.MeshHeader;
import com.getusroi.mesh.MeshHeaderConstant;
import com.getusroi.staticconfig.IStaticConfigurationService;
import com.getusroi.staticconfig.StaticConfigFetchException;
import com.getusroi.staticconfig.StaticConfigInitializationException;
import com.getusroi.staticconfig.factory.StaticConfigurationFactory;
import com.getusroi.staticconfig.impl.AccessProtectionException;

public class XsltEnricher {
	private Logger logger = LoggerFactory.getLogger(XsltEnricher.class.getName());
	private IStaticConfigurationService iStaticConfigurationService;

	/**
	 * Bean to process get the data to process and is called only when the
	 * configuration exists
	 * 
	 * @param exchange
	 * @throws ActivityEnricherException
	 * @throws StaticConfigInitializationException
	 * @throws StaticConfigFetchException
	 * @throws AccessProtectionException
	 * @throws FileNotFoundException
	 */
	public void processorBean(Exchange exchange) throws ActivityEnricherException, StaticConfigFetchException,
			StaticConfigInitializationException, AccessProtectionException {
		// JSONObject jsonObject = null;
		JSONObject jsonObject2 = new JSONObject();
		String xmlIn = null;
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		logger.debug("pipelineActivity in XSLTEnricher : " + pipeactivity.getXSLTEnricherActivity().getName());
		List<XsltPathMap> xsltPathList = pipeactivity.getXSLTEnricherActivity().getXsltpathMapper().getXsltPathMap();
		logger.debug("xsltPathList size in XSLTEnricher : " + xsltPathList.size());
		String xmlData = exchange.getIn().getBody(String.class);
		String temp;
		for (XsltPathMap pathMapper : xsltPathList) {
			String xsltNamePath = pathMapper.getFilePath();
			temp = xmlData;
			String xmlOut;
			try {
				xmlOut = xsltTransformFactory(temp, xsltNamePath, exchange);
				xmlData = xmlOut;
				logger.debug("Check-in for-each: " + xmlData);
			} catch (FileNotFoundException e) {
				throw new StaticConfigFetchException("File not found");
			}
		}
		logger.debug("ExchangeBodySet from Xslt-Enricher: " + xmlData);
		exchange.getIn().setBody(xmlData);
	}// ..end of the method

	/**
	 * locally used to instantiate the transformation factory and to process the
	 * transformation
	 * 
	 * @param inputXml
	 * @param xsltName
	 * @return transformed xml
	 * @throws ActivityEnricherException
	 * @throws StaticConfigInitializationException
	 * @throws StaticConfigFetchException
	 * @throws FileNotFoundException
	 * @throws AccessProtectionException
	 */
	private String xsltTransformFactory(String inputXml, String xsltName, Exchange exchange)
			throws ActivityEnricherException, StaticConfigFetchException, StaticConfigInitializationException,
			FileNotFoundException, AccessProtectionException {
		logger.debug(".xsltTransformFactory..." + xsltName);
		TransformerFactory factory = TransformerFactory.newInstance();

		MeshHeader meshHeader = (MeshHeader) exchange.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);
		RequestContext requestContext = meshHeader.getRequestContext();
		logger.debug(".xsltTransformFactory..." + requestContext);
		// fetching the filepath from the xsltTransformFactory
		try {
			iStaticConfigurationService = StaticConfigurationFactory.getFilemanagerInstance();
			logger.debug(".xsltTransformFactory..." + iStaticConfigurationService);
		} catch (InstantiationException | IllegalAccessException e1) {
			throw new ActivityEnricherException("Unable to initialize file-manager for configurations.." + xsltName,
					e1);
		}
		String xsltFile = null;
		try{
		xsltFile = iStaticConfigurationService.getStaticConfiguration(requestContext, xsltName);
		logger.debug(".XsltPath from specific tenant: " + xsltFile);
		}catch (StaticConfigFetchException | StaticConfigInitializationException |
				 AccessProtectionException  e) {
			RequestContext reCntx=new RequestContext(ActivityConstant.GLOBAL_TENANT_ID,ActivityConstant.GLOBAL_SITE_ID,requestContext.getFeatureGroup(), requestContext.getFeatureName(),requestContext.getImplementationName(), requestContext.getVendor(),requestContext.getVersion());
			xsltFile = iStaticConfigurationService.getStaticConfiguration(requestContext, xsltName);
			logger.debug("xslt File Fetch from Global :  " + xsltFile);
		} catch (Exception e) {
			
			RequestContext reCntx=new RequestContext(ActivityConstant.GLOBAL_TENANT_ID,ActivityConstant.GLOBAL_SITE_ID,requestContext.getFeatureGroup(), requestContext.getFeatureName(),requestContext.getImplementationName(), requestContext.getVendor(),requestContext.getVersion());
			xsltFile = iStaticConfigurationService.getStaticConfiguration(reCntx, xsltName);
			logger.debug("xslt File Fetch from Global After Exception :  " + xsltFile);

		}
		if(xsltFile==null){
			RequestContext reCntx=new RequestContext(ActivityConstant.GLOBAL_TENANT_ID,ActivityConstant.GLOBAL_SITE_ID,requestContext.getFeatureGroup(), requestContext.getFeatureName(),requestContext.getImplementationName(), requestContext.getVendor(),requestContext.getVersion());
			xsltFile = iStaticConfigurationService.getStaticConfiguration(reCntx, xsltName);
			logger.debug(".XsltPath Fetch from Global :  " + xsltFile);

		}
		InputStream in;
		try {
			in = IOUtils.toInputStream(xsltFile, "UTF-8");
		} catch (IOException e1) {
			throw new StaticConfigFetchException("Unable to get the config file for processing..", e1);
		}
		logger.debug("input stream in xsltTransformFactor : " + in);
		Source xslt;
		xslt = new StreamSource(in);
		logger.debug("xslt data: " + xslt);
		Transformer transformer = null;
		try {
			transformer = factory.newTransformer(xslt);
		} catch (TransformerConfigurationException e) {
			throw new ActivityEnricherException(
					"Unable to instantiate xsltTransformation..withe the current configuration", e);
		}
		Source text = new StreamSource(new StringReader(inputXml));
		logger.debug("xml data to be transformed : "+text);
		StringWriter outWriter = new StringWriter();
		StreamResult result = new StreamResult(outWriter);
		try {
			transformer.transform(text, result);
		} catch (TransformerException e) {
			throw new ActivityEnricherException(
					"Unable to perform xsltEnrichment..an exception occured in the transformation", e);
		}
		StringBuffer sb = outWriter.getBuffer();
		return sb.toString();
	}// ..end of the method
	

}