package com.redhat.waw.jstakun.amq;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.NamingException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.util.ByteSequence;

public class OseV2Consumer implements Runnable, ExceptionListener {
  
	private static final String topicName = "routinginfo";
	private static final String user = "routinginfo";
	private static final String password = "manager1";
    private static final String url = "tcp://broker.redhat.pl:61616";
	private static final boolean transacted = false;
	
	public static void main(String[] args) throws NamingException, JMSException
	{
		OseV2Consumer consumer = new OseV2Consumer();
		consumer.run();
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
            Topic topic = session.createTopic(topicName);
            MessageConsumer consumer = session.createDurableSubscriber(topic, "demoConsumer");
            
            System.out.println("Starting consumer");

            for (int i=0;i<1000;i++) {
            	Message message = consumer.receive(2000);

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
            
            //---
            //:action: :create_application
            //:app_name: wasndtest
            //:namespace: ose2
            //:scalable: false
            //:ha: false
            
            //---
            //:action: :delete_application
            //:app_name: wasndtest
            //:namespace: ose2
            //:scalable: false
            //:ha: false

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
    }
}
