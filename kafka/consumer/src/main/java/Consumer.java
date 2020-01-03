import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Consumer {

	private static final String BOOTSTRAP_SERVERS = System.getenv("BOOTSTRAP_SERVERS");
	private static final String TOPIC = System.getenv("TOPIC");
	private static final String GROUP_ID = System.getenv("GROUP_ID");

	private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

	public static void main(String[] args) throws Exception {
		Properties config = new Properties();
		// see https://kafka.apache.org/documentation/#consumerconfigs
		config.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		config.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
		config.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		config.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
		config.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

		logger.info("Connecting to Kafka at {}...", BOOTSTRAP_SERVERS);
		try (KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(config)) {
			consumer.subscribe(Collections.singletonList(TOPIC));
//			consumer.seek... - change read offset
			while (true) {
				ConsumerRecords<Long, String> records = consumer.poll(Duration.ofSeconds(10));
				if (records.isEmpty())
					continue;
				List<String> r = new ArrayList<>(records.count());
				for (ConsumerRecord<Long, String> record : records) {
					r.add(String.format("record %s-%d@%d with key=%d and value=\"%s\"", record.topic(),
							record.partition(), record.offset(), record.key(), record.value()));
				}
				if (r.size() == 1) {
					logger.info("Received {}", r.get(0));
				} else {
					logger.info("Received {} records:\n{}", r.size(), String.join("\n", r));
				}
			}
		}

	}

}
