# Consumer Group

Kafka can distribute the load across consumer instances using consumer groups.
See https://kafka.apache.org/intro#intro_consumers for details.

Requirements:
* Docker
* Java
* Maven

Build the example code:
```sh
mvn package -f ../pom.xml
```

The example starts 4 applications:
* ZooKeeper
* Kafka
* Producer
* Consumer

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

Topic `messages` with 2 partitions is created automatically.

In a few seconds when the start is complete you should see messages going from the producer to the consumer.

Notice that messages are distributed across the topic partitions.
Still messages with the same key are always stored in the same partition.
Here we use the default partitioner which selects the partition based on the message key.

Stop the consumer (run in a separate console):
```sh
docker-compose stop consumer
```
Notice that the producer is not affected. It keeps sending messages to Kafka.

Start again the consumer:
```sh
docker-compose start consumer
```
Notice that the consumer resumes from the point it left.
It receives in a batch the messages that were published while it was down.
So the consumer is able to catch up quickly with the producer.

Notice that the messages in the batch are not in the same order as they were sent by the producer.
The reason is that Kafka preserves only the order of messages within a partition.

Scale the consumer to run with 2 instances:
```sh
docker-compose up --scale consumer=2 -d
```
You can see in the logs that Kafka is rebalancing the topic partitions across the 2 processes in the consumer group.
Now each consumer instance receives messages from one partition.

Cleanup:
```sh
docker-compose down
```
