package com.getusroi.eventframework.dispatcher.chanel;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.config.RequestContext;
import com.getusroi.core.datagrid.DataGridService;
import com.getusroi.eventframework.config.EventFrameworkConfigurationException;
import com.getusroi.eventframework.config.IEventFrameworkConfigService;
import com.getusroi.eventframework.config.impl.EventFrameworkConfigService;
import com.getusroi.eventframework.jaxb.EventSubscription;
import com.getusroi.eventframework.jaxb.Subscriber;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class HazelcastQueueDispatchChanel extends AbstractDispatchChanel {
	final static Logger logger = LoggerFactory.getLogger(HazelcastQueueDispatchChanel.class);
	private String hcQueueName;
	
	public HazelcastQueueDispatchChanel(){
		
	}
	public HazelcastQueueDispatchChanel(String chaneljsonconfig) throws DispatchChanelInitializationException{
		super.chaneljsonconfig=chaneljsonconfig;
		initializeFromConfig();
	}
	
	public void dispatchMsg(Serializable msg,RequestContext rqCtx,String eventId) throws MessageDispatchingException{
		try {
				logger.debug(".dispatchMsg() start dispatchin MSG to queue="+hcQueueName+" Msg="+msg.toString());
				HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
				logger.debug(".dispatchMsg() Got Hazelcast Instance");
				ConfigurationContext configContext = new ConfigurationContext(rqCtx.getTenantId(), rqCtx.getSiteId(),null,null,null);
				IEventFrameworkConfigService evtFwkConfiService=new EventFrameworkConfigService();
				try {
					//check event subscription for 
					EventSubscription eventSubscription=evtFwkConfiService.getEventSubscriptionConfiguration(configContext, eventId);
					if(eventSubscription.isEnabled()){
					List<Subscriber> subscriberList=eventSubscription.getSubscriber();
					if(!(subscriberList.isEmpty())|| subscriberList !=null){
						for(Subscriber subscribe:subscriberList){
							if(subscribe.isEnabled()){
								IQueue<Object> queue = hazelcastInstance.getQueue(eventId.trim()+"-"+subscribe.getId().trim());
								logger.debug(".dispatchMsg() Got Queue from Hazelcast Instance");
								queue.put(msg );
								logger.debug(".dispatchMsg() end msg added to queue="+hcQueueName);
							}//end of if(subscribe.isEnabled())
						}//end of for
					}//end of if(!(subscriberList.isEmpty())|| subscriberList !=null)
					}else{
						throw new MessageDispatchingException("EventSubscription for event : "+eventId+", configuration context : "+configContext+", is disabled");
					}					
				} catch (EventFrameworkConfigurationException e) {
					throw new MessageDispatchingException("Unable to get the EventSubscription for event : "+eventId+", configuration context : "+configContext);
				}				
		} catch (InterruptedException e) {
			throw new MessageDispatchingException("HazelcastQueueDispatchChanel Failed to dispatch eventMsg to Hazelcast queue {"+hcQueueName+"}",e);
		}
	}//end of method

	@Override
	public void initializeFromConfig() throws DispatchChanelInitializationException {
		try {
			parseConfiguration(this.chaneljsonconfig);
		
		} catch (ParseException e) {
			throw new DispatchChanelInitializationException("Failed to Parse configuration for HazelcastQueueDispatch Chanel",e);
		}

	}
	
	/**
	 * This method is to parse json configuration
	 * {queueName=}
	 * @param chaneljsonconfig
	 * @throws ParseException 
	 */
	private void parseConfiguration(String chaneljsonconfig) throws ParseException {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(chaneljsonconfig);
		JSONObject jsonObject = (JSONObject) obj;
		this.hcQueueName = (String) jsonObject.get("queueName");
	}
}
