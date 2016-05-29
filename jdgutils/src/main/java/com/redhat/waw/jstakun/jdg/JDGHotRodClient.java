package com.redhat.waw.jstakun.jdg;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

public class JDGHotRodClient {

	//java -cp "jdgutils-0.0.1-SNAPSHOT.jar:infinispan-client-hotrod-6.3.0.Final-redhat-5.jar:infinispan-commons-6.3.0.Final-redhat-5.jar:jboss-logging-3.1.4.GA-redhat-2.jar:commons-pool-1.6.jar:jboss-marshalling-1.4.10.Final-redhat-1.jar:jboss-marshalling-river-1.4.10.Final-redhat-1.jar" com.redhat.waw.jstakun.jdg.JDGHotRodClient
	
	public static void main(String[] args) {
		
		String cache = "default";
		if (args.length > 0) {
			cache = args[0];
		} 
		System.out.println("Cache " + cache + " will be used.");
		
		RemoteCache<String, String> sensors = getRemoteCache(cache);
		sensors.put("sensordata", "test");
		
		System.out.println("Received cache data: " + sensors.get("sensordata"));

	}

	
	public static RemoteCache<String, String> getRemoteCache(String cache) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServer()
			.host("172.30.51.53").port(11333);
			//.security()
	        //.authentication()
	        //    .enable()
	        //    .serverName("tasks")
	        //    .saslMechanism("DIGEST-MD5")
	        //    .callbackHandler(new LoginHandler("thomas", "thomas-123".toCharArray(), "ApplicationRealm"));
		return new RemoteCacheManager(builder.build(), true).getCache(cache);
	}
}
