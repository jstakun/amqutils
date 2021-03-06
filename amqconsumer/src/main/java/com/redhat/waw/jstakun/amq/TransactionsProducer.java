package com.redhat.waw.jstakun.amq;

import java.util.ResourceBundle;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class TransactionsProducer implements Runnable, ExceptionListener {
	
	private static int max_iter = 100;
	private static ResourceBundle connProps;
	
	//java -cp amqutils-0.0.1-SNAPSHOT.jar com.redhat.waw.jstakun.amq.TransactionsProducer 5
	public static void main(String[] args) {
		
		try {
			connProps = ResourceBundle.getBundle("amq");
			if (connProps == null) {
				throw new NullPointerException("missing amq.properties file!");
			}
			if (args.length > 0) {
				max_iter = Integer.parseInt(args[0]);
				System.out.println("I will iterate " + max_iter + " times.");
			}
		} catch (Exception e) {
			
		}
		
		try {
			TransactionsProducer consumer = new TransactionsProducer();
			consumer.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		Connection connection = null;
		try {

			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					connProps.getString("url"));
			connection = connectionFactory.createConnection(connProps.getString("user"), connProps.getString("password"));
			connection.setClientID("demoProducer");
			connection.start();
			connection.setExceptionListener(this);
			Session session = connection.createSession(Boolean.parseBoolean(connProps.getString("transacted")), Session.AUTO_ACKNOWLEDGE);

			Queue queue = session.createQueue(connProps.getString("queueName"));
			MessageProducer producer = session.createProducer(queue);
			
			System.out.println("Starting producer...");

			for (int i = 1; i <= max_iter; i++) {
				String transaction = "{\"" + System.currentTimeMillis() + i + "\":\"29\",\"customerid\":\"CST01010\",\"amount\":523.45,\"transactionDate\":" + System.currentTimeMillis() + "}";
				TextMessage message = session.createTextMessage();
				message.setText(transaction);
				producer.send(message);		
				System.out.println("Created message (" + i + "/" + max_iter + ")");
			}

			System.out.println("Closing producer.");

			producer.close();
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
