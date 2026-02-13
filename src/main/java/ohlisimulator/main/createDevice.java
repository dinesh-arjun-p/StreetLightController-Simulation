package ohlisimulator.main;


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import ohlisimulator.vendor.*;
import ohlisimulator.serverside.*;

public class createDevice implements Runnable{
	int deviceGenerated=0;
	int deviceSerialNumberStart=0;
	int batteryCapacity=0;
	int batteryVoltage=0;
	int deviceGenerateThread(int noOfDevices, int deviceSerialNumberStart, 
			int batteryCapacity, int batteryVoltage) throws InterruptedException {
		
		this.deviceSerialNumberStart=deviceSerialNumberStart;
		this.batteryCapacity=batteryCapacity;
		this.batteryVoltage=batteryVoltage;
		
		mqttMessageListener listener=new mqttMessageListener();
		boolean connection =false;
		try {
			connection=listener.connectBroker();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		if(connection) {
		
			//long startTime = System.currentTimeMillis();
			
			
			ForkJoinPool pool = new ForkJoinPool(8);
			for(int i=0;i<noOfDevices;i++) {
				pool.execute(this);
			}
			pool.shutdown();
			
			try {
			    pool.awaitTermination(1, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
	//		long endTime = System.currentTimeMillis();
	//		long difference = endTime - startTime;
	//		System.out.println("Time difference in milliseconds: " + difference);
			return deviceGenerated;
		}
		else
			return 0;
	}
	@Override
	public 	void run() {
		boolean success=deviceGenerate();
		if(success)
			synchronized(this) {
				deviceGenerated++;
				//System.out.println("Device Generated:"+deviceGenerated);
			}
	}
	private boolean deviceGenerate() {
		Vendor vendor=new Bosun();
		if(vendor.deviceGenerated(deviceSerialNumberStart,batteryCapacity,batteryVoltage)) {
			synchronized(this) {
				deviceSerialNumberStart++;
				return true;
			}
		}
		return false;
	}
	
	
	
	
}
