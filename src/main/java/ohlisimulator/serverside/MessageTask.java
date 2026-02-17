package ohlisimulator.serverside;

import java.util.concurrent.RecursiveAction;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import ohlisimulator.vendor.Bosun;
import ohlisimulator.vendor.Vendor;

class MessageTask {
    private final String topic;
    private final MqttMessage msg;
    Vendor vendor;
    public MessageTask(Vendor vendor,String topic, MqttMessage message) {
    	this.vendor=vendor;
        this.topic = topic;
        this.msg = message;
    }

    public void process() {
    	System.out.println("From MessageTask");
    	vendor.messageArrived(topic, msg);
    }
}
