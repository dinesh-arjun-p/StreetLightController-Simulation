package ohlisimulator.dao;

import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ohlisimulator.main.SunRiseSunSetCalc;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

public class DragonFly extends Dao {

	private static final JedisPool pool = new JedisPool("localhost", 6379);
	Properties props;
	InputStream input;
	int batteryVoltage;
	double batteryFullChargeVoltage = 0;
	double batteryEmptyChargeVoltage = 0;
	double batteryOverChargeVoltage=0;
	double batteryOverDischargeVoltage=0;
	double batteryOverChargeReturnVoltage=0;
	double batteryOverDischargeReturnVoltage=0;
	double panelOverVoltage=0;
	double panelUnderVoltage=0;
	double overTemperature=0;
	double batteryOverCurrent=0;
	double ledRatedPower = 0;
	double ledRatedVoltage = 0;
	long dataSchedulerDuration=0;
	static String region;

	private void loadConfig(){
		try {
			Properties props = new Properties();
			InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");

			props.load(input);

			batteryVoltage = Integer.parseInt(props.getProperty("batteryVoltage"));

			if ((batteryVoltage == 12)) {
				batteryFullChargeVoltage = Double.parseDouble(props.getProperty("BatteryFullChargeVoltage1"));
				batteryEmptyChargeVoltage = Double.parseDouble(props.getProperty("BatteryEmptyChargeVoltage1"));
				batteryOverChargeVoltage= Double.parseDouble(props.getProperty("BatteryOverChargeVoltage1"));
				batteryOverDischargeVoltage= Double.parseDouble(props.getProperty("BatteryOverDischargeVoltage1"));
				batteryOverChargeReturnVoltage= Double.parseDouble(props.getProperty("BatteryOverChargeReturnVoltage1"));
				batteryOverDischargeReturnVoltage= Double.parseDouble(props.getProperty("BatteryOverDischargeReturnVoltage1"));
				panelOverVoltage=Double.parseDouble(props.getProperty("PanelOverVoltage1"));
				panelUnderVoltage=Double.parseDouble(props.getProperty("PanelUnderVoltage1"));
			}
			if ((batteryVoltage == 24)) {
				batteryFullChargeVoltage = Double.parseDouble(props.getProperty("BatteryFullChargeVoltage2"));
				batteryEmptyChargeVoltage = Double.parseDouble(props.getProperty("BatteryEmptyChargeVoltage2"));
				batteryOverChargeVoltage= Double.parseDouble(props.getProperty("BatteryOverChargeVoltage2"));
				batteryOverDischargeVoltage= Double.parseDouble(props.getProperty("BatteryOverDischargeVoltage2"));
				batteryOverChargeReturnVoltage= Double.parseDouble(props.getProperty("BatteryOverChargeReturnVoltage2"));
				batteryOverDischargeReturnVoltage= Double.parseDouble(props.getProperty("BatteryOverDischargeReturnVoltage2"));
				panelOverVoltage=Double.parseDouble(props.getProperty("PanelOverVoltage2"));
				panelUnderVoltage=Double.parseDouble(props.getProperty("PanelUnderVoltage2"));
			}
			batteryOverCurrent=Double.parseDouble(props.getProperty("BatteryOverCurrent"));
			overTemperature=Double.parseDouble(props.getProperty("OverTemperature"));
			ledRatedPower = Double.parseDouble(props.getProperty("LedRatedPower"));
			ledRatedVoltage = Double.parseDouble(props.getProperty("LedVoltage"));
			region=props.getProperty("Region");
			dataSchedulerDuration=Long.parseLong(props.getProperty("dataSchedulerDuration"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	

	public boolean registerDevice(String deviceId, String x, String y, long time, List<Long> duration,
			long batteryCapacity, int batteryVoltage) {
		String device = "device/" + deviceId;
		setBatteryVoltage(device, batteryVoltage);
		loadConfig();
		double BAT_U_100MV = batteryFullChargeVoltage;
		double DEVICE_TEMP = 25;
		setLatitude(device, x);
		setLongitude(device, y);
		int loadOrChange=setSunSetSunRise(device,x,y);
		setLoadOrChange(device, loadOrChange);
		setCannotUpdate(device,1);
		setLedRatedPower(device);
		setBatCapSoc(device, 100);
		setBatCurEnergy(device, batteryCapacity);
		setBatCapEnergy(device, batteryCapacity);
//		setBatU100mv(device, BAT_U_100MV);
		setBatU100mv(device, batteryFullChargeVoltage);
		setBatOverChargeVoltage(device,batteryOverChargeVoltage);
		setBatOverDischargeVoltage(device,batteryOverDischargeVoltage);
		setBatOverChargeReturnVoltage(device,batteryOverChargeReturnVoltage);
		setBatOverDischargeReturnVoltage(device,batteryOverDischargeReturnVoltage);
		setBatEmptyChargeVoltage(device,batteryEmptyChargeVoltage);
		setBatFullChargeVoltage(device,batteryFullChargeVoltage);
		setBatI10ma(device, 0);
		setBatOverCurrent(device,batteryOverCurrent);
		setDeviceTemp(device, DEVICE_TEMP);
		setOverTemp(device,overTemperature);
		setLedU100mv(device, ledRatedVoltage);
		setLedI10ma(device, 0);
		setLedOverCurrent(device,ledRatedPower/ledRatedVoltage);
		setLedPowerIn(device, 0);
		setPvU100mv(device, 0);
		setPanelOverVoltage(device,panelOverVoltage);
		setPanelUnderVoltage(device,panelUnderVoltage);
		setPvI10ma(device, 0);
		setChargePower1w(device, 0);
		setPanelTemp(device, 25);
		setMinBatUInDay100mv(device, BAT_U_100MV);
		setMaxBatUInDay100mv(device, 0);
		setMaxChargeBatIInDay10ma(device, 0);
		setMaxDischargeBatIInDay10ma(device, 0);
		setMaxChargePowerInDay1w(device, 0);
		setMaxDischargePowerInDay1w(device, 0);
		setChargeWhInDay(device, 0);
		setDischargeWhInDay(device, 0);
		setDaysTotal(device, 0);
		setBatOverDiscTimes(device, 0);
		setBatOverChargeTimes(device, 0);
		setChargeAhTotal(device, 0);
		setDischargeAhTotal(device, 0);
		setChargeWhTotal(device, 0);
		setDischargeWhTotal(device, 0);
		setWorkState(device, 0);
		setDayLengthIs(device, 0);
		setNightLengthIs(device, 0);
		setDataSchedulerDuration(device,dataSchedulerDuration);
		setManualPower(device,0);
		setManualTime(device,0);
		
		
		setLedCurrent(device,ledRatedPower/ledRatedVoltage);
		setLedLevel(device,0);
		setTimePeriod(device,0);
		
		
		updateRetry(deviceId, time);
		nextRetry(deviceId, duration.get(0));
		return true;
	}
	
	
	public void setCannotUpdate(String device, int i) {
		setCannotUpdate(device,String.valueOf(i));
	}




	public void setCannotUpdate(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "CANNOT_UPDATE",valueOf);
		}
	}

	public int getCannotUpdate(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Integer.parseInt(jedis.hget(device, "CANNOT_UPDATE"));
		}
	}


	public void setManualPower(String device, int i) {
		setManualPower(device,String.valueOf(i));
		
	}

	public void setManualPower(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MANUAL_MODE_POWER",valueOf);
		}
	}
	
	public int getManualPower(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Integer.parseInt(jedis.hget(device, "MANUAL_MODE_POWER"));
		}
	}
	
	public void setManualTime(String device, long i) {
		setManualTime(device,String.valueOf(i));
		
	}

	public void setManualTime(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MANUAL_MODE_TIME",valueOf);
		}
	}
	
	public long getManualTime(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Long.parseLong(jedis.hget(device, "MANUAL_MODE_TIME"));
		}
	}




	public void setBatOverCurrent(String device, double batteryOverCurrent2) {
		setBatOverCurrent(device,String.valueOf(batteryOverCurrent2));
		
	}


	public void setBatOverCurrent(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BATTERY_OVER_CURRENT",valueOf);
		}
	}

	public double getBatOverCurrent(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "BATTERY_OVER_CURRENT"));
		}
	}


	public void setPanelUnderVoltage(String device, double underVolt) {
		setPanelUnderVoltage(device,String.valueOf(underVolt));
		
	}

	public void setPanelUnderVoltage(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "PANEL_UNDER_VOLTAGE",valueOf);
		}
	}
	
	public double getPanelUnderVoltage(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "PANEL_UNDER_VOLTAGE"));
		}
		
	}
	
	public void setPanelOverVoltage(String device, double overVolt) {
		setPanelOverVoltage(device,String.valueOf(overVolt));
		
	}

	public void setPanelOverVoltage(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "PANEL_OVER_VOLTAGE",valueOf);
		}
	}
	
	public double getPanelOverVoltage(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "PANEL_OVER_VOLTAGE"));
		}
		
	}
	
	public void setBatOverDischargeVoltage(String device, double underVolt) {
		setBatOverDischargeVoltage(device,String.valueOf(underVolt));
		
	}

	public void setBatOverDischargeVoltage(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_OVER_DISCH_U",valueOf);
		}
	}
	
	public double getBatOverDischargeVoltage(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "BAT_OVER_DISCH_U"));
		}
		
	}
	
	public void setBatOverDischargeReturnVoltage(String device, double underVolt) {
		setBatOverDischargeReturnVoltage(device,String.valueOf(underVolt));
		
	}

	public void setBatOverDischargeReturnVoltage(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_OVER_DISCHARGE_BACK_U",valueOf);
		}
	}
	
	public double getBatOverDischargeReturnVoltage(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "BAT_OVER_DISCHARGE_BACK_U"));
		}
		
	}
	
	public void setBatOverChargeVoltage(String device, double overVolt) {
		setBatOverChargeVoltage(device,String.valueOf(overVolt));
		
	}

	public void setBatOverChargeVoltage(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_OVER_VOLT",valueOf);
		}
	}
	
	public double getBatOverChargeVoltage(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "BAT_OVER_VOLT"));
		}
		
	}
	
	public void setBatOverChargeReturnVoltage(String device, double overVolt) {
		setBatOverChargeReturnVoltage(device,String.valueOf(overVolt));
		
	}

	public void setBatOverChargeReturnVoltage(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_RECHARGE_U_100MV",valueOf);
		}
	}
	
	public double getBatOverChargeReturnVoltage(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "BAT_RECHARGE_U_100MV"));
		}
		
	}

	public void setOverTemp(String device, double overTemp) {
		setOverTemp(device,String.valueOf(overTemp));
		
	}

	public void setOverTemp(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "OVER_TEMP",valueOf);
		}
	}
	
	public double getOverTemp(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "OVER_TEMP"));
		}
		
	}

	public void setTimePeriod(String device, int i) {
		setTimePeriod(device,String.valueOf(i));
	}

	public void setTimePeriod(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "TIME_PERIOD",valueOf);
		}
	}
	
	public int getTimePeriod(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Integer.parseInt(jedis.hget(device, "TIME_PERIOD"));
		}
		
	}

	public void setDataSchedulerDuration(String device,long duration) {
		setDataSchedulerDuration(device,String.valueOf(duration));
	}

	public void setDataSchedulerDuration(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "DATA_SCHEDULER",valueOf);
		}
		
	}
	
	public long getDataSchedulerDuration(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Long.parseLong(jedis.hget(device, "DATA_SCHEDULER"));
		}
		
	}

	public void setLedLevel(String device, int i) {
		setLedLevel(device,String.valueOf(i));
	}

	public void setLedLevel(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LED_LEVEL",valueOf);
		}
	}
	
	public int getLedLevel(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Integer.parseInt(jedis.hget(device, "LED_LEVEL"));
		}
	}

	public void setLedCurrent(String device,double value) {
		setLedCurrent(device,String.valueOf(value));
	}

	public void setLedCurrent(String device, String valueOf) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LED_CURRENT",valueOf);
		}
	}
	
	public double getLedCurrent(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "LED_CURRENT"));
		}
	}

	public void setPanelTemp(String device, double i) {
		setPanelTemp(device, String.valueOf(i));
	}

	public double getPanelTemp(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "PANEL_TEMP"));
		}
	}

	private void setPanelTemp(String device, String temp) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "PANEL_TEMP", temp);
		}
	}

	public void setLedRatedPower(String device) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LED_RATED_POWER", String.valueOf(ledRatedPower));
		}
	}

	public void setLedRatedVoltage(String device) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LED_RATED_VOLTAGE", String.valueOf(ledRatedVoltage));
		}
	}

	public void setBatEmptyChargeVoltage(String device,double batEmptyChargeVoltage) {

		setBatEmptyChargeVoltage(device, String.valueOf(batEmptyChargeVoltage));
	}

	public void setBatEmptyChargeVoltage(String device, String batEmptyChargeVoltage) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_EMPTY_U_100MV", batEmptyChargeVoltage);
		}
	}
	
	public double getBatEmptyChargeVoltage(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "BAT_EMPTY_U_100MV"));
		}
	}

	public void setBatFullChargeVoltage(String device,double batteryFullChargeVoltage) {

		setBatFullChargeVoltage(device, String.valueOf(batteryFullChargeVoltage));
	}

	public void setBatFullChargeVoltage(String device, String batteryFullChargeVoltage) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_FULL_U_100MV", batteryFullChargeVoltage);
		}
	}
	
	public double getBatFullChargeVoltage(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "BAT_FULL_U_100MV"));
		}
	}

	public void setBatteryVoltage(String device, int batteryVoltage) {
		setBatteryVoltage(device, String.valueOf(batteryVoltage));
	}

	public void setBatteryVoltage(String device, String batteryVoltage) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "SYSTEM_VOLTAGE", batteryVoltage);
		}
	}
	
	public int getBatteryVoltage(String device) {
		try (Jedis jedis = pool.getResource()) {
			System.out.println("Device Name from getBatteryVoltage:"+device);
			return Integer.parseInt(jedis.hget(device, "SYSTEM_VOLTAGE"));
		}
	}

	public void setBatCurEnergy(String device, long i) {
		setBatCurEnergy(device, String.valueOf(i));
	}

	public void setBatCapEnergy(String device, long i) {
		setBatCapEnergy(device, String.valueOf(i));

	}

	public void setBatCurEnergy(String device, String i) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_CUR_ENERGY", i);
		}
	}

	public long getBatCurEnergy(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Long.parseLong(jedis.hget(device, "BAT_CUR_ENERGY"));
		}
	}

	public void setBatCapEnergy(String device, String i) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_CAP_ENERGY", i);
		}

	}

	public long getBatCapEnergy(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Long.parseLong(jedis.hget(device, "BAT_CAP_ENERGY"));
		}
	}

	public void setLatitude(String device, String x) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "X", x);
		}
	}

	public void setLongitude(String device, String y) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "Y", y);
		}
	}

	public int setSunSetSunRise(String device,String lat,String lon) {
		try (Jedis jedis = pool.getResource()) {
			double x = Double.parseDouble(lat);
			double y = Double.parseDouble(lon);
			ZonedDateTime now = ZonedDateTime.now(ZoneId.of(region));
			System.out.println("Now:"+now);
			SunRiseSunSetCalc calc = new SunRiseSunSetCalc();
			ZonedDateTime sunset = calc.getSunSet(x, y, 0);
			ZonedDateTime sunrise = calc.getSunRise(x, y, 0);
			if (now.isBefore(sunrise))
				sunset = calc.getSunSet(x, y, -1);
			else if (now.isAfter(sunset))
				sunrise = calc.getSunRise(x, y, 1);
			 Map<String, String> data = new HashMap<>();
		        data.put("SUNRISE", sunrise.toString());
		        data.put("SUNSET", sunset.toString());
			jedis.hset(device, data);
			if(now.isAfter(sunrise)&&now.isBefore(sunset))
				return 1;
			else
				return 0;
		}
	}

	public void setLoadOrChange(String device, int LOAD_OR_CHANGE) {

		setLoadOrChange(device, String.valueOf(LOAD_OR_CHANGE));
	}

	public void setBatCapSoc(String device, int value) {
		setBatCapSoc(device, String.valueOf(value));
	}

	public void setBatU100mv(String device, double value) {
		
		
		
		setBatU100mv(device, String.valueOf(value));
	}

	public void setBatI10ma(String device, double value) {
		
		
		setBatI10ma(device, String.valueOf(value));
	}

	public void setDeviceTemp(String device, double value) {
		
		setDeviceTemp(device, String.valueOf(value));
	}

	public void setLedU100mv(String device, double value) {
		setLedU100mv(device, String.valueOf(value));
	}

	public void setLedI10ma(String device, double value) {
		setLedI10ma(device, String.valueOf(value));
	}
	
	public void setLedOverCurrent(String device,double value) {
		setLedOverCurrent(device,String.valueOf(value));
	}

	public void setLedPowerIn(String device, double value) {
		setLedPowerIn(device, String.valueOf(value));
	}

	public void setPvU100mv(String device, double value) {
		setPvU100mv(device, String.valueOf(value));
	}

	public void setPvI10ma(String device, double value) {
		setPvI10ma(device, String.valueOf(value));
	}

	public void setChargePower1w(String device, double value) {
		setChargePower1w(device, String.valueOf(value));
	}

	public void setMinBatUInDay100mv(String device, double value) {
		setMinBatUInDay100mv(device, String.valueOf(value));
	}

	public void setMaxBatUInDay100mv(String device, double value) {
		setMaxBatUInDay100mv(device, String.valueOf(value));
	}

	public void setMaxChargeBatIInDay10ma(String device, double value) {
		setMaxChargeBatIInDay10ma(device, String.valueOf(value));
	}

	public void setMaxDischargeBatIInDay10ma(String device, double value) {
		setMaxDischargeBatIInDay10ma(device, String.valueOf(value));
	}

	public void setMaxChargePowerInDay1w(String device, double value) {
		setMaxChargePowerInDay1w(device, String.valueOf(value));
	}

	public void setMaxDischargePowerInDay1w(String device, double value) {
		setMaxDischargePowerInDay1w(device, String.valueOf(value));
	}

	public void setChargeWhInDay(String device, double value) {
		setChargeWhInDay(device, String.valueOf(value));
	}

	public void setDischargeWhInDay(String device, double value) {
		setDischargeWhInDay(device, String.valueOf(value));
	}

	public void setDaysTotal(String device, int value) {
		setDaysTotal(device, String.valueOf(value));
	}

	public void setBatOverDiscTimes(String device, int value) {
		setBatOverDiscTimes(device, String.valueOf(value));
	}

	public void setBatOverChargeTimes(String device, int value) {
		setBatOverChargeTimes(device, String.valueOf(value));
	}

	public void setChargeAhTotal(String device, double value) {
		setChargeAhTotal(device, String.valueOf(value));
	}

	public void setDischargeAhTotal(String device, double value) {
		setDischargeAhTotal(device, String.valueOf(value));
	}

	public void setChargeWhTotal(String device, double value) {
		setChargeWhTotal(device, String.valueOf(value));
	}

	public void setDischargeWhTotal(String device, double value) {
		setDischargeWhTotal(device, String.valueOf(value));
	}

	public void setWorkState(String device, int value) {
		System.out.println("From SetWorkState:"+device);
		System.out.println("From SetWorkState value:"+value);
		setWorkState(device, String.valueOf(value));
	}

	public void setDayLengthIs(String device, long sec) {
		setDayLengthIs(device, String.valueOf(sec));
	}

	public void setNightLengthIs(String device, long sec) {
		setNightLengthIs(device, String.valueOf(sec));
	}

	public void setLoadOrChange(String device, String LOAD_OR_CHANGE) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LOAD_OR_CHANGE", LOAD_OR_CHANGE);
		}
	}

	public void setSunRise(String device, String sunRise) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "SUNRISE", sunRise);
		}
	}

	public void setSunSet(String device, String sunSet) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "SUNSET", sunSet);
		}
	}
	
	public ZonedDateTime getSunRise(String device) {
		try (Jedis jedis = pool.getResource()) {
			return ZonedDateTime.parse(jedis.hget(device, "SUNRISE"));
		}
	}

	public ZonedDateTime getSunSet(String device) {
		try (Jedis jedis = pool.getResource()) {
			return ZonedDateTime.parse(jedis.hget(device, "SUNSET"));
		}
	}

	public void setBatCapSoc(String device, String BAT_CAP_SOC) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_CAP_SOC", BAT_CAP_SOC);
		}
	}
	
	public int getBatCapSoc(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Integer.parseInt(jedis.hget(device, "BAT_CAP_SOC"));
		}
	}

	public void setBatU100mv(String device, String BAT_U_100MV) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_U_100MV", BAT_U_100MV);
		}
	}

	public double getBatU100mv(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "BAT_U_100MV"));
		}
	}

	public void setBatI10ma(String device, String BAT_I_10MA) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_I_10MA", BAT_I_10MA);
		}
	}

	public double getBatI10ma(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "BAT_I_10MA"));
		}
	}

	public void setDeviceTemp(String device, String DEVICE_TEMP) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "DEVICE_TEMP", DEVICE_TEMP);
		}
	}
	
	public double getDeviceTemp(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "DEVICE_TEMP"));
		}
	}

	public void setLedU100mv(String device, String LED_U_100MV) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LED_U_100MV", LED_U_100MV);
		}
	}
	
	public double getLedU100mv(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "LED_U_100MV"));
		}
	}

	public void setLedI10ma(String device, String LED_I_10MA) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LED_I_10MA", LED_I_10MA);
		}
	}
	
	public void setLedOverCurrent(String device, String LED_OVERCURRENT) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LED_OVERCURRENT", LED_OVERCURRENT);
		}
	}
	
	public double getLedOverCurrent(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "LED_OVERCURRENT"));
		}
	}

	public void setLedPowerIn(String device, String LED_POWER_IN) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LED_POWER_1W", LED_POWER_IN);
		}
	}
	
	public double getLedPowerIn(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "LED_POWER_1W"));
		}catch(NullPointerException e){
			return 0;
		}
	}

	public void setPvU100mv(String device, String PV_U_100MV) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "PV_U_100MV", PV_U_100MV);
		}
	}
	
	public double getPvU100mv(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "PV_U_100MV"));
		}
	}

	public void setPvI10ma(String device, String PV_I_10MA) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "PV_I_10MA", PV_I_10MA);
		}
	}

	public void setChargePower1w(String device, String CHARGE_POWER_1W) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "CHARGE_POWER_1W", CHARGE_POWER_1W);
		}
	}

	public double getChargePower1w(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "CHARGE_POWER_1W"));
		}
	}

	public void setMinBatUInDay100mv(String device, String MIN_BAT_U_IN_DAY_100MV) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MIN_BAT_U_IN_DAY_100MV", MIN_BAT_U_IN_DAY_100MV);
		}
	}

	public double getMinBatUInDay100mv(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "MIN_BAT_U_IN_DAY_100MV"));
		}
	}

	public void setMaxBatUInDay100mv(String device, String MAX_BAT_U_IN_DAY_100MV) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MAX_BAT_U_IN_DAY_100MV", MAX_BAT_U_IN_DAY_100MV);
		}
	}

	public double getMaxBatUInDay100mv(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "MAX_BAT_U_IN_DAY_100MV"));
		}
	}

	public void setMaxChargeBatIInDay10ma(String device, String MAX_CHARGE_BAT_I_IN_DAY_10MA) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MAX_CHARGE_BAT_I_IN_DAY_10MA", MAX_CHARGE_BAT_I_IN_DAY_10MA);
		}
	}

	public double getMaxChargeBatIInDay10ma(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "MAX_CHARGE_BAT_I_IN_DAY_10MA"));
		}
	}

	public void setMaxDischargeBatIInDay10ma(String device, String MAX_DISCHARGE_BAT_I_IN_DAY_10MA) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MAX_DISCHARGE_BAT_I_IN_DAY_10MA", MAX_DISCHARGE_BAT_I_IN_DAY_10MA);
		}
	}

	public double getMaxDischargeBatIInDay10ma(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "MAX_DISCHARGE_BAT_I_IN_DAY_10MA"));
		}
	}

	public void setMaxChargePowerInDay1w(String device, String MAX_CHARGE_POWER_IN_DAY_1W) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MAX_CHARGE_POWER_IN_DAY_1W", MAX_CHARGE_POWER_IN_DAY_1W);
		}
	}

	public double getMaxChargePowerInDay1w(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "MAX_CHARGE_POWER_IN_DAY_1W"));
		}
	}

	public void setMaxDischargePowerInDay1w(String device, String MAX_DISCHARGE_POWER_IN_DAY_1W) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MAX_DISCHARGE_POWER_IN_DAY_1W", MAX_DISCHARGE_POWER_IN_DAY_1W);
		}
	}

	public double getMaxDischargePowerInDay1w(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "MAX_DISCHARGE_POWER_IN_DAY_1W"));
		}
	}

	public void setChargeWhInDay(String device, String CHARGE_WH_IN_DAY) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "CHARGE_WH_IN_DAY", CHARGE_WH_IN_DAY);
		}
	}

	public double getChargeWhInDay(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "CHARGE_WH_IN_DAY"));
		}
	}

	public void setDischargeWhInDay(String device, String DISCHARGE_WH_IN_DAY) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "DISCHARGE_WH_IN_DAY", DISCHARGE_WH_IN_DAY);
		}
	}

	public double getDischargeWhInDay(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "DISCHARGE_WH_IN_DAY"));
		}
	}

	public void setDaysTotal(String device, String DAYS_TOTAL) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "DAYS_TOTAL", DAYS_TOTAL);
		}
	}

	public int getDaysTotal(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Integer.parseInt(jedis.hget(device, "DAYS_TOTAL"));
		}
	}

	public void setBatOverDiscTimes(String device, String BAT_OVER_DISC_TIMES) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_OVER_DISC_TIMES", BAT_OVER_DISC_TIMES);
		}
	}

	public void setBatOverChargeTimes(String device, String BAT_OVER_CHARGE_TIMES) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_OVER_CHARGE_TIMES", BAT_OVER_CHARGE_TIMES);
		}
	}

	public void setChargeAhTotal(String device, String CHARGE_AH_TOTAL) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "CHARGE_AH_TOTAL", CHARGE_AH_TOTAL);
		}
	}

	public double getChargeAhTotal(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "CHARGE_AH_TOTAL"));
		}
	}

	public void setDischargeAhTotal(String device, String DISCHARGE_AH_TOTAL) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "DISCHARGE_AH_TOTAL", DISCHARGE_AH_TOTAL);
		}
	}

	public double getDischargeAhTotal(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "DISCHARGE_AH_TOTAL"));
		}
	}

	public void setChargeWhTotal(String device, String CHARGE_WH_TOTAL) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "CHARGE_WH_TOTAL", CHARGE_WH_TOTAL);
		}
	}

	public double getChargeWhTotal(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "CHARGE_WH_TOTAL"));
		}
	}

	public void setDischargeWhTotal(String device, String DISCHARGE_WH_TOTAL) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "DISCHARGE_WH_TOTAL", DISCHARGE_WH_TOTAL);
		}
	}

	public double getDischargeWhTotal(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Double.parseDouble(jedis.hget(device, "DISCHARGE_WH_TOTAL"));
		}
	}

	public void setWorkState(String device, String WORK_STATE) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "WORK_STATE", WORK_STATE);
		}
	}
	
	public int getWorkState(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Integer.parseInt(jedis.hget(device, "WORK_STATE"));
		}
	}

	public void setDayLengthIs(String device, String sec) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "DAY_LENGTH_IS", sec);
		}
	}

	public long getDayLengthIs(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Long.parseLong(jedis.hget(device, "DAY_LENGTH_IS"));
		}
	}

	public void setNightLengthIs(String device, String sec) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "NIGHT_LENGTH_IS", sec);
		}
	}

	public long getNightLengthIs(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Long.parseLong(jedis.hget(device, "NIGHT_LENGTH_IS"));
		}
	}

	public void updateRetry(String deviceId, long time) {
		try (Jedis jedis = pool.getResource()) {
			jedis.zadd("undiscovered_devices", time, deviceId);
		}
	}

	public void nextRetry(String deviceId, long duration) {
		try (Jedis jedis = pool.getResource()) {
			double currentRetry;
			try {
				currentRetry = jedis.zscore("nextretry", deviceId);
			} catch (NullPointerException e) {
				currentRetry = jedis.zscore("undiscovered_devices", deviceId);
			}
			System.out.println("Current Retry in DragonFly:" + currentRetry);
			long nextRetry = (long) currentRetry + duration;
			jedis.zadd("nextretry", (double) nextRetry, deviceId);
		}
	}

	public void markDiscovered(String deviceId) {
		try (Jedis jedis = pool.getResource()) {
			jedis.zrem("undiscovered_devices", deviceId);
			jedis.zrem("nextretry", deviceId);
			long now = System.currentTimeMillis();
			jedis.zadd("discovered_devices", now, deviceId);
		}
	}

	public void markAllDiscovered() {
		try (Jedis jedis = pool.getResource()) {
			List<String> devices = jedis.zrange("undiscovered_devices", 0, -1);

			long now = System.currentTimeMillis();

			for (String device : devices) {
				jedis.zrem("undiscovered_devices", device);
				jedis.zrem("nextretry", device);

				jedis.zadd("discovered_devices", now, device);
			}
		}
	}

	@Override
	public String getLatitude(String topic) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.hget(topic, "X");
		}

	}

	@Override
	public String getLongitude(String topic) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.hget(topic, "Y");
		}
	}

	@Override
	public int getIntUndiscovered() {
		try (Jedis jedis = pool.getResource()) {
			return (int) jedis.zcard("undiscovered_devices");
		}
	}

	@Override
	public List<String> getCurrentRetryReadyDevice(long now) {

		try (Jedis jedis = pool.getResource()) {
			return jedis.zrangeByScore("nextretry", 0, now);
		}
	}

	@Override
	public void clearDatabase() {
		try (Jedis jedis = pool.getResource()) {
			jedis.flushAll(); // optional
		}
		pool.close();

	}

	@Override
	public List<Tuple> getAllRetryDevices() {
		try (Jedis jedis = pool.getResource()) {
			return jedis.zrangeWithScores("undiscovered_devices", 0, -1);
		}
	}

	public List<String> getAllDevices() {
		try (Jedis jedis = pool.getResource()) {

			List<String> discovered = jedis.zrange("discovered_devices", 0, -1);

			List<String> undiscovered = jedis.zrange("undiscovered_devices", 0, -1);

			discovered.addAll(undiscovered);
			return discovered;
		}
	}

	@Override
	public double getCreatedTime(String deviceId) {

		try (Jedis jedis = pool.getResource()) {
			return jedis.zscore("undiscovered_devices", deviceId);
		}
	}

	@Override
	public double getFieldValue(String device, String field) {
		double value;
		try (Jedis jedis = pool.getResource()) {

			if (field == null)
				value = 0;
			else {
				try {
					System.out.println("Field from getFieldValue:"+field);
					value = Double.parseDouble(jedis.hget(device, field));
				} catch (NullPointerException e) {
					System.out.println("Null Pointer Exception:" + field);
					value = 0;
				}
				catch(IllegalArgumentException e) {
					System.out.println("Null Pointer Exception:" + field);
					value = 0;
				}
			}
		}
		return value;
	}

	@Override
//	public List<String> getDiscoveryDeviceFilter(long duration) {
//		List<String> StatsReadyDevice;
//		try (Jedis jedis = pool.getResource()) {
//			long now = System.currentTimeMillis();
//			long time = now - duration;
//			StatsReadyDevice = jedis.zrangeByScore("discovered_devices", 0, time);
//			for (String device : StatsReadyDevice) {
//				jedis.zadd("discovered_devices", now, device);
//			}
//		}
//		return StatsReadyDevice;
//
//	}
	
	public List<String> getDiscoveryDeviceFilter(long now) {
		List<String> StatsDevice;
		List<String> StatsReadyDevice=new ArrayList<>();
		try (Jedis jedis = pool.getResource()) {
			StatsDevice = jedis.zrangeByScore("discovered_devices",Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY);
			for (String deviceId : StatsDevice) {
				String device="device/"+deviceId;
				long duration=getDataSchedulerDuration(device);
				long time = now - duration;
				StatsReadyDevice = jedis.zrangeByScore("discovered_devices", 0, time);
				for (String dev : StatsReadyDevice) {
					jedis.zadd("discovered_devices", now, dev);
				}
//				if(isScoreLesser(deviceId,time)) {
//					jedis.zadd("discovered_devices", now, deviceId);
//					StatsReadyDevice.add(deviceId);
//				}
			}
		}
		return StatsReadyDevice;

	}
	
	public boolean isScoreLesser(String deviceId, long checkTime) {

	    try (Jedis jedis = pool.getResource()) {

	        Double score = jedis.zscore("discovered_devices", deviceId);

	        if (score == null) {
	            return false; // device not present
	        }
	        System.out.println("Score:"+score);
	        System.out.println("Check Time:"+checkTime);
	        return score < checkTime;
	    }
	}

	@Override
	public int getLOADORCHANGE(String device) {
		try (Jedis jedis = pool.getResource()) {
			return Integer.parseInt(jedis.hget(device, "LOAD_OR_CHANGE"));
		}
	}
	
	public void setUpdate(String device,Map<String,String> data) {
		try (Jedis jedis = pool.getResource()) {
		

			jedis.hset(device, data);
		}
	}
	
	public void setUpdateField(String device,String field,String value) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, field,value);
			value=jedis.hget(device, field);
			System.out.println("From Set Update Field"+device+field+value);
		}
	}
	
	public void addTimePeriodSchedule(String deviceId,int i,int value,double x,double y) {
		try (Jedis jedis = pool.getResource()) {
//			ZonedDateTime now = ZonedDateTime.now(ZoneId.of(region));
//			SunRiseSunSetCalc calc = new SunRiseSunSetCalc();
//			ZonedDateTime sunset = calc.getSunSet(x, y, 0);
//			ZonedDateTime sunrise = calc.getSunRise(x, y, 0);
//			if (now.isBefore(sunrise))
//				sunset = calc.getSunSet(x, y, -1);
//
//			long sunSet = sunset.toInstant().toEpochMilli();
			long nextTime=0;
			if(i/3!=1) {
				double score=jedis.zscore("schedule_duration",deviceId+"/"+String.valueOf((i-3)/3));
//				sunSet=(long)score;
				nextTime=(long)score;
			}
//			nextTime=sunSet+(value*1000);
			nextTime=nextTime+(value*1000);
			System.out.println("From addTimePeriodSchedule nextTime:"+nextTime);
//			Instant instant = Instant.ofEpochMilli(nextTime);
//
//			ZonedDateTime time =
//			        instant.atZone(ZoneId.of(region));
//
//			System.out.println(time);
			jedis.zadd("schedule_duration", nextTime,deviceId+"/"+String.valueOf(i/3));
		}
	}
	
	public long getScheduleDuration(String deviceId,int i) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.zscore("schedule_duration",deviceId+"/"+i).longValue();
		}
	}

	
	public void addCurrentForTimePeriod(String deviceId,int i,int value) {
		try (Jedis jedis = pool.getResource()) {
			int val=i/3;
			jedis.zadd("schedule_current", value,deviceId+"/"+String.valueOf(val));
		}
	}
	
	public int getScheduleCurrent(String deviceId,int i) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.zscore("schedule_current", deviceId+"/"+String.valueOf(i)).intValue();
		}
		catch(Exception e) {
			return 0;
		}
	}
	
	
	
	public void addCurrent2ForTimePeriod(String deviceId,int i,int value) {
		try (Jedis jedis = pool.getResource()) {
			int val=i/3;
			jedis.zadd("schedule_current2", value,deviceId+"/"+String.valueOf(val));
		}
	}

}
