package com.redhat.waw.jstakun.gateway;

public class UtilBean {

	
	public String resolveSensorNameFromTopicName(String topicName) {
		return topicName.substring(topicName.indexOf("/")+1);
	}
}
