package com.getusroi.eventframework.camel.eventproducer;

public interface CamelEventProducerConstant {
	public static final String TENANT_ID="TenantId";
	public static final String CAMEL_ROUTER_ID="CamelRouterId";
	public static final String CAMEL_CONTEXT_ID="CamelContextId";
	public static final String CAMEL_CREATED_DTM="CamelCreatedDTM";
	
	public static final String CAMEL_FAILED_ENDPONT="CamelFailedEndpoint";
	public static final String CAMEL_FAILURE_MSG="CamelFailureMsg";
	public static final String CAMEL_ROUTE_ROLLBACK="CamelRouteRollbacked";
	public static final String CAMEL_TIMESTAMP="CamelTimeStamp";
	public static final String SERVICE_TYPE="servicetype";
	public static final String REQUEST_UID="requestGUUID";
	
	public static final String FALIURE_RAISEON="Failure";
	public static final String SUCCESS_RAISEON="Success";
	
	public static final String INTERNAL_GROUP_KEY="evtConfig-internalevent";
	public static final String SYSTEM_GROUP_KEY="evtConfig-systemevent";
	public static final String CHANEL_GROUP_KEY="evtConfig-chanel";
	public static final String TENANT="gap";
	
	public static final String CUSTOM_EVENT_BUILDER="CUSTOM";
	public static final String OGNL_EVENT_BUILDER="OGNL";

}
