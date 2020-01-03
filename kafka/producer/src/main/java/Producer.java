import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.Reconfigurable;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Producer {

	private static final String BOOTSTRAP_SERVERS = System.getenv("BOOTSTRAP_SERVERS");
	private static final String TOPIC = System.getenv("TOPIC");
	private static final String TOTAL_MESSAGES = System.getenv("TOTAL_MESSAGES");
	private static final String KEY_GROUPS = System.getenv("KEY_GROUPS");
	private static final String MESSAGE_DELAY_MS = System.getenv("MESSAGE_DELAY_MS");

	private static final Logger logger = LoggerFactory.getLogger(Producer.class);
	
	public static void main(String[] args) throws Exception {
		Properties config = new Properties();
		// see https://kafka.apache.org/documentation/#producerconfigs
		config.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		config.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		config.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//		config.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "false"); true - no duplicates due to retries
//		config.setProperty(ProducerConfig.ACKS_CONFIG, "1"); // 0 - do not wait for ack; 1 - wait only the leader to ack; all - wait all replicas to ack
//		config.setProperty(ProducerConfig.PARTITIONER_CLASS_CONFIG, "<custom partitioner>"); https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/clients/producer/internals/DefaultPartitioner.java
		
		logger.info("Connecting to Kafka at {}...", BOOTSTRAP_SERVERS);
		try (KafkaProducer<Long, String> producer = new KafkaProducer<>(config)) {
			long keyGroups = KEY_GROUPS == null ? Long.MAX_VALUE : Long.parseLong(KEY_GROUPS);
			long messageDelay = MESSAGE_DELAY_MS == null ? 0 : Long.parseLong(MESSAGE_DELAY_MS);
			long totalMessages = TOTAL_MESSAGES == null ? 100 : Long.parseLong(TOTAL_MESSAGES);
			for (long i = 0; i < totalMessages; i++) {
				final long recordNumber = i; // required by lambda
				final long key = i % keyGroups;
				final String value = "Value " + recordNumber;
				ProducerRecord<Long, String> record = new ProducerRecord<>(TOPIC, key, value);
				logger.debug("Sending record with key={} and value=\"{}\"...", key, value);
				producer.send(record, (RecordMetadata metadata, Exception exception) -> {
					if (exception != null) {
						logger.error("Error sending record " + recordNumber, exception); 
					} else {
						logger.info("Record with key={} and value=\"{}\" sent to {}", key, value, metadata);
					}
				});
				Thread.sleep(messageDelay);
			}
		}
		logger.info("Done");
		
	}

}
