package com.getusroi.eventframework.dispatcher.transformer;


import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.eventframework.event.ROIEvent;

public class ROIEventXmlTransformer implements IROIEventTransformer{

	protected static final Logger logger = LoggerFactory.getLogger(ROIEventXmlTransformer.class);

	private String xsltname=null;
	private String xsltAsString=null;

	
	
	
	public ROIEventXmlTransformer(String xsltname,String xsltAsString) {
		this.xsltname = xsltname;
		this.xsltAsString=xsltAsString;
	}




	@Override
	public Serializable transformEvent(ROIEvent roievent)
			throws ROIEventTransformationException {
		
		logger.debug("inside transformEvent method of ROIEventXmlTransformer");
		XmlTransformerHelper xmlTransformerHelper=new XmlTransformerHelper();

		//converting ROIevent object to xml
		String eventxml=xmlTransformerHelper.convertEventObjectToXml(roievent);
		logger.debug("roi event xml string : "+eventxml);
		
		//converting eventxml to custom xml using xslt
		String customeventxml=xmlTransformerHelper.createCustomXml(eventxml,xsltname,xsltAsString);
		
		logger.debug("roi custom event xml : "+customeventxml);
		
		return customeventxml;
	}
	
	
	

}
