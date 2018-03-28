package com.attunedlabs.eventframework.processor;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.event.LeapEvent;

public class LeapEventRunnableTask implements Runnable,Serializable{
	final Logger logger = LoggerFactory.getLogger(LeapEventRunnableTask.class);
	
	private LeapEvent leapEvt;
	//private EventProcessors evtProcs;
	
	public LeapEventRunnableTask(LeapEvent leapEvt){
		this.leapEvt=leapEvt;
		
	}
	
	public void run() {
		logger.debug("RoiEvent="+leapEvt);
		
		/*try{
			List <IEventProcessor> procList=getEventProcessors();
			for(IEventProcessor evtProcIns:procList){
				logger.info(" Processor is-"+ evtProcIns.getClass());
				evtProcIns.processEvent(leapEvt);
			}
		}
		catch(Exception exp){
			exp.printStackTrace();
		}*/
		
		// once processor completed post to Dispatcher
		//IEventFrameworkIDispatcherService dispatcherService=new EventFrameworkDispatcherService();
		//dispatcherService.dispatchforEvent(leapEvt);
	}

	//#TODO the initialization should be out of this section into the generic
	//initantiation beans
	private IEventProcessor getProcessorInstance(String fqcn)throws EventProcessorInstantiationException{
		Class eventProcClass=null;
		IEventProcessor eventProcObj=null;
		try{
			eventProcClass=Class.forName(fqcn);
			eventProcObj=(IEventProcessor)eventProcClass.newInstance();
		} catch (InstantiationException | ClassNotFoundException |IllegalAccessException e) {
			throw new EventProcessorInstantiationException();
		}
		return eventProcObj;
	}
	
	
	

	
	public String toString() {
		return "LeapEventRunnableTask [leapEvt=" + leapEvt + "]";
	}

}
