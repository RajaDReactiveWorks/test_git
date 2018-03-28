package com.attunedlabs.leap.load.resource;

import org.apache.camel.spring.Main;

import com.attunedlabs.leap.eventsubscription.routebuilder.GenericRetryRouteBuilder;
import com.attunedlabs.leap.eventsubscription.routebuilder.SubscriberRouteBuilder;
import com.attunedlabs.leap.eventsubscription.routebuilder.SubscriberEvaluationRouteBuilder;

public class CamelApplicationRun {
	
	
	  public CamelApplicationRun() {
     }
    
     
     public void startCamelApplication() throws Exception{
   	//Main makes it easier to run a Spring application
        Main main = new Main();
        // configure the location of the Spring XML file
        main.setApplicationContextUri("META-INF/spring/baseroute.xml");
        // enable hangup support allows Camel to detect when the JVM is terminated
//        main.enableHangupSupport();
        
        main.addRouteBuilder(new SubscriberEvaluationRouteBuilder());
        main.addRouteBuilder(new SubscriberRouteBuilder());
        main.addRouteBuilder(new GenericRetryRouteBuilder());
        // run and block until Camel is stopped (or JVM terminated)
        main.run(); 
     }
     
}
