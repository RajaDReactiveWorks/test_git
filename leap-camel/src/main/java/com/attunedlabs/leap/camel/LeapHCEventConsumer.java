
package com.attunedlabs.leap.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

/**
 * The HelloWorld consumer.
 */
public class LeapHCEventConsumer extends ScheduledPollConsumer {
	final Logger logger = LoggerFactory.getLogger(LeapHCEventConsumer.class); 
	private final LeapHCEventEndpoint endpoint;

    public LeapHCEventConsumer(LeapHCEventEndpoint endpoint, Processor processor,HazelcastInstance hazelcastInstance) {
        super(endpoint, processor);
        this.endpoint=endpoint;
    }
    
    protected int poll() throws Exception { 
   	 String queueName=endpoint.getEventId();
   	 String subscriberId=endpoint.getSubscriberId();
   	 HazelcastInstance hazelcastInstance=endpoint.getHcInstance();
   	 //logger.debug(">>poll called-queueName="+queueName+" : subscriberId="+subscriberId+"-hazelcastInstance="+hazelcastInstance);
   	 IQueue queue=hazelcastInstance.getQueue(queueName+"-"+subscriberId);
   	 //logger.debug(">>poll hcqueue reference"+queue);
   	 int count=0;
   	 while(true){
	   	 Object value=queue.poll();
	   	 if(value==null)
	   		 return count;
	   	 Exchange exchange =getEndpoint().createExchange();
	   	 exchange.getIn().setBody(value);
	   	 exchange.getIn().setHeader("MESH_EVENT_QUEUE_NAME", queueName);
	   	 try{
	   		 getProcessor().process(exchange); 
	   		 logger.debug(">>poll called for queueName="+queueName+" msg="+value);
	   	 }catch (Exception e) {
	   		 exchange.setException(e);
	   	 }
	   	
	       if (exchange.getException() != null) {
	      	 	getExceptionHandler().handleException(String.format("Error processing exchange for hazelcastQueue with queueName="+queueName), exchange, exchange.getException());
	       }
   	 }
   	
   }

   
   protected void doStart() throws Exception { 
       // Pre-Start:
       // Place code here to execute just before start of processing.
       super.doStart();
       // Post-Start:
       // Place code here to execute just after start of processing.
   }

   @Override
   protected void doStop() throws Exception { 
       // Pre-Stop:
       // Place code here to execute just before processing stops.
       super.doStop();
       // Post-Stop:
       // Place code here to execute just after processing stops.
   }
}
