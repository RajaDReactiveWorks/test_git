package com.attunedlabs.eventframework.dispatcher.chanel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.config.util.GenericTestConstant;
import com.attunedlabs.eventframework.config.EventFrameworkConfigParserException;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.EventFrameworkTestConstant;
import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.event.ILeapEventService;
import com.attunedlabs.eventframework.event.LeapEventService;
import com.attunedlabs.eventframework.jaxb.DispatchChanel;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventFramework;

public class DispatchChanelsTest {
	String jsonConfigStr = "{\"queueName\":\"testQueue\"}";
	String jsonTopicConfigStr = "{\"bootstrapservers\":\"localhost:9092,topic\":\"testTopic1,isTenantAware\":\"false\"}";
	final static Logger logger = LoggerFactory.getLogger(DispatchChanelsTest.class);

	private static EventFramework eventFrameworkConfig;
	private IEventFrameworkConfigService eventConfigService = new EventFrameworkConfigService();
	private ILeapEventService eventService = new LeapEventService();

	private EventFramework getEventFrameworkConfiguration() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.configfileToParse);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		String evtFrmeworkXmlStr = out1.toString();
		EventFramework evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);

		return evtFramework;
	}

	@Before
	public void loadConfigurations() throws EventFrameworkConfigParserException, ConfigPersistenceException {
		if (eventFrameworkConfig == null) {
			eventFrameworkConfig = getEventFrameworkConfiguration();
		}
		// Clear all DB Data First for SITE=23 FG=25,FEATURE-26
		IConfigPersistenceService pesrsistence = new ConfigPersistenceServiceMySqlImpl();
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getConfigNodeId());
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getSiteConfigNodeId());
		pesrsistence.deleteConfigNodeDataByNodeId(GenericTestConstant.getFeatureGroupConfigNodeId());
	}

	@Test
	public void testKafkaTopicDispatchChanel() throws DispatchChanelInitializationException, InterruptedException,
			MessageDispatchingException, EventFrameworkConfigurationException {
		ConfigurationContext configContext = getConfigurationContext(6);
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		List<DispatchChanel> disChanelList = eventFrameworkConfig.getDispatchChanels().getDispatchChanel();

		Event eventConfig = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE")) {
				eventConfig = event;
				break;
			}
		}

		DispatchChanel disChanelConfig = null;
		for (DispatchChanel disChanel : disChanelList) {
			if (disChanel.getId().equalsIgnoreCase("KAFKA_TOPIC_CHANNEL")) {
				disChanelConfig = disChanel;
				break;
			}
		}

		// Kafka consumer configuration settings
		String topicName = "testTopic1";
		Properties props = new Properties();

		props.put("bootstrap.servers", "localhost:9092");
		props.put("group.id", "test");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("auto.offset.reset", "earliest");
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

		// Kafka Consumer subscribes list of topics here.
		consumer.subscribe(Arrays.asList(topicName));

		KafkaTopicDispatchChannel hcDisChanel = new KafkaTopicDispatchChannel(disChanelConfig.getChanelConfiguration());
		RequestContext requestContext = getRequestContext();
		hcDisChanel.dispatchMsg(jsonConfigStr, requestContext, "PRINT_SERVICE");

		ConsumerRecords<String, String> records = consumer.poll(1);
		for (ConsumerRecord<String, String> record : records) {
			Assert.assertEquals("Expected output to check with Actual one ", jsonConfigStr, record.value());
		}

	}// end of method

	private ConfigurationContext getConfigurationContext(int level) {
		ConfigurationContext configContext = null;
		if (level == 4)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
					GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),
					GenericTestConstant.TEST_IMPL_NAME);
		if (level == 2)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite());
		if (level == 6)
			configContext = new ConfigurationContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
					GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),
					GenericTestConstant.TEST_IMPL_NAME, GenericTestConstant.TEST_VENDOR,
					GenericTestConstant.TEST_VERSION);
		return configContext;

	}

	private RequestContext getRequestContext() {
		RequestContext reqContext = new RequestContext(GenericTestConstant.getTenant(), GenericTestConstant.getSite(),
				GenericTestConstant.getFeatureGroup(), GenericTestConstant.getFeature(),
				GenericTestConstant.TEST_IMPL_NAME);

		return reqContext;

	}
}
