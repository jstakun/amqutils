package com.redhat.waw.jstakun.amq;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.util.ByteSequence;

public class TransactionsConsumer implements Runnable, ExceptionListener, Commons {
	
	private static int max_iter = MAX_ITER;
 
	//java -jar amqutils-0.0.1-SNAPSHOT.jar 5
	public static void main(String[] args)
	{
		try {
			if (args.length > 0) {
				max_iter = Integer.parseInt(args[0]);
				System.out.println("I will iterate " + max_iter + " times.");
			}
		} catch (Exception e) {
			
		}
		
		try {
			TransactionsConsumer consumer = new TransactionsConsumer();
			consumer.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		Connection connection = null;
        try {

            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            connection = connectionFactory.createConnection(user, password);
            connection.setClientID("demoClient");
            connection.start();
            connection.setExceptionListener(this);
            Session session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);

            //non durable subscriber
            //Destination destination = session.createTopic(topicName);
            //MessageConsumer consumer = session.createConsumer(destination);
            
            //durable subscriber
            //Topic topic = session.createTopic(topicName);
            //MessageConsumer consumer = session.createDurableSubscriber(topic, "demoConsumer");
            
            Queue queue = session.createQueue(queueName);
            MessageConsumer consumer =session.createConsumer(queue);
            
            System.out.println("Starting consumer");

            for (int i=0;i<max_iter;i++) {
            	Message message = consumer.receive(2000);
            	System.out.println("Iteration (" + i + "/" + MAX_ITER + ").");
            	if (message instanceof TextMessage) {
            		TextMessage textMessage = (TextMessage) message;
            		String text = textMessage.getText();
            		System.out.println(i + ". Received: " + text);
            	} else if (message instanceof ActiveMQBytesMessage) {
            		ActiveMQBytesMessage bytesMessage = (ActiveMQBytesMessage) message;
            		ByteSequence bytesSeq = bytesMessage.getContent();
            		String text = new String (bytesSeq.data);
            		System.out.println(i + ". Decoded: " + text);
            	} else {
            		System.out.println(i + ". Received: " + message);
            	}
            }
            
            System.out.println("Closing consumer");
            
            consumer.close();
            session.close();
            
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        } finally {
        	if (connection != null) {
        		try {
        			connection.close();
        		} catch (Exception e) {
        	        System.out.println("Caught: " + e);
        	        e.printStackTrace();
        	    }
        	}		
        }
    }
	
    public synchronized void onException(JMSException ex) {
        System.out.println("JMS Exception occured.  Shutting down client.");
        ex.printStackTrace();
    }
}
