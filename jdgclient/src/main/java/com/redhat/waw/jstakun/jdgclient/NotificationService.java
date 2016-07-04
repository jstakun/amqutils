package com.redhat.waw.jstakun.jdgclient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/notification")
public class NotificationService {
	
	private static Logger logger = Logger.getLogger("com.redhat.waw.jstakun.jdgclient.NotificationService");
	
	private static Map<String, Double> registeredSessions = Collections.synchronizedMap(new HashMap<String, Double>());
	
	private static Map<String, Session> clients = Collections.synchronizedMap(new HashMap<String, Session>());
	
	/*@OnMessage
    public String sayHello(String name) {
        logger.log(Level.INFO, "Say hello to '" + name + "'");
        return ("Hello " + name + " from websocket endpoint");
    }*/

	@OnMessage
    public String registerForTemperatureNotification(String temperature, Session session) {
        logger.log(Level.INFO, "Registered for " + temperature + " temperature notification.");
        registeredSessions.put(session.getId(), Double.valueOf(temperature).doubleValue());
        clients.put(session.getId(), session);
        return ("Registered for " + temperature +  " notification.");
    }
	
    @OnOpen
    public void helloOnOpen(Session session) {
    	logger.log(Level.INFO, "WebSocket opened: " + session.getId());
    }

    @OnClose
    public void helloOnClose(CloseReason reason, Session session) {
    	registeredSessions.remove(session.getId());
    	clients.remove(session.getId());
    	logger.log(Level.INFO, "WebSocket connection closed with CloseCode: " + reason.getCloseCode());
    }
    
    public static void notifyAllSessions(String sensor, Double temperature) {
        for (Map.Entry<String, Double> s : registeredSessions.entrySet()) {
            if (temperature >= s.getValue()) {
            	try {
            		clients.get(s.getKey()).getBasicRemote().sendObject(sensor + ": " + Double.toString(temperature));
            	} catch (Exception e) {
            		logger.log(Level.SEVERE, e.getMessage(), e);
            	}
            }
        }
    }
}
