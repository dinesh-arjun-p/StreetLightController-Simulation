package ohlisimulator.main;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import ohlisimulator.vendor.*;
import ohlisimulator.serverside.*;

public class CreateDevice implements Runnable {

	int deviceGenerated = 0;
	int deviceSerialNumberStart = 0;
	long batteryCapacity = 0;
	int batteryVoltage = 0;
	MqttMessageListener listener = MqttMessageListener.getListener();

	int deviceGenerateThread(int noOfDevices, int deviceSerialNumberStart, long batteryCapacity, int batteryVoltage)
			throws InterruptedException {

		this.deviceSerialNumberStart = deviceSerialNumberStart;
		this.batteryCapacity = batteryCapacity;
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
			ForkJoinPool pool = new ForkJoinPool(cores * 2);
			for (int i = 0; i < noOfDevices; i++) {
				pool.execute(this);
			}
			pool.shutdown();

			try {
				pool.awaitTermination(1, TimeUnit.MINUTES);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long endTime = System.currentTimeMillis();
			long difference = endTime - startTime;
			System.out.println("Time difference in milliseconds: " + difference);
			return deviceGenerated;
		} else
			return 0;
	}

	@Override
	public void run() {
		// System.out.println(Thread.currentThread().getName()+"Started");
		boolean success = deviceGenerate();
		if (success)
			synchronized (this) {
				deviceGenerated++;
				// System.out.println("Device Generated:"+deviceGenerated);
			}
		// System.out.println(Thread.currentThread().getName()+"Ended");
	}

	

	private boolean deviceGenerate() {
	    int serial;

	    synchronized (this) {
	        serial = deviceSerialNumberStart++;
	    }

	    Vendor vendor = new Bosun();
	    return vendor.deviceGenerated(serial,
	                                  batteryCapacity,
	                                  batteryVoltage);
	}

}
