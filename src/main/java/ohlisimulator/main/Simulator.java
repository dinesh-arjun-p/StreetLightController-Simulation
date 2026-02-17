package ohlisimulator.main;
import ohlisimulator.serverside.*;
public class Simulator {
	MqttMessageListener listener;
	Simulator(MqttMessageListener listener){
		this.listener=listener;
	}
	public MqttMessageListener getListener() {
		return listener;
	}
}
