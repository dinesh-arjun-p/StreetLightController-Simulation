package ohlisimulator.vendor;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import ohlisimulator.serverside.*;

public abstract class Vendor {
	public abstract boolean deviceGenerated(int deviceSerialNumberStart,int batteryCapacity,int batteryVoltage);
	public abstract boolean registerDevice(int deviceSerialNumberStart,JSONObject device) ;
	public abstract void messageArrived(String topic, MqttMessage msg);
	public abstract void publishGatewayInfo(String topic,String...info) ;
}
