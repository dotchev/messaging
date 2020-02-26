# Kafka Streams

Kafka provides special support for stream processing applications.
Essentially these are application that read from input topics,
perform some processing and write the result into output topics.
See https://kafka.apache.org/documentation/streams/ for details.

This example splits each message from the input topic into words and
writes the word counts into the output topic.

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

Wait until the start is complete.

Start the producer in a separate console:
```sh
docker-compose exec kafka kafka-console-producer.sh --broker-list localhost:9092 --topic chat
```

Start the consumer in a separate console:
```sh
docker-compose exec kafka kafka-console-consumer.sh \
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
docker-compose scale word-count=4
```

Write again some text in the producer.
Notice how different parts are processed by different instances of the stream processor.
Notice that each word is processed by the same processor instance.
The reason is that Kafka assigns different partitions to different application instances.
The output topic partition is selected based on the message key, which is the word.

Kafka automatically reassigns partitions across available application instances.
This provides stream processors with scalability and fault tolerance.

You can find in the beginning of the application log a description of the processing topology.
It looks like this:
```
word-count_1  | Topologies:
word-count_1  |    Sub-topology: 0
word-count_1  |     Source: KSTREAM-SOURCE-0000000000 (topics: [chat])
word-count_1  |       --> KSTREAM-PEEK-0000000001
word-count_1  |     Processor: KSTREAM-PEEK-0000000001 (stores: [])
word-count_1  |       --> KSTREAM-FLATMAPVALUES-0000000002
word-count_1  |       <-- KSTREAM-SOURCE-0000000000
word-count_1  |     Processor: KSTREAM-FLATMAPVALUES-0000000002 (stores: [])
word-count_1  |       --> KSTREAM-KEY-SELECT-0000000003
word-count_1  |       <-- KSTREAM-PEEK-0000000001
word-count_1  |     Processor: KSTREAM-KEY-SELECT-0000000003 (stores: [])
word-count_1  |       --> KSTREAM-FILTER-0000000007
word-count_1  |       <-- KSTREAM-FLATMAPVALUES-0000000002
word-count_1  |     Processor: KSTREAM-FILTER-0000000007 (stores: [])
word-count_1  |       --> KSTREAM-SINK-0000000006
word-count_1  |       <-- KSTREAM-KEY-SELECT-0000000003
word-count_1  |     Sink: KSTREAM-SINK-0000000006 (topic: KSTREAM-AGGREGATE-STATE-STORE-0000000004-repartition)
word-count_1  |       <-- KSTREAM-FILTER-0000000007
word-count_1  | 
word-count_1  |   Sub-topology: 1
word-count_1  |     Source: KSTREAM-SOURCE-0000000008 (topics: [KSTREAM-AGGREGATE-STATE-STORE-0000000004-repartition])
word-count_1  |       --> KSTREAM-AGGREGATE-0000000005
word-count_1  |     Processor: KSTREAM-AGGREGATE-0000000005 (stores: [KSTREAM-AGGREGATE-STATE-STORE-0000000004])
word-count_1  |       --> KTABLE-TOSTREAM-0000000009
word-count_1  |       <-- KSTREAM-SOURCE-0000000008
word-count_1  |     Processor: KTABLE-TOSTREAM-0000000009 (stores: [])
word-count_1  |       --> KSTREAM-PEEK-0000000010
word-count_1  |       <-- KSTREAM-AGGREGATE-0000000005
word-count_1  |     Processor: KSTREAM-PEEK-0000000010 (stores: [])
word-count_1  |       --> KSTREAM-SINK-0000000011
word-count_1  |       <-- KTABLE-TOSTREAM-0000000009
word-count_1  |     Sink: KSTREAM-SINK-0000000011 (topic: word-count)
word-count_1  |       <-- KSTREAM-PEEK-0000000010
```
Each sub-topology represents independent processing, which can be performed in a separate process.
Sub-topologies communicate via a Kafka topic (KSTREAM-AGGREGATE-STATE-STORE-0000000004-repartition).

Check the partition assignments across consumer instances:
```sh
docker-compose exec kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --all-groups
```

Cleanup:
```sh
docker-compose down
```
