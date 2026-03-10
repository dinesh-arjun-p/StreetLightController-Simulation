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
import ohlisimulator.vendor.Bosun;
import ohlisimulator.vendor.Vendor;

public class AutomateDatas implements UpdateWorkState {
	Service service = new Service();
	int cores = Runtime.getRuntime().availableProcessors();
	ForkJoinPool pool;
	String region;
	public static int duration;
	AutomateDatas(int duration){
		this.duration=duration;
	}
	private final ThreadLocal<Bosun> threadBosun = ThreadLocal.withInitial(Bosun::new);
	double batteryFullChargeVoltage = 0;
	double batteryRechargeVoltage = 0;
	double batteryRechargeVoltage2 = 0;
	double panelVoltage=0,panelPower=0;
	int batteryVoltage=0;
	private void loadConfig(){
		try {
			Properties props = new Properties();
			InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");

			props.load(input);
			
			
			if((batteryVoltage==12)) {
				batteryFullChargeVoltage = Double.parseDouble(props.getProperty("BatteryFullChargeVoltage1"));
				panelVoltage=Double.parseDouble(props.getProperty("panelVoltage1"));
				panelPower=Double.parseDouble(props.getProperty("panelPower1"));
			}
			if((batteryVoltage==24)) {
				batteryFullChargeVoltage = Double.parseDouble(props.getProperty("BatteryFullChargeVoltage2"));
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
		pool = new ForkJoinPool(String.valueOf(devices.size()).length());
		for (String device : devices) {
			pool.execute(() -> updateDevice(device));
		}
	}

	private void updateDevice(String device) {
		batteryVoltage=service.getSystemVoltage(device);
		loadConfig();
		updatePanel(device);
		updateBattery(device);
		updateLoad(device);
		updateWorkState(device);
		
		
		
		
		SunRiseSunSetCalc calc=new SunRiseSunSetCalc();
		double x=service.getLatitude(device);
		double y=service.getLongitude(device);
		ZonedDateTime now=ZonedDateTime.now(ZoneId.of(region));
		ZonedDateTime sunRise=calc.getSunRise(x, y, 0);
		ZonedDateTime sunSet=calc.getSunSet(x,y,0);
		service.setSunRise(device,sunRise);
		service.setSunSet(device,sunSet);
		long seconds = Duration.between(sunRise,sunSet).getSeconds();
		if(now.isAfter(sunSet)&&now.isAfter(sunRise)) {
			service.setDayLengthIs(device,seconds);
			sunRise=calc.getSunRise(x, y, 1);
			service.setSunRise(device,sunRise);
		}
		if(now.isBefore(sunRise)&&now.isBefore(sunSet)) {
			sunSet=calc.getSunSet(x, y, -1);
			seconds=Duration.between(sunRise,sunSet).getSeconds();
			service.setNightLengthIs(device,seconds);
			service.setSunSet(device,sunSet);
		}
	}

//	public void updateWorkState(String device) {
//		int workState=service.getWorkState(device);
//		//System.out.println("WorkState:"+workState);
//		int newWorkState=0;
//		for(int i=15;i>=0;i--) {
//			int fault=(workState>>i)&1;
//			if(fault==0) {
//			//	System.out.println("Fault 0 i "+i);
//				if(i==0) {
//					if(!service.isDeviceTempNormal(device)) 		
//						newWorkState|=1;
//				}
//				else if(i==1) {
//					if(service.isBatteryOverCurrent(device)) {
//						newWorkState|=1;
//					}
//				}
//				else if(i==2) {
//					if(service.isBatteryOverDischarge(device))
//						newWorkState|=1;
//				}
//				else if(i==3) {
//					
//					if(service.isBatteryOverVoltage(device)) {
//						//System.out.println("Battery Over Voltage Workstate");
//						newWorkState|=1;
//					}
//				}
//				else if(i==4) {
//					
//					if(service.isBatteryOverDischargeVoltage(device)) {
//						//System.out.println("Battery Under Voltage Workstate");
//						newWorkState|=1;
//					}
//				}
//				else if(i==9) {
//					if(service.isPanelUnderVoltage(device)) 
//						newWorkState|=1;
//				}
//				else if(i==10) {
//					if(service.isPanelOverVoltage(device))
//						newWorkState|=1;
//				}
//				else if(i==11) {
//					if(service.isDayBurner(device))
//						newWorkState|=1;
//				}
//				else if(i==12) {
//					if(service.isNightOutage(device))
//						newWorkState|=1;
//				}
//				
//			}
//			else {
//				//System.out.println("Fault 1 i "+i);
//				if(i==0) {
//					if(service.isDeviceTempNormal(device)) 	{	
//						newWorkState&=~1;
//					}
//					else {
//						newWorkState|=1;
//					}
//				}
//				else if(i==1) {
//					if(!service.isBatteryOverCurrent(device)) {
//						newWorkState&=~1;
//					}else {
//						newWorkState|=1;
//					}
//				}
//				else if(i==2) {
////					if(!service.isBatteryOverDischarge(device))
//					if(!service.isBatteryOverDischargeVoltage(device))
//						newWorkState&=~1;
//					else {
//						newWorkState|=1;
//					}
//				}
//				else if(i==3) {
//					if(!service.isBatteryOverVoltage(device))
//						newWorkState&=~1;
//					else {
//						newWorkState|=1;
//					}
//				}
//				else if(i==4) {
//					if(!service.isBatteryOverDischargeVoltage(device))
//						newWorkState&=~1;
//					else {
//						newWorkState|=1;
//					}
//				}
//				else if(i==9) {
//					if(!service.isPanelUnderVoltage(device)) 
//						newWorkState&=~1;
//					else {
//						newWorkState|=1;
//					}
//				}
//				else if(i==10) {
//					if(!service.isPanelOverVoltage(device))
//						newWorkState&=~1;
//					else {
//						newWorkState|=1;
//					}
//				}
//				else if(i==11) {
//					if(!service.isDayBurner(device))
//						newWorkState&=~1;
//					else {
//						newWorkState|=1;
//					}
//				}
//				else if(i==12) {
//					if(!service.isNightOutage(device))
//						newWorkState&=~1;
//					else {
//						newWorkState|=1;
//					}
//				}
//				
//			}
//			if(i!=0)
//			newWorkState<<=1;
//			//System.out.println("New Work State:"+newWorkState);
//		}
//		if(newWorkState!=workState) {
//			System.out.println("New Work State "+newWorkState);
//			System.out.println("Work State "+workState);
//			Vendor vendor = threadBosun.get();
//			service.setWorkState(device,newWorkState);
//			vendor.publishWorkState(device,2,newWorkState);
//			
//		}
//		
//	}
	
	public void compareWorkState(String device,int newWorkState, int workState) {
		if(newWorkState!=workState) {
			System.out.println("New Work State "+newWorkState);
			System.out.println("Work State "+workState);
			Vendor vendor = threadBosun.get();
			service.setWorkState(device,newWorkState);
			vendor.publishWorkState(device,2,newWorkState);
			
		}
	}
	
	 public Service getService() {
	        return service;
	    }
	
	private void updateLoad(String device) {
		
		System.out.println("Update Load Started");
		
//			SunRiseSunSetCalc calc=new SunRiseSunSetCalc();
//			double x=service.getLatitude(device);
//			double y=service.getLongitude(device);
			ZonedDateTime now=ZonedDateTime.now(ZoneId.of(region));
			ZonedDateTime sunRise=service.getSunRise(device);
			ZonedDateTime sunSet=service.getSunSet(device);
//			if(now.isAfter(sunSet))
//				sunRise=calc.getSunRise(x, y, 1);
//			else if(now.isBefore(sunRise))
//				sunSet=calc.getSunRise(x, y, -1);
			if(service.getManualTime(device)<=0) {
				if(now.isAfter(sunSet)&&now.isBefore(sunRise)) {
					service.clearDayBurner(device);
					
//					if(loadOrChange==1&&service.getBatU100mv(device)>=batteryRechargeVoltage2) {
//						service.setLOADORCHANGE(device,0);
//						loadOrChange=0;
//					}
					//scheduleLogic
					if(service.getCannotUpdate(device)==2) {
						service.setLedLevel(device, 0);
					}else {
					
							int time=service.getTimePeriod(device);
							if(time==0) {
								service.setLOADORCHANGE(device, 0);
								time=1;
							}
							long elapsedTime = Duration.between(sunSet,now).toMillis();
							long duration=service.getScheduleDuration(device,time);
							
							while(duration<elapsedTime&&time<9) {
								
								if(duration==-1)
									break;
								time++;
								duration=service.getScheduleDuration(device,time);
							}
							System.out.println("Time Period from Update Load:"+time);
							service.setTimePeriod(device,time);
							int level=service.getScheduleCurrent(device,time);
							System.out.println("Level:"+level);
						
							service.setLedLevel(device, level);
					}
					
				}
				else {
					
							service.setTimePeriod(device,0);
							service.setLedLevel(device, 0);
					}
					
				
			}
			else {
				if(service.getCannotUpdate(device)!=2) {
					service.setLedLevel(device, service.getManualPower(device));
					service.updateManualTime(device,AutomateDatas.duration);
				}
				else {
					service.setLedLevel(device, 0);
					service.updateManualTime(device,AutomateDatas.duration);
				}
			}
		
	}

	private void updatePanel(String device) {
		System.out.println("Update Panel Started");
//		SunRiseSunSetCalc calc=new SunRiseSunSetCalc();
//		double x=service.getLatitude(device);
//		double y=service.getLongitude(device);
		double panelVoltageLocal=panelVoltage;
		double panelPowerLocal=panelPower;
		double panelCurrentLocal=0;
		
		ZonedDateTime now=ZonedDateTime.now(ZoneId.of(region));

		ZonedDateTime sunRise=service.getSunRise(device);
		ZonedDateTime sunSet=service.getSunSet(device);
		System.out.println("Now:"+now);
		System.out.println("sunRise:"+sunRise);
		System.out.println("sunSet:"+sunSet);
		if(now.isAfter(sunSet)&&now.isBefore(sunRise)) {
			System.out.println("Night");
			service.setPanel(device,0,0,0,0);
		}
		else {
			if(service.getLOADORCHANGE(device)==1&&service.getCannotUpdate(device)!=1) {
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
	    double TEMP_COEFF = -0.3;     
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
		
		System.out.println("BatteryPercentage:"+service.getBatCapSoc(device));
		ZonedDateTime now=ZonedDateTime.now(ZoneId.of(region));
//		SunRiseSunSetCalc calc=new SunRiseSunSetCalc();
//		double x=service.getLatitude(device);
//		double y=service.getLongitude(device);
		
		ZonedDateTime sunRise=service.getSunRise(device);
		ZonedDateTime sunSet=service.getSunSet(device);
//		if (service.getLOADORCHANGE(device) == 0) {
		if(service.getCannotUpdate(device)!=2) {
			//DischargingState
			if(now.isBefore(sunSet)&&now.isAfter(sunRise)) {
				if(service.getManualTime(device)<=0) {
					service.setLOADORCHANGE(device, 1);
					service.dishargeWithoutConnection(device);
				}
				if(service.getManualTime(device)>0) {
					service.setLOADORCHANGE(device, 0);
					service.dishargeWithConnection(device);
				}
				
				
			}
			else {
				service.dishargeWithConnection(device);
				service.updateMinimumBatteryVoltageDuringNight(device);
			}
			
			
		} 
		if(service.getLOADORCHANGE(device)==0) {
			service.updateDailyDischargingPower(device);
			service.updateDailyDischargingCurrent(device);
		}if(service.getLOADORCHANGE(device)==1) {
			service.updateDailyChargingPower(device);
			service.updateDailyChargingCurrent(device);
		}
		service.updateBatTemp(device);
		if (now.getHour() == 0 &&
			    now.getMinute() == 0 &&
			    now.getSecond() == 0) {
			service.resetDailyCharingAndDischarging(device);
			service.incrementWorkingDay(device);
		}
		service.updateMaximumBatteryVoltageDuringDay(device);
		service.updateMaximumCurrentOfDay(device);
		service.updateMaximumPowerOfDay(device);
		long batCurCap=service.getBatCurEnergy(device);
		long batCap=service.getBatCapEnergy(device);
		service.updateBatVoltage("device/"+device, batCurCap, batCap);
		
	}
}
