package com.attunedlabs.eventframework.dispatcher.transformer;


import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.event.LeapEvent;

public class LeapEventXmlTransformer implements ILeapEventTransformer{

	protected static final Logger logger = LoggerFactory.getLogger(LeapEventXmlTransformer.class);

	private String xsltname=null;
	private String xsltAsString=null;

	
	
	
	public LeapEventXmlTransformer(String xsltname,String xsltAsString) {
		this.xsltname = xsltname;
		this.xsltAsString=xsltAsString;
	}




	@Override
	public Serializable transformEvent(LeapEvent leapevent)
			throws LeapEventTransformationException {
		
		logger.debug("inside transformEvent method of LeapEventXmlTransformer");
		XmlTransformerHelper xmlTransformerHelper=new XmlTransformerHelper();

		//converting Leapevent object to xml
		String eventxml=xmlTransformerHelper.convertEventObjectToXml(leapevent);
		logger.debug("leap event xml string : "+eventxml);
		
		//converting eventxml to custom xml using xslt
		String customeventxml=xmlTransformerHelper.createCustomXml(eventxml,xsltname,xsltAsString);
		
		logger.debug("leap custom event xml : "+customeventxml);
		
		return customeventxml;
	}
	
	
	

}
