package com.redhat.waw.iot.model;


import java.io.Serializable;

public class SensorData implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private long timestamp;
	
	private int a;
	
	private float b;
	
	private double c;
	
	private String sensorId;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public float getB() {
		return b;
	}

	public void setB(float b) {
		this.b = b;
	}

	public double getC() {
		return c;
	}

	public void setC(double c) {
		this.c = c;
	}

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}
	
	public String toString() {
		return sensorId + ": " + timestamp + "," + a +  "," + b + "," + c;
	}

}
