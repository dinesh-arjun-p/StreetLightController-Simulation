package ohlisimulator.controller;

import java.io.InputStream;
import java.util.Properties;

import ohlisimulator.service.Service;

public class DataScheduler {
	static boolean stop=false;
	int duration;
	{
		Properties props = new Properties();
		InputStream input = Service.class.getClassLoader().getResourceAsStream("config.properties");
		try {
			props.load(input);
			duration = Integer.parseInt(props.getProperty("dataSchedulerDuration"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void setStop(boolean stop) {
		DataScheduler.stop=stop;
	}
	public void run() {
		while(!stop) {
			
		}
	}
}
