import java.text.SimpleDateFormat;
import java.util.Date;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Consumer {

  private final static String RABBITMQ_HOST = System.getenv("RABBITMQ_HOST");
  private final static String EXCHANGE_NAME = System.getenv("EXCHANGE_NAME");
  private final static String BINDING_KEY = System.getenv("BINDING_KEY");

  public static void main(String[] argv) throws Exception {
    Channel channel = connect();
    System.out.println("Declaring topic exchange " + EXCHANGE_NAME);
    channel.exchangeDeclare(EXCHANGE_NAME, "topic");
    String queueName = channel.queueDeclare().getQueue();
    System.out.println("Binding queue " + queueName + " to exchange " + 
      EXCHANGE_NAME + " with binding key " + BINDING_KEY);
    channel.queueBind(queueName, EXCHANGE_NAME, BINDING_KEY);

    System.out.println("Waiting for messages...");
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
      System.out.println(time + " Received message: " + message);
    };
    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
    });
  }

  private static Channel connect() throws Exception {
    while (true) {
      try {
        System.out.println("Connecting to RabbitMQ at host " + RABBITMQ_HOST);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        return channel;
      } catch (Exception e) {
        e.printStackTrace();
        Thread.sleep(5000);
      }
    }
  }
}
