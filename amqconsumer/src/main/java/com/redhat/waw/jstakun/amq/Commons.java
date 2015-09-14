package com.redhat.waw.jstakun.amq;

public interface Commons {
	public static final String queueName = "transactions";
	public static final String user = "userldd";
	public static final String password = "JfYG3buP";
	public static final String url = "tcp://localhost:61616";
	public static final boolean transacted = false;
	public static final int MAX_ITER = 1000;
}
