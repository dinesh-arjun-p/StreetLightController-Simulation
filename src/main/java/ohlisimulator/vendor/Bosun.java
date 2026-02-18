package ohlisimulator.vendor;

import ohlisimulator.serverside.*;
import ohlisimulator.random.*;
import ohlisimulator.controller.*;

import org.json.JSONObject;


import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Bosun extends Vendor {
	MqttMessageListener listener=MqttMessageListener.getListener();

	RequestProcessor req = new RequestProcessor(this);

	public boolean deviceGenerated(int deviceSerialNumberStart, int batteryCapacity, int batteryVoltage) {
		String deviceId=String.valueOf(deviceSerialNumberStart);
		Location loc = new Location();
		JSONObject device = new JSONObject();
		device.put("CMD", "0");
		device.put("TYPE", "1");
		device.put("M", "1");
		device.put("W", "1");
		device.put("X", String.valueOf(loc.generateRandomLatitude()));
		device.put("Y", String.valueOf(loc.generateRandomLongitude()));
		device.put("C", "123123");
		device.put("E", "123123");
		device.put("R", "-43");
		device.put("T", "3233");
		device.put("N", "3200");

		boolean register = registerDevice(deviceSerialNumberStart, device);
		if (register) {

			return req.registerDevice(deviceId, device);
		}
		return false;
	}

	public boolean registerDevice(int deviceSerialNumberStart, JSONObject device) {
		String deviceId=String.valueOf(deviceSerialNumberStart);
		try {
			listener.publishMessage(device, deviceId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public void publishControllerInfo(String topic,String cmd,String... info) {
		JSONObject device = new JSONObject();
		device.put("CMD", cmd);
		device.put("TYPE", info[0]);
		device.put("M", info[1]);
		device.put("W", info[2]);
		device.put("X", info[3]);
		device.put("Y", info[4]);
		device.put("C", info[5]);
		device.put("E", info[6]);
		device.put("R", info[7]);
		device.put("T", info[8]);
		device.put("N", info[9]);
		try {
			String parts[]=topic.split("/");
			System.out.println("From Bosun Topic:"+topic);
			listener.publishMessage(device, parts[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void messageArrived(String topic, MqttMessage mqttmsg) {
		String message = new String(mqttmsg.getPayload());
		JSONObject msg = new JSONObject(message);
		if (msg.getString("CMD").equals("0")) {
			req.registration(topic, msg);
		}
		if (msg.getString("CMD").equals("1")) {
			req.obtainControllerInfo(topic);
		}
		if (msg.getString("CMD").equals("4")) {
			System.out.println("CMD0");
			try {
				listener.closeClient();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
}
