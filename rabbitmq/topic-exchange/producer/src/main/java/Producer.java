import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.LongStream;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Producer {

  private final static String RABBITMQ_HOST = System.getenv("RABBITMQ_HOST");
  private final static String EXCHANGE_NAME = System.getenv("EXCHANGE_NAME");
  private final static String MESSAGE_PERIOD = System.getenv("MESSAGE_PERIOD");

  private final static class Task extends TimerTask {
    private Connection connection;
    private Channel channel;
    private long counter = 0;

    private boolean isPrime(long number) {
      return number > 2 && 
        LongStream.rangeClosed(2, (long) Math.sqrt(number)).noneMatch(n -> (number % n == 0));
    }

    private Channel connect() throws Exception {
      if (channel != null && channel.isOpen()) {
        return channel;
      }

      System.out.println("Connecting to RabbitMQ at host " + RABBITMQ_HOST);
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(RABBITMQ_HOST);
      connection = factory.newConnection();
      channel = connection.createChannel();
      System.out.println("Declaring exchange " + EXCHANGE_NAME);
      channel.exchangeDeclare(EXCHANGE_NAME, "topic");
      return channel;
    }

    @Override
    public void run() {
      try {
        connect();
        String routingKey;
        if (counter % 2 == 0) {
           routingKey = "number.even";
        } else {
          routingKey = "number.odd." + (isPrime(counter) ? "prime" : "composite");
        }
        String message = "E" + counter++;
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        System.out.println(time + " Sent message: " + message + " (" + routingKey + ")");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] argv) throws Exception {
    long period;
    try {
      period = Long.parseLong(MESSAGE_PERIOD);
    } catch (Exception e) {
      period = 5000;
    }

    Task task = new Task();
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(task, period, period);
  }
}

