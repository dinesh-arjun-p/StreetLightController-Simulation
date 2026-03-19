package ohlisimulator.controller;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;

import ohlisimulator.main.SunRiseSunSetCalc;
import ohlisimulator.service.Service;
import ohlisimulator.vendor.Bosun;
import ohlisimulator.vendor.Vendor;

public class AutomateDatas implements UpdateWorkState {
	Service service = new Service();
	
	ForkJoinPool pool;
	static String region;
	public static int duration;
	
	public AutomateDatas(int duration){
		this.duration=duration;
	}
	static Properties props;
	private final ThreadLocal<Bosun> threadBosun = ThreadLocal.withInitial(Bosun::new);
	static double[] panelVoltage=new double[2];
	static double[] panelPower=new double[2];
	static {
		try {
			props = new Properties();
			InputStream input = new FileInputStream("config/config.properties");

			props.load(input);
			region=props.getProperty("Region"+props.getProperty("region"));
			panelVoltage[0]=Double.parseDouble(props.getProperty("panelVoltage1"));
			panelVoltage[1]=Double.parseDouble(props.getProperty("panelVoltage2"));
			panelPower[0]=Double.parseDouble(props.getProperty("panelPower1"));
			panelPower[1]=Double.parseDouble(props.getProperty("panelPower1"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private double loadConfigPanelVoltage(int batteryVoltage){
		try {
			System.out.println("Battery Voltage from loadConfigPanelVoltage1:"+batteryVoltage);
			if((batteryVoltage==12)) {
				return panelVoltage[0];
			}
			if((batteryVoltage==24)) {
				return panelVoltage[1];
			}
			return 0.0;

		} catch (Exception e) {
			return 0.0;
		}
	}
	
	private double loadConfigPanelPower( int batteryVoltage){
		try {
			
			
			if((batteryVoltage==12)) {
				return panelPower[0];
			}
			if((batteryVoltage==24)) {
				return panelPower[1];
			}
			return 0.0;

		} catch (Exception e) {
			return 0.0;
		}
	}
	public void start(ForkJoinPool pool,List<String> devices) {
//		List<String> devices = service.getAllDevice();
////		pool = new ForkJoinPool(String.valueOf(devices.size()).length());
//		int cores = Runtime.getRuntime().availableProcessors();
//		pool = new ForkJoinPool(Math.min(cores*4,(int)Math.ceil(devices.size()/1250.0)*2));
//		long start = System.nanoTime(); 
		
		
		for (String device : devices) {
			pool.execute(() -> updateDevice(device));
		}
//		pool.shutdown();
//		try {
//			pool.awaitTermination(60,TimeUnit.SECONDS);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		long end = System.nanoTime();   // end time
//
//	    long timeMs = (end - start) / 1_000_000;
//	    
//	    synchronized(DataScheduler.class) {
//	    	DataScheduler.totalTime += timeMs;
//	    	DataScheduler.runCount++;
//		   
//
//		    if(DataScheduler.runCount == 3){
//		    	DataScheduler.runCount=0;
//		    	DataScheduler.writeTimeToFile(DataScheduler.totalTime);
//		    }
//	    }
	    
	}

	public void updateDevice(String device) {
	 
		SunRiseSunSetCalc calc;
		double x;
		double y;
		int day;
		System.out.println("Region:" + region + "-");
		ZonedDateTime now=ZonedDateTime.now(ZoneId.of(region));
		ZonedDateTime sunRise=service.getSunRise(device);
		ZonedDateTime sunSet=service.getSunSet(device);
		System.out.println("Now:"+now);
		System.out.println("SunRise:"+sunRise);
		System.out.println("SunSet:"+sunSet);
		long seconds;
		if(now.isAfter(sunSet)&&now.isAfter(sunRise)) {
			calc=new SunRiseSunSetCalc();
			x=service.getLatitude(device);
			y=service.getLongitude(device);
			if(sunSet.toLocalDate().equals(now.toLocalDate())) {
				seconds = Duration.between(sunRise,sunSet).getSeconds();
				service.setDayLengthIs(device,seconds);
				sunRise=calc.getSunRise(x, y, 1);
				service.setSunRise(device,sunRise);
			}
			else {
				seconds=Duration.between(sunRise,sunSet).getSeconds();
				sunSet=calc.getSunSet(x, y, 0);
				
				service.setNightLengthIs(device,seconds);
				service.setSunSet(device,sunSet);
			}
		}
//		if(now.isBefore(sunRise)&&now.isBefore(sunSet)) {
//			calc=new SunRiseSunSetCalc();
//			x=service.getLatitude(device);
//			y=service.getLongitude(device);
//			sunSet=calc.getSunSet(x, y, -1);
//			seconds=Duration.between(sunRise,sunSet).getSeconds();
//			
//			service.setNightLengthIs(device,seconds);
//			service.setSunSet(device,sunSet);
//		}
		if(now.isAfter(sunSet)&&now.isBefore(sunRise))
			day=0;
		else
			day=1;
		int cannotUpdate=service.getCannotUpdate(device);
		int batteryVoltage=service.getSystemVoltage(device);
		long batteryCurrentCapacity=service.getBatCurEnergy(device);
		long batteryCapacity=service.getBatCapEnergy(device);
		Map<String,String> data=new HashMap<>();
		cannotUpdate=service.updateBatVoltage("device/"+device, batteryCurrentCapacity,batteryCapacity,cannotUpdate,data);
		service.setUpdate(device, data);
		double panelPower=loadConfigPanelPower(batteryVoltage);
		double panelVoltage=loadConfigPanelVoltage(batteryVoltage);
		System.out.println("Panel Voltage From Config:"+panelVoltage);
		updatePanel(device,panelVoltage,panelPower,day,cannotUpdate,now,sunRise,sunSet);
		cannotUpdate=updateBattery(device,panelVoltage,panelPower,day,cannotUpdate,now,sunRise,sunSet);
		updateLoad(device,day,cannotUpdate,now);
		updateWorkState(device);
		
		
		
	    
		
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
	
	private void updateLoad(String device,int day,int cannotUpdate,ZonedDateTime now) {
		System.out.println("Update Load Started");
		Map<String,String> data=new HashMap<>();
//			SunRiseSunSetCalc calc=new SunRiseSunSetCalc();
//			double x=service.getLatitude(device);
//			double y=service.getLongitude(device);
//			ZonedDateTime now=ZonedDateTime.now(ZoneId.of(region));
			
//			if(now.isAfter(sunSet))
//				sunRise=calc.getSunRise(x, y, 1);
//			else if(now.isBefore(sunRise))
//				sunSet=calc.getSunRise(x, y, -1);
			if(service.getManualTime(device)<=0) {
				if(day==0) {
					service.clearDayBurner(device);
					
//					if(loadOrChange==1&&service.getBatU100mv(device)>=batteryRechargeVoltage2) {
//						service.setLOADORCHANGE(device,0);
//						loadOrChange=0;
//					}
					//scheduleLogic
					if(cannotUpdate==2) {
						service.setLedLevel(device, 0,data);
					}else {
						ZonedDateTime sunSet=service.getSunSet(device);
					
							int time=service.getTimePeriod(device);
							if(time==0) {
								service.setLOADORCHANGE(device, 0);
								time=1;
							}
							long elapsedTime = Duration.between(sunSet,now).toMillis();
							long duration=service.getScheduleDuration(device,time);
							
							while(duration<elapsedTime) {
								
								if(duration==-1)
									break;
								time++;
								duration=service.getScheduleDuration(device,time);
							}
							
							if(duration==-1) {
								service.setTimePeriod(device, time);
								service.setLedLevel(device,0,data);
							}
							System.out.println("Time Period from Update Load:"+time);
							service.setTimePeriod(device,time);
							int level=service.getScheduleCurrent(device,time);
							System.out.println("Level:"+level);
						
							service.setLedLevel(device, level,data);
					}
					
				}
				else {
					
							service.setTimePeriod(device,0);
							service.setLedLevel(device, 0,data);
					}
					
				
			}
			else {
				if(cannotUpdate!=2) {
					service.setLedLevel(device, service.getManualPower(device),data);
				}
				else {
					service.setLedLevel(device, 0,data);
				}
//				service.updateManualTime(device,AutomateDatas.duration);
			}
		service.setUpdate(device, data);
	}

	private void updatePanel(String device,double panelVoltage,double panelPower,int day,int cannotUpdate,ZonedDateTime now,
			ZonedDateTime sunRise,ZonedDateTime sunSet) {
		System.out.println("Update Panel Started");
//		SunRiseSunSetCalc calc=new SunRiseSunSetCalc();
//		double x=service.getLatitude(device);
//		double y=service.getLongitude(device);
		Map<String,String> data=new HashMap<>();
		double panelVoltageLocal=panelVoltage;
		double panelPowerLocal=panelPower;
		double panelCurrentLocal=0;
		
	
//		System.out.println("Now:"+now);
//		System.out.println("sunRise:"+sunRise);
//		System.out.println("sunSet:"+sunSet);
		int loadOrChange=service.getLOADORCHANGE(device);
		if(day==0) {
			System.out.println("Night");
			service.setPanel(device,0,0,0,loadOrChange,data);
		}
		else {
//			System.out.println("loadOr Change:"+loadOrChange);
//			System.out.println("Cannot Update:"+service.getCannotUpdate(device));
			if(loadOrChange==1&&cannotUpdate!=1) {
				
				System.out.println("Charging");
				double temp=service.getPanelTemp(device);
				double panelEfficiency=(100-((temp-25)*0.4))/100;
				double maxPower=panelPowerLocal;
//				ZonedDateTime now=ZonedDateTime.now(ZoneId.of(region));

			
				
				panelPowerLocal=calculateSolarPower(now,sunRise,sunSet,maxPower)*panelEfficiency;
				System.out.println("Panel Power:"+panelPowerLocal);
				double sunlightFactor = panelPowerLocal / maxPower;
				panelVoltageLocal=calculatePanelVoltage(sunlightFactor,temp,panelVoltage);
				
				
				System.out.println("Panel Voltage:"+panelVoltageLocal);
				panelCurrentLocal=panelPowerLocal/(panelVoltageLocal);
				System.out.println("Panel Current:"+panelCurrentLocal);
				//BatteryCharge
				service.chargeFromPanel(device,panelPowerLocal,data);
				
					
			}
			else {
				panelPowerLocal=0;
				panelCurrentLocal=0;
			}
			service.setPanel(device, panelVoltageLocal, panelCurrentLocal, panelPowerLocal,loadOrChange,data);
		}
		service.setUpdate(device,data);

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
		        
		        return maxPower * ratio;
		    }

		    if (timeBeforeSunset.compareTo(rampDuration) <= 0) {
		        double ratio =
		                (double) timeBeforeSunset.toMillis() /
		                rampDuration.toMillis();
		        
		        return maxPower * ratio;
		    }
		    return maxPower;
	}
	public double calculatePanelVoltage(
	        double sunlightFactor,  
	        double temperature  ,
	        double panelVoltage
	) {

	    double IDEAL_TEMP = 25.0;
	    double BASE_VOLTAGE = panelVoltage;     
	    double TEMP_COEFF = -0.06;     
	    double tempDifference = temperature - IDEAL_TEMP;
	    double tempEffect = BASE_VOLTAGE * TEMP_COEFF * tempDifference;

	    double voltageWithTemp = BASE_VOLTAGE + tempEffect;

	   
	    double sunlightEffect = (1 - sunlightFactor) * 2.0; 

	    double finalVoltage = voltageWithTemp - sunlightEffect;

	   
	    if (finalVoltage < panelVoltage-5)
	        finalVoltage = panelVoltage-5;

	    return finalVoltage;
	}
	private int updateBattery(String device,double panelVoltage,double panelPower,int day,int cannotUpdate,ZonedDateTime now
			,ZonedDateTime sunRise,ZonedDateTime sunSet) {
		System.out.println("BatteryPercentage:"+service.getBatCapSoc(device));
		Map<String,String> data=new HashMap<>();
//		ZonedDateTime now=ZonedDateTime.now(ZoneId.of(region));
//		SunRiseSunSetCalc calc=new SunRiseSunSetCalc();
//		double x=service.getLatitude(device);
//		double y=service.getLongitude(device);
//		if (service.getLOADORCHANGE(device) == 0) {
		long manualTime=service.getManualTime(device);
		int manualPower=service.getManualPower(device);
		
		if(cannotUpdate!=2) {
			//DischargingState
			if(day==1) {
//				System.out.println("Manual Time for "+device+":"+service.getManualTime(device));
				if(manualTime<=0) {
					service.setLOADORCHANGE(device, 1);
					if(cannotUpdate==1) {
						System.out.println("Discharging Without Connection 1");
						cannotUpdate=service.dischargeWithoutConnection(device,cannotUpdate,data);
						if(cannotUpdate!=0)
							updatePanel(device,panelVoltage,panelPower,day,cannotUpdate,now,sunRise,sunSet);
					}
				}
				if(manualTime>0) {
						if(manualPower>0) {
							System.out.println("Hello From Manual Mode");
							service.setLOADORCHANGE(device, 0);
							cannotUpdate=service.dischargeWithConnection(device,cannotUpdate,data);
							if(cannotUpdate==2) {
								service.setLOADORCHANGE(device, 1);
								updatePanel(device,panelVoltage,panelPower,day,cannotUpdate,now,sunRise,sunSet);		
							}
						}
						else {
							service.setLOADORCHANGE(device, 1);
							if(cannotUpdate==1) {
								System.out.println("Discharging Without Connection 2");
								cannotUpdate=service.dischargeWithoutConnection(device,cannotUpdate,data);
								if(cannotUpdate!=2)
									updatePanel(device,panelVoltage,panelPower,day,cannotUpdate,now,sunRise,sunSet);
							}
							else {
								System.out.println("Manual with No Power");
								updatePanel(device,panelVoltage,panelPower,day,cannotUpdate,now,sunRise,sunSet);
							}
						}
				}
				
				
			}
			else {
				cannotUpdate=service.dischargeWithConnection(device,cannotUpdate,data);
				service.updateMinimumBatteryVoltageDuringNight(device);
			}
			
			
		} 
		else {
			if(day==1){
				service.setLOADORCHANGE(device, 1);
			}
			cannotUpdate=service.dischargeWithConnection(device,cannotUpdate,data);
		}
		if(manualTime>0) {
			service.updateManualTime(device,AutomateDatas.duration);
		}
		if(service.getLOADORCHANGE(device)==0) {
			service.updateDailyDischargingPower(device);
			service.updateDailyDischargingCurrent(device);
		}else{
			service.updateDailyChargingPower(device);
			service.updateDailyChargingCurrent(device);
		}
		service.updateBatTemp(device);
		if (now.getHour() == 0 &&
			    now.getMinute() == 0 ) {
			service.resetDailyCharingAndDischarging(device);
			service.incrementWorkingDay(device);
		}
		service.updateMaximumBatteryVoltageDuringDay(device);
		service.updateMaximumCurrentOfDay(device);
		service.updateMaximumPowerOfDay(device);
		long batCurCap=service.getBatCurEnergy(device);
		long batCap=service.getBatCapEnergy(device);
		System.out.println("Before cannotUpdate:"+cannotUpdate);
		cannotUpdate=service.updateBatVoltage("device/"+device, batCurCap, batCap,cannotUpdate,data);
		System.out.println("cannotUpdate Frm updateBattery:"+cannotUpdate);
		service.setUpdate(device, data);
		return cannotUpdate;
	}
}
