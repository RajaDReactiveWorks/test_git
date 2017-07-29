package com.getusroi.dynastore;

import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.RequestContext;
import com.getusroi.core.datagrid.DataGridService;
import com.getusroi.dynastore.config.DynaStoreConfigRequestContextException;
import com.getusroi.dynastore.config.DynaStoreConfigurationUnit;
import com.getusroi.dynastore.config.DynaStoreEventBuilderException;
import com.getusroi.dynastore.config.DynaStoreHelper;
import com.getusroi.dynastore.config.IDynaStoreConfigurationService;
import com.getusroi.dynastore.config.impl.DynaStoreConfigurationService;
import com.getusroi.eventframework.event.DynastoreEvent;
import com.getusroi.eventframework.event.ROIEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.TransactionalList;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;


/**
 * @author Bizruntime
 * DynaStoreSession  is wrapper on top of HazelCast DataStore abstract the way data is stored.<br>
 * DynaStoreSession is using the HazelCast IMap to Store the Data<br>
 */
public class ConfigurableDynaStoreSession {
	protected static final Logger logger = LoggerFactory.getLogger(ConfigurableDynaStoreSession.class);
	private IDynaStoreConfigurationService dynaConfigService;
	private DynaStoreHelper dynaHelper;
	private DynaStoreConfigurationUnit dynaConfigUnit;
	
	
	public ConfigurableDynaStoreSession(RequestContext reqCtx,String dynaStoreName,String dynaVersion){
		dynaConfigService=new DynaStoreConfigurationService();
		this.dynaHelper=new DynaStoreHelper();
		
		try {
			this.dynaConfigUnit=dynaConfigService.getDynaStoreConfigurationUnit(reqCtx, dynaStoreName, dynaVersion);
			if(dynaConfigUnit==null){
				//Throw Exception
			}
		} catch (DynaStoreConfigRequestContextException e) {
			//#TODO We need to throw the custom exception from from here
			e.printStackTrace();
		}
	}
	
	public Serializable getSessionData(RequestContext reqCtx,String key) throws DynaStoreRequestException{
		//Getter is non Transactional
		if(reqCtx==null || !reqCtx.isValid() ){
			 throw new DynaStoreRequestException("RequestContext for DynastoreSession is invalid");
		}
		//Changing Configurable dynastore to get data from running transactional context instead of IMAP, Need to review
		TransactionContext hcTransactionalContext=reqCtx.getHcTransactionalContext();
		//HazelcastInstance hcInstance=DataGridService.getDataGridInstance().getHazelcastInstance();
		String mapId=this.dynaConfigUnit.getDynaCollectionId();
		//get TransactionalMap
		TransactionalMap<String,Serializable>  dynaMap=hcTransactionalContext.getMap(mapId);
		//Map<String,Serializable>dynaMap=hcInstance.getMap(mapId);
		return dynaMap.get(key);
	}
	
	public Map<String,Serializable> getAllSessionData(RequestContext reqCtx,String key) throws DynaStoreRequestException{
		//Getter is non Transactional
		if(reqCtx==null || !reqCtx.isValid() ){
			 throw new DynaStoreRequestException("RequestContext for DynastoreSession is invalid");
		}
		//#TODO I have not made this method as configurable due to its return type,Need to verify it
		HazelcastInstance hcInstance=DataGridService.getDataGridInstance().getHazelcastInstance();
		String mapId=this.dynaConfigUnit.getDynaCollectionId();
		IMap<String,Serializable>dynaMap=hcInstance.getMap(mapId);
		return dynaMap;
	}
	
	
	
	public void addSessionData(RequestContext reqCtx,String key,Serializable value) throws DynaStoreRequestException{
		if(reqCtx==null || !reqCtx.isValid() ){
			 throw new DynaStoreRequestException("RequestContext for DynastoreSession is invalid");
		}else if(reqCtx.getHcTransactionalContext()==null){
			 throw new DynaStoreRequestException("RequestContext must have a DataGridTransactionalContext");
		}
		try{
			TransactionContext hcTransactionalContext=reqCtx.getHcTransactionalContext();
			String mapId=this.dynaConfigUnit.getDynaCollectionId();
			//get TransactionalMap
			TransactionalMap<String,Serializable>  dynaMap=hcTransactionalContext.getMap(mapId);
			boolean doesKeyExist=dynaMap.containsKey(key);
			if(!doesKeyExist){
				//Key Does not exist its an Add Operation
				dynaMap.put(key, value);
				logger.debug(".addSessionData() key="+key+"--value="+value); 
				//Handle event rasing post
				DynastoreEvent event=dynaHelper.entryAdditionPostHandler(this.dynaConfigUnit.getDynastoreConfiguration(), reqCtx);
				logger.debug(".addSessionData() Event Raised is event="+event);
				if(event!=null){
					event.addEventParam(event.PARAM_ENTRY_KEY, key);
					event.addEventParam(event.PARAM_ENTRY_VALUE, value);
					//add Event to EventList
					addEvent(hcTransactionalContext,reqCtx.getRequestId(),event);
				}
			}else{
				//Key already exist it is an Update operation
				Serializable oldValue=dynaMap.get(key);
				dynaMap.replace(key, value);
				DynastoreEvent event=dynaHelper.entryUpdationPostHandler(this.dynaConfigUnit.getDynastoreConfiguration(), reqCtx);
				logger.debug(".addSessionData() data is update. Event Raised is event="+event);
				if(event!=null){
					event.addEventParam(event.PARAM_ENTRY_KEY, key);
					event.addEventParam(event.PARAM_ENTRY_VALUE, value);
					event.addEventParam(event.PARAM_OLD_ENTRY_VALUE, oldValue);
					//add Event to EventList
					addEvent(hcTransactionalContext,reqCtx.getRequestId(),event);
				}//end of if
			}//end of else
		}catch(DynaStoreEventBuilderException exp){
			throw new DynaStoreRequestException("Failure in fulfilling the Dynastore{"+this.dynaConfigUnit.getKey()+"} addition Request for key="+key,exp);
		}
	}
	
	public void removeSessionData(RequestContext reqCtx,String key) throws DynaStoreRequestException{
		if(reqCtx==null || !reqCtx.isValid() ){
			 throw new DynaStoreRequestException("RequestContext for DynastoreSession is invalid");
		}else if(reqCtx.getHcTransactionalContext()==null){
			 throw new DynaStoreRequestException("RequestContext must have a DataGridTransactionalContext");
		}
		try{
			TransactionContext hcTransactionalContext=reqCtx.getHcTransactionalContext();
			String mapId=this.dynaConfigUnit.getDynaCollectionId();
			//get TransactionalMap
			TransactionalMap<String,Serializable>  dynaMap=hcTransactionalContext.getMap(mapId);
			//need deleted value for the Event
			Serializable value=dynaMap.get(key);
			dynaMap.delete(key);
			logger.debug(".removeSessionData() key="+key); 
			//Handle event rasing post
			DynastoreEvent event=dynaHelper.entryDeletionPostHandler(this.dynaConfigUnit.getDynastoreConfiguration(), reqCtx);
			logger.debug(".removeSessionData() Event Raised is event="+event);
			if(event!=null){
				event.addEventParam(event.PARAM_ENTRY_KEY, key);
				event.addEventParam(event.PARAM_ENTRY_VALUE, value);
				//add Event to EventList
				addEvent(hcTransactionalContext,reqCtx.getRequestId(),event);
			}
		}catch(DynaStoreEventBuilderException exp){
			throw new DynaStoreRequestException("Failure in fulfilling the Dynastore{"+this.dynaConfigUnit.getKey()+"} removal Request for key="+key,exp);
		}
	}
	
	public boolean terminateDynastore(RequestContext reqCtx)throws DynaStoreRequestException{
		if(reqCtx==null || !reqCtx.isValid() ){
			 throw new DynaStoreRequestException("RequestContext for DynastoreSession is invalid");
		}else if(reqCtx.getHcTransactionalContext()==null){
			 throw new DynaStoreRequestException("RequestContext must have a DataGridTransactionalContext");
		}
		TransactionContext hcTransactionalContext=reqCtx.getHcTransactionalContext();
		String mapId=this.dynaConfigUnit.getDynaCollectionId();
		//get TransactionalMap
		TransactionalMap<String,Serializable>  dynaMap=hcTransactionalContext.getMap(mapId);
		dynaMap.destroy();
		return true;
	}
	
	private void addEvent(TransactionContext hcTransactionContext,String requestId,DynastoreEvent dynaEvent){
		if(hcTransactionContext==null || dynaEvent==null)
			return ;//Nothing doing here

		logger.debug("addEvent() requestId=" + requestId + "--hcTransactionContextId=" + hcTransactionContext.getTxnId()+"-dynaEvent="+dynaEvent);
		TransactionalList<ROIEvent> txEventList = hcTransactionContext.getList(requestId);
		logger.debug("addEvent()  transactionalListSize=" + txEventList.size());
		txEventList.add(dynaEvent);
	}
	
	
}
