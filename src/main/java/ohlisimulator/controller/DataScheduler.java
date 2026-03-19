package ohlisimulator.controller;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

import ohlisimulator.service.Service;
import ohlisimulator.vendor.Bosun;
import ohlisimulator.vendor.Vendor;

public class DataScheduler {

	private final Service service = new Service();

	private ScheduledExecutorService scheduler;
	private ExecutorService workerPool1, workerPool2;

	
	private final List<Long> retryTime = new ArrayList<>();

	private final ThreadLocal<Bosun> threadBosun = ThreadLocal.withInitial(Bosun::new);
	public static long lastTimeUpdatedAutomateDatas;
	ForkJoinPool pool;
	static long totalTime = 0;
	static int runCount = 0;
	int automateDuration=1;
	public void start() {
		
		loadConfig();
		
		scheduler = Executors.newScheduledThreadPool(3);
		
		// ONE worker pool (change to 2 if needed)

		
		
		AutomateDatas automateDatas = new AutomateDatas(automateDuration*60);
		List<String> devices = service.getAllDevice();
//		pool = new ForkJoinPool(String.valueOf(devices.size()).length());
		int cores = Runtime.getRuntime().availableProcessors();
		 pool = new ForkJoinPool(Math.min(cores*4,(int)Math.ceil(devices.size()/1250.0)*2));
		scheduler.scheduleAtFixedRate(() -> {
			lastTimeUpdatedAutomateDatas=System.currentTimeMillis();
			automateDatas.start(pool,devices);
			System.out.println("AutoMateDatas Started");

		}, 0, automateDuration, TimeUnit.MINUTES);
		
		scheduler.scheduleAtFixedRate(() -> {
			
			
			
			long now = System.currentTimeMillis();

			// ---- 1️⃣ Discovery Retry Logic ----
			if (!service.isEmptyUndiscovered()) {
//				long start = System.nanoTime(); 
				List<String> retryDevices = service.getCurrentRetryReadyDevice(now);
				if (retryDevices.size() > 0) {
//					workerPool1 = Executors.newFixedThreadPool(String.valueOf(retryDevices.size()).length());
					workerPool1 = Executors.newFixedThreadPool(Math.min(cores*2,(int)Math.ceil(retryDevices.size()/1250.0)*1));
					for (String device : retryDevices) {
						workerPool1.execute(() -> {
							service.processDevice(device, now, retryTime);
							publishProbe(device);
						});
					}
					workerPool1.shutdown();
					try {
						workerPool1.awaitTermination(60,TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
//				long end = System.nanoTime();   // end time
//
//			    long timeMs = (end - start) / 1_000_000;
//
//			    synchronized(DataScheduler.class) {
//				    totalTime += timeMs;
//				    runCount++;
//				    
//
//			    if(runCount == 3){
//			    	runCount=0;
//			        writeTimeToFile(totalTime);
//			    }
//			    }
			}

			

		}, 0, 60, TimeUnit.SECONDS);
		
		
		scheduler.scheduleAtFixedRate(() -> {
//			long start = System.nanoTime(); 
			long now=System.currentTimeMillis();
			List<String> discoveredDevices = service.getDiscoveryDeviceFilter(now);
			System.out.println("Discovered Device:"+discoveredDevices.size());
			if(discoveredDevices.size()>0) {
				workerPool2 = Executors.newFixedThreadPool(Math.min(cores*2,(int)Math.ceil(discoveredDevices.size()/1250.0)*1));
//				workerPool2 = Executors.newFixedThreadPool(String.valueOf(discoveredDevices.size()).length());
			for (String device : discoveredDevices) {
				workerPool2.execute(() -> publishRealTime(device));
			}
			workerPool2.shutdown();
			try {
				workerPool2.awaitTermination(60,TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
//			long end = System.nanoTime();   // end time
//
//		    long timeMs = (end - start) / 1_000_000;
//		    synchronized(DataScheduler.class) {
//		    totalTime += timeMs;
//		    runCount++;
//		   
//
//		    if(runCount == 3){
//		    	runCount=0;
//		        writeTimeToFile(totalTime);
//		    }
//		    }
		}, 0, 60, TimeUnit.SECONDS);
		
		

	}
	
	public synchronized static void writeTimeToFile(long totalTime){
	    try(java.io.FileWriter fw = new java.io.FileWriter("execution_time.txt", true);
	        java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)){

	        bw.write("Total time for 3 runs: " + totalTime + " ms");
	        bw.newLine();

	    }catch(Exception e){
	        e.printStackTrace();
	    }
	}

	private void publishProbe(String device) {

		Vendor vendor = threadBosun.get();

		String topic = "BS_Dev/" + device;

		vendor.publishControllerInfo(topic, "0", service.obtainControllerInfo(device));
	}

	private void publishRealTime(String device) {

		Vendor vendor = threadBosun.get();
		vendor.publishRealTimeMetrics(device);
	}

	private void loadConfig() {

		try {
			Properties props = new Properties();
			InputStream input = new FileInputStream("config/config.properties");

			props.load(input);
			
			automateDuration=Integer.parseInt(props.getProperty("automateDataDuration"));
			
			retryTime.add(convert(props.getProperty("probeRetryTime1")));
			retryTime.add(convert(props.getProperty("probeRetryTime2")));
			retryTime.add(convert(props.getProperty("probeRetryTime3")));
			retryTime.add(convert(props.getProperty("probeRetryTime4")));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private long convert(String value) {
		return value.equals("+inf") ? Long.MAX_VALUE : Long.parseLong(value);
	}

	public void shutdown() {
		scheduler.shutdown();
		if(pool!=null)
			pool.shutdown();
		if(workerPool1!=null)
		workerPool1.shutdown();
		if(workerPool2!=null)
		workerPool2.shutdown();
	}
}
