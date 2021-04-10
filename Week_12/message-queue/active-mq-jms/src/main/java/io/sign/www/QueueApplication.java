package io.sign.www;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueApplication {

    public static void main(String[] args) {
        Destination destination = new ActiveMQQueue("test.queue");
        testDestination(destination);
    }

    public static void testDestination(Destination destination) {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
            Connection connection = factory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(destination);
            final AtomicInteger count = new AtomicInteger(0);
            MessageListener listener = new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        System.out.println(count.incrementAndGet() + " => receive from " + destination.toString() + ": " + message);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            consumer.setMessageListener(listener);
            MessageProducer producer = session.createProducer(destination);
            int index = 0;
            while (index++ < 100) {
                TextMessage message = session.createTextMessage(index + " message.");
                producer.send(message);
            }
            Thread.sleep(2000);
            session.close();
            connection.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
