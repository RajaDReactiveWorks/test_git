package com.attunedlabs.eventframework.dispatcher;

import java.io.Serializable;

import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.core.BeanDependencyResolverFactory;
import com.attunedlabs.core.IBeanDependencyResolver;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.chanel.AbstractDispatchChanel;
import com.attunedlabs.eventframework.dispatcher.chanel.DispatchChanelService;
import com.attunedlabs.eventframework.dispatcher.transformer.GenericLeapEventJsonTransformer;
import com.attunedlabs.eventframework.dispatcher.transformer.ILeapEventTransformer;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventXmlTransformer;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.osgi.helper.BeanResolutionHelper;
import com.attunedlabs.osgi.helper.OSGIEnvironmentHelper;

public class LeapEventDispatchTask implements Serializable {
	private static final long serialVersionUID = 7935162772233460912L;
	private static final Logger logger = LoggerFactory.getLogger(LeapEventDispatchTask.class);

	private LeapEvent leapEvt;
	private String chanelId;
	private String tranformationBeanFQCN;
	private String xslname;
	private String xsltAsString;

	public LeapEventDispatchTask(LeapEvent leapEvt, String chanelId, String tranformationBeanFQCN, String xslname,
			String xsltAsString) {
		this.leapEvt = leapEvt;
		this.chanelId = chanelId;
		this.tranformationBeanFQCN = tranformationBeanFQCN;
		this.xslname = xslname;
		this.xsltAsString = xsltAsString;

	}

	/**
	 * dispatching to channel task given specific amount of time.
	 * 
	 * @throws LeapEventTransformationException
	 * @throws MessageDispatchingException
	 * @throws EventFrameworkDispatcherException
	 * 
	 */
	public void dispatchToChannel() throws LeapEventTransformationException, MessageDispatchingException {
		ILeapEventTransformer transformer = null;
		try {
			logger.debug("run() for EventId=" + leapEvt.getEventId() + "--chanelId=" + chanelId
					+ "--transformationBean=" + tranformationBeanFQCN + ", xslName : " + xslname);

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

			Serializable transformedMsg = transformer.transformEvent(leapEvt);
			// Dispatch the transformed Event msg to the chanel
			// #TODO fIX Dispatch Chanel service than handle the error
			DispatchChanelService disChanelSer = DispatchChanelService.getDispatchChanelService();
			AbstractDispatchChanel chanel = disChanelSer.getDispatchChanel(leapEvt.getRequestContext(), chanelId);
			chanel.dispatchMsg(transformedMsg, leapEvt.getRequestContext(), leapEvt.getEventId());
		} catch (BeanDependencyResolveException e) {
			logger.error("LeapEventDispatching Failed for Event " + leapEvt, e);
			throw new LeapEventTransformationException(e.getMessage());
		}
	}

	private ILeapEventTransformer getTransformerInstance(String fqcn) throws BeanDependencyResolveException {
		logger.debug("inside getTransformerInstance : " + fqcn);
		ILeapEventTransformer transformer = null;
		logger.debug("OSGIEnvironmentHelper.isOSGIEnabled : " + OSGIEnvironmentHelper.isOSGIEnabled);
		if (OSGIEnvironmentHelper.isOSGIEnabled) {
			BeanResolutionHelper beanResolutionHelper = new BeanResolutionHelper();
			try {
				transformer = (ILeapEventTransformer) beanResolutionHelper
						.resolveBean(ILeapEventTransformer.class.getName(), fqcn);
			} catch (InvalidSyntaxException e) {
				logger.error("Unable to Load/instantiate CustomEventTransformer=" + fqcn);
				throw new BeanDependencyResolveException("Unable to Load/instantiate CustomEventTransformer=" + fqcn,
						e);
			}
		} else {

			IBeanDependencyResolver beanResolver = BeanDependencyResolverFactory.getBeanDependencyResolver();

			if (fqcn.equalsIgnoreCase(
					"com.attunedlabs.eventframework.dispatcher.transformer.GenericLeapEventJsonTransformer")) {
				return new GenericLeapEventJsonTransformer();
			}
			transformer = (ILeapEventTransformer) beanResolver.getBeanInstance(ILeapEventTransformer.class, fqcn);
		}
		return transformer;
	}

	/**
	 * This method is to generate XSL-XSLT transformer Instance
	 * 
	 * @param xslname
	 * @return
	 */
	private ILeapEventTransformer getXmlXsltTransformerInstance(String xslname) {
		logger.debug("inside getXmlXsltTransformationInstance ");
		ILeapEventTransformer transformer = new LeapEventXmlTransformer(xslname, xsltAsString);
		return transformer;

	}

	private ILeapEventTransformer getJSONTransformerInstance() {
		logger.debug("inside getJSONTransformerInstance ");
		ILeapEventTransformer transformer = new GenericLeapEventJsonTransformer();

		return transformer;
	}

}
