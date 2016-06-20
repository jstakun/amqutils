package com.redhat.waw.jstakun.jdgclient;

import java.io.StringReader;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
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
	
	@GET
	@Path("/rain/prague")
	@Produces({"application/json"})
	public Decision getRainProbabilty() {
		Map<String, Object> data = getBulk("13556381");
		int probability = 0;
		double humidityAvg = Double.valueOf(getSensorAvg(data, "c").getValue()).doubleValue(); 
		if (humidityAvg > 50) {
			probability += (humidityAvg - 50);
		}
		int pressure = getCurrentPressure();
		if (pressure > 0 && pressure < 1000) {
			probability += (1000 - pressure);
		}
		if (probability > 100) {
			probability = 100;
		}
		
		Decision d = new Decision();
		
		d.setId("Prague rain probability %");
		d.setValue(Integer.toString(probability));
		
		return d;
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
		String id = "unknown";
		for (String key : data.keySet()) {
			Object o = data.get(key);
			if (o instanceof SensorData) {
				SensorData value = (SensorData)o;
				if (sensor.equals("a")) {
					sum += value.getA();
					id = "temperature";
				} else if (sensor.equals("b")) {
					sum += value.getB();
					id = "voltage";
				} else if (sensor.equals("c")) {
					sum += value.getC();
					id = "humidity";
				}
				count++;			
			} else {
				System.out.println("Can't cast from " + o.getClass().getName() + " to " + SensorData.class.getName());
			}
		}
		
		Decision d = new Decision();
		d.setId(id);
		
		double avg = 0;
		if (count > 0) {
			avg = sum/count;
		}
		d.setValue(Double.toString(avg));
		
		return d;
	}
	
	public static int getCurrentPressure() {
		try {
			Client client = ClientBuilder.newClient();
			String weather = client.target("http://api.openweathermap.org/data/2.5/weather?q=Prag,cz&APPID=ea915ccfec3c2dd13466103568649663&units=metric")
		        .request(MediaType.APPLICATION_JSON)
		        .get(String.class);
		
			JsonReader jsonReader = Json.createReader(new StringReader(weather));
		
			JsonObject root = jsonReader.readObject();
		
			jsonReader.close();
		
			JsonObject main = root.getJsonObject("main");
		
			return main.getInt("pressure");
		} catch (Exception e) {
			return -1;
		}
	}
	
	
}
