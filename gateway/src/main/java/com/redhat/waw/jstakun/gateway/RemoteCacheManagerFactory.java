package com.redhat.waw.jstakun.gateway;

import java.util.Map;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

public class RemoteCacheManagerFactory {      
    ConfigurationBuilder clientBuilder;
    public RemoteCacheManagerFactory(String hostname, int port) {
        clientBuilder = new ConfigurationBuilder();
        clientBuilder.addServer()
            .host(hostname)
            .port(port);
    }
    public RemoteCacheManager newRemoteCacheManager() {
    	System.out.println("Reading env vars...");
    	Map<String, String> env = System.getenv();
    	for (Map.Entry<String, String> entry : env.entrySet()) {
    		System.out.println(entry.getKey() + ": " + entry.getValue());
    	}
        return new RemoteCacheManager(clientBuilder.build(), true);
    }
}
