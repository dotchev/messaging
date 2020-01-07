/*
 * Based on https://github.com/apache/kafka/blob/2.4/streams/examples/src/main/java/org/apache/kafka/streams/examples/wordcount/WordCountDemo.java
 */

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

/**
 * Demonstrates, using the high-level KStream DSL, how to implement the WordCount program
 * that computes a simple word occurrence histogram from an input text.
 * <p>
 * In this example, the input topic values of messages represent lines of text.
 * The histogram is written to the output topic where each record
 * is an updated count of each distinct word.
 * <p>
 * Before running this example you must create the input topic and the output topic (e.g. via
 * {@code kafka-topics.sh --create ...}), and write some data to the input topic (e.g. via
 * {@code kafka-console-producer.sh}). Otherwise you won't see any data arriving in the output topic.
 */
public final class WordCount {

	private static final String BOOTSTRAP_SERVERS = System.getenv("BOOTSTRAP_SERVERS");
	private static final String INPUT_TOPIC = System.getenv("INPUT_TOPIC");
	private static final String OUTPUT_TOPIC = System.getenv("OUTPUT_TOPIC");

    public static void main(final String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> source = builder.stream(INPUT_TOPIC);

        final KTable<String, Long> counts = source
            .flatMapValues(value -> Arrays.stream(value.toLowerCase(Locale.getDefault()).split(" "))
            		.filter(s -> !s.isEmpty()).collect(Collectors.toList()))
            .groupBy((key, value) -> value)
            .count();

        // need to override value serde to Long type
        counts.toStream().to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));
        
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        /*
        CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-wordcount-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            System.exit(1);
        }
        System.exit(0);
        */
    }
}