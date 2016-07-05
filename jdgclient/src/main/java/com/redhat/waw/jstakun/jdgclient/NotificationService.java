package com.redhat.waw.jstakun.jdgclient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang3.StringUtils;

import com.redhat.waw.ose.model.Decision;

@ServerEndpoint("/notification")
public class NotificationService {
	
	private static Logger logger = Logger.getLogger("com.redhat.waw.jstakun.jdgclient.NotificationService");
	
	private static Map<String, Double> registeredSessions = Collections.synchronizedMap(new HashMap<String, Double>());
	
	private static Map<String, Session> clients = Collections.synchronizedMap(new HashMap<String, Session>());
	
	private Runnable intervalNotifier;
	
	@OnMessage
    public String registerForTemperatureNotification(String temperature, Session session) {
        logger.log(Level.INFO, "Registered for " + temperature + " temperature notification.");
        registeredSessions.put(session.getId(), Double.valueOf(temperature).doubleValue());
        clients.put(session.getId(), session);
        return ("Registered for " + temperature +  " temperature notification.");
    }
	
    @OnOpen
    public void notificationOnOpen(Session session) {
    	logger.log(Level.INFO, "WebSocket opened: " + session.getId());
    }

    @OnClose
    public void notificationOnClose(CloseReason reason, Session session) {
    	registeredSessions.remove(session.getId());
    	clients.remove(session.getId());
    	logger.log(Level.INFO, "WebSocket connection closed with CloseCode: " + reason.getCloseCode());
    }
    
    public static void notifyAllSessions(String sensor, Double temperature) {
    	logger.log(Level.INFO, "Received notification " + sensor + ": " + temperature);
        for (Map.Entry<String, Double> s : registeredSessions.entrySet()) {
            if (temperature >= s.getValue()) {
            	try {
            		clients.get(s.getKey()).getBasicRemote().sendObject(sensor + ": " + temperature);
            	} catch (Exception e) {
            		logger.log(Level.SEVERE, e.getMessage(), e);
            	}
            }
        }
    }
    
    @PostConstruct
    public void startIntervalNotifier() {
        logger.log(Level.INFO, "Starting interval notifier");
        String cacheNames = System.getenv("CACHE_NAMES");
		String[] caches = StringUtils.split(cacheNames, ',');
		if (caches != null) {            
			intervalNotifier = new Runnable() {
          
				@Override
				public void run() {
					try {
						while (true) {
							Thread.sleep(10000);
                        	for (int i=0;i<caches.length;i++) {
                        		Decision d = JDGService.getSensorAvg(caches[i], "a");
                        		notifyAllSessions(caches[i], Double.valueOf(d.getValue()));
                        	}
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			};
        
			new Thread(intervalNotifier).start();
        } else {
			logger.log(Level.WARNING, "Caches list is empty!");
		}

    }
}
