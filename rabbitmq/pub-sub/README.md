# Publish/Subscribe

This example demonstrates the publish subscribe use-case where one application emits events and other applications listen to these events.

Here we use the [fanout exchange](https://www.rabbitmq.com/tutorials/amqp-concepts.html#exchange-fanout) of RabbitMQ. This exchange broadcasts each message to all bound queues.

The example starts 3 applications:
* RabbitMQ - the message broker
* Producer - publishes a new event to the exchange on regular intervals, see [Producer.java](producer/src/main/java/Producer.java)
* Consumer - creates a new queue and binds it to the exchange, then it listens for events on the queue and prints them on the console, see [Consumer.java](consumer/src/main/java/Consumer.java)

See [docker-compose.yml](docker-compose.yml)

Start the example:
```sh
docker-compose up
```
RabbitMQ starts after a few seconds and then you should see in the console the message exchange between the producer and the consumer:
```
producer_1  | 2019-10-01 16:41:01.979 Sent message: E1
consumer_1  | 2019-10-01 16:41:02.072 Received message: E1
producer_1  | 2019-10-01 16:41:06.979 Sent message: E2
consumer_1  | 2019-10-01 16:41:06.984 Received message: E2
producer_1  | 2019-10-01 16:41:11.979 Sent message: E3
consumer_1  | 2019-10-01 16:41:11.983 Received message: E3
```

Stop the consumer (run in a separate console):
```sh
docker-compose stop consumer
```
Notice that the producer is not affected. It keeps publishing messages to the exchange but they are lost since no queue is bound to it.

Start again the consumer:
```sh
docker-compose start consumer
```
Notice that the consumer receives only new messages but not those that were published while it was down.

Scale the consumer to run with 2 instances:
```sh
docker-compose up --scale consumer=2 -d
```
Notice that each message is dispatched to all consumers.
```
producer_1  | 2019-10-01 19:35:55.965 Sent message: E16
consumer_1  | 2019-10-01 19:35:55.969 Received message: E16
consumer_2  | 2019-10-01 19:35:56.021 Received message: E16
producer_1  | 2019-10-01 19:36:00.964 Sent message: E17
consumer_1  | 2019-10-01 19:36:00.970 Received message: E17
consumer_2  | 2019-10-01 19:36:00.973 Received message: E17
producer_1  | 2019-10-01 19:36:05.965 Sent message: E18
consumer_2  | 2019-10-01 19:36:05.969 Received message: E18
consumer_1  | 2019-10-01 19:36:05.969 Received message: E18
```
