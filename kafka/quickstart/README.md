# Kafka Quickstart

Based on https://kafka.apache.org/quickstart

Export environment variable `HOST_IP` with your local host IP.
```sh
export HOST_IP=<your-ip>
```
You can find your local IP with `ifconfig` or `ipconfig`.

Start Kafka:
```sh
docker-compose up -d
```
This command starts one docker container with ZooKeeper and one with Kafka.

Open a Kafka shell:
```sh
docker-compose exec kafka bash
```

Create a new topic:
```sh
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic test --partitions 1 --replication-factor 1
```

List available topics:
```sh
kafka-topics.sh --bootstrap-server localhost:9092 --list
```
You should see your new topic `test`.

Start the console producer:
```sh
kafka-console-producer.sh --broker-list localhost:9092 --topic test
```
This producer sends each line of input as a separate message to the specified topic.

In a separate terminal open a second Kafka shell (do not forget to export again HOST_IP) and start the console consumer:
```sh
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
```
This consumer reads messages from the specified topic and prints them on the console.

Whatever you enter is the producer, is displayed by the consumer.

**Bonus:** connect to a common Kafka server and you will get a kind of a group chat.

Quit the console producer and consumer by hitting Ctrl-C.
Exit the Kafka shell and go back to the OS shell.

Scale Kafka to 3 brokers:
```sh
docker-compose up -d --scale kafka=3
```

List running containers:
```sh
docker-compose ps
```
Now you should see 3 Kafka containers and one ZooKeeper.

Start again the Kafka shell.
Create a new topic that is replicated on 3 servers:
```sh
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic ha-test --partitions 1 --replication-factor 3
```
Display details about the new topic:
```sh
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic ha-test
```
You should see that the topic is replicated on 3 servers and one of them is the leader. Note down the leader id.
"Isr" stands for in-sync-replicas.
Here broker id 1001 corresponds to container quickstart_kafka_1, 1002 to quickstart_kafka_2, etc.

Open again the console producer and consumer in separate terminals and connect them to the new topic. Send some messages. They should appear in the consumer as before.

From the OS shell stop the leader container:
```sh
docker stop quickstart_kafka_3
```
You may need to change the container name to match the topic leader.
From the Kafka shell describe again the topic. Notice that now another broker is the leader. The stopped broker is still listed in `Replicas:` but is removed from `Isr:`.
The messages are still available. Start again the console consumer and you should see the old messages.
