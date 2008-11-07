package play;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import com.rabbitmq.client.*;

import java.io.IOException;


// playing with this http://www.rabbitmq.com/api-guide.html
// had to do a few things on the server

//

//  sudo rabbitmqctl add_vhost test
//  sudo rabbitmqctl add_user davie davie
//  sudo rabbitmqctl map_user_vhost davie test
//  sudo rabbitmqctl add_realm test /data/testrealm
//  sudo rabbitmqctl set_permissions davie test /data/testrealm active read write passive



public class RabbitExample {
    private static final String EXCHANGE_NAME = "exchangeName";
    private static final String QUEUE_NAME = "queueName";
    private static final String ROUTING_KEY = "routingKey";

    @Test
    public void connectToRabbit() throws IOException {
        Connection connection = connect();
        Channel channel = connection.createChannel();

        for(int i = 0 ; i < 100; i++)  {
            sendMessage(channel, "Hello, world!".getBytes());
        }

        checkMessageSent(channel, QUEUE_NAME, "Hello, world!");

        channel.close(AMQP.REPLY_SUCCESS, "Goodbye");
        connection.close(AMQP.REPLY_SUCCESS);

    }

    private void sendMessage(Channel channel, byte[] message) throws IOException {
        byte[] messageBodyBytes = message;
        channel.basicPublish(channel.accessRequest("/data/testrealm"), EXCHANGE_NAME, ROUTING_KEY, null, messageBodyBytes);
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
        Channel channel = getChannelFor("myQueue");

        sendMessage(channel, "test".getBytes());

        checkMessageSent(channel, "myQueue", "test");
    }

    private Channel getChannelFor(String queueName) throws IOException {
        Connection connection = connect();
        Channel channel = connection.createChannel();

        int ticket = channel.accessRequest("/data/testrealm");
        channel.exchangeDeclare(ticket, EXCHANGE_NAME, "direct");
        channel.queueDeclare(ticket, queueName);
        channel.queueBind(ticket, queueName, EXCHANGE_NAME, ROUTING_KEY);
        return channel;
    }

    private void checkMessageSent(Channel channel, String queueName, String content) throws IOException {
        boolean noAck = false;
        int ticket = channel.accessRequest("/data/testrealm");
        GetResponse response = channel.basicGet(ticket, queueName, noAck);
        
        if (response == null) {
            // No message retrieved.
            fail("should have had a message");
        } else {
            long deliveryTag = response.getEnvelope().getDeliveryTag();
            channel.basicAck(deliveryTag, false); // acknowledge receipt of the message
            assertEquals(content, new String(response.getBody()));

        }
    }


}
