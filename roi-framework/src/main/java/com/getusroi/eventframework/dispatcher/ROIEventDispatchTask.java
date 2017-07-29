package com.getusroi.eventframework.dispatcher;

import java.io.Serializable;

import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.ConfigurationContext;
import com.getusroi.core.BeanDependencyResolveException;
import com.getusroi.core.BeanDependencyResolverFactory;
import com.getusroi.core.IBeanDependencyResolver;
import com.getusroi.eventframework.dispatcher.chanel.AbstractDispatchChanel;
import com.getusroi.eventframework.dispatcher.chanel.DispatchChanelService;
import com.getusroi.eventframework.dispatcher.chanel.MessageDispatchingException;
import com.getusroi.eventframework.dispatcher.transformer.GenericROIEventJsonTransformer;
import com.getusroi.eventframework.dispatcher.transformer.IROIEventTransformer;
import com.getusroi.eventframework.dispatcher.transformer.ROIEventTransformationException;
import com.getusroi.eventframework.dispatcher.transformer.ROIEventXmlTransformer;
import com.getusroi.eventframework.event.ROIEvent;
import com.getusroi.osgi.helper.BeanResolutionHelper;
import com.getusroi.osgi.helper.OSGIEnvironmentHelper;

public class ROIEventDispatchTask implements Runnable, Serializable {
	private static final long serialVersionUID = 7935162772233460912L;
	private static final Logger logger = LoggerFactory.getLogger(ROIEventDispatchTask.class);

	private ROIEvent roiEvt;
	private String chanelId;
	private String tranformationBeanFQCN;
	private String xslname;
	private String xsltAsString;

	public ROIEventDispatchTask(ROIEvent roiEvt, String chanelId, String tranformationBeanFQCN, String xslname,String xsltAsString) {
		this.roiEvt = roiEvt;
		this.chanelId = chanelId;
		this.tranformationBeanFQCN = tranformationBeanFQCN;
		this.xslname = xslname;
		this.xsltAsString=xsltAsString;

	}

	public void run() {
		IROIEventTransformer transformer = null;
		try {
			logger.debug("run() for EventId=" + roiEvt.getEventId() + "--chanelId=" + chanelId + "--transformationBean=" + tranformationBeanFQCN + ", xslName : "
					+ xslname);

			// decide which transformation to call
			if (tranformationBeanFQCN != null) {
				logger.debug("tansformation bean is not null");
				// Transform the Event msg
				transformer = getTransformerInstance(tranformationBeanFQCN);
			} else if (xslname != null) {
				logger.debug("xslname is not null");
				transformer = getXmlXsltTransformerInstance(xslname);
			} else {
				logger.debug("its for json");
				transformer = getJSONTransformerInstance();
			}

			Serializable transformedMsg = transformer.transformEvent(roiEvt);
			// Dispatch the transformed Event msg to the chanel
			//#TODO fIX Dispatch Chanel service than handle the error
			DispatchChanelService disChanelSer = DispatchChanelService.getDispatchChanelService();
			AbstractDispatchChanel chanel = disChanelSer.getDispatchChanel(roiEvt.getRequestContext(), chanelId);
			
			chanel.dispatchMsg(transformedMsg,roiEvt.getRequestContext(),roiEvt.getEventId());
		} catch (ROIEventTransformationException | BeanDependencyResolveException e) {
			//#TODO fIX exception Handling and posting to DeadLetter Chanel
			logger.error("ROIEventDispatching Failed for Event "+roiEvt, e);
			
		} catch (MessageDispatchingException e) {
			//#TODO fIX exception Handling and posting to DeadLetter Chanel
			logger.error("ROIEventDispatching Failed for Event "+roiEvt, e);
		}
	}

	private IROIEventTransformer getTransformerInstance(String fqcn) throws BeanDependencyResolveException {
		logger.debug("inside getTransformerInstance : " + fqcn);
		IROIEventTransformer transformer = null;
		logger.debug("OSGIEnvironmentHelper.isOSGIEnabled : "+OSGIEnvironmentHelper.isOSGIEnabled);
		if(OSGIEnvironmentHelper.isOSGIEnabled){
			BeanResolutionHelper beanResolutionHelper=new BeanResolutionHelper();
			try {
				transformer=(IROIEventTransformer)beanResolutionHelper.resolveBean(IROIEventTransformer.class.getName(),fqcn);
			} catch (InvalidSyntaxException e) {
				logger.error("Unable to Load/instantiate CustomEventTransformer="+fqcn);
				throw new BeanDependencyResolveException("Unable to Load/instantiate CustomEventTransformer="+fqcn,e);
			}
		}else{

		IBeanDependencyResolver beanResolver = BeanDependencyResolverFactory.getBeanDependencyResolver();
		
		if(fqcn.equalsIgnoreCase("com.getusroi.eventframework.dispatcher.transformer.GenericROIEventJsonTransformer")){
				return new GenericROIEventJsonTransformer();
		}
		transformer = (IROIEventTransformer) beanResolver.getBeanInstance(IROIEventTransformer.class, fqcn);
		}
		return transformer;
	}

	/**
	 * This method is to generate XSL-XSLT transformer Instance
	 * 
	 * @param xslname
	 * @return
	 */
	private IROIEventTransformer getXmlXsltTransformerInstance(String xslname) {
		logger.debug("inside getXmlXsltTransformationInstance ");
		IROIEventTransformer transformer = new ROIEventXmlTransformer(xslname,xsltAsString);
		return transformer;

	}

	private IROIEventTransformer getJSONTransformerInstance() {
		logger.debug("inside getJSONTransformerInstance ");
		IROIEventTransformer transformer = new GenericROIEventJsonTransformer();

		return transformer;
	}

}
