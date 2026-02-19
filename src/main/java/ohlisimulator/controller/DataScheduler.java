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
    private ExecutorService workerPool;

    private long dataDuration;
    private final List<Long> retryTime = new ArrayList<>();

    private final ThreadLocal<Bosun> threadBosun =
            ThreadLocal.withInitial(Bosun::new);

    public void start() {

        loadConfig();

        scheduler = Executors.newSingleThreadScheduledExecutor();

        // ONE worker pool (change to 2 if needed)
        workerPool =  Executors.newFixedThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {

            long now = System.currentTimeMillis();

            // ---- 1️⃣ Discovery Retry Logic ----
            if (!service.isEmptyUndiscovered()) {

                List<String> retryDevices =
                        service.getCurrentRetryReadyDevice(now);

                for (String device : retryDevices) {
                    workerPool.execute(() -> {
                        service.processDevice(device, now, retryTime);
                        publishProbe(device);
                    });
                }
            }

            List<String> discoveredDevices =
                    service.getDiscoveryDeviceFilter(dataDuration);

            for (String device : discoveredDevices) {
                workerPool.execute(() ->
                        publishRealTime(device)
                );
            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    private void publishProbe(String device) {

        Vendor vendor = threadBosun.get();

        String topic = "BS_Dev/" + device;

        vendor.publishControllerInfo(
                topic,
                "0",
                service.obtainControllerInfo(device)
        );
    }

    private void publishRealTime(String device) {

        Vendor vendor = threadBosun.get();
        vendor.publishRealTimeMetrics(device);
    }

    private void loadConfig() {

        try {
            Properties props = new Properties();
            InputStream input = getClass()
                    .getClassLoader()
                    .getResourceAsStream("config.properties");

            props.load(input);

            dataDuration = Long.parseLong(
                    props.getProperty("dataSchedulerDuration")
            );

            retryTime.add(convert(props.getProperty("probeRetryTime1")));
            retryTime.add(convert(props.getProperty("probeRetryTime2")));
            retryTime.add(convert(props.getProperty("probeRetryTime3")));
            retryTime.add(convert(props.getProperty("probeRetryTime4")));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long convert(String value) {
        return value.equals("+inf")
                ? Long.MAX_VALUE
                : Long.parseLong(value);
    }

    public void shutdown() {
        scheduler.shutdown();
        workerPool.shutdown();
    }
}

