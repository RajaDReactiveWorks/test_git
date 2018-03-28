# **Camel MESH Component Project**
====================
##InProgress

Overview: MESH Event are generated throughout the execution of the services these event can be subscribed. We  have subscription of event over HazelCast-Queue/ActiveMq/Kafka.
 to insulate the route from change of technology we have created a leap camel component. The component internally  can switch between the tech changes without impacting the routes and code.

**Currently this component only supports the Hazelcast-Queue in the future it will be extended**  

MESH Event Subscribing from Route
	from("leap://Event?eventId=PicEvent&subscriberId=mockroute")
	.log(LoggingLevel.DEBUG, ">>Processing ${body}")
	.to("mock:result");
	
eventId: Event to subscribe as per the eventId defined in the Eventing.xml
subscriberId:- SubscriberId as per defined in the Eventing.xml under the Subscription



