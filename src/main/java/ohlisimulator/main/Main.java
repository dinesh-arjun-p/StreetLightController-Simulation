package ohlisimulator.main;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import ohlisimulator.controller.DataScheduler;
import ohlisimulator.serverside.MqttMessageListener;

public class Main {

    public static void main(String[] args) throws Exception {
    	
    	
    	 int noOfDevices=0;
         int deviceSerialNumberStart=0;
         int batteryCapacity=0;
         int batteryVoltage =0;
         
         Scanner scan=new Scanner(System.in);
         MqttMessageListener listener=MqttMessageListener.getListener();
         
        Properties props = new Properties();

        InputStream input = Main.class
                .getClassLoader()
                .getResourceAsStream("config.properties");

        props.load(input);
        try {
        noOfDevices = parseInt(props.getProperty("noOfDevices"));
        deviceSerialNumberStart = parseInt(props.getProperty("deviceSerialNumberStart"));
        batteryCapacity = parseInt(props.getProperty("batteryCapacity"));
        batteryVoltage = parseInt(props.getProperty("batteryVoltage"));
        }
        catch(Exception e) {
        	System.out.println("Give Integer Parameters in config.properties ");
        }
        //Creating Devices
        CreateDevice deviceGenerator1=new CreateDevice();
        while(true) {
	        int success=deviceGenerator1.deviceGenerateThread(noOfDevices,deviceSerialNumberStart,
	        		batteryCapacity,batteryVoltage);
	        if(success==0) {
		        System.out.println("Check the connection:");
		        System.out.println("If Checked Press y");
		        if(scan.next().charAt(0)!='y') {
		        	return;
		        }
	        }
	        else if(success!=noOfDevices) {
	        	System.out.println("No of Device Generated:"+success);
	        	System.out.println("Retrying...");
	        	//cleanDatabase
	        }
	        else {
	        	System.out.println("No of Device Generated:"+success);
	        	break;
	        }
        }
        
        
        
        
        DataScheduler datascheduler=new DataScheduler();
        listener.setDataScheduler(datascheduler);
        datascheduler.start();
        
        scan.close();
        
    }
    private static int parseInt(String x) {
    	return Integer.parseInt(x);
    }
}
