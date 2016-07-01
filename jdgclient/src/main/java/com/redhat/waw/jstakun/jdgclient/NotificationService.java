package com.redhat.waw.jstakun.jdgclient;

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

	
	private Logger logger = Logger.getLogger(getClass().getName());
	
	@OnMessage
    public String sayHello(String name) {
        logger.log(Level.INFO, "Say hello to '" + name + "'");
        return ("Hello " + name + " from websocket endpoint");
    }

    @OnOpen
    public void helloOnOpen(Session session) {
    	logger.log(Level.INFO, "WebSocket opened: " + session.getId());
    }

    @OnClose
    public void helloOnClose(CloseReason reason) {
    	logger.log(Level.INFO, "WebSocket connection closed with CloseCode: " + reason.getCloseCode());
    }
}
