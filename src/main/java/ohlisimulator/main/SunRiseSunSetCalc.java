package ohlisimulator.main;
import org.shredzone.commons.suncalc.SunTimes;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;

public class SunRiseSunSetCalc {
	String region;
	private void loadConfig() {

		try {
			Properties props = new Properties();
			InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");

			props.load(input);
			region=props.getProperty("Region");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	{
		loadConfig();
	}
    public ZonedDateTime getSunSet(double x,double y,int day) {
    	LocalDate today = LocalDate.now();
    	LocalDate yesterday = today.minusDays(1);
    	LocalDate tomorrow = today.plusDays(1);
    	SunTimes times  = SunTimes.compute()
                .on(today)
                .at(x, y) // New York
                .timezone(ZoneId.of(region)) // your local time
                .execute();;
    	if(day==0) {
        times = SunTimes.compute()
                .on(today)
                .at(x, y) // New York
                .timezone(ZoneId.of(region)) // your local time
                .execute();
    	}else if(day==1) {
    		times = SunTimes.compute()
                    .on(tomorrow)
                    .at(x, y) // New York
                    .timezone(ZoneId.of(region)) // your local time
                    .execute();
    	}
    	else if(day==-1) {
    		times = SunTimes.compute()
                    .on(yesterday)
                    .at(x, y) // New York
                    .timezone(ZoneId.of(region)) // your local time
                    .execute();
    	}
        
        System.out.println("Sunrise: " + times.getRise());
        System.out.println("Sunset : " + times.getSet());
        return times.getSet();
       

    }
    
 public ZonedDateTime getSunRise(double x,double y,int day) {
    	
    	LocalDate today = LocalDate.now();
    	LocalDate yesterday = today.minusDays(1);
    	LocalDate tomorrow = today.plusDays(1);
    	SunTimes times  = SunTimes.compute()
                .on(today)
                .at(x, y) // New York
                .timezone(ZoneId.of(region)) // your local time
                .execute();;
    	if(day==0) {
        times = SunTimes.compute()
                .on(today)
                .at(x, y) // New York
                .timezone(ZoneId.of(region)) // your local time
                .execute();
    	}else if(day==1) {
    		times = SunTimes.compute()
                    .on(tomorrow)
                    .at(x, y) // New York
                    .timezone(ZoneId.of(region)) // your local time
                    .execute();
    	}
    	else if(day==-1) {
    		times = SunTimes.compute()
                    .on(yesterday)
                    .at(x, y) // New York
                    .timezone(ZoneId.of(region)) // your local time
                    .execute();
    	}
        
        System.out.println("Sunrise: " + times.getRise());
        System.out.println("Sunset : " + times.getSet());
        return times.getRise();
       

    }
}
