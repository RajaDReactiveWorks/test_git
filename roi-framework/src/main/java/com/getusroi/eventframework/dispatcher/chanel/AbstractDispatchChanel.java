package com.getusroi.eventframework.dispatcher.chanel;

import java.io.Serializable;

import com.getusroi.config.RequestContext;


public abstract class AbstractDispatchChanel {
	protected String chaneljsonconfig;
	public abstract void dispatchMsg(Serializable msg,RequestContext reqContext,String evendId)throws MessageDispatchingException;
	
	public String getChaneljsonconfig() {
		return chaneljsonconfig;
	}
	public void setChaneljsonconfig(String chaneljsonconfig) throws DispatchChanelInitializationException {
		this.chaneljsonconfig = chaneljsonconfig;
		initializeFromConfig();
	}
	public abstract void initializeFromConfig()throws DispatchChanelInitializationException ;
}
