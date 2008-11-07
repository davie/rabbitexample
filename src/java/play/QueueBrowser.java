package play;

import com.rabbitmq.client.*;

import java.io.IOException;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

public class QueueBrowser {
    private String queueName;

    public QueueBrowser(String queueName) {

        this.queueName = queueName;
    }

    public String consumeMessage() throws IOException {
        String message = null;
        Connection connection = connect();
        Channel channel = connection.createChannel();

        boolean noAck = false;
        int ticket = channel.accessRequest("/data/testrealm");
        GetResponse response = channel.basicGet(ticket, queueName, noAck);

        if (response == null) {
            // No message retrieved.
            fail("should have had a message");
        } else {
            long deliveryTag = response.getEnvelope().getDeliveryTag();
            channel.basicAck(deliveryTag, false); // acknowledge receipt of the message
            message = new String(response.getBody());

        }

        return message;  //To change body of created methods use File | Settings | File Templates.
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
}
