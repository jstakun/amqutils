package com.redhat.waw.jstakun.mqtt;

import java.util.Random;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

public class Producer {

	//java -cp "mqttutils-0.0.1-SNAPSHOT.jar:mqtt-client-1.12.jar:hawtbuf-1.11.jar:hawtdispatch-transport-1.21.jar:hawtdispatch-1.21.jar" com.redhat.waw.jstakun.mqtt.Producer
	
	private static String sensor = "sensor1";
	private static String message = "Default message";
	private static Random r = new Random();
	
	public static void main(String[] args) {
		MQTT mqtt = new MQTT();
		
		if (args.length > 0) {
			message = args[0];
			if (args.length > 1) {
				sensor = args[1];
			}
		}
		
		try {
			mqtt.setHost("master.osecloud.com", 31883); //using node port
			//mqtt.setHost("172.30.214.17", 1883);
			mqtt.setUserName("admin");
			mqtt.setPassword("manager1");
			BlockingConnection connection = mqtt.blockingConnection();
	        connection.connect();

	        //message = "blink"; //off, on
	        //connection.publish("iotdemocommand/light", message.getBytes(), QoS.AT_LEAST_ONCE, false);
	        
	        for (int i=1;i<=100;i++) {
	        	message = r.nextInt(100) + "," + r.nextGaussian() + "," + r.nextFloat();
	        	System.out.println(i + ". Sending message " + message + " to sensor.receiver/" + sensor);
	        	connection.publish("sensor.receiver/" + sensor, message.getBytes(), QoS.AT_LEAST_ONCE, false);
	        	if (i < 100) {
	        		Thread.sleep(10000);
	        	}
	        }
	        System.out.println("Done");
	        
	        connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

        
	}

}
