package ohlisimulator.service;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.json.JSONObject;

import ohlisimulator.controller.AutomateDatas;
import ohlisimulator.controller.RequestProcessor;
import ohlisimulator.dao.Dao;
import ohlisimulator.dao.DragonFly;
import ohlisimulator.dao.DragonFlyServiceRegisters;

public class Service {
	RequestProcessor req;
	List<Long> duration=new ArrayList<>();
	Properties props;
	private void loadConfig(){
		 props= new Properties();
		InputStream input = Service.class.getClassLoader().getResourceAsStream("config.properties");
		try {
			props.load(input);
			duration.add(Long.parseLong(props.getProperty("probeRetryDuration1")));
			duration.add(Long.parseLong(props.getProperty("probeRetryDuration2")));
			duration.add(Long.parseLong(props.getProperty("probeRetryDuration3")));
			duration.add(Long.parseLong(props.getProperty("probeRetryDuration4")));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	private  void putValue(Map<String, String> map,
            String key,
            Object value) {
		try {
		map.put(DragonFlyServiceRegisters.serviceToDao.get(key), String.valueOf(value));
		}catch(IllegalArgumentException e) {
			System.out.println("IllegalArgumentException:"+key);
		}
	}
	
	
	public Service(RequestProcessor req) {
		this.req = req;
	}

	public Service() {

	}

	Dao dao = new DragonFly();

	public boolean registerDevice(String SerialNumber, JSONObject device,long batteryCapacity, int batteryVoltage) {
		loadConfig();
		String X = device.getString("X");
		String Y = device.getString("Y");
		long time=System.currentTimeMillis();
		return dao.registerDevice(SerialNumber, X, Y, time,duration,batteryCapacity, batteryVoltage);
	}

	public void registrationSuccess(String topic) {
		System.out.println("From Service Layer Topic"+topic);
		String[] parts = topic.split("/");
		String deviceId = parts[1];
		if(deviceId.equals("all"))
			dao.markAllDiscovered();
		else 
			dao.markDiscovered(deviceId);
	}

	public void getControllerInfo(String topic) {

		req.publishControllerInfo(topic, "1", obtainControllerInfo(topic));
	}

	public String[] obtainControllerInfo(String device) {
		String topic ="device/"+device;
		String x = dao.getLatitude(topic);
		String y = dao.getLongitude(topic);
		return new String[] { "1", "1", "1", x, y, "123123", "123123", "-43", "3233", "3200" };
	}

	public boolean isEmptyUndiscovered() {
		System.out.println("UndiscoveredDevices:" + dao.getIntUndiscovered());
		if (dao.getIntUndiscovered() == 0)
			return true;
		return false;
	}

	public List<String> getCurrentRetryReadyDevice(long now) {
		return dao.getCurrentRetryReadyDevice(now);
	}
	
	public void processDevice(String device, long now,List<Long> time1) {

	    long createdTime = (long)dao.getCreatedTime(device);
	    long age = now - createdTime;

	    for(int i=0;i<time1.size();i++) {
			if(age<=time1.get(i)) {
				System.out.println("Coming Under Time:"+time1.get(i));
				updateRetry(device,i);
				break;
			}
		}


	}

	private void updateRetry(String device, int i) {
		loadConfig();
		System.out.println("Going to add Duration"+duration.get(i));
		dao.nextRetry(device,duration.get(i));
	}

	public double getFieldValue(String deviceId, String serviceField) {
		String device="device/"+deviceId;
		String field=DragonFlyServiceRegisters.serviceToDao.get(serviceField);
		return dao.getFieldValue(device,field);
	}

	public List<String> getDiscoveryDeviceFilter(long duration) {
		
		return dao.getDiscoveryDeviceFilter(duration);
	}

	public List<String> getAllDevice() {
		return dao.getAllDevices();
	}

	public int getLOADORCHANGE(String deviceId) {
		String device="device/"+deviceId;
		return dao.getLOADORCHANGE(device);
	}

	public void dishargeWithoutConnection(String deviceId) {
		Map<String,String> data=new HashMap<>();
		String device="device/"+deviceId;
		long batteryCurrentCapacity=dao.getBatCurEnergy(device);
		Random random = new Random();
		//In milliAmpere
		int number = random.nextInt(50+1) + 30;
		//Battery Current Capactity is stored in milli Amperes
		batteryCurrentCapacity-=(number* AutomateDatas.duration);
		System.out.println("Battery Current in dishargeWithoutConnection:"+number);
		long batteryCapacity=dao.getBatCapEnergy(device);
		//dao.setBatCurEnergy(device, batteryCurrentCapacity);
		putValue(data,"batteryCurrentCapacity",batteryCurrentCapacity);
		updateBatVoltage(device,batteryCurrentCapacity,batteryCapacity);
		
		//dao.setBatI10ma(device,number/1000.0);
		putValue(data,"batteryCurrent",number/1000.0);
		System.out.println("Battery Current:"+number/1000.0);
		dao.setUpdate(device, data);
	}
	
	public void dishargeWithConnection(String deviceId) {
		Map<String,String> data=new HashMap<>();
		String device="device/"+deviceId;
		double ledPower=dao.getLedPowerIn(device);
		double batVoltage=dao.getBatU100mv(device);
		double batCurrent=ledPower/batVoltage;
		System.out.println("Battery Power in dishargeWithConnection:"+ledPower);
		System.out.println("Battery Voltage in dishargeWithConnection:"+batVoltage);
		System.out.println("Battery Current in dishargeWithConnection:"+batCurrent);
		//dao.setBatI10ma(device, batCurrent);
		putValue(data, "batteryCurrent", batCurrent);
		long batteryCurrentCapacity=dao.getBatCurEnergy(device);
		batteryCurrentCapacity-=(batCurrent*1000* AutomateDatas.duration);
		long batteryCapacity=dao.getBatCapEnergy(device);
		System.out.println("Battery Current Capacity:"+batteryCurrentCapacity);
		System.out.println("Battery Total Capacity:"+batteryCapacity);
		putValue(data,"batteryCurrentCapacity",batteryCurrentCapacity);
		updateBatVoltage(device,batteryCurrentCapacity,batteryCapacity);
		dao.setUpdate(device, data);
		
	}

	private void updateBatVoltage(String device, long batteryCurrentCapacity, long batteryCapacity) {
		Map<String,String> data=new HashMap<>();
		loadConfig();
		double batteryFullChargeVoltage=0,batteryEmptyChargeVoltage=0,batteryRechargeVoltage=0;
		int batteryVoltage=0;
		try {
			batteryVoltage=Integer.parseInt(props.getProperty("batteryVoltage"));
			if(batteryVoltage==12) {
				batteryFullChargeVoltage=Double.parseDouble(props.getProperty("BatteryFullChargeVoltage1"));
				batteryEmptyChargeVoltage=Double.parseDouble(props.getProperty("BatteryEmptyChargeVoltage1"));
				batteryRechargeVoltage=Double.parseDouble(props.getProperty("batteryRechargeVoltage1"));
			}
			if(batteryVoltage==24) {
				batteryFullChargeVoltage=Double.parseDouble(props.getProperty("BatteryFullChargeVoltage2"));
				batteryEmptyChargeVoltage=Double.parseDouble(props.getProperty("BatteryEmptyChargeVoltage2"));
				batteryRechargeVoltage=Double.parseDouble(props.getProperty("batteryRechargeVoltage2"));
			}
		}
		catch(Exception e) {
			
		}
		System.out.println("Battery Voltage:"+batteryVoltage);
		System.out.println("batteryFullChargeVoltage:"+batteryFullChargeVoltage);
		System.out.println("batteryEmptyChargeVoltage:"+batteryEmptyChargeVoltage);
		double batVoltage=batteryEmptyChargeVoltage+
				((batteryCurrentCapacity/(double)batteryCapacity)*
				(batteryFullChargeVoltage-batteryEmptyChargeVoltage));
		System.out.println("Battery Voltage :"+batVoltage);
//		dao.setBatU100mv(device,batVoltage);
		putValue(data,"batteryVoltage",batVoltage);
		if(batVoltage<=batteryRechargeVoltage) {
			System.out.println("Changing Load or Change");
			//dao.setLoadOrChange(device,1);
			putValue(data,"loadOrChange",1);
			
			//ariseFault
		}
		if(batVoltage>=batteryFullChargeVoltage) {
			System.out.println("Changing Load or Change");
//			dao.setLoadOrChange(device,0);
			putValue(data,"loadOrChange",0);
		}
//		dao.setBatCapSoc(device, (int)(((double)batteryCurrentCapacity/batteryCapacity)*100));
		putValue(data,"batteryPercentage",(int)(((double)batteryCurrentCapacity/batteryCapacity)*100));
		dao.setUpdate(device, data);
	}
	

	public double getLongitude(String deviceId) {
		String device="device/"+deviceId;
		return Double.parseDouble(dao.getLongitude(device));
	}
	
	public double getLatitude(String deviceId) {
		String device="device/"+deviceId;
		return Double.parseDouble(dao.getLatitude(device));
	}

	public void setPanel(String deviceId,double panelVoltage, double panelCurrent, double panelPower,int day) {
		String device="device/"+deviceId;
		Map<String,String> data=new HashMap<>();
		putValue(data,"panelVoltage",panelVoltage);
//		dao.setPvU100mv(device,panelVoltage);
		putValue(data,"panelCurrent",panelCurrent);
//		dao.setPvI10ma(device,panelCurrent);
		putValue(data,"panelPower",panelPower);
//		dao.setChargePower1w(device,panelPower);
		double temp=generatePanelTemp(device,day);
		putValue(data,"panelTemp",temp);
		dao.setUpdate(device, data);
		generatePanelTemp(device,day);
		
	}

	private double generatePanelTemp(String device,int day) {
		double temp=dao.getPanelTemp(device);
		if(day==0) {
			if(temp>25)
				return temp-0.1;
			else
				return temp-0.01;
		}
		else {
			if(temp<30)
				return temp+0.1;
			else
				return temp+0.01;
		}
	}

	public double getPanelTemp(String deviceId) {
		String device="device/"+deviceId;
		return dao.getPanelTemp(device);
	}

	public void updateMinimumBatteryVoltageDuringNight(String deviceId) {
		String device="device/"+deviceId;
		double minVoltage=dao.getMinBatUInDay100mv(device);
		double curVoltage=dao.getBatU100mv(device);
		if(curVoltage<minVoltage)
				dao.setMinBatUInDay100mv(device,curVoltage);
		
	}

	public void updateMaximumBatteryVoltageDuringDay(String deviceId) {
		String device="device/"+deviceId;
		double maxVoltage=dao.getMaxBatUInDay100mv(device);
		double curVoltage=dao.getBatU100mv(device);
		if(curVoltage>maxVoltage)
				dao.setMaxBatUInDay100mv(device,curVoltage);
		
	}

	public void updateMaximumCurrentOfDay(String deviceId) {
		String device="device/"+deviceId;
		double curCurrent=dao.getBatI10ma(device);
		if(dao.getLOADORCHANGE(device)==1) {
			double maxChargingCurrent=dao.getMaxChargeBatIInDay10ma(device);
			if(curCurrent>maxChargingCurrent)
				dao.setMaxChargeBatIInDay10ma(device,curCurrent);
		}
		else {
			double maxDischargingCurrent=dao.getMaxDischargeBatIInDay10ma(device);
			if(curCurrent>maxDischargingCurrent)
				dao.setMaxDischargeBatIInDay10ma(device,curCurrent);
		}
		
	}
	
	public void updateMaximumPowerOfDay(String deviceId) {
		String device="device/"+deviceId;
		double curPower=dao.getBatI10ma(device);
		if(dao.getLOADORCHANGE(device)==1) {
			double maxChargingPower=dao.getMaxChargePowerInDay1w(device);
			if(curPower>maxChargingPower)
				dao.setMaxChargePowerInDay1w(device,curPower);
		}
		else {
			double maxDischargingPower=dao.getMaxDischargePowerInDay1w(device);
			if(curPower>maxDischargingPower)
				dao.setMaxDischargePowerInDay1w(device,curPower);
		}
		
	}

	public void updateDailyChargingPower(String deviceId) {
		String device="device/"+deviceId;
		Map<String,String> data=new HashMap<>();
		double currentDailyCharge=dao.getChargeWhInDay(device);
		double historyDailyCharge=dao.getChargeWhTotal(device);
		double currentCharge=dao.getChargePower1w(device);
		currentDailyCharge+=((currentCharge*  AutomateDatas.duration)/3600);
		historyDailyCharge+=((currentCharge*  AutomateDatas.duration)/3600);
		putValue(data,"DailyChargingWh",currentDailyCharge);
		//dao.setChargeWhInDay(device, currentDailyCharge);
		putValue(data,"HistoricalCumulativeChargingWh",historyDailyCharge);
//		dao.setChargeWhTotal(device,historyDailyCharge);
		dao.setUpdate(device, data);
	}

	
	public void updateDailyDischargingPower(String deviceId) {
		String device="device/"+deviceId;
		Map<String,String> data=new HashMap<>();
		double currentDailyDischarge=dao.getDischargeWhInDay(device);
		double historyDailyDischarge=dao.getDischargeWhTotal(device);
		double currentCharge=dao.getChargePower1w(device);
		System.out.println("Current Charge from updateDailyDischargingPower Service:"+currentCharge);
		currentDailyDischarge+=((currentCharge* AutomateDatas.duration)/3600);
		historyDailyDischarge+=((currentCharge* AutomateDatas.duration)/3600);
//		dao.setDischargeWhInDay(device, currentDailyDischarge);
//		dao.setDischargeWhTotal(device,historyDailyDischarge);
		putValue(data,"DailyDischargingWh",currentDailyDischarge);
		putValue(data,"HistoricalCumulativeDischargingWh",historyDailyDischarge);
		dao.setUpdate(device, data);
		
	}
	
	public void resetDailyCharingAndDischarging(String deviceId) {
		String device="device/"+deviceId;
		Map<String,String> data=new HashMap<>();
//		dao.setChargeWhInDay(device,0);
//		dao.setDischargeWhInDay(device,0);
		putValue(data,"DailyChargingWh",0);
		putValue(data,"DailyDischargingWh",0);
		dao.setUpdate(device, data);
		
	}

	public void incrementWorkingDay(String deviceId) {
		String device="device/"+deviceId;
		int days=dao.getDaysTotal(device);
		dao.setDaysTotal(device,days+1);
	}

	public void updateDailyDischargingCurrent(String deviceId) {
		String device="device/"+deviceId;
		double historyDailyDischarge=dao.getDischargeAhTotal(device);
		double currentCurrent=dao.getBatI10ma(device);
		historyDailyDischarge+=((currentCurrent* AutomateDatas.duration)/3600);
		dao.setDischargeAhTotal(device,historyDailyDischarge);
		
	}
	
	public void updateDailyChargingCurrent(String deviceId) {
		String device="device/"+deviceId;
		double historyDailyCharge=dao.getChargeAhTotal(device);
		double currentCurrent=dao.getBatI10ma(device);
		historyDailyCharge+=((currentCurrent* AutomateDatas.duration)/3600);
		dao.setDischargeAhTotal(device,historyDailyCharge);
		
	}

	public void setDayLengthIs(String deviceId, long seconds) {
		String device="device/"+deviceId;
		dao.setDayLengthIs(device,seconds);
		
	}

	public void setNightLengthIs(String deviceId, long seconds) {
		String device="device/"+deviceId;
		dao.setNightLengthIs(device,seconds);
	}

	public void setSunRise(String device, ZonedDateTime sunRise) {
		dao.setSunRise(device,sunRise.toString());
		
	}

	public void setSunSet(String device, ZonedDateTime sunSet) {
		dao.setSunSet(device,sunSet.toString());
	}

	public void chargeFromPanel(String deviceId, double panelPowerLocal) {
		String device="device/"+deviceId;
		double batVoltage=dao.getBatU100mv(device);
		double batCurrent=panelPowerLocal/batVoltage;
		long batCurCap=dao.getBatCurEnergy(device);
		long batCap=dao.getBatCapEnergy(device);
		long cur=(long)(batCurrent*1000);
		batCurCap+=(cur* AutomateDatas.duration);
		if(batCurCap>=batCap) {
			batCurCap=batCap;
			//Arise Fault
		}
		dao.setBatCurEnergy(device,batCurCap);
		
		updateBatVoltage(device, batCurCap, batCap);
		
	}




	public void setLampLevel(String deviceId, int level) {
		String device="device/"+deviceId;
		dao.setLedLevel(device,level);
		double maxCurrent=dao.getLedCurrent(device);
		double ledCurrent=(maxCurrent*level)/100;
		System.out.println("Led Current From setLampLevel:"+ledCurrent);
		double ledVoltage=dao.getLedU100mv(device);
		double ledPower=ledVoltage*ledCurrent;
		Map<String,String> data=new HashMap<>();
		putValue(data,"loadCurrent",ledCurrent);
		putValue(data,"loadPower",ledPower);
		dao.setUpdate(device, data);
		
	}




	public void setLOADORCHANGE(String device, int i) {
		dao.setLoadOrChange(device, i);
		
	}




	public void setUpdateField(String deviceId, String field, Object value) {
		String device="device/"+deviceId;
		String dbField=DragonFlyServiceRegisters.serviceToDao.get(field);
		
		dao.setUpdateField(deviceId, field,String.valueOf(value));
		
	}


	public void setUpdateField(String deviceId, int i, Object value) {
		if(i>=57490 && i<=57519)
			scheduleUpdate(deviceId,i,(int)value);
		
	}




	private void scheduleUpdate(String deviceId, int i, int value) {
		if((i-57487)%3==0)
			dao.addTimePeriodSchedule(deviceId,i-57487,value);
		
	}




	




	
	

	
	

	
	

}
