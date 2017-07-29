package com.getusroi.eventframework.dispatcher.chanel;

import java.io.Serializable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.RequestContext;
import com.getusroi.core.datagrid.DataGridService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class HazelcastTopicDispatchChanel extends AbstractDispatchChanel {
	final static Logger logger = LoggerFactory.getLogger(HazelcastTopicDispatchChanel.class);
	private String hcQueueName;
	
	public HazelcastTopicDispatchChanel(){
		
	}
	public HazelcastTopicDispatchChanel(String chaneljsonconfig) throws DispatchChanelInitializationException{
		super.chaneljsonconfig=chaneljsonconfig;
		initializeFromConfig();
	}
	
	public void dispatchMsg(Serializable msg,RequestContext requestContext,String eventId) throws MessageDispatchingException{
		try {
				logger.debug(".dispatchMsg() start dispatchin MSG to queue="+hcQueueName+" Msg="+msg.toString());
				HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
				logger.debug(".dispatchMsg() Got Hazelcast Instance");
				IQueue<Object> queue = hazelcastInstance.getQueue( hcQueueName );
				logger.debug(".dispatchMsg() Got Queue from Hazelcast Instance");
				queue.put(msg );
				logger.debug(".dispatchMsg() end msg added to queue="+hcQueueName);
		} catch (InterruptedException e) {
			throw new MessageDispatchingException("HazelcastQueueDispatchChanel Failed to dispatch eventMsg to Hazelcast queue {"+hcQueueName+"}",e);
		}

	}

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
