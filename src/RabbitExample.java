import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import com.rabbitmq.client.*;

import java.io.IOException;

public class RabbitExample {
    private static final String EXCHANGE_NAME = "exchangeName";
    private static final String QUEUE_NAME = "queueName";
    private static final String ROUTING_KEY = "routingKey";

    @Test
    public void connectToRabbit() throws IOException {
        // playing with this http://www.rabbitmq.com/api-guide.html
        // had to do a few things on the server


        //  sudo rabbitmqctl add_vhost test
        //  sudo rabbitmqctl add_user davie davie
        //  sudo rabbitmqctl map_user_vhost davie test
        //     sudo rabbitmqctl add_realm test /data/testrealm
        //    sudo rabbitmqctl set_permissions davie test /data/testrealm active read write passive

        Connection connection = connect();

        Channel channel = connection.createChannel();
        int ticket = channel.accessRequest("/data/testrealm");

//        channel.exchangeDeclare(ticket, EXCHANGE_NAME, "direct");
//        channel.queueDeclare(ticket, QUEUE_NAME);
//        channel.queueBind(ticket, QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

        byte[] messageBodyBytes = "Hello, world!".getBytes();
        channel.basicPublish(ticket, EXCHANGE_NAME, ROUTING_KEY, null, messageBodyBytes);


        checkMessageSent(channel, ticket, QUEUE_NAME, "Hello, world!");

        channel.close(AMQP.REPLY_SUCCESS, "Goodbye");
        connection.close(AMQP.REPLY_SUCCESS);

    }

    private Connection connect() throws IOException {
        ConnectionParameters parameters = new ConnectionParameters();
        parameters.setUsername("davie");
        parameters.setPassword("davie");
        parameters.setVirtualHost("test");
        parameters.setRequestedHeartbeat(0);


        ConnectionFactory connectionFactory = new ConnectionFactory(parameters);
        Connection connection = connectionFactory.newConnection("localhost", 5672);
        return connection;
    }

    @Test
    public void checkMessageWasSent() throws IOException {
        Connection connection = connect();
        Channel channel = connection.createChannel();
        int ticket = channel.accessRequest("/data/testrealm");


        checkMessageSent(channel, ticket, QUEUE_NAME, "test");
    }

    private void checkMessageSent(Channel channel, int ticket, String queueName, String content) throws IOException {
        boolean noAck = false;
        GetResponse response = channel.basicGet(ticket, queueName, noAck);
        
        if (response == null) {
            // No message retrieved.
            fail("should have had a message");
        } else {
            AMQP.BasicProperties props = response.getProps();
            long deliveryTag = response.getEnvelope().getDeliveryTag();
            assertEquals(content, new String(response.getBody()));
        }
    }


}
