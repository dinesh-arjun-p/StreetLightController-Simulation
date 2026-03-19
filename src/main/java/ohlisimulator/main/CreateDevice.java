package ohlisimulator.main;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ohlisimulator.vendor.*;
import ohlisimulator.serverside.*;
import java.io.FileOutputStream;
import java.util.Properties;
public class CreateDevice implements Runnable {

	int deviceGenerated = 0;
	int deviceSerialNumberStart = 0;

	int batteryVoltage = 0;
	MqttMessageListener listener = MqttMessageListener.getListener();

	int deviceGenerateThread(int noOfDevices, int deviceSerialNumberStart, int batteryVoltage)
			throws InterruptedException {

		this.deviceSerialNumberStart = deviceSerialNumberStart;
		this.batteryVoltage = batteryVoltage;

		boolean connection = false;
		try {
			connection = listener.connectBroker();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (connection) {

			long startTime = System.currentTimeMillis();

			int cores = Runtime.getRuntime().availableProcessors();
			lock = new AtomicInteger(deviceSerialNumberStart);
			deviceGenerateSuccess = new AtomicInteger(1);
//			ForkJoinPool pool = new ForkJoinPool(4);
			ForkJoinPool pool = new ForkJoinPool(Math.min(cores*2,(int)Math.ceil(noOfDevices/2500.0)*4));
			for (int i = 0; i < noOfDevices; i++) {
				pool.execute(this);
			}
			pool.shutdown();

			try {
				pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

			} catch (InterruptedException e) {
				e.printStackTrace()	;
			}
			long endTime = System.currentTimeMillis();
			long difference = endTime - startTime;
			
			

			Properties props = new Properties();

			// value you want to store
			props.setProperty("executionTime", String.valueOf(difference));

			try (FileOutputStream output =
			        new FileOutputStream("Time Taken")) {

			    props.store(output, "Updated execution time");

			} catch (Exception e) {
			    e.printStackTrace();
			}
			System.out.println("Time difference in milliseconds: " + difference);
			return deviceGenerated;
//			return lock.get();
		} else
			return 0;
	}

	@Override
	public void run() {
		// System.out.println(Thread.currentThread().getName()+"Started");
		
		boolean success = deviceGenerate();
		
		if (success)
//			synchronized (this) {
//				deviceGenerated++;
//				// System.out.println("Device Generated:"+deviceGenerated);
//			}
			deviceGenerated=deviceGenerateSuccess.getAndIncrement();
		// System.out.println(Thread.currentThread().getName()+"Ended");
	}

	
	private AtomicInteger lock;
	private AtomicInteger deviceGenerateSuccess;
	
	private boolean deviceGenerate() {
	    int serial;

//	    synchronized (this) {
//	        serial = deviceGenerated++;
//	    }

	    Vendor vendor = new Bosun();
	    return vendor.deviceGenerated(lock.getAndIncrement(),
	                                  
	                                  batteryVoltage);
	}

}
