package ohlisimulator.controller;

import ohlisimulator.vendor.*;
import ohlisimulator.main.Simulator;
import ohlisimulator.service.*;
import org.json.JSONObject;

public class RequestProcessor {
	Simulator sim;

	Vendor vendor;
	public RequestProcessor(Vendor vendor){
		this.vendor=vendor;
	}
	Service service=new Service(this);
	public boolean registerDevice(int SerialNumber,JSONObject device) {
		
		return service.registerDevice(SerialNumber,device);
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
	public void obtainGatewayInfo(String topic) {
		service.obtainGatewayInfo(topic);
	}
	public void publishGatewayInfo(String topic ,String...info ) {
		vendor.publishGatewayInfo(topic,info);
	}
}
