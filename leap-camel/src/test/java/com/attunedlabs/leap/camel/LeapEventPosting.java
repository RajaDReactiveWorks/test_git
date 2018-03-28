package com.attunedlabs.leap.camel;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class LeapEventPosting {
	public static void main(String args[]){
		 HazelcastInstance hcInstance=Hazelcast.newHazelcastInstance();
	  	  	IQueue queue=hcInstance.getQueue("PicEvent");
	  	   queue.add("Test Msg 3");
	  	  System.out.println("Test-queue size="+queue.size());
	}
}
