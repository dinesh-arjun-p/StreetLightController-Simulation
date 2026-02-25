package ohlisimulator.controller;

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

	private long dataDuration;
	private final List<Long> retryTime = new ArrayList<>();

	private final ThreadLocal<Bosun> threadBosun = ThreadLocal.withInitial(Bosun::new);

	public void start() {

		loadConfig();

		scheduler = Executors.newScheduledThreadPool(3);
		
		// ONE worker pool (change to 2 if needed)

		int automateDuration=5;
		scheduler.scheduleAtFixedRate(() -> {

			AutomateDatas automateDatas = new AutomateDatas(automateDuration);
			automateDatas.start();
			System.out.println("AutoMateDatas Started");

		}, 0, automateDuration, TimeUnit.SECONDS);
		
		
		scheduler.scheduleAtFixedRate(() -> {
			
			
			
			long now = System.currentTimeMillis();

			// ---- 1️⃣ Discovery Retry Logic ----
			if (!service.isEmptyUndiscovered()) {

				List<String> retryDevices = service.getCurrentRetryReadyDevice(now);
				if (retryDevices.size() > 0) {
					workerPool1 = Executors.newFixedThreadPool(String.valueOf(retryDevices.size()).length());
					for (String device : retryDevices) {
						workerPool1.execute(() -> {
							service.processDevice(device, now, retryTime);
							publishProbe(device);
						});
					}
					workerPool1.shutdown();
				}
			}

			

		}, 0, 60, TimeUnit.SECONDS);
		
		
		scheduler.scheduleAtFixedRate(() -> {

			List<String> discoveredDevices = service.getDiscoveryDeviceFilter(dataDuration);
			if(discoveredDevices.size()>0) {
				workerPool2 = Executors.newFixedThreadPool(String.valueOf(discoveredDevices.size()).length());
			for (String device : discoveredDevices) {
				workerPool2.execute(() -> publishRealTime(device));
			}
			workerPool2.shutdown();
			}
		}, 0, 15, TimeUnit.SECONDS);
		
		

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
			InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");

			props.load(input);

			dataDuration = Long.parseLong(props.getProperty("dataSchedulerDuration"));

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
		if(workerPool1!=null)
		workerPool1.shutdown();
		if(workerPool2!=null)
		workerPool2.shutdown();
	}
}
