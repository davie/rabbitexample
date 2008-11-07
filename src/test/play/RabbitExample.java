package play;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import static junit.framework.Assert.assertTrue;


// playing with this http://www.rabbitmq.com/api-guide.html
// had to do a few things on the server

//

//  sudo rabbitmqctl add_vhost test
//  sudo rabbitmqctl add_user davie davie
//  sudo rabbitmqctl map_user_vhost davie test
//  sudo rabbitmqctl add_realm test /data/testrealm
//  sudo rabbitmqctl set_permissions davie test /data/testrealm active read write passive


public class RabbitExample {

    @Test
    public void connectToRabbit() throws IOException {
        MessageSender sender = new MessageSender("queueName");
        sender.sendMessage("Hello, world!".getBytes());

        QueueBrowser queueBrowser = new QueueBrowser("queueName");
        assertEquals("Hello, world!", queueBrowser.consumeMessage());

        sender.cleanup();
    }

    @Test
    public void checkMessageWasSent() throws IOException {
        MessageSender sender = new MessageSender("myQueue");
        sender.sendMessage("test".getBytes());

        QueueBrowser queueBrowser = new QueueBrowser("myQueue");
        assertEquals("test", queueBrowser.consumeMessage());
    }


}
