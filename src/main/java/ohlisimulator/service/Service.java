package ohlisimulator.service;

import org.json.JSONObject;
import ohlisimulator.dao.*;
import ohlisimulator.main.Simulator;
import ohlisimulator.controller.*;

public class Service {
	RequestProcessor req;
	public Service(RequestProcessor req){
		this.req=req;
	}
	
	Dao dao=new DragonFly();
	public boolean registerDevice(int SerialNumber,JSONObject device) {
		
		String X=device.getString("X");
		String Y=device.getString("Y");
		
		
		return dao.registerDevice(SerialNumber,X,Y);
	}
	public void registrationSuccess(String topic) {
		System.out.println("From Service Layer");
		dao.updateDiscover(topic,"Yes");
	}
	
	public void obtainGatewayInfo(String topic) {
		String x=dao.getLatitude(topic);
		String y=dao.getLongitude(topic);
		req.publishGatewayInfo(topic,"1","1","1",x,y,"123123","123123",
				"-43","3233","3200");
	}
}
