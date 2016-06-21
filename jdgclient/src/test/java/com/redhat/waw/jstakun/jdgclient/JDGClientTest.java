package com.redhat.waw.jstakun.jdgclient;

import static org.junit.Assert.*;

import org.junit.Test;

public class JDGClientTest {

	@Test
	public void testWeather() {
		int weather = JDGService.getCurrentPressure("Prag,cz");
		assertNotNull("Weather is null!", weather);
		System.out.println(weather);
	}

}
