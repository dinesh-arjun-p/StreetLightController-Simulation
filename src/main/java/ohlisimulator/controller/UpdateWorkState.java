package ohlisimulator.controller;

import ohlisimulator.service.Service;

public interface UpdateWorkState {
	Service getService();
	
	public default void updateWorkState(String device) {
		Service service=getService();
		int workState=service.getWorkState(device);
		//System.out.println("WorkState:"+workState);
		int newWorkState=0;
		for(int i=15;i>=0;i--) {
			int fault=(workState>>i)&1;
			if(fault==0) {
			//	System.out.println("Fault 0 i "+i);
				if(i==0) {
					if(!service.isDeviceTempNormal(device)) 		
						newWorkState|=1;
				}
				else if(i==1) {
					if(service.isBatteryOverCurrent(device)) {
						newWorkState|=1;
					}
				}
				else if(i==2) {
					if(service.isBatteryOverDischargeVoltage(device))
						newWorkState|=1;
				}
				else if(i==3) {
					
					if(service.isBatteryOverVoltage(device)) {
						//System.out.println("Battery Over Voltage Workstate");
						newWorkState|=1;
					}
				}
				else if(i==4) {
					
					if(service.isBatteryUnderVoltage(device)) {
						//System.out.println("Battery Under Voltage Workstate");
						newWorkState|=1;
					}
				}
				else if(i==9) {
					if(service.isPanelUnderVoltage(device)) 
						newWorkState|=1;
				}
				else if(i==10) {
					if(service.isPanelOverVoltage(device))
						newWorkState|=1;
				}
				else if(i==11) {
					if(service.isDayBurner(device))
						newWorkState|=1;
				}
				else if(i==12) {
					if(service.isNightOutage(device))
						newWorkState|=1;
				}
				
			}
			else {
				//System.out.println("Fault 1 i "+i);
				if(i==0) {
					if(service.isDeviceTempNormal(device)) 	{	
						newWorkState&=~1;
					}
					else {
						newWorkState|=1;
					}
				}
				else if(i==1) {
					if(!service.isBatteryOverCurrent(device)) {
						newWorkState&=~1;
					}else {
						newWorkState|=1;
					}
				}
				else if(i==2) {
//					if(!service.isBatteryOverDischarge(device))
					if(!service.isBatteryOverDischargeVoltage(device))
						newWorkState&=~1;
					else {
						newWorkState|=1;
					}
				}
				else if(i==3) {
					if(!service.isBatteryOverVoltage(device))
						newWorkState&=~1;
					else {
						newWorkState|=1;
					}
				}
				else if(i==4) {
					if(!service.isBatteryUnderVoltage(device))
						newWorkState&=~1;
					else {
						newWorkState|=1;
					}
				}
				else if(i==9) {
					if(!service.isPanelUnderVoltage(device)) 
						newWorkState&=~1;
					else {
						newWorkState|=1;
					}
				}
				else if(i==10) {
					if(!service.isPanelOverVoltage(device))
						newWorkState&=~1;
					else {
						newWorkState|=1;
					}
				}
				else if(i==11) {
					if(!service.isDayBurner(device))
						newWorkState&=~1;
					else {
						newWorkState|=1;
					}
				}
				else if(i==12) {
					if(!service.isNightOutage(device))
						newWorkState&=~1;
					else {
						newWorkState|=1;
					}
				}
				
			}
			if(i!=0)
			newWorkState<<=1;
			//System.out.println("New Work State:"+newWorkState);
			
		}
		compareWorkState(device,newWorkState,workState);
		
	}

	public void compareWorkState(String device,int newWorkState, int workState);
}
