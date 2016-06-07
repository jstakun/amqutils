package com.redhat.waw.jstakun.gateway;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;

import com.redhat.waw.iot.model.SensorData;

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
		String[] tokens = dataStr.split(",");
		
		if (tokens.length == 3) {
			sd.setA(Integer.valueOf(tokens[0]));
			sd.setC(Double.valueOf(tokens[1]));
			sd.setB(Float.valueOf(tokens[2]));
		}
		
		sd.setTimestamp(System.currentTimeMillis());
		
		return sd;
	}
	
}
