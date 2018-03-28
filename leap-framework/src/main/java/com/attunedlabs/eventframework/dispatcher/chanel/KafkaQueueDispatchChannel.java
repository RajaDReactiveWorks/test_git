package com.attunedlabs.eventframework.dispatcher.chanel;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.BufferExhaustedException;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.InterruptException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.abstractbean.util.CassandraUtil;
import com.attunedlabs.eventframework.abstractbean.util.ConnectionConfigurationException;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatchchannel.exception.RetryableMessageDispatchingException;

public class KafkaQueueDispatchChannel extends AbstractDispatchChanel {

	private static String KAFKA_PRODUCER_CONFIGS = "globalAppDeploymentConfig.properties";
	private static Properties props = new Properties();
	final static Logger log = LoggerFactory.getLogger(KafkaQueueDispatchChannel.class);
	private String bootstrapservers;
	private String queue;
	private Boolean isTenantAware;
	private KafkaProducer<String, Serializable> producer;
	static {
		InputStream inputStream;
		try {
			inputStream = KafkaQueueDispatchChannel.class.getClassLoader().getResourceAsStream(KAFKA_PRODUCER_CONFIGS);
			props.load(inputStream);
		} catch (IOException e) {
		}
	}// ..end of static block to load the ProducerProperties

	public KafkaQueueDispatchChannel(String chaneljsonconfig) throws DispatchChanelInitializationException {
		initializeFromConfig();
		this.chaneljsonconfig = chaneljsonconfig;
	}

	public KafkaQueueDispatchChannel() {
	}

	/**
	 * 
	 * Method which is called for dispatching the event message to a Kafka topic
	 * "tenant-topic" - for tenantAware, else the topic name specified on json
	 * string. A Random-UUID is generated for each messages appended with the
	 * EventId.
	 * 
	 * @param msg
	 * @param reqContext
	 * @param evendId
	 * @throws MessageDispatchingException
	 * 
	 **/
	@Override
	public void dispatchMsg(Serializable msg, RequestContext reqContext, String evendId)
			throws MessageDispatchingException {
		String topicName = "";
		if (isTenantAware) {
			topicName = reqContext.getTenantId() + "-" + queue;
		} else {
			topicName = queue;
		}
		log.debug("Event message incoming: " + msg + "EventID: " + evendId);
		List<ProducerRecord<String, Serializable>> buffer = new ArrayList<ProducerRecord<String, Serializable>>();
		ProducerRecord<String, Serializable> producerRecord = new ProducerRecord<String, Serializable>(topicName, 0,
				evendId + "-" + generateRecKey(), msg);
		buffer.add(producerRecord);
		log.debug("Event bufferSize: " + buffer.size());
		for (ProducerRecord<String, Serializable> record : buffer) {
			try {
				Future<RecordMetadata> future = producer.send(record, new Callback() {
					public void onCompletion(RecordMetadata metadata, Exception e) {
						if (e != null) {
							log.error("Unable to produce to the topic.. ", e.getMessage());
						}
					}
				});
			} catch (InterruptException | BufferExhaustedException e) {
				throw new RetryableMessageDispatchingException("Failed to produce to the topic.. " + e.getMessage());
			}
			log.debug(" -Data - " + record.toString());
		}
		producer.close();

	}// ..end
		// of
		// the
		// method

	/**
	 * to initialize few values before startup
	 * 
	 * @throws DispatchChanelInitializationException
	 */
	@Override
	public void initializeFromConfig() throws DispatchChanelInitializationException {
		parseConfiguration(chaneljsonconfig);
		try {
			Properties propAppsDeploymentEnv = CassandraUtil.getAppsDeploymentEnvConfigProperties();
			String deployemntEnv = propAppsDeploymentEnv.getProperty(CassandraUtil.DEPLOYMENT_ENVIRONMENT_KEY);
			if (deployemntEnv != null && !(deployemntEnv.isEmpty()) && deployemntEnv.length() > 0
					&& deployemntEnv.equalsIgnoreCase(CassandraUtil.PAAS_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY)) {
				bootstrapservers = System.getenv("bootstrapservers");
				Properties propsKafka = new Properties();
				propsKafka.setProperty("bootstrap.servers", bootstrapservers);
				propsKafka.put("zookeeper.connect", System.getenv("zookeeperconnect"));
				propsKafka.put("acks", props.getProperty("acks"));
				propsKafka.put("retries", props.getProperty("retries"));
				propsKafka.put("batch.size", props.getProperty("batch.size"));
				propsKafka.put("linger.ms", props.getProperty("linger.ms"));
				propsKafka.put("buffer.memory", props.getProperty("buffer.memory"));
				propsKafka.put("key.serializer", props.getProperty("key.serializer"));
				propsKafka.put("value.serializer", props.getProperty("value.serializer"));
				propsKafka.put("producer.type", props.getProperty("producer.type"));
				propsKafka.put("buffer.size", props.getProperty("buffer.size"));
				propsKafka.put("reconnect.interval", props.getProperty("reconnect.interval"));
				propsKafka.put("request.required.acks", props.getProperty("request.required.acks"));
				propsKafka.put("rebalance.retries.max", props.getProperty("rebalance.retries.max"));
				propsKafka.put("mirror.consumer.numthreads", props.getProperty("mirror.consumer.numthreads"));
				propsKafka.put("metadata.max.age.ms", props.getProperty("metadata.max.age.ms"));
				propsKafka.put("security.protocol", props.getProperty("security.protocol"));
				propsKafka.put("ssl.truststore.location", props.getProperty("ssl.truststore.location"));
				propsKafka.put("ssl.truststore.password", props.getProperty("ssl.truststore.password"));
				this.producer = new KafkaProducer<>(propsKafka);
			} else {
				props.setProperty("bootstrap.servers", bootstrapservers);
				this.producer = new KafkaProducer<>(props);
			}
		} catch (ConnectionConfigurationException e) {
			log.error("Problem in getting the deloyment config file ", e);
		}

	}// ..end of the method

	/**
	 * This method is to parse json configuration eg:
	 * {"bootstrapservers":"host:port","queue":"topicName",
	 * "isTenantAware":true}
	 * 
	 * @param chaneljsonconfig
	 * @throws ParseException
	 */
	private void parseConfiguration(String chaneljsonconfig) {
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(chaneljsonconfig);
		} catch (ParseException e) {

		}
		JSONObject jsonObject = (JSONObject) obj;
		this.bootstrapservers = (String) jsonObject.get("bootstrapservers");
		this.queue = (String) jsonObject.get("queue");
		this.isTenantAware = Boolean.valueOf((String) jsonObject.get("isTenantAware"));
		log.debug("Queue configured: " + chaneljsonconfig);
	}// .. end of the method

	/**
	 * generate random uuid
	 * 
	 * @return
	 */
	private String generateRecKey() {
		return UUID.randomUUID().toString();
	}// ..end of the method

}
