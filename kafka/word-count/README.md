# Kafka Streams

Kafka provides special support for stream processing applications.
Essentially these are application that read from input topics,
perform some processing and write the result into output topics.
See https://kafka.apache.org/documentation/streams/ for details.

This example splits each message from the input topic into words and
writes the word counts into the output topic.

Requirements:
* Docker
* Java
* Maven

Build the example code:
```sh
mvn package
```

The example starts 3 applications:
* ZooKeeper
* Kafka
* WordCount stream processor

See [docker-compose.yml](docker-compose.yml)

Export environment variable `HOST_IP` with your local host IP.
```sh
export HOST_IP=<your-ip>
```
You can find your local IP with `ifconfig` or `ipconfig`.

Start the example:
```sh
docker-compose up --build
```

The following topics are created automatically:
* `chat` - this is the input topic, it consists of 2 partitions
* `word-count` - this is the output topic, it is configured with log compaction

In a few seconds when the start is complete.

Start the producer in a separate console:
```sh
doco exec kafka kafka-console-producer.sh --broker-list localhost:9092 --topic chat
```

Start the consumer in a separate console:
```sh
doco exec kafka kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic word-count \
    --from-beginning \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```

Write some text in the producer console.
Notice the word counts in the consumer console.
Each message in the output topic provides the latest count of a specific word.

Scale the stream processor to run with multiple instances:
```sh
doco scale word-count=4
```

Write again some text in the producer.
Notice how different parts are processed by different instances of the stream processor.
Notice that each word is processed by the same processor instance.
The reason is that Kafka assigns different partitions to different application instances.
The output topic partition is selected based on the message key, which is the word.

Kafka automatically reassigns partitions across available application instances.
This allows stream processors to handle higher load and provides fault tolerance.

Check the partition assignments across consumer instances:
```sh
doco exec kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --all-groups
```

Cleanup:
```sh
docker-compose down
```
