package com.attunedlabs.eventframework.config;

import org.xml.sax.SAXException;

public class EventingXmlXSDValidationException extends SAXException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4326015570666738747L;

	@Override
	public String getMessage() {
			String validationError="unable to validate EventFramwork.xml with leapeventframework.XSD";
		return validationError;
	}
}
