package com.attunedlabs.leap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.pipeline.PipelineContext;
import com.hazelcast.transaction.TransactionContext;

public class LeapHeader implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String tenant;
	private String site;
	private String endpointType;
	private String servicetype;
	private String featureGroup;
	private String featureName;
	private String implementationName;
	private String vendor;
	private String version;
	private String operation;
	private String requestUUID;
	private TransactionContext hazelcastTransactionalContext;
	private RequestContext requestContext;
	private PipelineContext pipelineContext;
	Map<String, Object> genricdata = new HashMap<>();
	Map<String, Object> permadata = new HashMap<>();
	Map<String, Object> policydata = new HashMap<>();
	Map<Object, Object> resourceHolder = new HashMap<>();
	Map<String, Object> integrationpipelineData = new HashMap<>();
	Map<String, Object> pipeContext = new HashMap<>();
	Map<String, Object> serviceRequestData = new HashMap<>();
	Map<String, Object> originalLeapHeader = new HashMap<>();

	public LeapHeader() {
		super();
	}

	public LeapHeader deepClone() {
		LeapHeader clonedLeapHeader = new LeapHeader();
		clonedLeapHeader.tenant = this.tenant;
		clonedLeapHeader.site = this.site;
		clonedLeapHeader.endpointType = this.endpointType;
		clonedLeapHeader.servicetype = this.servicetype;
		clonedLeapHeader.featureGroup = this.featureGroup;
		clonedLeapHeader.featureName = this.featureName;
		clonedLeapHeader.implementationName = this.implementationName;
		clonedLeapHeader.vendor = this.vendor;
		clonedLeapHeader.version = this.version;
		clonedLeapHeader.operation = this.operation;
		clonedLeapHeader.requestUUID = this.requestUUID;
		clonedLeapHeader.hazelcastTransactionalContext = this.hazelcastTransactionalContext;
		clonedLeapHeader.requestContext = this.requestContext;
		clonedLeapHeader.pipelineContext = this.pipelineContext;
		clonedLeapHeader.genricdata = this.genricdata;
		clonedLeapHeader.permadata = this.permadata;
		clonedLeapHeader.policydata = this.policydata;
		clonedLeapHeader.resourceHolder = this.resourceHolder;
		clonedLeapHeader.integrationpipelineData = this.integrationpipelineData;
		clonedLeapHeader.pipeContext = this.pipeContext;
		clonedLeapHeader.serviceRequestData = this.serviceRequestData;
		clonedLeapHeader.originalLeapHeader = this.originalLeapHeader;
		return clonedLeapHeader;

	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getServicetype() {
		return servicetype;
	}

	public void setServicetype(String servicetype) {
		this.servicetype = servicetype;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getFeatureGroup() {
		return featureGroup;
	}

	public void setFeatureGroup(String featureGroup) {
		this.featureGroup = featureGroup;
	}

	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public String getRequestUUID() {
		return requestUUID;
	}

	public void setRequestUUID(String requestUUID) {
		this.requestUUID = requestUUID;
	}

	public Map<String, Object> getGenricdata() {
		return genricdata;
	}

	public void setGenricdata(Map<String, Object> genricdata) {
		this.genricdata = genricdata;
	}

	public Map<String, Object> getPermadata() {
		return permadata;
	}

	public void setPermadata(Map<String, Object> permadata) {
		this.permadata = permadata;
	}

	public Map<String, Object> getPolicydata() {
		return policydata;
	}

	public void setPolicydata(Map<String, Object> policydata) {
		this.policydata = policydata;
	}

	public Map<Object, Object> getResourceHolder() {
		return resourceHolder;
	}

	public void setResourceHolder(Map<Object, Object> resourceHolder) {
		this.resourceHolder = resourceHolder;
	}

	public TransactionContext getHazelcastTransactionalContext() {
		return hazelcastTransactionalContext;
	}

	public void setHazelcastTransactionalContext(TransactionContext hazelcastTransactionalContext) {
		this.hazelcastTransactionalContext = hazelcastTransactionalContext;
	}

	public String getEndpointType() {
		return endpointType;
	}

	public void setEndpointType(String endpointType) {
		this.endpointType = endpointType;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setRequestContext(String vendor, String version) {
		if (requestContext != null) {
			requestContext.setVendor(vendor);
			requestContext.setVersion(version);
		}
	}

	public void setRequestContext(String tenant, String site, String featureGroup, String feature,
			String implementationName, String vendor, String version) {
		RequestContext requestContext = new RequestContext(tenant, site, featureGroup, feature, implementationName,
				vendor, version);
		this.requestContext = requestContext;

	}

	public void setRequestContext(String tenant, String site, String featureGroup, String feature) {
		RequestContext requestContext = new RequestContext(tenant, site, featureGroup, featureName, implementationName);
		this.requestContext = requestContext;
	}

	public void setRequestContext(RequestContext requestContext) {
		this.requestContext = requestContext;
	}

	public RequestContext getRequestContext() {
		if (requestContext != null) {
			requestContext.setHcTransactionalContext(this.hazelcastTransactionalContext);
			requestContext.setRequestId(requestUUID);
			return requestContext;
		} else {
			// this will be removed when we cleanup the code by removing
			// tenant,site,vendor,feature from leap header etc
			RequestContext reqCtx = new RequestContext(tenant, site, featureGroup, featureName, implementationName,
					vendor, version);
			reqCtx.setHcTransactionalContext(this.hazelcastTransactionalContext);
			reqCtx.setRequestId(requestUUID);
			return reqCtx;
		}
	}

	public Map<String, Object> getIntegrationpipelineData() {
		return integrationpipelineData;
	}

	public void setIntegrationpipelineData(Map<String, Object> integrationpipelineData) {
		this.integrationpipelineData = integrationpipelineData;
	}

	public Map<String, Object> getPipeContext() {
		return pipeContext;
	}

	public void setPipeContext(Map<String, Object> pipeContext) {
		this.pipeContext = pipeContext;
	}

	public Map<String, Object> getOriginalLeapHeader() {
		return originalLeapHeader;
	}

	public void setOriginalLeapHeader(Map<String, Object> originalLeapHeader) {
		this.originalLeapHeader = originalLeapHeader;
	}

	public Map<String, Object> getServiceRequestData() {
		return serviceRequestData;
	}

	public void setServiceRequestData(Map<String, Object> serviceRequestData) {
		this.serviceRequestData = serviceRequestData;
	}

	public String getImplementationName() {
		return implementationName;
	}

	public void setImplementationName(String implementationName) {
		this.implementationName = implementationName;
	}

	public PipelineContext getPipelineContext() {
		return pipelineContext;
	}

	public void setPipelineContext(PipelineContext pipelineContext) {
		this.pipelineContext = pipelineContext;
	}

	@Override
	public String toString() {
		return "getGeneric ==> " + getGenricdata();
	}

}