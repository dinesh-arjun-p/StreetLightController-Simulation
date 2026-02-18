package ohlisimulator.controller;

import java.io.InputStream;
import java.util.*;

import ohlisimulator.service.Service;
import ohlisimulator.vendor.Bosun;
import ohlisimulator.vendor.Vendor;

public class DiscoveryRetryScheduler implements Runnable{
	
	List<Long> time=new ArrayList<>();
	
	Service service=new Service();
	Vendor vendor=new Bosun();
	private boolean retryMethod() {
		if(service.isEmptyUndiscovered())
			return true;
		long now = System.currentTimeMillis(); 
		List<String> devices=service.getCurrentRetryReadyDevice(now,time);
		for(String device:devices) {
			String topic ="device/"+device;
			publishControllerInfo(topic,"0",service.obtainControllerInfo(topic));
		}
		return false;
	}
	
	public void publishControllerInfo(String topic,String cmd,String...info ) {
		vendor.publishControllerInfo(topic,cmd,info);
	}
	
	
	public void run() {
		Properties props = new Properties();
		InputStream input = DiscoveryRetryScheduler.class
	            .getClassLoader()
	            .getResourceAsStream("config.properties");
		try {
			props.load(input);
			String probeRetryTime1=props.getProperty("probeRetryTime1");
			String probeRetryTime2=props.getProperty("probeRetryTime2");
			String probeRetryTime3=props.getProperty("probeRetryTime3");
			String probeRetryTime4=props.getProperty("probeRetryTime4");
			time.add(convertToLong(probeRetryTime1));
			time.add(convertToLong(probeRetryTime2));
			time.add(convertToLong(probeRetryTime3));
			time.add(convertToLong(probeRetryTime4));
	    	
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
		while(true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				break;
			}
			System.out.println("DiscoveryRun\n");
			boolean allDiscovered=retryMethod();
			if(allDiscovered)
				break;
		}
		
	}
	
	
	 
	private long convertToLong(String probeRetryTime) {
		if(probeRetryTime.equals("+inf"))
			return Long.MAX_VALUE;
		else
			return Long.parseLong(probeRetryTime);
	}
}
