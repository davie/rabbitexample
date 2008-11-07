package play;

import com.rabbitmq.client.*;

import java.io.IOException;

public class MessageSender {
    private static final String EXCHANGE_NAME = "exchangeName";
    private final String queueName;
    private static final String ROUTING_KEY = "routingKey";
    private Channel channel;
    private Connection connection;

    public MessageSender(String queueName) {
        this.queueName = queueName;
    }


    public void sendMessage(byte[] message) throws IOException {
        Connection connection = connect();
        Channel channel = getChannel(connection);

        int ticket = channel.accessRequest("/data/testrealm");
        channel.exchangeDeclare(ticket, EXCHANGE_NAME, "direct");
        channel.queueDeclare(ticket, queueName);
        channel.queueBind(ticket, queueName, EXCHANGE_NAME, ROUTING_KEY);

        channel.basicPublish(channel.accessRequest("/data/testrealm"), EXCHANGE_NAME, ROUTING_KEY, null, message);

    }

    private Channel getChannel(Connection connection) throws IOException {
        if(channel == null){
            channel = connection.createChannel();
        }
        return channel;
    }


    private Connection connect() throws IOException {
        if(connection == null){
            ConnectionParameters parameters = new ConnectionParameters();
            parameters.setUsername("davie");
            parameters.setPassword("davie");
            parameters.setVirtualHost("test");
            parameters.setRequestedHeartbeat(0);

            ConnectionFactory connectionFactory = new ConnectionFactory(parameters);
            connection = connectionFactory.newConnection("localhost", 5672);
        }
        return connection;
    }

    public void cleanup() throws IOException {
        channel.close(AMQP.REPLY_SUCCESS, "Goodbye");
        connection.close(AMQP.REPLY_SUCCESS);        
    }
}
