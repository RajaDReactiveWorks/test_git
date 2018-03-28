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

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class LeapEventComponentTest extends CamelTestSupport
{
	static final Logger logger = LoggerFactory.getLogger(LeapEventComponentTest.class);

	@Test
    public void testHelloWorld() throws Exception {
        HazelcastInstance hcInstance=Hazelcast.newHazelcastInstance();
   	  IQueue queue=hcInstance.getQueue("PicEvent-mockroute");
   	  queue.add("Test Msg");
   	  logger.debug("Test-queue size="+queue.size());
   	  MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);       
        assertMockEndpointsSatisfied();
       
       
    }
   
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("leap://Event?eventId=PicEvent&subscriberId=mockroute")
                	.log(LoggingLevel.DEBUG, ">>Processing ${body}")
                  .to("mock:result");
           }
        };
    }
    
    public static void main(String args[]) throws Exception{
   	 HazelcastInstance hcInstance=Hazelcast.newHazelcastInstance();
  	  	 IQueue queue=hcInstance.getQueue("PicEvent-mockroute");
  	  	 queue.add("Test Msg");
  	  	 System.out.println("Test-queue size="+queue.size());
  	  logger.debug("Test-queue size="+queue.size());
   	 
   	 CamelContext context = new DefaultCamelContext();
   	 context.addRoutes(new RouteBuilder() {
   		 public void configure() {
             from("leap://Event?eventId=PicEvent&subscriberId=mockroute")
             	.log(LoggingLevel.DEBUG, ">>Processing ${body}")
               .to("mock:result");
             
             from("leap://Event?eventId=PicCompletedEvent&subscriberId=mockroute")
           	.log(LoggingLevel.DEBUG, ">>Processing ${body}")
             .to("mock:result"); 
        }
   	});
   	 context.start();
   	 queue.add("Test Msg2");
   	 //Thread.currentThread().wait(2000);
   	 logger.debug("Test main exiting queueSize="+queue);
    }
}
