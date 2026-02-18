package ohlisimulator.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONObject;

import ohlisimulator.controller.RequestProcessor;
import ohlisimulator.dao.Dao;
import ohlisimulator.dao.DragonFly;
import redis.clients.jedis.resps.Tuple;

public class Service {
	RequestProcessor req;
	List<Long> duration=new ArrayList<>();
	{
		Properties props = new Properties();
		InputStream input = Service.class.getClassLoader().getResourceAsStream("config.properties");
		try {
			props.load(input);
			duration.add(Long.parseLong(props.getProperty("probeRetryDuration1")));
			duration.add(Long.parseLong(props.getProperty("probeRetryDuration2")));
			duration.add(Long.parseLong(props.getProperty("probeRetryDuration3")));
			duration.add(Long.parseLong(props.getProperty("probeRetryDuration4")));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Service(RequestProcessor req) {
		this.req = req;
	}

	public Service() {

	}

	Dao dao = new DragonFly();

	public boolean registerDevice(String SerialNumber, JSONObject device) {

		String X = device.getString("X");
		String Y = device.getString("Y");
		long time=System.currentTimeMillis();
		return dao.registerDevice(SerialNumber, X, Y, time,duration);
	}

	public void registrationSuccess(String topic) {
		System.out.println("From Service Layer Topic"+topic);
		String[] parts = topic.split("/");
		String deviceId = parts[1];
		dao.markDiscovered(deviceId);
	}

	public void getControllerInfo(String topic) {

		req.publishControllerInfo(topic, "1", obtainControllerInfo(topic));
	}

	public String[] obtainControllerInfo(String topic) {
		String x = dao.getLatitude(topic);
		String y = dao.getLongitude(topic);
		return new String[] { "1", "1", "1", x, y, "123123", "123123", "-43", "3233", "3200" };
	}

	public boolean isEmptyUndiscovered() {
		System.out.println("UndiscoveredDevices:" + dao.getIntUndiscovered());
		if (dao.getIntUndiscovered() == 0)
			return true;
		return false;
	}

	public List<String> getCurrentRetryReadyDevice(long now,List<Long> time) {
		List<String> devices=dao.getCurrentRetryReadyDevice(now);
		for(String device:devices) {
			long createdTime=(long)dao.getCreatedTime(device);
			System.out.println("Now:"+now);
			System.out.println("Created Time:"+createdTime);
			long age=now-createdTime;
			System.out.println("Age"+age);
			for(int i=0;i<time.size();i++) {
				if(age<=time.get(i)) {
					System.out.println("Coming Under Time:"+time.get(i));
					updateRetry(device,i);
					break;
				}
			}
		}
		return devices;
	}

	private void updateRetry(String device, int i) {
		System.out.println("Going to add Duration"+duration.get(i));
		dao.nextRetry(device,duration.get(i));
	}
	

}
