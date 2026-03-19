package ohlisimulator.controller;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;

import ohlisimulator.service.Service;
import ohlisimulator.vendor.Vendor;

public class RequestProcessor implements UpdateWorkState {

	Vendor vendor;

	public RequestProcessor(Vendor vendor) {
		this.vendor = vendor;
	}

	Service service = new Service(this);

	public boolean registerDevice(String SerialNumber, JSONObject device,int batteryVoltage) {

		return service.registerDevice(SerialNumber, device,  batteryVoltage);
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

//	public void riseAlarm(JSONObject msg) {
//		
//		loadConfig();
//		int st=deviceSerialNumberStart;
//		try {
//			st=Integer.parseInt(msg.getString("device"));
//		}catch(Exception e) {
//			
//		}
//		for(int i=0;i<16;i++) {
//			st=riseAlarm(msg,String.valueOf(i),st);
//		}
//	}

	
	Properties props;
	int noOfDevice=0;
	int deviceSerialNumberStart;
	private void loadConfig(){
		props= new Properties();
		
		try {
			InputStream input = new FileInputStream("config/config.properties");
			props.load(input);
			noOfDevice=Integer.parseInt(props.getProperty("noOfDevices"));
			deviceSerialNumberStart=Integer.parseInt(props.getProperty("deviceSerialNumberStart"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//	public int riseAlarm(JSONObject msg,String m,int st) {
//		String deviceId;
//		int n;
//		try {
//			
//			n=Integer.parseInt(msg.getString(m));
//			if(n>noOfDevice)
//				n=noOfDevice;
//			System.out.println("No of Alarms for "+m+":"+n);
//			for(int i=0;i<n;i++) {
//				deviceId=String.valueOf(st);
//				if(m.equals("0")) {
//					service.riseDeviceTemp(deviceId);
//				}
//				if(m.equals("1")) {
//					service.riseBatteryOverCurrent(deviceId);
//				}
//				if(m.equals("2")) {
//					service.riseOverDischargeVoltage(deviceId);
//				}
//				if(m.equals("3")) {
//					service.riseBatteryOverVoltage(deviceId);
//				}
//				if(m.equals("4")) {
//					service.riseBatteryUnderVoltage(deviceId);
//				}
//				if(m.equals("9")) {
//					service.risePanelUnderVoltage(deviceId);
//				}
//				if(m.equals("10")) {
//					service.risePanelOverVoltage(deviceId);
//				}
//				if(m.equals("11")) {
//					service.riseDayBurner(deviceId);
//				}
//				if(m.equals("12")) {
//					service.riseNightOutage(deviceId);
//				}
//				updateWorkState(deviceId);
//				st++;
//				if(st>=(deviceSerialNumberStart+noOfDevice))
//					st=deviceSerialNumberStart;
//			}
//			
//		}
//		catch(Exception e) {	
//		}
//		return st;
//	}
	
	
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
	
	
	
//	public void clearFault(String deviceId, JSONObject msg) {
////		Map<String,String> data=new HashMap<>();
//		if(msg.length()>1)
//		for(int i=0;i<16;i++) {
//			clearFault(deviceId,msg,String.valueOf(i));
//			
//		}
//		else
//			clearAllFault(deviceId);
//		
//	}

//	public void clearAllFault(String deviceId) {
//		int workState=service.getWorkState(deviceId);
//		int i=0;
//		
//		while(workState!=0) {
//			int fault=workState&1;
//			if(fault==1) {
//				clearFault(deviceId, String.valueOf(i));
//			}
//			i++;
//			workState>>=1;
//		}
//		updateWorkState(deviceId);
//		
//	}

//	public void clearFault(String deviceId, JSONObject msg,String m) {
//		int n;
//		try {
//			n=Integer.parseInt(msg.getString(m));
////			if(n>noOfDevice)
////				n=noOfDevice;
////			for(int i=0;i<n;i++) {
//				clearFault(deviceId, m);
//				updateWorkState(deviceId);
////			}
//			
//		}
//		catch(Exception e) {	
//		}
//	}

//	void clearFault(String deviceId, String m) {
//		
//		if(m.equals("0")) {
//			service.clearDeviceTemp(deviceId);
//		}
//		if(m.equals("1")) {
//			service.clearBatteryOverCurrent(deviceId);
//		}
//		if(m.equals("2")) {
//			service.clearBatteryVoltageFault(deviceId);
//		}
//		if(m.equals("3")) {
//			service.clearBatteryVoltageFault(deviceId);
//		}
//		if(m.equals("4")) {
//			service.clearBatteryVoltageFault(deviceId);
//		}
//		if(m.equals("9")) {
//			service.clearPanelFault(deviceId);
//		}
//		if(m.equals("10")) {
//			service.clearPanelFault(deviceId);
//		}
//		if(m.equals("11")) {
//			service.clearDayBurnerFault(deviceId);
//		}
//		if(m.equals("12")) {
//			service.clearNightOutageFault(deviceId);
//		}
//		
//	}

//	public void clearAlarm() {
//		List<String>devices=service.getAllDevice();
//		for(String deviceId:devices) {
//			clearAllFault(deviceId);
//		}
//	}

	public void riseDeviceTemp(String deviceId) {
		service.riseDeviceTemp(deviceId);
	}

	public void riseBatteryOverCurrent(String deviceId) {
		service.riseBatteryOverCurrent(deviceId);
	}

	public void riseOverDischargeVoltage(String deviceId) {
		service.riseOverDischargeVoltage(deviceId);
	}

	public void riseBatteryOverVoltage(String deviceId) {
		service.riseBatteryOverVoltage(deviceId);
	}

	public void riseBatteryUnderVoltage(String deviceId) {
		service.riseBatteryUnderVoltage(deviceId);
	}

	public void risePanelUnderVoltage(String deviceId) {
		service.risePanelUnderVoltage(deviceId);
	}

	public void risePanelOverVoltage(String deviceId) {
		service.risePanelOverVoltage(deviceId);
	}

	public void riseDayBurner(String deviceId) {
		service.riseDayBurner(deviceId);
	}

	public void riseNightOutage(String deviceId) {
		service.riseNightOutage(deviceId);
	}

	public List<String> getAllDevice() {
		return service.getAllDevice();
	}

	public int getWorkState(String deviceId) {
		return service.getWorkState(deviceId);
	}

	public void clearDeviceTemp(String deviceId) {
		service.clearDeviceTemp(deviceId);
		
	}

	public void clearBatteryOverCurrent(String deviceId) {
		service.clearBatteryOverCurrent(deviceId);
	}

	public void clearBatteryVoltageFault(String deviceId) {
		service.clearBatteryVoltageFault(deviceId);
	}

	public void clearPanelFault(String deviceId) {
		service.clearPanelFault(deviceId);
	}

	public void clearDayBurnerFault(String deviceId) {
		service.clearDayBurnerFault(deviceId);
	}

	public void clearNightOutageFault(String deviceId) {
		service.clearNightOutageFault(deviceId);
	}
}
