package com.redhat.waw.jstakun.mqtt;

import static org.fusesource.hawtbuf.Buffer.utf8;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

public class Consumer {
	
	//java -cp "mqttutils-0.0.1-SNAPSHOT.jar:mqtt-client-1.12.jar:hawtbuf-1.11.jar:hawtdispatch-transport-1.21.jar:hawtdispatch-1.21.jar" com.redhat.waw.jstakun.mqtt.Consumer
	
	public static void main(String[] args) {
		MQTT mqtt = new MQTT();
		
		try {
			mqtt.setHost("172.30.214.17", 1883);
			mqtt.setUserName("admin");
			mqtt.setPassword("manager1");
			BlockingConnection connection = mqtt.blockingConnection();
	        connection.connect();

	        Topic[] topics = {new Topic(utf8("sensor.receiver"), QoS.AT_LEAST_ONCE)};
	        byte[] qoses = connection.subscribe(topics);
	        
	        Message message = connection.receive();
	        
	        System.out.println("Received following message: " + new String(message.getPayload()));
	        
	        message.ack();
	        
	        connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

        
	}

}