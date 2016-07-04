package com.redhat.waw.jstakun.jdgclient;

import java.io.StringReader;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.validation.constraints.NotNull;
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
	
	//sensors endpoints
	
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
	@Path("/{cache}/keys")
	@Produces({"application/json"})
	public Set<String> getCacheKeys(@PathParam("cache") String cache) {	
		return getRemoteCache(cache).keySet();
	}
	
	@GET
	@Path("/{cache}/data")
	@Produces({"application/json"})
	public Map<String, Object> getCacheData(@PathParam("cache") String cache) {	
		return getBulk(cache);
	}
	
	@GET
	@Path("/{cache}/avg/{type}")
	@Produces({"application/json"})
	public Decision getCacheAvg(@PathParam("cache") String cache, @PathParam("type") String type) {
		return getSensorAvg(getBulk(cache), type);
	}
	
	@GET
	@Path("/{cache}/clear")
	@Produces({"application/json"})
	public Response clearCache(@PathParam("cache") String cache) {
		getRemoteCache(cache).clear();
		return Response.status(200).entity("{\"status\": \"ok\"}").build();	
	}
	
	//weather endpoints
	
	/*@GET
	@Path("/rain/prague")
	@Produces({"application/json"})
	public Decision getRainProbabilty() {
		Map<String, Object> data = getBulk("13556381");
		int probability = 0;
		double humidityAvg = Double.valueOf(getSensorAvg(data, "c").getValue()).doubleValue(); 
		if (humidityAvg >= 50 && humidityAvg <= 100) {
			probability += (humidityAvg - 50);
		}
		int pressure = getCurrentPressure("Prag,cz");
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
	}*/
	
	@GET
	@Path("/pressure/{location}")
	@Produces({"application/json"})
	public Decision getPressure(@NotNull @PathParam("location") String location) {
		Decision d = new Decision();	
		d.setId(location + " current pressure");
		d.setValue(Integer.toString(getCurrentPressure(location)));	
		return d;
	}
	
	@GET
	@Path("/notify")
	@Produces({"application/json"})
	public Response sendNotification() {
		
		//TODO read from env variable
		String[] caches = {"sensor1","sensor2","S13556381","S8633913","S13540890"};
		
		for (int i=0;i<caches.length;i++) {
			Decision d = getSensorAvg(getBulk(caches[i]), "a");
			NotificationService.notifyAllSessions(caches[i], Double.valueOf(d.getValue()));
		}
		return Response.status(200).entity("{\"status\": \"ok\"}").build();	
	}
	
	//utils
	
	private static Map<String, Object> getBulk(String cache) {
		return getRemoteCache(cache).getBulk();
	}

	private static RemoteCacheManager getRemoteCacheManager() {
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
		
		return rcm;
	}
	
	private static RemoteCache<String, Object> getRemoteCache(String cache) {
		System.out.println("Reading S" + cache + " data...");
		return getRemoteCacheManager().getCache("S" + cache);
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
	
	protected static int getCurrentPressure(String location) {
		try {
			Client client = ClientBuilder.newClient();
			String weather = client.target("http://api.openweathermap.org/data/2.5/weather?q=" + location + "&APPID=ea915ccfec3c2dd13466103568649663&units=metric")
		        .request(MediaType.APPLICATION_JSON)
		        .get(String.class);
		
			JsonReader jsonReader = Json.createReader(new StringReader(weather));
		
			JsonObject root = jsonReader.readObject();
		
			jsonReader.close();
		
			JsonObject main = root.getJsonObject("main");
			
			int pressure = main.getInt("pressure");
			
			System.out.println("Received current Prague pressure: " + pressure);
		
			return pressure;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return -1;
		}
	}
	
	
}
