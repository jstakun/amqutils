package com.redhat.waw.jstakun.gateway;

public class UtilBean {

	
	public String resolveSensorNameFromTopicName(String topicName) throws Exception {
		if (topicName != null) {
			int pos = topicName.indexOf("/");
			if (pos > 0 && pos < topicName.length()) {
				String cacheName = topicName.substring(topicName.indexOf("/")+1);		
				if (Character.isDigit(cacheName.charAt(0))) {		
					cacheName = "S" + cacheName;
				}
				return cacheName;
			} else {
				throw new Exception("Topic name must match pattern prefix/sensor_name !");
			}
		} else {
			throw new Exception("Topic name can't be empty !");
		}
	}

    public String getCurrentMillis() {
    	return Long.toString(System.currentTimeMillis());
    }
}
