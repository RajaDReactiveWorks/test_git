/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.attunedlabs.leap.camel;

import java.util.Map;
import java.util.Set;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Represents the component that manages {@link LeapHCEventEndpoint}.
 */
public class LeapComponent extends DefaultComponent {
	final Logger logger = LoggerFactory.getLogger(LeapComponent.class);
	private HazelcastInstance hazelcastInstance;
	
	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
   	 logger.debug(">>uri="+uri);
   	 logger.debug(">>remaining="+remaining);
   	 logger.debug(">>parameters="+parameters);
   	 
   	 if(remaining!=null && remaining.equalsIgnoreCase("Event")){
   		 return createHCEventEndpoint(uri,remaining,parameters);
   	 }else{
   		 String eventId=remaining;
      	 parameters.put("eventId", eventId);
      	 Endpoint endpoint = new LeapFwkEndpoint(uri, this);
          setProperties(endpoint, parameters);
          return endpoint;
   	 }
 
    }
    
    private Endpoint createHCEventEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception{
   	 String eventId=(String)parameters.get("eventId");
   	 hazelcastInstance=getOrCreateHCInstance(parameters);
   	 Endpoint endpoint = new LeapHCEventEndpoint(uri, this,hazelcastInstance);
   	 setProperties(endpoint, parameters);
       return endpoint;
    }
    
    private HazelcastInstance getOrCreateHCInstance(Map<String, Object> parameters){
   	 // Query param named 'hazelcastInstance' (if exists) overrides the instance that was set
       HazelcastInstance hzInstance = resolveAndRemoveReferenceParameter(parameters, "GridInstanceName", HazelcastInstance.class);

       // check if an already created instance is given then just get instance by its name.
       if (hzInstance == null && parameters.get("GridInstanceName") != null) {
           hzInstance = Hazelcast.getHazelcastInstanceByName((String) parameters.get("GridInstanceName"));
       }
       
       //No instance found by this name than try to get any running HazelcastInstance
       if(hzInstance==null){
      	 Set<HazelcastInstance> hcInstanceList=Hazelcast.getAllHazelcastInstances();
      	 logger.debug(">>hcInstanceList="+hcInstanceList);
      	for(HazelcastInstance hcIns:hcInstanceList){
      		hzInstance=hcIns;
      		logger.debug(">>Inside for loop hcInstanceList="+hcIns.toString());
      		break;
      	}
      }
       

       // Now create onw instance component
       if (hzInstance == null) {
           if (hazelcastInstance == null) {
               hazelcastInstance = Hazelcast.newHazelcastInstance();
           }
           hzInstance = hazelcastInstance;
       }
       return hzInstance;
    }
}
