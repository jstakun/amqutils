package com.redhat.waw.jstakun.gateway;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;

@Converter
public class SensorConverter {
	
	private static UtilBean ub = new UtilBean();
	
	@Converter
	public static SensorData toSensorData(byte[] data, Exchange exchange) throws Exception {
		
		SensorData sd = new SensorData();
		
		String topicName = (String)exchange.getIn().getHeader("CamelMQTTSubscribeTopic");
		
		String sensorId = ub.resolveSensorNameFromTopicName(topicName);
		
		sd.setSensorId(sensorId);
		
		String dataStr = new String(data);
		//TODO parse data string
		
		sd.setTimestamp(System.currentTimeMillis());
		
		return sd;
	}
	
}
