package com.redhat.waw.jstakun.mqtt;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

public class Producer {

	//java -cp "mqttutils-0.0.1-SNAPSHOT.jar:mqtt-client-1.12.jar:hawtbuf-1.11.jar:hawtdispatch-transport-1.21.jar:hawtdispatch-1.21.jar" com.redhat.waw.jstakun.mqtt.Producer
	
	private static final String message = "Hello";
	
	public static void main(String[] args) {
		MQTT mqtt = new MQTT();
		
		try {
			//mqtt.setHost("master.osecloud.com", 1883);
			mqtt.setHost("172.30.214.17", 1883);
			mqtt.setUserName("admin");
			mqtt.setPassword("manager1");
			BlockingConnection connection = mqtt.blockingConnection();
	        connection.connect();

	        System.out.println("Sending message: " + message);
	        connection.publish("sensor.receiver", message.getBytes(), QoS.AT_LEAST_ONCE, false);
	        System.out.println("Done");
	        
	        connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

        
	}

}
