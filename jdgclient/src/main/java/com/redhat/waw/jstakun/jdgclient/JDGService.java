package com.redhat.waw.jstakun.jdgclient;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import com.redhat.waw.iot.model.SensorData;

@Path("/")
public class JDGService {

	private static final String VERSION = "1.0.0";
	
	private static RemoteCacheManager rcm;
	
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
		RemoteCache<String, Object> sensorCache = getRemoteCache(cache);
		return sensorCache.keySet();
	}
	
	@GET
	@Path("/sensor/{cache}/data")
	@Produces({"application/json"})
	public Map<String, Object> getCacheData(@PathParam("cache") String cache) {	
		RemoteCache<String, Object> sensorCache = getRemoteCache(cache);
		return sensorCache.getBulk();
	}
	
	@GET
	@Path("/sensor/{cache}/avg/a")
	@Produces({"application/json"})
	public Double getCacheAvgA(@PathParam("cache") String cache) {	
		RemoteCache<String, Object> sensorCache = getRemoteCache(cache);
		Map<String, Object> data = sensorCache.getBulk();
		int sum = 0;
		int count = 0;
		for (String key : data.keySet()) {
			Object value = data.get(key);
			if (value instanceof SensorData) {
				sum += ((SensorData)value).getA();
				count++;
			} else {
				System.out.println("Entry with key " + key + " is instanceof " + value.getClass().getName());
			}
		}
		
		if (count == 0) {
			return (double)count;
		} else {
			return sum/(double)count;
		}
	}

	private static RemoteCache<String, Object> getRemoteCache(String cache) {
		if (rcm == null) {
			
			System.out.println("Creating RemoteCacheManager instance...");
			
			String host = System.getenv("SENSOR_DATAGRID_HOTROD_SERVICE_HOST");
			if (host == null) {
				host = "localhost";
			}
			int port = 11333;
			try {
				port = Integer.valueOf(System.getenv("SENSOR_DATAGRID_HOTROD_SERVICE_PORT"));
			} catch (Exception e) {
				
			}
			
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.addServer()
				.host(host).port(port);
			//.security()
	        //.authentication()
	        //    .enable()
	        //    .serverName("tasks")
	        //    .saslMechanism("DIGEST-MD5")
	        //    .callbackHandler(new LoginHandler("admin", "manager1".toCharArray(), "ApplicationRealm"));
			rcm = new RemoteCacheManager(builder.build(), true); 		
		}
		
		System.out.println("Reading " + cache + " data...");
		
		return rcm.getCache(cache);
	}
}
