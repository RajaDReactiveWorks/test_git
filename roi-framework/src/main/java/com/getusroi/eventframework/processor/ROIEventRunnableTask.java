package com.getusroi.eventframework.processor;

import java.io.Serializable;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.getusroi.eventframework.event.ROIEvent;

public class ROIEventRunnableTask implements Runnable,Serializable{
	final Logger logger = LoggerFactory.getLogger(ROIEventRunnableTask.class);
	
	private ROIEvent roiEvt;
	//private EventProcessors evtProcs;
	
	public ROIEventRunnableTask(ROIEvent roiEvt){
		this.roiEvt=roiEvt;
		
	}
	
	public void run() {
		logger.debug("RoiEvent="+roiEvt);
		
		/*try{
			List <IEventProcessor> procList=getEventProcessors();
			for(IEventProcessor evtProcIns:procList){
				logger.info(" Processor is-"+ evtProcIns.getClass());
				evtProcIns.processEvent(roiEvt);
			}
		}
		catch(Exception exp){
			exp.printStackTrace();
		}*/
		
		// once processor completed post to Dispatcher
		//IEventFrameworkIDispatcherService dispatcherService=new EventFrameworkDispatcherService();
		//dispatcherService.dispatchforEvent(roiEvt);
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
		return "ROIEventRunnableTask [roiEvt=" + roiEvt + "]";
	}

}
