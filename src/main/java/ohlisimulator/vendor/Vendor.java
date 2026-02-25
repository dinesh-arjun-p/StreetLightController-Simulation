package ohlisimulator.vendor;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;


public abstract class Vendor {
	public abstract boolean deviceGenerated(int deviceSerialNumberStart,long batteryCapacity,int batteryVoltage);
	public abstract boolean registerDevice(int deviceSerialNumberStart,JSONObject device) ;
	public abstract void messageArrived(String topic, MqttMessage msg);
	public abstract void publishControllerInfo(String topic,String cmd,String...info) ;
	public abstract void publishRealTimeMetrics(String deviceId) ;
}
