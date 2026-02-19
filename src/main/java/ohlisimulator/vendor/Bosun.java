package ohlisimulator.vendor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import ohlisimulator.controller.RequestProcessor;
import ohlisimulator.random.Location;
import ohlisimulator.serverside.MqttMessageListener;

public class Bosun extends Vendor {
	
	public enum Register {
	    LOAD_OR_CHANGE("00FD"),
	    FAULT_CODE_1("00FE"),
	    FAULT_CODE_2("00FF"),
	    BAT_CAP_SOC("0100"),
	    BAT_U_100MV("0101"),
	    BAT_I_10MA("0102"),
	    DEVICE_TEMP("0103"),
	    LED_U_100MV("0104"),
	    LED_I_10MA("0105"),
	    LED_POWER_1W("0106"),
	    PV_U_100MV("0107"),
	    PV_I_10MA("0108"),
	    CHARGE_POWER_1W("0109"),
	    DC_ON_OFF("010A"),
	    MIN_BAT_U_IN_DAY_100MV("010B"),
	    MAX_BAT_U_IN_DAY_100MV("010C"),
	    MAX_CHARGE_BAT_I_IN_DAY_10MA("010D"),
	    MAX_DISCHARGE_BAT_I_IN_DAY_10MA("010E"),
	    MAX_CHARGE_POWER_IN_DAY_1W("010F"),
	    MAX_DISCHARGE_POWER_IN_DAY_1W("0110"),
	    CHARGE_AH_IN_DAY("0111"),
	    DISCHARGE_AH_IN_DAY("0112"),
	    CHARGE_WH_IN_DAY("0113"),
	    DISCHARGE_WH_IN_DAY("0114"),
	    DAYS_TOTAL("0115"),
	    BAT_OVER_DISC_TIMES("0116"),
	    BAT_OVER_CHARGE_TIMES("0117"),
	    CHARGE_AH_TOTAL_H16("0118"),
	    CHARGE_AH_TOTAL_L16("0119"),
	    DISCHARGE_AH_TOTAL_H16("011A"),
	    DISCHARGE_AH_TOTAL_L16("011B"),
	    CHARGE_WH_TOTAL_H16("011C"),
	    CHARGE_WH_TOTAL_L16("011D"),
	    DISCHARGE_WH_TOTAL_H16("011E"),
	    DISCHARGE_WH_TOTAL_L16("011F"),
	    WORK_STATE("0120"),
	    FAULT_CODE_A_1("0121"),
	    FAULT_CODE_A_2("0122"),
	    MAX_DEVICE_TEMP("0123"),
	    MIN_DEVICE_TEMP("0124"),
	    LOAD_WORK_TIME_TOTAL_H16("0125"),
	    LOAD_WORK_TIME_TOTAL_L16("0126"),
	    YOUREN_TIME_IN_DAY("0127"),
	    WUREN_TIME_IN_DAY("0128"),
	    LIGHT_INDEX("0129"),
	    POWER_INDEX("012A"),
	    SYS_INDEX("012B"),
	    DAY_LENGHT_1S("012C"),
	    NIGHT_LENGHT_1S("012D");

	    private final String address;

	    Register(String address) {
	        this.address = address;
	    }

	    public String getAddress() {
	        return address;
	    }
	}

	
	MqttMessageListener listener=MqttMessageListener.getListener();

	RequestProcessor req = new RequestProcessor(this);
	static Map<String,String> fieldToRegister;
	static Map<String,String> registerToField;
	
	public static void loadRegisters() {
		loadFieldToRegister();
		loadRegisterToField();
	}

	private static void loadFieldToRegister() {
		fieldToRegister=Arrays.stream(Register.values())
		        .collect(Collectors.toMap(
			            Register::name,
			            Register::getAddress
			        ));
		
	}
	public static void loadRegisterToField() {
		registerToField = Arrays.stream(Register.values())
		        .collect(Collectors.toMap(
		            Register::getAddress,
		            Register::name
		        ));

	}


	
	
	public boolean deviceGenerated(int deviceSerialNumberStart, int batteryCapacity, int batteryVoltage) {
		String deviceId=String.valueOf(deviceSerialNumberStart);
		Location loc = new Location();
		JSONObject device = new JSONObject();
		device.put("CMD", "0");
		device.put("TYPE", "1");
		device.put("M", "1");
		device.put("W", "1");
		device.put("X", String.valueOf(loc.generateRandomLatitude()));
		device.put("Y", String.valueOf(loc.generateRandomLongitude()));
		device.put("C", "123123");
		device.put("E", "123123");
		device.put("R", "-43");
		device.put("T", "3233");
		device.put("N", "3200");

		boolean register = registerDevice(deviceSerialNumberStart, device);
		if (register) {

			return req.registerDevice(deviceId, device);
		}
		return false;
	}

	public boolean registerDevice(int deviceSerialNumberStart, JSONObject device) {
		String deviceId=String.valueOf(deviceSerialNumberStart);
		try {
			listener.publishMessage(device, deviceId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public void publishControllerInfo(String topic,String cmd,String... info) {
		JSONObject device = new JSONObject();
		device.put("CMD", cmd);
		device.put("TYPE", info[0]);
		device.put("M", info[1]);
		device.put("W", info[2]);
		device.put("X", info[3]);
		device.put("Y", info[4]);
		device.put("C", info[5]);
		device.put("E", info[6]);
		device.put("R", info[7]);
		device.put("T", info[8]);
		device.put("N", info[9]);
		try {
			String parts[]=topic.split("/");
			System.out.println("From Bosun Topic:"+topic);
			listener.publishMessage(device, parts[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void messageArrived(String topic, MqttMessage mqttmsg) {
		String message = new String(mqttmsg.getPayload());
		JSONObject msg = new JSONObject(message);
		if (msg.getString("CMD").equals("0")) {
			req.registration(topic, msg);
		}
		if (msg.getString("CMD").equals("1")) {
			req.obtainControllerInfo(topic);
		}
		if (msg.getString("CMD").equals("4")) {
			System.out.println("CMD0");
			try {
				listener.closeClient();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void publishRealTimeMetrics(String deviceId) {
		String device="device/"+deviceId;
		String D="0103";
		String crc="";
		String startingRegister="00FD";
		int len=48;
		D=D+startingRegister+Integer.toHexString(len).toUpperCase().substring(2);
		String register=startingRegister;
//		for(int i=0;i<len;i++) {
//			String field=registerToField.get(register);
//			double fieldValue=req.getFieldValue(device,field);
//			String fieldValueHex=Integer.toHexString((int)fieldValue*1).toUpperCase();
//		}
		req.obtainControllerInfo(device);
	}

	

	
}
