# Publish/Subscribe

This example demonstrates the publish subscribe use-case where one application emits events and other applications listen to these events.

The example starts 5 applications:
* ZooKeeper
* Kafka
* Producer
* Consumer-A
* Consumer-B

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

In a few seconds when the start is complete you should see messages going from the producer to the consumers.

Notice that each message is received by both consumers.
The reason is that they use different `GROUP_ID`, so for Kafka these are two separate consumer groups.

Stop one of the consumers (run in a separate console):
```sh
docker-compose stop consumer-a
```
Notice that the producer and the other consumer are not affected.

Start again the consumer:
```sh
docker-compose start consumer-a
```
Notice that the consumer resumes from the point it left.
It receives in batches the messages that were published while it was down.
So the consumer is able to catch up quickly with the producer.

Notice that the messages in the batch are not in the same order as they were sent by the producer.
The reason is that Kafka preserves only the order of messages within a partition.

If necessary, we can still scale the two consumers independently.

Scale the consumer to run with 2 instances:
```sh
docker-compose up --scale consumer-a=2 -d
```
You can see in the logs that Kafka is rebalancing the topic partitions across the 2 processes in consumer group consumer-a.
Now each consumer instance receives messages from one partition.

Cleanup:
```sh
docker-compose down
```
