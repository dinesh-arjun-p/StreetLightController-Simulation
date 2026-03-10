package ohlisimulator.controller;

import ohlisimulator.vendor.*;
import ohlisimulator.service.*;

import java.io.InputStream;
import java.util.Properties;

import org.json.JSONObject;

public class RequestProcessor implements UpdateWorkState {

	Vendor vendor;

	public RequestProcessor(Vendor vendor) {
		this.vendor = vendor;
	}

	Service service = new Service(this);

	public boolean registerDevice(String SerialNumber, JSONObject device, long batteryCapacity, int batteryVoltage) {

		return service.registerDevice(SerialNumber, device, batteryCapacity, batteryVoltage);
	}

	// Register Device Cmd 0
	public void registration(String topic, JSONObject msg) {
		System.out.println("From RequestProcessor" + msg.toString());
		if (msg.getString("STATE").equals("OK")) {
			service.registrationSuccess(topic);
		}
		System.out.println("Dicovered:" + topic);
	}

	// Gateway Info CMD 1
	public void obtainControllerInfo(String topic) {
		service.getControllerInfo(topic);
	}

	public void publishControllerInfo(String topic, String cmd, String... info) {
		vendor.publishControllerInfo(topic, cmd, info);
	}

	public double getFieldValue(String device, String field) {

		return service.getFieldValue(device, field);
	}

	public void updateField(String deviceId, String field, Object value) {

		service.setUpdateField(deviceId, field, value);

	}

	public void updateField(String deviceId, int i, Object value) {

		service.setUpdateField(deviceId, i, value);

	}

	public void riseAlarm(JSONObject msg) {
		int st=1000;
		for(int i=0;i<16;i++) {
			riseAlarm(msg,String.valueOf(i),st);
		}
	}

	
	Properties props;
	int noOfDevice=0;
	private void loadConfig(){
		 props= new Properties();
		InputStream input = Service.class.getClassLoader().getResourceAsStream("config.properties");
		try {
			props.load(input);
			noOfDevice=Integer.parseInt(props.getProperty("noOfDevices"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void riseAlarm(JSONObject msg,String m,int st) {
		String deviceId=String.valueOf(st);
		loadConfig();
		int n;
		try {
			n=Integer.parseInt(msg.getString(m));
			if(n>noOfDevice)
				n=noOfDevice;
			for(int i=0;i<n;i++) {
				if(m.equals("0")) {
					service.riseDeviceTemp(deviceId);
				}
				if(m.equals("1")) {
					service.riseBatteryOverCurrent(deviceId);
				}
				if(m.equals("2")) {
					service.riseOverDischargeVoltage(deviceId);
				}
				if(m.equals("3")) {
					service.riseBatteryOverVoltage(deviceId);
				}
				if(m.equals("4")) {
					service.riseBatteryUnderVoltage(deviceId);
				}
				if(m.equals("9")) {
					service.risePanelUnderVoltage(deviceId);
				}
				if(m.equals("10")) {
					service.risePanelOverVoltage(deviceId);
				}
				if(m.equals("11")) {
					service.riseDayBurner(deviceId);
				}
				if(m.equals("12")) {
					service.riseNightOutage(deviceId);
				}
				updateWorkState(deviceId);
				st++;
				if(st>n)
					st=1000;
			}
			
		}
		catch(Exception e) {	
		}
	}
	
	
	public void compareWorkState(String device,int newWorkState, int workState) {
		if(newWorkState!=workState) {
			System.out.println("New Work State "+newWorkState);
			System.out.println("Work State "+workState);
			service.setWorkState(device,newWorkState);
			vendor.publishWorkState(device,2,newWorkState);
			
		}
	}
	
	 public Service getService() {
	        return service;
	    }
	
	
	

	public void clearFault(String deviceId, JSONObject msg) {
		
	}
}
