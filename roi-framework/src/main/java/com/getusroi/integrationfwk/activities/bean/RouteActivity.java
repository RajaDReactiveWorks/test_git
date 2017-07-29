package com.getusroi.integrationfwk.activities.bean;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.integrationfwk.config.jaxb.PipeActivity;
import com.getusroi.mesh.MeshHeader;
import com.getusroi.mesh.MeshHeaderConstant;

public class RouteActivity {
	final Logger logger = LoggerFactory.getLogger(RouteActivity.class);
	private String featureGroup;
	private String featureName;
	private String vendorName;
	private String version;
	private String serviceName;
	private String executionRoute;

	/**
	 * method to configure the meshheader contraints and setting the fetched
	 * route endpoint from the pipeline configuration in the header
	 * 
	 * @param exchange
	 * @throws RouteActivityException
	 */
	public void routeDecidingActivity(Exchange exchange) throws RouteActivityException {

		//#TODO, Hard-coded data to be put into exchange when being called to
		// workOrder Service
		String myvar = "<DATA2SC>" + "<PIN>609988</PIN>" + "<ID>67049933</ID>" + "<CALL>" + "<TYPE> WONEW </TYPE>"
				+ "<DATETIME>2016/01/20 10:12:00</DATETIME>" + "<OPERATOR>KostyaS?XML</OPERATOR>"
				+ "<CALLER>test?xml</CALLER>" + "<CATEGORY>CAPITAL ? OTHER</CATEGORY>" + "<SUB>2014917018</SUB>"
				+ "<LOC>SoWerx?XML</LOC>" + "<TRADE>BUILDING EXTERIOR</TRADE>" + "<PRO>2000057450</PRO>"
				+ "<PRO_NAME>Boo Inc</PRO_NAME>" + "<TR_NUM>67049933</TR_NUM>" + "<WO_NUM>67049933</WO_NUM>"
				+ "<PO_NUM>67049933</PO_NUM>" + "<STATUS> OPEN </STATUS>" + "<PRIORITY>Sev 3</PRIORITY>"
				+ "<EQP_ID>6786867</EQP_ID>" + "<PRICE>1200</PRICE>" + "<TAX>23</TAX>" + "<TAX2>20</TAX2>"
				+ "<NTE>50000</NTE>" + "<SCHED_DATETIME>2016/01/27 10:16:00</SCHED_DATETIME>"
				+ "<CURRENCY>123</CURRENCY>" + "<RECALL>1</RECALL>" + "<PROBLEM>Reassign test</PROBLEM>" + "</CALL>"
				+ "</DATA2SC>";

		// setting into exchange's body
		//exchange.getIn().setBody(myvar);

		// fetched the pipeactivity from the header defined before
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);

		// Checking if the route defined belongs to the same feature or not
		boolean samefeature = pipeactivity.getCamelRouteEndPoint().getCamelRoute().isIsSameFeature();

		MeshHeader meshHeader = (MeshHeader) exchange.getIn().getHeader(MeshHeaderConstant.MESH_HEADER_KEY);

		// based on whether the current route belongs to the same feature or not, it will be decided which
		// contraints to be changed in the mesh header
		if (samefeature) {
			configForSameFeature(pipeactivity,meshHeader,exchange);
		} else {
			//#TODO need to test with different feature
			configForDifferentFeature(pipeactivity,meshHeader,exchange);
		}
	}
	
	/**
	 * Method to configure the mesh header when the defined route is of same Feature and Feature group
	 * 
	 * @param pipeactivity
	 * @param meshHeader
	 * @param exchange
	 */
	public void configForSameFeature(PipeActivity pipeactivity, MeshHeader meshHeader, Exchange exchange){
		serviceName = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getServiceName();
		executionRoute = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getExecutionRoute();
		meshHeader.setServicetype(serviceName);
		exchange.getIn().setHeader("exeroute", serviceName);
		logger.debug(".SERVICEnAME : " + meshHeader.getServicetype());
		//setting the route endpoint in the header
		exchange.getIn().setHeader("executionRoute", executionRoute);
	}
	
	/**
	 * Method to configure the mesh header when the defined route is of Different Feature and Feature group
	 * 
	 * @param pipeactivity
	 * @param meshHeader
	 * @param exchange
	 * @throws RouteActivityException
	 */
	public void configForDifferentFeature(PipeActivity pipeactivity, MeshHeader meshHeader, Exchange exchange) throws RouteActivityException{
		//when feature and feature group is different
		try {
			featureGroup = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getFeatureContext()
					.getFeatureGroup();
			featureName = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getFeatureContext().getFeatureName();
			vendorName = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getFeatureContext().getVendorName();
			version = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getFeatureContext().getVersion();
			serviceName = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getServiceName();
			executionRoute = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getExecutionRoute();
			meshHeader.setFeatureGroup(featureGroup);
			meshHeader.setFeatureName(featureName);
			meshHeader.setVendor(vendorName);
			meshHeader.setVersion(version);
			meshHeader.setServicetype(serviceName);
			//setting the route endpoint in the header
			exchange.getIn().setHeader("executionRoute", executionRoute);
			
			if (featureGroup.equals("") || featureName.equals("") || vendorName.equals("") || version.equals("")
					|| serviceName.equals("") || executionRoute.equals("")) {
				throw new RouteActivityException("Data cannot be empty");
			}
		} catch (NullPointerException e) {
			throw new RouteActivityException("Data Cannot be Null");
		}
	}
}