package com.redhat.waw.jstakun.jdgclient;

import java.net.InetAddress;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

@Path("/")
public class JDGService {

	private static final String VERSION = "1.0.0";
	
	@GET
	@Path("/info")
	@Produces({"application/xml"})
	public Response info() {
		String addr = "127.0.0.1";
		try {
			InetAddress ip = InetAddress.getLocalHost();
			addr = ip.getHostAddress();	  
		} catch (Exception e) {
			
		}
		return Response.status(200).entity("<info><name>JDG client service</name><version>" + VERSION + "</version><ip>" + addr + "</ip></info>").build();
	}
	
	@GET
	@Produces({"application/xml"})
	public Response root() {
		return info();
	}
	
	@GET
	@Path("/sensor/{cache}/keys")
	@Produces({"application/json"})
	public Set<String> getCacheKeys(@PathParam("cache") String cache) {
		String host = System.getenv("SENSOR_DATAGRID_HOTROD_SERVICE_HOST");
		if (host == null) {
			host = "localhost";
		}
		int port = 11333;
		try {
			port = Integer.valueOf(System.getenv("SENSOR_DATAGRID_HOTROD_SERVICE_PORT"));
		} catch (Exception e) {
			
		}
		
		System.out.println("Reading " + cache + " keys from " + host + ":" + port);
		
		RemoteCache<String, String> sensorCache = getRemoteCache(cache, host, port);
		return sensorCache.keySet();
	}

	public static RemoteCache<String, String> getRemoteCache(String cache, String host, int port) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServer()
			.host(host).port(port);
			//.security()
	        //.authentication()
	        //    .enable()
	        //    .serverName("tasks")
	        //    .saslMechanism("DIGEST-MD5")
	        //    .callbackHandler(new LoginHandler("admin", "manager1".toCharArray(), "ApplicationRealm"));
		return new RemoteCacheManager(builder.build(), true).getCache(cache);
	}
}
