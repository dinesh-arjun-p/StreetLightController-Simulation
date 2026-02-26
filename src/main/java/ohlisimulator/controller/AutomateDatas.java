package ohlisimulator.controller;

import java.io.InputStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;

import ohlisimulator.main.SunRiseSunSetCalc;
import ohlisimulator.service.Service;

public class AutomateDatas {
	Service service = new Service();
	int cores = Runtime.getRuntime().availableProcessors();
	ForkJoinPool pool = new ForkJoinPool(cores);
	String region;
	public static int duration;
	AutomateDatas(int duration){
		this.duration=duration;
	}
	
	
	double panelVoltage=0,panelPower=0,batteryVoltage=0;
	{
		try {
			Properties props = new Properties();
			InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");

			props.load(input);
			
			batteryVoltage=Integer.parseInt(props.getProperty("batteryVoltage"));
			
			if((batteryVoltage==12)) {
				panelVoltage=Double.parseDouble(props.getProperty("panelVoltage1"));
				panelPower=Double.parseDouble(props.getProperty("panelPower1"));
			}
			if((batteryVoltage==24)) {
				panelVoltage=Double.parseDouble(props.getProperty("panelVoltage2"));
				panelPower=Double.parseDouble(props.getProperty("panelPower2"));
			}
			region=props.getProperty("Region");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void start() {
		List<String> devices = service.getAllDevice();
		for (String device : devices) {
			pool.execute(() -> updateDevice(device));
		}
	}

	private void updateDevice(String device) {
		updatePanel(device);
		updateBattery(device);
		updateLoad(device);
		
		
		
		SunRiseSunSetCalc calc=new SunRiseSunSetCalc();
		double x=service.getLatitude(device);
		double y=service.getLongitude(device);
		ZonedDateTime now=ZonedDateTime.now(ZoneId.of(region));
		ZonedDateTime sunRise=calc.getSunRise(x, y, 0);
		ZonedDateTime sunSet=calc.getSunSet(x,y,0);
		long seconds = Duration.between(sunRise,sunSet).getSeconds();
		if(now.getHour()==sunSet.getHour()&&now.getMinute()==sunSet.getMinute()) {
			service.setDayLengthIs(device,seconds);
			sunRise=calc.getSunRise(x, y, 1);
			service.setSunRise(device,sunRise);
		}
		if(now.getHour()==sunRise.getHour()&&now.getMinute()==sunRise.getMinute()) {
			sunSet=calc.getSunSet(x, y, -1);
			seconds=Duration.between(sunRise,sunSet).getSeconds();
			service.setNightLengthIs(device,seconds);
			sunSet=calc.getSunSet(x, y,0);
			service.setSunSet(device,sunSet);
		}
	}

	private void updateLoad(String device) {
		
		System.out.println("Update Load Started");
		
			SunRiseSunSetCalc calc=new SunRiseSunSetCalc();
			double x=service.getLatitude(device);
			double y=service.getLongitude(device);
			ZonedDateTime now=ZonedDateTime.now(ZoneId.of(region));
			ZonedDateTime sunRise=calc.getSunRise(x, y, 0);
			ZonedDateTime sunSet=calc.getSunSet(x,y,0);
			if(now.isAfter(sunSet))
				sunRise=calc.getSunRise(x, y, 1);
			else if(now.isBefore(sunRise))
				sunSet=calc.getSunRise(x, y, -1);
			if(now.isAfter(sunSet)&&now.isBefore(sunSet)) {
				if(service.getLOADORCHANGE(device)==1) {
					service.setLOADORCHANGE(device,0);
				}
				//scheduleLogic
				service.setLampLevel(device,100);
			}
			else {
				//service.setLampLevel(device,200);
			}
		
		
	}

	private void updatePanel(String device) {
		System.out.println("Update Panel Started");
		SunRiseSunSetCalc calc=new SunRiseSunSetCalc();
		double x=service.getLatitude(device);
		double y=service.getLongitude(device);
		double panelVoltageLocal=panelVoltage;
		double panelPowerLocal=panelPower;
		double panelCurrentLocal=0;
		ZonedDateTime now=ZonedDateTime.now(ZoneId.of(region));

		ZonedDateTime sunRise=calc.getSunRise(x, y, 0);
		ZonedDateTime sunSet=calc.getSunSet(x,y,0);
		System.out.println("Now:"+now);
		System.out.println("sunRise:"+sunRise);
		System.out.println("sunSet:"+sunSet);
		if(now.isAfter(sunSet)||now.isBefore(sunRise)) {
			System.out.println("Night");
			service.setPanel(device,0,0,0,0);
		}
		else {
			if(service.getLOADORCHANGE(device)==1) {
				double temp=service.getPanelTemp(device);
				double panelEfficiency=(100-((temp-25)*0.4))/100;
				double maxPower=panelPowerLocal;
				
				panelPowerLocal=calculateSolarPower(now,sunRise,sunSet,maxPower)*panelEfficiency;
				System.out.println("Panel Power:"+panelPowerLocal);
				double sunlightFactor = panelPowerLocal / maxPower;
				panelVoltageLocal=calculatePanelVoltage(sunlightFactor,temp);
				System.out.println("Panel Voltage:"+panelVoltageLocal);
				panelCurrentLocal=panelPowerLocal/(panelVoltageLocal);
				System.out.println("Panel Current:"+panelCurrentLocal);
				//BatteryCharge
				service.chargeFromPanel(device,panelPowerLocal);
				if(service.getLOADORCHANGE(device)==0) {
					//Arise Faults
				}
					
			}
			else {
				panelPowerLocal=0;
				panelCurrentLocal=0;
			}
			service.setPanel(device, panelVoltageLocal, panelCurrentLocal, panelPowerLocal, 1);
		}
	}
	
	public double calculateSolarPower(
			 ZonedDateTime now,
		        ZonedDateTime sunrise,
		        ZonedDateTime sunset,
	        double maxPower) {

		 Duration rampDuration = Duration.ofHours(3);
		 System.out.println("Ramp Duration:"+rampDuration);

		 Duration elapsedFromSunrise = Duration.between(sunrise, now);
		    Duration timeBeforeSunset = Duration.between(now, sunset);
		    System.out.println("elapsedFromSunrise:"+elapsedFromSunrise);
		    System.out.println("timeBeforeSunset:"+timeBeforeSunset);
		    if (elapsedFromSunrise.compareTo(rampDuration) <= 0) {
		        double ratio =
		                (double) elapsedFromSunrise.toMillis() /
		                rampDuration.toMillis();
		        System.out.println("Ratio");
		        return maxPower * ratio;
		    }

		    if (timeBeforeSunset.compareTo(rampDuration) <= 0) {
		        double ratio =
		                (double) timeBeforeSunset.toMillis() /
		                rampDuration.toMillis();
		        System.out.println("Ratio");
		        return maxPower * ratio;
		    }
		    System.out.println("Ratio");
		    return maxPower;
	}
	public double calculatePanelVoltage(
	        double sunlightFactor,  
	        double temperature       
	) {

	    double IDEAL_TEMP = 25.0;
	    double BASE_VOLTAGE = panelVoltage;     
	    double TEMP_COEFF = -0.003;     
	    double tempDifference = temperature - IDEAL_TEMP;
	    double tempEffect = BASE_VOLTAGE * TEMP_COEFF * tempDifference;

	    double voltageWithTemp = BASE_VOLTAGE + tempEffect;

	   
	    double sunlightEffect = (1 - sunlightFactor) * 2.0; 

	    double finalVoltage = voltageWithTemp - sunlightEffect;

	   
	    if (finalVoltage < panelVoltage-5)
	        finalVoltage = panelVoltage-5;

	    return finalVoltage;
	}
	private void updateBattery(String device) {
		
		//arise Fault OverTemp
		ZonedDateTime now=ZonedDateTime.now(ZoneId.of(region));
		SunRiseSunSetCalc calc=new SunRiseSunSetCalc();
		double x=service.getLatitude(device);
		double y=service.getLongitude(device);
		
		ZonedDateTime sunRise=calc.getSunRise(x, y, 0);
		ZonedDateTime sunSet=calc.getSunSet(x,y,0);
		if (service.getLOADORCHANGE(device) == 0) {
			//DischargingState
			if(now.isBefore(sunSet)&&now.isAfter(sunRise)) {
				service.dishargeWithoutConnection(device);
				
				
			}
			else {
				service.dishargeWithConnection(device);
				service.updateMinimumBatteryVoltageDuringNight(device);
			}
			
			service.updateDailyDischargingPower(device);
			service.updateDailyDischargingCurrent(device);
			
		} else {
			//ChargingState
			//Login in Update Panel
			service.updateDailyChargingPower(device);
			service.updateDailyChargingCurrent(device);
		}
		if (now.getHour() == 0 &&
			    now.getMinute() == 0 &&
			    now.getSecond() == 0) {
			service.resetDailyCharingAndDischarging(device);
			service.incrementWorkingDay(device);
		}
		service.updateMaximumBatteryVoltageDuringDay(device);
		service.updateMaximumCurrentOfDay(device);
		service.updateMaximumPowerOfDay(device);
		
	}
}
