package com.attunedlabs.featuremetainfo.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CustomLoaderFromJar extends URLClassLoader {
	final static Logger logger = LoggerFactory.getLogger(CustomLoaderFromJar.class);

	private URL[] urls;
	
	public CustomLoaderFromJar(URL[] urls) {
		super(urls);
		this.urls=urls;
		}

	@Override
	public URL getResource(String name) {
		 /* eliminate the "/" at the beginning if there is one to avoid 
	    conflicts with the "!/" at the end of the URL */
	    if (name.startsWith("/"))
	   	 name.substring(1);
	    /* prepend "jar:" to indicate that the URL points to a jar file and use 
	    "!/" as the separator to indicate that the resource being loaded is 
	    inside the jar file */
	    String URLstring = "jar:" + getURLs()[0] + "!/" + name;
	    logger.debug("URL string :"+URLstring);
	    try {
	        return new URL(URLstring);
	    } catch (MalformedURLException exception) {
	        logger.error("There was something wrong with the URL representing this resource!");
	        logger.error("URL=\"" + URLstring + "\"");
	       return null;			
	     }
	}//end of method
	
}
