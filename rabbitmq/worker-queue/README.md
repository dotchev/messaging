# Worker Queue

This example demonstrates the worker queue use-case where one application creates tasks and another one executes them.

Here we use the [default direct exchange](https://www.rabbitmq.com/tutorials/amqp-concepts.html#exchange-default) of RabbitMQ. Every queue is automatically bound to it with a routing key which is the same as the queue name.

The example starts 3 applications:
* RabbitMQ - the message broker
* Producer - publishes a new task in the queue on regular intervals, see [Producer.java](producer/src/main/java/Producer.java)
* Consumer - listens for tasks on the queue and prints them on the console, see [Consumer.java](consumer/src/main/java/Consumer.java)

See [docker-compose.yml](docker-compose.yml)

Start the example:
```sh
docker-compose up
```
RabbitMQ starts after a few seconds and then you should see in the console the message exchange between the producer and the consumer:
```
producer_1  | 2019-09-25 19:36:57.260 Sent message: T2
consumer_1  | 2019-09-25 19:36:57.264 Received message: T2
producer_1  | 2019-09-25 19:37:02.260 Sent message: T3
consumer_1  | 2019-09-25 19:37:02.264 Received message: T3
producer_1  | 2019-09-25 19:37:07.260 Sent message: T4
consumer_1  | 2019-09-25 19:37:07.264 Received message: T4
producer_1  | 2019-09-25 19:37:12.260 Sent message: T5
consumer_1  | 2019-09-25 19:37:12.265 Received message: T5
```

Stop the consumer (run in a separate console):
```sh
docker-compose stop consumer
```
Notice that the producer is not affected. It keeps publishing messages to the queue but they are not processed.
Start again the consumer:
```sh
docker-compose start consumer
```
Notice that the consumer receives the messages that were published while it was down.

Scale the consumer to run with 2 instances:
```sh
docker-compose up --scale consumer=2 -d
```
Notice that messages are dispatched to different consumers.
```
producer_1  | 2019-09-25 19:57:23.922 Sent message: T26
consumer_1  | 2019-09-25 19:57:23.924 Received message: T26
producer_1  | 2019-09-25 19:57:28.923 Sent message: T27
consumer_2  | 2019-09-25 19:57:28.926 Received message: T27
producer_1  | 2019-09-25 19:57:33.924 Sent message: T28
consumer_1  | 2019-09-25 19:57:33.927 Received message: T28
producer_1  | 2019-09-25 19:57:38.924 Sent message: T29
consumer_2  | 2019-09-25 19:57:38.930 Received message: T29
```
The reason is that both consumers read from the same queue. This pattern can be used to load balance tasks between multiple consumers.