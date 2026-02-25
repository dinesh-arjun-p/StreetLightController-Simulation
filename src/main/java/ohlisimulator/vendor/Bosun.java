package ohlisimulator.vendor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import ohlisimulator.controller.RequestProcessor;
import ohlisimulator.random.Location;
import ohlisimulator.serverside.MqttMessageListener;

public class Bosun extends Vendor {

	public enum Register {
		LOAD_OR_CHANGE("00FD"), FAULT_CODE_1("00FE"), FAULT_CODE_2("00FF"), BAT_CAP_SOC("0100"), BAT_U_100MV("0101"),
		BAT_I_10MA("0102"), DEVICE_TEMP("0103"), LED_U_100MV("0104"), LED_I_10MA("0105"), LED_POWER_1W("0106"),
		PV_U_100MV("0107"), PV_I_10MA("0108"), CHARGE_POWER_1W("0109"), DC_ON_OFF("010A"),
		MIN_BAT_U_IN_DAY_100MV("010B"), MAX_BAT_U_IN_DAY_100MV("010C"), MAX_CHARGE_BAT_I_IN_DAY_10MA("010D"),
		MAX_DISCHARGE_BAT_I_IN_DAY_10MA("010E"), MAX_CHARGE_POWER_IN_DAY_1W("010F"),
		MAX_DISCHARGE_POWER_IN_DAY_1W("0110"), CHARGE_AH_IN_DAY("0111"), DISCHARGE_AH_IN_DAY("0112"),
		CHARGE_WH_IN_DAY("0113"), DISCHARGE_WH_IN_DAY("0114"), DAYS_TOTAL("0115"), BAT_OVER_DISC_TIMES("0116"),
		BAT_OVER_CHARGE_TIMES("0117"), CHARGE_AH_TOTAL_H16("0118"), CHARGE_AH_TOTAL_L16("0119"),
		DISCHARGE_AH_TOTAL_H16("011A"), DISCHARGE_AH_TOTAL_L16("011B"), CHARGE_WH_TOTAL_H16("011C"),
		CHARGE_WH_TOTAL_L16("011D"), DISCHARGE_WH_TOTAL_H16("011E"), DISCHARGE_WH_TOTAL_L16("011F"), WORK_STATE("0120"),
		FAULT_CODE_A_1("0121"), FAULT_CODE_A_2("0122"), MAX_DEVICE_TEMP("0123"), MIN_DEVICE_TEMP("0124"),
		LOAD_WORK_TIME_TOTAL_H16("0125"), LOAD_WORK_TIME_TOTAL_L16("0126"), YOUREN_TIME_IN_DAY("0127"),
		WUREN_TIME_IN_DAY("0128"), LIGHT_INDEX("0129"), POWER_INDEX("012A"), SYS_INDEX("012B"), DAY_LENGHT_1S("012C"),
		NIGHT_LENGHT_1S("012D");

		private final String address;

		Register(String address) {
			this.address = address;
		}

		public String getAddress() {
			return address;
		}
	}

	public enum Multiplier {
		LOAD_OR_CHANGE(1), FAULT_CODE_1(1), FAULT_CODE_2(1), BAT_CAP_SOC(1), BAT_U_100MV(10), BAT_I_10MA(100),
		DEVICE_TEMP(1), LED_U_100MV(10), LED_I_10MA(10), LED_POWER_1W(1), PV_U_100MV(10), PV_I_10MA(0.1),
		CHARGE_POWER_1W(1), DC_ON_OFF(1), MIN_BAT_U_IN_DAY_100MV(10), MAX_BAT_U_IN_DAY_100MV(10),
		MAX_CHARGE_BAT_I_IN_DAY_10MA(0.1), MAX_DISCHARGE_BAT_I_IN_DAY_10MA(10), MAX_CHARGE_POWER_IN_DAY_1W(10),
		MAX_DISCHARGE_POWER_IN_DAY_1W(10), CHARGE_AH_IN_DAY(10), DISCHARGE_AH_IN_DAY(10), CHARGE_WH_IN_DAY(100),
		DISCHARGE_WH_IN_DAY(100), DAYS_TOTAL(10), BAT_OVER_DISC_TIMES(10), BAT_OVER_CHARGE_TIMES(10),
		CHARGE_AH_TOTAL(1), DISCHARGE_AH_TOTAL(1), CHARGE_WH_TOTAL(1), DISCHARGE_WH_TOTAL(1), WORK_STATE(1),
		FAULT_CODE_A_1(1), FAULT_CODE_A_2(1), MAX_DEVICE_TEMP(1), MIN_DEVICE_TEMP(1), LOAD_WORK_TIME_TOTAL(1),
		YOUREN_TIME_IN_DAY(1), WUREN_TIME_IN_DAY(1), LIGHT_INDEX(1), POWER_INDEX(1), SYS_INDEX(1), DAY_LENGHT_1S(1),
		NIGHT_LENGHT_1S(1);

		private final double address;

		Multiplier(double address) {
			this.address = address;
		}

		public double getAddress() {
			return address;
		}
	}

	MqttMessageListener listener = MqttMessageListener.getListener();

	RequestProcessor req = new RequestProcessor(this);
	static Map<String, String> fieldToRegister;
	static Map<String, String> registerToField;

	static {
		loadFieldToRegister();
		loadRegisterToField();
	}

	private static void loadFieldToRegister() {
		fieldToRegister = Arrays.stream(Register.values())
				.collect(Collectors.toMap(Register::name, Register::getAddress));

	}

	public static void loadRegisterToField() {
		registerToField = Arrays.stream(Register.values())
				.collect(Collectors.toMap(Register::getAddress, Register::name));

	}

	public boolean deviceGenerated(int deviceSerialNumberStart, long batteryCapacity, int batteryVoltage) {
		String deviceId = String.valueOf(deviceSerialNumberStart);
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

			return req.registerDevice(deviceId, device,batteryCapacity,batteryVoltage);
		}
		return false;
	}

	public boolean registerDevice(int deviceSerialNumberStart, JSONObject device) {
		String deviceId = String.valueOf(deviceSerialNumberStart);
		try {
			listener.publishMessage(device, deviceId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public void publishControllerInfo(String topic, String cmd, String... info) {
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
			String parts[] = topic.split("/");
			System.out.println("From Bosun Topic:" + topic);
			listener.publishMessage(device, parts[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void messageArrived(String topic, MqttMessage mqttmsg) {
		String[] parts = topic.split("/");
		String deviceId = parts[1];
		String message = new String(mqttmsg.getPayload());
		JSONObject msg = new JSONObject(message);
		if (msg.getString("CMD").equals("0")) {
			req.registration(topic, msg);
		}
		if (msg.getString("CMD").equals("1")) {
			req.obtainControllerInfo(topic);
		}
		if (msg.getString("CMD").equals("2")) {
			parseDData(deviceId, msg);
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

	private void parseDData(String deviceId, JSONObject msg) {
		boolean crcCheck = true;
		String recieveD = msg.getString("D");
		String crc = recieveD.substring(recieveD.length() - 4);
		if (crc == getCrcCheck(new StringBuffer(recieveD)))
			;
		crcCheck = true;
		if (crcCheck) {
			if (recieveD.substring(2, 4).equals("09")) {
				publishRequestedData(deviceId, recieveD);
			}
		}
	}

	private void publishRequestedData(String deviceId, String recieveD) {
		String device = "device/" + deviceId;
		StringBuffer publishd = new StringBuffer("0103");
		String startingRegister = recieveD.substring(4, 8);
		String lenHex = recieveD.substring(8, 12);
		int len = Integer.parseInt(lenHex, 16);
		publishd.append(startingRegister).append(String.format("%02X", len));
		int register = Integer.parseInt(startingRegister, 16);
		for (int i = register; i < register + len; i++) {
			System.out.println(String.format("%04X", i));
			String fieldValueInHex = getFieldValueInHex(i, device);
			publishd.append(fieldValueInHex);
			if (fieldValueInHex.length() == 8)
				i++;
		}
		publishd.append(getCrcCheck(publishd));
		String publishD = publishd.toString();
		JSONObject response = genericResponse(publishD);
		try {
			listener.publishMessage(response, deviceId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void publishRealTimeMetrics(String deviceId) {
		String device = "device/" + deviceId;

		String publishD = "0103";
		StringBuffer publishd = new StringBuffer();

		String startingRegister = "00FD";
		int len = 49;
		publishd.append(publishD).append(startingRegister).append(String.format("%02X", len));
		int register = Integer.parseInt(startingRegister, 16);
		for (int i = register; i < register + len; i++) {
			//System.out.println(String.format("%04X", i));
			String fieldValueInHex = getFieldValueInHex(i, device);
			publishd.append(fieldValueInHex);
			if (fieldValueInHex.length() == 8)
				i++;
		}
		publishd.append(getCrcCheck(publishd));
		publishD = publishd.toString();
		JSONObject realTimeMetrics = createJsonforReatTimeMetrics(publishD);
		try {
			listener.publishMessage(realTimeMetrics, deviceId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getCrcCheck(StringBuffer d) {
		byte[] bytes = hexToBytes(d.toString());
		int crc = calculateCRC(bytes);

		int low = crc & 0xFF;
		int high = (crc >> 8) & 0xFF;

		return String.format("%02X%02X", low, high);
	}

	public byte[] hexToBytes(String hex) {
		int len = hex.length();
		byte[] data = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
		}
		return data;
	}

	public int calculateCRC(byte[] data) {
		int crc = 0xFFFF;

		for (byte b : data) {
			crc ^= (b & 0xFF);

			for (int i = 0; i < 8; i++) {
				if ((crc & 0x0001) != 0) {
					crc = (crc >> 1) ^ 0xA001;
				} else {
					crc = crc >> 1;
				}
			}
		}
		return crc & 0xFFFF;
	}

	private String getFieldValueInHex(int i, String device) {
		String field;
		try {
		field = registerToField.get(String.format("%04X", i));
		}
		catch(Exception e ) {
			field="0000";
		}
		if (i == 280 || i == 282 || i == 284 || i == 286 || i == 293) {

			field = field.substring(0, field.length() - 4);
		}
		double fieldValue = req.getFieldValue(device, field);
		if(field!=null)
			
		fieldValue = fieldValue * Multiplier.valueOf(field).getAddress();
		if (i == 280 || i == 282 || i == 284 || i == 286 || i == 293) {
			i++;
			return String.format("%08X", (int) fieldValue);

		} else {
			return String.format("%04X", (int) fieldValue);
		}
	}

	private JSONObject createJsonforReatTimeMetrics(String D) {
		JSONObject device = genericResponse(D);

		device.put("R", "-53");
		device.put("T", "1212");
		device.put("N", "3200");
		device.put("SZ-X", "1111");
		device.put("SZ-Y", "2222");
		device.put("SZ-Z", "3333");
		return device;
	}

	JSONObject genericResponse(String D) {
		JSONObject device = new JSONObject();
		device.put("CMD", "2");
		device.put("D", D);
		return device;
	}

}
