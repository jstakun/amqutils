package com.redhat.waw.jstakun.jdg;

import java.util.Set;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

public class JDGHotRodClient {

	//java -cp "jdgutils-0.0.1-SNAPSHOT.jar:infinispan-client-hotrod-6.3.0.Final-redhat-5.jar:infinispan-commons-6.3.0.Final-redhat-5.jar:jboss-logging-3.1.4.GA-redhat-2.jar:commons-pool-1.6.jar:jboss-marshalling-1.4.10.Final-redhat-1.jar:jboss-marshalling-river-1.4.10.Final-redhat-1.jar" com.redhat.waw.jstakun.jdg.JDGHotRodClient
	private static final String key = "testdata";
	
	public static void main(String[] args) {
		
		String cache = "default";
		String host = "172.30.51.53";
		int port = 11333;
		if (args.length > 0) {
			cache = args[0];
			if (args.length > 1) {
				host = args[1];
			}
			if (args.length > 2) {
				port = Integer.valueOf(args[2]);
			}
		} 
		System.out.println("Host: " + host + ":" + port + ", cache: " + cache + " will be used.");
		
		RemoteCache<String, Object> sensors = getRemoteCache(cache, host, port);
		System.out.println("Putting entry " + key + " to cache " + cache);
		sensors.put(key, "test");
		
		Set<String> allEntries = sensors.keySet();
		
		System.out.println("Found following entries in cache " + cache + ":");
		for (String entry : allEntries) {
			System.out.println(entry);
		}
		
		/*for (int i = 0;i< 10;i++) {
			try {
				Thread.sleep(10000);
				System.out.println("Received cache data: " + sensors.get(key));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
	}

	
	public static RemoteCache<String, Object> getRemoteCache(String cache, String host, int port) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServer()
			.host(host).port(port);
			//.security()
	        //.authentication()
	        //    .enable()
	        //    .serverName("tasks")
	        //    .saslMechanism("DIGEST-MD5")
	        //    .callbackHandler(new LoginHandler("thomas", "thomas-123".toCharArray(), "ApplicationRealm"));
		return new RemoteCacheManager(builder.build(), true).getCache(cache);
	}
}
