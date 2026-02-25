package ohlisimulator.controller;

import ohlisimulator.vendor.*;
import ohlisimulator.service.*;
import org.json.JSONObject;

public class RequestProcessor {

	Vendor vendor;
	public RequestProcessor(Vendor vendor){
		this.vendor=vendor;
	}
	Service service=new Service(this);
	public boolean registerDevice(String SerialNumber,JSONObject device,long batteryCapacity, int batteryVoltage) {
		
		return service.registerDevice(SerialNumber,device,batteryCapacity, batteryVoltage);
	}
	
	//Register Device Cmd 0
	public void registration(String topic ,JSONObject msg) {
		System.out.println("From RequestProcessor"+msg.toString());
		if(msg.getString("STATE").equals("OK")) {
			service.registrationSuccess(topic);
		}
		System.out.println("Dicovered:"+topic);
	}
	
	
	//Gateway Info CMD 1
	public void obtainControllerInfo(String topic) {
		service.getControllerInfo(topic);
	}
	public void publishControllerInfo(String topic,String cmd,String...info ) {
		vendor.publishControllerInfo(topic,cmd,info);
	}

	public double getFieldValue(String device,String field) {
		
		return service.getFieldValue(device,field);
	}
}
