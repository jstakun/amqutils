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
import com.redhat.waw.ose.model.Decision;

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
		return getRemoteCache(cache).keySet();
	}
	
	@GET
	@Path("/sensor/{cache}/data")
	@Produces({"application/json"})
	public Map<String, Object> getCacheData(@PathParam("cache") String cache) {	
		return getBulk(cache);
	}
	
	@GET
	@Path("/sensor/{cache}/avg/{type}")
	@Produces({"application/json"})
	public Decision getCacheAvg(@PathParam("cache") String cache, @PathParam("type") String type) {
		return getSensorAvg(getBulk(cache), type);
	}

	private static Map<String, Object> getBulk(String cache) {
		return getRemoteCache(cache).getBulk();
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
	
	private static Decision getSensorAvg(Map<String, Object> data, String sensor) {	
		double sum = 0;
		int count = 0;
		for (String key : data.keySet()) {
			Object o = data.get(key);
			if (o instanceof SensorData) {
				SensorData value = (SensorData)o;
				if (sensor.equals("a")) {
					sum += value.getA();
				} else if (sensor.equals("b")) {
					sum += value.getB();
				} else if (sensor.equals("c")) {
					sum += value.getC();
				}
				count++;			
			} else {
				System.out.println("Can't cast from " + o.getClass().getName() + " to " + SensorData.class.getName());
			}
		}
		
		Decision d = new Decision();
		d.setId("avg-a");
		
		double avg = 0;
		if (count > 0) {
			avg = sum/count;
		}
		d.setValue(Double.toString(avg));
		
		return d;
	}
}
