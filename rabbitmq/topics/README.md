# Topics

This example demonstrates the publish subscribe use-case where consumer applications subscribe to different topics.

Here we use the [topic exchange](https://www.rabbitmq.com/tutorials/amqp-concepts.html#exchange-topic) of RabbitMQ. This exchange broadcasts each message to all queues with binding key that matches the message routing key (a.k.a. topic).

The example starts 4 applications:
* RabbitMQ - the message broker
* Producer - publishes a new event to the exchange on regular intervals, events are published with different topics (`number.even` / `number.odd.prime` / `number.odd.composite`), see [Producer.java](producer/src/main/java/Producer.java)
* Odd Consumer - creates a new queue and binds it to the exchange with binding key `number.odd.*`, then it listens for events on the queue and prints them on the console, see [Consumer.java](consumer/src/main/java/Consumer.java)
* Prime Consumer - creates a new queue and binds it to the exchange with binding key `number.odd.prime`, then it listens for events on the queue and prints them on the console, see [Consumer.java](consumer/src/main/java/Consumer.java)

See [docker-compose.yml](docker-compose.yml)

Start the example:
```sh
docker-compose up
```
RabbitMQ starts after a few seconds and then you should see in the console the message exchange between the producer and the consumers:
```
producer_1        | 2019-10-01 20:05:42.591 Sent message: E6 (number.even)
producer_1        | 2019-10-01 20:05:47.592 Sent message: E7 (number.odd.prime)
prime-consumer_1  | 2019-10-01 20:05:47.596 Received message: E7
odd-consumer_1    | 2019-10-01 20:05:47.596 Received message: E7
producer_1        | 2019-10-01 20:05:52.590 Sent message: E8 (number.even)
producer_1        | 2019-10-01 20:05:57.591 Sent message: E9 (number.odd.composite)
odd-consumer_1    | 2019-10-01 20:05:57.593 Received message: E9
```
Notice that _odd-consumer_ receives events with both topics `number.odd.prime` and `number.odd.composite`,
while _prime-consumer_ receives only events with topic `number.odd.prime`.
Events with topic `number.even` are ignored because no queue binding matches it.

Cleanup:
```sh
docker-compose down
```
